package lox;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import static lox.TokenType.*;


/*
program        → declaration* EOF;
declaration    → varDecl | statement;
varDecl        → "var" IDENTIFIER ( "=" expression )? ";" ;
statement      → exprStatement | printStatemt | block | ifStatement | whileStatement | jumpStatement;
jumpStatement  → "break" | "continue";
whileStatement → "while" "(" expression ")" statement ;
ifStatement    → "if" "(" expression ")" statement ("elif" "(" expression ")" statement)* ("else" statement)? ;
block          → "{" declaration "}"
exprStatement  → expression ";" ;
printStatement → "print" expression ";" ;
expression     → assignment ("," assignment)*;
assignment     → IDENTIFIER "=" assignment | logic_or;
logic_or       → logic_and ("or" logic_and)* ;
logic_and      → equality ("and" equality)* ;
equality       → tern ( ( "!=" | "==" ) tern )* ;
tern           → comparison "?" comparison ":" tern
comparison     → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
addition       → multiplication ( ( "-" | "+" ) multiplication )* ;
multiplication → unary ( ( "/" | "*" ) unary )* ;
unary          → ( "!" | "-" ) unary
| primary ;
primary        → NUMBER | STRING | "false" | "true" | "nil"
| "(" expression ")" | IDENTIFIER ;

1 > 2 ? 0 : 1
1 > 2 ? 1 ? -1 : 0
*/

/*
TODO:
> < not working [\/]
 */
class Parser {
    private static class ParseError extends RuntimeException {}
    private final List<Token> tokens;
    private int current = 0;

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();


        while (!isAtEnd()) {
            statements.add(declaration(null));
        }
        return statements;

    }

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Stmt declaration(Stmt parent) {
        try {
            if (match(VAR)) {
                return varDecl();
            }
            return statement(parent);
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt varDecl() {
        Token name = consume(IDENTIFIER, "Expect variable name.");
        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }
        consume(SEMICOLON, "Expect ;.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement(Stmt parent) {
        if (match(FOR)) {
            return forStatement(parent);
        }
        if (match(PRINT)) {
            return printStatement(parent);
        }
        if (match(IF)) {
            return ifStatement(parent);
        }
        if (match(WHILE)) {
            return whileStatement(parent);
        }
        if (match(BREAK, CONTINUE)) {
            if (parent == null) throw error(previous(), "'break' without loop");
            //System.out.println(parent);
            Stmt.Jump jump = new Stmt.Jump(previous(), parent);
            consume(SEMICOLON, "expected ';'");
            return jump;
        }
        if (match(LEFT_BRACE)) {
            return new Stmt.Block(block(parent));
        }
        return expressionStatement(parent);
        //consume(SEMICOLON, "Expect ';' after stetements.");
    }

    private Stmt climbJump(Stmt parent) {
        if (parent instanceof Stmt.While) {
            return parent;
        }
        if (parent == null) {
            return null;
        }
        return climbJump(parent.parent);
    }

    private Stmt forStatement(Stmt parent) {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Stmt initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDecl();
        } else {
            initializer = expressionStatement(parent);
        }
        Expr condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after loop condition.");

        Expr increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.");
        Stmt.While whileStmt = new Stmt.While(null, null);
        Stmt body = statement(whileStmt);
        if (increment != null) {
            body = new Stmt.Block(Arrays.asList(
                    body,
                    new Stmt.Expression(increment, false)));
        }
        if (condition == null) condition = new Expr.Literal(true);
        body = new Stmt.While(condition, body);
        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }
        return body;
    }

    private Stmt whileStatement(Stmt parent) {
        Stmt.While whileStmt = new Stmt.While(null, null);
        whileStmt.parent = parent;
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "excpected )");
        Stmt body = statement(whileStmt);
        whileStmt.condition = condition;
        whileStmt.body = body;
        return whileStmt;
    }

    private Stmt ifStatement(Stmt parent) {
        Stmt elseBranch = null;
        //System.out.println("========IF========");
        //System.out.println(parent);
        //System.out.println("==================");
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect '(' after 'if'.");
        Stmt thenBranch = statement(parent);
        if (match(ELSE)) {
            elseBranch = statement(parent);
        }
        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private List<Stmt> block(Stmt parent) {
        List<Stmt> statements = new ArrayList<>();
        while(!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration(parent));
        }
        consume(RIGHT_BRACE, "expected }");
        return statements;
    }

    private Stmt expressionStatement(Stmt parent) {
        Expr expr = expression();
        //consume(SEMICOLON, "Expect ';' after stetements.");
        if(peek().type == EOF) return new Stmt.Expression(expr, true);
        consume(SEMICOLON, "Expect ';' after stetements.");
        return new Stmt.Expression(expr, false);
    }

    private Stmt printStatement(Stmt parent) {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after stetements.");
        return new Stmt.Print(expr);
        //System.out.println(expr)
    }

    private Expr expression() {
        Expr expr = assignment();
        while (match(COMMA)) {
            Token comma = previous();
            expr = assignment();
        }
        return expr;
    }

    private Expr assignment() {
        Expr expr = logicOr();
        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }
            error(equals, "Invalid assignment target.");
        }
        return expr;
    }

    private Expr logicOr() {
        Expr expr = logicAnd();
        while (match(OR)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr logicAnd() {
        Expr expr = equality();
        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr equality() {
        Expr expr = tern();
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = tern();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr tern() {
        Expr expr = comparison();
        if (match(QUESTION)) {
            Token operator = previous();
            Expr right = comparison();
            if (match(COLON)) {
                operator = previous();
                Expr mostRight = tern();
                expr = new Expr.Ternary(expr, right, mostRight);
            } else {
                throw error(peek(), "Expect :.");
            }
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = addition();
        while (match(GREATER, GREATER_EQUAL, GREATER_EQUAL, LESS_EQUAL, LESSER)) {
            Token operator = previous();
            Expr right = addition();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr addition() {
        Expr expr = multiplication();
        while (match(PLUS, MINUS)) {
            Token operator = previous();
            Expr right = multiplication();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr multiplication() {
        Expr expr = unary();
        while (match(STAR, SLASH)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current-1);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}