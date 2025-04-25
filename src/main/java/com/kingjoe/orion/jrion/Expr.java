package com.kingjoe.orion.jrion;

import java.util.List;

public abstract class Expr {

    abstract <E> E accept(Visitor<E> visitor);

    public static class Binary extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;

        Binary(
                Expr left,
                Token operator,
                Expr right
        ) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <E> E accept(Visitor<E> visitor) {
            return visitor.visitBinaryExpression(this);
        }
    }

    public static class Logical extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;

        Logical(
                Expr left,
                Token operator,
                Expr right
        ) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <E> E accept(Visitor<E> visitor) {
            return visitor.visitLogicalExpression(this);
        }
    }

    public static class Grouping extends Expr {
        final Expr expression;

        Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        <E> E accept(Visitor<E> visitor) {
            return visitor.visitGroupingExpression(this);
        }
    }

    public static class Literal extends Expr {
        final Object value;

        Literal(Object value) {
            this.value = value;
        }

        @Override
        <E> E accept(Visitor<E> visitor) {
            return visitor.visitLiteralExpression(this);
        }
    }

    public static class This extends Expr {
        final Token keyword;

        public This(Token keyword) {
            this.keyword = keyword;
        }

        @Override
        <E> E accept(Visitor<E> visitor) {
            return visitor.visitThisExpression(this);
        }
    }

    public static class Unary extends Expr {
        final Token operator;
        final Expr right;

        Unary(
                Token operator,
                Expr right
        ) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        <E> E accept(Visitor<E> visitor) {
            return visitor.visitUnaryExpression(this);
        }
    }

    /*
     * A variable usage refers to the preceding declaration of that variable
     * in the innermost scope that encloses the expression where the variable is used.
     */
    public static class Variable extends Expr {
        final Token name;

        Variable(Token name) {
            this.name = name;
        }

        @Override
        <E> E accept(Visitor<E> visitor) {
            return visitor.visitVariableExpression(this);
        }
    }

    public static class Assign extends Expr {
        final Token name;
        final Expr value;


        public Assign(
                Token name,
                Expr value
        ) {
            this.name = name;
            this.value = value;
        }

        @Override
        <E> E accept(Visitor<E> visitor) {
            return visitor.visitAssignExpression(this);
        }
    }

    public static class IndexAssign extends Expr {
        final Token squareBrace;
        final Expr indexee;
        final Expr index;
        final Expr value;

        public IndexAssign(
                Token squareBrace,
                Expr indexee,
                Expr index,
                Expr value
        ) {
            this.squareBrace = squareBrace;
            this.indexee = indexee;
            this.index = index;
            this.value = value;
        }

        @Override
        <E> E accept(Visitor<E> visitor) {
            return visitor.visitIndexAssignExpression(this);
        }
    }

    public static class Call extends Expr {
        final Expr callee;
        final Token paren;
        final List<Expr> arguments;

        public Call(
                Expr callee,
                Token paren,
                List<Expr> arguments
        ) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }

        @Override
        <E> E accept(Visitor<E> visitor) {
            return visitor.visitCallExpression(this);
        }
    }

    public static class Indexing extends Expr {
        final Expr indexee;
        final Token squareBrace;
        final Expr index;

        public Indexing(
                Expr indexee,
                Token squareBrace,
                Expr index
        ) {
            this.indexee = indexee;
            this.squareBrace = squareBrace;
            this.index = index;
        }

        @Override
        <E> E accept(Visitor<E> visitor) {
            return visitor.visitIndexingExpression(this);
        }
    }

    public static class Get extends Expr {
        final Expr object;
        final Token property;

        public Get(
                Expr object,
                Token property
        ) {
            this.object = object;
            this.property = property;
        }

        @Override
        <E> E accept(Visitor<E> visitor) {
            return visitor.visitGetExpression(this);
        }
    }

    public static class Set extends Expr {
        final Expr object;
        final Token property;
        final Expr value;

        public Set(
                Expr object,
                Token property,
                Expr value
        ) {
            this.object = object;
            this.property = property;
            this.value = value;
        }

        @Override
        <E> E accept(Visitor<E> visitor) {
            return visitor.visitSetExpression(this);
        }
    }

    public static class AnonFunc extends Expr {
        final Token paren;
        final List<Token> parameters;
        final Stmt body;

        public AnonFunc(
                Token paren,
                List<Token> parameters,
                Stmt body
        ) {
            this.paren = paren;
            this.parameters = parameters;
            this.body = body;
        }

        @Override
        <E> E accept(Visitor<E> visitor) {
            return visitor.visitAnonFunctionExpression(this);
        }
    }

    public static class Array extends Expr {
        final Token squareBrace;
        final List<Expr> elements;

        public Array(
                Token squareBrace,
                List<Expr> elements
        ) {
            this.squareBrace = squareBrace;
            this.elements = elements;
        }

        @Override
        <E> E accept(Visitor<E> visitor) {
            return visitor.visitArrayExpression(this);
        }
    }

    public static class Map extends Expr {
        final Token brace;
        final List<Expr> keys;
        final List<Expr> values;

        public Map(
                Token brace,
                List<Expr> keys,
                List<Expr> values
        ) {
            this.brace = brace;
            this.keys = keys;
            this.values = values;
        }

        @Override
        <E> E accept(Visitor<E> visitor) {
            return visitor.visitMapExpression(this);
        }
    }

    public static class Super extends Expr {
        final Token keyword;
        Token method;
        Token paren;
        List<Expr> arguments;

        public Super(
                Token keyword,
                Token method
        ) {
            this.keyword = keyword;
            this.method = method;
        }

        public Super(
                Token keyword,
                Token paren,
                List<Expr> arguments
        ) {
            this.keyword = keyword;
            this.paren = paren;
            this.arguments = arguments;
        }

        @Override
        <E> E accept(Visitor<E> visitor) {
            return visitor.visitSuperExpression(this);
        }
    }

    interface Visitor<T> {
        T visitBinaryExpression(Binary expr);
        T visitLogicalExpression(Logical expr);
        T visitGroupingExpression(Grouping expr);
        T visitLiteralExpression(Literal expr);
        T visitThisExpression(This expr);
        T visitUnaryExpression(Unary expr);
        T visitVariableExpression(Variable expr);
        T visitAssignExpression(Assign expr);
        T visitIndexAssignExpression(IndexAssign expr);
        T visitCallExpression(Call expr);
        T visitIndexingExpression(Indexing expr);
        T visitGetExpression(Get expr);
        T visitSetExpression(Set expr);
        T visitAnonFunctionExpression(AnonFunc expr);
        T visitArrayExpression(Array expr);
        T visitMapExpression(Map expr);
        T visitSuperExpression(Super expr);
    }
}
