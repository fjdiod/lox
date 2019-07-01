package lox;

import java.util.List;

class LoxFunction implements LoxCallable {
    private final Expr.Function declaration;
    private final String name;
    private final Environment closure;
    private Object ret = null;
    LoxFunction(String name, Expr.Function declaration, Environment closure) {
        this.name = name;
        this.declaration = declaration;
        this.closure = closure;
    }
    @Override
    public String toString() {
        return "<fn " + name + ">";
    }
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);

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