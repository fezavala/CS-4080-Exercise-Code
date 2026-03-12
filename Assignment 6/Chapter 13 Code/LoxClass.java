package com.craftinginterpreters.lox;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

class LoxClass implements LoxCallable{
    final String name;
    final LoxClass superclass;
    private final Map<String, LoxFunction> methods;

    LoxClass(String name, LoxClass superclass, Map<String, LoxFunction> methods) {
        this.superclass = superclass;
        this.name = name;
        this.methods = methods;
    }

    LoxFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        if (superclass != null) {
            return superclass.findMethod(name);
        }

        return null;
    }

    // Finds the method chain from bottom to top and returns a top to bottom method chain
    List<LoxFunction> findMethodChain(String name) {
        ArrayList<LoxFunction> chain = new ArrayList<>();

        // Collect superclasses methods
        LoxClass currentSuperClass = superclass;
        while (currentSuperClass != null) {
            chain.addAll(currentSuperClass.findMethodChain(name));
            currentSuperClass = currentSuperClass.superclass;
        }

        // Add current classes methods
        LoxFunction here = methods.get(name);
        if (here != null) chain.add(here);

        return chain;
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
}
