package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


// NOTE: This printer does not support all Token types, only mathematical Token types and nil
// The RPNPrinter first takes the expression, converts it into a list of tokens in correct operational order,
// then applies the Shunting Yard Algorithm to the Token list to convert it into Reverse Polish Notation
class RPNPrinter implements Expr.Visitor<List<Token>> {
    String print(Expr expr) {
        return composeToRPN(expr);
    }

    @Override
    public List<Token> visitBinaryExpr(Expr.Binary expr) {
        List<Token> tokens = new ArrayList<>(expr.left.accept(this));
//        tokens.add(new Token(expr.operator.type, expr.operator.lexeme, null, 1));
        tokens.add(expr.operator);
        tokens.addAll(expr.right.accept(this));

        return tokens;
    }

    @Override
    public List<Token> visitGroupingExpr(Expr.Grouping expr) {
        List<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.LEFT_PAREN, "(", null, 1));
        tokens.addAll(expr.expression.accept(this));
        tokens.add(new Token(TokenType.RIGHT_PAREN, ")", null, 1));

        return tokens;
    }

    // Note that all literals are treated as numbers for this implementation, but the syntax remains the same.
    @Override
    public List<Token> visitLiteralExpr(Expr.Literal expr) {
        List<Token> token = new ArrayList<>();
        if (expr.value == null) token.add(new Token(TokenType.NIL, "null", null, 1));
        else token.add(new Token(TokenType.NUMBER, expr.value.toString(), null, 1));
        return token;
    }

    @Override
    public List<Token> visitUnaryExpr(Expr.Unary expr) {
        List<Token> token = new ArrayList<>(expr.right.accept(this));
        token.add(expr.operator);
        return token;
    }

    private String composeToRPN(Expr expr) {
        StringBuilder builder = new StringBuilder();

        Stack<Token> operatorStack = new Stack<>();

        List<Token> expression = expr.accept(this);

        // Implementation of the Shunting Yard Algorithm
        for (Token token : expression) {
            switch (token.type) {
                case NUMBER, NIL: // Numbers, nil, identifiers, true, false
                    builder.append(token.lexeme).append(" ");
                    break;
                case PLUS, MINUS: // Lower value operators
                    if (operatorStack.isEmpty()) operatorStack.push(token);
                    else{
                        Token topToken = operatorStack.peek();
                        while (!operatorStack.isEmpty() && topToken.type != TokenType.LEFT_PAREN) {
                            builder.append(operatorStack.pop().lexeme).append(" ");
                            if (!operatorStack.isEmpty()){
                                topToken = operatorStack.peek();
                            }
                        }
                        operatorStack.push(token);
                    }
                    break;
                case STAR, SLASH: // Higher value operators
                    if (operatorStack.isEmpty()) operatorStack.push(token);
                    else{
                        Token topToken = operatorStack.peek();
                        while (!operatorStack.isEmpty() && topToken.type != TokenType.LEFT_PAREN && topToken.type != TokenType.PLUS && topToken.type != TokenType.MINUS) {
                            builder.append(operatorStack.pop().lexeme).append(" ");
                            if (!operatorStack.isEmpty()){
                                topToken = operatorStack.peek();
                            }
                        }
                        operatorStack.push(token);
                    }
                    break;
                case LEFT_PAREN: // grouping start
                    operatorStack.push(token);
                    break;
                case RIGHT_PAREN: // grouping end
                    while (!operatorStack.isEmpty() && operatorStack.peek().type != TokenType.LEFT_PAREN) {
                        builder.append(operatorStack.pop().lexeme).append(" ");
                    }
                    if (!operatorStack.isEmpty() && operatorStack.peek().type == TokenType.LEFT_PAREN) {
                        operatorStack.pop(); // Pop the left parenthesis once done
                    } else {
                        // Error: uneven parenthesis exists since left parenthesis should have been here
                        System.err.println("Uneven right parenthesis detected");
                        System.exit(1);
                    }
                    break;
                default:
                    System.err.println("Unsupported token detected");
                    System.exit(1);
                    break;
            }
        }

        while (!operatorStack.isEmpty()) builder.append(operatorStack.pop().lexeme).append(" ");

        return builder.toString();
    }

    public static void main(String[] args) {
        // Example expression created and tested
        Expr expression = new Expr.Binary(
                new Expr.Grouping(
                        new Expr.Binary(
                                new Expr.Literal(1),
                                new Token(TokenType.PLUS, "+", null, 1),
                                new Expr.Literal(2)
                        )
                ),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(
                        new Expr.Binary(
                                new Expr.Literal(4),
                                new Token(TokenType.MINUS, "-", null, 1),
                                new Expr.Literal(3)
                        )
                )
        );

        System.out.println(new RPNPrinter().print(expression)); // 1 2 + 4 3 - *
    }
}
