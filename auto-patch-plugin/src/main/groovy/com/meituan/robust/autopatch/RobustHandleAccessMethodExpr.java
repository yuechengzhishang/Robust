package com.meituan.robust.autopatch;

import com.meituan.robust.change.RobustChangeInfo;
import com.meituan.robust.utils.ProguardUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import robust.gradle.plugin.RobustNewAddCustomClassExpr;

import static com.meituan.robust.Constants.ORIGINCLASS;
import static com.meituan.robust.utils.CustomModifiedClassUtils.getCustomModifiedClasses;
import static robust.gradle.plugin.RobustNewAddCustomClassExpr.isAccessModifiedClass;

/**
 * Created by hedingxu on 17/9/5.
 */

public class RobustHandleAccessMethodExpr extends ExprEditor {
    /**
     * Edits a method call (overridable).
     *
     * The default implementation performs nothing.
     */
    public void edit(MethodCall methodCall) throws CannotCompileException {
        if (!ProguardUtils.isAccess$Method(methodCall)){
            return;
        }
        try {
            CtMethod callCtMethod = methodCall.getMethod();

            if (isAccessModifiedClass(callCtMethod)){
                if (RobustChangeInfo.isNewAddMethod(callCtMethod)){
                    String modifyClassName = callCtMethod.getDeclaringClass().getName();
                    HashMap<String,String> customModifiedClasses = getCustomModifiedClasses();
                    String patchClassName = customModifiedClasses.get(modifyClassName);

                    if (RobustNewAddCustomClassExpr.isStatic(callCtMethod.getModifiers())){
//                        TestPatchActivity.hello(new Thread());
//                        *TestPatchActivityPatch.hello(new Thread());//处理参数
//                        *参数如果含有TestPatchActivity，需要替换成TestPatchActivityPatch
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("{");
                        CtClass[] params = callCtMethod.getParameterTypes();
                        List<String> paramList = new ArrayList<String>();
                        int index = 0;
                        for (CtClass param : params) {
                            index++;
                            if (param.getName().equals(modifyClassName)) {
//                    System.err.println("param.getName(): " +param.getName());
//                    System.err.println("sourceClassName Patch: " +patchClassName+"Patch");
                                stringBuilder.append(patchClassName + " patchInstance" + index + " = new " + patchClassName+ "();");
                                stringBuilder.append(" patchInstance" + index + "." + ORIGINCLASS  + " = $" + index + ";");
                                paramList.add("patchInstance" + index);
                            } else {
                                paramList.add("$" + index);
                            }
                        }

                        String statement = " $_=($r) " + patchClassName + "." + methodCall.getMethodName() + "("+String.join(",",paramList)+");";
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

                        CtClass[] params = callCtMethod.getParameterTypes();
                        List<String> paramList = new ArrayList<String>();
                        int index = 0;
                        for (CtClass param : params) {
                            index++;
                            if (param.getName().equals(modifyClassName)) {
                                stringBuilder.append(patchClassName + " patchInstance" + index + " = new " + patchClassName+ "();");
                                stringBuilder.append(" patchInstance" + index + "." + ORIGINCLASS  + " = $" + index + ";");
                                paramList.add("patchInstance" + index);
                            } else {
                                paramList.add("$" + index);
                            }
                        }

                        String statement = "$_=($r)" + " instancePatch" + "." + methodCall.getMethodName() + "("+String.join(",",paramList)+");";
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
}
