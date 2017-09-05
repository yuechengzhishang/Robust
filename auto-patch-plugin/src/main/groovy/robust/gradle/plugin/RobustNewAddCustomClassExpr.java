package robust.gradle.plugin;

import com.meituan.robust.autopatch.CheckCodeChanges;
import com.meituan.robust.autopatch.Config;
import com.meituan.robust.autopatch.HasRobustProxyUtils;
import com.meituan.robust.change.RobustChangeInfo;

import java.util.HashMap;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMember;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.expr.Expr;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;

import static com.meituan.robust.Constants.ORIGINCLASS;

/**
 * Created by hedingxu on 17/9/1.
 */

public class RobustNewAddCustomClassExpr extends ExprEditor {
    CtBehavior outCtBehavior ;
    CtClass outCtClass;

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


    public RobustNewAddCustomClassExpr(CtClass outCtClass,CtBehavior outCtBehavior){
        this.outCtClass = outCtClass;
        if (outCtBehavior instanceof CtMethod){
//            javassist.CtMethod@1bd22b8c[public static visit ()V]
        }
        if (outCtBehavior instanceof CtConstructor){
//            javassist.CtConstructor@f9ac336[public AddCustomClass ()V]
        }
        this.hasRobustProxyCode = HasRobustProxyUtils.hasRobustProxy(outCtClass,null,outCtBehavior);
    }
    @Override
    public void edit(FieldAccess fieldAccess) throws CannotCompileException {
        if (hasRobustProxyCode){
            if (repalceWithEmpty(fieldAccess)) {
                return;
            }
        }
        if (fieldAccess.isStatic()){
          try {
              //javassist.CtClassType@7d7d2118[public class com.meituan.sample.AddCustomClass fields=com.meituan.sample.AddCustomClass.changeQuickRedirect:Lcom/meituan/robust/ChangeQuickRedirect;,  constructors=javassist.CtConstructor@6ee0ec46[public AddCustomClass ()V],  methods=javassist.CtMethod@1bd22b8c[public static visit ()V], javassist.CtMethod@77c33d28[public initRobustPatch ()V], ]
              if(fieldAccess.getField().getType().getName().equals("com.meituan.robust.ChangeQuickRedirect")){
                    return;
                }
            } catch (NotFoundException e) {
                e.printStackTrace();
            }

        } else {
            // TODO: 17/9/1 处理非静态field的调用

        }

        try {
            CtField callCtField = fieldAccess.getField();

            if (isAccessModifiedClass(callCtField)){
                if (RobustChangeInfo.isNewAddField(callCtField)){
                    String modifyClassName = callCtField.getDeclaringClass().getName();
                    HashMap<String,String> customModifiedClasses = getCustomModifiedClasses();
                    String patchClassName = customModifiedClasses.get(modifyClassName);
                    //// TODO: 17/9/1
                    if (fieldAccess.isStatic()){
                        if (fieldAccess.isReader()){
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("{");
                            String statement = " $_=($r) " + patchClassName + "." + callCtField.getName() + ";";
                            stringBuilder.append(statement);
                            stringBuilder.append("}");
                            fieldAccess.replace(stringBuilder.toString());
                            return;
                        }
                        if (fieldAccess.isWriter()){
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("{");
                            String statement = patchClassName + "." + callCtField.getName() + " = ($r) $proceed($$);" +  ";";
                            stringBuilder.append(statement);
                            stringBuilder.append("}");
                            fieldAccess.replace(stringBuilder.toString());
                            return;
                        }

                        return;
                    }
                }
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    public static boolean isAccessModifiedClass(CtMember ctMember){
        CtClass callCtClass = ctMember.getDeclaringClass();
        String className = callCtClass.getName();
        boolean isCallModifiedClass =  getCustomModifiedClasses().keySet().contains(className);
        return isCallModifiedClass;
    }


    @Override
    public void edit(MethodCall methodCall) throws CannotCompileException {
        if (hasRobustProxyCode){
            if (isCallProxyAccessDispatchMethod(methodCall)) {
                hasHandledProxyCode = true;
                return;
            }
            if (repalceWithEmpty(methodCall)) {
                return;
            }
        }

        methodCall.getSignature();
        methodCall.getMethodName();
        try {
            methodCall.getMethod();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }


        try {
            CtMethod callCtMethod = methodCall.getMethod();

            if (isAccessModifiedClass(callCtMethod)){
                if (RobustChangeInfo.isNewAddMethod(callCtMethod)){
                    // TODO: 17/9/1
                    String modifyClassName = callCtMethod.getDeclaringClass().getName();
                    HashMap<String,String> customModifiedClasses = getCustomModifiedClasses();
                    String patchClassName = customModifiedClasses.get(modifyClassName);

                    if (isStatic(callCtMethod.getModifiers())){
//                        TestPatchActivity.hello(new Thread());
//                        *TestPatchActivityPatch.hello(new Thread());//处理参数
//                        *参数如果含有TestPatchActivity，需要替换成TestPatchActivityPatch
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("{");
                        String statement = " $_=($r) " + patchClassName + "." + methodCall.getMethodName() + "($$);";
                        stringBuilder.append(statement);
                        stringBuilder.append("}");
                        methodCall.replace(stringBuilder.toString());
                        return;
                    } else {
//                        TestPatchActivity testPatchActivity = new TestPatchActivity(1);
//                        testPatchActivity.toString();
//                        *TestPatchActivityPatch testPatchActivityPatch = new TestPatchActivityPatch();
//                        *testPatchActivityPatch.originClass = $0;
//                        *testPatchActivityPatch.toString();//处理参数
//                        *参数如果含有TestPatchActivity，需要替换成TestPatchActivityPatch

                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("{");
                        stringBuilder.append(" " + patchClassName + " instancePatch = new " + patchClassName + "();");
                        stringBuilder.append(" instancePatch."+ ORIGINCLASS + " = $0 ;");
                        String statement = "$_=($r)" + " instancePatch" + "." + methodCall.getMethodName() + "($$);";
                        stringBuilder.append(statement);
                        stringBuilder.append("}");
                        methodCall.replace(stringBuilder.toString());
                        return;
                    }

                }
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }
    public static boolean isStatic(int modifiers) {
        return (modifiers & AccessFlag.STATIC) != 0;
    }
    public static HashMap<String,String> customModifiedClasses = null;
    public static HashMap<String,String> getCustomModifiedClasses(){
        if (null == customModifiedClasses){
            customModifiedClasses= new HashMap<String,String>();
            for (String className : Config.modifiedClassNameList){
                boolean is_$1_or_$$lambda$1 = CheckCodeChanges.isAnonymousInnerClass(className) || CheckCodeChanges.isAnonymousInnerClass_$$Lambda$1(className);
                if (is_$1_or_$$lambda$1){

                } else {
                    String modifiedClassName = className;
                    String patchClassName = className + "Patch";
                    customModifiedClasses.put(modifiedClassName,patchClassName);
                    String modifiedClassNameAsm = modifiedClassName.replace(".","/");
                    String patchClassNameAsm = patchClassName.replace(".","/");
                    customModifiedClasses.put(modifiedClassNameAsm,patchClassNameAsm);
                }
            }
        }
        return customModifiedClasses;
    }
}
