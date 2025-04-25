package com.kingjoe.orion.jrion.builtin;

import com.kingjoe.orion.jrion.Interpreter;
import com.kingjoe.orion.jrion.RionCallable;
import com.kingjoe.orion.jrion.RuntimeError;
import com.kingjoe.orion.jrion.Token;

import java.util.ArrayList;
import java.util.List;

public class RionArray implements RionIndexable {
    private final Interpreter interpreter;
    private final List<Object> elements;

    public RionArray(Interpreter interpreter, List<Object> elements) {
        this.interpreter = interpreter;
        this.elements = new ArrayList<>(elements);
    }

    public Object get(Token token, Object index) {
        if (!interpreter.isWholeNumber(index)) {
            throw new RuntimeError(token, "index should be a whole number");
        }
        int intIndex = ((Double) index).intValue();
        if (intIndex >= elements.size()) {
            throw new RuntimeError(token, String.format("index %s out of bound for size %s", intIndex, elements.size()));
        }
        return elements.get(intIndex);
    }

    public void set(Token token, Object index, Object value) {
        if (!interpreter.isWholeNumber(index)) {
            throw new RuntimeError(token, "index should be a whole number");
        }
        int intIndex = ((Double) index).intValue();
        if (intIndex >= 0 && intIndex < elements.size()) {
            elements.set(intIndex, value);
        } else {
            throw new RuntimeError(token, String.format("index %s out of bound for size %s. if you were attempting to append to the array, index should be the current size of the array", index, elements.size()));
        }
    }

    @Override
    public RionCallable getProperty(Token token, String property) {
        if ("length".equals(property)) {
            return new RionCallable() {
                @Override
                public Object call(
                        Interpreter interpreter,
                        List<Object> arguments
                ) {
                    return size().doubleValue();
                }

                @Override
                public int getArity() {
                    return 0;
                }
            };
        }
        throw new RuntimeError(token, "property '" + property + "' does not exist in array.");
    }

    public Integer size() {
        return elements.size();
    }

    public List<Object> getElements() {
        return elements;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < elements.size(); i++) {
            builder.append(interpreter.stringify(elements.get(i)));
            if (i < elements.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append("]");
        return builder.toString();
    }
}
