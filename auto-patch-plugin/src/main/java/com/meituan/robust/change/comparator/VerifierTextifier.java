package com.meituan.robust.change.comparator;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.Textifier;

/**
 * Created by hedingxu on 17/8/26.
 */

public class VerifierTextifier extends Textifier {
    public VerifierTextifier() {
        super(Opcodes.ASM5);
    }

    @Override
    public void visitLineNumber(int i, Label label) {
        // don't care about line numbers
    }
}
