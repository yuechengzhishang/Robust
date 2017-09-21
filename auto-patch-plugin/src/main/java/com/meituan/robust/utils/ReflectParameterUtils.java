package com.meituan.robust.utils;

/**
 * Created by hedingxu on 17/9/20.
 */

public class ReflectParameterUtils {
    public static String getBoxedParameter(String parameter,String parameterType){
        switch (parameterType) {
            case "boolean":
                return "java.lang.Boolean.valueOf(" + parameter + ")";
            case "byte":
                return "java.lang.Byte.valueOf(" + parameter + ")";
            case "char":
                return "java.lang.Character.valueOf(" + parameter + ")";
            case "double":
                return "java.lang.Double.valueOf(" + parameter + ")";
            case "float":
                return "java.lang.Float.valueOf(" + parameter + ")";
            case "int":
                return "java.lang.Integer.valueOf(" + parameter + ")";
            case "long":
                return "java.lang.Long.valueOf(" + parameter + ")";
            case "short":
                return "java.lang.Short.valueOf(" + parameter + ")";
            default:
                return parameter;
        }
    }
}
