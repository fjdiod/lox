package lox;

import java.util.List;

abstract class Expr {
interface Visitor<R> {
R visitUnaryExpr(Unary expr);
R visitBinaryExpr(Binary expr);
R visitLiteralExpr(Literal expr);
R visitTernaryExpr(Ternary expr);
R visitVariableExpr(Variable expr);
R visitAssignExpr(Assign expr);
R visitLogicalExpr(Logical expr);
R visitGroupingExpr(Grouping expr);
R visitCallExpr(Call expr);
R visitFunctionExpr(Function expr);
}
static class Unary extends Expr {
Unary(Token operator, Expr right) {
this.operator = operator;
this.right = right;

}

<R> R accept(Visitor<R> visitor) {
return visitor.visitUnaryExpr(this);
}

final Token operator;
final Expr right;


}

static class Binary extends Expr {
Binary(Expr left, Token operator, Expr right) {
this.left = left;
this.operator = operator;
this.right = right;

}

<R> R accept(Visitor<R> visitor) {
return visitor.visitBinaryExpr(this);
}

final Expr left;
final Token operator;
final Expr right;


}

    static class Call extends Expr {
        Call(Expr callee, Token paren, List<Expr> args) {
            this.callee = callee;
            this.paren = paren;
            this.args = args;

        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }

        final Expr callee;
        final Token paren;
        final List<Expr> args;


    }

    static class Function extends Expr {
        Function(List<Token> args, List<Stmt> body) {
            this.args = args;
            this.body = body;

        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionExpr(this);
        }

        final List<Token> args;
        final List<Stmt> body;


    }

static class Literal extends Expr {
Literal(Object value) {
this.value = value;

}

<R> R accept(Visitor<R> visitor) {
return visitor.visitLiteralExpr(this);
}

final Object value;


}

static class Ternary extends Expr {
Ternary(Expr left, Expr right, Expr mostRight) {
this.left = left;
this.right = right;
this.mostRight = mostRight;

}

<R> R accept(Visitor<R> visitor) {
return visitor.visitTernaryExpr(this);
}

final Expr left;
final Expr right;
final Expr mostRight;


}

static class Variable extends Expr {
Variable(Token name) {
this.name = name;

}

<R> R accept(Visitor<R> visitor) {
return visitor.visitVariableExpr(this);
}

final Token name;


}

static class Assign extends Expr {
Assign(Token name, Expr value) {
this.name = name;
this.value = value;

}

<R> R accept(Visitor<R> visitor) {
return visitor.visitAssignExpr(this);
}

final Token name;
final Expr value;


}

static class Logical extends Expr {
Logical(Expr left, Token operator, Expr right) {
this.left = left;
this.operator = operator;
this.right = right;

}

<R> R accept(Visitor<R> visitor) {
return visitor.visitLogicalExpr(this);
}

final Expr left;
final Token operator;
final Expr right;


}

static class Grouping extends Expr {
Grouping(Expr expression) {
this.expression = expression;

}

<R> R accept(Visitor<R> visitor) {
return visitor.visitGroupingExpr(this);
}

final Expr expression;


}



abstract <R> R accept(Visitor<R> visitor);
}