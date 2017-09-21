package com.meituan.robust.change.comparator;

import org.objectweb.asm.tree.FieldNode;

import javassist.bytecode.AccessFlag;

/**
 * Created by hedingxu on 17/8/26.
 */

public class FieldComparator implements Comparator<FieldNode> {
    @Override
    public boolean areEqual(FieldNode first,  FieldNode second) {
        if ((first == null) && (second == null)) {
            return true;
        }
        if (first == null || second == null) {
            return true;
        }
        int access1 = first.access;
        int access2 = second.access;
        // 忽略private package protected public ...
        access1 = AccessFlag.setPublic(access1);
        access2 = AccessFlag.setPublic(access2);
        return first.name.equals(second.name)
                && first.desc.equals(second.desc)
                && access1 == access2
                && equal(first.value, second.value);
    }

    public static boolean equal( Object a, Object b) {
        return a == b || a != null && a.equals(b);
    }
}
