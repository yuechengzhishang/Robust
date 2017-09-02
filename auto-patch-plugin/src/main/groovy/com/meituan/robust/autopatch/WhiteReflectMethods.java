package com.meituan.robust.autopatch;

/**
 * Created by hedingxu on 17/9/2.
 */

public class WhiteReflectMethods {
    // List of all black listed methods.
    // All these methods are java.lang.reflect classes and associated : since the new version of the
    // class is loaded in a different class loader, the classes are in a different package and
    // package private methods would need a setAccessble(true) to work correctly. Eventually, we
    // could transform all reflection calls to automatically insert these setAccessible calls but
    // at this point, we just don't enable InstantRun on those.
//    private static final ImmutableMultimap<Type, Method> blackListedMethods =
//            ImmutableMultimap.<Type, Method>builder()
//                    .put(Type.getObjectType("java/lang/reflect/Field"), Method.getMethod("Object get(Object)"))
//                    .put(Type.getObjectType("java/lang/reflect/Field"), Method.getMethod("boolean getBoolean(Object)"))
//                    .put(Type.getObjectType("java/lang/reflect/Field"), Method.getMethod("byte getByte(Object)"))
//                    .put(Type.getObjectType("java/lang/reflect/Field"), Method.getMethod("char getChar(Object)"))
//                    .put(Type.getObjectType("java/lang/reflect/Field"), Method.getMethod("double getDouble(Object)"))
//                    .put(Type.getObjectType("java/lang/reflect/Field"), Method.getMethod("float getFloat(Object)"))
//                    .put(Type.getObjectType("java/lang/reflect/Field"), Method.getMethod("int getInt(Object)"))
//                    .put(Type.getObjectType("java/lang/reflect/Field"), Method.getMethod("long getLong(Object)"))
//                    .put(Type.getObjectType("java/lang/reflect/Field"), Method.getMethod("short getShort(Object)"))
//                    .put(Type.getObjectType("java/lang/reflect/Field"), Method.getMethod("void set(Object, Object)"))
//                    .put(Type.getObjectType("java/lang/reflect/Field"), Method.getMethod("void setBoolean(Object, boolean)"))
//                    .put(Type.getObjectType("java/lang/reflect/Field"), Method.getMethod("void setByte(Object, byte)"))
//                    .put(Type.getObjectType("java/lang/reflect/Field"), Method.getMethod("void setChar(Object, char)"))
//                    .put(Type.getObjectType("java/lang/reflect/Field"), Method.getMethod("void setDouble(Object, double)"))
//                    .put(Type.getObjectType("java/lang/reflect/Field"), Method.getMethod("void setFloat(Object, float)"))
//                    .put(Type.getObjectType("java/lang/reflect/Field"), Method.getMethod("void setInt(Object, int)"))
//                    .put(Type.getObjectType("java/lang/reflect/Field"), Method.getMethod("void setLong(Object, long)"))
//                    .put(Type.getObjectType("java/lang/reflect/Field"), Method.getMethod("void setShort(Object, short)"))
//                    .put(Type.getObjectType("java/lang/reflect/Constructor"), Method.getMethod("Object newInstance(Object[])"))
//                    .put(Type.getObjectType("java/lang/Class"), Method.getMethod("Object newInstance()"))
//                    .put(Type.getObjectType("java/lang/reflect/Method"), Method.getMethod("Object invoke(Object, Object[])"))
//                    .build();
//    java.lang.reflect
}
