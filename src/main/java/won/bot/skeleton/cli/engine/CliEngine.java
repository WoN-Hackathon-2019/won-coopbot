package won.bot.skeleton.cli.engine;

import org.apache.jena.atlas.lib.Pair;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class CliEngine {

    private Map<String, Pair<Method, Object>> commands = new HashMap<>();

    public void add(Object obj) {
        for (Method m: obj.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(Command.class)) {

                commands.put(m.getAnnotation(Command.class).value(), new Pair<>(m, obj));
            }
        }
    }

    public void parse(String cmd) {
        if (!cmd.startsWith("/")) {
            return;
        }
        String[] parts = cmd.split(" ");
        String c = parts[0];
        if (!commands.containsKey(c)) {
            return;
        }
        Pair<Method, Object> methodObjectPair = commands.get(c);
        Parameter[] params = methodObjectPair.getLeft().getParameters();
        Object[] arguments = new Object[params.length];

        int commandIndex = 1;
        for (int i = 0; i < params.length; i++) {
            if (parts.length - 1 < i) {
                arguments[i] = null;
            } else if (params[i].getType().isArray()) {
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
                arguments[i] = parseType(params[i].getType(), parts[commandIndex]);
            }
            commandIndex++;
        }

        try {
            methodObjectPair.getLeft().setAccessible(true);
            methodObjectPair.getLeft().invoke(methodObjectPair.getRight(), arguments);
        } catch (Exception e) {
            e.printStackTrace();
        }

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
