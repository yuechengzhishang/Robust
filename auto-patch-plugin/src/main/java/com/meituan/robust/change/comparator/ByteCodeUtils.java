package com.meituan.robust.change.comparator;


import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.util.List;
import java.util.Optional;

/**
 * Bytecode generation utilities to work around some ASM / Dex issues.
 */
public class ByteCodeUtils {

    public static final String CONSTRUCTOR = "<init>";
    public static final String CLASS_INITIALIZER = "<clinit>";
    private static final Type NUMBER_TYPE = Type.getObjectType("java/lang/Number");
    private static final Method SHORT_VALUE = Method.getMethod("short shortValue()");
    private static final Method BYTE_VALUE = Method.getMethod("byte byteValue()");

    /**
     * Generates unboxing bytecode for the passed type. An {@link Object} is expected to be on the
     * stack when these bytecodes are inserted.
     *
     * ASM takes a short cut when dealing with short/byte types and convert them into int rather
     * than short/byte types. This is not an issue on the jvm nor Android's ART but it is an issue
     * on Dalvik.
     *
     * @param mv the {@link GeneratorAdapter} generating a method implementation.
     * @param type the expected un-boxed type.
     */
    public static void unbox(GeneratorAdapter mv, Type type) {
        if (type.equals(Type.SHORT_TYPE)) {
            mv.checkCast(NUMBER_TYPE);
            mv.invokeVirtual(NUMBER_TYPE, SHORT_VALUE);
        } else if (type.equals(Type.BYTE_TYPE)) {
            mv.checkCast(NUMBER_TYPE);
            mv.invokeVirtual(NUMBER_TYPE, BYTE_VALUE);
        } else {
            mv.unbox(type);
        }
    }

    /**
     * Converts the given method to a String.
     */
    public static String textify(MethodNode method) {
        Textifier textifier = new Textifier();
        TraceMethodVisitor trace = new TraceMethodVisitor(textifier);
        method.accept(trace);
        String ret = "";
        for (Object line : textifier.getText()) {
            ret += line;
        }
        return ret;
    }









    /**
     * Given a *STORE opcode, it returns the type associated to the variable, or null if
     * not a valid opcode.
     */
    static Type getTypeForStoreOpcode(int opcode) {
        switch (opcode) {
            case Opcodes.ISTORE:
                return Type.INT_TYPE;
            case Opcodes.LSTORE:
                return Type.LONG_TYPE;
            case Opcodes.FSTORE:
                return Type.FLOAT_TYPE;
            case Opcodes.DSTORE:
                return Type.DOUBLE_TYPE;
            case Opcodes.ASTORE:
                return Type.getType(Object.class);
        }
        return null;
    }

    /**
     * Converts a class name from the Java language naming convention (foo.bar.baz) to the JVM
     * internal naming convention (foo/bar/baz).
     */
    public static String toInternalName( String className) {
        return className.replace('.', '/');
    }

    /**
     * Gets the class name from a class member internal name, like {@code com/foo/Bar.baz:(I)V}.
     */

    public static String getClassName( String memberName) {
        Preconditions.checkArgument(memberName.contains(":"), "Class name passed as argument.");
        return memberName.substring(0, memberName.indexOf('.'));
    }

    /**
     * Returns the package name, based on the internal class name. For example, given 'com/foo/Bar'
     * return 'com.foo'.
     *
     * <p>Returns {@link Optional#empty()} for classes in the anonymous package.
     */

    public static Optional<String> getPackageName( String internalName) {
        List<String> parts = Splitter.on('/').splitToList(internalName);
        if (parts.size() == 1) {
            return Optional.empty();
        }

        return Optional.of(Joiner.on('.').join(parts.subList(0, parts.size() - 1)));
    }
}