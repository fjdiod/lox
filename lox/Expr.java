package lox;

import java.util.List;

abstract class Expr {
interface Visitor<R> {
R visitUnaryExpr(Unary expr);
R visitBinaryExpr(Binary expr);
R visitLiteralExpr(Literal expr);
R visitGroupingExpr(Grouping expr);
R visitTernaryExpr(Ternary expr);

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

static class Literal extends Expr {
Literal(Object value) {
this.value = value;

}

<R> R accept(Visitor<R> visitor) {
return visitor.visitLiteralExpr(this);
}

final Object value;


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