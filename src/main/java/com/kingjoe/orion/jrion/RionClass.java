package com.kingjoe.orion.jrion;

import java.util.*;

public class RionClass implements RionCallable {
    private final Token name;
    private final RionClass superClass;
    private final Set<String> privateFields = new HashSet<>();
    private final Set<String> publicFields = new HashSet<>();
    private final Map<String, RionFunction> privateMethods = new HashMap<>();
    private final Map<String, RionFunction> publicMethods = new HashMap<>();

    public RionClass(Token name,
                     RionClass superClass,
                     Set<String> declaredFields,
                     Map<String, RionFunction> methods
    ) {
        this.name = name;
        this.superClass = superClass;
        storeFields(declaredFields);
        defineClassMethods(methods);
    }

    @Override
    public String toString() {
        return "<class '" + name.lexeme +  "'>";
    }

    @Override
    public Object call(
            Interpreter interpreter,
            List<Object> arguments
    ) {
        RionInstance rionInstance = new RionInstance(this, privateFields, publicFields);

        Optional<RionFunction> initializer = getClassInitializer();
        initializer.ifPresent(rionFunction -> rionFunction.bind(rionInstance, this.superClass).call(interpreter, arguments));

        return rionInstance;
    }

    @Override
    public int getArity() {
        Optional<RionFunction> initializer = getClassInitializer();
        return initializer.map(RionFunction::getArity).orElse(0);
    }

    public String getName() {
        return name.lexeme;
    }

    /*
     * return any method defined in the class/super class except the initializer
     */
    public AbstractMap.SimpleImmutableEntry<RionFunction, RionClass> findMethod(Environment environment, Token name) {
        if (this.name.lexeme.equals(name.lexeme)) {
            return new AbstractMap.SimpleImmutableEntry<>(null, null);
        }

        if (publicMethods.containsKey(name.lexeme)) {
            return new AbstractMap.SimpleImmutableEntry<>(publicMethods.get(name.lexeme), this.superClass);
        }

        if (privateMethods.containsKey(name.lexeme)) {
            RuntimeError error = new RuntimeError(name,
                                                  "method '" + name.lexeme + "' is private in class '" + this.name.lexeme + "'.");
            try {
                // if environment contains 'this', then we check
                // if it is an instance of the class where the method is defined.
                RionInstance instance =  (RionInstance) environment.get(new Token(TokenType.THIS, "this", "this", 0));
                if (instance.getRionClass() != this) {
                    throw error;
                }
            } catch (RuntimeError e) {
                throw error;
            }
            return new AbstractMap.SimpleImmutableEntry<>(privateMethods.get(name.lexeme), this.superClass);
        }

        if (superClass != null) {
            return superClass.findMethod(environment, name);
        }

        return new AbstractMap.SimpleImmutableEntry<>(null, null);
    }

    public Optional<RionFunction> findMethodInternal(String name) {
        //don't consider a class initializer as a method, hence return nothing when name equals the initializer name.
        if (this.name.lexeme.equals(name)) {
            return Optional.empty();
        }

        if (publicMethods.containsKey(name)) {
            return Optional.of(publicMethods.get(name));
        }

        return Optional.ofNullable(privateMethods.get(name));
    }

    public Optional<String> findPrivateField(String name) {
        if (privateFields.contains(name)) {
            return Optional.of(name);
        }
        return Optional.empty();
    }

    public Optional<String> findPublicField(String name) {
        if (publicFields.contains(name)) {
            return Optional.of(name);
        }
        return Optional.empty();
    }

    public Optional<RionFunction> getClassInitializer() {
        return Optional.ofNullable(publicMethods.get(name.lexeme));
    }
    public RionClass getSuperClass() {
        return superClass;
    }

    private void defineClassMethods(Map<String, RionFunction> methods) {
        for (Map.Entry<String, RionFunction> entry : methods.entrySet()) {
            if (entry.getKey().startsWith("_")) {
                privateMethods.put(entry.getKey().substring(1), entry.getValue());
            } else {
                publicMethods.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private void storeFields(Set<String> declaredFields) {
        for (String field : declaredFields) {
            if (field.startsWith("_")) {
                privateFields.add(field.substring(1));
            } else {
                publicFields.add(field);
            }
        }
    }
}
