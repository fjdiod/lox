package lox;

import java.util.List;

abstract class Stmt {
interface Visitor<R> {
R visitPrintStmt(Print stmt);
R visitVarStmt(Var stmt);
R visitExpressionStmt(Expression stmt);
R visitBlockStmt(Block stmt);

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

static class Var extends Stmt {
Var(Token name, Expr initializer) {
this.name = name;
this.initializer = initializer;

}

<R> R accept(Visitor<R> visitor) {
return visitor.visitVarStmt(this);
}

final Token name;
final Expr initializer;


}

static class Expression extends Stmt {
Expression(Expr expression, boolean print) {
this.expression = expression;
this.print = print;

}

<R> R accept(Visitor<R> visitor) {
return visitor.visitExpressionStmt(this);
}

final Expr expression;
final boolean print;


}

static class Block extends Stmt {
Block(List<Stmt> statements) {
this.statements = statements;

}

<R> R accept(Visitor<R> visitor) {
return visitor.visitBlockStmt(this);
}

final List<Stmt> statements;


}



abstract <R> R accept(Visitor<R> visitor);
}