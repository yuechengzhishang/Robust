package robust.gradle.plugin.asm;

/**
 * Created by hedingxu on 17/8/22.
 */
import org.objectweb.asm.Opcodes;

public class ASMAccessUtils {

    private ASMAccessUtils() {}

    public static boolean isProtected(int access) {
        return (access & Opcodes.ACC_PROTECTED) == Opcodes.ACC_PROTECTED;
    }

    public static boolean isPublic(int access) {
        return (access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC;
    }

    public static boolean isPrivate(int access) {
        return (access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE;
    }

    public static boolean isFinal(int access) {
        return (access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL;
    }

    public static boolean isPackage(int access) {
        return !isProtected(access) && !isPublic(access) && !isPrivate(access);
    }
}