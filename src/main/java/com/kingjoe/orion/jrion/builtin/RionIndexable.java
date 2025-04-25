package com.kingjoe.orion.jrion.builtin;

import com.kingjoe.orion.jrion.RionCallable;
import com.kingjoe.orion.jrion.Token;

public interface RionIndexable {
    Object get(Token token, Object index);
    void set(Token token, Object index, Object value);
    RionCallable getProperty(Token token, String property);
}
