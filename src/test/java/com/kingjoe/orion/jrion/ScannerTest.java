package com.kingjoe.orion.jrion;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ScannerTest {

    private List<Token> scan(String source) {
        Scanner scanner = new Scanner(source);
        return scanner.scanTokens();
    }

    @Test
    public void testSingleCharacterTokens() {
        //Given
        String source = "()+-*/;{}";

        //When
        List<Token> tokens = scan(source);

        //Then
        assertEquals(10, tokens.size());
        assertEquals(TokenType.LEFT_PAREN, tokens.get(0).type);
        assertEquals(TokenType.RIGHT_PAREN, tokens.get(1).type);
        assertEquals(TokenType.PLUS, tokens.get(2).type);
        assertEquals(TokenType.MINUS, tokens.get(3).type);
        assertEquals(TokenType.STAR, tokens.get(4).type);
        assertEquals(TokenType.SLASH, tokens.get(5).type);
        assertEquals(TokenType.SEMICOLON, tokens.get(6).type);
        assertEquals(TokenType.LEFT_BRACE, tokens.get(7).type);
        assertEquals(TokenType.RIGHT_BRACE, tokens.get(8).type);
    }

    @Test
    public void testKeywords() {
        //Given
        String source = "if else while var";

        //When
        List<Token> tokens = scan(source);

        //Then
        assertEquals(5, tokens.size());
        assertEquals(TokenType.IF, tokens.get(0).type);
        assertEquals(TokenType.ELSE, tokens.get(1).type);
        assertEquals(TokenType.WHILE, tokens.get(2).type);
        assertEquals(TokenType.VAR, tokens.get(3).type);
        assertEquals(TokenType.EOF, tokens.get(4).type);
    }

    @Test
    public void testIdentifiers() {
        //Given
        String source = "variable anotherVar _underscore";

        //When
        List<Token> tokens = scan(source);

        //Then
        assertEquals(4, tokens.size());
        assertEquals(TokenType.IDENTIFIER, tokens.get(0).type);
        assertEquals("variable", tokens.get(0).lexeme);
        assertEquals(TokenType.IDENTIFIER, tokens.get(1).type);
        assertEquals("anotherVar", tokens.get(1).lexeme);
        assertEquals(TokenType.IDENTIFIER, tokens.get(2).type);
        assertEquals("_underscore", tokens.get(2).lexeme);
    }

    @Test
    public void testNumbers() {
        //Given
        String source = "123 456.789";

        //When
        List<Token> tokens = scan(source);

        //Then
        assertEquals(3, tokens.size());
        assertEquals(TokenType.NUMBER, tokens.get(0).type);
        assertEquals(123.0, tokens.get(0).literal);
        assertEquals(TokenType.NUMBER, tokens.get(1).type);
        assertEquals(456.789, tokens.get(1).literal);
    }

    @Test
    public void testStringLiterals() {
        //Given
        String source = "\"Hello, World!\"";

        //When
        List<Token> tokens = scan(source);

        //Then
        assertEquals(2, tokens.size());
        assertEquals(TokenType.STRING, tokens.get(0).type);
        assertEquals("Hello, World!", tokens.get(0).literal);
    }

    @Test
    public void testMultiLineStringLiterals() {
        //Given
        String source = "\" This is a\n multiline string \n it is allowed.\"";

        //When
        List<Token> tokens = scan(source);

        //Then
        assertEquals(2, tokens.size());
        assertEquals(TokenType.STRING, tokens.get(0).type);
        assertEquals(" This is a\n multiline string \n it is allowed.", tokens.get(0).literal);
    }

    @Test
    public void testOperators() {
        //Given
        String source = "!= == <= >= = < >";

        //When
        List<Token> tokens = scan(source);

        //Then
        assertEquals(8, tokens.size());
        assertEquals(TokenType.BANG_EQUAL, tokens.get(0).type);
        assertEquals(TokenType.EQUAL_EQUAL, tokens.get(1).type);
        assertEquals(TokenType.LESS_EQUAL, tokens.get(2).type);
        assertEquals(TokenType.GREATER_EQUAL, tokens.get(3).type);
        assertEquals(TokenType.EQUAL, tokens.get(4).type);
        assertEquals(TokenType.LESS, tokens.get(5).type);
        assertEquals(TokenType.GREATER, tokens.get(6).type);
    }

    @Test
    public void testCommentsAreIgnored() {
        //Given
        String source = "var x = 10; // This is a comment\n var y = 20;";

        //When
        List<Token> tokens = scan(source);

        //Then
        assertEquals(11, tokens.size());
        assertEquals(TokenType.VAR, tokens.get(0).type);
        assertEquals(TokenType.IDENTIFIER, tokens.get(1).type);
        assertEquals(TokenType.EQUAL, tokens.get(2).type);
        assertEquals(TokenType.NUMBER, tokens.get(3).type);
        assertEquals(TokenType.SEMICOLON, tokens.get(4).type);
        assertEquals(TokenType.VAR, tokens.get(5).type);
        assertEquals(TokenType.IDENTIFIER, tokens.get(6).type);
        assertEquals(TokenType.EQUAL, tokens.get(7).type);
        assertEquals(TokenType.NUMBER, tokens.get(8).type);
        assertEquals(TokenType.SEMICOLON, tokens.get(9).type);
    }

    @Test
    public void testBlockedCommentsAreIgnored() {
        //Given
        String source = "var x = 10; /** This is a comment\nblocked comment\npn multiple lines **/\n var y = 20;";

        //When
        List<Token> tokens = scan(source);

        //Then
        assertEquals(11, tokens.size());
        assertEquals(TokenType.VAR, tokens.get(0).type);
        assertEquals(TokenType.IDENTIFIER, tokens.get(1).type);
        assertEquals(TokenType.EQUAL, tokens.get(2).type);
        assertEquals(TokenType.NUMBER, tokens.get(3).type);
        assertEquals(TokenType.SEMICOLON, tokens.get(4).type);
        assertEquals(TokenType.VAR, tokens.get(5).type);
        assertEquals(TokenType.IDENTIFIER, tokens.get(6).type);
        assertEquals(TokenType.EQUAL, tokens.get(7).type);
        assertEquals(TokenType.NUMBER, tokens.get(8).type);
        assertEquals(TokenType.SEMICOLON, tokens.get(9).type);
    }

    @Test
    public void testUnterminatedStringError() {
        //Given
        String source = "\"Unclosed string";

        ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errorOutput));

        //When
        scan(source);

        //Then
        assertTrue(errorOutput.toString().contains("Unterminated string"));

    }

    @Test
    public void testUnterminatedBlockComment() {
        //Given
        String source = "/* This is an unclosed comment";

        ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errorOutput));

        //When
        scan(source);

        //Then
        assertTrue(errorOutput.toString().contains("Unterminated block comment."));
    }
}
