package com.meituan.robust.utils;

import com.meituan.robust.autopatch.Config;
import com.meituan.robust.mapping.ClassMapping;
import com.meituan.robust.mapping.ClassMethodMapping;

import java.util.ArrayList;
import java.util.List;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.MethodCall;

import static com.meituan.robust.Constants.File_SEPARATOR;
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
            if (null != proguardMethodInfo){
                JavaUtils.MethodInfo unProguardMethodInfo = new JavaUtils.MethodInfo();
                unProguardMethodInfo.className = RobustProguardMapping.getUnProguardName(proguardMethodInfo.className);
                if (null != proguardMethodInfo.paramTypes) {
                    unProguardMethodInfo.paramTypes = new String[proguardMethodInfo.paramTypes.length];
                    int index = 0;
                    while (index < proguardMethodInfo.paramTypes.length){
                        String proguardClassNameTemp = proguardMethodInfo.paramTypes[index];
                        String unProguardClassNameTemp = RobustProguardMapping.getUnProguardName(proguardClassNameTemp);
                        unProguardMethodInfo.paramTypes[index] = unProguardClassNameTemp;
                        index++;
                    }
                }

                String methodParams = "";
                if (null != unProguardMethodInfo.paramTypes){
                    methodParams = String.join(",",unProguardMethodInfo.paramTypes);
                }
                String unProguardName = getUnProguardMethodName(proguardMethodInfo.className,proguardMethodInfo.methodName,null,methodParams);

                unProguardMethodInfo.methodName = unProguardName;

                return unProguardMethodInfo.getOriginalMethodStr();
            }
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

    public static String getUnProguardMethodName(MethodCall methodCall){
        String proguardMethodName = methodCall.getMethodName();
        if (RobustProguardMapping.isProguard()){
            CtMethod ctMethod = null;
            try {
                ctMethod = methodCall.getMethod();
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
            if (null == ctMethod){
                RobustLog.log("ctMethod is null",new RuntimeException("ctMethod is null ,info :" + methodCall.getClassName() + " " + methodCall.getMethodName() + " " + methodCall.getSignature()));
            } else {
                String methodSignure = null;
                try {
                    methodSignure = JavaUtils.getJavaMethodSignure(ctMethod);
                } catch (NotFoundException e) {
                    e.printStackTrace();
                }
                String modifiedClassName = methodCall.getClassName();
                String patchClassName = new String(modifiedClassName);

                if (modifiedClassName.endsWith("Patch")) {
                    modifiedClassName = modifiedClassName + "ROBUST_FOR_DELETE";
                    String tempStr = "Patch" + "ROBUST_FOR_DELETE";
                    modifiedClassName = modifiedClassName.replace(tempStr,"");
                }
                String originClassName = new String(modifiedClassName);
                String methodLongName = modifiedClassName + "." + methodSignure;
                methodLongName = methodLongName.replace(patchClassName,originClassName);
                String unProguardMethodLongName =getUnProguardMethodLongName(methodLongName);
                JavaUtils.MethodInfo methodInfo = new JavaUtils.MethodInfo(unProguardMethodLongName);
                if (null != methodInfo){
                    if (null != methodInfo.methodName){
//                        RobustLog.log(" -getUnProguardMethodName-> 1 ：" + methodLongName);
//                        RobustLog.log(" -getUnProguardMethodName-> 2 ：" + unProguardMethodLongName);
                        return methodInfo.methodName;
                    }
                }
            }

        }
        return proguardMethodName;

    }

    public static boolean isLambdaFactoryMethod(String originClassName,String patchClassName,String lambdaClassName,String methodName,String methodSignature) {
        if (!AnonymousLambdaUtils.isAnonymousInnerClass_$$Lambda$1(lambdaClassName)){
            return false;
        }
        if (RobustProguardMapping.isProguard()){
            String proguardLambdaClassName = lambdaClassName;
            String proguardMethodName = methodName;
            String proguardMethodSignature = methodSignature;
            String proguardOriginClassName = originClassName;
            String proguardPatchClassName = patchClassName;


            if (null != proguardMethodSignature){
                String tempproguardOriginClassName = proguardOriginClassName.replace(".","/");
                String tempproguardPatchClassName = proguardPatchClassName.replace(".","/");
                proguardMethodSignature = proguardMethodSignature.replace(tempproguardPatchClassName,tempproguardOriginClassName);
            }
            CtClass lambdaCtClass = null;
            try {
                lambdaCtClass = Config.classPool.getCtClass(lambdaClassName);
            } catch (NotFoundException e) {
                e.printStackTrace();
            }

            if (null == lambdaCtClass){
                RobustLog.log("null == lambdaCtClass 221");
            }

            CtMethod lambdaCtMethod = null;
            try {
                lambdaCtMethod = lambdaCtClass.getMethod(proguardMethodName,proguardMethodSignature);
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
            if (null == lambdaCtMethod){
                RobustLog.log("null == lambdaCtMethod 226");
            }

            String methodSignure = null;
            try {
                methodSignure = JavaUtils.getJavaMethodSignure(lambdaCtMethod);
            } catch (NotFoundException e) {
                e.printStackTrace();
            }


            String unProguardMethodName = null ;

            String methodLongName = proguardLambdaClassName + "." + methodSignure;
            methodLongName = methodLongName.replace(patchClassName,originClassName);
            String unProguardMethodLongName =getUnProguardMethodLongName(methodLongName);
            JavaUtils.MethodInfo methodInfo = new JavaUtils.MethodInfo(unProguardMethodLongName);
            if (null != methodInfo){
                if (null != methodInfo.methodName){
                    unProguardMethodName = methodInfo.methodName;
                }
            }

            if (null != unProguardMethodName && unProguardMethodName.contains("lambdaFactory$")){
                return true;
            }
            return false;
        }
        return methodName.contains("lambdaFactory$");
    }

    public static boolean isInHotfixPackage(String dotClassName, String packageName){
        if (RobustProguardMapping.isProguard()){
            String proguardDotClassName = dotClassName;
            String unProguardDotClassName = RobustProguardMapping.getUnProguardName(proguardDotClassName);
            dotClassName = unProguardDotClassName;
        }
        boolean isInHotfixPackage = dotClassName.startsWith(packageName.trim()) || dotClassName.startsWith(packageName.trim().replace(".", File_SEPARATOR));
        return isInHotfixPackage;
    }

    public static boolean isInExceptPackage(String dotClassName, String exceptPackage){
        if (RobustProguardMapping.isProguard()){
            String proguardDotClassName = dotClassName;
            String unProguardDotClassName = RobustProguardMapping.getUnProguardName(proguardDotClassName);
            dotClassName = unProguardDotClassName;
        }
        boolean isInExceptPackage = dotClassName.startsWith(exceptPackage.trim()) || dotClassName.startsWith(exceptPackage.trim().replace(".", File_SEPARATOR));
        return isInExceptPackage;
    }

    public static boolean isUpdateClassNameHas$$Lambda$(String updatedClassName){
        if (RobustProguardMapping.isProguard()){
//            updatedClassName com/sample/test/
            updatedClassName = updatedClassName.replace("/",".");
            String proguardDotClassName = updatedClassName;
            String unProguardDotClassName = RobustProguardMapping.getUnProguardName(proguardDotClassName);
            updatedClassName = unProguardDotClassName;
        }
        return updatedClassName.contains("$$Lambda$");
    }

    public static String getLambdaClassNameFromLine(String line){
//                DiffLineByLine:
//                line1:     INVOKESTATIC com/meituan/sample/SecondActivity$$Lambda$4.lambdaFactory$ (Lcom/meituan/sample/SecondActivity;)Landroid/view/View$OnClickListener;
//                line2:     INVOKESTATIC com/meituan/sample/SecondActivity$$Lambda$2.lambdaFactory$ (Lcom/meituan/sample/SecondActivity;)Landroid/view/View$OnClickListener;
        String lineTemp = line.replace("INVOKESTATIC ","").trim();
        int index_end = line.indexOf(".");
        String lambdaClassName = lineTemp.substring(0,index_end);
        return lambdaClassName;
    }

    public static boolean isMayBeLambdaFactory$InLine(String line){
        if (line.contains("INVOKESTATIC ") && line.contains(".") ){
            int index_start = line.indexOf("INVOKESTATIC ");
            int index_end = line.indexOf(".");
            return index_end > index_start;
        }
        return false;
    }

    //只处理新的line
    public static boolean isLambdaFactory$InLine(String line){
        if (isMayBeLambdaFactory$InLine(line)){
            String lambdaClassName = getLambdaClassNameFromLine(line);
            String lambdaDotClassName = lambdaClassName.replace("/",".").trim();
            if (RobustProguardMapping.isProguard()){
                String proguardDotClassName = lambdaDotClassName;
                lambdaClassName = RobustProguardMapping.getUnProguardName(proguardDotClassName);
            }
            return AnonymousLambdaUtils.isAnonymousInnerClass_$$Lambda$1(lambdaClassName);
        }
        return false;
    }

    public static boolean isHasLambdaFactory$InLine1_Line2(String line1 ,String line2){
        if (RobustProguardMapping.isProguard()){
            return isMayBeLambdaFactory$InLine(line1) && isLambdaFactory$InLine(line2);

        } else {
           return line1.contains(".lambdaFactory$") && line2.contains(".lambdaFactory$");
        }
    }

    public static boolean isProguard(){
        return RobustProguardMapping.isProguard();
    }


}
