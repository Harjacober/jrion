package com.kingjoe.orion.jrion;

import java.util.*;

public class Resolver implements Expr.Visitor<Object>, Stmt.Visitor<Object> {
    private final Interpreter interpreter;
    private final Stack<Map<String, VariableInfo>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;
    private ClassType currentClass = ClassType.NONE;

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    private enum FunctionType {
        NONE, FUNCTION, INITIALIZER, METHOD
    }

    private enum ClassType {
        NONE, CLASS, SUBCLASS
    }

    private enum VariableState {
        DECLARED, DEFINED, USED
    }

    private record VariableInfo(VariableState state, Token token, int index) { }

    @Override
    public Object visitBinaryExpression(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Object visitLogicalExpression(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Object visitGroupingExpression(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Object visitLiteralExpression(Expr.Literal expr) {
        return null;
    }

    @Override
    public Object visitThisExpression(Expr.This expr) {
        if (currentClass == ClassType.NONE) {
            Rion.error(expr.keyword, "'this' keyword not allowed here.");
        }
        resolveLocal(expr, expr.keyword);
        return null;
    }

    @Override
    public Object visitUnaryExpression(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Object visitVariableExpression(Expr.Variable expr) {
        //note: map.get returns null if key is not present, hence if a variable is not declared in the current scope,
        // the if block is not entered
        if (!scopes.isEmpty()) {
            VariableInfo variableInfo =  scopes.peek().get(expr.name.lexeme);
            if (variableInfo != null && variableInfo.state == VariableState.DECLARED) {
                Rion.error(expr.name, "Can't have local variable in its own initializer.");
            }
        }
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Object visitAssignExpression(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Object visitIndexAssignExpression(Expr.IndexAssign expr) {
        resolve(expr.indexee);
        resolve(expr.index);
        resolve(expr.value);
        return null;
    }

    @Override
    public Object visitCallExpression(Expr.Call expr) {
        resolve(expr.callee);
        for (Expr args : expr.arguments) {
            resolve(args);
        }
        return null;
    }

    @Override
    public Object visitIndexingExpression(Expr.Indexing expr) {
        resolve(expr.indexee);
        resolve(expr.index);
        return null;
    }

    @Override
    public Object visitGetExpression(Expr.Get expr) {
        resolve(expr.object);
        return null;
    }

    @Override
    public Object visitAnonFunctionExpression(Expr.AnonFunc expr) {
        resolveAnonFunction(expr);
        return null;
    }

    @Override
    public Object visitArrayExpression(Expr.Array expr) {
        for (Expr elemExpr : expr.elements) {
            resolve(elemExpr);
        }
        return null;
    }

    @Override
    public Object visitMapExpression(Expr.Map expr) {
        for (Expr elemExpr : expr.keys) {
            resolve(elemExpr);
        }
        for (Expr elemExpr : expr.values) {
            resolve(elemExpr);
        }
        return null;
    }

    @Override
    public Object visitSuperExpression(Expr.Super expr) {
        if (currentClass == ClassType.NONE) {
            Rion.error(expr.keyword, "'super' is only allowed inside a class.");
        } else if (currentClass != ClassType.SUBCLASS) {
            Rion.error(expr.keyword, "'super' can't be used in a class with no superclass.");
        }
        if (expr.arguments != null) {
            for (Expr args : expr.arguments) {
                resolve(args);
            }
        }
        return null;
    }

    @Override
    public Object visitSetExpression(Expr.Set expr) {
        resolve(expr.object);
        resolve(expr.value);
        return null;
    }

    @Override
    public Object visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Object visitVarStmt(Stmt.Var stmt) {
        declare(stmt.name);
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    @Override
    public Object visitClassStmt(Stmt.Class stmt) {
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;

        declare(stmt.name);
        define(stmt.name);



        beginScope();
        defineKeywordInScope(stmt.name, "this");

        if (stmt.superClass != null) {
            if (stmt.name.lexeme.equals(stmt.superClass.name.lexeme)) {
                Rion.error(stmt.superClass.name, "A class can't inherit from itself");
            }
            currentClass = ClassType.SUBCLASS;
            resolve(stmt.superClass);
        }

        FunctionType declaration = FunctionType.METHOD;
        for (Stmt.Function method : stmt.methods) {
            if (method.name.lexeme.equals(stmt.name.lexeme)) {
                declaration = FunctionType.INITIALIZER;
            }
            resolveFunction(method, declaration);
            declaration = FunctionType.METHOD;
        }

        endScope();

        currentClass = enclosingClass;
        return null;
    }

    @Override
    public Object visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.name);
        define(stmt.name);
        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Object visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        checkUnusedVariables(scopes.peek());
        endScope();
        return null;
    }

    @Override
    public Object visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) {
            resolve(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Object visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Object visitReturnStmt(Stmt.Return stmt) {
        if (currentFunction == FunctionType.NONE) {
            Rion.error(stmt.keyword, "return statement is not allowed here.");
        } else if (currentFunction == FunctionType.INITIALIZER && stmt.expression != null) {
            Rion.error(stmt.keyword, "cannot return a value inside class initializer.");
        }

        if (stmt.expression != null) {
            resolve(stmt.expression);
        }
        return null;
    }

    @Override
    public Object visitBreakStmt(Stmt.Break stmt) {
        return null;
    }

    @Override
    public Object visitContinueStmt(Stmt.Continue stmt) {
        return null;
    }

    public void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        scopes.pop();
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) {
            //variable is declared globally
            return;
        }

        Map<String, VariableInfo> scope = scopes.peek();
        if (scope.containsKey(name.lexeme)) {
            Rion.error(name, "Variable with this name '" + name.lexeme + "' is already defined in this scope.");
        }
        int index = scope.size();
        scope.put(name.lexeme, new VariableInfo(VariableState.DECLARED, name, index));
    }


    private void define(Token name) {
        if (scopes.isEmpty()) {
            return;
        }
        Map<String, VariableInfo> scope = scopes.peek();
        int index = scope.get(name.lexeme).index;
        scope.put(name.lexeme, new VariableInfo(VariableState.DEFINED, name, index));
    }

    private void defineKeywordInScope(Token classToken, String name) {
        if (scopes.isEmpty()) {
            return;
        }
        Token keyword = new Token(TokenType.THIS, name, name, classToken.line);
        Map<String, VariableInfo> scope = scopes.peek();
        int index = scope.size();
        // since we are defining "this" as a local variable, to prevent a "variable not used" runtime error
        // we mark it as used by default in-case it never gets used in a method
        scope.put(keyword.lexeme, new VariableInfo(VariableState.USED, keyword, index));
    }

    private void resolveLocal(
            Expr expr,
            Token name
    ) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                //current innermost scope = 0, enclosing scope = 1, and so on... walking upwards
                int index = scopes.get(i).get(name.lexeme).index;
                interpreter.resolve(expr, scopes.size() - 1 - i, index);
                markVariableAsUsed(name, i, index);
                return;
            }
            //if the variable is not found in all the local scopes, we leave it unresolved and assume it is global
        }
    }

    private void markVariableAsUsed(
            Token name,
            int scopeIndex,
            int index
    ) {
        scopes.get(scopeIndex).put(name.lexeme, new VariableInfo(VariableState.USED, name, index));
    }

    private void resolveFunction(
            Stmt.Function function,
            FunctionType functionType
    ) {
        resolveFunction(functionType, function.parameters, function.body);
    }

    private void resolveAnonFunction(
            Expr.AnonFunc function
    ) {
        resolveFunction(FunctionType.FUNCTION, function.parameters, function.body);
    }

    private void resolveFunction(
            FunctionType type,
            List<Token> parameters,
            Stmt body
    ) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;
        beginScope();
        for (Token param : parameters) {
            declare(param);
            define(param);
        }
        resolve(body);
        checkUnusedVariables(scopes.peek());
        endScope();
        currentFunction = enclosingFunction;
    }

    private void checkUnusedVariables(Map<String, VariableInfo> scope) {
        scope.forEach((name, info) -> {
            if (info.state != VariableState.USED) {
                Rion.error(info.token, "Unused local variable '" + name + "'.");
            }
        });
    }
}
