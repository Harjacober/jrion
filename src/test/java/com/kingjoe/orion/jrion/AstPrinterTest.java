package com.kingjoe.orion.jrion;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AstPrinterTest {

    @Test
    public void print() {
        //Given
        String source = "(4 - 5) + 5 / 6 - 3 * 6 + 1";
        List<Token> tokens = new Scanner(source).scanTokens();
        List<Stmt> statements = new Parser(tokens).parse();

        //When
        Stmt.Expression expression = (Stmt.Expression) statements.get(0);
        String output = new AstPrinter().print(expression.expression);

        //Then
        assertEquals("(+ (- (+ (group (- 4.0 5.0)) (/ 5.0 6.0)) (* 3.0 6.0)) 1.0)", output);
    }
}