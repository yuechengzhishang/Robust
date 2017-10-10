package com.meituan.robust.change;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.meituan.robust.change.comparator.ByteCodeUtils;
import com.meituan.robust.change.comparator.Comparator;
import com.meituan.robust.change.comparator.FieldComparator;
import com.meituan.robust.change.comparator.MethodNodeComparator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * check how many class(method/field) have changed between two jars
 * <p>
 * ThreadSafe
 */
public class RobustCodeChangeChecker {

    private static final MethodNodeComparator METHOD_COMPARATOR = new MethodNodeComparator();

    private static final Comparator<FieldNode> FIELD_COMPARATOR = new FieldComparator();
    private static final Comparator<String> STRING_COMPARATOR = new Comparator<String>(){
        @Override
        public boolean areEqual(String first, String second) {
            return first.equals(second);
        }
    };

    public interface ClassBytesProvider {
        byte[] load() throws IOException;
    }

    public static class ClassBytesFileProvider implements ClassBytesProvider {

        private final File file;

        public ClassBytesFileProvider(File file) {
            this.file = file;
        }

        @Override
        public byte[] load() throws IOException {
            return Files.toByteArray(file);
        }

        public File getFile() {
            return file;
        }
    }

    public static class ClassBytesJarEntryProvider implements ClassBytesProvider {

        private final JarFile jarFile;
        private final JarEntry jarEntry;

        public ClassBytesJarEntryProvider(JarFile jarFile, JarEntry jarEntry) {
            this.jarFile = jarFile;
            this.jarEntry = jarEntry;
        }

        @Override
        public byte[] load() throws IOException {
            InputStream is = jarFile.getInputStream(jarEntry);

            try {
//                return org.apache.commons.io.IOUtils.toByteArray(is);
                return ByteStreams.toByteArray(is);
            } finally {
                Closeables.close(is, false /* swallowIOException */);
            }
        }
    }

    /**
     * describe the difference between two collections of the same elements.
     */
    enum Diff {
        /**
         * no changeClasses, the collections are equals
         */
        NONE,
        /**
         * an element was added to the first collection.
         */
        ADDITION,
        /**
         * an element was removed from the first collection.
         */
        REMOVAL,
        /**
         * an element was changed.
         */
        CHANGE
    }

    private RobustCodeChangeChecker() {
    }

    public static ClassNode getClassNode(File classFile) throws IOException {
        byte[] classBytes = org.apache.commons.io.FileUtils.readFileToByteArray(classFile);
        ClassReader classReader = new ClassReader(classBytes);
        org.objectweb.asm.tree.ClassNode classNode = new org.objectweb.asm.tree.ClassNode();
        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
        return classNode;
    }

    public static ClassNode getClassNode(byte[] classBytes) throws IOException {
        ClassReader classReader = new ClassReader(classBytes);
        org.objectweb.asm.tree.ClassNode classNode = new org.objectweb.asm.tree.ClassNode();
        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
        return classNode;
    }

    public static RobustChangeInfo.ClassChange diffClass(ClassNode originalClass, ClassNode updatedClass)
            throws IOException {
        if (isRDotClass(originalClass)) {
            return null;
        }

        if (!originalClass.superName.equals(updatedClass.superName)) {
            ChangeLog.log(updatedClass.name,"PARENT_CLASS_CHANGED");
            return null;
        }

        if (diffList(originalClass.interfaces, updatedClass.interfaces,
                STRING_COMPARATOR) != Diff.NONE) {
            ChangeLog.log(updatedClass.name,"IMPLEMENTED_INTERFACES_CHANGE");
            return null;
        }

        RobustChangeInfo.ClassChange classChange = new RobustChangeInfo.ClassChange();
        classChange.methodChange = diffMethods(originalClass, updatedClass);
        // TODO: 17/8/24 先不考虑field
        classChange.fieldChange = diffFields(originalClass, updatedClass);;
        classChange.classNode = updatedClass;
        return classChange;
    }


    private static RobustChangeInfo.MethodChange diffMethods(
             ClassNode originalClass,  ClassNode updatedClass) {

//        @SuppressWarnings("unchecked") // ASM API.
        List<MethodNode> nonVisitedMethodsOnUpdatedClass = new ArrayList<>(updatedClass.methods);
        List<MethodNode> changedMethods = new ArrayList<>();
        List<MethodNode> addMethods = new ArrayList<>();
        List<MethodNode> invariantMethods = new ArrayList<>();

        List<MethodNode> changedParamMethods = new ArrayList<>();
        //noinspection unchecked
        for (MethodNode methodNode : (List<MethodNode>) originalClass.methods) {
//            if (methodNode.name.equals("initRobustPatch")){
//                System.err.println(originalClass.name + " : " + methodNode.name + " " + methodNode.desc);
//            }
            MethodNode updatedMethod = findMethod(updatedClass, methodNode.name, methodNode.desc);
            if (updatedMethod == null) {
                // although it's probably ok if a method got deleted since nobody should be calling
                // it anymore BUT the application might be using reflection to get the list of
                // methods and would still see the deleted methods. To be prudent, restart.
                // However, if the class initializer got removed, it's always fine.

                //删除的方法,先忽略掉，对于override的方法后面需要考虑
                //todo 删除方法（大部分可以忽略，但是如果是有super的调用，则需要处理一下新方法调用super方法即可)
                ChangeLog.log(updatedClass.name, "METHOD_DELETED : " + methodNode.name + " " + methodNode.desc);
            } else {
                if (methodNode.name.equals(ByteCodeUtils.CLASS_INITIALIZER)) {
                    //ignore
                    boolean isEqual = METHOD_COMPARATOR.areEqual(methodNode, updatedMethod, originalClass,  updatedClass);
                    if (isEqual) {
                        invariantMethods.add(updatedMethod);
                    } else {
                        ChangeLog.log(updatedClass.name, "METHOD_CHANGE : " + methodNode.name + " " + methodNode.desc);
                        changedMethods.add(updatedMethod);
                    }
                } else {
                    MethodNode changedParamMethodNode = getParamChangedMethod(methodNode, updatedMethod, originalClass,  updatedClass);
                    if (null != changedParamMethodNode){
                        changedParamMethods.add(changedParamMethodNode);
                    } else {
                        boolean isEqual = METHOD_COMPARATOR.areEqual(methodNode, updatedMethod, originalClass,  updatedClass);
                        if (isEqual) {
                            invariantMethods.add(updatedMethod);
                        } else {
                            ChangeLog.log(updatedClass.name, "METHOD_CHANGE : " + methodNode.name + " " + methodNode.desc);
                            changedMethods.add(updatedMethod);
                        }
                    }
                }
            }

            // remove the method from the visited ones on the updated class.
            nonVisitedMethodsOnUpdatedClass.remove(updatedMethod);

        }

        addMethods.addAll(changedParamMethods);

        if (!nonVisitedMethodsOnUpdatedClass.isEmpty()) {
            addMethods.addAll(nonVisitedMethodsOnUpdatedClass);
        }

        for (MethodNode methodNode : addMethods) {
            ChangeLog.log(updatedClass.name, "METHOD_ADD : " + methodNode.name + " " + methodNode.desc);
        }

        //record
        if (changedMethods.isEmpty() && addMethods.isEmpty()) {
//            RobustChangeInfo.MethodChange methodChange = new RobustChangeInfo.MethodChange();
//            methodChange.changeStatus = RobustChangeStatus.CHANGE_NONE;
//            return methodChange;
            return null;
        } else {
            RobustChangeInfo.MethodChange methodChange = new RobustChangeInfo.MethodChange();
            methodChange.addList.addAll(addMethods);
            methodChange.changeList.addAll(changedMethods);
            methodChange.invariantList.addAll(invariantMethods);
            return methodChange;
        }
    }

    //methodNode, updatedMethod, originalClass,  updatedClass
    public static MethodNode getParamChangedMethod( MethodNode first,  MethodNode second,  ClassNode originalClass,  ClassNode updatedClass){
        if (first == null && second == null) {
            return null;
        }
        if (first == null || second == null) {
            return null;
        }
        if (!first.name.equals(second.name)) {
            return null;
        }

        if (!first.desc.equals(second.desc)){
            return second;
        }
        return null;

    }



    private static RobustChangeInfo.FieldChange diffFields(
             ClassNode originalClass,
             ClassNode updatedClass) {

        List<FieldNode> one =originalClass.fields;
        List<FieldNode> two =updatedClass.fields;
        if (one == null && two == null) {
            return null;
        }
        if (one != null && two == null){
            //delete all fields
            ChangeLog.log(updatedClass.name,"DELETED ALL FIELDS");
            return null;
        }

        if (one == null && two != null) {
            RobustChangeInfo.FieldChange fieldChange = new RobustChangeInfo.FieldChange();
            fieldChange.addList.addAll(two);
            ChangeLog.log(updatedClass.name,"ADD FIELDS");
            return fieldChange;
        }

        if (one != null && two != null) {
            List<FieldNode> copyOfOne = new ArrayList<FieldNode>(one);
            List<FieldNode> copyOfTwo = new ArrayList<FieldNode>(two);

            List<FieldNode> invariantFields = new ArrayList<FieldNode>();
            for (FieldNode elementOfTwo : two) {
                //遍历2
                FieldNode commonElement = getElementOf(copyOfOne, elementOfTwo, FIELD_COMPARATOR);
                if (commonElement != null) {
                    copyOfOne.remove(commonElement);
                    invariantFields.add(elementOfTwo);
                }
            }

            for (FieldNode elementOfOne : one) {
                FieldNode commonElement = getElementOf(copyOfTwo, elementOfOne, FIELD_COMPARATOR);
                if (commonElement != null) {
                    copyOfTwo.remove(commonElement);
                }
            }

            if (copyOfTwo.isEmpty()){
                //有删除的，没有修改和新增的Field 忽略删除的
                return null;
            }

            for (FieldNode fieldNode :copyOfTwo){
                ChangeLog.log(updatedClass.name,"FIELD ADD OR CHANGED : " + fieldNode.name + " , " + fieldNode.desc);
            }

            RobustChangeInfo.FieldChange fieldChange = new RobustChangeInfo.FieldChange();
            fieldChange.changeList.addAll(copyOfTwo);
            fieldChange.addList.addAll(copyOfOne);
            fieldChange.invariantList.addAll(invariantFields);

            return  fieldChange;
        }
        return null;
    }

    public static boolean isRDotClass(ClassNode originalClass) {
        // Detect R$something classes, and report changes in them separately.
        String name = originalClass.name;
        if (name.endsWith("/R")) {
            return true;
        }
        int index = name.lastIndexOf('/');
        if (index != -1 &&
                name.startsWith("R$", index + 1) &&
                (originalClass.access & Opcodes.ACC_PUBLIC) != 0 &&
                (originalClass.access & Opcodes.ACC_FINAL) != 0 &&
                originalClass.outerClass == null &&
                originalClass.interfaces.isEmpty() &&
                originalClass.superName.equals("java/lang/Object") &&
                name.length() > 3 && Character.isLowerCase(name.charAt(2))) {
            return true;
        }
        return false;
    }


    private static MethodNode findMethod( ClassNode classNode,
                                          String name,
                                          String desc) {
        //noinspection unchecked
        for (MethodNode methodNode : (List<MethodNode>) classNode.methods) {
            if (methodNode.name.equals(name) &&
                    ((desc == null && methodNode.desc == null) || (methodNode.desc.equals(desc)))) {
                return methodNode;
            }
        }
        return null;
    }

    static <T> Diff diffList(
             List<T> one,
             List<T> two,
             Comparator<T> comparator) {

        if (one == null && two == null) {
            return Diff.NONE;
        }
        if (one == null) {
            return Diff.ADDITION;
        }
        if (two == null) {
            return Diff.REMOVAL;
        }
        List<T> copyOfOne = new ArrayList<T>(one);
        List<T> copyOfTwo = new ArrayList<T>(two);

        for (T elementOfTwo : two) {
            T commonElement = getElementOf(copyOfOne, elementOfTwo, comparator);
            if (commonElement != null) {
                copyOfOne.remove(commonElement);
            }
        }

        for (T elementOfOne : one) {
            T commonElement = getElementOf(copyOfTwo, elementOfOne, comparator);
            if (commonElement != null) {
                copyOfTwo.remove(commonElement);
            }
        }
        if ((!copyOfOne.isEmpty()) && (copyOfOne.size() == copyOfTwo.size())) {
            return Diff.CHANGE;
        }
        if (!copyOfOne.isEmpty()) {
            return Diff.REMOVAL;
        }
        return copyOfTwo.isEmpty() ? Diff.NONE : Diff.ADDITION;
    }


    public static <T> T getElementOf(List<T> list, T element, Comparator<T> comparator) {
        for (T elementOfList : list) {
            if (comparator.areEqual(elementOfList, element)) {
                return elementOfList;
            }
        }
        return null;
    }

    static ClassNode loadClass(ClassBytesProvider classFile) throws IOException {
        byte[] classBytes = classFile.load();
//        classFile.name.startWith("")
        ClassReader classReader = new ClassReader(classBytes);


        org.objectweb.asm.tree.ClassNode classNode = new org.objectweb.asm.tree.ClassNode();
        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
        return classNode;
    }
}
