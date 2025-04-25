package com.kingjoe.orion.jrion;

import java.util.*;

public class RionInstance {
    private final RionClass rionClass;
    private final Map<String, Object> privateFields = new HashMap<>();
    private final Map<String, Object> publicFields = new HashMap<>();

    public RionInstance(
            RionClass rionClass,
            Set<String> privateFields,
            Set<String> publicFields
    ) {
        this.rionClass = rionClass;
        declareFieldsOnInstance(privateFields, publicFields);
    }

    Object get(
            Environment environment,
            Token name
    ) {
        if (publicFields.containsKey(name.lexeme)) {
            return publicFields.get(name.lexeme);
        }

        if (privateFields.containsKey(name.lexeme)) {
            checkIfPrivateFieldAccessIsAllowed(environment, name);
            return privateFields.get(name.lexeme);
        }

        AbstractMap.SimpleImmutableEntry<RionFunction, RionClass> methodRes = rionClass.findMethod(environment, name);
        if (methodRes.getKey() != null) {
            return methodRes.getKey().bind(this, methodRes.getValue());
        }

        Optional<String> fieldInSuper = findPublicFieldInSuperClass(name.lexeme);
        if (fieldInSuper.isPresent()) {
            return null;
        }

        throw new RuntimeError(name, "Undefined property '" + name.lexeme + "' on '" + this + "'.");
    }

    Object set(
            Environment environment,
            Token name,
            Object value
    ) {
        if (publicFields.containsKey(name.lexeme)) {
            publicFields.put(name.lexeme, value);
            return this;
        }

        if (privateFields.containsKey(name.lexeme)) {
            checkIfPrivateFieldAccessIsAllowed(environment, name);
            privateFields.put(name.lexeme, value);
            return this;
        }

        Optional<String> fieldInSuper = findPublicFieldInSuperClass(name.lexeme);
        if (fieldInSuper.isPresent()) {
            publicFields.put(name.lexeme, value);
            return this;
        }

        throw new RuntimeError(name, "Undefined field '" + name.lexeme + "' on '" + this + "'.");
    }

    private void declareFieldsOnInstance(Set<String> privateFields, Set<String> publicFields) {
        for (String field : privateFields) {
            this.privateFields.put(field, null);
        }

        for (String field : publicFields) {
            this.publicFields.put(field, null);
        }
    }

    private void checkIfPrivateFieldAccessIsAllowed(
            Environment environment,
            Token name
    ) {
        RuntimeError error = new RuntimeError(name,
                                              "field '" + name.lexeme + "' is private in class '" + rionClass.getName() + "'.");
        try {
            // if environment contains 'this', then we check
            // if it is an instance of the class where the field is declared
            Object instance = environment.get(new Token(TokenType.THIS, "this", "this", 0));
            if (instance != this) {
                throw error;
            }
        } catch (RuntimeError e) {
            throw error;
        }
    }

    public RionClass getRionClass() {
        return rionClass;
    }

    private Optional<String> findPublicFieldInSuperClass(String name) {
        if (rionClass.getSuperClass() != null) {
            return rionClass.getSuperClass().findPublicField(name);
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "<instance of class '" + rionClass.getName() + "'>";
    }
}
