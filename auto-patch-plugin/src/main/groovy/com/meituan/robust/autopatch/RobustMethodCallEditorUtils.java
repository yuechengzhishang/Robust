package com.meituan.robust.autopatch;

import com.meituan.robust.change.RobustChangeInfo;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.expr.MethodCall;

import static com.meituan.robust.autopatch.RobustMethodExprEditor.getMethodCallString_this_static_method_call;

/**
 * Created by hedingxu on 17/8/31.
 */

public class RobustMethodCallEditorUtils {

    public static void staticMethodCallStaticMethod(CtMethod ctMethod, MethodCall m,CtClass patchClass,CtClass sourceClass) throws NotFoundException, CannotCompileException {
        {
            CtMethod callCtMethod = m.getMethod();
            boolean outerMethodIsStatic = true;
            CtClass methodTargetClass = callCtMethod.getDeclaringClass();
            if (patchClass.getName().equals(methodTargetClass.getName())) {
                if (RobustChangeInfo.isInvariantMethod(callCtMethod)) {
                    if (AccessFlag.isPublic(callCtMethod.getModifiers())) {
                        //need to reflect static method
                        String statement = "$_=($r)" + sourceClass.getName() + "." + m.getMethod().getName() + "($$);";
                        try {
                            m.replace(statement);
                        } catch (javassist.CannotCompileException e){
                            m.replace(getMethodCallString_this_static_method_call(m,patchClass,outerMethodIsStatic,sourceClass.getName()));
                        }
                        return;
                    } else {
                        m.replace(getMethodCallString_this_static_method_call(m,patchClass,outerMethodIsStatic,sourceClass.getName()));
                        return;
                    }
                } else {
                    //do nothing
                    return;
                }
            }
        }
    }
}
