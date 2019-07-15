all: lox scanner interpreter expr stmt parser env token token_type callable function resolver.class

lox: lox/Lox.java
	javac lox/Lox.java

scanner: lox/Scanner.java
	javac lox/Scanner.java

interpreter: lox/Interpreter.java
	javac lox/Interpreter.java

expr: lox/Expr.java
	javac lox/Expr.java

stmt: lox/Stmt.java
	javac lox/Stmt.java

parser: lox/RecDec.java
	javac lox/RecDec.java

env: lox/Environment.java
	javac lox/Environment.java

token: lox/Token.java
	javac lox/Token.java

token_type: lox/TokenType.java
	javac lox/TokenType.java

callable: lox/LoxCallable.java
	javac lox/LoxCallable.java

function: lox/LoxFunction.java
	javac lox/LoxFunction.java

resolver.class: lox/Resolver.java
	javac lox/Resolver.java
