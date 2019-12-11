package won.bot.skeleton.cli.engine;

import org.apache.commons.cli.MissingArgumentException;
import org.apache.jena.atlas.lib.Pair;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CliEngine {

    private Map<String, Pair<Method, Object>> commands = new HashMap<>();

    public void add(Object obj) {
        for (Method m: obj.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(Command.class)) {

                commands.put(m.getAnnotation(Command.class).value(), new Pair<>(m, obj));
            }
        }
    }

    public boolean isCommand(String str) {
        return str.startsWith("/");
    }

    public Object parse(String cmd) {
        if (!cmd.startsWith("/")) {
            return null;
        }
        String[] parts = split(cmd);
        String c = parts[0];
        if (!commands.containsKey(c)) {
            return null;
        }
        Pair<Method, Object> methodObjectPair = commands.get(c);
        Parameter[] params = methodObjectPair.getLeft().getParameters();
        Object[] arguments = new Object[params.length];

        int commandIndex = 1;
        for (int i = 0; i < params.length; i++) {
            if (parts.length - 1 <= i) {
                // parameter not set
                if (params[i].isAnnotationPresent(Optional.class)) {
                    if (params[i].getType().equals(String.class) || params[i].getType().isArray()) {
                        arguments[i] = null;
                    } else if (params[i].getType().equals(boolean.class)) {
                        arguments[i] = false;
                    } else {
                        arguments[i] = 0;
                    }
                } else if (params[i].isAnnotationPresent(DefaultValue.class)) {
                    String defaultValue = params[i].getAnnotation(DefaultValue.class).value();
                    arguments[i] = parseType(params[i].getType(), defaultValue);
                } else {
                    throw new RuntimeException("Parameter " + params[i].getName() + " is missing");
                }
            } else if (params[i].getType().isArray()) {
                // Array argument
                Class arrayType = params[i].getType().getComponentType();
                if (arrayType.equals(int.class)) {
                    int[] arr = new int[parts.length - commandIndex];
                    int j = 0;
                    while (commandIndex < parts.length) {
                        arr[j] = toInt(parts[commandIndex]);
                        commandIndex++;
                        j++;
                    }
                    arguments[i] = arr;
                } else if (arrayType.equals(long.class)) {
                    long[] arr = new long[parts.length - commandIndex];
                    int j = 0;
                    while (commandIndex < parts.length) {
                        arr[j] = toLong(parts[commandIndex]);
                        commandIndex++;
                        j++;
                    }
                    arguments[i] = arr;
                } else if (arrayType.equals(float.class)) {
                    float[] arr = new float[parts.length - commandIndex];
                    int j = 0;
                    while (commandIndex < parts.length) {
                        arr[j] = toFloat(parts[commandIndex]);
                        commandIndex++;
                        j++;
                    }
                    arguments[i] = arr;
                } else if (arrayType.equals(double.class)) {
                    double[] arr = new double[parts.length - commandIndex];
                    int j = 0;
                    while (commandIndex < parts.length) {
                        arr[j] = toDouble(parts[commandIndex]);
                        commandIndex++;
                        j++;
                    }
                    arguments[i] = arr;
                } else if (arrayType.equals(boolean.class)) {
                    boolean[] arr = new boolean[parts.length - commandIndex];
                    int j = 0;
                    while (commandIndex < parts.length) {
                        arr[j] = toBool(parts[commandIndex]);
                        commandIndex++;
                        j++;
                    }
                    arguments[i] = arr;
                } else if (arrayType.equals(String.class)) {
                    String[] arr = new String[parts.length - commandIndex];
                    int j = 0;
                    while (commandIndex < parts.length) {
                        arr[j] = parts[commandIndex];
                        commandIndex++;
                        j++;
                    }
                    arguments[i] = arr;
                }
                break;
            } else {
                // Simpletype argument
                arguments[i] = parseType(params[i].getType(), parts[commandIndex]);
            }
            commandIndex++;
        }

        try {
            methodObjectPair.getLeft().setAccessible(true);
            return methodObjectPair.getLeft().invoke(methodObjectPair.getRight(), arguments);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private String[] split(String cmd) {
        List<String> parts = new LinkedList<>();
        int lastPos = 0;
        boolean inString = false;

        for (int i = 0; i < cmd.length(); i++) {
            if (cmd.charAt(i) == '"' && (i == 0 || cmd.charAt(i - 1) != '\\')) {
                inString = !inString;
            }
            if (!inString && cmd.charAt(i) == ' ') {
                parts.add(cmd.substring(lastPos, i));
                lastPos = i + 1;
            }
        }

        if (inString) {
            throw new RuntimeException("Mal formarted string. \" is missing");
        }

        parts.add(cmd.substring(lastPos, cmd.length()));

        parts = parts.stream()
                .map(s -> s.startsWith("\"") ? s.substring(1) : s)
                .map(s -> s.endsWith("\"") ? s.substring(0, s.length() - 1) : s)
                .map(s -> s.replace("\\\"", "\""))
                .collect(Collectors.toList());

        String[] arr = new String[parts.size()];
        arr = parts.toArray(arr);
        return arr;
    }

    private Object parseType(Class type, String str) {
        if (type.equals(int.class)) {
            return toInt(str);
        } else if (type.equals(float.class)) {
            return toFloat(str);
        } else  if (type.equals(double.class)) {
            return toDouble(str);
        } else if (type.equals(long.class)) {
            return toLong(str);
        } else if (type.equals(String.class)) {
            return str;
        } else if (type.equals(boolean.class)) {
            return toBool(str);
        } else {
            return null;
        }
    }

    private int toInt(String str) {
        return Integer.parseInt(str);
    }

    private long toLong(String str) {
        return Long.parseLong(str);
    }

    private float toFloat(String str) {
        return Float.parseFloat(str);
    }

    private double toDouble(String str) {
        return Double.parseDouble(str);
    }

    private boolean toBool(String str) {
        return Boolean.parseBoolean(str);
    }
}
