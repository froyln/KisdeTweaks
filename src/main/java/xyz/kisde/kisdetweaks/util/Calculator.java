package xyz.kisde.kisdetweaks.util;

import net.minecraft.client.Minecraft;

public class Calculator {
    private final String input;
    private int index;

    private Calculator(String input) {
        this.input = input;
    }

    public static double evaluate(String input) {
        if (input.length() > 128) throw new IllegalArgumentException("Expression is too long");
        Calculator parser = new Calculator(input);
        double result = parser.expression();
        parser.whitespace();
        if (parser.index != input.length()) throw new IllegalArgumentException("Unexpected character");
        if (!Double.isFinite(result)) throw new IllegalArgumentException("Result is not finite");
        return result;
    }

    public static String command(String input) {
        String[] args = input.trim().split("\\s+");
        String name = args[0].toLowerCase(java.util.Locale.ROOT);
        if (name.equals("variables")) return "stack=64, stack16=16, sb=1728, sb16=432, dub=3456, dub16=864, min=60, h=3600, x/y/z=player position, health=player health";
        if (name.equals("rates") && args.length == 3) return format(evaluate(args[1]) * 3600.0 / evaluate(args[2])) + " items/hour";
        if (name.equals("itemtosb") && args.length == 2) return format(evaluate(args[1]) / 1728.0) + " shulker boxes";
        if (name.equals("sbtoitem") && args.length == 2) return format(evaluate(args[1]) * 1728.0) + " items";
        if (name.equals("itemtostack") && args.length == 2) return format(evaluate(args[1]) / 64.0) + " stacks";
        if (name.equals("stacktoitem") && args.length == 2) return format(evaluate(args[1]) * 64.0) + " items";
        if ((name.equals("nether") || name.equals("overworld")) && (args.length == 3 || args.length == 4)) {
            double scale = name.equals("nether") ? 0.125 : 8.0;
            double x = evaluate(args[1]) * scale, z = evaluate(args[args.length - 1]) * scale;
            return "(" + format(x) + ", " + (args.length == 4 ? format(evaluate(args[2])) + ", " : "") + format(z) + ")";
        }
        if (name.equals("dist") && (args.length == 4 || args.length == 7)) {
            double x0, y0, z0, x, y, z;
            if (args.length == 4) {
                if (Minecraft.getMinecraft().player == null) throw new IllegalArgumentException("Player position unavailable");
                x0 = Minecraft.getMinecraft().player.posX;
                y0 = Minecraft.getMinecraft().player.posY;
                z0 = Minecraft.getMinecraft().player.posZ;
                x = evaluate(args[1]); y = evaluate(args[2]); z = evaluate(args[3]);
            } else {
                x0 = evaluate(args[1]); y0 = evaluate(args[2]); z0 = evaluate(args[3]);
                x = evaluate(args[4]); y = evaluate(args[5]); z = evaluate(args[6]);
            }
            return format(Math.sqrt(Math.pow(x - x0, 2) + Math.pow(y - y0, 2) + Math.pow(z - z0, 2))) + " blocks";
        }
        return null;
    }

    private static String format(double value) {
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Result is not finite");
        return java.math.BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private double expression() {
        double value = term();
        while (true) {
            whitespace();
            if (take('+')) value += term();
            else if (take('-')) value -= term();
            else return finite(value);
        }
    }

    private double term() {
        double value = unary();
        while (true) {
            whitespace();
            if (take('*')) value *= unary();
            else if (take('/')) {
                double divisor = unary();
                if (divisor == 0) throw new IllegalArgumentException("Division by zero");
                value /= divisor;
            } else return finite(value);
        }
    }

    private double unary() {
        whitespace();
        if (take('+')) return unary();
        if (take('-')) return -unary();
        return power();
    }

    private double power() {
        double value = primary();
        whitespace();
        if (take('^')) value = Math.pow(value, unary());
        return finite(value);
    }

    private double primary() {
        whitespace();
        if (take('(')) {
            double value = expression();
            whitespace();
            if (!take(')')) throw new IllegalArgumentException("Missing )");
            return value;
        }
        int start = index;
        if (index < input.length() && Character.isLetter(input.charAt(index))) {
            while (index < input.length() && Character.isLetterOrDigit(input.charAt(index))) index++;
            String variable = input.substring(start, index).toLowerCase(java.util.Locale.ROOT);
            switch (variable) {
                case "pi": return Math.PI;
                case "e": return Math.E;
                case "stack": case "stack64": return 64;
                case "stack16": return 16;
                case "stack1": return 1;
                case "sb": case "sb64": return 1728;
                case "sb16": return 432;
                case "sb1": return 27;
                case "dub": case "dub64": return 3456;
                case "dub16": return 864;
                case "dub1": return 54;
                case "min": return 60;
                case "h": return 3600;
                case "x": return playerValue(0);
                case "y": return playerValue(1);
                case "z": return playerValue(2);
                case "health": return Minecraft.getMinecraft().player == null ? 0 : Minecraft.getMinecraft().player.getHealth();
                default: throw new IllegalArgumentException("Unknown variable " + variable);
            }
        }
        boolean digit = false;
        while (index < input.length() && Character.isDigit(input.charAt(index))) {
            digit = true;
            index++;
        }
        if (index < input.length() && input.charAt(index) == '.') {
            index++;
            while (index < input.length() && Character.isDigit(input.charAt(index))) {
                digit = true;
                index++;
            }
        }
        if (!digit) throw new IllegalArgumentException("Expected a number");
        return Double.parseDouble(input.substring(start, index));
    }

    private static double playerValue(int axis) {
        if (Minecraft.getMinecraft().player == null) return 0;
        return axis == 0 ? Minecraft.getMinecraft().player.posX : axis == 1 ? Minecraft.getMinecraft().player.posY : Minecraft.getMinecraft().player.posZ;
    }

    private double finite(double value) {
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Result is not finite");
        return value;
    }

    private boolean take(char expected) {
        if (index < input.length() && input.charAt(index) == expected) {
            index++;
            return true;
        }
        return false;
    }

    private void whitespace() {
        while (index < input.length() && Character.isWhitespace(input.charAt(index))) index++;
    }
}
