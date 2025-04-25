package com.kingjoe.orion.jrion;

import java.util.List;

public interface RionCallable {
    Object call(Interpreter interpreter, List<Object> arguments);
    int getArity();
}
