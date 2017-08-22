package robust.gradle.plugin.asm;

import com.google.common.collect.Maps;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import java.util.Map;

/**
 * Created by hedingxu on 17/8/22.
 */

public class FieldGetterChecker {
    private FieldGetterChecker(){

    }
    // TODO: 17/8/22 setter
    public static boolean isGetterMethod(ClassNode classNode, MethodNode methodNode) {
        if (isGetterMethodByName(classNode,methodNode)){
            return isGetterMethod1(classNode,methodNode)
                    || isGetterMethod3(classNode,methodNode);
        } else {
            return false;
        }
    }

    private static boolean isGetterMethod1(ClassNode classNode, MethodNode methodNode) {
        Map<String, String> validGetters = Maps.newHashMap();
        String fieldName = null;
        if (methodNode.desc.startsWith("()")) { //$NON-NLS-1$ // (): No arguments
            InsnList instructions = methodNode.instructions;
            int mState = 1;
            checkMethod:
            for (AbstractInsnNode curr = instructions.getFirst();
                 curr != null;
                 curr = curr.getNext()) {
                switch (curr.getOpcode()) {
                    case -1:
                        // Skip label and line number nodes
                        continue;
                    case Opcodes.ALOAD:
                        if (mState == 1) {
                            fieldName = null;
                            mState = 2;
                        } else {
                            continue checkMethod;
                        }
                        break;
                    case Opcodes.GETFIELD:
                        if (mState == 2) {
                            FieldInsnNode field = (FieldInsnNode) curr;
                            fieldName = field.name;
                            mState = 3;
                        } else {
                            continue checkMethod;
                        }
                        break;
                    case Opcodes.ARETURN:
                    case Opcodes.FRETURN:
                    case Opcodes.IRETURN:
                    case Opcodes.DRETURN:
                    case Opcodes.LRETURN:
                    case Opcodes.RETURN:
                        if (mState == 3) {
                            validGetters.put(methodNode.name, fieldName);
                        }
                        continue checkMethod;
                    default:
                        continue checkMethod;
                }
            }
        }
        return validGetters.size() > 0 ;
    }

    private static boolean isGetterMethodByName(ClassNode classNode, MethodNode methodNode) {
        String name = methodNode.name;
        if (((name.startsWith("get") && name.length() > 3     //$NON-NLS-1$
                && Character.isUpperCase(name.charAt(3)))
                || (name.startsWith("is") && name.length() > 2    //$NON-NLS-1$
                && Character.isUpperCase(name.charAt(2))))) {
            return true;
        } else {
            return false;
        }
    }
    private static boolean isGetterMethod3(ClassNode classNode, MethodNode methodNode){
        if ((methodNode.access & Opcodes.ACC_STATIC) != 0) {
            // Not an instance method
            return false;
        }
        InsnList inList = methodNode.instructions;
        for (int i = 0; i < inList.size(); i++) {
            AbstractInsnNode abstractInsnNode = inList.get(i);
            if (abstractInsnNode.getType() == AbstractInsnNode.METHOD_INSN) {
                return false;
            }
        }

        return true;
    }

}
