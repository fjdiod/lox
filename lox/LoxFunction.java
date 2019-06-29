package lox;

import java.util.List;

class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    private Object ret = null;
    LoxFunction(Stmt.Function declaration) {
        this.declaration = declaration;
    }
    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(interpreter.globals);

        interpreter.ret = null;
        for (int i = 0; i < declaration.args.size(); i++) {
            environment.define(declaration.args.get(i).lexeme,
                    arguments.get(i));
            //System.out.print("body " + this + " " + declaration.body.get(i));

        }

        interpreter.executeBlock(declaration.body, environment);
        interpreter.shouldReturn = false;
        //System.out.println("call " + this + "and return" + interpreter.ret);
        Object ret = interpreter.ret;
        return ret;
    }
    public int arity() {
        return declaration.args.size();
    }
}