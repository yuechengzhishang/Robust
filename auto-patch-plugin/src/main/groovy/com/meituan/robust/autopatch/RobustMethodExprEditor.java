package com.meituan.robust.autopatch;

import com.meituan.robust.Constants;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.expr.Expr;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import javassist.expr.NewArray;
import javassist.expr.NewExpr;

import static com.meituan.robust.Constants.ORIGINCLASS;

/**
 * Created by hedingxu on 17/7/23.
 */

public class RobustMethodExprEditor extends ExprEditor {
    public CtClass sourceClass;
    public CtClass patchClass;
    public CtMethod ctMethod;
    private boolean hasRobustProxyCode = false;
    private boolean hasHandledProxyCode = false;

    private boolean isNeedReplaceEmpty() {
        if (hasRobustProxyCode) {
            return hasHandledProxyCode == false;
        }
        return false;
    }

    private boolean repalceWithEmpty(Expr expr) {
        if (isNeedReplaceEmpty()) {
            try {
//                expr.replace(";");
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public RobustMethodExprEditor(CtClass sourceClass, CtClass patchClass, CtMethod ctMethod) {
        this.sourceClass = sourceClass;
        this.patchClass = patchClass;
        this.ctMethod = ctMethod;
        this.hasRobustProxyCode = true;
    }

    public void edit(FieldAccess f) throws CannotCompileException {

        if (repalceWithEmpty(f)) {
            return;
        }

        if (Config.newlyAddedClassNameList.contains(f.getClassName())) {
            return;
        }

        try {
            if (f.isReader()) {
                f.replace(ReflectUtils.getFieldString2(f.getField(), patchClass.getName(), sourceClass.getName()));
            } else if (f.isWriter()) {
                f.replace(ReflectUtils.setFieldString2(f.getField(), patchClass.getName(), sourceClass.getName()));
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void edit(NewArray a) throws CannotCompileException {
        if (repalceWithEmpty(a)) {
            return;
        }
    }

    @Override
    public void edit(NewExpr e) throws CannotCompileException {
        if (repalceWithEmpty(e)) {
            return;
        }
//        if (Config.newlyAddedClassNameList.contains(e.getClassName()) || Config.noNeedReflectClassSet.contains(e.getClassName())) {
//            return;
//        }


        boolean outerMethodIsStatic = isStatic(ctMethod.getModifiers());
        if (outerMethodIsStatic) {
            return;
            //外部方法是static，则没有this这个概念了
        }
//        else {
//            try {
//                replaceParamThisToOriginalClassInstance2(e);
//            } catch (NotFoundException e1) {
//                e1.printStackTrace();
//            }
//
//        }

        String newExprClassName = e.getClassName();

        int paramsCount = 0;
        {

            try {

                paramsCount = new RobustNewExprParamsCount().replace(e);
                System.err.println("RobustNewExprParamsCount: " + paramsCount);
            } catch (NotFoundException e1) {
                e1.printStackTrace();
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("{");
//            stringBuilder.append(NewExprParamsReplaceThisMethod(paramsCount));
//            stringBuilder.append("$_= new " + newExprClassName + "($args);");
            String params = "($$)";
            try {
                params = RobustNewExprParamsCount.getParamsString(e, patchClass.getName());
            } catch (NotFoundException e1) {
                e1.printStackTrace();
            }

//            stringBuilder.append("$_= ($r) new " + newExprClassName + "(this.originClass);");
            if (CheckCodeChanges.isAnonymousInnerClass_$1(newExprClassName)) {
                //create public Field outerPatchClassName
                CtClass anonymousInnerCtClass = Config.classPool.getOrNull(newExprClassName);
                CtField ctField = new CtField(patchClass, "outerPatchClassName", anonymousInnerCtClass);
                ctField.setModifiers(AccessFlag.PUBLIC);
                anonymousInnerCtClass.addField(ctField);
                //set field value
                stringBuilder.append(newExprClassName + " anonymousInnerClass = new " + newExprClassName + params + ";");
                stringBuilder.append("anonymousInnerClass.outerPatchClassName = this ;");
                stringBuilder.append("$_ = anonymousInnerClass; ");
                System.err.println("isAnonymousInnerClass_$1 :" + stringBuilder.toString());
            } else {
                stringBuilder.append("$_ = new " + newExprClassName + params + ";");
            }
            stringBuilder.append("};");

            e.replace(stringBuilder.toString());
            return;
        }


//        try {
//            CtClass newExpClass = Config.classPool.get(newExprClassName);
//            replaceParamThisToOriginalClassInstance2(e);
//        } catch (NotFoundException e1) {
//            e1.printStackTrace();
//        }


//        if (isAnonymousInnerClass_$1(newExprClassName)) {
//
//            //// TODO: 17/8/14 优化
//            try {
//                if (!ReflectUtils.isStatic(Config.classPool.get(e.getClassName()).getModifiers()) && JavaUtils.isInnerClassInModifiedClass(e.getClassName(), anonymousInnerClass)) {
//                    e.replace(ReflectUtils.getNewInnerClassString(e.getSignature(), outerClass.getName(), ReflectUtils.isStatic(Config.classPool.get(e.getClassName()).getModifiers()), e.getClassName()));
//                    return;
//                }
//            } catch (NotFoundException e1) {
//                e1.printStackTrace();
//            }
//
//            e.replace(ReflectUtils.getCreateClassString(e, e.getClassName(), outerClass.getName(), ReflectUtils.isStatic(ctMethod.getModifiers())));
//            return;
//        }


        //其他情况不用处理(// TODO: 17/8/3 需要将所有新增的class都设置成public的
        //需要处理 package访问属性的method,直接在插桩的时候改成public好了(同样的，把那个字段的也改了，这里就可以少很多代码了）
    }


    //        过滤robust proxy
    //com.meituan.robust.PatchProxy
        /*
        if (PatchProxy.isSupport(new Object[0], this, changeQuickRedirect, false, "4de6c62b640b9546c89a1540fbb998f1", new Class[0], Void.TYPE))
        {
            PatchProxy.accessDispatch(new Object[0], this, changeQuickRedirect, false, "4de6c62b640b9546c89a1540fbb998f1", new Class[0], Void.TYPE);return;
        }
        */
    private boolean isCallProxyAccessDispatchMethod(MethodCall methodCall) {
        if (methodCall.getMethodName().equals("accessDispatch")) {
            try {
                boolean isCallAccessDispatch = methodCall.getMethod().getLongName().startsWith("com.meituan.robust.PatchProxy.accessDispatch(");
                if (isCallAccessDispatch) {
                    return true;
                }
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void edit(MethodCall m) throws CannotCompileException {
        if (isCallProxyAccessDispatchMethod(m)) {
//            m.replace("$_ = ($r) new Object();");
            hasHandledProxyCode = true;
            return;
        }
        if (repalceWithEmpty(m)) {
            return;
        }

        if (m.getMethodName().contains("lambdaFactory")){
            //ignore // TODO: 17/8/26 because below
//            lambdaFactory$(..) is not found in com.meituan.sample.SecondActivity$$Lambda$2
            System.err.println("OutMethod : " + ctMethod.getName() + " , method call : " + m.getMethodName());
            return;
        }
        boolean outerMethodIsStatic = isStatic(ctMethod.getModifiers());
        int accessFlag = 0;
        try {
            accessFlag = m.getMethod().getModifiers();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        boolean methodClassIsStatic = isStatic(accessFlag);

        //如果是新增class的Method就不用替换了
        try {
            if (Config.newlyAddedClassNameList.contains(m.getMethod().getDeclaringClass().getName())) {
                //需要换一下参数this为originalClass
                if (outerMethodIsStatic) {
                    //没有this，不用处理
                    return;
                } else {
                    replaceParam_ThisToOriginalClassInstance(m);
                    return;
                }
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }

        //todo 如何区分super方法 MainActivity#super.oncreate()
        if (m.isSuper()) {
            /*
            protected void onCreate(@Nullable Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
            }
            */
            //放在assist类处理了 case : MainActivity#super.oncreate()
            System.err.println(m.getClassName() + "," + m.getMethodName() + ", is super: " + m.isSuper());
            m.replace(ReflectUtils.invokeSuperString(m));
            return;
        }

//        {
//            //是否需要反射处理 // TODO: 17/8/26  插桩的时候把package 、 protected方法全部改成了public方法 自己的private方法就访问自己的方法即可
//            //这里的package可以在插桩的时候将所有的protect & package （method + 构造函数 + field）都改成public
//            boolean isNeedReflected = AccessFlag.isProtected(accessFlag) || AccessFlag.isPrivate(accessFlag) || AccessFlag.isPackage(accessFlag);
//            if (isNeedReflected) {
//                //反射 // TODO: 17/8/13
//                m.replace(ReflectUtils.getMethodCallString(m, patchClass, outerMethodIsStatic));
//                return;
//            }
//        }

        if (outerMethodIsStatic) {
            /*
            protected void onCreate(@Nullable Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_robust_compat);
                Toast.makeText(this, "Hello onCreate TestPatchActivity", Toast.LENGTH_SHORT).show();
            }
            */

            return;
        } else {
            //非静态方法才有this，需要替换
            /*
            protected void onCreate(@Nullable Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_robust_compat);
                Toast.makeText(this, "Hello onCreate TestPatchActivity", Toast.LENGTH_SHORT).show();
            }
            */
            if (methodClassIsStatic) {
                try {
                    replaceParam_ThisToOriginalClassInstance(m);
                } catch (NotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    CtClass methodTargetClass = m.getMethod().getDeclaringClass();
//              System.err.println("is sub class of  " + methodTargetClass.getName() + ", " + sourceCla.getName());
                    if (sourceClass.getName().equals(methodTargetClass.getName())) {
                        replaceThisToOriginClassMethodDirectly(m);
                        return;
                    } else if (sourceClass.subclassOf(methodTargetClass) && !methodTargetClass.getName().contentEquals("java.lang.Object")) {
                        //// TODO: 17/8/7 判断是否父类方法 或者本类方法
                        //*** getClass , com.meituan.sample.SecondActivity is sub class Of : java.lang.Object
                        System.err.println("*** " + m.getMethod().getName() + " , " + sourceClass.getName() + " is sub class Of : " + methodTargetClass.getName());
                        //需要考虑一下protect方法（package方法全部在插桩的时候改掉）
                        replaceThisToOriginClassMethodDirectly(m);
                        return;
                    } else {
                        //do noting
                        return;
                    }
                } catch (NotFoundException e) {
                    e.printStackTrace();
                    System.err.println("error: " + m.getClassName() + "," + m.getClass().getName() + ", ");
                }
            }
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
//                        if (ctClass.getName().startsWith(anonymousInnerClass.getName())) {
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
//                                    m.replace(ReflectUtils.getNewInnerClassString(m.getSignature(), outerClass.getName(), ReflectUtils.isStatic(method.getModifiers()), /*getClassValue(*/m.getClassName()));
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
//                                        m.replace(ReflectUtils.getMethodCallString(m, memberMappingInfo, outerClass, ReflectUtils.isStatic(method.getModifiers()), false));
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

    public void replaceParam_ThisToOriginalClassInstance(MethodCall m) throws NotFoundException, CannotCompileException {
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
                    System.err.println("getParamsThisReplacedString2 : " + ctClass.getName() + ":" + sourceClass.getName());
                    if (sourceClass.subclassOf(ctClass)) {
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
        if (m.getSignature().contains("isGrantSDCardReadPermission")) {
            System.err.println("isGrantSDCardReadPermission");
        }
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
                    if (sourceClass.subclassOf(ctClass) && !ctClass.getName().contains("java.lang.Object")) {
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
