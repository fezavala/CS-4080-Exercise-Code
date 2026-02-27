package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

class Environment {
    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();
    // To detect if a variable has been initialized, a parallel Hashmap is used
    // to track if assignment has occurred.
    private final Map<String, Boolean> initialized = new HashMap<>();

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            // If the variable is not initialized, a runtime error is thrown
            if (!initialized.get(name.lexeme)) {
                throw new RuntimeError(name,
                        "Unassigned variable '" + name.lexeme + "'.");
            }
            return values.get(name.lexeme);
        }

        if (enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            initialized.put(name.lexeme, true);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }

    void define(String name, Object value) {
        values.put(name, value);
        initialized.put(name, true);
    }

    // New define overload method that does not initialize the variable
    void define(String name) {
        values.put(name, null);
        initialized.put(name, false);
    }
}
