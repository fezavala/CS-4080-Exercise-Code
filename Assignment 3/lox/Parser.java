package com.craftinginterpreters.lox;

import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

/*
Updated Grammar for exercises

expression     → comma ;
comma          → ternary ("," ternary)* ;
ternary        → ( equality "?" expression ":" )* ( ternary | equality) ;
equality       → comparison ( ( "!=" | "==" ) comparison )* ;
comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term           → factor ( ( "-" | "+" ) factor )* ;
factor         → unary ( ( "/" | "*" ) unary )* ;
unary          → ( "!" | "-" ) unary
               | primary ;
primary        → NUMBER | STRING | "true" | "false" | "nil"
               | "(" expression ")" ;

The comma has the lowest associativity level in c.
The ternary operator has lower associativity than the comparator operators,
but higher associativity than assignment and comma. In between ? and :, any expression
is allowed. It is treated as having parenthesis, so it is internally grouped.
 */

class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    // Modified to go-to comma() first instead of equality
    private Expr expression() {
        return comma();
    }

    // New comma parsing code
    private Expr comma() {
        Expr expr = ternary();
        while (match(COMMA)) {
            Token operator = previous();
            Expr right = ternary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // New ternary parsing code
    private Expr ternary() {
        Expr expr = equality();

        // Detect and handle ternary
        while (match(QUESTION)) {
            Token operator_1 = previous();
            // C-style ternary expressions treat the middle operator as a grouped expression,
            // allowing expressions of lower precedence as well.
            Expr center = new Expr.Grouping(expression());
            Token operator_2 = consume(COLON, "Expect : after middle expression.");
            Expr right = ternary(); // Ternary is made right-associative with a recursive call

            // To avoid explicitly adding a ternary operation to the expressions class (Expr), I just
            // used Binary expressions to mimic how a ternary operation would look like. The conditional
            // expression operand (left) is first for the ? operator, and the : operator is the second operand of ?,
            // which contains the true (center) and false (right) operands of the ternary operation.
            // Example: 2 == 2 ? true : false -> (? (== 2.0 2.0) (: (group true) false))
            Expr tempEquality = expr;
            expr = new Expr.Binary(center, operator_2, right);
            expr = new Expr.Binary(tempEquality, operator_1, expr);

//            expr = new Expr.Binary(expr, operator_2, center);
//            expr = new Expr.Binary(expr, operator_1, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        // Error productions for operators
        if (detectOperatorErrors()) return null;

        throw error(peek(), "Expect expression.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private boolean detectOperatorErrors() {
        // Error productions for operators
        if (match(COMMA)) {
            Token erroneousOperator = previous();

            comma();
            error(erroneousOperator, "Missing left hand operand.");
            return true;
        }
        if (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token erroneousOperator = previous();

            equality();
            error(erroneousOperator, "Missing right hand operand.");
            return true;
        }
        if (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token erroneousOperator = previous();

            comparison();
            error(erroneousOperator, "Missing left hand operand.");
            return true;
        }
        if (match(PLUS, MINUS)) {
            Token erroneousOperator = previous();

            term();
            error(erroneousOperator, "Missing right hand operand.");
            return true;
        }
        if (match(SLASH, STAR)) {
            Token erroneousOperator = previous();

            factor();
            error(erroneousOperator, "Missing right hand operand.");
            return true;
        }
        return false;
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}
