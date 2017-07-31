package com.meituan.robust.runtime;


import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;
import java.util.logging.Level;


/**
 * Generic Instant Run services. must not depend on Android APIs.
 */
@SuppressWarnings("unused")
public class AndroidInstantRuntime {
    private static Logging logger = new Logging() {
        @Override
        public void log(@NonNull Level level, @NonNull String string) {
            System.err.println(string);
//            Log.d("robust-log", string);
        }

        @Override
        public boolean isLoggable(@NonNull Level level) {
            return true;
        }

        @Override
        public void log(@NonNull Level level, @NonNull String string,
                        @Nullable Throwable throwable) {
            System.err.println(string + ", throwable: " + throwable.toString());
            throwable.printStackTrace();
//            Log.d("robust-log", string, throwable);
        }
    };
    private static Logging logging = logger;

    public interface Logging {
        void log(@NonNull Level level, @NonNull String string);

        boolean isLoggable(@NonNull Level level);

        void log(@NonNull Level level, @NonNull String string, @Nullable Throwable throwable);
    }

    public static void setLogger(final Logging logger) {

    }

    @Nullable
    public static Object getStaticPrivateField(Class targetClass, String fieldName) {
        return getPrivateField(null /* targetObject */, targetClass, fieldName);
    }

    public static void setStaticPrivateField(
            @NonNull Object value, @NonNull Class targetClass, @NonNull  String fieldName) {
        setPrivateField(null /* targetObject */, value, targetClass, fieldName);
    }

    public static void setPrivateField(
            @Nullable Object targetObject,
            @Nullable  Object value,
            @NonNull Class targetClass,
            @NonNull  String fieldName) {

        try {
            Field declaredField = getField(targetClass, fieldName);
            declaredField.set(targetObject, value);
        } catch (IllegalAccessException e) {
            if (logging != null) {
                logging.log(Level.SEVERE,
                        String.format("Exception during setPrivateField %s", fieldName), e);
            }
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public static Object getPrivateField(
            @Nullable Object targetObject,
            @NonNull Class targetClass,
            @NonNull  String fieldName) {

        try {
            Field declaredField = getField(targetClass, fieldName);
            return declaredField.get(targetObject);
        } catch (IllegalAccessException e) {
            if (logging != null) {
                logging.log(Level.SEVERE,
                        String.format("Exception during%1$s getField %2$s",
                                targetObject == null ? " static" : "",
                                fieldName), e);
            }
            throw new RuntimeException(e);
        }
    }


    @NonNull
    public static Field getField(Class target, String name) {
        Field declareField = getFieldByName(target, name);
        if (declareField == null) {
            throw new RuntimeException(new NoSuchElementException(name));
        }
        declareField.setAccessible(true);
        return declareField;
    }

    public static Object invokeProtectedMethod(Object receiver,
                                               Object[] params,
                                               Class[] parameterTypes,
                                               String methodName) throws Throwable {

        if (logging!=null && logging.isLoggable(Level.FINE)) {
            logging.log(Level.FINE, String.format("protectedMethod:%s on %s", methodName, receiver));
        }
        try {
            Method toDispatchTo = getMethodByName(receiver.getClass(), methodName, parameterTypes);
            if (toDispatchTo == null) {
                throw new RuntimeException(new NoSuchMethodException(methodName));
            }
            toDispatchTo.setAccessible(true);
            return toDispatchTo.invoke(receiver, params);
        } catch (InvocationTargetException e) {
            // The called method threw an exception, rethrow
            throw e.getCause();
        } catch (IllegalAccessException e) {
            logging.log(Level.SEVERE, String.format("Exception while invoking %s", methodName), e);
            throw new RuntimeException(e);
        }
    }

    public static Object invokeProtectedStaticMethod(
            Object[] params,
            Class[] parameterTypes,
            String methodName,
            Class receiverClass) throws Throwable {

        if (logging!=null && logging.isLoggable(Level.FINE)) {
            logging.log(Level.FINE,
                    String.format("protectedStaticMethod:%s on %s", methodName, receiverClass.getName()));
        }
        try {
            Method toDispatchTo = getMethodByName(receiverClass, methodName, parameterTypes);
            if (toDispatchTo == null) {
                throw new RuntimeException(new NoSuchMethodException(
                        methodName + " in class " + receiverClass.getName()));
            }
            toDispatchTo.setAccessible(true);
            return toDispatchTo.invoke(null /* target */, params);
        } catch (InvocationTargetException e) {
            // The called method threw an exception, rethrow
            throw e.getCause();
        } catch (IllegalAccessException e) {
            logging.log(Level.SEVERE, String.format("Exception while invoking %s", methodName), e);
            throw new RuntimeException(e);
        }
    }

    public static <T> T newForClass(Object[] params, Class[] paramTypes, Class<T> targetClass)
            throws Throwable {
        Constructor declaredConstructor;
        try {
            declaredConstructor = targetClass.getDeclaredConstructor(paramTypes);
        } catch (NoSuchMethodException e) {
            logging.log(Level.SEVERE, "Exception while resolving constructor", e);
            throw new RuntimeException(e);
        }
        declaredConstructor.setAccessible(true);
        try {
            return targetClass.cast(declaredConstructor.newInstance(params));
        } catch (InvocationTargetException e) {
            // The called method threw an exception, rethrow
            throw e.getCause();
        } catch (InstantiationException e) {
            logging.log(Level.SEVERE, String.format("Exception while instantiating %s", targetClass), e);
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            logging.log(Level.SEVERE, String.format("Exception while instantiating %s", targetClass), e);
            throw new RuntimeException(e);
        }
    }

    public static Field getFieldByName(Class<?> aClass, String name) {

        if (logging!= null && logging.isLoggable(Level.FINE)) {
            logging.log(Level.FINE, String.format("getFieldByName:%s in %s", name, aClass.getName()));
        }

        Class<?> currentClass = aClass;
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                // ignored.
            }
            currentClass = currentClass.getSuperclass();
        }
        return null;
    }

    public static Method getMethodByName(Class<?> aClass, String name, Class[] paramTypes) {

        if (aClass == null) {
            return null;
        }

        Class<?> currentClass = aClass;
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredMethod(name, paramTypes);
            } catch (NoSuchMethodException e) {
                // ignored.
            }
            currentClass = currentClass.getSuperclass();
            if (currentClass!= null && logging!=null && logging.isLoggable(Level.FINE)) {
                logging.log(Level.FINE, String.format(
                        "getMethodByName:Looking in %s now", currentClass.getName()));
            }

        }
        return null;
    }

    public static void trace(String s) {
        if (logging != null) {
            logging.log(Level.FINE, s);
        }
    }

    public static void trace(String s1, String s2) {
        if (logging != null) {
            logging.log(Level.FINE, String.format("%s %s", s1, s2));
        }
    }

    public static void trace(String s1, String s2, String s3) {
        if (logging != null) {
            logging.log(Level.FINE, String.format("%s %s %s", s1, s2, s3));
        }
    }

    public static void trace(String s1, String s2, String s3, String s4) {
        if (logging != null) {
            logging.log(Level.FINE, String.format("%s %s %s %s", s1, s2, s3, s4));
        }
    }
}
