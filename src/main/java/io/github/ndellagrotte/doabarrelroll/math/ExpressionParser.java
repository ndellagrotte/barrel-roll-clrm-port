package io.github.ndellagrotte.doabarrelroll.math;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ExpressionParser extends Parser {
    private static final Random RANDOM = new Random();

    private Expression compiled;
    private RuntimeException error;

    public static Expression parse(String string) {
        return new ExpressionParser(string).build();
    }

    public ExpressionParser(String string) {
        super(string);
    }

    public Expression getCompiled() {
        if (compiled == null && error == null) {
            try {
                return build();
            } catch (RuntimeException e) {
                error = e;
                return null;
            }
        }
        return compiled;
    }

    public Expression getCompiledOrDefaulting(double defaultValue) {
        return hasError() ? vars -> defaultValue : getCompiled();
    }

    public RuntimeException getError() {
        getCompiled();
        return error;
    }

    public boolean hasError() {
        getCompiled();
        return error != null;
    }

    public Expression build() {
        nextChar();
        Expression x = parseExpression();
        if (pos < string.length()) throw new RuntimeException("Unexpected character '" + ch + "' at position " + pos);
        compiled = x;
        return x;
    }

    // Grammar:
    // expression = term | expression `+` term | expression `-` term
    // term = factor | term `*` factor | term `/` factor
    // factor = `+` factor | `-` factor | `(` expression `)` | number
    //        | functionName `(` expression `)` | functionName factor
    //        | factor `^` factor
    private Expression parseExpression() {
        Expression x = parseTerm();
        while (true) {
            if (weat('+')) { // addition
                Expression a = x;
                Expression b = parseTerm();
                x = vars -> a.eval(vars) + b.eval(vars);
            } else if (weat('-')) { // subtraction
                Expression a = x;
                Expression b = parseTerm();
                x = vars -> a.eval(vars) - b.eval(vars);
            } else return x;
        }
    }

    private Expression parseTerm() {
        Expression x = parseFactor();
        while (true) {
            if (weat('*')) { // multiplication
                Expression a = x;
                Expression b = parseFactor();
                x = vars -> a.eval(vars) * b.eval(vars);
            } else if (weat('/')) { // division
                Expression a = x;
                Expression b = parseFactor();
                x = vars -> a.eval(vars) / b.eval(vars);
            } else return x;
        }
    }

    private Expression parseFactor() {
        if (weat('+')) { // unary plus
            Expression a = parseFactor();
            return vars -> +a.eval(vars);
        }
        if (weat('-')) { // unary minus
            Expression a = parseFactor();
            return vars -> -a.eval(vars);
        }
        Expression x;
        int startPos = pos;
        if (weat('(')) { // parentheses
            x = parseExpression();
            if (!weat(')')) throw new RuntimeException("Missing ')' at position " + pos);
        } else if (ch >= '0' && ch <= '9' || ch == '.') { // number literals
            while (ch >= '0' && ch <= '9' || ch == '.') nextChar();
            double a = Double.parseDouble(string.substring(startPos, pos));
            x = vars -> a;
        } else if (isVariableChar()) {
            while (isVariableChar()) nextChar();
            String name = string.substring(startPos, pos);
            List<Expression> args = new ArrayList<>();
            if (weat('(')) { // functions
                do {
                    args.add(parseExpression());
                } while (weat(','));
                if (!weat(')')) throw new RuntimeException("Missing ')' after argument to '" + name + "'");
                if (args.size() == 1) {
                    Expression a = args.get(0);
                    switch (name) {
                        case "sqrt": x = vars -> Math.sqrt(a.eval(vars)); break;
                        case "sin": x = vars -> Math.sin(a.eval(vars)); break;
                        case "cos": x = vars -> Math.cos(a.eval(vars)); break;
                        case "tan": x = vars -> Math.tan(a.eval(vars)); break;
                        case "asin": x = vars -> Math.asin(a.eval(vars)); break;
                        case "acos": x = vars -> Math.acos(a.eval(vars)); break;
                        case "atan": x = vars -> Math.atan(a.eval(vars)); break;
                        case "abs": x = vars -> Math.abs(a.eval(vars)); break;
                        case "ceil": x = vars -> Math.ceil(a.eval(vars)); break;
                        case "floor": x = vars -> Math.floor(a.eval(vars)); break;
                        case "log": x = vars -> Math.log(a.eval(vars)); break;
                        case "round": x = vars -> Math.round(a.eval(vars)); break;
                        case "randint": x = vars -> RANDOM.nextInt((int) a.eval(vars)); break;
                        default:
                            throw new RuntimeException("Unknown function '" + name + "' for 1 arg at position " + (pos - name.length()));
                    }
                } else if (args.size() == 2) {
                    Expression a = args.get(0);
                    Expression b = args.get(1);
                    switch (name) {
                        case "min": x = vars -> Math.min(a.eval(vars), b.eval(vars)); break;
                        case "max": x = vars -> Math.max(a.eval(vars), b.eval(vars)); break;
                        case "randint":
                            x = vars -> {
                                double av = a.eval(vars);
                                return av + RANDOM.nextInt((int) (b.eval(vars) - av));
                            };
                            break;
                        default:
                            throw new RuntimeException("Unknown function '" + name + "' for 2 args at position " + (pos - name.length()));
                    }
                } else {
                    throw new RuntimeException("Unknown function '" + name + "' for " + args.size() + " args at position " + (pos - name.length()));
                }
            } else { // constants
                final double a;
                switch (name) {
                    case "PI": a = Math.PI; break;
                    case "E": a = Math.E; break;
                    case "TO_RAD": a = Math.PI / 180; break;
                    case "TO_DEG": a = 180 / Math.PI; break;
                    default:
                        throw new RuntimeException("Unknown constant '" + name + "' at position " + (pos - name.length()));
                }
                x = vars -> a;
            }
        } else if (weat('$')) {
            while (isVariableChar()) nextChar();
            String variable = string.substring(startPos + 1, pos);
            x = vars -> {
                Double value = vars.get(variable);
                return value != null ? value : 0; // TODO better error handling
            };
        } else {
            throw new RuntimeException("Unexpected '" + ch + "' at position " + pos);
        }
        if (weat('^')) { // exponentiation
            Expression a = x;
            Expression b = parseFactor();
            x = vars -> Math.pow(a.eval(vars), b.eval(vars));
        }
        return x;
    }
}
