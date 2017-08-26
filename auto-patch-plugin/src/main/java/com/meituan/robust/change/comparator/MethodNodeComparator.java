package com.meituan.robust.change.comparator;

import com.android.annotations.Nullable;

import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by hedingxu on 17/8/26.
 */

public class MethodNodeComparator implements Comparator<MethodNode> {

    @Override
    public boolean areEqual(@Nullable MethodNode first, @Nullable MethodNode second) {
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

        return firstText.toString().equals(secondText.toString());
    }
}
