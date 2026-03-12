package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

class LoxInstance {
    private LoxClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    LoxInstance(LoxClass klass) {
        this.klass = klass;
    }

    Object get(Token name) {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        // Now return the method chain if available
        List<LoxFunction> chain = klass.findMethodChain(name.lexeme);
        if (!chain.isEmpty()) {
            return new LoxMethodChain(chain, this, 0);
        }

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
