package com.kingjoe.orion.jrion;

import java.util.List;

abstract public class Stmt {

    abstract <E> E accept(Visitor<E> visitor);

    public static class Expression extends Stmt {
        final Expr expression;

        Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        <E> E accept(Visitor<E> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    public static class Class extends Stmt {
        final Token name;
        final Expr.Variable superClass;
        final List<Stmt.Var> fields;
        final List<Stmt.Function> methods;

        public Class(
                Token name,
                Expr.Variable superClass,
                List<Stmt.Var> fields,
                List<Stmt.Function> methods
        ) {
            this.name = name;
            this.superClass = superClass;
            this.fields = fields;
            this.methods = methods;
        }

        @Override
        <E> E accept(Visitor<E> visitor) {
            return visitor.visitClassStmt(this);
        }
    }

    public static class Function extends Stmt {
        final Token name;
        final List<Token> parameters;
        final Stmt body;

        Function(
                Token name,
                List<Token> parameters,
                Stmt body
        ) {
            this.name = name;
            this.parameters = parameters;
            this.body = body;
        }

        @Override
        <E> E accept(Visitor<E> visitor) {
            return visitor.visitFunctionStmt(this);
        }
    }

    public static class Var extends Stmt {
        final Token name;
        final Expr initializer;

        Var(
                Token name,
                Expr initializer
        ) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        <E> E accept(Visitor<E> visitor) {
            return visitor.visitVarStmt(this);
        }
    }

    public static class Block extends Stmt {
        final List<Stmt> statements;

        Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        <E> E accept(Visitor<E> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    public static class If extends Stmt {
        final Expr condition;
        final Stmt thenBranch;
        final Stmt elseBranch;
        final Token keyword;


        If(
                Expr condition,
                Stmt thenBranch,
                Stmt elseBranch,
                Token keyword
        ) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
            this.keyword = keyword;
        }

        @Override
        <E> E accept(Visitor<E> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    public static class While extends Stmt {
        final Expr condition;
        final Stmt body;
        final Token keyword;

        While(
                Expr condition,
                Stmt body,
                Token keyword
        ) {
            this.condition = condition;
            this.body = body;
            this.keyword = keyword;
        }

        @Override
        <E> E accept(Visitor<E> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }

    public static class Return extends Stmt {
        final Token keyword;
        final Expr expression;

        Return(
                Token keyword,
                Expr expression) {
            this.keyword = keyword;
            this.expression = expression;
        }

        @Override
        <E> E accept(Visitor<E> visitor) {
            return visitor.visitReturnStmt(this);
        }
    }

    public static class Break extends Stmt {
        @Override
        <E> E accept(Visitor<E> visitor) {
            return visitor.visitBreakStmt(this);
        }
    }

    public static class Continue extends Stmt {
        @Override
        <E> E accept(Visitor<E> visitor) {
            return visitor.visitContinueStmt(this);
        }
    }


    interface Visitor<T> {
        T visitExpressionStmt(Expression stmt);
        T visitVarStmt(Var stmt);
        T visitClassStmt(Class stmt);
        T visitFunctionStmt(Function stmt);
        T visitBlockStmt(Block stmt);
        T visitIfStmt(If stmt);
        T visitWhileStmt(While stmt);
        T visitReturnStmt(Return stmt);
        T visitBreakStmt(Break stmt);
        T visitContinueStmt(Continue stmt);
    }
}
