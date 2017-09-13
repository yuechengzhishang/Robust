package com.meituan.robust.autopatch;

import com.meituan.robust.Constants;
import com.meituan.robust.change.RobustChangeInfo;
import com.meituan.robust.utils.ProguardUtils;
import com.meituan.robust.utils.RobustLog;

import java.util.ArrayList;
import java.util.List;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;

import static com.meituan.robust.Constants.ORIGINCLASS;

/**
 * Created by hedingxu on 17/7/23.
 * handle access$100
 */

public class AnonymousInnerClassMethodExprEditor extends ExprEditor {
    public CtClass anonymousInnerClass;
    public String outerSourceClassName;
    public String outerPatchClassName;
    public CtMethod ctMethod;

    public AnonymousInnerClassMethodExprEditor(CtClass anonymousInnerClass, String outerSourceClassName, String outerPatchClassName, CtMethod ctMethod) {
        this.anonymousInnerClass = anonymousInnerClass;
        this.outerSourceClassName = outerSourceClassName;
        this.outerPatchClassName = outerPatchClassName;
        this.ctMethod = ctMethod;
    }


//    private boolean is_access$lambda$_method(MethodCall methodCall) {
//        boolean isLambdaAccess = methodCall.getMethodName().contains("access$lambda$");
//        return isLambdaAccess;
//    }

//    public static void main(String[] args) {
//        if ("access$000".matches("access\\$[0-9]{1,5}")) {
//            System.err.println("it is access method");
//        }
//    }

    @Override
    public void edit(MethodCall m) throws CannotCompileException {
//        System.err.println("MethodCall :" + m.getMethodName());
        boolean outerMethodIsStatic = isStatic(ctMethod.getModifiers());
        if (outerMethodIsStatic) {
            return;
        }
        //static synthetic access$000
        if (ProguardUtils.isAccess$Method(m)) {
            System.err.println(m.getMethodName() + " is access method");
            /*
            String zz = MainActivity.access$300(this.this$0); //非静态private方法  MainActivityPatch.access$300(this.outerPatchClassName);
            MainActivity.access$400(this.this$0, zz); //非静态private方法
            MainActivity.access$500(); //静态private方法
            */

            //replace access method
            //replace params

            //// TODO: 17/9/10 不处理$$lambda$1中的access方法, testing
//            if (is_access$lambda$_method(m)) {
//                final List<CtMethod> ctMethods = new ArrayList<>();
//                try {
//                    m.getMethod().instrument(new ExprEditor() {
//                        /**
//                         * Edits a method call (overridable).
//                         *
//                         * The default implementation performs nothing.
//                         */
//                        @Override
//                        public void edit(MethodCall m) throws CannotCompileException {
//                            try {
//                                ctMethods.add(m.getMethod());
//                            } catch (NotFoundException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    });
//                } catch (NotFoundException e) {
//                    e.printStackTrace();
//                }
//                CtMethod ctMethod1 = ctMethods.get(0);
//                if (!isStatic(ctMethod1.getModifiers())) {
//                    String lambda$onCreate$3_methodName = ctMethod1.getName();
//                    List<String> params = new ArrayList<>();
//                    try {
//                        if (m.getMethod().getParameterTypes().length == ctMethod1.getParameterTypes().length +1){
//                            int index = 0;
//                            while (index < ctMethod1.getParameterTypes().length){
//                                index++;
//                                params.add("$"+(index+1));
//                            }
//                        }
//                    } catch (NotFoundException e) {
//                        e.printStackTrace();
//                    }
//
//                    String statement = "this." + "outerPatchClassName" + "." + lambda$onCreate$3_methodName + "(" + String.join(",",params)+");";
//                    m.replace(statement);
//                    return;
//                }
//            }


            CtMethod callCtMethod;
            try {
                callCtMethod = m.getMethod();
            } catch (Throwable t) {
                RobustLog.log("callCtMethod is Throwable ", t);
                return;
            }

            if (RobustChangeInfo.isNewAddMethod(callCtMethod)) {
                if (callCtMethod.getDeclaringClass().getName().equals(outerPatchClassName)) {
                    //it is right
                }
                if (callCtMethod.getDeclaringClass().getName().equals(outerSourceClassName)) {
                    try {
                        CtClass[] params = callCtMethod.getParameterTypes();
                        List<String> paramList = new ArrayList<String>();
                        int index = 0;
                        StringBuilder stringBuilder = new StringBuilder();
                        for (CtClass param : params) {
                            index++;
                            if (param.getName().equals(outerSourceClassName)) {
                                stringBuilder.append(outerPatchClassName + " patchInstance" + index + " = new " + outerPatchClassName + "();");
                                stringBuilder.append(" patchInstance" + index + "." + ORIGINCLASS + " = $" + index + ";");
                                paramList.add("patchInstance" + index);
                            } else {
                                paramList.add("$" + index);
                            }
                        }
                        String statement = "$_=($r) " + outerPatchClassName + "." + m.getMethodName() + "(" + String.join(",",paramList)+ ")" + " ; ";
                        m.replace(statement);
                    } catch (javassist.CannotCompileException e) {
                        RobustLog.log("javassist.CannotCompileException", e);
                    } catch (NotFoundException e) {
                        RobustLog.log("NotFoundException", e);
                    }
                }
                return;
            } else {
                if (callCtMethod.getDeclaringClass().getName().equals(outerPatchClassName)) {
                    try {
                        CtClass[] params = callCtMethod.getParameterTypes();
                        List<String> paramList = new ArrayList<String>();
                        int index = 0;
                        StringBuilder stringBuilder = new StringBuilder();
                        for (CtClass param : params) {
                            index++;
                            if (param.getName().equals(outerPatchClassName)) {
                                paramList.add("$" + index + "." + ORIGINCLASS);
                            } else {
                                paramList.add("$" + index);
                            }
                        }
                        String statement = "$_=($r) " + outerSourceClassName + "." + m.getMethodName() + "(" + String.join(",",paramList)+ ")" + " ; ";
                        m.replace(statement);
                    } catch (javassist.CannotCompileException e) {
                        RobustLog.log("javassist.CannotCompileException", e);
                    } catch (NotFoundException e) {
                        RobustLog.log("NotFoundException", e);
                    }
                }
                if (callCtMethod.getDeclaringClass().getName().equals(outerSourceClassName)) {
                    //it is right
                }
                return;
            }


        }

    }


    private String getParamsStr(MethodCall methodCall) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        CtClass[] params = null;
        try {
            params = methodCall.getMethod().getParameterTypes();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        if (null != params && params.length > 0) {
            List<String> paramList = new ArrayList<String>();
            int index = 0;
            for (CtClass param : params) {
                index++;
                if (param.getName().equals(outerSourceClassName)) {
                    paramList.add("this.outerPatchClassName");
                } else {
                    paramList.add("$" + index);
                }
            }
            stringBuilder.append(String.join(",", paramList));
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }


    public static String setFieldString(CtField field, CtClass patchClass, CtClass sourceClass) {


        CtClass fieldDeclaringClass = field.getDeclaringClass();
        boolean isWriteSuperClassField = patchClass.subclassOf(fieldDeclaringClass);

        boolean isStatic = isStatic(field.getModifiers());
        StringBuilder stringBuilder = new StringBuilder("{");


        String patchClassName = patchClass.getName();
        String originalClassName = sourceClass.getName();
        String declaringClassName = field.getDeclaringClass().getName();
        //静态字段
        if (isStatic) {
            System.err.println("setFieldString static field " + field.getName() + "  declaringClass   " + declaringClassName);

            if (declaringClassName.equals(patchClassName)) {
                //如果是本patch类的field
                //如果是新增的字段，需要重新处理一下 // TODO: 17/8/2
                stringBuilder.append("$_ = $proceed($$);");
                stringBuilder.append(Constants.ROBUST_UTILS_FULL_NAME + ".setStaticFieldValue(\"" + field.getName() + "\"," + originalClassName + ".class,$1);");
            } else if (declaringClassName.equals(originalClassName)) {
                //如果是本patch类的field
                stringBuilder.append("$_ = $proceed($$);");
                stringBuilder.append(Constants.ROBUST_UTILS_FULL_NAME + ".setStaticFieldValue(\"" + field.getName() + "\"," + patchClassName + ".class,$1);");
            } else if (isWriteSuperClassField) {
                stringBuilder.append("$_ = $proceed($$);");
                //如果是父类的field,静态字段无需处理
            } else if (AccessFlag.isPackage(field.getModifiers())) {
                //package
//                stringBuilder.append("$_ = $proceed($$);");
                stringBuilder.append(Constants.ROBUST_UTILS_FULL_NAME + ".setStaticFieldValue(\"" + field.getName() + "\"," + declaringClassName + ".class,$1);");
            } else {
                //保持原样
                stringBuilder.append("$_ = $proceed($$);");
            }

            if (Constants.isLogging) {
                stringBuilder.append("  android.util.Log.d(\"robust\",\"set static  value is \" +\"" + (field.getName()) + " ${getCoutNumber()}\");");
            }
        } else {
            //非静态字段
            System.err.println("setFieldString field " + field.getName() + "  declaringClass   " + declaringClassName);

            if (declaringClassName.equals(patchClassName)) {
                //如果是新增的字段，需要重新处理一下 // TODO: 17/8/2
                //如果是本patch类的field
                stringBuilder.append("$_ = $proceed($$);");

                stringBuilder.append("{");
                stringBuilder.append(originalClassName + " instance;");
                stringBuilder.append("instance=((" + patchClassName + ")$0)." + ORIGINCLASS + ";");
                stringBuilder.append(Constants.ROBUST_UTILS_FULL_NAME + ".setFieldValue(\"" + (field.getName()) + "\",instance,$1," + originalClassName + ".class);");
                stringBuilder.append("}");
//                stringBuilder.append(Constants.ROBUST_UTILS_FULL_NAME + ".setFieldValue(\"" + field.getName() + "\"," + originalClassName + ".class,$1);");
            } else if (declaringClassName.equals(originalClassName)) {
                //如果是本patch类的field
                stringBuilder.append("$_ = $proceed($$);");

                stringBuilder.append("{");
                stringBuilder.append(originalClassName + " instance;");
                stringBuilder.append("instance=((" + patchClassName + ")$0)." + ORIGINCLASS + ";");
                stringBuilder.append(Constants.ROBUST_UTILS_FULL_NAME + ".setFieldValue(\"" + (field.getName()) + "\",instance,$1," + originalClassName + ".class);");
                stringBuilder.append("}");

//                stringBuilder.append(Constants.ROBUST_UTILS_FULL_NAME + ".setFieldValue(\"" + field.getName() + "\"," + patchClassName + ".class,$1);");
            } else if (isWriteSuperClassField) {
                stringBuilder.append("$_ = $proceed($$);");

                stringBuilder.append("{");
                stringBuilder.append(originalClassName + " instance;");
                stringBuilder.append("instance = ((" + patchClassName + ")$0)." + ORIGINCLASS + ";");
                stringBuilder.append(Constants.ROBUST_UTILS_FULL_NAME + ".setFieldValue(\"" + (field.getName()) + "\",instance,$1," + declaringClassName + ".class);");
                stringBuilder.append("}");

//                stringBuilder.append(Constants.ROBUST_UTILS_FULL_NAME + ".setFieldValue(\"" + field.getName() + "\"," + originalClassName + ".class,$1);");
            } else if (AccessFlag.isPackage(field.getModifiers())) {
                //package
//                stringBuilder.append("$_ = $proceed($$);");
                stringBuilder.append("{");
                stringBuilder.append(declaringClassName + " instance;");
                stringBuilder.append("instance = (" + declaringClassName + ") $0;");
                stringBuilder.append(Constants.ROBUST_UTILS_FULL_NAME + ".setFieldValue(\"" + (field.getName()) + "\",instance,$1," + declaringClassName + ".class);");
                stringBuilder.append("}");

            } else {
                //保持原样
                stringBuilder.append("$_ = $proceed($$);");
            }
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    static boolean isStatic(int modifiers) {
        return (modifiers & AccessFlag.STATIC) != 0;
    }


    public void replaceThisToOriginClassMethodDirectly(MethodCall m) throws NotFoundException, CannotCompileException {
        int accessFlag = m.getMethod().getModifiers();
        if (AccessFlag.isProtected(accessFlag) || AccessFlag.isPrivate(accessFlag) || AccessFlag.isPackage(accessFlag)) {
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        stringBuilder.append(getParamsThisReplacedString(m));
        stringBuilder.append("$_=($r)this." + ORIGINCLASS + "." + m.getMethod().getName() + "($$);");
        stringBuilder.append("}");
        m.replace(stringBuilder.toString());
    }

    public void replaceParamThisToOriginalClassInstance(MethodCall m) throws NotFoundException, CannotCompileException {
        if (isStatic(ctMethod.getModifiers())) {
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        stringBuilder.append(getParamsThisReplacedString(m));
        stringBuilder.append("$_ = $proceed($$);");
        stringBuilder.append("}");
        m.replace(stringBuilder.toString());
        return;
    }

    public void replaceParamThisToOriginalClassInstance2(NewExpr m) throws NotFoundException, CannotCompileException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        System.err.println("replaceParamThisToOriginalClassInstance2 :" + m.getClassName());
        stringBuilder.append(getParamsThisReplacedString2(m));
        stringBuilder.append("$_ = $proceed($$);");
        stringBuilder.append("}");
        m.replace(stringBuilder.toString());
        return;
    }

    public String getParamsThisReplacedString2(NewExpr m) throws NotFoundException {
//        if (isStatic(ctMethod.getModifiers())) {
//            return "";
//        }
        StringBuilder stringBuilder = new StringBuilder();
//      System.err.println("is sub class of  " + methodTargetClass.getName() + ", " + sourceCla.getName());
        //replace this to originalClass  ,只有非静态方法才有
        CtClass[] types = m.getConstructor().getParameterTypes();
        if (null == types) {

        } else {
            //针对含有this的方法做this替换
            if (types.length > 0) {
                int indexArg = 0;
                for (CtClass ctClass : types) {
                    indexArg++;
                    System.err.println("getParamsThisReplacedString2 : " + ctClass.getName() + ":" + anonymousInnerClass.getName());
                    if (anonymousInnerClass.subclassOf(ctClass)) {
                        stringBuilder.append("$" + indexArg + "=  this." + ORIGINCLASS + ";");
                    }
                }
            }
        }
        return stringBuilder.toString();
    }


    public String getParamsThisReplacedString(MethodCall m) throws NotFoundException {
        if (isStatic(ctMethod.getModifiers())) {
            return "";
        }
//        if (m.getSignature().contains("isGrantSDCardReadPermission")) {
//            System.err.println("isGrantSDCardReadPermission");
//        }
        StringBuilder stringBuilder = new StringBuilder();
        CtClass methodTargetClass = m.getMethod().getDeclaringClass();
//      System.err.println("is sub class of  " + methodTargetClass.getName() + ", " + sourceCla.getName());
//        if (anonymousInnerClass.getName().equals(methodTargetClass.getName())) {
        //replace this to originalClass  ,只有非静态方法才有
        CtClass[] types = m.getMethod().getParameterTypes();
        if (null == types) {

        } else {
            //针对含有this的方法做this替换
            if (types.length > 0) {
                int indexArg = 0;
                for (CtClass ctClass : types) {
                    indexArg++;
                    if (anonymousInnerClass.subclassOf(ctClass)) {
                        stringBuilder.append("$" + indexArg + "=  this." + ORIGINCLASS + ";");
                    }
                }
            }
        }
//        }
        return stringBuilder.toString();
    }


    private static String getMethodCallString(MethodCall methodCall, CtClass patchClass, boolean isInStaticMethod) throws NotFoundException {
        String signatureBuilder = getParameterClassString(methodCall.getMethod().getParameterTypes());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        stringBuilder.append(methodCall.getMethod().getDeclaringClass().getName() + " instance;");
        if (isStatic(methodCall.getMethod().getModifiers())) {
            if (isInStaticMethod) {
                //在static method使用static method
                if (AccessFlag.isPublic(methodCall.getMethod().getModifiers())) {
                    stringBuilder.append("$_ = $proceed($$);");
                } else {
                    if (signatureBuilder.toString().length() > 1) {
                        stringBuilder.append("$_=($r) " + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectStaticMethod(\"" + getJavaMethodSignureWithReturnType(methodCall.getMethod()) + "\"," + methodCall.getMethod().getDeclaringClass().getName() + ".class,$args,new Class[]{" + signatureBuilder.toString() + "});");
                    } else
                        stringBuilder.append("$_=($r)" + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectStaticMethod(\"" + getJavaMethodSignureWithReturnType(methodCall.getMethod()) + "\"," + methodCall.getMethod().getDeclaringClass().getName() + ".class,$args,null);");
                }
                if (Constants.isLogging) {
                    stringBuilder.append("  android.util.Log.d(\"robust\",\"invoke static  method is      ${getCoutNumber()}  \" +\"" + methodCall.getMethodName() + "\");");
                }
            } else {
                //在非static method中使用static method
                stringBuilder.append("java.lang.Object parameters[]=" + Constants.GET_REAL_PARAMETER + "($args);");
                if (signatureBuilder.toString().length() > 1) {
                    stringBuilder.append("$_=($r) " + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectStaticMethod(\"" + getJavaMethodSignureWithReturnType(methodCall.getMethod()) + "\"," + methodCall.getMethod().getDeclaringClass().getName() + ".class,parameters,new Class[]{" + signatureBuilder.toString() + "});");
                } else
                    stringBuilder.append("$_=($r)" + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectStaticMethod(\"" + getJavaMethodSignureWithReturnType(methodCall.getMethod()) + "\"," + methodCall.getMethod().getDeclaringClass().getName() + ".class,parameters,null);");
            }

        } else {
            if (!isInStaticMethod) {
                //在非static method中使用非static method
                stringBuilder.append(" if($0 == this ){");
                stringBuilder.append("instance=((" + patchClass.getName() + ")$0)." + Constants.ORIGINCLASS + ";");
                stringBuilder.append("}else{");
                stringBuilder.append("instance=$0;");
                stringBuilder.append("}");
                if (signatureBuilder.toString().length() > 1) {
                    stringBuilder.append("java.lang.Object parameters[]=" + Constants.GET_REAL_PARAMETER + "($args);");
                    stringBuilder.append("$_=($r) " + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectMethod(\"" + getJavaMethodSignureWithReturnType(methodCall.getMethod()) + "\",instance,parameters,new Class[]{" + signatureBuilder.toString() + "},${methodCall.method.declaringClass.name}.class);");
                } else
                    stringBuilder.append("$_=($r)" + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectMethod(\"" + getJavaMethodSignureWithReturnType(methodCall.getMethod()) + "\",instance,$args,null," + methodCall.getMethod().getDeclaringClass().getName() + ".class);");
                if (Constants.isLogging) {
                    stringBuilder.append("  android.util.Log.d(\"robust\",\"invoke  method is      ${getCoutNumber()} \" +\"" + methodCall.getMethodName() + "\");");
                }
            } else {
                stringBuilder.append("instance=(" + methodCall.getMethod().getDeclaringClass().getName() + ")$0;");
                //在static method中使用非static method
                if (signatureBuilder.toString().length() > 1) {
                    stringBuilder.append("$_=($r) " + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectMethod(\"" + methodCall.getMethod() + "\",instance,$args,new Class[]{" + signatureBuilder.toString() + "},${methodCall.method.declaringClass.name}.class);");
                } else
                    stringBuilder.append("$_=($r)" + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectMethod(\"" + methodCall.getMethod() + "\",instance,$args,null," + methodCall.getMethod().getDeclaringClass().getName() + ".class);");

            }
        }
//        }
        stringBuilder.append("}");
//        println("getMethodCallString  " + stringBuilder.toString())
        return stringBuilder.toString();
    }

    private static String getParameterClassString(CtClass[] parameters) {
        if (parameters == null || parameters.length < 1) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int index = 0; index < parameters.length; index++) {
            stringBuilder.append(parameters[index].getName() + ".class");
            if (index != parameters.length - 1) {
                stringBuilder.append(",");
            }
        }
        return stringBuilder.toString();
    }

    private static String getJavaMethodSignureWithReturnType(CtMethod ctMethod) throws NotFoundException {
        StringBuilder methodSignure = new StringBuilder();
        methodSignure.append(ctMethod.getReturnType().getName());
        methodSignure.append(" ");
        methodSignure.append(ctMethod.getName());
        methodSignure.append("(");
        for (int i = 0; i < ctMethod.getParameterTypes().length; i++) {
            methodSignure.append(ctMethod.getParameterTypes()[i].getName());
            if (i != ctMethod.getParameterTypes().length - 1) {
                methodSignure.append(",");
            }
        }
        methodSignure.append(")");
        return methodSignure.toString();
    }


    private static String NewExprParamsReplaceThisMethod(int count) {
        StringBuilder stringBuilder = new StringBuilder();
        int index = 0;
        while (index < count) {
//            stringBuilder.append("    if ($0 ==  " + "$" + (index + 1) + ") {");
            stringBuilder.append("$" + (index + 1) + "=  this." + ORIGINCLASS + ";");
//            stringBuilder.append("    }");
            index++;
        }
        return stringBuilder.toString();
    }

}
