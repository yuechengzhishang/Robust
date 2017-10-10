package com.meituan.robust.utils;

import com.meituan.robust.Constants;
import com.meituan.robust.common.TxtFileReaderAndWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;

import static com.meituan.robust.Constants.ORIGINCLASS;

/**
 * Created by mivanzhang on 16/11/25.
 */

public class JavaUtils {

//    public static void removeJarFromLibs() {
//        File file;
//        for (String libName : LIB_NAME_ARRAY) {
//            file = new File(AutoPatchTransform.ROBUST_DIR + libName);
//            if (file.exists()) {
//                file.delete();
//            }
//        }
//    }

    public static void main(String[] args) {
        String path0 = "/Users/hedingxu/workspace-meituan/robustpatch/app-patch/robust/old/methodsMap.robust";

        parseRobustMethodsMap2File(path0, new File(path0 + ".txt"));
        if (true) {
            return;
        }

//        long currentTime = System.currentTimeMillis();
//        MethodInfo methodInfo = getMethodInfo(getMapFromZippedFile(path0), "3a14784fc776abddcbc524a840f8378a");
//        System.err.println("originalMethodStr: " + methodInfo.originalMethodStr);
//        System.err.println("className        : " + methodInfo.className);
//        System.err.println("methodName       : " + methodInfo.methodName);
//        System.err.println("paramTypes       : " + String.join(",", methodInfo.paramTypes));
//        System.err.println("spend time       : " + (System.currentTimeMillis() - currentTime));

        HashMap<String, String> robustMethodsMap = getMapFromZippedFile(path0);
        Set<String> keySet = robustMethodsMap.keySet();
        for (String key : keySet) {
            String methodSignature = key.trim();
            MethodInfo methodInfo = new MethodInfo(methodSignature);

//            if (methodInfo.paramTypes.length>0){
//                methodInfo.paramTypes[0] = methodInfo.paramTypes[0] + "222";
//            }
            String methodId = getMethodId(robustMethodsMap, methodInfo.className, methodInfo.methodName, methodInfo.paramTypes);
            System.err.println("methodId : " + methodId + " ,methodString : " + methodSignature);
        }

    }

    public static void parseRobustMethodsMap2File(String robustMethodsMapPathStr, File targetFile) {
        HashMap<String, String> robustMethodsMap = getMapFromZippedFile(robustMethodsMapPathStr);
        printMap2File(robustMethodsMap, targetFile);
    }

    public static void printMap2File(HashMap<String, String> robustMethodsMap, File targetFile) {
        StringBuilder methodBuilder = new StringBuilder();
        for (String key : robustMethodsMap.keySet()) {
            methodBuilder.append("key is   " + key + "  value is    " + String.valueOf(robustMethodsMap.get(key)) + "\n");
        }
        TxtFileReaderAndWriter.writeFile(targetFile, methodBuilder.toString());
    }

    public static HashMap<String, String> getMapFromZippedFile(String path) {
        File file = new File(path);
        HashMap<String, String> result = null;
        try {
            if (file.exists()) {
                FileInputStream fileIn = new FileInputStream(file);
                GZIPInputStream gzipIn = new GZIPInputStream(fileIn);
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                int count;
                byte[] data = new byte[1024];
                while ((count = gzipIn.read(data, 0, 1024)) != -1) {
                    byteOut.write(data, 0, count);
                }
                ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
                ObjectInputStream oi = new ObjectInputStream(byteIn);
                result = (HashMap<String, String>) oi.readObject();
                fileIn.close();
                gzipIn.close();
                oi.close();
            } else {
                throw new RuntimeException("getMapFromZippedFile error,file doesn't exist ,path is " + path);
            }
        } catch (Exception e) {
            RobustLog.log("Exception ",e);
            throw new RuntimeException("getMapFromZippedFile from " + path + "  error ");
        }
        return result;
    }

    public static class MethodInfo {
        public String originalMethodStr;
        public String className;
        public String methodName;
        public String[] paramTypes;

        public MethodInfo(String originalMethodStr) {
            this.originalMethodStr = originalMethodStr;
            parseMethodInfo();
        }

        public MethodInfo(){

        }
        String leftBrace = "(";
        String rightBrace = ")";
        String comma = ",";
        private void parseMethodInfo() {
            // originalMethodStr = com.meituan.sample.SecondActivity.getReflectField(java.lang.String,java.lang.Object)
            int paramStart = originalMethodStr.indexOf(leftBrace);
            int paramEnd = originalMethodStr.indexOf(rightBrace);
            if (paramEnd > 0 & paramStart > 0 & paramEnd > paramStart) {
                String paramTypesStr = this.originalMethodStr.substring(paramStart + 1, paramEnd);//java.lang.String,java.lang.Object
                if (!paramTypesStr.trim().equals("")) {
                    this.paramTypes = paramTypesStr.trim().split(comma);//[java.lang.String,java.lang.Object]
                }
                String classAndMethod = originalMethodStr.substring(0, paramStart);//com.meituan.sample.SecondActivity.getReflectField
                String[] tempStrArray = classAndMethod.split("\\.");//[......SecondActivity,getReflectField]
                if (null != tempStrArray) {
                    int tempLength = tempStrArray.length;
                    if (tempStrArray.length > 0) {
                        this.methodName = tempStrArray[tempLength - 1];//getReflectField
                        //com.meituan.sample.test.a.a()
                        this.className = (classAndMethod + leftBrace+rightBrace).replace("." + methodName + leftBrace+rightBrace,"" );//com.meituan.sample.SecondActivity
                    }
                }
            }

        }

        public String getOriginalMethodStr(){
            if (null != originalMethodStr){
                return originalMethodStr;
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(className);
                stringBuilder.append(".");
                stringBuilder.append(methodName);
                stringBuilder.append(leftBrace);
                if (null != paramTypes){
                    stringBuilder.append(String.join(comma,paramTypes));
                }
                stringBuilder.append(rightBrace);
                return stringBuilder.toString();
            }

        }

    }

    public static MethodInfo getMethodInfo(HashMap<String, String> robustMethodsMap, String robustMethodMd5) {
        robustMethodMd5 = robustMethodMd5.trim();
        for (String key : robustMethodsMap.keySet()) {
            if (robustMethodMd5.equalsIgnoreCase(String.valueOf(robustMethodsMap.get(key)))) {
                return new MethodInfo(key);
            }
        }

        return null;
    }

    public static String getMethodId(HashMap<String, String> robustMethodsMap, String className, String methodName, String[] parameterTypes) {
        String methodSignature = getMethodSignature(className, methodName, parameterTypes);
        return robustMethodsMap.get(methodSignature);
//        return RobustMethodId.getMethodId(methodSignature);
    }

    public static String getMethodSignature(String className, String methodName, String[] parameterTypes) {
        className = className.trim();
        methodName = methodName.trim();

        StringBuilder methodSignature = new StringBuilder();
        methodSignature.append(className);
        methodSignature.append(".");
        methodSignature.append(methodName);
        methodSignature.append("(");
        if (null != parameterTypes) {
            for (int i = 0; i < parameterTypes.length; i++) {
                methodSignature.append(parameterTypes[i].trim());
                if (i != parameterTypes.length - 1) {
                    methodSignature.append(",");
                }
            }
        }
        methodSignature.append(")");
        return methodSignature.toString();
    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        return count > 2147483647L ? -1 : (int) count;
    }

    private static long copyLarge(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        long count = 0L;

        int n1;
        for (boolean n = false; -1 != (n1 = input.read(buffer)); count += (long) n1) {
            output.write(buffer, 0, n1);
        }

        return count;
    }

    public static String fileMd5(File file) {
        if (!file.isFile()) {
            return "";
        }
        MessageDigest digest = null;
        byte[] buffer = new byte[4096];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            FileInputStream inputStream = new FileInputStream(file);
            while ((len = inputStream.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            inputStream.close();
        } catch (Exception e) {
            RobustLog.log("Exception ",e);
            return "";
        }

        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }


    public static boolean booleanPrimeType(String typeName) {
        return "boolean".equals(typeName);
    }
    public static String getWrapperClass(String typeName) {
        String warpperType = typeName;
        switch (typeName) {
            case "boolean":
                warpperType = "java.lang.Boolean";
                break;
            case "byte":
                warpperType = "java.lang.Byte";
                break;
            case "char":
                warpperType = "java.lang.Character";
                break;
            case "double":
                warpperType = "java.lang.Double";
                break;
            case "float":
                warpperType = "java.lang.Float";
                break;
            case "int":
                warpperType = "java.lang.Integer";
                break;
            case "long":
                warpperType = "java.lang.Long";
                break;
            case "short":
                warpperType = "java.lang.Short";
                break;
            default:
                break;
        }
        return warpperType;
    }

    public static String wrapperToPrime(String typeName) {
        String warpperType = "";
        switch (typeName) {
            case "boolean":
                warpperType = ".booleanValue()";
                break;
            case "byte":
                warpperType = ".byteValue()";
                break;
            case "char":
                warpperType = ".charValue()";
                break;
            case "double":
                warpperType = ".doubleValue()";
                break;
            case "float":
                warpperType = ".floatValue()";
                break;
            case "int":
                warpperType = ".intValue()";
                break;
            case "long":
                warpperType = ".longValue()";
                break;
            case "short":
                warpperType = ".shortValue()";
                break;
            default:
                break;
        }
        return warpperType;
    }

    public static String getParameterValue(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            stringBuilder.append("var" + i);
            if (i != length - 1) {
                stringBuilder.append(",");
            }
        }
        return stringBuilder.toString();
    }

    public static String getParameterSignure(CtMethod ctMethod) {
        StringBuilder methodSignure = new StringBuilder();
        try {
            for (int i = 0; i < ctMethod.getParameterTypes().length; i++) {
                methodSignure.append(ctMethod.getParameterTypes()[i].getName());
                methodSignure.append(" var" + i);
                if (i != ctMethod.getParameterTypes().length - 1) {
                    methodSignure.append(",");
                }
            }
        } catch (Exception e) {
            RobustLog.log("Exception ",e);
        }
        boolean hh = (methodSignure.toString() instanceof String);
        return methodSignure.toString();
    }

    public static String getRealParamtersBody(String patchClassFullName) {
        StringBuilder realParameterBuilder = new StringBuilder();
        realParameterBuilder.append("public  Object[] " + Constants.GET_REAL_PARAMETER + " (Object[] args){");
        realParameterBuilder.append("if (args == null || args.length < 1) {");
        realParameterBuilder.append(" return args;");
        realParameterBuilder.append("}");
        realParameterBuilder.append(" Object[] realParameter = new Object[args.length];");
        realParameterBuilder.append("for (int i = 0; i < args.length; i++) {");
        realParameterBuilder.append("if (args[i] instanceof " + patchClassFullName + ") {");
        realParameterBuilder.append(" realParameter[i] =this." + ORIGINCLASS + ";");
        realParameterBuilder.append("} else {");
        realParameterBuilder.append(" realParameter[i] = args[i];");
        realParameterBuilder.append(" }");
        realParameterBuilder.append(" }");
        realParameterBuilder.append("  return realParameter;");
        realParameterBuilder.append(" }");
        return realParameterBuilder.toString();
    }

    public static boolean isInnerClassInModifiedClass(String className, CtClass modifedClass) {
        //only the inner class directly defined in modifedClass
        int index = className.lastIndexOf('$');
        if (index < 0) {
            return false;
        }
        return className.substring(0, index).equals(modifedClass.getName());
    }

    public static CtClass addField_OriginClass(CtClass patchClass, CtClass sourceClass) {
        try {
            CtField originField = new CtField(sourceClass, ORIGINCLASS, patchClass);
            originField.setModifiers(AccessFlag.setPublic(originField.getModifiers()));
            patchClass.addField(originField);

//            String patchClassName = patchClass.getSimpleName();
//            System.err.println("patchClassName : "+ patchClassName);
//            StringBuilder patchClassConstruct = new StringBuilder();
//            patchClassConstruct.append(" public " +patchClassName+"(" + sourceClass.getName() + " originalObj) {");
//            patchClassConstruct.append(ORIGINCLASS + "= originalObj;");
//            patchClassConstruct.append("}");
//            CtConstructor constructor = CtNewConstructor.make(patchClassConstruct.toString(), patchClass);
//            patchClass.addConstructor(constructor);
        } catch (Exception e) {
            RobustLog.log("Exception ",e);

            throw new RuntimeException();
        }
        return patchClass;
    }

    public
    static boolean isMethodSignureContainPatchClassName(String name, List<String> modifiedClassNameList) {
        for (String classname : modifiedClassNameList) {
            if (name.startsWith(classname)) {
                return true;
            }
        }
        return false;
    }

    public static void printMap(Map<String, ?> memberMappingInfo) {
        if (memberMappingInfo == null) {
            return;
        }
        for (String key : memberMappingInfo.keySet()) {
            com.meituan.robust.utils.RobustLog.log("key is   " + key + "  value is    " + String.valueOf(memberMappingInfo.get(key)));
        }
        com.meituan.robust.utils.RobustLog.log("");
    }

    public static void printList(Collection<String> list) {
        if (list == null) {
            return;
        }
        for (String key : list)
            com.meituan.robust.utils.RobustLog.log("key is   " + key);
        com.meituan.robust.utils.RobustLog.log("");
    }


    public static String getFullClassNameFromFile(String path) {
        if (path.indexOf("classout") > 0) {
            return path.substring(path.indexOf("classout") + "classout".length() + 1, path.lastIndexOf(".smali")).replace(File.separatorChar, '.');
        }
        if (path.indexOf("main") > 0) {
            return path.substring(path.indexOf("main") + "main".length() + 1, path.lastIndexOf(".class")).replace(File.separatorChar, '.');
        }
        throw new RuntimeException("can not analysis " + path + "  get full class name error!!");
    }

    public static String eradicatReturnType(String name) {
        int blankIndex = name.indexOf(" ");
        if (blankIndex != -1) {
            //method with return type
            return name.substring(blankIndex + 1);
        } else {
            return name;
        }
    }

    public static String getJavaMethodSignure(CtMethod ctMethod) throws NotFoundException {
        StringBuilder methodSignure = new StringBuilder();
        methodSignure.append(ctMethod.getName());
        methodSignure.append("(");
        try {
            for (int i = 0; i < ctMethod.getParameterTypes().length; i++) {
                methodSignure.append(ctMethod.getParameterTypes()[i].getName());
                if (i != ctMethod.getParameterTypes().length - 1) {
                    methodSignure.append(",");
                }
            }
        } catch (Throwable throwable){
            RobustLog.log("Throwable getJavaMethodSignure ",throwable);
        }

        methodSignure.append(")");
        return methodSignure.toString();
    }

}
