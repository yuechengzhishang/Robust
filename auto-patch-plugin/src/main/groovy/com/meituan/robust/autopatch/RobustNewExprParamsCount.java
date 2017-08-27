package com.meituan.robust.autopatch;

import com.meituan.robust.Constants;
import com.meituan.robust.utils.EnhancedRobustUtils;

import java.util.ArrayList;
import java.util.List;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import javassist.compiler.Javac;
import javassist.expr.Expr;
import javassist.expr.NewExpr;

/**
 * Created by hedingxu on 17/8/17.
 */

public class RobustNewExprParamsCount {
    /**
     * Replaces the <tt>new</tt> expression with the bytecode derived from
     * the given source text.
     * <p>
     * <p>$0 is available but the value is null.
     *
     * @param newExpr a Java statement except try-catch.
     */
    public int replace(NewExpr newExpr) throws CannotCompileException, NotFoundException {
        CtClass thisClass = Config.classPool.get(newExpr.getClassName());
        thisClass.getClassFile();   // to call checkModify().

        final int bytecodeSize = 3;

        int newPos = (int) EnhancedRobustUtils.getFieldValue("newPos", newExpr, NewExpr.class);
        int pos = newPos;

        CodeIterator iterator = (CodeIterator) EnhancedRobustUtils.getFieldValue("iterator", newExpr, Expr.class);

        int newIndex = iterator.u16bitAt(pos + 1);

        /* delete the preceding NEW and DUP (or DUP_X1, SWAP) instructions.
         */
        int codeSize = (int) EnhancedRobustUtils.invokeReflectMethod("canReplace", newExpr, new Object[]{}, new Class[]{}, NewExpr.class);
        int end = pos + codeSize;
        for (int i = pos; i < end; ++i) {
        }

        int currentPos = newExpr.indexOfBytecode();
        ConstPool constPool = (ConstPool) EnhancedRobustUtils.invokeReflectMethod("getConstPool", newExpr, new Object[]{}, new Class[]{}, Expr.class);
        pos = currentPos;
        int methodIndex = iterator.u16bitAt(pos + 1);   // constructor

        String signature = constPool.getMethodrefType(methodIndex);

        Javac jc = new Javac(thisClass);
        ClassPool cp = thisClass.getClassPool();
        try {
            CtClass[] params = Descriptor.getParameterTypes(signature, cp);

            if (null == params || 0 == params.length) {
                return 0;
            }
            return params.length;
        } catch (Exception e) {
            throw new CannotCompileException("broken method");
        }
    }


    /**
     * Replaces the <tt>new</tt> expression with the bytecode derived from
     * the given source text.
     * <p>
     * <p>$0 is available but the value is null.
     *
     * @param newExpr a Java statement except try-catch.
     */
    public static String getParamsString(NewExpr newExpr, String patchClassName) throws CannotCompileException, NotFoundException {
        CtClass thisClass = Config.classPool.get(newExpr.getClassName());
        thisClass.getClassFile();   // to call checkModify().

        final int bytecodeSize = 3;

        int newPos = (int) EnhancedRobustUtils.getFieldValue("newPos", newExpr, NewExpr.class);
        int pos = newPos;

        CodeIterator iterator = (CodeIterator) EnhancedRobustUtils.getFieldValue("iterator", newExpr, Expr.class);

        /* delete the preceding NEW and DUP (or DUP_X1, SWAP) instructions.
         */
        int codeSize = (int) EnhancedRobustUtils.invokeReflectMethod("canReplace", newExpr, new Object[]{}, new Class[]{}, NewExpr.class);
        int end = pos + codeSize;
        for (int i = pos; i < end; ++i) {
        }

        int currentPos = newExpr.indexOfBytecode();
        ConstPool constPool = (ConstPool) EnhancedRobustUtils.invokeReflectMethod("getConstPool", newExpr, new Object[]{}, new Class[]{}, Expr.class);
        pos = currentPos;
        int methodIndex = iterator.u16bitAt(pos + 1);   // constructor

        String signature = constPool.getMethodrefType(methodIndex);

        ClassPool cp = thisClass.getClassPool();
        try {
            CtClass[] params = Descriptor.getParameterTypes(signature, cp);

            if (null == params || 0 == params.length) {
            }

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("(");
            int index = 0;

            List<String> paramList = new ArrayList<String>();
            for (CtClass param : params) {
                index++;
                if (param.getName().equals(patchClassName)) {
//                    System.err.println("param.getName(): " +param.getName());
//                    System.err.println("sourceClassName Patch: " +patchClassName+"Patch");
                    paramList.add("this." + Constants.ORIGINCLASS);
                } else {
                    paramList.add("$" + index);
                }
            }
            stringBuilder.append(String.join(",",paramList));
            stringBuilder.append(")");
            String targetString = stringBuilder.toString();
//            System.err.println("getParamsString : " + targetString);
            return targetString;
        } catch (Exception e) {
            throw new CannotCompileException("broken method");
        }
    }
}
