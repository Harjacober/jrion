package com.kingjoe.orion.jrion.builtin;

import com.kingjoe.orion.jrion.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RionMap implements RionIndexable {
    private final Interpreter interpreter;
    private final Token token;

    private final Map<Object, Object> map = new HashMap<>();

    public RionMap(
            Interpreter interpreter,
            Token token,
            List<Object> keys,
            List<Object> values
    ) {
        this.interpreter = interpreter;
        this.token = token;
        createMap(keys, values);
    }

    @Override
    public Object get(
            Token token,
            Object key
    ) {
        if (!map.containsKey(key)) {
            throw new RuntimeError(token, "key: '" + interpreter.stringify(key) + "' not present in map.");
        }
        return map.get(key);
    }

    @Override
    public RionCallable getProperty(Token token, String property) {
        if ("containsKey".equals(property)) {
            return new RionCallable() {
                @Override
                public Object call(
                        Interpreter interpreter,
                        List<Object> arguments
                ) {
                    return map.containsKey(arguments.getFirst());
                }

                @Override
                public int getArity() {
                    return 1;
                }
            };
        }
        throw new RuntimeError(token, "property '" + property + "' does not exist in map.");
    }

    @Override
    public void set(
            Token token,
            Object key,
            Object value
    ) {
        addToMap(token, key, value);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        int i = 0;
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            builder.append(interpreter.stringify(key));
            builder.append(":").append(interpreter.stringify(value));
            if (i < map.size() - 1) {
                builder.append(", ");
            }
            i++;
        }
        builder.append("}");
        return builder.toString();
    }

    private void createMap(List<Object> keys, List<Object> values) {
        for (int i = 0; i < keys.size(); i++) {
            Object key = keys.get(i);
            Object value = values.get(i);
            addToMap(token, key, value);
        }
    }

    private void addToMap(
            Token token,
            Object key,
            Object value
    ) {
        if (key instanceof RionIndexable) {
            throw new RuntimeError(token, "provided key cannot be hashed.");
        }
        map.put(key, value);
    }


}
