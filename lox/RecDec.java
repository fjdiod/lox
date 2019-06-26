package lox;

import java.util.List;
import java.util.ArrayList;

import static lox.TokenType.*;


/*
program        → declaration* EOF;
declaration    → varDecl | statement;
varDecl        → "var" IDENTIFIER ( "=" expression )? ";" ;
statement      → exprStatement | printStatemt ;
exprStatement  → expression ";" ;
printStatement -> "print" expression ";" ;
expression     → equality ("," equality)* ;
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

        try {
            while (!isAtEnd()) {
                statements.add(statement());
            }
            return statements;
        } catch {

        }

    }

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private void program() {
        while (peek().type != EOF) {
             statement();
        }
    }

    private Stmt statement() {
        if (match(PRINT)) {
            return printStatement();
        }
        return expressionStatement();
        //consume(SEMICOLON, "Expect ';' after stetements.");
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after stetements.");
        return new Stmt.Expression(expr);
        //System.out.println(expr)
    }

    private Stmt printStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after stetements.");
        return new Stmt.Print(expr);
        //System.out.println(expr)
    }

    private Expr expression() {
        Expr expr = equality();
        while (match(COMMA)) {
            Token comma = previous();
            expr = equality();
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