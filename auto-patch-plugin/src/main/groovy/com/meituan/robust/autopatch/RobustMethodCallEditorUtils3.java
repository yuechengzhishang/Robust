package com.meituan.robust.autopatch;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.MethodCall;

/**
 * Created by hedingxu on 17/8/31.
 */

public class RobustMethodCallEditorUtils3 {

    public static void nonstaticMethod_Call_nonStaticMethod(CtMethod ctMethod, MethodCall m, CtClass patchClass, CtClass sourceClass) throws NotFoundException, CannotCompileException {

        CtMethod callCtMethod = m.getMethod();
        boolean outerMethodIsStatic = false;


    }
}
