package com.meituan.robust.autopatch;

import com.android.build.gradle.internal.incremental.ByteCodeUtils;
import com.meituan.robust.utils.ProguardUtils;

import org.objectweb.asm.Type;

import javassist.CtBehavior;
import javassist.CtClass;

/**
 * Created by hedingxu on 17/8/4.
 */

public class HasRobustProxyUtils {
    public static boolean hasRobustProxy(CtClass sourceClass, CtClass patchClass, CtBehavior ctMethod) {

        StringBuilder parameters = new StringBuilder();
        Type[] types = Type.getArgumentTypes(ctMethod.getSignature());
        for (Type type : types) {
            parameters.append(type.getClassName()).append(",");
        }
        if (parameters.length() > 0 && parameters.charAt(parameters.length() - 1) == ',') {
            parameters.deleteCharAt(parameters.length() - 1);
        }

        String methodName = ctMethod.getName();
        if (methodName.contains("initRobustPatch")){
            methodName = methodName.replace("initRobustPatch", ByteCodeUtils.CONSTRUCTOR);
        }
        String key = sourceClass.getName().replace('/', '.') + "." + methodName + "(" + parameters.toString() + ")";

        String methodID = ProguardUtils.getMethodID(key);

        boolean isHas;
        if (null == methodID || "".equals(methodID)){
            isHas = false;
        } else {
            isHas = true;
        }
        System.err.println(key + " hasRobustProxy : " + isHas);
        return isHas;
    }
}
