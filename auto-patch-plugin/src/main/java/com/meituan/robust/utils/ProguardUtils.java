package com.meituan.robust.utils;

import com.meituan.robust.autopatch.Config;
import com.meituan.robust.common.StringUtil;
import com.meituan.robust.mapping.ClassMapping;
import com.meituan.robust.mapping.ClassMethodMapping;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.MethodCall;

import static com.meituan.robust.Constants.File_SEPARATOR;
import static com.meituan.robust.autopatch.Config.classPool;
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
            RobustLog.log("Exception ", e);
        }
        return methodSignure.toString();
    }

    public static String getUnProguardMethodLongName(String methodLongName) {
        if (RobustProguardMapping.isProguard()) {
            JavaUtils.MethodInfo proguardMethodInfo = new JavaUtils.MethodInfo(methodLongName);
            if (null != proguardMethodInfo) {
                JavaUtils.MethodInfo unProguardMethodInfo = new JavaUtils.MethodInfo();
                unProguardMethodInfo.className = RobustProguardMapping.getUnProguardName(proguardMethodInfo.className);
                if (null != proguardMethodInfo.paramTypes) {
                    unProguardMethodInfo.paramTypes = new String[proguardMethodInfo.paramTypes.length];
                    int index = 0;
                    while (index < proguardMethodInfo.paramTypes.length) {
                        String proguardClassNameTemp = proguardMethodInfo.paramTypes[index];
                        String unProguardClassNameTemp = RobustProguardMapping.getUnProguardName(proguardClassNameTemp);
                        unProguardMethodInfo.paramTypes[index] = unProguardClassNameTemp;
                        index++;
                    }
                }

                String methodParams = "";
                if (null != unProguardMethodInfo.paramTypes) {
                    methodParams = String.join(",", unProguardMethodInfo.paramTypes);
                }
                String unProguardName = getUnProguardMethodName(proguardMethodInfo.className, proguardMethodInfo.methodName, null, methodParams);

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
        if (null == proguardMethodParams || "".equals(proguardMethodParams)) {
            return "";
        }

        String[] tempParams = proguardMethodParams.split(",");
        if (null == tempParams || tempParams.length == 0) {
            return "";
        }

        List<String> unProguardMethodParams = new ArrayList<String>(tempParams.length);
        for (String temClassName : tempParams) {
            unProguardMethodParams.add(RobustProguardMapping.getUnProguardName(temClassName));
        }

        return String.join(",", unProguardMethodParams);
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

    public static String getMethodID(String proguardMethodLongName) {
        if (RobustProguardMapping.isProguard()) {
            String unProguardMethodLongName = getUnProguardMethodLongName(proguardMethodLongName);
            return getSimilarMethodID(unProguardMethodLongName);
        }
        return getSimilarMethodID(proguardMethodLongName);
    }

    public static boolean isSimilar(String realMethodLongName,String robustMethodLongName){
        int index1_1 = realMethodLongName.indexOf("(");
        int index1_2 = realMethodLongName.indexOf(")");
        int index2_1 = robustMethodLongName.indexOf("(");
        int index2_2 = robustMethodLongName.indexOf(")");
        String classNameAndMethodName1 = realMethodLongName.substring(0,index1_1);
        String classNameAndMethodName2 = robustMethodLongName.substring(0,index2_1);
        if (!classNameAndMethodName1.equals(classNameAndMethodName2)){
            return false;
        }
        realMethodLongName = realMethodLongName.substring(index1_1,index1_2).replace("(","").replace(")","").trim();
        robustMethodLongName = robustMethodLongName.substring(index2_1,index2_2).replace("(","").replace(")","").trim();

        String OBJSTRING = "java.lang.Object";
        String unProguardMethodLongNamePattern = realMethodLongName.replace("$","").replace(".","\\.").replace(OBJSTRING.replace(".","\\."),".*");
        boolean isPattern = false;
        try {
            Pattern pattern = Pattern.compile(unProguardMethodLongNamePattern);
            Matcher matcher = pattern.matcher(robustMethodLongName);
            isPattern = matcher.matches();
        } catch (Exception e){

        }
        return isPattern;
    }

    public static void main(String[] args){
//        Config.methodMap = JavaUtils.getMapFromZippedFile("/Users/hedingxu/workspace-meituan/robustpatch/app-patch/robust/old/methodsMap.robust");
//        String unProguardMethodLongName = "com.sankuai.meituan.search.result.SearchResultFragment.onLoadFinished(android.support.v4.content.Loader,java.lang.Object,java.lang.Exception)";
//        String realLongName = "com.sankuai.meituan.search.result.SearchResultFragment.onLoadFinished(android.support.v4.content.Loader,com.sankuai.meituan.search.result.model.SearchResult,java.lang.Exception)";
//        String methodId = getSimilarMethodID(unProguardMethodLongName);
//        System.err.println("methodId: " + methodId);
    }

    public static String getSimilarMethodID(String unProguardMethodLongName) {
        String methodID = Config.methodMap.get(unProguardMethodLongName);
        if (null == methodID || "".equals(methodID)) {
            String OBJSTRING = "java.lang.Object";
            if (unProguardMethodLongName.contains(OBJSTRING)) {
                List<String> similarUnProguardMethodLongNameList = new ArrayList<String>();
                for (String unProguardMethodLongNameTemp : Config.methodMap.keySet()) {
                    boolean similar = isSimilar(unProguardMethodLongName,unProguardMethodLongNameTemp);
                    if (similar) {
                        similarUnProguardMethodLongNameList.add(unProguardMethodLongNameTemp);
                    }
                }

                if (similarUnProguardMethodLongNameList.size() == 1) {
                    //get first MD5
                    //find 相似度最高的方法MD5
                    String similarUnProguardMethodLongName = similarUnProguardMethodLongNameList.get(0);
                    RobustLog.log("change methodLongName from " + unProguardMethodLongName + " to " + similarUnProguardMethodLongName);
                    methodID = Config.methodMap.get(similarUnProguardMethodLongName);
                } else if (similarUnProguardMethodLongNameList.size() > 1){
                    //get first MD5
                    //find 相似度最高的方法MD5
                    String similarUnProguardMethodLongName = similarUnProguardMethodLongNameList.get(0);
                    RobustLog.log("change methodLongName from " + unProguardMethodLongName + " to " + similarUnProguardMethodLongName);
                    methodID = Config.methodMap.get(similarUnProguardMethodLongName);
                    RobustLog.log("Error: similarUnProguardMethodLongNameList size > 1 :" + similarUnProguardMethodLongNameList.size());
                } else if (similarUnProguardMethodLongNameList.size() == 0){
                    RobustLog.log("Error: similarUnProguardMethodLongNameList size == 0 ");
                }
            }
        }
        return methodID;
    }

    public static String getUnProguardMethodName(MethodCall methodCall) {
        String proguardMethodName = methodCall.getMethodName();
        if (RobustProguardMapping.isProguard()) {
            CtMethod ctMethod = null;
            try {
                ctMethod = methodCall.getMethod();
            } catch (NotFoundException e) {
                RobustLog.log("NotFoundException ", e);
            }
            if (null == ctMethod) {
                RobustLog.log("ctMethod is null", new RuntimeException("ctMethod is null ,info :" + methodCall.getClassName() + " " + methodCall.getMethodName() + " " + methodCall.getSignature()));
            } else {
                String methodSignure = null;
                try {
                    methodSignure = JavaUtils.getJavaMethodSignure(ctMethod);
                } catch (NotFoundException e) {
                    RobustLog.log("NotFoundException ", e);
                }
                String modifiedClassName = methodCall.getClassName();
                String patchClassName = new String(modifiedClassName);

                if (modifiedClassName.endsWith("Patch")) {
                    modifiedClassName = modifiedClassName + "ROBUST_FOR_DELETE";
                    String tempStr = "Patch" + "ROBUST_FOR_DELETE";
                    modifiedClassName = modifiedClassName.replace(tempStr, "");
                }
                String originClassName = new String(modifiedClassName);
                String methodLongName = modifiedClassName + "." + methodSignure;
                methodLongName = methodLongName.replace(patchClassName, originClassName);
                String unProguardMethodLongName = getUnProguardMethodLongName(methodLongName);
                JavaUtils.MethodInfo methodInfo = new JavaUtils.MethodInfo(unProguardMethodLongName);
                if (null != methodInfo) {
                    if (null != methodInfo.methodName) {
//                        RobustLog.log(" -getUnProguardMethodName-> 1 ：" + methodLongName);
//                        RobustLog.log(" -getUnProguardMethodName-> 2 ：" + unProguardMethodLongName);
                        return methodInfo.methodName;
                    }
                }
            }

        }
        return proguardMethodName;

    }

    public static boolean isLambdaFactoryMethod(String originClassName, String patchClassName, String lambdaClassName, String methodName, String methodSignature) {
        if (!AnonymousLambdaUtils.isAnonymousInnerClass_$$Lambda$1(lambdaClassName)) {
            return false;
        }
        if (RobustProguardMapping.isProguard()) {
            String proguardLambdaClassName = lambdaClassName;
            String proguardMethodName = methodName;
            String proguardMethodSignature = methodSignature;
            String proguardOriginClassName = originClassName;
            String proguardPatchClassName = patchClassName;


            if (null != proguardMethodSignature) {
                String tempproguardOriginClassName = proguardOriginClassName.replace(".", "/");
                String tempproguardPatchClassName = proguardPatchClassName.replace(".", "/");
                proguardMethodSignature = proguardMethodSignature.replace(tempproguardPatchClassName, tempproguardOriginClassName);
            }
            CtClass lambdaCtClass = null;
            try {
                lambdaCtClass = classPool.getCtClass(lambdaClassName);
            } catch (NotFoundException e) {
                RobustLog.log("NotFoundException ", e);
            }

            if (null == lambdaCtClass) {
                RobustLog.log("null == lambdaCtClass 221");
            }

            CtMethod lambdaCtMethod = null;
            try {
                lambdaCtMethod = lambdaCtClass.getMethod(proguardMethodName, proguardMethodSignature);
            } catch (NotFoundException e) {
                RobustLog.log("NotFoundException ", e);
            }
            if (null == lambdaCtMethod) {
                RobustLog.log("null == lambdaCtMethod 226");
            }

            String methodSignure = null;
            try {
                methodSignure = JavaUtils.getJavaMethodSignure(lambdaCtMethod);
            } catch (NotFoundException e) {
                RobustLog.log("NotFoundException ", e);
            }


            String unProguardMethodName = null;

            String methodLongName = proguardLambdaClassName + "." + methodSignure;
            methodLongName = methodLongName.replace(patchClassName, originClassName);
            String unProguardMethodLongName = getUnProguardMethodLongName(methodLongName);
            JavaUtils.MethodInfo methodInfo = new JavaUtils.MethodInfo(unProguardMethodLongName);
            if (null != methodInfo) {
                if (null != methodInfo.methodName) {
                    unProguardMethodName = methodInfo.methodName;
                }
            }

            if (null != unProguardMethodName && unProguardMethodName.contains("lambdaFactory$")) {
                return true;
            }
            return false;
        }
        return methodName.contains("lambdaFactory$");
    }


    private static boolean isAccessMethod2(MethodCall methodCall) {
//        static synthetic access$000
//        boolean isLambdaAccess = methodCall.getMethodName().contains("access$lambda$");
        boolean isAccess = methodCall.getMethodName().startsWith("access$");// access$100 + access$lambda$oncreate3

        if (isAccess) {
            return true;
        }
        if (RobustProguardMapping.isProguard()) {
            String unProguardMethodName = ProguardUtils.getUnProguardMethodName(methodCall);
            if (unProguardMethodName.startsWith("access$")) {
                isAccess = true;
            }
        }
        if (isAccess) {
            return true;
        }
        return false;
    }

    private static boolean isLambdaMethod2(MethodCall methodCall) {
//        static synthetic access$000
//        boolean isLambdaAccess = methodCall.getMethodName().contains("access$lambda$");
        boolean isAccess = methodCall.getMethodName().startsWith("lambda$");// access$100 + access$lambda$oncreate3

        if (isAccess) {
            return true;
        }
        if (RobustProguardMapping.isProguard()) {
            String unProguardMethodName = ProguardUtils.getUnProguardMethodName(methodCall);
            if (unProguardMethodName.startsWith("lambda$")) {
                isAccess = true;
            }
        }
        if (isAccess) {
            return true;
        }
        return false;
    }

    public static boolean isAccess$Method(ClassNode classNode, MethodNode methodNode) {
        if (!ProguardUtils.isProguard()) {
            return methodNode.name.startsWith("access$");
        }

        String dotClassName = classNode.name.replace("/", ".");
        String proguardMethodName = methodNode.name;
        String proguardMethodSignature = methodNode.desc;

        CtClass ctClass = null;
        try {
            ctClass = Config.classPool.get(dotClassName);
            for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {

            }
        } catch (Exception e) {
            RobustLog.log("Exception in 296", e);
        }

        CtMethod lambdaCtMethod = null;
        try {
            lambdaCtMethod = ctClass.getMethod(proguardMethodName, proguardMethodSignature);
        } catch (NotFoundException e) {
            RobustLog.log("NotFoundException ", e);
        }
        if (null == lambdaCtMethod) {
            RobustLog.log("null == lambdaCtMethod 226");
            return false;
        }

        String methodSignure = null;
        try {
            methodSignure = JavaUtils.getJavaMethodSignure(lambdaCtMethod);
        } catch (NotFoundException e) {
            RobustLog.log("NotFoundException ", e);
        }

        String methodLongName = dotClassName + "." + methodSignure;
        String unProguardMethodLongName = getUnProguardMethodLongName(methodLongName);
        JavaUtils.MethodInfo methodInfo = new JavaUtils.MethodInfo(unProguardMethodLongName);
        if (null != methodInfo) {
            if (null != methodInfo.methodName) {
//                        RobustLog.log(" -getUnProguardMethodName-> 1 ：" + methodLongName);
//                        RobustLog.log(" -getUnProguardMethodName-> 2 ：" + unProguardMethodLongName);
                return methodInfo.methodName.startsWith("access$");
            }
        }

        return false;
    }

    public static boolean isLambda$Method(ClassNode classNode, MethodNode methodNode) {
        if (!ProguardUtils.isProguard()) {
            return methodNode.name.startsWith("lambda$");
        }

        String dotClassName = classNode.name.replace("/", ".");
        String proguardMethodName = methodNode.name;
        String proguardMethodSignature = methodNode.desc;

        CtClass ctClass = null;
        try {
            ctClass = Config.classPool.get(dotClassName);
            for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {

            }
        } catch (Exception e) {
            RobustLog.log("Exception in 296", e);
        }

        CtMethod lambdaCtMethod = null;
        try {
            lambdaCtMethod = ctClass.getMethod(proguardMethodName, proguardMethodSignature);
        } catch (NotFoundException e) {
            RobustLog.log("NotFoundException ", e);
        }
        if (null == lambdaCtMethod) {
            RobustLog.log("null == lambdaCtMethod 226");
            return false;
        }

        String methodSignure = null;
        try {
            methodSignure = JavaUtils.getJavaMethodSignure(lambdaCtMethod);
        } catch (NotFoundException e) {
            RobustLog.log("NotFoundException ", e);
        }

        String methodLongName = dotClassName + "." + methodSignure;
        String unProguardMethodLongName = getUnProguardMethodLongName(methodLongName);
        JavaUtils.MethodInfo methodInfo = new JavaUtils.MethodInfo(unProguardMethodLongName);
        if (null != methodInfo) {
            if (null != methodInfo.methodName) {
//                        RobustLog.log(" -getUnProguardMethodName-> 1 ：" + methodLongName);
//                        RobustLog.log(" -getUnProguardMethodName-> 2 ：" + unProguardMethodLongName);
                return methodInfo.methodName.startsWith("lambda$");
            }
        }

        return false;
    }


    public static boolean isAccess$Method(MethodCall methodCall) {
        if (isAccessMethod2(methodCall)) {
            return true;
        }

        if (!RobustProguardMapping.isProguard()) {
            return methodCall.getMethodName().startsWith("access$");
        }

        String originClassName = "";
        String patchClassName = "";
        try {
            CtMethod callCtMethod = methodCall.getMethod();
            originClassName = callCtMethod.getDeclaringClass().getName();
            HashMap<String, String> customModifiedClasses = CustomModifiedClassUtils.getCustomModifiedClasses();
            patchClassName = customModifiedClasses.get(originClassName);
            if (null == patchClassName) {
                patchClassName = originClassName;
            }
        } catch (Exception e) {
            RobustLog.log("get ctmethod from method call null", e);
        }

        String callClassName = methodCall.getClassName();
        String callMethodName = methodCall.getMethodName();
        String callMethodSignature = methodCall.getSignature();

        if (RobustProguardMapping.isProguard()) {
            String proguardLambdaClassName = callClassName;
            String proguardMethodName = callMethodName;
            String proguardMethodSignature = callMethodSignature;
            String proguardOriginClassName = originClassName;
            String proguardPatchClassName = patchClassName;

            if (null != proguardMethodSignature) {
                String tempproguardOriginClassName = proguardOriginClassName.replace(".", "/");
                String tempproguardPatchClassName = proguardPatchClassName.replace(".", "/");
                proguardMethodSignature = proguardMethodSignature.replace(tempproguardPatchClassName, tempproguardOriginClassName);
            }
            CtClass innerCtClass = null;
            try {
                innerCtClass = classPool.getCtClass(callClassName);
            } catch (NotFoundException e) {
                RobustLog.log("NotFoundException ", e);
            }

            if (null == innerCtClass) {
                RobustLog.log("null == lambdaCtClass 221");
            }

            CtMethod accessCtMethod = null;
            try {
                accessCtMethod = innerCtClass.getMethod(proguardMethodName, proguardMethodSignature);
            } catch (NotFoundException e) {
                RobustLog.log("NotFoundException ", e);
            }
            if (null == accessCtMethod) {
                RobustLog.log("null == lambdaCtMethod 226");
            }

            String methodSignure = null;
            try {
                methodSignure = JavaUtils.getJavaMethodSignure(accessCtMethod);
            } catch (Exception e) {
                RobustLog.log("Exception " + methodCall.getClassName() + " " + methodCall.getMethodName() + " " + methodCall.getLineNumber(), e);
                return false;
            }


            String unProguardMethodName = null;

            String methodLongName = proguardLambdaClassName + "." + methodSignure;
            methodLongName = methodLongName.replace(patchClassName, originClassName);
            String unProguardMethodLongName = getUnProguardMethodLongName(methodLongName);
            JavaUtils.MethodInfo methodInfo = new JavaUtils.MethodInfo(unProguardMethodLongName);
            if (null != methodInfo) {
                if (null != methodInfo.methodName) {
                    unProguardMethodName = methodInfo.methodName;
                }
            }

            if (null != unProguardMethodName && unProguardMethodName.startsWith("access$")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isLambda$Method(MethodCall methodCall) {
        if (isLambdaMethod2(methodCall)) {
            return true;
        }

        if (!RobustProguardMapping.isProguard()) {
            return methodCall.getMethodName().startsWith("lambda$");
        }

        String originClassName = "";
        String patchClassName = "";
        try {
            CtMethod callCtMethod = methodCall.getMethod();
            originClassName = callCtMethod.getDeclaringClass().getName();
            HashMap<String, String> customModifiedClasses = CustomModifiedClassUtils.getCustomModifiedClasses();
            patchClassName = customModifiedClasses.get(originClassName);
            if (null == patchClassName) {
                patchClassName = originClassName;
            }
        } catch (Exception e) {
            RobustLog.log("get ctmethod from method call null", e);
        }

        String callClassName = methodCall.getClassName();
        String callMethodName = methodCall.getMethodName();
        String callMethodSignature = methodCall.getSignature();

        if (RobustProguardMapping.isProguard()) {
            String proguardLambdaClassName = callClassName;
            String proguardMethodName = callMethodName;
            String proguardMethodSignature = callMethodSignature;
            String proguardOriginClassName = originClassName;
            String proguardPatchClassName = patchClassName;

            if (null != proguardMethodSignature) {
                String tempproguardOriginClassName = proguardOriginClassName.replace(".", "/");
                String tempproguardPatchClassName = proguardPatchClassName.replace(".", "/");
                proguardMethodSignature = proguardMethodSignature.replace(tempproguardPatchClassName, tempproguardOriginClassName);
            }
            CtClass innerCtClass = null;
            try {
                innerCtClass = classPool.getCtClass(callClassName);
            } catch (NotFoundException e) {
                RobustLog.log("NotFoundException ", e);
            }

            if (null == innerCtClass) {
                RobustLog.log("null == lambdaCtClass 221");
            }

            CtMethod accessCtMethod = null;
            try {
                accessCtMethod = innerCtClass.getMethod(proguardMethodName, proguardMethodSignature);
            } catch (NotFoundException e) {
                RobustLog.log("NotFoundException ", e);
            }
            if (null == accessCtMethod) {
                RobustLog.log("null == lambdaCtMethod 226");
            }

            String methodSignure = null;
            try {
                methodSignure = JavaUtils.getJavaMethodSignure(accessCtMethod);
            } catch (NotFoundException e) {
                RobustLog.log("NotFoundException ", e);
            }


            String unProguardMethodName = null;

            String methodLongName = proguardLambdaClassName + "." + methodSignure;
            methodLongName = methodLongName.replace(patchClassName, originClassName);
            String unProguardMethodLongName = getUnProguardMethodLongName(methodLongName);
            JavaUtils.MethodInfo methodInfo = new JavaUtils.MethodInfo(unProguardMethodLongName);
            if (null != methodInfo) {
                if (null != methodInfo.methodName) {
                    unProguardMethodName = methodInfo.methodName;
                }
            }

            if (null != unProguardMethodName && unProguardMethodName.startsWith("lambda$")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInHotfixPackage(String dotClassName, String packageName) {
        if (RobustProguardMapping.isProguard()) {
            String proguardDotClassName = dotClassName;
            String unProguardDotClassName = RobustProguardMapping.getUnProguardName(proguardDotClassName);
            dotClassName = unProguardDotClassName;
        }
        boolean isInHotfixPackage = dotClassName.startsWith(packageName.trim()) || dotClassName.startsWith(packageName.trim().replace(".", File_SEPARATOR));
        return isInHotfixPackage;
    }

    public static boolean isInExceptPackage(String dotClassName, String exceptPackage) {
        if (RobustProguardMapping.isProguard()) {
            String proguardDotClassName = dotClassName;
            String unProguardDotClassName = RobustProguardMapping.getUnProguardName(proguardDotClassName);
            dotClassName = unProguardDotClassName;
        }
        boolean isInExceptPackage = dotClassName.startsWith(exceptPackage.trim()) || dotClassName.startsWith(exceptPackage.trim().replace(".", File_SEPARATOR));
        return isInExceptPackage;
    }

    public static boolean isUpdateClassNameHas$$Lambda$(String updatedClassName) {
        if (RobustProguardMapping.isProguard()) {
//            updatedClassName com/sample/test/
            updatedClassName = updatedClassName.replace("/", ".");
            String proguardDotClassName = updatedClassName;
            String unProguardDotClassName = RobustProguardMapping.getUnProguardName(proguardDotClassName);
            updatedClassName = unProguardDotClassName;
        }
        return updatedClassName.contains("$$Lambda$");
    }

    public static String getLambdaClassNameFromLine(String line) {
//                DiffLineByLine:
//                line1:     INVOKESTATIC com/meituan/sample/SecondActivity$$Lambda$4.lambdaFactory$ (Lcom/meituan/sample/SecondActivity;)Landroid/view/View$OnClickListener;
//                line2:     INVOKESTATIC com/meituan/sample/SecondActivity$$Lambda$2.lambdaFactory$ (Lcom/meituan/sample/SecondActivity;)Landroid/view/View$OnClickListener;
        String lineTemp = line.replace("INVOKESTATIC ", "").trim();
        int index_end = line.indexOf(".");
        String lambdaClassName = lineTemp.substring(0, index_end);
        return lambdaClassName;
    }

    public static boolean isMayBeLambdaFactory$InLine(String line) {
        if (line.contains("INVOKESTATIC ") && line.contains(".")) {
            int index_start = line.indexOf("INVOKESTATIC ");
            int index_end = line.indexOf(".");
            return index_end > index_start;
        }
        return false;
    }

    //只处理新的line
    public static boolean isLambdaFactory$InLine(String line) {
        if (isMayBeLambdaFactory$InLine(line)) {
            String lambdaClassName = getLambdaClassNameFromLine(line);
            String lambdaDotClassName = lambdaClassName.replace("/", ".").trim();
            if (RobustProguardMapping.isProguard()) {
                String proguardDotClassName = lambdaDotClassName;
                lambdaClassName = RobustProguardMapping.getUnProguardName(proguardDotClassName);
            }
            return AnonymousLambdaUtils.isAnonymousInnerClass_$$Lambda$1(lambdaClassName);
        }
        return false;
    }

    public static boolean isHasLambdaFactory$InLine1_Line2(String line1, String line2) {
        if (RobustProguardMapping.isProguard()) {
            return isMayBeLambdaFactory$InLine(line1) && isLambdaFactory$InLine(line2);

        } else {
            return line1.contains(".lambdaFactory$") && line2.contains(".lambdaFactory$");
        }
    }

    public static boolean isProguard() {
        return RobustProguardMapping.isProguard();
    }


    //                    if (ProguardUtils.isProguard()){
//                        lambdaClassName1 = ProguardUtils.getLambdaClassNameFromLine(line1);
//                        lambdaClassName2 = ProguardUtils.getLambdaClassNameFromLine(line2);
//                    } else {
//                        {
//                            int outerClassIndex1 = line1.indexOf(originalClass.name.replace(".class", ""));
//                            int lambdaIndex1 = line1.indexOf(".lambdaFactory$");
//                            lambdaClassName1 = line1.substring(outerClassIndex1, lambdaIndex1);
//                        }
//                        {
//                            int outerClassIndex2 = line2.indexOf(updatedClass.name.replace(".class", ""));
//                            int lambdaIndex2 = line2.indexOf(".lambdaFactory$");
//                            lambdaClassName2 = line2.substring(outerClassIndex2, lambdaIndex2);
//                        }
//                    }
    public static String getLambdaClassNameFromLine1(String line1, String originalClassName) {
        String lambdaClassName1;
        if (ProguardUtils.isProguard()) {
            lambdaClassName1 = ProguardUtils.getLambdaClassNameFromLine(line1);
        } else {
            int outerClassIndex1 = line1.indexOf(originalClassName.replace(".class", ""));
            int lambdaIndex1 = line1.indexOf(".lambdaFactory$");
            lambdaClassName1 = line1.substring(outerClassIndex1, lambdaIndex1);
        }
        return lambdaClassName1;
    }

    public static String getLambdaClassNameFromLine2(String line2, String updatedClassName) {
        String lambdaClassName2;
        if (ProguardUtils.isProguard()) {
            lambdaClassName2 = ProguardUtils.getLambdaClassNameFromLine(line2);
        } else {
            int outerClassIndex2 = line2.indexOf(updatedClassName.replace(".class", ""));
            int lambdaIndex2 = line2.indexOf(".lambdaFactory$");
            lambdaClassName2 = line2.substring(outerClassIndex2, lambdaIndex2);
        }
        return lambdaClassName2;
    }

    public static String getAnonymousClassNameFromLine(String line) {
//        new-instance
        String tempLine = new String(line);
        if (tempLine.contains("new-instance ")) {
            if (tempLine.contains(",")) {
                if (tempLine.contains("L")) {
                    if (tempLine.contains(";")) {
                        int start = tempLine.indexOf("L");
                        int end = tempLine.indexOf(";");
                        String proguardClassName = tempLine.substring(start + 1, end);
                        return proguardClassName;
                    }
                }
            }
        }
        return null;
    }

    public static boolean mayAnonymousClassNameFromLine(String line) {
        //        new-instance
        String tempLine = new String(line);
        if (tempLine.contains("new-instance ")) {
            if (tempLine.contains(",")) {
                if (tempLine.contains("L")) {
                    if (tempLine.contains(";")) {
                        int start = tempLine.indexOf("L");
                        int end = tempLine.indexOf(";");
//                        String proguardClassName = tempLine.substring(start+1,end);
                        return end > start + 1;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isHasNewAnonymousClass(String line1, String line2) {
        if (mayAnonymousClassNameFromLine(line1) && hasAnonymousClassNameFromLine2(line2)) {
            return true;
        }
        return false;
    }

    public static boolean hasAnonymousClassNameFromLine2(String line2) {
        if (mayAnonymousClassNameFromLine(line2)) {
            String proguardAnonymousDotClass = getAnonymousClassNameFromLine2(line2);
            if (ProguardUtils.isProguard()) {
                proguardAnonymousDotClass = RobustProguardMapping.getUnProguardName(proguardAnonymousDotClass);
            }
            return AnonymousLambdaUtils.isAnonymousInnerClass_$1(proguardAnonymousDotClass);
        }
        return false;
    }

    public static String getAnonymousClassNameFromLine2(String line2) {
//        new-instance
        String tempLine = new String(line2);
        if (tempLine.contains("new-instance ")) {
            if (tempLine.contains(",")) {
                if (tempLine.contains("L")) {
                    if (tempLine.contains(";")) {
                        int start = tempLine.indexOf("L");
                        int end = tempLine.indexOf(";");
                        String proguardDotClassName = tempLine.substring(start + 1, end).replace("/", ".");
                        return proguardDotClassName;
                    }
                }
            }
        }
        return null;
    }


    public static boolean isClassNameHas$(String customInnerClassName) {
        if (ProguardUtils.isProguard()) {
            customInnerClassName = RobustProguardMapping.getUnProguardName(customInnerClassName);
        }
        return customInnerClassName.contains("$");
    }

    public static boolean isSubClass(String subClassName, String outerClassName) {
        if (ProguardUtils.isProguard()) {
            subClassName = RobustProguardMapping.getUnProguardName(subClassName);
            outerClassName = RobustProguardMapping.getUnProguardName(outerClassName);
        }
        return subClassName.startsWith(outerClassName);
    }

    public static String getLambdaClassOuterClassDotName(String lambdaDotClassName) {
        String unProguardLambdaName = lambdaDotClassName;
        if (ProguardUtils.isProguard()) {
            unProguardLambdaName = RobustProguardMapping.getUnProguardName(lambdaDotClassName);
        }

        String unProguardOuterClassDotName = LambdaUtils.getOuterClassName(unProguardLambdaName);
        String proguardOuterClassDotName = unProguardOuterClassDotName;
        if (ProguardUtils.isProguard()) {
            proguardOuterClassDotName = RobustProguardMapping.getProguardName(unProguardOuterClassDotName);
        }
        return proguardOuterClassDotName;
    }

    public static String getUnProguardClassName(String dotClassName) {
        String unProguardName = dotClassName;
        if (ProguardUtils.isProguard()) {
            unProguardName = RobustProguardMapping.getUnProguardName(dotClassName);
        }
        return unProguardName;
    }

    public static boolean hasAnonymousInit(String line1, String line2) {
        return hasAnonymousInitLine(line1) && hasAnonymousInitLine(line2);
    }

    public static boolean hasAnonymousInitLine(String line) {
        return null != getAnonymousClassNameByInitString(line);
    }

    //invoke-direct {v1, p0}, Lcom/robust/sample/MainActivity$1;-><init>(
    public static String getAnonymousClassNameByInitString(String line) {
        String tempLine = new String(line);
        if (tempLine.contains("invoke-direct ")) {
            if (tempLine.contains(",")) {
                if (tempLine.contains("L")) {
                    if (tempLine.contains(";-><init>(")) {
                        int start = tempLine.indexOf("L");
                        int end = tempLine.indexOf(";-><init>(");
                        String proguardDotClassName = tempLine.substring(start + 1, end).replace("/", ".");
                        return proguardDotClassName;
                    }
                }
            }
        }
        return null;
    }

    public static boolean isAspectJField(CtField ctField) {
        try {
            String preFix = "org.aspectj.lang.JoinPoint";
            String proguardName = ctField.getType().getName();
            String unProguardName = ProguardUtils.getUnProguardClassName(proguardName);
            if (unProguardName.startsWith(preFix)) {
                return true;
            }
        } catch (NotFoundException e) {
            RobustLog.log("NotFoundException", e);
        }
        return false;
    }

}
