package com.glowfischdesignstudio.jsonte.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class JsonUtils {

    public static void removeNulls(JSONObject obj) {
        List<String> keys = new ArrayList<>(obj.keySet());
        for (String s : keys) {
            Object o = obj.get(s);
            removeNulls(o, s, obj::remove);
        }
    }

    public static void removeNulls(JSONArray arr) {
        for (int i = arr.length() - 1; i >= 0; i--) {
            Object o = arr.get(i);
            removeNulls(o, i, arr::remove);
        }
    }

    private static <T> void removeNulls(Object o, T param, Consumer<T> remove) {
        if (o == JSONObject.NULL || (o instanceof String && o.equals("null"))) {
            remove.accept(param);
        }
        else if (o instanceof JSONObject) {
            removeNulls((JSONObject) o);
        }
        else if (o instanceof JSONArray) {
            removeNulls((JSONArray) o);
        }
    }

    public static JSONObject merge(JSONObject template, JSONObject parent) {
        if (template == parent) {
            throw new IllegalArgumentException("Template and parent cannot be the same!");
        }
        for (String s : parent.keySet()) {
            if (template.has(s)) {
                if (parent.get(s) instanceof JSONObject) {
                    if (template.get(s) instanceof JSONArray) {
                        JSONArray arr = new JSONArray();
                        arr.put(parent.get(s));
                        parent.put(s, arr);
                        merge(template.getJSONArray(s), arr);
                    }
                    else if (template.get(s) == JSONObject.NULL) {
                        template.remove(s);
                    }
                    else {
                        merge(template.getJSONObject(s), parent.getJSONObject(s));
                    }
                }
                else if (parent.get(s) instanceof JSONArray) {
                    if (template.get(s) instanceof JSONArray) {
                        merge(template.getJSONArray(s), parent.getJSONArray(s));
                    }
                    else if (template.get(s) == JSONObject.NULL ||
                            (template.get(s) instanceof String && template.getString(s).equals("null"))) {
                        template.remove(s);
                    }
                    else {
                        JSONArray arr = new JSONArray();
                        arr.put(template.get(s));
                        template.put(s, arr);
                        merge(arr, parent.getJSONArray(s));
                    }
                }
            }
            else if (parent.get(s) instanceof JSONObject) {
                template.put(s, new JSONObject(parent.getJSONObject(s).toString()));
            }
            else {
                template.put(s, parent.get(s));
            }
        }
        return template;
    }

    public static JSONArray merge(JSONArray template, JSONArray parent) {
        if (template == parent) {
            throw new IllegalArgumentException("Template and parent cannot be the same!");
        }
        for (Object o : parent) {
            if (o instanceof JSONObject) {
                template.put(new JSONObject(o.toString()));
            }
            else if (o instanceof JSONArray) {
                template.put(new JSONArray(o.toString()));
            }
            else {
                template.put(o);
            }
        }
        return template;
    }

    public static JSONObject createIterationExtraScope(JSONObject extraScope, JSONArray arr1, int i1, String name) {
        JSONObject extra = new JSONObject();
        extra.put("index", i1);
        extra.put(name, arr1.get(i1));
        for (String s1 : extraScope.keySet()) {
            if (!extra.has(s1)) {
                extra.put(s1, extraScope.get(s1));
            }
        }
        return extra;
    }

    public static boolean toBoolean(Object o) {
        return o != null && (o instanceof JSONObject ||
                o instanceof JSONArray ||
                (o instanceof Boolean && (Boolean) o) ||
                (o instanceof Number && ((Number) o).doubleValue() != 0) ||
                (o instanceof String && !((String) o).isEmpty()));
    }

    public static Number toNumber(Object o) {
        if (o instanceof Number) {
            return (Number) o;
        }
        else if (o instanceof String) {
            if (((String) o).indexOf('.') != -1) {
                try {
                    return Double.parseDouble(o.toString());
                } catch (NumberFormatException ignored) {
                }
            }
            else {
                try {
                    return Integer.parseInt(o.toString());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }

    public static Object getByIndex(JSONObject obj, int index) {
        if (index < 0) {
            return null;
        }
        int i = 0;
        for (Object t : obj.keySet()) {
            if (i == index) {
                return t;
            }
            i++;
        }
        return null;
    }

    public static Object copyJson(Object obj) {
        Object copy = obj;
        if (obj instanceof JSONObject) {
            JSONObject o = (JSONObject) obj;
            copy = new JSONObject(o, o.keySet().toArray(new String[]{}));
        }
        if (obj instanceof JSONArray) {
            JSONArray o = (JSONArray) obj;
            copy = new JSONArray(o.toList());
        }
        return copy;
    }
}