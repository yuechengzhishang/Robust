package com.meituan.robust.autopatch;

import com.meituan.robust.Constants;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;

import static com.meituan.robust.Constants.ORIGINCLASS;
import static com.meituan.robust.autopatch.CheckCodeChanges.changedClassAndItsAnonymousInnerClass;
import static com.meituan.robust.autopatch.CheckCodeChanges.isAnonymousInnerClass;

/**
 * Created by hedingxu on 17/7/23.
 */

public class RobustMethodExprEditor extends ExprEditor {
    public CtClass sourceClass;
    public CtClass patchClass;
    public CtMethod ctMethod;

    public RobustMethodExprEditor(CtClass sourceClass, CtClass patchClass, CtMethod ctMethod) {
        this.sourceClass = sourceClass;
        this.patchClass = patchClass;
        this.ctMethod = ctMethod;
    }

    public void edit(FieldAccess f) throws CannotCompileException {

        if (Config.newlyAddedClassNameList.contains(f.getClassName())) {
            return;
        }

                                try {
                                    if (f.isReader()) {
                                        f.replace(ReflectUtils.getFieldString(f.getField(), memberMappingInfo, temPatchClass.getName(), modifiedClass.getName()));
                                    } else if (f.isWriter()) {
                                        f.replace(ReflectUtils.setFieldString(f.getField(), memberMappingInfo, temPatchClass.getName(), modifiedClass.getName()));
                                    }
                                } catch (NotFoundException e) {
                                    e.printStackTrace();
                                    throw new RuntimeException(e.getMessage());
                                }
        try {
            if (f.isStatic()) {

            } else if (f.isWriter()) {
                f.replace(setFieldString(f.getField(), patchClass, sourceClass));
            } else if (f.isReader()) {

            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void edit(NewExpr e) throws CannotCompileException {
        if (Config.newlyAddedClassNameList.contains(e.getClassName()) || Config.noNeedReflectClassSet.contains(e.getClassName())) {
            return;
        }

        String newExprClassName = e.getClassName();

        if (isAnonymousInnerClass(newExprClassName)) {
            HashSet<String> anonymousInnerClasses = changedClassAndItsAnonymousInnerClass.get(patchClass.getName());
            if (anonymousInnerClasses == null) {
                anonymousInnerClasses = new LinkedHashSet<String>();
            }
            anonymousInnerClasses.add(newExprClassName);
            //记录需要变更的匿名内部类，需要处理匿名内部类里面的匿名内部类?
            changedClassAndItsAnonymousInnerClass.put(patchClass.getName(), anonymousInnerClasses);
//            try {
//                //TODO: 17/8/11 匿名内部类的构造方法改成public, 不用反射
//                e.replace(ReflectUtils.getNewInnerClassString(e.getSignature(), patchClass.getName(), ReflectUtils.isStatic(Config.classPool.get(e.getClassName()).getModifiers()), e.getClassName()));
//            } catch (NotFoundException e1) {
//                e1.printStackTrace();
//            }
            return;
        }


        //其他情况不用处理(// TODO: 17/8/3 需要将所有新增的class都设置成public的
        //需要处理 package访问属性的method,直接在插桩的时候改成public好了(同样的，把那个字段的也改了，这里就可以少很多代码了）
    }

    @Override
    public void edit(MethodCall m) throws CannotCompileException {
        boolean outerMethodIsStatic = isStatic(ctMethod.getModifiers());
        //如果是新增的class就不用替换了
        // todo 如果是新增的方法,需要内联进去
        try {
            if (Config.newlyAddedClassNameList.contains(m.getMethod().getDeclaringClass().getName())) {
                //需要换一下参数this为originalClass
                if (outerMethodIsStatic){
                    return;
                } else {
                    replaceParamThisToOriginalClassInstance(m);
                    return;
                }
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }

        int accessFlag = 0;
        try {
            accessFlag = m.getMethod().getModifiers();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        //这里的package可以在插桩的时候将所有的package （method + 构造函数 + field）都改成public
        boolean isNeedReflected = AccessFlag.isProtected(accessFlag) || AccessFlag.isPrivate(accessFlag) || AccessFlag.isPackage(accessFlag);

        if (isNeedReflected) {
            //反射 // TODO: 17/8/13

            try {
                m.replace(getMethodCallString(m, patchClass, outerMethodIsStatic));
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }

        boolean isStatic = isStatic(accessFlag);

        {//MainActivity#findViewById
            try {
                CtClass methodTargetClass = m.getMethod().getDeclaringClass();
//              System.err.println("is sub class of  " + methodTargetClass.getName() + ", " + sourceCla.getName());
                if (sourceClass.getName().equals(methodTargetClass.getName())) {
                    replaceThisToOriginClassMethodDirectly(m);
                    return;
                } else if (sourceClass.subclassOf(methodTargetClass)) {
                    //// TODO: 17/8/7 判断是否父类方法 或者本类方法
                    System.err.println("*** " + m.getMethod().getName() + " , " + sourceClass.getName() + " is sub class Of : " + methodTargetClass.getName());
                    //需要考虑一下protect方法（package方法全部在插桩的时候改掉）
                    replaceThisToOriginClassMethodDirectly(m);
                    return;
                }
            } catch (NotFoundException e) {
                e.printStackTrace();
                System.err.println("error: " + m.getClassName() + "," + m.getClass().getName() + ", ");
            }
//                                    System.err.println(m.getClassName() + "," + m.getMethodName() + "");
        }

        //todo 如何区分super方法 MainActivity#super.oncreate()
        if (m.isSuper()) {
            //放在assist类处理了
            System.err.println(m.getClassName() + "," + m.getMethodName() + ", is super: " + m.isSuper());
            m.replace(ReflectUtils.invokeSuperString(m));
            return;
        }

        // 大部分情况下是需要替换this为originalClass的
//        {
//            try {
//                boolean isContainThis = false;
//                m.getMethod().getReturnType();//// TODO: 17/8/7 处理builder 除了匿名内部类，都把this换成originalClass
//                CtClass[] paramTypes = m.getMethod().getParameterTypes();
//                StringBuilder stringBuilder = new StringBuilder();
//
//                if (null != paramTypes) {
//                    int index = 0;
//                    for (CtClass ctClass : paramTypes) {
//                        index++;
//                        //MainActivityPatch contains MainActivity and MainActivityPatch
//                        if (ctClass.getName().startsWith(sourceClass.getName())) {
//                            //this ????// TODO: 17/8/7
//                            isContainThis = true;
//                            stringBuilder.append("$" + index + "= $" + index + "." + ORIGINCLASS + ";");
//                        }
//                    }
//
//                }
//
//                if (isContainThis) {
//                    stringBuilder.append("$_ = $proceed($$);");
//                    stringBuilder.toString();
//                }
//
//            } catch (NotFoundException e) {
//                e.printStackTrace();
//            }
//
//        }

        //可能会出现一个class，被修改多次，这时候如何预警？？？
        //有一种问题，就是
        // class A {
        //   class InnerB{
        //    //如果访问了A的私有方法，则A可能会出现Method的新增(access$200),对这部分的改动需要过滤掉，使用反射处理
        //   }
        // }
//                                if (m.getMethodName().contains("access$")) {
//                                    //method contain
//
//                                    m.replace(ReflectUtils.getNewInnerClassString(m.getSignature(), patchClass.getName(), ReflectUtils.isStatic(method.getModifiers()), /*getClassValue(*/m.getClassName()));
//                                    return;
//                                }

        //处理内联？  proguard之后做，保存之前打包的jar，与现在对比;
        //就不用处理内联了?
        //做合成？
        //old.jar(same diff1)
        //new.jar(same diff2)
        //changed.jar(same diff1/diff2)
        //combined.jar(same
//                                try {
//                                    if (!repalceInlineMethod(m, method, false)) {
//                                        Map memberMappingInfo = new HashMap();
//                                        m.replace(ReflectUtils.getMethodCallString(m, memberMappingInfo, patchClass, ReflectUtils.isStatic(method.getModifiers()), false));
//                                    }
//                                } catch (Throwable e) {
//                                    e.printStackTrace();
//                                }
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

//            if (Constants.isLogging) {
//                stringBuilder.append("  android.util.Log.d(\"robust\",\"set static  value is \" +\"" + (field.getName()) + " ${getCoutNumber()}\");");
//            }
//
//            if (Constants.isLogging) {
//                stringBuilder.append("  android.util.Log.d(\"robust\",\"set value is \" + \"" + (field.getName()) + "    ${getCoutNumber()}\");");
//            }
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
        if (isStatic(ctMethod.getModifiers())){
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

    public String getParamsThisReplacedString(MethodCall m) throws NotFoundException {
        if (isStatic(ctMethod.getModifiers())){
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        CtClass methodTargetClass = m.getMethod().getDeclaringClass();
//      System.err.println("is sub class of  " + methodTargetClass.getName() + ", " + sourceCla.getName());
        if (sourceClass.getName().equals(methodTargetClass.getName())) {
            //replace this to originalClass  ,只有非静态方法才有
            CtClass[] types = m.getMethod().getParameterTypes();
            if (null == types) {

            } else {
                //针对含有this的方法做this替换
                if (types.length > 0) {
                    int indexArg = 0;
                    for (CtClass ctClass : types) {
                        indexArg++;
                        if (ctClass.equals(sourceClass) ) {
                            stringBuilder.append("$" + indexArg + "=  this." + ORIGINCLASS + ";");
                        }
                    }
                }
            }
        }
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
                        stringBuilder.append("$_=($r) " + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectStaticMethod(\"" + getMappingValue(getJavaMethodSignureWithReturnType(methodCall.method), memberMappingInfo) + "\"," + methodCall.method.declaringClass.name + ".class,\$args,new Class[]{" + signatureBuilder.toString() + "});");
                    } else
                        stringBuilder.append("$_=($r)" + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectStaticMethod(\"" + getMappingValue(getJavaMethodSignureWithReturnType(methodCall.method), memberMappingInfo) + "\"," + methodCall.method.declaringClass.name + ".class,\$args,null);");
                }
                if (Constants.isLogging) {
                    stringBuilder.append("  android.util.Log.d(\"robust\",\"invoke static  method is      ${getCoutNumber()}  \" +\"" + methodCall.getMethodName() + "\");");
                }
            } else {
                //在非static method中使用static method
                stringBuilder.append("java.lang.Object parameters[]=" + Constants.GET_REAL_PARAMETER + "(\$args);");
                if (signatureBuilder.toString().length() > 1) {
                    stringBuilder.append("$_=($r) " + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectStaticMethod(\"" + getJavaMethodSignureWithReturnType(methodCall.getMethod()) + "\"," + methodCall.getMethod().getDeclaringClass().getName() + ".class,parameters,new Class[]{" + signatureBuilder.toString() + "});");
                } else
                    stringBuilder.append("$_=($r)" + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectStaticMethod(\"" + getJavaMethodSignureWithReturnType(methodCall.getMethod()) + "\"," + methodCall.getMethod().getDeclaringClass().getName() + ".class,parameters,null);");
            }

        } else {
            if (!isInStaticMethod) {
                //在非static method中使用非static method
                stringBuilder.append(" if($0 == this ){");
                stringBuilder.append("instance=((" + patchClass.getName() + ")$0)." + Constants.ORIGINCLASS + ";")
                stringBuilder.append("}else{");
                stringBuilder.append("instance=$0;");
                stringBuilder.append("}");
                if (signatureBuilder.toString().length() > 1) {
                    stringBuilder.append("java.lang.Object parameters[]=" + Constants.GET_REAL_PARAMETER + "($args);");
                    stringBuilder.append("$_=($r) " + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectMethod(\"" + getJavaMethodSignureWithReturnType(methodCall.getMethod()) + "\",instance,parameters,new Class[]{" + signatureBuilder.toString() + "},${methodCall.method.declaringClass.name}.class);");
                } else
                    stringBuilder.append("$_=($r)" + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectMethod(\"" + getJavaMethodSignureWithReturnType(methodCall.getMethod()) + "\",instance,$args,null,"+methodCall.getMethod().getDeclaringClass().getName()+".class);");
                if (Constants.isLogging) {
                    stringBuilder.append("  android.util.Log.d(\"robust\",\"invoke  method is      ${getCoutNumber()} \" +\"" + methodCall.getMethodName() + "\");");
                }
            } else {
                stringBuilder.append("instance=(" + methodCall.getMethod().getDeclaringClass().getName() + ")$0;");
                //在static method中使用非static method
                if (signatureBuilder.toString().length() > 1) {
                    stringBuilder.append("$_=($r) " + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectMethod(\"" + methodCall.getMethod() + "\",instance,$args,new Class[]{" + signatureBuilder.toString() + "},${methodCall.method.declaringClass.name}.class);");
                } else
                    stringBuilder.append("$_=($r)" + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectMethod(\"" + methodCall.getMethod()+ "\",instance,$args,null,"+methodCall.getMethod().getDeclaringClass().getName()+".class);");

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

}
