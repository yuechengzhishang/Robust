package com.meituan.robust.change.comparator;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by hedingxu on 17/8/26.
 */

public class MethodNodeComparator {
    public boolean areEqual(@Nullable MethodNode first, @Nullable MethodNode second, @NonNull ClassNode originalClass, @NonNull ClassNode updatedClass) {
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

        if (!firstText.toString().equals(secondText.toString())){
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
            return isRealSame;
        }
        return firstText.toString().equals(secondText.toString());
    }
}
