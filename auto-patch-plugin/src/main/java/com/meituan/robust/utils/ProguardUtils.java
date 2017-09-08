package com.meituan.robust.utils;

import com.meituan.robust.autopatch.Config;
import com.meituan.robust.mapping.ClassMapping;
import com.meituan.robust.mapping.ClassMethodMapping;

import java.util.ArrayList;
import java.util.List;

import javassist.CtMethod;

import static com.meituan.robust.utils.RobustProguardMapping.proguardClassMappings;

/**
 * Created by hedingxu on 17/8/8.
 */

public class ProguardUtils {
    public static String getParameterTypeSignature(CtMethod ctMethod) {
        StringBuilder methodSignure = new StringBuilder();
        try {
            for (int i = 0; i < ctMethod.getParameterTypes().length; i++) {
                methodSignure.append(ctMethod.getParameterTypes()[i].getName());
                if (i != ctMethod.getParameterTypes().length - 1) {
                    methodSignure.append(",");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return methodSignure.toString();
    }
    public static String getUnProguardMethodLongName(String methodLongName){
        if (RobustProguardMapping.isProguard()) {
            JavaUtils.MethodInfo proguardMethodInfo = new JavaUtils.MethodInfo(methodLongName);

            JavaUtils.MethodInfo unProguardMethodInfo = new JavaUtils.MethodInfo();
            unProguardMethodInfo.className = RobustProguardMapping.getUnProguardName(proguardMethodInfo.className);
            unProguardMethodInfo.paramTypes = new String[proguardMethodInfo.paramTypes.length];
            if (null != proguardMethodInfo.paramTypes) {
                int index = 0;
                while (index < proguardMethodInfo.paramTypes.length){
                    String proguardClassNameTemp = proguardMethodInfo.paramTypes[index];
                    String unProguardClassNameTemp = RobustProguardMapping.getUnProguardName(proguardClassNameTemp);
                    unProguardMethodInfo.paramTypes[index] = unProguardClassNameTemp;
                    index++;
                }
            }

            String methodParams = String.join(",",unProguardMethodInfo.paramTypes);
            String unProguardName = getUnProguardMethodName(proguardMethodInfo.className,proguardMethodInfo.methodName,null,methodParams);

            unProguardMethodInfo.methodName = unProguardName;

            return unProguardMethodInfo.getOriginalMethodStr();
        }
        return methodLongName;
    }

    //还原class
    //还原参数列表
    //还原方法名称



//    private static List<JavaUtils.MethodInfo> robustMethodInfoList;
//    public static void convertRobustMethodMap2MethodInfoList(){
//        if (null == robustMethodInfoList){
//            if (null != Config.methodMap){
//                robustMethodInfoList = new ArrayList<JavaUtils.MethodInfo>();
//                Set<String> keySet = Config.methodMap.keySet();
//                for (String key : keySet) {
//                    String methodSignature = key.trim();
//
//
//                    String methodId = getMethodId(robustMethodsMap, methodInfo.className, methodInfo.methodName, methodInfo.paramTypes);
//                    System.err.println("methodId : " + methodId + " ,methodString : " + methodSignature);
//                }
//            }
//        }
//    }


    public static String getUnProguardMethodParamsStr(String proguardMethodParams) {
        if (null == proguardMethodParams || "".equals(proguardMethodParams)){
            return "";
        }

        String[] tempParams = proguardMethodParams.split(",");
        if (null == tempParams || tempParams.length == 0){
            return "";
        }

        List<String> unProguardMethodParams = new ArrayList<String>(tempParams.length);
        for (String temClassName : tempParams){
            unProguardMethodParams.add(RobustProguardMapping.getUnProguardName(temClassName));
        }

        return String.join(",",unProguardMethodParams);
    }

    public static String getUnProguardMethodName(String proguardClassName, String proguardMethodName, String proguardMethodReturnType, String proguardMethodParams) {
        if (RobustProguardMapping.isProguard()) {
            if (proguardClassMappings.containsKey(proguardClassName)) {
                ClassMapping classMapping = proguardClassMappings.get(proguardClassName);
                List<ClassMethodMapping> methodMappings = classMapping.methodMappings;
                if (null == methodMappings || methodMappings.size() == 0) {

                } else {
                    for (ClassMethodMapping classMethodMapping : methodMappings) {
                        if (classMethodMapping.newMethodName.equals(proguardMethodName)) {
                            if (classMethodMapping.methodArguments.equals(proguardMethodParams)) {
                                if (true/* && proguardMethodReturnType.equals(classMethodMapping.methodReturnType)*/) {
                                    return classMethodMapping.methodName;
                                }
                            }
                        }
                    }
                }
            }
        }
        return proguardMethodName;
    }

    public static String getMethodID(String proguardMethodLongName ){
        if (RobustProguardMapping.isProguard()){
            String unProguardMethodLongName =getUnProguardMethodLongName(proguardMethodLongName);
            return Config.methodMap.get(unProguardMethodLongName);
        }
        return Config.methodMap.get(proguardMethodLongName);
    }
}
