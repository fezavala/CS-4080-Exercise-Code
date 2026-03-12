package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

class LoxInstance {
    protected LoxClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    LoxInstance(LoxClass klass) {
        this.klass = klass;
    }

    Object get(Token name, Interpreter interpreter) {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        // Start by checking getters
        LoxFunction getter = klass.findGetterMethod(name.lexeme);
        // Return the methods result instead of the function by calling it.
        if (getter != null) return getter.bind(this).call(interpreter, null);

        LoxFunction method = klass.findMethod(name.lexeme);
        if (method != null) return method.bind(this);

        // Check static methods if regular methods or getters not available
        LoxFunction staticMethod = klass.findStaticMethod(name.lexeme);
        if (staticMethod != null) return staticMethod.bind(this);

        throw new RuntimeError(name,
                "Undefined property '" + name.lexeme + "'.");
    }

    void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }

    @Override
    public String toString() {
        return klass.name + " instance";
    }
}
