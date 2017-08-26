package com.meituan.robust.change;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

import javassist.CtMethod;

/**
 * Created by hedingxu on 17/8/24.
 */

public class RobustChangeInfo {

    public static List<String> addClasses = new ArrayList<String>();
    public static List<ClassChange> changeClasses = new ArrayList<ClassChange>();

    public static class MethodChange {
        public List<MethodNode> changeList = new ArrayList<MethodNode>();
        public List<MethodNode> addList = new ArrayList<MethodNode>();
    }

    public static class FieldChange {
        public List<FieldNode> changeList = new ArrayList<FieldNode>();
        public List<FieldNode> addList = new ArrayList<FieldNode>();
    }

    public static class ClassChange {
        public ClassNode classNode;
        public MethodChange methodChange;
        public FieldChange fieldChange;
    }

    public static boolean isChangedMethod(CtMethod ctMethod) {
        String dotClass = ctMethod.getDeclaringClass().getName();
        if (dotClass.endsWith("Patch")) {
            dotClass = dotClass.replace("Patch", "");
        }
        String methodName = ctMethod.getName();
        String signature = ctMethod.getSignature();
        for (ClassChange classChange : changeClasses) {
            if (null != classChange) {
                if (classChange.classNode.name.replace("/",".").equals(dotClass)) {
                    if (null != classChange.methodChange) {
                        List<MethodNode> changeMethodList = classChange.methodChange.changeList;
                        for (MethodNode methodNode : changeMethodList) {
                            if (methodName.equals(methodNode.name)) {
                                //methodNode.desc (Landroid/os/Bundle;)V
                                //ctMethod.getSignature() (Landroid/os/Bundle;)V
                                if (methodNode.desc.equals(signature)) {
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }

        }
        return false;
    }
}
