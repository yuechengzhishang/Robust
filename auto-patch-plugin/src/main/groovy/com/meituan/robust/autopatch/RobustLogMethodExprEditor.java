package com.meituan.robust.autopatch;

import java.util.concurrent.atomic.AtomicInteger;

import javassist.CannotCompileException;
import javassist.expr.Expr;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import static com.meituan.robust.autopatch.RobustMethodExprEditor.getLogString;

/**
 * Created by hedingxu on 17/10/10.
 */

public class RobustLogMethodExprEditor extends ExprEditor {
    String logPrefix;
    public RobustLogMethodExprEditor(String logPrefix){
        this.logPrefix = logPrefix;
    }
    AtomicInteger atomicInteger = new AtomicInteger(1000000);

    @Override
    public void edit(MethodCall m) throws CannotCompileException {
        if (1000000 == atomicInteger.getAndAdd(1)) {
            try {
                m.replace("{$_ = $proceed($$); " + getLog(logPrefix + " : " +  getLineNumber(m)) + "}");
            } catch (Throwable throwable){

            }
        } else if (1 == atomicInteger.addAndGet(1)%2) {
            try {
                m.replace("{$_ = $proceed($$); " + getLog(logPrefix + " : " +  getLineNumber(m)) + "}");
            } catch (Throwable throwable){
            }
        }
    }

    public int getLineNumber(Expr expr){
        int lineNumber = expr.getLineNumber();
        if (lineNumber == -1 || lineNumber == 0){
            lineNumber = atomicInteger.get();
        }
        return lineNumber;
    }

    public static String getLog(String value){
        return getLogString(value);
    }
}
