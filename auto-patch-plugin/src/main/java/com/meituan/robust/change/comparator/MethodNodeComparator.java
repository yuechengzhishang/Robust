package com.meituan.robust.change.comparator;

import com.meituan.robust.utils.RobustLog;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by hedingxu on 17/8/26.
 */

public class MethodNodeComparator {
    public boolean areEqual( MethodNode first,  MethodNode second,  ClassNode originalClass,  ClassNode updatedClass) {
        if (first == null && second == null) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }
        if (!first.name.equals(second.name) || !first.desc.equals(second.desc)) {
            return false;
        }
        VerifierTextifier firstMethodTextifier = new VerifierTextifier();
        VerifierTextifier secondMethodTextifier = new VerifierTextifier();
        first.accept(new TraceMethodVisitor(firstMethodTextifier));
        second.accept(new TraceMethodVisitor(secondMethodTextifier));

        StringWriter firstText = new StringWriter();
        StringWriter secondText = new StringWriter();
        firstMethodTextifier.print(new PrintWriter(firstText));
        secondMethodTextifier.print(new PrintWriter(secondText));

        boolean isEqualSuperficial = firstText.toString().equals(secondText.toString());
        if (!isEqualSuperficial){
//            System.err.println();
//            System.err.println("first:");
//            System.err.println(firstText.toString());
//            System.err.println();
//            System.err.println("second:");
//            System.err.println(secondText.toString());
//            System.err.println("DiffLineByLine:" + first.name + " " + first.desc);

            boolean isRealSame =  DiffLineByLine.diff(firstText.toString(),secondText.toString(),originalClass,  updatedClass);
//            System.err.println("isRealSame if lambda1 == lambda2 :" + isRealSame);
//            System.err.println();
            if (false == isRealSame) {
                RobustLog.log("");
                RobustLog.log("==== method diff info ====");
                RobustLog.log("===class name : " + originalClass.name);
                RobustLog.log("===method name : " + first.name);
                RobustLog.log("===method old text : " );
                RobustLog.log(firstText.toString());
                RobustLog.log("===method new text : " );
                RobustLog.log(secondText.toString());
                RobustLog.log("==== method diff info ====");
                RobustLog.log("");
            }
            return isRealSame;
        }
        return isEqualSuperficial;
    }
}
