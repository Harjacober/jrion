package com.kingjoe.orion.jrion;

public enum TokenType {
    // single-character tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, LEFT_SQUARE_BRACE, RIGHT_SQUARE_BRACE, COMMA, DOT, MINUS, PLUS, SEMICOLON, COLON, SLASH, STAR, MODULO,

    // one or two character tokens
    BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,

    // literals
    IDENTIFIER, STRING, NUMBER,

    // keywords
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, ELSEIF, NIL, OR, RETURN, SUPER, THIS, TRUE, VAR, WHILE, BREAK, CONTINUE,

    EOF
}
