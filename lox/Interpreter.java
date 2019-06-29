package lox;

import java.util.List;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void>{
    private Environment environment = new Environment();
    static boolean shouldBreak, shouldContinue;

    void interpret(List<Stmt> statements) {
        try {
            this.shouldBreak = false;
            this.shouldContinue = false;
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        Object res = evaluate(stmt.expression);
        if (stmt.print) System.out.println(stringify(res));
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        Object condition = evaluate(stmt.condition);
        if (isBool(condition)) execute(stmt.thenBranch);
        else if (stmt.elseBranch != null){
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isBool(evaluate(stmt.condition))) {
            execute(stmt.body);
            if (this.shouldBreak) {
                break;
            }
            if (this.shouldContinue) {
                this.shouldContinue = false;
                continue;
            }
        }
        this.shouldBreak = false;
        this.shouldContinue = false;
        return null;
    }

    @Override
    public Void visitJumpStmt(Stmt.Jump stmt) {
        if (stmt.jump.type == TokenType.BREAK) this.shouldBreak = true;
        if (stmt.jump.type == TokenType.CONTINUE) this.shouldContinue = true;
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                checkUnaryOperand(expr.operator, right);
                return -(double)right;
            case BANG:
                return !isBool(right);
        }

        // Unreachable.
        return null;
    }

    private boolean isBool(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        if (expr.operator.type == TokenType.OR) {
            if (isBool(left)) return left;
        } else {
            if (!isBool(left)) return left;
        }
        return right;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                checkBinaryOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double)
                    return (double)left + (double)right;
                if (left instanceof String && right instanceof String)
                    return (String)left + (String)right;
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            case STAR:
                checkBinaryOperands(expr.operator, left, right);
                return (double)left * (double)right;
            case SLASH:
                checkBinaryOperands(expr.operator, left, right);
                return (double)left / (double)right;
            case GREATER:
                checkBinaryOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkBinaryOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESSER:
                checkBinaryOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkBinaryOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case BANG_EQUAL:
                return !isEqual(left, right);
        }
        // Unreachable.
        return null;
    }

    private boolean isEqual(Object a, Object b) {
        // nil is only equal to nil.
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        Object mostRight = evaluate(expr.mostRight);
        if (right instanceof Double && mostRight instanceof Double) {
            return isBool(left) ? (double)right: (double)mostRight;
        }
        if (right instanceof String && mostRight instanceof String) {
            return isBool(left) ? (String)right: (String)mostRight;
        }
        return null;
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        if (!this.shouldBreak && !this.shouldContinue) stmt.accept(this);
    }

    private void executeBlock(List<Stmt> statements, Environment env) {
        Environment previous = this.environment;
        try {
            this.environment = env;
            for (Stmt stmt : statements) {

                if (this.shouldBreak || this.shouldContinue) break;
                stmt.accept(this);
            }
        } finally {
            this.environment = previous;
        }
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        // Hack. Work around Java adding ".0" to integer-valued doubles.
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    private void checkUnaryOperand(Token operator, Object right) {
        if (right instanceof Double) return;
        throw new RuntimeError(operator, "operand must be double");
    }
    private void checkBinaryOperands(Token operator, Object left, Object right) {
        if (right instanceof Double && left instanceof Double) return;
        throw new RuntimeError(operator, "operands must be double");
    }
}