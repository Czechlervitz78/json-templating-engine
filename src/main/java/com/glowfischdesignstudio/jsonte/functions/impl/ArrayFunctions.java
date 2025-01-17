package com.glowfischdesignstudio.jsonte.functions.impl;

import com.glowfischdesignstudio.jsonte.functions.JSONLambda;
import com.glowfischdesignstudio.jsonte.exception.JsonTemplatingException;
import com.glowfischdesignstudio.jsonte.functions.JSONFunction;
import com.glowfischdesignstudio.jsonte.functions.JSONInstanceFunction;
import com.glowfischdesignstudio.jsonte.utils.JsonUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Array functions are for getting information on and transforming arrays.
 */
public class ArrayFunctions {

    /**
     * Returns an array of objects from an object, where each object in resulting array has fields with field name and value.
     * @param obj object: Object to be mapped
     * @param key keyName: Name of the key field
     * @param value valueName: Name of the value field
     * @example
     * Given scope
     * <code>
     * {
     *   "testObject": {
     *     "test1": "someVal",
     *     "test2": "anotherVal"
     *   }
     * }
     * </code>
     * for query
     * <code>
     * {
     *   "$template": {
     *     "test": "{{asArray(testObject, 'key', 'value')}}"
     *   }
     * }
     * </code>
     * the result will be
     * <code>
     * [
     *   {
     *     "key": "test1",
     *     "value": "someVal"
     *   },
     *   {
     *     "key": "test2",
     *     "value": "anotherVal"
     *   }
     * ]
     * </code>
     */
    @JSONFunction
    private static JSONArray asArray(JSONObject obj, String key, String value) {
        return new JSONArray(obj.keySet().stream().map(s -> {
            JSONObject r = new JSONObject();
            r.put(key, s);
            r.put(value, obj.get(s));
            return r;
        }).collect(Collectors.toList()));
    }

    /**
     * Returns an array of keys from the object.
     * @param obj object: Source object
     * @example
     * Given scope
     * <code>
     * {
     *   "testObject": {
     *     "test1": "someVal",
     *     "test2": "anotherVal"
     *   }
     * }
     * </code>
     * for query
     * <code>
     * {
     *   "$template": {
     *     "test": "{{keys(testObject)}}"
     *   }
     * }
     * </code>
     * the result will be
     * <code>
     * [
     *   "test1", "test2"
     * ]
     * </code>
     */
    @JSONFunction
    private static JSONArray keys(JSONObject obj) {
        return new JSONArray(new ArrayList<>(obj.keySet()));
    }

    /**
     * Returns an array of values from an object.
     * @param obj object: Source object
     * @example
     * Given scope
     * <code>
     * {
     *   "testObject": {
     *     "test1": "someVal",
     *     "test2": "anotherVal"
     *   }
     * }
     * </code>
     * for query
     * <code>
     * {
     *   "$template": {
     *     "test": "{{values(testObject)}}"
     *   }
     * }
     * </code>
     * the result will be
     * <code>
     * [
     *   "someVal",
     *   "anotherVal"
     * ]
     * </code>
     */
    @JSONFunction
    private static JSONArray values(JSONObject obj) {
        return new JSONArray(new ArrayList<>(obj.toMap().values()));
    }

    /**
     * Returns an array with reversed order.
     * @param arr array: Source array
     * @example
     * <code>
     * {
     *   "$template": {
     *     "$comment": "The field below will be [4, 3, 2, 1, 0]",
     *     "test": "{{reverse(0..4)}}"
     *   }
     * }
     * </code>
     */
    @JSONFunction
    @JSONInstanceFunction
    private static JSONArray reverse(JSONArray arr) {
        List<?> l = new ArrayList<>(arr.toList());
        Collections.reverse(l);
        return new JSONArray(l);
    }

    /**
     * Returns whether supplied array contains provided value.
     * @param arr array: Array to check
     * @param value value: Value to search for (currently a string, or a number only)
     * @example
     * <code>
     * {
     *   "$template": {
     *     "$comment": "The field below will be true",
     *     "test": "{{contains(['asd', '123', 123, 9], '123')}}"
     *   }
     * }
     * </code>
     */
    @JSONFunction
    @JSONInstanceFunction
    private static Boolean contains(JSONArray arr, String value) {
        return arr.toList().stream().anyMatch(o -> o instanceof String && o.equals(value));
    }

    @JSONFunction
    @JSONInstanceFunction
    private static Boolean contains(JSONArray arr, Number value) {
        return arr.toList()
                .stream()
                .anyMatch(o -> o instanceof Number && ((Number) o).doubleValue() == value.doubleValue());
    }

    /**
     * Returns a new array that is filtered based on a predicate.
     * @param arr array: Source array
     * @param predicate predicate(element, index): Lambda, that should return whether an element should remain
     * @example
     * <code>
     * {
     *   "$template": {
     *     "$comment": "The field below will be [0, 2, 4]",
     *     "test": "{{filter(0..4, x => mod(x, 2) == 0)}}"
     *   }
     * }
     * </code>
     */
    @JSONFunction
    @JSONInstanceFunction
    private static JSONArray filter(JSONArray arr, JSONLambda predicate) {
        List<Object> objects = arr.toList();
        for (int i = objects.size() - 1; i >= 0; i--) {
            if (!JsonUtils.toBoolean(predicate.execute(objects.get(i), i))) {
                objects.remove(i);
            }
        }
        return new JSONArray(objects);
    }

    /**
     * Returns a new array, where every element is mapped to another value using provided lambda.
     * @param arr array: Source array
     * @param predicate lambda(element, index): Lambda, that should return new element value
     * @example
     * <code>
     * {
     *   "$template": {
     *     "$comment": "The field below will be [0, 2, 4, 6, 8]",
     *     "test": "{{map(0..4, x => x => x * 2)}}"
     *   }
     * }
     * </code>
     */
    @JSONFunction
    @JSONInstanceFunction
    private static JSONArray map(JSONArray arr, JSONLambda predicate) {
        List<Object> objects = arr.toList();
        for (int i = 0; i < objects.size(); i++) {
            objects.set(i, predicate.execute(objects.get(i), i));
        }
        return new JSONArray(objects);
    }

    /**
     * Returns a new array, where every array from lambda is merged into source array.
     * @param arr array: Source array
     * @param predicate lambda(element, index): Lambda, that should return an array to merge. If none provided, it will use identity lambda (`x => x`)
     * @example
     * <code>
     * {
     *   "$template": {
     *     "$comment": "The field below will be [1, 2, 3, 4]",
     *     "test": "{{flatMap([[1, 2], 3, [4]])}}"
     *   }
     * }
     * </code>
     */
    @JSONFunction
    @JSONInstanceFunction
    private static JSONArray flatMap(JSONArray arr, JSONLambda predicate) {
        List<Object> objects = arr.toList();
        for (int i = 0; i < objects.size(); i++) {
            objects.set(i, predicate.execute(objects.get(i), i));
        }
        return new JSONArray(objects.stream()
                .flatMap(o -> o instanceof JSONArray ?
                        ((JSONArray) o).toList().stream() : o instanceof Collection ?
                        ((Collection<?>) o).stream() : Stream.of(o))
                .collect(Collectors.toList()));
    }

    @JSONFunction
    @JSONInstanceFunction
    private static JSONArray flatMap(JSONArray arr) {
        return new JSONArray(arr.toList()
                .stream()
                .flatMap(o -> o instanceof JSONArray ? ((JSONArray) o).toList()
                        .stream() : o instanceof Collection ? ((Collection<?>) o).stream() : Stream.of(o))
                .collect(Collectors.toList()));
    }

    /**
     * Returns the number of elements in an array.
     * @param arr array: Source array
     * @param predicate predicate(element, index): (optional) Lambda, that should return whether an element should be counted
     * @example
     * <code>
     * {
     *   "$template": {
     *     "$comment": "The field below will be 3",
     *     "test": "{{count(0..4, x => mod(x, 2) == 0)}}"
     *   }
     * }
     * </code>
     */
    @JSONFunction
    @JSONInstanceFunction
    private static Long count(JSONArray arr, JSONLambda predicate) {
        List<Object> objects = arr.toList();
        for (int i = objects.size() - 1; i >= 0; i--) {
            if (!JsonUtils.toBoolean(predicate.execute(objects.get(i), i))) {
                objects.remove(i);
            }
        }
        return (long) objects.size();
    }

    @JSONFunction
    @JSONInstanceFunction
    private static Integer count(JSONArray arr) {
        return arr.toList().size();
    }


    /**
     * Returns the first element from an array filtered by the predicate.
     * @param arr array: Source array
     * @param predicate predicate(element, index): Lambda, that should return whether an element should remain
     * @example
     * <code>
     * {
     *   "$template": {
     *     "$comment": "The field below will be 0",
     *     "test": "{{findFirst(0..4, x => mod(x, 2) == 0)}}"
     *   }
     * }
     * </code>
     */
    @JSONFunction
    @JSONInstanceFunction
    private static Object findFirst(JSONArray arr, JSONLambda predicate) {
        List<Object> objects = arr.toList();
        for (int i = objects.size() - 1; i >= 0; i--) {
            if (JsonUtils.toBoolean(predicate.execute(objects.get(i), i))) {
                return objects.get(i);
            }
        }
        throw new JsonTemplatingException("No matching items found!");
    }


    /**
     * Returns index of given element inside the array or -1 if not found.
     * @param arr array: Source array
     * @param element element: Element to find
     * @example
     * <code>
     * {
     *   "$template": {
     *     "$comment": "The field below will be 1",
     *     "test": "{{indexOf(1..5, 2)}}"
     *   }
     * }
     * </code>
     */
    @JSONFunction
    @JSONInstanceFunction
    private static Object indexOf(JSONArray arr, Object element) {
        if (element instanceof Iterable) {
            element = new JSONArray((Iterable<?>) element);
        }
        else if (element instanceof Map) {
            element = new JSONObject((Map<?, ?>) element);
        }
        List<Object> objects = arr.toList();
        return objects.stream()
                .map(o -> o instanceof Map ? new JSONObject((Map<?, ?>) o) : o)
                .map(o -> o instanceof Iterable ? new JSONArray((Iterable<?>) o) : o)
                .map(Object::toString)
                .collect(Collectors.toList())
                .indexOf(element.toString());
    }


    /**
     * Returns a number with numbers encoded in given bit space.
     *
     * @param arr array: Source array
     * @param space space: Bit space for the values. Must be power of 2.
     * @param predicate predicate(element, index): Lambda, that should return an integer number to encode
     * @example
     * <code>
     * {
     *   "$template": {
     *     "$comment": "The field below will be -2023406815 (1000 0111 0110 0101 0100 0011 0010 0001)",
     *     "test": "{{1..10.encode(16, x => x)}}"
     *   }
     * }
     * </code>
     */
    @JSONFunction
    @JSONInstanceFunction
    private static Number encode(JSONArray arr, Number space, JSONLambda predicate) {
        if (space.intValue() <= 0 || (space.intValue() & (space.intValue() - 1)) != 0) {
            throw new JsonTemplatingException("Space must be a power of 2 and greater than 0!");
        }
        List<Object> objects = arr.toList();
        int result = 0;
        int bitsPerElement = (int) (Math.log(space.intValue()) / Math.log(2));
        for (int i = 0; i < Math.min(objects.size(), 32 / bitsPerElement); i++) {
            Number number = JsonUtils.toNumber(predicate.execute(objects.get(i), i));
            if (number == null) {
                throw new JsonTemplatingException("Predicate must return a number!");
            }
            if (number.intValue() < 0 || number.intValue() >= space.intValue()) {
                throw new JsonTemplatingException("Number " + number + " is out of range 0.." + (space.intValue() - 1));
            }
            result += number.intValue() << i * bitsPerElement;
        }
        return result;
    }


    /**
     * Returns a portion of the array between the specified startIndex, inclusive, and endIndex, exclusive. (If startIndex and endIndex are equal, the returned array is empty.)
     *
     * @param arr array: Source array
     * @param startIndex startIndex: Starting index (inclusive) of the sub list
     * @param endIndex endIndex: Ending index (exclusive) of the sub list. If none provided, will use the end of the array
     * @example
     * <code>
     * {
     *   "$template": {
     *     "$comment": "The field below will be [4, 5, 6, 7]",
     *     "test": "{{0..10.sublist(4, 8)}}"
     *   }
     * }
     * </code>
     */
    @JSONFunction
    @JSONInstanceFunction
    private static JSONArray sublist(JSONArray arr, Number startIndex, Number endIndex) {
        List<Object> objects = arr.toList();
        if (startIndex.intValue() < 0 || startIndex.intValue() >= objects.size()) {
            throw new JsonTemplatingException("Start index " + startIndex + " is out of range 0.." + (objects.size() - 1));
        }
        if (endIndex.intValue() < startIndex.intValue() || endIndex.intValue() >= objects.size()) {
            throw new JsonTemplatingException("End index " + endIndex + " is out of range startIndex.." + (objects.size() - 1));
        }
        return new JSONArray(objects.subList(startIndex.intValue(), endIndex.intValue()));
    }

    @JSONFunction
    @JSONInstanceFunction
    private static JSONArray sublist(JSONArray arr, Number startIndex) {
        List<Object> objects = arr.toList();
        if (startIndex.intValue() < 0 || startIndex.intValue() >= objects.size()) {
            throw new JsonTemplatingException("Start index " + startIndex + " is out of range 0.." + (objects.size() - 1));
        }
        return new JSONArray(objects.subList(startIndex.intValue(), objects.size()));
    }


    /**
     * Returns the element, for which the predicate will return the maximum value or null if the array is empty.
     * @param arr array: Source array
     * @param predicate predicate(element, index): Lambda, that should return a number to compare. If none provided, it will use identity lambda (`x => x`)
     * @example
     * <code>
     * {
     *   "$template": {
     *     "$comment": "The field below will be 4",
     *     "test": "{{max(0..4)}}"
     *   }
     * }
     * </code>
     */
    @JSONFunction
    @JSONInstanceFunction
    private static Object max(JSONArray arr, JSONLambda predicate) {
        List<Object> objects = arr.toList();
        return objects.stream()
                .max(Comparator.comparingDouble(args -> {
                    Number number = JsonUtils.toNumber(predicate.execute(args));
                    if (number == null) {
                        throw new JsonTemplatingException("Predicate must return a number!");
                    }
                    return number.doubleValue();
                }))
                .orElse(null);
    }

    @JSONFunction
    @JSONInstanceFunction
    private static Object max(JSONArray arr) {
        List<Object> objects = arr.toList();
        return objects.stream()
                .max(Comparator.comparingDouble(args -> {
                    Number number = JsonUtils.toNumber(args);
                    if (number == null) {
                        throw new JsonTemplatingException("Object must be a number!");
                    }
                    return number.doubleValue();
                }))
                .orElse(null);
    }


    /**
     * Returns the element, for which the predicate will return the minimum value or null if the array is empty.
     * @param arr array: Source array
     * @param predicate predicate(element, index): Lambda, that should return a number to compare. If none provided, it will use identity lambda (`x => x`)
     * @example
     * <code>
     * {
     *   "$template": {
     *     "$comment": "The field below will be 0",
     *     "test": "{{min(0..4)}}"
     *   }
     * }
     * </code>
     */
    @JSONFunction
    @JSONInstanceFunction
    private static Object min(JSONArray arr, JSONLambda predicate) {
        List<Object> objects = arr.toList();
        return objects.stream()
                .min(Comparator.comparingDouble(args -> {
                    Number number = JsonUtils.toNumber(predicate.execute(args));
                    if (number == null) {
                        throw new JsonTemplatingException("Predicate must return a number!");
                    }
                    return number.doubleValue();
                }))
                .orElse(null);
    }

    @JSONFunction
    @JSONInstanceFunction
    private static Object min(JSONArray arr) {
        List<Object> objects = arr.toList();
        return objects.stream()
                .min(Comparator.comparingDouble(args -> {
                    Number number = JsonUtils.toNumber(args);
                    if (number == null) {
                        throw new JsonTemplatingException("Object must be a number!");
                    }
                    return number.doubleValue();
                }))
                .orElse(null);
    }

}
