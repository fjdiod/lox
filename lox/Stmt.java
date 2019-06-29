package lox;

import java.util.List;
import java.util.ArrayList;

abstract class Stmt {
interface Visitor<R> {
R visitJumpStmt(Jump stmt);
R visitVarStmt(Var stmt);
R visitWhileStmt(While stmt);
R visitPrintStmt(Print stmt);
R visitExpressionStmt(Expression stmt);
R visitBlockStmt(Block stmt);
R visitIfStmt(If stmt);
R visitFunctionStmt(Function stmt);
}

Stmt parent;

static class Jump extends Stmt {
Jump(Token jump) {
this.jump = jump;

}

<R> R accept(Visitor<R> visitor) {
return visitor.visitJumpStmt(this);
}

Token jump;
Stmt loop;
Stmt parent;


}

static class Var extends Stmt {
Var(Token name, Expr initializer) {
this.name = name;
this.initializer = initializer;

}

<R> R accept(Visitor<R> visitor) {
return visitor.visitVarStmt(this);
}

Token name;
Expr initializer;
Stmt parent;


}

static class While extends Stmt {
While(Expr condition, Stmt body) {
this.condition = condition;
if (body != null) body.parent = this;
this.body = body;


}

<R> R accept(Visitor<R> visitor) {
return visitor.visitWhileStmt(this);
}

Expr condition;
Stmt body;
Stmt parent;
boolean shouldBreak;


}

static class Print extends Stmt {
Print(Expr expression) {
this.expression = expression;

}

<R> R accept(Visitor<R> visitor) {
return visitor.visitPrintStmt(this);
}

Expr expression;
Stmt parent;


}

    static class Function extends Stmt {
        Function(Token name, List<Token> args, List<Stmt> body) {
            this.name = name;
            this.args = args;
            this.body = body;

        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionStmt(this);
        }

        Token name;
        List<Token> args;
        List<Stmt> body;
    }

static class Expression extends Stmt {
Expression(Expr expression, boolean print) {
this.expression = expression;
this.print = print;


}

<R> R accept(Visitor<R> visitor) {
return visitor.visitExpressionStmt(this);
}

Expr expression;
boolean print;
Stmt parent;


}

static class Block extends Stmt {
Block(List<Stmt> statements) {
    List<Stmt> tmp = new ArrayList<>();
    for (Stmt stmt : statements) {
        stmt.parent = this;
        tmp.add(stmt);
    }
this.statements = tmp;

}

<R> R accept(Visitor<R> visitor) {
return visitor.visitBlockStmt(this);
}

List<Stmt> statements;
Stmt parent;


}

static class If extends Stmt {
If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
this.condition = condition;
    thenBranch.parent = this;
    if (elseBranch != null) elseBranch.parent = this;
this.thenBranch = thenBranch;
this.elseBranch = elseBranch;

}

<R> R accept(Visitor<R> visitor) {
return visitor.visitIfStmt(this);
}

Expr condition;
Stmt thenBranch;
Stmt elseBranch;
Stmt parent;


}



abstract <R> R accept(Visitor<R> visitor);
}