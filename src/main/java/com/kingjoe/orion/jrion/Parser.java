package com.kingjoe.orion.jrion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
/* Grammar Definition
 * program          -> declaration* EOF
 * declaration      -> classDeclaration | funcDeclaration | varDeclaration | statement | block;
 * classDeclaration -> "class" IDENTIFIER ( "<" IDENTIFIER )? "{" function* "}"
 * funcDeclaration  -> "fun" function
 * function         -> IDENTIFIER "(" parameters? ")" block
 * parameters       -> IDENTIFIER ( "," IDENTIFIER )*
 * varDeclaration   -> "var" IDENTIFIER ( "=" expression )? ";"
 * statement        -> exprStatement | block | ifStatement | whileStatement | forStatement | jumpStmt | returnStmt
 * ifStatement      -> "if" "(" expression ")" block ( "elseif" "(" expression ")" block )* ( "else" block ) ?
 * whileStatement   -> "while" "(" expression ")" block;
 * forStatement     -> "for" "(" ( varDeclaration | exprStatement | ";" ) expression? ";" expression? ")" block
 * jumpStmt         -> ( "break" | "continue" | "return" expression? ) ";"
 * returnStmt       -> "return" expression? ";"
 * block            -> "{" declaration* "}"
 * exprStatement    -> expression ";"
 * expression       -> assignment
 * assignment       -> (call "." )? IDENTIFIER "=" assignment | logical_or
 * logical_or       -> logical_and ( "or" logical_and )*
 * logical_and      -> equality ( "and" equality )*
 * equality         -> comparison ( ("!=" | "==") comparison )*
 * comparison       -> term ( (">" | ">=" | "<" | "<=") term )*
 * term             -> factor ( ("-" | "+") factor )*
 * factor           -> unary ( ("*" | "/" | "%") unary )*
 * unary            -> ( "!" | "-" ) unary | builtInTypes
 * builtInTypes     -> array
 * array            -> "[" expression ( "," expression )* "]" ( "[" expression ( "," expression )* "]" )* | map
 * map              -> "{" expression ":" expression ( "," expression : expression )* "}" | call
 * call             -> anonFunction ( ( "(" arguments? ")" ) | ( "[" expression "]" ) | "." IDENTIFIER )*
 * arguments        -> expression ( "," expression )*
 * anonFunction     -> "fun" "(" parameters? ")" block | primary
 * primary          -> IDENTIFIER | "super" "." IDENTIFIER | "super" "(" arguments? ")" | NUMBER | STRING | "true" | "false" | "nil" | "("  expression ")"
 */

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }
    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Stmt declaration() {
        try {
            if (match(TokenType.CLASS)) {
                return classDeclaration();
            }
            if (match(TokenType.FUN)) {
                return functionDeclaration();
            }
            if (match(TokenType.VAR)) {
                return variableDeclaration();
            }
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt classDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expected class name");

        Expr.Variable superClass = null;
        if (match(TokenType.LESS)) {
            Token superClassName = consume(TokenType.IDENTIFIER, "Expected superclass name");
            superClass = new Expr.Variable(superClassName);
        }

        consume(TokenType.LEFT_BRACE, "Expected '{' after class name");

        List<Stmt.Var> fields = new ArrayList<>();
        List<Stmt.Function> methods = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            if (match(TokenType.VAR)) {
                fields.add(variableDeclarationWOInitializer());
            } else {
                methods.add(function());
            }
        }
        consume(TokenType.RIGHT_BRACE, "Expected matching '}' to close class");

        return new Stmt.Class(name, superClass, fields, methods);
    }

    private Stmt functionDeclaration() {
        return function();
    }

    private Stmt.Function function() {
        Token name = consume(TokenType.IDENTIFIER, "Expected function name.");
        consume(TokenType.LEFT_PAREN, "Expected '(' after function name.");

        List<Token> parameters = new ArrayList<>();
        while (!check(TokenType.RIGHT_PAREN)) {
            if (parameters.size() > 255) {
                error(peek(), "Function cannot have more than 255 parameters.");
            }
            Token param = consume(TokenType.IDENTIFIER, "Expected parameter name to be an identifier.");
            parameters.add(param);
            if (!match(TokenType.COMMA)) {
                break;
            }
        }
        consume(TokenType.RIGHT_PAREN, "Expected ')' to close function");
        consume(TokenType.LEFT_BRACE, "Expected '{' to start a function body.");
        Stmt body = block();

        return new Stmt.Function(name, parameters, body);
    }

    private Stmt variableDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "");

        Expr initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }

        consume(TokenType.SEMICOLON, "Expected ';' to terminate variable declaration.");

        return new Stmt.Var(name, initializer);
    }

    private Stmt.Var variableDeclarationWOInitializer() {
        Token name = consume(TokenType.IDENTIFIER, "");
        consume(TokenType.SEMICOLON, "Expected ';' to terminate variable declaration.");

        return new Stmt.Var(name, null);
    }

    private Stmt statement() {
        if (match(TokenType.LEFT_BRACE)) {
            return block();
        }
        if (match(TokenType.IF)) {
            return ifStatement();
        }
        if (match(TokenType.WHILE)) {
            return whileStatement();
        }
        if (match(TokenType.FOR)) {
            return forStatement();
        }
        if (match(TokenType.BREAK, TokenType.CONTINUE, TokenType.RETURN)) {
            return jumpStatement();
        }
        return expressionStatement();
    }

    private Stmt block() {
        List<Stmt> statements = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(TokenType.RIGHT_BRACE, "Expected matching closing brace '}'");
        return new Stmt.Block(statements);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Expected ';' to terminate a statement");
        return new Stmt.Expression(expr);
    }

    private Stmt ifStatement() {
        Token keyword = previous();
        Expr condition = expression();

        consume(TokenType.LEFT_BRACE, "Expected '{' to start conditional statement body");
        Stmt thenBranch = block();

        if (match(TokenType.ELSEIF)) {
            keyword = previous();
            return new Stmt.If(condition, thenBranch, ifStatement(), keyword);
        } else if (match(TokenType.ELSE)) {
            keyword = previous();
            consume(TokenType.LEFT_BRACE, "Expected '{' to start conditional statement body");
            return new Stmt.If(condition, thenBranch, block(), keyword);
        }

        return new Stmt.If(condition, thenBranch, null, keyword);
    }

    int loopCounter = 0;
    private Stmt whileStatement() {
        loopCounter++;
        Token keyword = previous();
        Expr condition = expression();

        consume(TokenType.LEFT_BRACE, "Expected '{' to start while statement body");
        Stmt body = block();
        loopCounter--;
        return new Stmt.While(condition, body, keyword);
    }

    private Stmt forStatement() {
        loopCounter++;
        Token keyword = previous();
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'for'");

        Stmt initializer;
        if (match(TokenType.SEMICOLON)) {
            initializer = null;
;        } else if (match(TokenType.VAR)) {
            initializer = variableDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = new Expr.Literal(true);
        if (!check(TokenType.SEMICOLON)) {
            condition = expression();
        }
        consume(TokenType.SEMICOLON, "Expected ';' to terminate initializer in for");

        Stmt increment = null;
        if (!check(TokenType.RIGHT_PAREN)) {
            increment = new Stmt.Expression(expression());
        }
        consume(TokenType.RIGHT_PAREN, "Expected ')' to close 'for'");

        consume(TokenType.LEFT_BRACE, "Expected '{' to start 'for' loop body");
        Stmt.Block body = (Stmt.Block) block();
        if (increment != null) {
            body.statements.add(increment);
        }

        Stmt transformedStmt = new Stmt.While(condition, body, keyword);

        if (initializer != null) {
            transformedStmt = new Stmt.Block(Arrays.asList(initializer, transformedStmt));
        }

        loopCounter--;
        return transformedStmt;
    }

    private Stmt jumpStatement() {
        Token keyword = previous();
        switch (keyword.type) {
            case BREAK -> {
                consume(TokenType.SEMICOLON, "Expected ';' to terminate break");
                if (loopCounter < 1) {
                    error(keyword, "break statement cannot appear outside of loop");
                }
                return new Stmt.Break();
            }
            case CONTINUE -> {
                consume(TokenType.SEMICOLON, "Expected ';' to terminate continue");
                if (loopCounter < 1) {
                    throw error(keyword, "break statement cannot appear outside of loop");
                }
                return new Stmt.Continue();
            }
            case RETURN -> {
                Expr expr = null;
                if (!check(TokenType.SEMICOLON)) {
                    expr = expression();
                }
                consume(TokenType.SEMICOLON, "Expected ';' to terminate return");
                return new Stmt.Return(previous(), expr);
            }
        }
        return null;
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        /*
        left hand side of an assignment statement is an l-value expression (expressions that evaluates to a storage location)
        as opposed to r-value (expressions that evaluates to a value).
        Example of l-value expressions are the left hand sides of the following.
        a = "value"
        instance.field = value
        arr[index] = value etc.
         */
        Expr left = logicalOr();

        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expr right = assignment();

            if (left instanceof Expr.Variable variable) {
                Token name = variable.name;
                return new Expr.Assign(name, right);

            } else if (left instanceof Expr.Indexing expr) {
                return new Expr.IndexAssign(expr.squareBrace, expr.indexee, expr.index, right);
            } else if (left instanceof Expr.Get expr) {
                return new Expr.Set(expr.object, expr.property, right);
            }
            error(equals, "Invalid assignment target.");
        }

        return left;
    }

    private Expr logicalOr() {
        Expr left = logicalAnd();

        while (match(TokenType.OR)) {
            Token operator = previous();
            Expr right = logicalAnd();
            left = new Expr.Logical(left, operator, right);
        }
        return left;
    }

    private Expr logicalAnd() {
        Expr left = equality();

        while (match(TokenType.AND)) {
            Token operator = previous();
            Expr right = equality();
            left = new Expr.Logical(left, operator, right);
        }
        return left;
    }

    private Expr equality() {
        Expr left = comparison();

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            left = new Expr.Binary(left, operator, right);
        }
        return left;
    }

    private Expr comparison() {
        Expr left = term();
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            left = new Expr.Binary(left, operator, right);
        }
        return left;
    }

    private Expr term() {
        Expr left = factor();
        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expr right = factor();
            left = new Expr.Binary(left, operator, right);
        }
        return left;
    }

    private Expr factor() {
        Expr left = unary();
        while (match(TokenType.SLASH, TokenType.STAR, TokenType.MODULO)) {
            Token operator = previous();
            Expr right = unary();
            left = new Expr.Binary(left, operator, right);
        }
        return left;
    }

    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        // binary operator appearing without a left operand
        if (match(TokenType.SLASH, TokenType.STAR, TokenType.PLUS, TokenType.GREATER, TokenType.GREATER_EQUAL,
                  TokenType.LESS, TokenType.LESS_EQUAL, TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = unary();
            throw error(operator, "Binary operator requires two operands. left operand is missing.");
        }

        return builtInTypes();
    }

    private Expr builtInTypes() {
        return array();
    }

    private Expr array() {
        if (match(TokenType.LEFT_SQUARE_BRACE)) {
            List<Expr> elements = new ArrayList<>();
            if (!check(TokenType.RIGHT_SQUARE_BRACE)) {
                do {
                    elements.add(expression());
                } while (match(TokenType.COMMA));
            }
            Token squareBrace = consume(TokenType.RIGHT_SQUARE_BRACE, "Expected ']' to close array expression");
            return new Expr.Array(squareBrace, elements);
        }
        return map();
    }

    private Expr map() {
        if (match(TokenType.LEFT_BRACE)) {
            if (match(TokenType.RIGHT_BRACE)) {
                return new Expr.Map(previous(), new ArrayList<>(), new ArrayList<>());
            }
            Expr first = expression();
            consume(TokenType.COLON, "Expected ':' after key of map");
            return finishMap(first);
        }
        return call();
    }

    private Expr finishMap(Expr firstKey) {
        List<Expr> keys = new ArrayList<>(Arrays.asList(firstKey));
        List<Expr> values = new ArrayList<>(Arrays.asList(expression()));
        if (match(TokenType.COMMA)) {
            do {
                keys.add(expression());
                consume(TokenType.COLON,  "Expected ':' after key of map");
                values.add(expression());
            } while (match(TokenType.COMMA));
        }
        Token brace = consume(TokenType.RIGHT_BRACE, "Expected '}' to close map expression");

        return new Expr.Map(brace, keys, values);
    }

    private Expr call() {
        Expr expr = anonymousFunction();

        while (true) {
            if (match(TokenType.LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(TokenType.LEFT_SQUARE_BRACE)){
                expr = finishIndexing(expr);
            } else if (match(TokenType.DOT)) {
                expr = finishGet(expr);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expr finishCall(Expr expr) {
        List<Expr> arguments = arguments();
        Token paren = previous();

        return new Expr.Call(expr, paren, arguments);
    }

    private Expr finishIndexing(Expr expr) {
        Expr index = expression();
        Token rightBrace = consume(TokenType.RIGHT_SQUARE_BRACE, "Expected ']' after index");

        return new Expr.Indexing(expr, rightBrace, index);
    }

    private Expr finishGet(Expr expr) {
        Token name = consume(TokenType.IDENTIFIER, "Expected property name after '.'");
        return new Expr.Get(expr, name);
    }

    private Expr anonymousFunction() {
        if (match(TokenType.FUN)) {
            Token paren = consume(TokenType.LEFT_PAREN, "Expected '(' after 'fun' for anonymous function");
            List<Token> parameters = new ArrayList<>();
            if (!check(TokenType.RIGHT_PAREN)) {
                do {
                    if (parameters.size() > 255) {
                        error(peek(), "Anonymous function cannot have more than 255 parameters");
                    }
                    Token param = consume(TokenType.IDENTIFIER, "Expected parameter to be an identifier");
                    parameters.add(param);
                } while (match(TokenType.COMMA));
            }
            consume(TokenType.RIGHT_PAREN, "Expected ')' to close anonymous function definition");
            consume(TokenType.LEFT_BRACE, "Expected '{' to start anonymous function body");
            Stmt body = block();

            return new Expr.AnonFunc(paren, parameters, body);
        }
        return primary();
    }

    private Expr primary() {
        if (match(TokenType.FALSE)) {
            return new Expr.Literal(false);
        }
        if (match(TokenType.TRUE)) {
            return new Expr.Literal(true);
        }
        if (match(TokenType.NIL)) {
            return new Expr.Literal(null);
        }
        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect a closing ')' after expression.");
            return new Expr.Grouping(expr);
        }
        if (match(TokenType.THIS)) {
            return new Expr.This(previous());
        }
        if (match(TokenType.SUPER)) {
            Token keyword = previous();
            if (match(TokenType.DOT)) {
                Token method = consume(TokenType.IDENTIFIER, "Expected superclass method name");
                return new Expr.Super(keyword, method);
            } else if (match(TokenType.LEFT_PAREN)) {
                List<Expr> arguments = arguments();
                Token paren = previous();
                return new Expr.Super(keyword, paren, arguments);
            } else {
                throw new RuntimeError(keyword, "Expected property access or call after super");
            }
        }
        if (match(TokenType.IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        throw error(peek(), "Expect expression.");
    }

    private List<Expr> arguments() {
        List<Expr> arguments = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (arguments.size() > 255) {
                    error(peek(), "Cannot have more than 255 arguments");
                }
                arguments.add(expression());
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RIGHT_PAREN, "Expected ')' after arguments");
        return arguments;
    }

    private boolean match(TokenType... tokenTypes) {
        for (TokenType tokenType : tokenTypes) {
            if (check(tokenType)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }
        throw error(peek(), message);
        
    }

    private ParseError error(Token token, String message) {
        Rion.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) {
                return;
            }
            switch (peek().type) {
                case CLASS, FOR, FUN, IF, RETURN, VAR, WHILE -> {return;}
            }
            advance();
        }
    }
}
