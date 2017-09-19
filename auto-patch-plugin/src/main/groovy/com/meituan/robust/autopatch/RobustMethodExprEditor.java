package com.meituan.robust.autopatch;

import com.meituan.robust.ChangeQuickRedirect;
import com.meituan.robust.Constants;
import com.meituan.robust.PatchProxy;
import com.meituan.robust.change.RobustChangeInfo;
import com.meituan.robust.utils.AnonymousLambdaUtils;
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
            if (null == outerClassName || "".equals(outerClassName)) {

            } else {
                if (outerClassName.equals(f.getField().getType().getName())) {
                    //this$0.publicField
                    isThis$0 = true;
                }
            }
        } catch (NotFoundException e) {
//            e.printStackTrace();
            RobustLog.log("NotFoundException e", e);
        } catch (NullPointerException e) {
//            RobustLog.log("NullPointerException e",e);
        }
        if (false == isThis$0) {
            if (hasRobustProxyCode) {
                if (isNeedReplaceEmpty()) {
                    return;
                }
            }
        }

        if (Config.newlyAddedClassNameList.contains(f.getClassName())) {
            return;
        }

        if (f.isReader()){
            if (f.getSignature().contains(ChangeQuickRedirect.class.getCanonicalName().replace(".","/"))){// is quickchangeredirect //
                return;
            }
        }
        String replaceStatment = null;
        try {
            //根据field的访问类型
            if (f.isReader()) {
                //reader done
                replaceStatment = ReflectUtils.getFieldString2(f.getField(), patchClass.getName(), sourceClass.getName());
                f.replace(replaceStatment);
            } else if (f.isWriter()) {
                replaceStatment = ReflectUtils.setFieldString2(f.getField(), patchClass.getName(), sourceClass.getName());
                f.replace(replaceStatment);
            }
        } catch (NotFoundException e) {
            RobustLog.log("Field access replace NotFoundException : " + replaceStatment , e);
//            throw new RuntimeException(e.getMessage());
        } catch (javassist.CannotCompileException e) {
            if (e.getMessage().contains("no such field:")) {

            } else {
                RobustLog.log("Field access replace NotFoundException : " + replaceStatment, e);
            }
        } catch (Exception e){
            RobustLog.log("Exception : " + replaceStatment,e);
        }
    }

    @Override
    public void edit(NewArray a) throws CannotCompileException {
        if (hasRobustProxyCode) {
            if (isNeedReplaceEmpty()) {
                return;
            }
        }
    }

    @Override
    public void edit(NewExpr e) throws CannotCompileException {
        if (hasRobustProxyCode) {
            if (isNeedReplaceEmpty()) {
                return;
            }
        }

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
            boolean isChangeOrNewAdd_AnonymousInnerClass_$1 = Config.modifiedAnonymousClassNameList.contains(newExprClassName);
            if (isChangeOrNewAdd_AnonymousInnerClass_$1 && AnonymousLambdaUtils.isAnonymousInnerClass_$1(newExprClassName)) {
                //create public Field outerPatchClassName
                CtClass anonymousInnerCtClass = Config.classPool.getOrNull(newExprClassName);
                CtField ctField = new CtField(patchClass, "outerPatchClassName", anonymousInnerCtClass);
                ctField.setModifiers(AccessFlag.PUBLIC);
                anonymousInnerCtClass.addField(ctField);
                //set field value
                stringBuilder.append(newExprClassName + " anonymousInnerClass = new " + newExprClassName + params + ";");
                stringBuilder.append("anonymousInnerClass.outerPatchClassName = this ;");
                stringBuilder.append("$_ = anonymousInnerClass; ");
                com.meituan.robust.utils.RobustLog.log("isAnonymousInnerClass_$1 :" + stringBuilder.toString());
            } else {
                stringBuilder.append("$_ = new " + newExprClassName + params + ";");
            }
            stringBuilder.append("};");

            e.replace(stringBuilder.toString());
            return;
        }

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
            if (isPatchProxyClass(methodCall)){
                return true;
            } else {
                try {
                    boolean isCallAccessDispatch = methodCall.getMethod().getLongName().startsWith("com.meituan.robust.PatchProxy.accessDispatch(");
                    if (isCallAccessDispatch) {
                        return true;
                    }
                } catch (NotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private boolean isCallProxyisSupportMethod(MethodCall methodCall) {
        if (methodCall.getMethodName().equals("isSupport")) {
            if (isPatchProxyClass(methodCall)){
                return true;
            } else {
                try {
                    boolean isCallAccessDispatch = methodCall.getMethod().getLongName().startsWith("com.meituan.robust.PatchProxy.accessDispatch(");
                    if (isCallAccessDispatch) {
                        return true;
                    }
                } catch (NotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public static boolean isPatchProxyClass(MethodCall m){
        String PatchProxyClassName = PatchProxy.class.getCanonicalName();
        String callMethodOnClassName = m.getClassName();
        if (PatchProxyClassName.equals(callMethodOnClassName)){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void edit(MethodCall m) throws CannotCompileException {
        if (hasRobustProxyCode) {
            if (isCallProxyAccessDispatchMethod(m)) {
                hasHandledProxyCode = true;
                return;
            }
            if (isNeedReplaceEmpty()) {
                if (isCallProxyisSupportMethod(m)){
                    m.replace("$_ = false ;");
                }
                return;
            }
        }

        if (isCallProxyisSupportMethod(m)){
            m.replace("$_ = false ;");
            return;
        }
        if (isCallProxyAccessDispatchMethod(m)){
            return;
        }

        boolean outerMethodIsStatic = isStatic(ctMethod.getModifiers());

//  com.meituan.sample.TestPatchActivity$$Lambda$1 -> com.meituan.sample.s:
//        com.meituan.robust.ChangeQuickRedirect changeQuickRedirect -> a
//        com.meituan.sample.TestPatchActivity arg$1 -> b
//        void <init>(com.meituan.sample.TestPatchActivity) -> <init>
//        void onClick(android.view.View) -> onClick
//        android.view.View$OnClickListener lambdaFactory$(com.meituan.sample.TestPatchActivity) -> a

        //m.getMethodName().contains("lambdaFactory$")
        if (outerMethodIsStatic == false && ProguardUtils.isLambdaFactoryMethod(sourceClass.getName(), patchClass.getName(), m.getClassName(), m.getMethodName(), m.getSignature())) {
//            lambdaFactory$(..) is not found in com.meituan.sample.SecondActivity$$Lambda$2
            try {
                RobustMethodCallEditorUtils2.handleLambdaFactory(ctMethod, m, patchClass, sourceClass);
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
            return;
        }

        if (ProguardUtils.isAccess$Method(m)) {
            //在RobustHandleAccessMethodExpr处理了
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
        if (null == callCtMethod) {
            RobustLog.log("callCtMethod is null ");
        }
        boolean callMethodIsStatic = isStatic(callCtMethod.getModifiers());
        //callMethodIsStatic
        //outerMethodIsStatic


        if (ProguardUtils.isLambda$Method(m)) {
            try {
                if (RobustChangeInfo.isChangedMethod(m.getMethod())) {
                    return;
                }
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }

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


        if (callMethodIsStatic) {
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
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }


        if (outerMethodIsStatic) {

            try {
                CtClass methodTargetClass = m.getMethod().getDeclaringClass();
                if (patchClass.getName().equals(methodTargetClass.getName())) {
                    if (RobustChangeInfo.isNewAddMethod(callCtMethod)) {
                        //do nothing
                        return;
                    } else {
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
                    }
                }
            } catch (Exception e) {
                RobustLog.log("Exception 480 ", e);
            }


            return;
        }
        if (!outerMethodIsStatic) {
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
                return;
            } else {
                /*
            protected void onCreate(@Nullable Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_robust_compat);
                Toast.makeText(this, "Hello onCreate TestPatchActivity", Toast.LENGTH_SHORT).show();
            }
            */
                try {
                    CtClass methodTargetClass = m.getMethod().getDeclaringClass();
//              System.err.println("is sub class of  " + methodTargetClass.getName() + ", " + sourceCla.getName());
                        if (sourceClass.getName().equals(methodTargetClass.getName()) || patchClass.getName().equals(methodTargetClass.getName())) {
                            if (RobustChangeInfo.isInvariantMethod(m.getMethod())) {
                                replaceThisToOriginClassMethodDirectly_nonstatic_nonstatic(m);
                            }
                            return;
                        } else if (sourceClass.subclassOf(methodTargetClass) && !methodTargetClass.getName().contentEquals("java.lang.Object")) {
                            //*** getClass , com.meituan.sample.SecondActivity is sub class Of : java.lang.Object
//                        System.err.println("*** " + m.getMethod().getName() + " , " + sourceClass.getName() + " is sub class Of : " + methodTargetClass.getName());
                            replaceThisToOriginClassMethodDirectly_nonstatic_nonstatic(m);
                            return;
                        } else {
                            boolean isOuterMethod = false;
                            try {
                                CtClass outerCtClass = sourceClass.getDeclaringClass();
                                if (null != outerCtClass) {
                                    String outerClassName = outerCtClass.getName();
                                    if (null == outerClassName || "".equals(outerClassName)) {

                                    } else {
                                        if (outerClassName.equals(m.getMethod().getDeclaringClass().getName())) {
                                            //this$0.publicField
                                            isOuterMethod = true;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                RobustLog.log("Exception", e);
                            }

                            if (isOuterMethod) {
                                replaceThisToOriginClassMethodDirectlyByCallOuterMethod(m);
                            }
                            return;
                        }

                } catch (NotFoundException e) {
                    e.printStackTrace();
                    com.meituan.robust.utils.RobustLog.log("error: " + m.getClassName() + "," + m.getClass().getName() + ", ");
                }
            }
        }

        // 大部分情况下是需要替换this为originalClass的

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

    }

    static boolean isStatic(int modifiers) {
        return (modifiers & AccessFlag.STATIC) != 0;
    }


    public void replaceThisToOriginClassMethodDirectly_nonstatic_nonstatic(MethodCall m) throws NotFoundException, CannotCompileException {
        if (RobustChangeInfo.isNewAddMethod(m.getMethod())) {
            //新方法不用处理，老方法需要走下面的逻辑
            return;
        }
        int accessFlag = m.getMethod().getModifiers();
        if (AccessFlag.isProtected(accessFlag) || AccessFlag.isPrivate(accessFlag) || AccessFlag.isPackage(accessFlag)) {
            MethodCall methodCall = m;
            String methodParamSignature = getParameterClassString(methodCall.getMethod().getParameterTypes());
            methodParamSignature = methodParamSignature.replaceAll(patchClass.getName(), sourceClass.getName());
            StringBuilder stringBuilder = new StringBuilder();
            String methodTargetClassName = methodCall.getMethod().getDeclaringClass().getName();
            if (patchClass.getName().equals(methodTargetClassName)) {
                methodTargetClassName = sourceClass.getName();
            }
            stringBuilder.append("{");
            stringBuilder.append("java.lang.Object " + " instance;");
            stringBuilder.append("instance = ((" + patchClass.getName() + ")$0)." + Constants.ORIGINCLASS + ";");
            if (methodParamSignature.toString().length() > 0) {
                String paramsStr = RobustMethodCallEditorUtils2.replace_$args_to_this_origin_class(null, methodCall, patchClass, sourceClass);
                paramsStr = paramsStr.replace("(", "").replace(")", "");
                stringBuilder.append("java.lang.Object parameters[]= new Object[]{" + paramsStr + "};");
                stringBuilder.append("$_=($r) " + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectMethod(\"" + methodCall.getMethodName() + "\",instance,parameters,new Class[]{" + methodParamSignature.toString() + "}," + methodTargetClassName + ".class);");
            } else
                stringBuilder.append("$_=($r)" + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectMethod(\"" + methodCall.getMethodName() + "\",instance,$args,null," + methodTargetClassName + ".class);");

            stringBuilder.append("}");
            m.replace(stringBuilder.toString());
            return;
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("{");
            stringBuilder.append(getParamsThisReplacedString(m));
            stringBuilder.append("$_=($r)this." + ORIGINCLASS + "." + m.getMethod().getName() + "($$);");
            stringBuilder.append("}");
            m.replace(stringBuilder.toString());
        }
    }

    public void replaceThisToOriginClassMethodDirectlyByCallOuterMethod(MethodCall m) throws NotFoundException, CannotCompileException {
        int accessFlag = m.getMethod().getModifiers();
        if (AccessFlag.isProtected(accessFlag) || AccessFlag.isPrivate(accessFlag) || AccessFlag.isPackage(accessFlag)) {
            //反射
            com.meituan.robust.utils.RobustLog.log("replaceThisToOriginClassMethodDirectlyByCallOuterMethod :" + m.getMethod().getLongName());
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("{");
            stringBuilder.append(getParamsThisReplacedString(m));
            stringBuilder.append("$_=($r)this." + ORIGINCLASS + "." + m.getMethod().getName() + "($$);");
            stringBuilder.append("}");
            try {
                m.replace(stringBuilder.toString());
            } catch (Throwable e) {
                RobustLog.log("replace error", e);
            }

        }
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
        com.meituan.robust.utils.RobustLog.log("replaceParamThisToOriginalClassInstance2 :" + m.getClassName());
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
                    com.meituan.robust.utils.RobustLog.log("getParamsThisReplacedString2 : " + ctClass.getName() + ":" + sourceClass.getName());
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
                    if (signatureBuilder.toString().length() > 0) {
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
                if (signatureBuilder.toString().length() > 0) {
                    stringBuilder.append("$_=($r) " + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectStaticMethod(\"" + methodCall.getMethod().getName() + "\"," + sourceClassName + ".class,parameters,new Class[]{" + signatureBuilder.toString() + "});");
                } else
                    stringBuilder.append("$_=($r)" + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectStaticMethod(\"" + methodCall.getMethod().getName() + "\"," + sourceClassName + ".class,parameters,null);");
            }

        } else {

            String methodTargetClassName = methodCall.getMethod().getDeclaringClass().getName();
            if (patchClass.getName().equals(methodTargetClassName)) {
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
                if (signatureBuilder.toString().length() > 0) {
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
//                public static void access$100(TestPatchActivityPatch x0, String x1) {
//                    TestPatchActivityPatch var5 = (TestPatchActivityPatch)x0;
//                    EnhancedRobustUtils.invokeReflectMethod("setPrivateString", var5, new Object[]{x1}, new Class[]{String.class}, TestPatchActivity.class);
//                }
//              考虑使用这个 RobustMethodCallEditorUtils2.replace_$args_to_this_origin_class();
                if (signatureBuilder.toString().length() > 0) {
                    String paramsStr = RobustMethodCallEditorUtils2.replace_$args_to_this_origin_class(null, methodCall, patchClass, Config.classPool.get(sourceClassName));
                    paramsStr = paramsStr.replace("(", "").replace(")", "");
                    stringBuilder.append("java.lang.Object parameters[]= new Object[]{" + paramsStr + "};");
                    stringBuilder.append("$_=($r) " + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectMethod(\"" + methodCall.getMethodName() + "\",instance,parameters,new Class[]{" + signatureBuilder.toString() + "}," + methodTargetClassName + ".class);");
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
