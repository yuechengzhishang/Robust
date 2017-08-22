package robust.gradle.plugin.asm;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;

/**
 * Created by hedingxu on 17/8/22.
 */

public class ReflectMethod {
    public static boolean isOverrideMethod(ClassNode classNode ,MethodNode methodNode ){
        String className = classNode.name;
        String methodName = methodNode.name;
//        methodNode.parameters;
        return true;
    }

    public static Method getDeclaredMethod(Object object, String methodName, Class[] parameterTypes, Class declaringClass) {
        Method method = null;
        if (null == declaringClass || !declaringClass.isInterface()) {

            for (Class<?> clazz = object.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
                try {
                    method = clazz.getDeclaredMethod(methodName, parameterTypes);
                    if (!method.isAccessible()) {
                        method.setAccessible(true);
                    }
                    if (null == declaringClass || clazz.equals(declaringClass)) {
                        return method;
                    }
                } catch (Exception e) {
                }
            }
        } else {
            try {
                method = declaringClass.getDeclaredMethod(methodName, parameterTypes);
                return method;
            } catch (Exception e) {

            }
        }
        return null;
    }


}
