package com.craftinginterpreters.lox;

import java.util.List;
import java.util.Map;

class LoxClass extends LoxInstance implements LoxCallable{
    final String name;
    private final Map<String, LoxFunction> methods;
    private final Map<String, LoxFunction> staticMethods; // New static methods map
    private final Map<String, LoxFunction> getterMethods; // New getter methods map

    LoxClass(String name, Map<String, LoxFunction> methods, Map<String, LoxFunction> staticMethods, Map<String, LoxFunction> getterMethods) {
        super(null);
        this.name = name;
        this.methods = methods;
        this.staticMethods = staticMethods;
        this.getterMethods = getterMethods;
        this.klass = this;
    }

    LoxFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        return null;
    }

    // Search through static methods
    LoxFunction findStaticMethod(String name) {
        if (staticMethods.containsKey(name)) {
            return staticMethods.get(name);
        }

        return null;
    }

    // Search through getter methods
    LoxFunction findGetterMethod(String name) {
        if (getterMethods.containsKey(name)) {
            return getterMethods.get(name);
        }

        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Object call(Interpreter interpreter,
                       List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initializer = findMethod("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }

        return instance;
    }

    @Override
    public int arity() {
        LoxFunction initializer = findMethod("init");
        if (initializer == null) return 0;
        return initializer.arity();
    }

    // Make it an error to assign fields to a class object
    @Override
    void set(Token name, Object value) {
        throw new RuntimeError(name, "Cannot assign value to class object.");
    }
}
