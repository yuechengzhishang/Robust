package com.meituan.robust.autopatch;

import com.meituan.robust.Constants;
import com.meituan.robust.change.RobustChangeInfo;

import java.util.ArrayList;
import java.util.List;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.expr.MethodCall;

import static com.meituan.robust.Constants.ORIGINCLASS;
import static com.meituan.robust.autopatch.RobustMethodExprEditor.getMethodCallString_this_static_method_call;

/**
 * Created by hedingxu on 17/8/31.
 */

public class RobustMethodCallEditorUtils2 {

    public static void nonstaticMethodCallStaticMethod(CtMethod ctMethod, MethodCall m, CtClass patchClass, CtClass sourceClass) throws NotFoundException, CannotCompileException {

        CtMethod callCtMethod = m.getMethod();
        boolean outerMethodIsStatic = false;
        CtClass methodTargetClass = callCtMethod.getDeclaringClass();
        if (patchClass.getName().equals(methodTargetClass.getName())) {
            if (RobustChangeInfo.isInvariantMethod(callCtMethod)) {
                if (AccessFlag.isPublic(callCtMethod.getModifiers())) {
                    //need to reflect static method
                    //如果参数里面有this，需要处理一下 // TODO: 17/8/31
//                    if (){
//
//                    }
                    String statement = "$_=($r)" + sourceClass.getName() + "." + m.getMethod().getName() + "($$);";
                    try {
                        m.replace(statement);
                    } catch (CannotCompileException e) {
                        m.replace(getMethodCallString_this_static_method_call(m, patchClass, outerMethodIsStatic, sourceClass.getName()));
                    }
                    return;
                } else {
                    m.replace(getMethodCallString_this_static_method_call(m, patchClass, outerMethodIsStatic, sourceClass.getName()));
                    return;
                }
            } else {
                //如果参数里面有this，需要处理一下
                return;
            }
        }

    }

    //判断是否父类方法 或者本类方法 这样可能需要判断$0 instanceof patchClassName ...
    public static boolean $0_may_equal_this(CtMethod ctMethod, MethodCall m, CtClass patchClass, CtClass sourceClass) throws NotFoundException {
        CtClass methodTargetClass = m.getMethod().getDeclaringClass();
        return isThisClassOrSubclass(methodTargetClass,patchClass,sourceClass);
    }

    public static boolean isThisClassOrSubclass(CtClass methodTargetClass ,CtClass patchClass,CtClass sourceClass) throws NotFoundException {
        if (sourceClass.getName().equals(patchClass.getName())) {
            return true;
        }
        if (sourceClass.getName().equals(methodTargetClass.getName())) {
            return true;
        }
        if (sourceClass.subclassOf(methodTargetClass) && !methodTargetClass.getName().contentEquals("java.lang.Object")) {
            //// TODO: 17/8/7 判断是否父类方法 或者本类方法
            //*** getClass , com.meituan.sample.SecondActivity is sub class Of : java.lang.Object
            //需要考虑一下protect方法（package方法全部在插桩的时候改掉）
            return true;
        }
        return false;
    }


    public static boolean $args_may_has_this(CtMethod ctMethod, MethodCall m, CtClass patchClass, CtClass sourceClass) throws NotFoundException {
        CtClass[] params = m.getMethod().getParameterTypes();
        if (null == params || params.length == 0) {
            return false;
        }
        for (CtClass param : params) {
            if (isThisClassOrSubclass(param,patchClass,sourceClass)){
                return true;
            }
        }
        return false;
    }

    //non static method call non static method
    //PatchClass this.publicField -> this.OriginClass.publicField
    public static boolean replace_$0_to_this(CtMethod ctMethod, MethodCall m, CtClass patchClass, CtClass sourceClass) throws NotFoundException {

        CtClass methodTargetClass = m.getMethod().getDeclaringClass();
        if (sourceClass.getName().equals(methodTargetClass.getName())) {
        }

        if (sourceClass.getName().equals(methodTargetClass.getName())) {
//            replaceThisToOriginClassMethodDirectly_nonstatic_nonstatic(m);
        } else if (sourceClass.subclassOf(methodTargetClass) && !methodTargetClass.getName().contentEquals("java.lang.Object")) {
            //// TODO: 17/8/7 判断是否父类方法 或者本类方法
            //*** getClass , com.meituan.sample.SecondActivity is sub class Of : java.lang.Object
//            System.err.println("*** " + m.getMethod().getName() + " , " + sourceClass.getName() + " is sub class Of : " + methodTargetClass.getName());
            //需要考虑一下protect方法（package方法全部在插桩的时候改掉）
//            replaceThisToOriginClassMethodDirectly_nonstatic_nonstatic(m);
        }
        String $0_str = "$0";


        {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("{");
//            stringBuilder.append(getParamsThisReplacedString(m));
            stringBuilder.append("$_=($r)this." + ORIGINCLASS + "." + m.getMethod().getName() + "($$);");
            stringBuilder.append("}");
//            m.replace(stringBuilder.toString());
        }


        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        int index = 0;

//        List<String> paramList = new ArrayList<String>();
//        for (CtClass param : params) {
//            index++;
//            if (param.getName().equals(patchClassName)) {
////                    System.err.println("param.getName(): " +param.getName());
////                    System.err.println("sourceClassName Patch: " +patchClassName+"Patch");
//                paramList.add("$" + index + "." + Constants.ORIGINCLASS);
//            } else {
//                paramList.add("$" + index);
//            }
//        }
//        stringBuilder.append(String.join(",", paramList));
//        stringBuilder.append(")");

        return false;
    }

    //PatchClass get(this) -> get(this.OriginClass)
    public static String replace_$args_to_this_origin_class(CtMethod ctMethod, MethodCall m, CtClass patchClass, CtClass sourceClass) throws NotFoundException {
        String paramsStr = "($$)";
        CtClass[] params = m.getMethod().getParameterTypes();
        if (null == params || params.length == 0) {
            return paramsStr;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        int index = 0;
        List<String> paramList = new ArrayList<String>();
        for (CtClass param : params) {
            index++;
            if (param.getName().equals(patchClass.getName())) {
                //// TODO: 17/8/31 如果是patch的父类呢？
//                    System.err.println("param.getName(): " +param.getName());
//                    System.err.println("sourceClassName Patch: " +patchClassName+"Patch");
                paramList.add("$" + index + "." + Constants.ORIGINCLASS);
            } else {
                paramList.add("$" + index);
            }
        }
        stringBuilder.append(String.join(",", paramList));
        stringBuilder.append(")");
        paramsStr = stringBuilder.toString();
        return paramsStr;
    }

//    lambdaFactory$(..) is not found in com.meituan.sample.SecondActivity$$Lambda$2


    public static boolean handleLambdaFactory(CtMethod ctMethod, MethodCall m, CtClass patchClass, CtClass sourceClass) throws NotFoundException, CannotCompileException {
        String lambdaClassName = m.getClassName();
        CtMethod callCtMethod = null;
//        try {
//            callCtMethod = m.getMethod();
//            lambdaClassName = callCtMethod.getDeclaringClass().getName();
//        } catch (NotFoundException e){
//            if (e.getMessage().startsWith("lambdaFactory$(..) is not found in ")){
//                lambdaClassName =  e.getMessage().replace("lambdaFactory$(..) is not found in ","").trim();
//            } else {
//                throw new RuntimeException(e);
//            }
//        }

        CtClass lambdaCtClass = null;
        if (null != lambdaClassName ){
            if (null == callCtMethod){
                lambdaCtClass = Config.classPool.get(lambdaClassName);
                if (null != lambdaCtClass){
                    for (CtMethod ctMethod1 : lambdaCtClass.getDeclaredMethods()){
                        if (ctMethod1.getName().equals(m.getMethodName())){
                            //(Lcom/meituan/sample/SecondActivity;)Landroid/view/View$OnClickListener;
                            //(Lcom/meituan/sample/SecondActivityPatch;)Landroid/view/View$OnClickListener;
                            if (ctMethod1.getSignature().equals(m.getSignature().replace("Patch;",";"))){
                                callCtMethod = ctMethod1;
                                break;
                            }
                        }
                    }
                }
            }
        }

        String paramsStr = "($$)";
        CtClass[] params = callCtMethod.getParameterTypes();
        if (null == params || params.length == 0) {

        }
        else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("(");
            int index = 0;
            List<String> paramList = new ArrayList<String>();
            for (CtClass param : params) {
                index++;
                if (param.getName().equals(patchClass.getName())||param.getName().equals(sourceClass.getName())) {
                    paramList.add("$" + index + "." + Constants.ORIGINCLASS);
                } else {
                    paramList.add("$" + index);
                }
            }
            stringBuilder.append(String.join(",", paramList));
            stringBuilder.append(")");
            paramsStr = stringBuilder.toString();
        }

        if (Config.modifiedClassNameList.contains(lambdaClassName) || Config.newlyAddedClassNameList.contains(lambdaClassName)){

        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("{");
            stringBuilder.append("$_ = $proceed"+paramsStr+";");
            stringBuilder.append("}");
            m.replace(stringBuilder.toString());
            return true;
        }
        if (null == Config.classPool.getOrNull(lambdaClassName)){
            lambdaClassName =  lambdaClassName.replace(sourceClass.getName(),patchClass.getName());
        }
        if (null == lambdaCtClass){
            lambdaCtClass = Config.classPool.getOrNull(lambdaClassName);
        }

        String statement1 = lambdaClassName +  " lambdaInstance = "+ "("+lambdaClassName+ ")" + lambdaClassName + "." + m.getMethodName() + paramsStr +";";
        String statement2 = "lambdaInstance.outerPatchClassName = this ;" ;
        String statement3 = "$_ = ($r) lambdaInstance ;" ;


        CtField ctField = new CtField(patchClass, "outerPatchClassName", lambdaCtClass);
        ctField.setModifiers(AccessFlag.PUBLIC);
        lambdaCtClass.addField(ctField);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        stringBuilder.append(statement1);
        stringBuilder.append(statement2);
        stringBuilder.append(statement3);
        stringBuilder.append("}");
        m.replace(stringBuilder.toString());
        return true;
    }
}
