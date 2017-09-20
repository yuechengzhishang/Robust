package com.meituan.robust.autopatch.innerclass.anonymous;

import com.meituan.robust.autopatch.AnonymousInnerClassMethodExprEditor;
import com.meituan.robust.utils.RobustLog;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;

/**
 * Created by hedingxu on 17/8/23.
 */

public class AnonymousInnerClassTransform {
    public static void handleAccessMethodCall(CtClass anonymousInnerClass,String outerSourceClassName, String outerPatchClassName){
        for (CtMethod method : anonymousInnerClass.getDeclaredMethods()) {
            try {
                method.instrument(
                        new AnonymousInnerClassMethodExprEditor(anonymousInnerClass, outerSourceClassName,outerPatchClassName, method)
                );
            } catch (CannotCompileException e) {
                RobustLog.log("Exception ",e);
            }
        }
    }
}
