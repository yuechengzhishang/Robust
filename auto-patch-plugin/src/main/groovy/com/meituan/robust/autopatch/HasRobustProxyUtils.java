package com.meituan.robust.autopatch;

import com.android.build.gradle.internal.incremental.ByteCodeUtils;
import com.meituan.robust.utils.JavaUtils;

import org.objectweb.asm.Type;

import java.io.File;
import java.util.HashMap;

import javassist.CtClass;
import javassist.CtMethod;

/**
 * Created by hedingxu on 17/9/4.
 */

public class HasRobustProxyUtils {
    public static final String METHOD_MAP = "methodsMap.robust";

    static HashMap<String, String> methodMaps;

    public static boolean hasRobustProxy(CtClass sourceClass, CtClass patchClass, CtMethod ctMethod) {
        if (null == methodMaps){
            File methodMap = new File(Config.robustGenerateDirectory, METHOD_MAP);
            methodMaps = JavaUtils.getMapFromZippedFile(methodMap.getAbsolutePath());
        }

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

        boolean isHas = methodMaps.containsKey(key);
        System.err.println(key + " hasRobustProxy : " + isHas);
        return isHas;
    }
}
