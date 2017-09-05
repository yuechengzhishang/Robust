package com.meituan.robust.change;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

import javassist.CtField;
import javassist.CtMethod;

/**
 * Created by hedingxu on 17/8/24.
 */

public class RobustChangeInfo {

    public static List<String> addClasses = new ArrayList<String>();
    public static List<ClassChange> changeClasses = new ArrayList<ClassChange>();

    public static class MethodChange {
        public List<MethodNode> invariantList = new ArrayList<MethodNode>();
        public List<MethodNode> changeList = new ArrayList<MethodNode>();
        public List<MethodNode> addList = new ArrayList<MethodNode>();
    }

    public static class FieldChange {
        public List<FieldNode> invariantList = new ArrayList<FieldNode>();
        public List<FieldNode> changeList = new ArrayList<FieldNode>();
        public List<FieldNode> addList = new ArrayList<FieldNode>();
    }

    public static class ClassChange {
        public ClassNode classNode;
        public MethodChange methodChange;
        public FieldChange fieldChange;
    }

    public static boolean isNewAddMethod(CtMethod ctMethod){
        String dotClass = ctMethod.getDeclaringClass().getName();
        String methodName = ctMethod.getName();
        String signature = ctMethod.getSignature();
        for (ClassChange classChange : changeClasses) {
            if (null != classChange) {
                if (classChange.classNode.name.replace("/", ".").equals(dotClass)) {
                    if (null != classChange.methodChange) {
                        List<MethodNode> changeMethodList = classChange.methodChange.addList;
                        // 如果改变的方法是access$，则视为新增方法处理，访问patch里面的
                        for (MethodNode changedMethodNode:classChange.methodChange.changeList){
                            if (changedMethodNode.name.contains("access$")){
                                changeMethodList.add(changedMethodNode);
                            }
                        }

                        for (MethodNode methodNode : changeMethodList) {
                            if (methodName.equals(methodNode.name)) {
                                //methodNode.desc (Landroid/os/Bundle;)V
                                //ctMethod.getSignature() (Landroid/os/Bundle;)V
                                if (methodNode.desc.equals(signature)) {
                                    return true;
                                }
                            }
                        }
                    } else {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isNewAddField(CtField ctField){
        String dotClass = ctField.getDeclaringClass().getName();
        String methodName = ctField.getName();
        String signature = ctField.getSignature();
        for (ClassChange classChange : changeClasses) {
            if (null != classChange) {
                if (classChange.classNode.name.replace("/", ".").equals(dotClass)) {
                    if (null != classChange.fieldChange) {
                        List<FieldNode> changeMethodList = classChange.fieldChange.addList;
                        for (FieldNode methodNode : changeMethodList) {
                            if (methodName.equals(methodNode.name)) {
                                //methodNode.desc (Landroid/os/Bundle;)V
                                //ctMethod.getSignature() (Landroid/os/Bundle;)V
                                if (methodNode.desc.equals(signature)) {
                                    return true;
                                }
                            }
                        }
                    } else {
                        return false;
                    }
                }
            }
        }
        return false;
    }


    public static boolean isInvariantMethod(CtMethod ctMethod) {
        String dotClass = ctMethod.getDeclaringClass().getName();
        String patchClassName = new String(dotClass);
        if (dotClass.endsWith("Patch")) {
            dotClass = dotClass + "ROBUST_FOR_DELETE";
            String tempStr = "Patch" + "ROBUST_FOR_DELETE";
            dotClass = dotClass.replace(tempStr,"");
        }
        String sourceClassName = new String(dotClass);
        String methodName = ctMethod.getName();
        String signature = ctMethod.getSignature();
        for (ClassChange classChange : changeClasses) {
            if (null != classChange) {
                if (classChange.classNode.name.replace("/",".").equals(dotClass)) {
                    if (null != classChange.methodChange) {
                        List<MethodNode> changeMethodList = classChange.methodChange.invariantList;
                        for (MethodNode methodNode : changeMethodList) {
                            if (methodName.equals(methodNode.name)) {
                                //methodNode.desc (Landroid/os/Bundle;)V
                                //ctMethod.getSignature() (Landroid/os/Bundle;)V
                                if (methodNode.desc.equals(signature)) {
                                    return true;
                                } else {
                                    if (!patchClassName.equals(sourceClassName)){
                                        String signature2 = signature.replace(patchClassName.replace(".","/"),sourceClassName.replace(".","/"));
                                        if (methodNode.desc.equals(signature2)){
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        return true;
                    }
                }
            }

        }
        return false;
    }


    public static boolean isInvariantField(CtField ctField) {
        String dotClass = ctField.getDeclaringClass().getName();
        if (dotClass.endsWith("Patch")) {
            dotClass = dotClass + "ROBUST_FOR_DELETE";
            String tempStr = "Patch" + "ROBUST_FOR_DELETE";
            dotClass = dotClass.replace(tempStr,"");
        }
        String methodName = ctField.getName();
        String signature = ctField.getSignature();
        for (ClassChange classChange : changeClasses) {
            if (null != classChange) {
                if (classChange.classNode.name.replace("/",".").equals(dotClass)) {
                    if (null != classChange.fieldChange) {
                        List<FieldNode> fieldNodeList = classChange.fieldChange.invariantList;
                        for (FieldNode fieldNode : fieldNodeList) {
                            if (methodName.equals(fieldNode.name)) {
                                //methodNode.desc (Landroid/os/Bundle;)V
                                //ctMethod.getSignature() (Landroid/os/Bundle;)V
                                if (fieldNode.desc.equals(signature)) {
                                    return true;
                                }
                            }
                        }
                    } else {
                        return true;
                    }
                }
            }

        }
        return false;
    }

    public static boolean isChangedMethod(CtMethod ctMethod) {
        String dotClass = ctMethod.getDeclaringClass().getName();
        if (dotClass.endsWith("Patch")) {
            dotClass = dotClass + "ROBUST_FOR_DELETE";
            String tempStr = "Patch" + "ROBUST_FOR_DELETE";
            dotClass = dotClass.replace(tempStr,"");
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
                                }
                            }
                        }
                        return false;
                    } else {
                        return false;
                    }
                }
            }

        }
        return false;
    }
}
