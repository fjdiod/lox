package lox;

import java.util.List;

abstract class Stmt {
interface Visitor<R> {
R visitPrintStmt(Print stmt);
R visitExpressionStmt(Expression stmt);

}
static class Print extends Stmt {
Print(Expr expression) {
this.expression = expression;

}

<R> R accept(Visitor<R> visitor) {
return visitor.visitPrintStmt(this);
}

final Expr expression;


}

static class Expression extends Stmt {
Expression(Expr expression) {
this.expression = expression;

}

<R> R accept(Visitor<R> visitor) {
return visitor.visitExpressionStmt(this);
}

final Expr expression;


}



abstract <R> R accept(Visitor<R> visitor);
}