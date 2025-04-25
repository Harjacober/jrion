package com.kingjoe.orion.jrion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Environment {
    Map<String, Object> values = new HashMap<>();
    List<Object> indexedValues = new ArrayList<>(); //for storing local variables
    final Environment enclosing;

    public Environment() {
        this.enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    void define(String name, Object value) {
        values.put(name, value);
        indexedValues.add(value);
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            Object value = values.get(name.lexeme);
            if (value == null) {
                throw new RuntimeError(name, "Variable '" + name.lexeme + "' has not been initialized.");
            }
            return value;
        }
        if (enclosing != null) {
            return enclosing.get(name);
        }
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    public Object getAt(
            Token name,
            int distance
    ) {
        return ancestor(distance).values.get(name.lexeme);
    }

    public Object getAt(
            int distance,
            int index
    ) {
        return ancestor(distance).indexedValues.get(index);
    }

    void assignAt(Token name, Object value, int distance) {
        ancestor(distance).values.put(name.lexeme, value);
    }

    void assignAt(Token name, Object value, int distance, int index) {
        ancestor(distance).values.put(name.lexeme, value);
        ancestor(distance).indexedValues.set(index, value);
    }

    private Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.enclosing;
        }
        return environment;
    }
}
