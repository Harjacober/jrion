package com.kingjoe.orion.jrion;

import com.kingjoe.orion.jrion.builtin.RionArray;
import com.kingjoe.orion.jrion.builtin.RionIndexable;
import com.kingjoe.orion.jrion.builtin.RionMap;

import java.util.*;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Object> {
    Environment globals = new Environment();
    Environment environment = globals;
    Map<Expr, VariableInfo> locals = new HashMap<>();

    public Interpreter() {
        NativeFunction.load(globals);
    }

    public record VariableInfo(int depth, int index){}

    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt stmt : statements) {
                execute(stmt);
            }
        } catch (RuntimeError e) {
            Rion.runtimeError(e);
        }
    }

    public void repl(List<Stmt> statements) {
        try {
            for (Stmt stmt : statements) {
                Object value = execute(stmt);
                if (value != null) {
                    printLine(value);
                }
            }
        } catch (RuntimeError e) {
            Rion.runtimeError(e);
        }
    }

    @Override
    public Object visitBinaryExpression(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS -> {
                checkNumberOperand(expr.operator, left, right);
                return (double) left - (double) right;
            }
            case SLASH -> {
                checkNumberOperand(expr.operator, left, right);
                if ((double) right == 0) {
                    throw new RuntimeError(expr.operator, "Invalid operation, division by zero.");
                }
                return (double) left / (double) right;
            }
            case MODULO -> {
                checkNumberOperand(expr.operator, left, right);
                return (double) left % (double) right;
            }
            case GREATER -> {
                checkNumberOperand(expr.operator, left, right);
                return (double) left > (double) right;
            }
            case GREATER_EQUAL -> {
                checkNumberOperand(expr.operator, left, right);
                return (double) left >= (double) right;
            }
            case LESS -> {
                checkNumberOperand(expr.operator, left, right);
                return (double) left < (double) right;
            }
            case LESS_EQUAL -> {
                checkNumberOperand(expr.operator, left, right);
                return (double) left <= (double) right;
            }
            case BANG_EQUAL -> {
                return !isEqual(left, right);
            }
            case EQUAL_EQUAL -> {
                return isEqual(left, right);
            }
            case STAR -> {
                if (isNumber(left) && isNumber(right)) {
                    return (double) left * (double) right;
                }
                if (isString(left) && isWholeNumber(right)) {
                    int count = Integer.parseInt(stringify(right));
                    return ((String) left).repeat(count);
                }
                if (left instanceof RionArray array && isWholeNumber(right)) {
                    return initializedFixedSizeArray(expr.operator, array, right);
                }
                throw new RuntimeError(expr.operator, "cannot perform '*' operation on the provided type");
            }
            case PLUS -> {
                if (isNumber(left) && isNumber(right)) {
                    return (double) left + (double) right;
                }
                if (isString(left) && isString(right)) {
                    return left.toString() + right;
                }
                if (isString(left)) {
                    return left + stringify(right);
                }
                if (isString(right)) {
                    return stringify(left) + right;
                }
                throw new RuntimeError(expr.operator, "cannot perform  '+' operation on the provided type");
            }
        }
        return null;
    }

    @Override
    public Object visitLogicalExpression(Expr.Logical expr) {
        switch (expr.operator.type) {
            case OR -> {
                return isTruthy(expr.left)|| isTruthy(expr.right);
            }
            case AND -> {
                return isTruthy(expr.left) && isTruthy(expr.right);
            }
        }
        return null;
    }

    @Override
    public Object visitGroupingExpression(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpression(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitThisExpression(Expr.This expr) {
        return lookupVariable(expr.keyword, expr);
    }

    @Override
    public Object visitUnaryExpression(Expr.Unary expr) {
        switch (expr.operator.type) {
            case BANG -> {
                return !isTruthy(expr.right);
            }
            case MINUS -> {
                Object right = evaluate(expr.right);
                return -(double) right;
            }
        }
        return null;
    }

    @Override
    public Object visitVariableExpression(Expr.Variable expr) {
        return lookupVariable(expr.name, expr);
    }

    private Object lookupVariable(Token name, Expr expr) {
        VariableInfo variableInfo = locals.get(expr);
        if (variableInfo != null) {
            //return environment.get(expr.name); //look up variables by key in map
            return environment.getAt(variableInfo.depth, variableInfo.index);
        } else {
            return globals.get(name);
        }
    }

    @Override
    public Object visitAssignExpression(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        VariableInfo variableInfo = locals.get(expr);
        if (variableInfo != null) {
            //environment.assign(expr.name, value); //look up variables by key in map and assign value
            environment.assignAt(expr.name, value, variableInfo.depth, variableInfo.index); //look up local variables by index
        } else {
            globals.assign(expr.name, value);
        }
        return value;
    }

    @Override
    public Object visitIndexAssignExpression(Expr.IndexAssign expr) {
        Object indexee = evaluate(expr.indexee);
        if (!(indexee instanceof RionIndexable indexable)) {
            throw new RuntimeError(expr.squareBrace, "Can only index array or map builtin types");
        }
        Object index = evaluate(expr.index);
        Object value = evaluate(expr.value);

        indexable.set(expr.squareBrace, index, value);
        return null;
    }

    @Override
    public Object visitCallExpression(Expr.Call expr) {
        Object callee = evaluate(expr.callee);
        if (!(callee instanceof RionCallable callable)) {
            throw new RuntimeError(expr.paren, "Can only call functions and classes");
        }

        // checking for no of args passed at runtime is costly, should we attempt to adopt smalltalk approach of
        // looking up func name based on the no of args passed?
        if (expr.arguments.size() != callable.getArity()) {
            throw new RuntimeError(expr.paren, "Expected " + callable.getArity() + " arguments, but got " + expr.arguments.size());
        }

        List<Object> arguments = new ArrayList<>();
        for (Expr arg : expr.arguments) {
            arguments.add(evaluate(arg));
        }

        return callable.call(this, arguments);
    }

    @Override
    public Object visitIndexingExpression(Expr.Indexing expr) {
        Object indexee = evaluate(expr.indexee);
        if (!(indexee instanceof RionIndexable indexable)) {
            throw new RuntimeError(expr.squareBrace, "can only index array or map builtin types");
        }

        Object index = evaluate(expr.index);

        return indexable.get(expr.squareBrace, index);
    }

    @Override
    public Object visitGetExpression(Expr.Get expr) {
        Object object = evaluate(expr.object);
        if (object instanceof RionInstance instance) {
            return instance.get(environment, expr.property);
        }
        if (object instanceof RionArray array) {
            return array.getProperty(expr.property, expr.property.lexeme);
        }
        if (object instanceof RionMap map) {
            return map.getProperty(expr.property, expr.property.lexeme);
        }

        throw new RuntimeError(expr.property, "Only instances have properties.");
    }

    @Override
    public Object visitSetExpression(Expr.Set expr) {
        if (!(evaluate(expr.object) instanceof RionInstance instance)) {
            throw new RuntimeError(expr.property, "Only instances have fields.");
        }
        return instance.set(environment, expr.property, evaluate(expr.value));
    }

    @Override
    public Object visitAnonFunctionExpression(Expr.AnonFunc expr) {
        Token name = new Token(TokenType.IDENTIFIER, "anonymous", null, -1);
        Stmt.Function stmt = new Stmt.Function(name, expr.parameters, expr.body);
        return new RionFunction(stmt, environment, "fn");
    }

    @Override
    public Object visitArrayExpression(Expr.Array expr) {
        List<Object> elements = new ArrayList<>();
        for (Expr elemExpr : expr.elements) {
            elements.add(evaluate(elemExpr));
        }
        return new RionArray(this, elements);
    }

    @Override
    public Object visitMapExpression(Expr.Map expr) {
        List<Object> keys = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (int i = 0; i < expr.keys.size(); i++) {
            keys.add(evaluate(expr.keys.get(i)));
            values.add(evaluate(expr.values.get(i)));
        }

        return new RionMap(this, expr.brace, keys, values);
    }

    @Override
    public Object visitSuperExpression(Expr.Super expr) {
        RionClass superClass = (RionClass) environment.get(new Token(TokenType.THIS, "super", "super", 0));
        RionInstance rionInstance = (RionInstance) environment.get(new Token(TokenType.THIS, "this", "this", 0));

        if (expr.method != null) {
            AbstractMap.SimpleImmutableEntry<RionFunction, RionClass> methodRes = superClass.findMethod(environment, expr.method);
            if (methodRes != null) {
                return methodRes.getKey().bind(rionInstance, methodRes.getValue());
            }

            throw new RuntimeError(expr.method, "Undefined property '" + expr.method.lexeme + "'");
        } else if (expr.arguments != null) {
            //we only allow access to the initializer of the immediate parent class when super(args*) is called.
            Optional<RionFunction> initializer = superClass.getClassInitializer();
            if (initializer.isPresent()) {
                List<Object> args = new ArrayList<>();
                for (Expr argExpr : expr.arguments) {
                    args.add(evaluate(argExpr));
                }

                return initializer.get().bind(rionInstance, superClass).call(this, args);
            } else {
                throw new RuntimeError(expr.keyword, "Superclass does not have an initializer.");
            }
        }

        return null;
    }

    @Override
    public Object visitExpressionStmt(Stmt.Expression stmt) {
        return evaluate(stmt.expression);
    }

    @Override
    public Object visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Object visitClassStmt(Stmt.Class stmt) {
        RionClass superClass = null;
        if (stmt.superClass != null) {
            if (evaluate(stmt.superClass) instanceof RionClass rionSuperClass) {
                superClass = rionSuperClass;
            } else {
                throw new RuntimeError(stmt.superClass.name, "Superclass must be a class");
            }
        }

        environment.define(stmt.name.lexeme, null);

        Set<String> fields = new HashSet<>();
        for (Stmt.Var varStmt : stmt.fields) {
            fields.add(varStmt.name.lexeme);
        }

        Map<String, RionFunction> methods = new HashMap<>();
        for (Stmt.Function method : stmt.methods) {
            methods.put(method.name.lexeme, new RionFunction(method, environment, "method"));
        }

        RionClass rionClass = new RionClass(stmt.name, superClass, fields, methods);
        environment.assign(stmt.name, rionClass);
        return null;
    }

    @Override
    public Object visitFunctionStmt(Stmt.Function stmt) {
        RionFunction function = new RionFunction(stmt, environment, "fn");
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Object visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt, new Environment(this.environment));
        return null;
    }

    public void executeBlock(Stmt.Block statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements.statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Object visitIfStmt(Stmt.If stmt) {
        if (isTruthy(stmt.condition)) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null){
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Object visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(stmt.condition)) {
            try {
                execute(stmt.body);
            } catch (Signal.Break s) {
                break;
            } catch (Signal.Continue s) {
                continue;
            }
        }
        return null;
    }

    @Override
    public Object visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.expression != null) {
            value = evaluate(stmt.expression);
        }
        throw new Signal.Return(value);
    }

    @Override
    public Object visitBreakStmt(Stmt.Break stmt) {
        throw new Signal.Break();
    }

    @Override
    public Object visitContinueStmt(Stmt.Continue stmt) {
        throw new Signal.Continue();
    }

    private Object execute(Stmt stmt) {
        return stmt.accept(this);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private boolean isNumber(Object o) {
        if (o == null) {
            return false;
        }
        return o instanceof Double;
    }

    public boolean isWholeNumber(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof Double)) {
            return false;
        }
        return ((double) o) * 10 % 10 == 0;
    }

    private boolean isString(Object o) {
        if (o == null) {
            return false;
        }
        return o instanceof String;
    }

    private boolean isEqual(
            Object o1,
            Object o2
    ) {
        if (o1 == null && o2 == null) {
            return true;
        }
        if (o1 == null) {
            return false;
        }
        return o1.equals(o2);
    }

    private void checkNumberOperand(
            Token operator,
            Object left,
            Object right
    ) {
        if (left instanceof Double && right instanceof Double) {
            return;
        }
        throw new RuntimeError(operator, "Operands must be a number.");
    }

    public String stringify(Object o) {
        if (o == null) {
            return "nil";
        }
        if (o instanceof Double) {
            if (((double) o) * 10 % 10 == 0) {
                String text = o.toString();
                return text.substring(0, text.length() - 2);
            }
        }
        if (o instanceof String) {
            return "'" + o + "'";
        }
        return o.toString();
    }

    public String prettyPrint(Object o) {
        if (o == null) {
            return "nil";
        }
        if (o instanceof Double) {
            if (((double) o) * 10 % 10 == 0) {
                String text = o.toString();
                return text.substring(0, text.length() - 2);
            }
        }
        return o.toString();
    }

    private boolean isTruthy(Expr expr) {
        Object value = evaluate(expr);
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (boolean) value;
        }
        return true;
    }

    private void printLine(Object value) {
        System.out.println(stringify(value));
    }

    public void resolve(
            Expr expr,
            int depth,
            int index
    ) {
        locals.put(expr, new VariableInfo(depth, index));
    }

    private Object initializedFixedSizeArray(
            Token operator,
            RionArray array,
            Object right
    ) {
        if (array.size() != 1) {
            throw new RuntimeError(operator, "must provide only one initial value for array initializer.");
        }
        int length = Integer.parseInt(stringify(right));
        Object element = array.getElements().getFirst();
        List<Object> elements = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            elements.add(element);
        }
        return new RionArray(this, elements);
    }
}
