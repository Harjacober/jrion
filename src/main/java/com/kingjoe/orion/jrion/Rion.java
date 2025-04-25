package com.kingjoe.orion.jrion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Rion {

    private static boolean hadError = false;
    private static boolean hadRuntimeError = false;
    private static final Interpreter interpreter = new Interpreter();

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jrion [path to script]");
            System.exit(64);
        } else if (args.length == 1) {
            if (args[0].equals("--version")) {
                System.out.println("jrion version 0.1.0");
                System.exit(0);
            } else {
                runFile(args[0]);
            }
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        //check if file exists
        if (!Files.exists(Paths.get(path))) {
            System.out.println("File not found: " + path);
            System.exit(64);
        }
        byte[]  bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError) {
            System.exit(65);
        }
        if (hadRuntimeError) {
            System.exit(70);
        }
    }

    private static void runPrompt() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            while (true) {
                String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                System.out.print("\n[" + dateTime + "] >> ");
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                runRepl(line);
            }
        }
    }

    private static void run(String source) {
        List<Stmt> statements = getStatements(source);

        if (hadError) {
            hadError = false;
            return;
        }

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        if (hadError) {
            hadError = false;
            return;
        }

        interpreter.interpret(statements);
    }

    private static void runRepl(String source) {
        List<Stmt> statements = getStatements(source);

        if (hadError) {
            hadError = false;
            return;
        }

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        if (hadError) {
            hadError = false;
            return;
        }

        interpreter.repl(statements);
    }

    private static List<Stmt> getStatements(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        return parser.parse();
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    static void error(
            Token token,
            String message
    ) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(RuntimeError error) {
        System.err.println("[line " + error.token.line + "] " + error.getMessage());
        hadRuntimeError = true;
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

}
