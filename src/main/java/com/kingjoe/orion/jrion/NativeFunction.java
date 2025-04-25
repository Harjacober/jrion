package com.kingjoe.orion.jrion;

import java.util.List;

public class NativeFunction {

    public static void load(Environment globals) {
        time(globals);
        print(globals);
        println(globals);
    }

    private static void time(Environment globals) {
        globals.define("time", new RionCallable() {

            @Override
            public Object call(
                    Interpreter interpreter,
                    List<Object> arguments
            ) {
                return (double) System.currentTimeMillis();
            }

            @Override
            public int getArity() {
                return 0;
            }

            @Override
            public String toString() {
                return "<native fn time>";
            }
        });
    }

    private static void print(Environment globals) {
        globals.define("print", new RionCallable() {

            @Override
            public Object call(
                    Interpreter interpreter,
                    List<Object> arguments
            ) {
                System.out.print(interpreter.prettyPrint(arguments.getFirst()));
                return null;
            }

            @Override
            public int getArity() {
                return 1;
            }

            @Override
            public String toString() {
                return "<native fn print>";
            }
        });
    }

    private static void println(Environment globals) {
        globals.define("println", new RionCallable() {

            @Override
            public Object call(
                    Interpreter interpreter,
                    List<Object> arguments
            ) {
                System.out.println(interpreter.prettyPrint(arguments.getFirst()));
                return null;
            }

            @Override
            public int getArity() {
                return 1;
            }

            @Override
            public String toString() {
                return "<native fn print>";
            }
        });
    }
}
