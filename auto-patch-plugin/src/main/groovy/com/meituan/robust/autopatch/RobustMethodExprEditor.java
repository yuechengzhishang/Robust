package com.meituan.robust.autopatch;

import com.meituan.robust.Constants;
import com.meituan.robust.change.RobustChangeInfo;
import com.meituan.robust.utils.RobustLogUtils;

import java.util.ArrayList;
import java.util.List;

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
        this.hasRobustProxyCode = HasRobustProxyUtils.hasRobustProxy(sourceClass, patchClass, ctMethod);
    }

    @Override
    public void edit(FieldAccess f) throws CannotCompileException {
        boolean isThis$0 = false;
        try {
            CtClass outerCtClass = sourceClass.getDeclaringClass();
            String outerClassName = outerCtClass.getName();
            if (outerClassName.equals(f.getField().getType().getName())) {
                //this$0.publicField
                isThis$0 = true;
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        if (false == isThis$0) {
            if (hasRobustProxyCode) {
                if (repalceWithEmpty(f)) {
                    return;
                }
            }
        }

        if (Config.newlyAddedClassNameList.contains(f.getClassName())) {
            return;
        }

        try {
            //TODO: 17/8/29 尽量不使用反射
            //todo 根据field的访问类型
            if (f.isReader()) {
                //reader done
                f.replace(ReflectUtils.getFieldString2(f.getField(), patchClass.getName(), sourceClass.getName()));
            } else if (f.isWriter()) {
                f.replace(ReflectUtils.setFieldString2(f.getField(), patchClass.getName(), sourceClass.getName()));
            }
        } catch (NotFoundException e) {
            RobustLogUtils.log("Field access replace NotFoundException", e);
//            throw new RuntimeException(e.getMessage());
        } catch (javassist.CannotCompileException e) {
            RobustLogUtils.log("Field access replace NotFoundException", e);
        }
    }

    @Override
    public void edit(NewArray a) throws CannotCompileException {
        if (hasRobustProxyCode) {
            if (repalceWithEmpty(a)) {
                return;
            }
        }
    }

    @Override
    public void edit(NewExpr e) throws CannotCompileException {
        if (hasRobustProxyCode) {
            if (repalceWithEmpty(e)) {
                return;
            }
        }
//        if (Config.newlyAddedClassNameList.contains(e.getClassName()) || Config.noNeedReflectClassSet.contains(e.getClassName())) {
//            return;
//        }


        boolean outerMethodIsStatic = isStatic(ctMethod.getModifiers());
        if (outerMethodIsStatic) {
            return;
            //外部方法是static，则没有this这个概念了
        }

        String newExprClassName = e.getClassName();

        int paramsCount = 0;
        {

            try {

                paramsCount = new RobustNewExprParamsCount().replace(e);
//                System.err.println("RobustNewExprParamsCount: " + paramsCount);
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


    //    public String staticMethodCall (MethodCall methodCall) throws NotFoundException {
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("{");
//        String signatureBuilder = getParameterClassString(methodCall.getMethod().getParameterTypes());
//        stringBuilder.append(methodCall.getMethod().getDeclaringClass().getName() + " instance;");
//        if (signatureBuilder.toString().length() > 1) {
//            String state = "$_=($r)" +
//                    Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectStaticMethod(" +"\"" + methodCall.getMethod().getName() + "\"" + "," + sourceClass.getName() + ".class,$args,null);";
//            stringBuilder.append("$_=($r) " + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectStaticMethod(\"" + methodCall.getMethod() + "\"," + methodCall.method.declaringClass.name + ".class,\$args,new Class[]{" + signatureBuilder.toString() + "});");
//        } else
//            stringBuilder.append("$_=($r)" + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectStaticMethod(\"" + methodCall.getMethod() + "\"," + methodCall.method.declaringClass.name + ".class,\$args,null);");
//
//    stringBuilder.append("}");
//    return "";
//    }
    @Override
    public void edit(MethodCall m) throws CannotCompileException {
        if (hasRobustProxyCode) {
            if (isCallProxyAccessDispatchMethod(m)) {
//            m.replace("$_ = ($r) new Object();");
                hasHandledProxyCode = true;
                return;
            }
            if (repalceWithEmpty(m)) {
                return;
            }
        }

//        if (ctMethod.getName().contains("lambda$onCreate$3")){
//            System.err.println("lambda$onCreate$3 : "+ctMethod.getLongName());
//        }
        boolean outerMethodIsStatic = isStatic(ctMethod.getModifiers());

        if (outerMethodIsStatic == false && m.getMethodName().contains("lambdaFactory")) {
            //ignore // TODO: 17/8/26 because below
//            lambdaFactory$(..) is not found in com.meituan.sample.SecondActivity$$Lambda$2
//            System.err.println("OutMethod : " + ctMethod.getName() + " , method call : " + m.getMethodName());
//            m.getMethod() : lambdaFactory$(..) is not found in com.meituan.sample.SecondActivity$$Lambda$2
            try {

//                getParamsThisReplacedString(m);
//                replaceParam_ThisToOriginalClassInstance(m);
//                String params = RobustMethodCallEditorUtils2.replace_$args_to_this_origin_class(ctMethod,m,patchClass,sourceClass);
//                StringBuilder stringBuilder = new StringBuilder();
//                stringBuilder.append("{");
//                stringBuilder.append("$_ = $proceed" +params+ ";");
//                stringBuilder.append("}");
//                m.replace(stringBuilder.toString());
                RobustMethodCallEditorUtils2.handleLambdaFactory(ctMethod, m, patchClass, sourceClass);

            } catch (NotFoundException e) {
                e.printStackTrace();
            }
            return;
        }
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
//            System.err.println(m.getClassName() + "," + m.getMethodName() + ", is super: " + m.isSuper());
            m.replace(ReflectUtils.invokeSuperString(m));
            return;
        }
        CtMethod callCtMethod = null;
        try {
            callCtMethod = m.getMethod();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        boolean callMethodIsStatic = isStatic(callCtMethod.getModifiers());
        //callMethodIsStatic
        //outerMethodIsStatic


        if (outerMethodIsStatic && callMethodIsStatic) {
            //外部静态方法 call 静态方法
            try {
                RobustMethodCallEditorUtils.staticMethodCallStaticMethod(ctMethod, m, patchClass, sourceClass);
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
            return;
        }

        if (!outerMethodIsStatic && callMethodIsStatic) {
            try {
                if (AccessFlag.isPublic(m.getMethod().getModifiers())) {

                } else {
                    if (RobustChangeInfo.isInvariantMethod(m.getMethod())) {
                        if (RobustMethodCallEditorUtils2.isThisClassOrSubclass(m.getMethod().getDeclaringClass(), patchClass, sourceClass)) {
                            String statement = ReflectUtils.getMethodCallString(m, patchClass, false);
                            m.replace(statement);
                        }
                    }
                }

            } catch (NotFoundException e) {

            }

        }

//        if (!outerMethodIsStatic && callMethodIsStatic) {
//                //外部非静态方法 call 静态方法
//            try {
//                RobustMethodCallEditorUtils2.nonstaticMethodCallStaticMethod(ctMethod, m, patchClass, sourceClass);
//            } catch (NotFoundException e) {
//                e.printStackTrace();
//            }
//            return;
//        }

//        try {
//            if (outerMethodIsStatic && callMethodIsStatic) {
//                //外部静态方法 call 静态方法
//                RobustMethodCallEditorUtils.staticMethodCallStaticMethod(ctMethod, m, patchClass, sourceClass);
//                return;
//            } else if (outerMethodIsStatic && !callMethodIsStatic) {
//                //外部静态方法 call 非静态方法
//                RobustMethodCallEditorUtils4.staticMethod_Call_nonStaticMethod(ctMethod, m, patchClass, sourceClass);
//                return;
//            } else if (!outerMethodIsStatic && callMethodIsStatic) {
//                //外部非静态方法 call 静态方法
//                RobustMethodCallEditorUtils2.nonstaticMethodCallStaticMethod(ctMethod, m, patchClass, sourceClass);
//                return;
//            } else if (!outerMethodIsStatic && !callMethodIsStatic) {
//                //外部非静态方法 call 非静态方法
//                RobustMethodCallEditorUtils3.nonstaticMethod_Call_nonStaticMethod(ctMethod, m, patchClass, sourceClass);
//                return;
//            }
//        } catch (NotFoundException e) {
//            e.printStackTrace();
//            throw new RuntimeException(e);
//        }

        try {
            if (callMethodIsStatic) {
                CtClass methodTargetClass = m.getMethod().getDeclaringClass();
                if (patchClass.getName().equals(methodTargetClass.getName())) {
                    if (RobustChangeInfo.isInvariantMethod(callCtMethod)) {
                        if (AccessFlag.isPublic(callCtMethod.getModifiers())) {
                            //need to reflect static method
                            String statement = "$_=($r)" + sourceClass.getName() + "." + m.getMethod().getName() + "($$);";
                            try {
                                m.replace(statement);
                            } catch (javassist.CannotCompileException e) {
                                m.replace(getMethodCallString_this_static_method_call(m, patchClass, outerMethodIsStatic, sourceClass.getName()));
                            }
                            return;
                        } else {
                            m.replace(getMethodCallString_this_static_method_call(m, patchClass, outerMethodIsStatic, sourceClass.getName()));
                            return;
                        }
                    } else {
                        //do nothing
                        return;
                    }
                }
            } else {
                //call method is not non-static
//                TestPatchActivity#onCreate({private String hello()}
//                CtClass methodTargetClass = m.getMethod().getDeclaringClass();
//                if (patchClass.getName().equals(methodTargetClass.getName())) {
//                    if (RobustChangeInfo.isInvariantMethod(callCtMethod)) {
//                        if (AccessFlag.isPublic(callCtMethod.getModifiers())) {
//                            //need to reflect static method
//                            String statement = "$_=($r)" + sourceClass.getName() + "." + m.getMethod().getName() + "($$);";
//                            try {
//                                m.replace(statement);
//                            } catch (javassist.CannotCompileException e){
//                                m.replace(getMethodCallString_this_static_method_call(m,patchClass,outerMethodIsStatic,sourceClass.getName()));
//                            }
//                            return;
//                        } else {
////                            String statement = "$_=($r)" +
////                             Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectStaticMethod(" +"\"" + callCtMethod.getName() + "\"" + "," + sourceClass.getName() + ".class,$args,null);";
////                            m.replace(statement);
//                            m.replace(getMethodCallString_this_static_method_call(m,patchClass,outerMethodIsStatic,sourceClass.getName()));
//                            return;
//                        }
//                    } else {
//                        //do nothing
//                        return;
//                    }
//                }
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }

        if (outerMethodIsStatic) {
            /*
            protected void onCreate(@Nullable Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_robust_compat);
                Toast.makeText(this, "Hello onCreate TestPatchActivity", Toast.LENGTH_SHORT).show();
            }
            */
            //// TODO: 17/9/6
            try {
                CtClass methodTargetClass = m.getMethod().getDeclaringClass();
                if (patchClass.getName().equals(methodTargetClass.getName())) {
                    if (RobustChangeInfo.isInvariantMethod(callCtMethod)) {
                        if (AccessFlag.isPublic(callCtMethod.getModifiers())) {
                            //need to reflect static method
                            String statement = "$_=($r)" + sourceClass.getName() + "." + m.getMethod().getName() + "($$);";
                            try {
                                m.replace(statement);
                            } catch (javassist.CannotCompileException e) {
                                m.replace(getMethodCallString_this_static_method_call(m, patchClass, outerMethodIsStatic, sourceClass.getName()));
                            }
                            return;
                        } else {
                            m.replace(getMethodCallString_this_static_method_call(m, patchClass, outerMethodIsStatic, sourceClass.getName()));
                            return;
                        }
                    } else {
                        //do nothing
                        return;
                    }
                }
            } catch (Exception e){
                RobustLogUtils.log("Exception 480 ",e);
            }


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
                    if (sourceClass.getName().equals(methodTargetClass.getName()) || patchClass.getName().equals(methodTargetClass.getName())) {
                        replaceThisToOriginClassMethodDirectly(m);
                        return;
                    } else if (sourceClass.subclassOf(methodTargetClass) && !methodTargetClass.getName().contentEquals("java.lang.Object")) {
                        //// TODO: 17/8/7 判断是否父类方法 或者本类方法
                        //*** getClass , com.meituan.sample.SecondActivity is sub class Of : java.lang.Object
//                        System.err.println("*** " + m.getMethod().getName() + " , " + sourceClass.getName() + " is sub class Of : " + methodTargetClass.getName());
                        //需要考虑一下protect方法（package方法全部在插桩的时候改掉）
                        replaceThisToOriginClassMethodDirectly(m);
                        return;
                    } else {
                        //do noting // TODO: 17/9/6
                        boolean isOuterMethod = false;
                        try {
                            CtClass outerCtClass = sourceClass.getDeclaringClass();
                            String outerClassName = outerCtClass.getName();
                            if (m.getMethodName().contains("hello")){
                                System.err.println("hello");
                            }
                            if (outerClassName.equals(m.getMethod().getDeclaringClass().getName())) {
                                //this$0.publicField
                                isOuterMethod = true;
                            }
                        } catch (Exception e) {
                            RobustLogUtils.log("Exception", e);
                        }

                        if (isOuterMethod) {
                            replaceThisToOriginClassMethodDirectly(m);
                        }
                        return;
                    }
                } catch (NotFoundException e) {
                    e.printStackTrace();
                    System.err.println("error: " + m.getClassName() + "," + m.getClass().getName() + ", ");
                }
            }
        }

        // 大部分情况下是需要替换this为originalClass的
        // TODO: 17/8/7 处理builder 除了匿名内部类，都把this换成originalClass

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

        //repalceInlineMethod(m, method, false)
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


    public void replaceParam_This_lambda(MethodCall m) throws NotFoundException, CannotCompileException {
        if (m.getMethod().getParameterTypes().length == 0) {
            return;
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("{");
            stringBuilder.append(getParamsThisReplacedString(m));
            stringBuilder.append("$_ = ($r) " + m.getMethod().getDeclaringClass() + "." + m.getMethod().getName());
            stringBuilder.append("(");
            List<String> paramList = new ArrayList<String>();
            for (int index = 1; index <= m.getMethod().getParameterTypes().length; index++) {
                paramList.add("$" + index);
            }
            stringBuilder.append(String.join(",", paramList));
            stringBuilder.append(")");
            stringBuilder.append("}");

//            stringBuilder.append(getParamsThisReplacedString(m));
//            stringBuilder.append("$_ = $proceed($args);");
            m.replace(stringBuilder.toString());

//            m.replace(ReflectUtils.getNewInnerClassString(m.getSignature(), temPatchClass.getName(), ReflectUtils.isStatic(method.getModifiers()), m.getClassName()));
            return;
        }
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


    public static String getMethodCallString_this_static_method_call(MethodCall methodCall, CtClass patchClass, boolean isInStaticMethod, String sourceClassName) throws NotFoundException {
        String signatureBuilder = getParameterClassString(methodCall.getMethod().getParameterTypes());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        if (isStatic(methodCall.getMethod().getModifiers())) {
            if (isInStaticMethod) {
                //在static method使用static method
                if (AccessFlag.isPublic(methodCall.getMethod().getModifiers())) {
                    stringBuilder.append("$_ = $proceed($$);");
                } else {
                    if (signatureBuilder.toString().length() > 1) {
                        stringBuilder.append("$_=($r) " + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectStaticMethod(\"" + methodCall.getMethod().getName() + "\"," + sourceClassName + ".class,$args,new Class[]{" + signatureBuilder.toString() + "});");
                    } else
                        stringBuilder.append("$_=($r)" + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectStaticMethod(\"" + methodCall.getMethod().getName() + "\"," + sourceClassName + ".class,$args,null);");
                }
                if (Constants.isLogging) {
                    stringBuilder.append("  android.util.Log.d(\"robust\",\"invoke static  method is      ${getCoutNumber()}  \" +\"" + methodCall.getMethodName() + "\");");
                }
            } else {
                //在非static method中使用static method
                stringBuilder.append("java.lang.Object parameters[]=" + Constants.GET_REAL_PARAMETER + "($args);");
                if (signatureBuilder.toString().length() > 1) {
                    stringBuilder.append("$_=($r) " + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectStaticMethod(\"" + methodCall.getMethod().getName() + "\"," + sourceClassName + ".class,parameters,new Class[]{" + signatureBuilder.toString() + "});");
                } else
                    stringBuilder.append("$_=($r)" + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectStaticMethod(\"" + methodCall.getMethod().getName() + "\"," + sourceClassName + ".class,parameters,null);");
            }

        } else {

            String methodTargetClassName = methodCall.getMethod().getDeclaringClass().getName();
            if (patchClass.getName().equals(methodTargetClassName)){
                methodTargetClassName = sourceClassName;
            }

            stringBuilder.append("java.lang.Object " + " instance;");
            if (!isInStaticMethod) {
                //在非static method中使用非static method
                stringBuilder.append(" if($0 == this ){");
                stringBuilder.append("instance=((" + patchClass.getName() + ")$0)." + Constants.ORIGINCLASS + ";");
                stringBuilder.append("}else{");
                stringBuilder.append("instance=$0;");
                stringBuilder.append("}");
                if (signatureBuilder.toString().length() > 1) {
                    stringBuilder.append("java.lang.Object parameters[]=" + Constants.GET_REAL_PARAMETER + "($args);");
                    stringBuilder.append("$_=($r) " + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectMethod(\"" + methodCall.getMethodName() + "\",instance,parameters,new Class[]{" + signatureBuilder.toString() + "}," + methodTargetClassName + ".class);");
                } else
                    stringBuilder.append("$_=($r)" + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectMethod(\"" + methodCall.getMethodName() + "\",instance,$args,null," + methodTargetClassName + ".class);");
                if (Constants.isLogging) {
                    stringBuilder.append("  android.util.Log.d(\"robust\",\"invoke  method is      ${getCoutNumber()} \" +\"" + methodCall.getMethodName() + "\");");
                }
            } else {
                stringBuilder.append("instance = (" + methodCall.getMethod().getDeclaringClass().getName() + ")$0;");
                stringBuilder.append(" if($0 instanceof " + patchClass.getName() + "){");
                stringBuilder.append("instance = ((" + patchClass.getName() + ")$0)." + Constants.ORIGINCLASS + ";");
                stringBuilder.append("} ");
                //在static method中使用非static method
                //// TODO: 17/9/7 参数传错了
//                public static void access$100(TestPatchActivityPatch x0, String x1) {
//                    TestPatchActivityPatch var5 = (TestPatchActivityPatch)x0;
//                    EnhancedRobustUtils.invokeReflectMethod("setPrivateString", var5, new Object[]{x1}, new Class[]{String.class}, TestPatchActivity.class);
//                }
//               todo 考虑使用这个 RobustMethodCallEditorUtils2.replace_$args_to_this_origin_class();
                if (signatureBuilder.toString().length() > 1) {
                    //// TODO: 17/9/7  parameters
                    String paramsStr = RobustMethodCallEditorUtils2.replace_$args_to_this_origin_class(null,methodCall,patchClass,Config.classPool.get(sourceClassName));
                    paramsStr = paramsStr.replace("(","").replace(")","");
                    stringBuilder.append("java.lang.Object parameters[]= new Object[]{"+paramsStr+"};");
                    stringBuilder.append("$_=($r) " + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectMethod(\"" + methodCall.getMethodName() + "\",instance,parameters,new Class[]{" + signatureBuilder.toString() + "},"+methodTargetClassName+".class);");
                } else
                    stringBuilder.append("$_=($r)" + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectMethod(\"" + methodCall.getMethodName() + "\",instance,$args,null," + methodTargetClassName + ".class);");

            }
        }
//        }
        stringBuilder.append("}");
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
