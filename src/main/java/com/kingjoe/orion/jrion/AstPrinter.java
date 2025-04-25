package com.kingjoe.orion.jrion;

public class AstPrinter implements Expr.Visitor<String> {

    public String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpression(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitLogicalExpression(Expr.Logical expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpression(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpression(Expr.Literal expr) {
        if (expr == null) {
            return "nil";
        }
        return expr.value.toString();
    }

    @Override
    public String visitThisExpression(Expr.This expr) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public String visitUnaryExpression(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitVariableExpression(Expr.Variable expr) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public String visitAssignExpression(Expr.Assign expr) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public String visitIndexAssignExpression(Expr.IndexAssign expr) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public String visitCallExpression(Expr.Call expr) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public String visitIndexingExpression(Expr.Indexing expr) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public String visitGetExpression(Expr.Get expr) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public String visitAnonFunctionExpression(Expr.AnonFunc expr) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public String visitArrayExpression(Expr.Array expr) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public String visitMapExpression(Expr.Map expr) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public String visitSuperExpression(Expr.Super expr) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public String visitSetExpression(Expr.Set expr) {
        throw new IllegalStateException("Not implemented");
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("(").append(name);
        for (Expr expr : exprs) {
            stringBuilder.append(" ");
            stringBuilder.append(expr.accept(this));
        }
        stringBuilder.append(")");

        return stringBuilder.toString();
    }
}
