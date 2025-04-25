package com.kingjoe.orion.jrion;

import java.util.List;

public class RionFunction implements RionCallable {
    private final Stmt.Function declaration;
    private final Environment closure;
    private final String type;

    public RionFunction(Stmt.Function declaration,
                        Environment closure,
                        String type
    ) {
        this.declaration = declaration;
        this.closure = closure;
        this.type = type;
    }

    RionFunction bind(RionInstance rionInstance, RionClass superClass) {
        Environment environment = new Environment(closure);
        environment.define("this", rionInstance);
        if (superClass != null) {
            environment.define("super", superClass);
        }
        return new RionFunction(declaration, environment, "method");
    }

    @Override
    public Object call(
            Interpreter interpreter,
            List<Object> arguments
    ) {
        Environment environment = new Environment(closure);

        for (int i = 0; i < declaration.parameters.size(); i++) {
            environment.define(declaration.parameters.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock((Stmt.Block) declaration.body, new Environment(environment));
        } catch (Signal.Return r) {
            return r.value;
        }
        return null;
    }

    @Override
    public int getArity() {
        return declaration.parameters.size();
    }

    @Override
    public String toString() {
        return "<" + type + " " + declaration.name.lexeme + ">";
    }
}
