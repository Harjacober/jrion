package com.kingjoe.orion.jrion;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.opentest4j.AssertionFailedError;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.Permission;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ProgramTest {

    final String pathToPrograms = "src/test/resources/jrion_programs";

    @BeforeAll
    static void before() {
        //preventSystemExit();
    }

    @Test
    void testExpressionAndStatement() throws IOException {
        //Given
        String source = """
                println("Hello, Welcome to Jrion: The Java implementation of the Orion programming language");
                println("This expression 4 + 5 * 3 evaluates to: " + (4 + 5 * 3));
                println("String concatenation in Jrion looks like this: " + "Hello there!");
                """;
        //When
        String output = executeProgram(source).output;

        //Then
        String expected = """
                Hello, Welcome to Jrion: The Java implementation of the Orion programming language
                This expression 4 + 5 * 3 evaluates to: 19
                String concatenation in Jrion looks like this: Hello there!
                """;
        assertEquals(expected.trim(), output);
    }

    @Test
    void testCreatingGlobalVariables() throws IOException {
        //Given
        String source = """
                var message = "Velocity = ";
                var distance = 1200;
                var time = 12;
                var speed = distance / time;
                println(message + speed + " m/s");
                """;
        //When
        String output = executeProgram(source).output;

        //Then
        String expected = "Velocity = 100 m/s";
        assertEquals(expected, output);
    }

    @Test
    void testVariableAssignments() throws IOException {
        //Given
        String source = """
                var length = 2;
                var breadth = 3;
                var height = 4;
                                
                var volume = length * breadth * height;
                                
                println(volume);
                                
                length = 1;
                breadth = 2;
                height = 3;
                                
                volume = length * breadth * height;
                                
                println(volume);
                """;
        //When
        String output = executeProgram(source).output;

        //Then
        String expected = """
                24
                6
                """;
        assertEquals(expected.trim(), output);
    }

    @Test
    void testBlockAndScope() throws IOException {
        //Given
        String source = """
                var l = 2;
                var b = 3;
                var area = l * b;
                {
                    var r = 2;
                    var pi = 3.142;
                    var area = 2 * pi * r * r;
                    println("Area of Circle = " + area);
                }
                println("Area of Rectangle = " + area);
                """;
        //When
        String output = executeProgram(source).output;

        //Then
        String expected = """
                Area of Circle = 25.136
                Area of Rectangle = 6
                """;
        assertEquals(expected.trim(), output);
    }

    @Test
    void testInnerScopeCanAccessVariableDefinedInEnclosingScope() throws IOException {
        //Given
        String source = """
                var l = 2;
                var b = 3;
                var area;
                {
                    area = l * b;
                    println(area);
                    {
                        l = 1; b = 2;
                        area = l * b;
                        println(area);
                        {
                            l = 4; b = 5;
                            area = l * b;
                            println(area);
                            {
                                l = 10;
                                b = 10;
                            }
                        }
                    }
                }
                println("outermost scope");
                println(l * b);
                """;
        //When
        String output = executeProgram(source).output;

        //Then
        String expected = """
                6
                2
                20
                outermost scope
                100
                """;
        assertEquals(expected.trim(), output);
    }

    @Test
    void testControlFlowConditional() throws IOException {
        //Given
        String source = """
                var n = 4;
                if n == 4 {
                    println("even: " + n);
                } elseif n == 1 {
                    println("odd: " + n);
                } else {
                    println("invalid");
                }
                
                n = 1;
                if (n == 4) {
                    println("even: " + n);
                } elseif n == 1 {
                    println("odd: " + n);
                } else {
                    println("invalid");
                }
                
                n = 7;
                if n == 4 {
                    println("even: " + n);
                } elseif (n == 1) {
                    println("odd: " + n);
                } else {
                    println("invalid");
                }
                
                if true {
                    println("yes");
                }
                """;
        //When
        String output = executeProgram(source).output;

        //Then
        String expected = """
                even: 4
                odd: 1
                invalid
                yes
                """;
        assertEquals(expected.trim(), output);
    }

    @Test
    void testControlFlowNestedConditional() throws IOException {
        //Given
        String source = """
                var n = 4;
                if n == 4 {
                    if n - 4 == 0 {
                        println("even and special: " + n);
                    } else {
                        println("even: " + n);
                    }
                } else {
                    println("invalid");
                }
                
                n = 1;
                if n == 4 {
                    if n - 4 == 0 {
                        println("even and special: " + n);
                    } else {
                        println("even: " + n);
                    }
                } elseif (n == 1) {
                    if n - 1 == 2 {
                        println("odd and special: " + n);
                    } else {
                        println("odd: " + n);
                    }
                } else {
                    println("invalid");
                }
                
                if false {
                    println("no");
                }
                """;
        //When
        String output = executeProgram(source).output;

        //Then
        String expected = """
                even and special: 4
                odd: 1
                """;
        assertEquals(expected.trim(), output);
    }

    @Test
    void testLogicalOperator() throws IOException {
        //Given
        String source = """
                println(true or false);
                println(false or true);
                println(true or true);
                println(false or false);
                
                println((1 == 1) and true);
                println(true and (4 >= 6));
                println((10 < 9) and (4 != 5));
                println(false and false);
                
                """;
        //When
        String output = executeProgram(source).output;

        //Then
        String expected = """
                true
                true
                true
                false
                true
                false
                false
                false
                """;
        assertEquals(expected.trim(), output);
    }

    @Test
    void testControlFlowWhileLoops() throws IOException {
        //Given
        String source = """
                var n = 1;
                while n <= 5 {
                    if n % 2 == 0 {
                        println("even: " + n);
                    } elseif n % 2 == 1 {
                        println("odd: " + n);
                    }
                    n = n + 1;
                }
                """;
        //When
        String output = executeProgram(source).output;

        //Then
        String expected = """
                odd: 1
                even: 2
                odd: 3
                even: 4
                odd: 5
                """;
        assertEquals(expected.trim(), output);
    }

    @Test
    void testControlFlowForLoops() throws IOException {
        //Given
        String source = """
                for (var n = 1; n <= 5; n = n + 1) {
                    if (n % 2 == 0) {
                        println("even: " + n);
                    } elseif (n % 2 == 1) {
                        println("odd: " + n);
                    }
                }
                """;
        //When
        String output = executeProgram(source).output;

        //Then
        String expected = """
                odd: 1
                even: 2
                odd: 3
                even: 4
                odd: 5
                """;
        assertEquals(expected.trim(), output);
    }

    @Test
    void testControlFlowNestedForLoops() throws IOException {
        //Given
        String source = """
                for (var i = 1; i <= 3; i = i + 1) {
                    for(var j = 1; j <= 3; j = j + 1) {
                        println(i + " x " + j + " = " + (i * j));
                    }
                }
                """;
        //When
        String output = executeProgram(source).output;

        //Then
        String expected = """
                1 x 1 = 1
                1 x 2 = 2
                1 x 3 = 3
                2 x 1 = 2
                2 x 2 = 4
                2 x 3 = 6
                3 x 1 = 3
                3 x 2 = 6
                3 x 3 = 9
                """;
        assertEquals(expected.trim(), output);
    }

    @Test
    void testJumpStatementBreak() throws IOException {
        //Given
        String source = """
                var signal = -1;
                println("signal is: " + signal);
                
                var n = 1;
                while n <= 10 {
                    if n == 5 {
                        signal = 999;
                        break;
                    }
                    println(n);
                    n = n + 1;
                }
                println("signal is: " + signal);
                """;

        //When
        String output = executeProgram(source).output;

        //Then
        String expected = """
                signal is: -1
                1
                2
                3
                4
                signal is: 999
                """;
        assertEquals(expected.trim(), output);
    }

    @Test
    void shouldFailWhenBreakIsUsedOutsideLoopContext() throws IOException {
        //Given
        String source = """
                while true {
                    break;
                }
                break;
                """;

        //When
        String error = executeProgram(source).error;

        //Then
        String expected = """
                [line 4] Error at 'break': break statement cannot appear outside of loop
                """;
        assertEquals(expected.trim(), error);
    }

    @Test
    void testJumpStatementBreakInNestedLoops() throws IOException {
        //Given
        String source = """
                var i = 1;
                while i <= 3 {
                    var j = 1;
                    while j <= 3 {
                        if (i == j) {
                            println("break at: " + i + " and " + j);
                            break;
                        }
                        println("(" + i + ", " + j + ")");
                        j = j + 1;
                    }
                    println("outer: " + i);
                    i = i + 1;
                }
                
                println("==========");
                
                for (var i = 1; i <= 3; i = i + 1) {
                    for(var j = 1; j <= 3; j = j + 1) {
                          if (i == j) {
                            println("break at: " + i + " and " + j);
                            break;
                        }
                        println("(" + i + ", " + j + ")");
                    }
                     println("outer: " + i);
                }
                
                """;

        //When
        String output = executeProgram(source).output;

        //Then
        String expected = """
                break at: 1 and 1
                outer: 1
                (2, 1)
                break at: 2 and 2
                outer: 2
                (3, 1)
                (3, 2)
                break at: 3 and 3
                outer: 3
                ==========
                break at: 1 and 1
                outer: 1
                (2, 1)
                break at: 2 and 2
                outer: 2
                (3, 1)
                (3, 2)
                break at: 3 and 3
                outer: 3
                """;
        assertEquals(expected.trim(), output);
    }

    @Test
    void testFunctions() throws IOException {
        //Given
        String source = """
                fun fibonacci(n) {
                    var first = 0;
                    var second = 1;
                    print(second);
                    while (n > 1) {
                        var temp = second;
                        second = first + second;
                        first = temp;
                        print(" " + second);
                        n = n - 1;
                    }
                }
                
                fibonacci(10);
                """;
        
        //When
        String output = executeProgram(source).output;
        
        //Then
        String expected = """
                1 1 2 3 5 8 13 21 34 55
                """;
        assertEquals(expected.trim(), output);
    }

    @Test
    void testFunctionWithReturnStatement() throws IOException {
        //Given
        String source = """
                fun count(n) {
                    while(n < 100) {
                        if n == 5 {
                            return n;
                        }
                        print(n + " ");
                        n = n + 1;
                    }
                }
                
                print("Return value = " + count(1));
                """;

        //When
        String output = executeProgram(source).output;

        //Then
        String expected = "1 2 3 4 Return value = 5";
        assertEquals(expected.trim(), output);
    }

    @Test
    void testRecursiveFunction() throws IOException {
        //Given
        String source = """
                fun fibonacci(n) {
                    if (n < 2) {
                        return n;
                    }
                    return fibonacci(n - 1) + fibonacci(n - 2);
                }
                fun printFibonacci(n) {
                    for (var i = 1; i <= n; i = i + 1) {
                        print(fibonacci(i) + " ");
                    }
                }
                printFibonacci(20);
                """;
        //When
        String output = executeProgram(source).output;

        //Then
        String expected = "1 1 2 3 5 8 13 21 34 55 89 144 233 377 610 987 1597 2584 4181 6765";
        assertEquals(expected.trim(), output);
    }

    @Test
    void testLocalFunctionAndClosure() throws IOException {
        //Given
        String source = """
                fun makeCounter() {
                    var i = 0;
                    fun count(n) {
                        while (i <= n) {
                            print(i + " ");
                            i = i + 1;
                        }
                    }
                    return count;
                }
                
                var counter = makeCounter();
                counter(10);
                """;

        //When
        String output = executeProgram(source).output;

        //Then
        String expected = "0 1 2 3 4 5 6 7 8 9 10";
        assertEquals(expected.trim(), output);
    }

    @Test
    void testVariableScopesInClosure() throws Exception {
        //Given
        String source = """
                var a = "global1";
                {
                    fun showA() {
                        println(a);
                    }
                    showA();
                    var a = "block1";
                    showA();
                    a; //using variable 'a' to prevent compile time error
                }
                
                var a = "global2";
                fun show() {
                    fun showA() {
                        println(a);
                    }
                    showA();
                    var a = "block2";
                    showA();
                    a; //using variable 'a' to prevent compile time error
                }
                show();
                """;

        //When
        String output = executeProgram(source).output;

        //Then
        String expected = """
                global1
                global1
                global2
                global2
                """;
        assertEquals(expected.trim(), output);
    }

    @Test
    void testAnonymousFunction() throws Exception {
        //Given
        String source = """
                fun printSquares(n, squares) {
                    for (var i = 1; i <= n; i = i + 1) {
                        print(squares(i) + " ");
                    }
                }
                
                printSquares(10, fun (k) {
                    return k * k;
                });
                """;
        //When
        String output = executeProgram(source).output;

        //Then
        String expected = "1 4 9 16 25 36 49 64 81 100";
        assertEquals(expected, output);
    }

    @Test
    void testAnonymousFunctionII() throws Exception {
        //Given
        String source = """
                var squares = fun (k) {
                    return k * k;
                };
                fun printSquares(n) {
                    for (var i = 1; i <= n; i = i + 1) {
                        print(squares(i) + " ");
                    }
                }
                
                printSquares(10);
                
                (fun(name) {
                    print("Hello there: " + name);
                })("Bob");
                """;
        //When
        String output = executeProgram(source).output;

        //Then
        String expected = """
                1 4 9 16 25 36 49 64 81 100 Hello there: Bob""";
        assertEquals(expected, output);
    }

    @Test
    void testReturnStatementNotAllowedAtTopLevel() throws Exception{
        //Given
        String source = "return 5;";

        //When
        String output = executeProgram(source).error;

        //Then
        assertTrue(output.contains("return statement is not allowed here"));
    }

    @Test
    void testDuplicateDeclarationOfVariableWithinTheSameScopeNotAllowed() throws Exception {
        //Given
        String source = """
                fun varDeclaration() {
                    var message = "hello";
                    print(message);
                    var message = "World!";
                    print(message);
                }
                """;

        //When
        String error = executeProgram(source).error;

        //Then
        String expected =
                "[line 4] Error at 'message': Variable with this name 'message' is already defined in this scope.";
        assertTrue(error.contains(expected));
    }

    @Test
    void testVariableShadowingNotAllowed() throws Exception {
        //Given
        String source = """
                var message = "Hello";
                fun handleMessage() {
                    var message = message;
                    print(message);
                }
                """;

        //When
        String error = executeProgram(source).error;

        //Then
        String expected = "[line 3] Error at 'message': Can't have local variable in its own initializer.";
        assertTrue(error.contains(expected));
    }

    @Test
    void testUnusedLocalVariableNotAllowed() throws Exception {
        //Given
        String source = """
                var unusedGlobal = "Hello";
                fun handleMessage() {
                    var unusedLocal = "World";
                }
                """;

        //When
        String error = executeProgram(source).error;

        //Then
        String expected = "[line 3] Error at 'unusedLocal': Unused local variable 'unusedLocal'.";
        assertEquals(expected, error);
    }

    @Test
    void testDuplicateFunctionParameterNamesNotAllowed() throws Exception {
        //Given
        String source = """
                fun add(a, b, a) {
                    return a + b + a;
                }
                """;

        //When
        String error = executeProgram(source).error;

        //Then
        String expected = "[line 1] Error at 'a': Variable with this name 'a' is already defined in this scope.";
        assertTrue(error.contains(expected));
    }

    @Test
    void testArrayDeclaration() throws Exception {
        //Given
        String source = """
                var arr = [1,2, true,"true",  "5"];
                println(arr);
                arr = [2]*3;
                println(arr);
                arr = [nil]*2;
                print(arr);
                """;

        //When
        String output = executeProgram(source).output;

        //Then
        String expected = """
                [1, 2, true, 'true', '5']
                [2, 2, 2]
                [nil, nil]""";
        assertEquals(expected, output);
    }

    @Test
    void testArrayIndexing() throws Exception {
        //Given
        String source = """
                var arr = [1,2, 3,4,  5];
                for (var i = 0; i < 5; i = i + 1) {
                    print(arr[i]);
                }
                """;

        //When
        String output = executeProgram(source).output;

        //Then
        assertEquals("12345", output);
    }

    @Test
    void testArrayModificationViaIndexing() throws Exception {
        //Given
        String source = """
                fun numSquares(arr) {
                    for (var i = 0; i < 5; i = i + 1) {
                       arr[i] = arr[i] * arr[i];
                    }
                    return arr;
                }
                print(numSquares([1, 2, 3, 4, 5]));
                """;
        //When
        String output = executeProgram(source).output;

        //Then
        assertEquals("[1, 4, 9, 16, 25]", output);
    }

    @Test
    void testMultiDimensionalArrays() throws Exception {
        //Given
        String source = """
                var arr = [[1, 2, 3], [4, 5, 6], [7, 8, 9]];
                println(arr);
                for (var i = 0; i < 3; i = i + 1) {
                    for (var j = 0; j < 3; j = j + 1) {
                        arr[i][j] = arr[i][j] * arr[i][j];
                    }
                }
                print(arr);
                """;

        //When
        String output = executeProgram(source).output;

        //Then
        String expected = """
                [[1, 2, 3], [4, 5, 6], [7, 8, 9]]
                [[1, 4, 9], [16, 25, 36], [49, 64, 81]]""";
        assertEquals(expected, output);
    }

    @Test
    void testMapDeclaration() throws Exception {
        //Given
        String source = """
                var map = {"a": 1, "b": 2, true:3, 4: "d"};
                print(map);
                """;

        //When
        String output = executeProgram(source).output;

        //Then
        assertTrue(output.contains("'a':1"));
        assertTrue(output.contains("'b':2"));
        assertTrue(output.contains("true:3"));
        assertTrue(output.contains("4:'d'"));
    }

    @Test
    void testMapPuttingAndRetrievingValuesFromMap() throws Exception {
        //Given
        String source = """
                var map = {"a": 1, "b": 2, "c":3, "d": [true, 5], 4: false};
                map["a"] = 11;
                map["d"][0] = false;
                map[4] = "false";
                println(map["a"]);
                println(map["d"][0]);
                println(map[4]);
                println(map["c"]);
                
                var anotherMap = {};
                print(anotherMap);
                """;

        //When
        String output = executeProgram(source).output;

        //Then
        String expected = """
                11
                false
                false
                3
                {}""";
        assertEquals(expected, output);
    }

    @Test
    void testClassDeclaration() throws Exception {
        //Given
        String source = """
                class List {
                    var array;
                    var size;
                    size() {
                        this.array = [];
                        this.size = array.length();
                    }
                    add(item) {
                        print(item);
                    }
                    get(index) {
                        print(index);
                    }
                    set(index, item) {
                        print(index);
                        print(item);
                    }
                }
                
                print(List);
                """;

        //When
        String output = executeProgram(source).output;

        //Then
        assertEquals("<class 'List'>", output);
    }

    @Test
    void testClassInstantiation() throws Exception {
        //Given
        String source = """
                class List {
                    var _arr;
                    var _length;
                    List() {
                        this.arr = [];
                        this.length = 0;
                    }
                    size() {
                        return this.length;
                    }
                    add(item) {
                        this.expandArr();
                        this.arr[this.length] = item;
                        this.length = this.length + 1;
                        return this;
                    }
                    get(index) {
                        return this.arr[index];
                    }
                    expandArr() {
                        var newSize = this.length * 2 + 1;
                        var newArr = [nil]*(newSize);
                        for (var i = 0; i < this.length; i = i + 1) {
                            newArr[i] = this.arr[i];
                        }
                        this.arr = newArr;
                    }
                }
                var list = List();
                var size = list.size;
                println(size); //<class method 'size'>
                println(size()); //0
                list.add(1).add(2.64).add(true).add("hello");
                println(size()); //4
                println(list.get(3)); //hello
                """;

        //When
        Console result = executeProgram(source);

        //Then
        String expectedOutput = """
                <method size>
                0
                4
                hello""";
        assertEquals(expectedOutput, result.output);
    }

    @Disabled("re-enable when Rion supports error handling")
    @Test
    void testCannotInvokeClassInitializerDirectly() throws Exception {
        //Given
        String source = """
                class Item {
                    Item() {}
                }
                var item = Item();
                item.Item(); //runtime error
                """;

        //When
        String expected = "[line 5] Undefined property 'Item' on '<instance of class 'Item'>'.";
        String error = executeProgram(source).error;

        //Then
        assertTrue(error.contains(expected));
    }

    @Test
    void testCannotReturnValueInsideClassInitializerButCanUseReturnStatementWithoutValue() throws Exception {
        //Given
        String source = """
                class Item {
                    Item() {
                        return; //allowed
                    }
                }
                class Bag {
                    Bag() {
                        return ""; //compile error
                    }
                }
                """;

        //When
        String expected = "[line 8] Error at ';': cannot return a value inside class initializer.";
        String error = executeProgram(source).error;

        //Then
        assertTrue(error.contains(expected));
    }

    @Disabled("re-enable when Rion supports error handling")
    @Test
    void testCannotAccessPrivateFieldOutsideOfClass() throws Exception {
        //Given
        String source = """
                class A {
                    var fieldIsPublic;
                    var _fieldIsPrivate;
                }
                var a = A();
                println(a.fieldIsPublic); //nil
                println(a.fieldIsPrivate); //runtime error
                """;

        //When
        Console result = executeProgram(source);

        //Then
        assertEquals("nil", result.output);
        assertTrue(result.error.contains("[line 7] field 'fieldIsPrivate' is private in class 'A'."));
    }

    @Disabled("re-enable when Rion supports error handling")
    @Test
    void testCannotAccessPrivateMethodOutsideOfClass() throws Exception {
        //Given
        String source = """
                class A {
                    methodIsPublic(){
                        this.methodIsPrivate();
                    }
                    _methodIsPrivate() {
                        println("I am private");
                    }
                }
                var a = A();
                a.methodIsPublic(); //I am private
                println(a.methodIsPrivate()); //runtime error
                """;

        //When
        Console result = executeProgram(source);

        //Then
        assertEquals("I am private", result.output);
        assertTrue(result.error.contains("[line 11] method 'methodIsPrivate' is private in class 'A'."));
    }

    @Disabled("re-enable when Rion supports error handline")
    @Test
    void testCannotAccessPrivateMethodOutsideOfClassII() throws Exception {
        //Given
        String source = """
                class A {
                    callPrivateMethod(){
                        B().methodIsPrivate();
                    }
                    callPublicMethod(){
                        B().methodIsPublic();
                    }
                }
                class B {
                    methodIsPublic(){
                        println("I am public");
                    }
                    _methodIsPrivate() {
                        println("I am private");
                    }
                }
                var a = A();
                a.callPublicMethod(); //I am public
                a.callPrivateMethod(); //runtime error
                """;

        //When
        Console result = executeProgram(source);

        //Then
        assertEquals("I am public", result.output);
        assertTrue(result.error.contains("[line 3] method 'methodIsPrivate' is private in class 'B'."));
    }

    @Test
    void testClassInheritance() throws Exception {
        //Given
        String source = """
                class Noodles {
                    cook() {
                        print("Cook with plenty pepper");
                    }
                }
                class Indomie < Noodles {}
                
                Indomie().cook();
                """;

        //When
        String output = executeProgram(source).output;

        //Then
        assertEquals("Cook with plenty pepper", output);
    }

    @Test
    void testClassNotAllowedToInheritItself() throws Exception {
        //Given
        String source = """
                class A < A {}
                """;

        //When
        String error = executeProgram(source).error;

        //Then
        assertEquals("[line 1] Error at 'A': A class can't inherit from itself", error);
    }

    @Test
    void testMethodOverriding() throws Exception {
        //Given
        String source = """
                class Noodles {
                    cook() {
                        print("Cook with plenty pepper");
                    }
                }
                class Indomie < Noodles {
                    cook() {
                        print("Cook Indomie with plenty pepper");
                    }
                }
                
                Indomie().cook();
                """;

        //When
        String output = executeProgram(source).output;

        //Then
        assertEquals("Cook Indomie with plenty pepper", output);
    }

    @Test
    void testCallingSuperClassMethods() throws Exception {
        //Given
        String source = """
                class A {
                    method() {
                        print("A method");
                    }
                }
                class B < A {
                    method() {
                        print("B method");
                    }
                    test() {
                        super.method();
                    }
                }
                class C < B {}
                
                C().test();
                """;

        //When
        String output = executeProgram(source).output;

        //Then
        assertEquals("A method", output);
    }

    @Test
    void testCallingSuperClassMethodsII() throws Exception {
        //Given
        String source = """
                class A {
                    method() {
                        println("A method");
                    }
                }
                class B < A {
                    method() {
                        println("B method");
                    }
                    test() {
                        super.method();
                    }
                }
                class C < B {
                    method() {
                        println("C method");
                    }
                    test() {
                        this.method();
                        super.method();
                        super.test();
                        println("C test");
                    }
                }
                
                C().test();
                """;

        //When
        String output = executeProgram(source).output;

        //Then
        String expected = """
                C method
                B method
                A method
                C test""";
        assertEquals(expected, output);
    }

    @Test
    void testCallingSuperConstructor() throws Exception {
        //Given
        String source = """
                class Noodles {
                    var brand;
                    Noodles(brand) {
                        this.brand = brand;
                    }
                    printBrand() {
                        println("This is " + this.brand + " Noodles");
                    }
                }
                class Indomie < Noodles {
                    Indomie() {
                        super("indomie");
                    }
                }
                class GoldenPenny < Noodles {
                    GoldenPenny() {
                        super("golden penny");
                    }
                }
                Indomie().printBrand();
                GoldenPenny().printBrand();
                """;

        //When
        String output = executeProgram(source).output;

        //Then
        String expected = """
                This is indomie Noodles
                This is golden penny Noodles""";
        assertEquals(expected, output);
    }

    @Test
    void testCallingSuperConstructorII() throws Exception {
        //Given
        String source = """
                class Animal {
                    var species;
                
                    Animal(species) {
                        this.species = species;
                    }
                
                    makeSound() {
                        println("Animal sound");
                    }
                }
                class Dog < Animal {
                    var breed;
                
                    Dog(species, breed) {
                        super(species);
                        this.breed = breed;
                    }
                
                    makeSound() {
                        println("I am a " + this.breed + " " + this.species);
                    }
                }
                var dog = Dog("Dog", "Labrador");
                dog.makeSound();
                """;

        //When
        String output = executeProgram(source).output;

        //Then
        String expected = "I am a Labrador Dog";
        assertEquals(expected, output);
    }

    @Test
    void testInvalidUseOfSuper() throws Exception {
        //Given
        String source = """
                class A {
                    method() {
                        super.method();
                    }
                }
                """;

        //When
        String error = executeProgram(source).error;

        //
        assertEquals("[line 3] Error at 'super': 'super' can't be used in a class with no superclass.", error);
    }

    @Test
    void executePrograms() throws Exception {
        Path path = Paths.get(pathToPrograms);
        AtomicBoolean isSuccess = new AtomicBoolean(true);
        AtomicInteger count = new AtomicInteger(0);

        Files.walkFileTree(path, new SimpleFileVisitor<>(){
            @NotNull
            @Override
            public FileVisitResult visitFile(
                    Path file,
                    @NotNull BasicFileAttributes attrs
            ) throws IOException {
                if (!isSourceFile(file)) {
                    return FileVisitResult.CONTINUE;
                }
                String fileName = file.getFileName().toString().split("\\.")[0];
                Path outputFile = Path.of(pathToPrograms, fileName + ".output");
                count.set(count.get() + 1);
                System.out.println("\n==========Running test: ==========" + count.get());
                if (!runProgram(file, outputFile)) {
                    isSuccess.set(false);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        System.out.println("\nExecuted a total of [" + count + "] test(s).");
        if (!isSuccess.get()) {
            fail();
        }
    }

    private boolean runProgram(Path sourceFile, Path outputFile) throws IOException {
        System.out.println("Running program from source file: [" + sourceFile + "]");
        //Given
        String expectedOutput = new String(Files.readAllBytes(outputFile)).trim();
        PrintStream originalOutputStream = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(new CustomOutputStream(originalOutputStream, outputStream)));

        String[] args = {sourceFile.toAbsolutePath().toString()};

        //When
        try {
            Rion.main(args);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Then
        String actualOutput = outputStream.toString().trim();
        try {
            assertEquals(expectedOutput, actualOutput);
            return true;
        } catch (AssertionFailedError error) {
            System.err.println("=========Failed due to the following reason=======");
            error.printStackTrace();
            System.err.println("====================End============================");
            return false;
        } finally {
            //cleanup
            System.setOut(originalOutputStream);
        }
    }

    @TempDir
    private static Path tempDir;

    private Console executeProgram(String source) throws IOException {
        PrintStream originalOutputStream = System.out;
        PrintStream originalErrorStream = System.out;
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        System.setOut(new PrintStream(new CustomOutputStream(originalOutputStream, outputStream)));
        System.setErr(new PrintStream(new CustomOutputStream(originalOutputStream, errorStream)));


        Path sourceFile = Files.createTempFile(tempDir, "source_", ".jrion");
        Files.writeString(sourceFile, source);

        String[] args = {sourceFile.toAbsolutePath().toString()};

        try {
            Rion.main(args);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.setOut(originalOutputStream);
            System.setErr(originalErrorStream);
        }

        return new Console(outputStream.toString().trim(), errorStream.toString().trim());
    }
    

    private boolean isSourceFile(Path path) {
        return path.toAbsolutePath().toString().endsWith(".jrion");
    }
    
    static class Console {
        String output;
        String error;
        public Console(String output, String error) {
            this.output = output;
            this.error = error;
        }
    }

    static class CustomOutputStream extends OutputStream {
        final OutputStream outputStreamA;
        final OutputStream outputStreamB;

        CustomOutputStream(
                OutputStream outputStreamA,
                OutputStream outputStreamB
        ) {
            this.outputStreamA = outputStreamA;
            this.outputStreamB = outputStreamB;
        }

        @Override
        public void write(int b) throws IOException {
            outputStreamA.write(b);
            outputStreamB.write(b);
        }

        @Override
        public void flush() throws IOException {
            outputStreamA.flush();
            outputStreamB.flush();
        }

        @Override
        public void close() throws IOException {
            outputStreamA.close();
            outputStreamB.close();
        }
    }

    private static void preventSystemExit() {
        SecurityManager securityManager = new SecurityManager() {
            @Override
            public void checkPermission(Permission perm) {
                //do nothing
            }

            @Override
            public void checkExit(int status) {
                super.checkExit(status);
                throw new RuntimeException("System exit '" + status + "' was prevented from happening.", null);
            }
        };
        System.setSecurityManager(securityManager);
    }
}
