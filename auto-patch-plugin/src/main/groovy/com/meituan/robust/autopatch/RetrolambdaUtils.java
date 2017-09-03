package com.meituan.robust.autopatch;

import com.meituan.robust.change.RobustCodeChangeChecker;
import com.meituan.robust.utils.JavaUtils;

import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.meituan.robust.Constants.File_SEPARATOR;

/**
 * Created by hedingxu on 17/9/3.
 */

public class RetrolambdaUtils {
    /**
     * Java 8 produces at runtime classes named {@code EnclosingClass$$Lambda$1}
     */
    public static final Pattern LAMBDA_CLASS = Pattern.compile("^.+\\$\\$Lambda\\$\\d+$");

    public static boolean isLambda(String className) {
        if (className.endsWith(".class")) {
            className = className.replace(".class", "");
        }
        Matcher lambdaMatcher = LAMBDA_CLASS.matcher(className);
        if (lambdaMatcher.matches()) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        String name = "com.meituan.sample.TestPatchActivity$$Lambda$1";
        System.err.println(isLambda(name));
    }


    public static void handleLambda(Map<String, JarEntry> Entries,
                                    Map<String, JarEntry> backupEntries,
                                    JarFile backupJar, JarFile newJar

    ) throws IOException {


//        System.err.println("modifiedClassNameList is ：");
//        JavaUtils.printList(Config.modifiedClassNameList);
//
//        System.err.println("newlyAddedClassNameList is ：");
//        JavaUtils.printList(Config.newlyAddedClassNameList);
//
//        System.err.println("modifiedAnonymousInnerClassNameList is :");
//        JavaUtils.printList(Config.modifiedAnonymousInnerClassNameList);

        for (String dotClassName : Config.modifiedClassNameList) {
            if (CheckCodeChanges.isAnonymousInnerClass_$$Lambda$1(dotClassName)) {
                Config.lambdaDotClassNameList.add(dotClassName);
            }
        }

        for (String dotClassName : Config.newlyAddedClassNameList) {
            if (CheckCodeChanges.isAnonymousInnerClass_$$Lambda$1(dotClassName)) {
                Config.lambdaDotClassNameList.add(dotClassName);
            }
        }

        for (String dotClassName : Config.modifiedAnonymousInnerClassNameList) {
            if (CheckCodeChanges.isAnonymousInnerClass_$$Lambda$1(dotClassName)) {
                Config.lambdaDotClassNameList.add(dotClassName);
            }
        }

        System.err.println("===lambda : ");
        JavaUtils.printList(Config.lambdaDotClassNameList);
//        CheckCodeChangesFix.
        // TODO: 17/9/2
        System.err.println("===fix lambda start : ");
        HashSet<String> lambdaOuterDotClassNameSet = new HashSet<String>();
        for (String lambdaOuterDotClassName : Config.lambdaDotClassNameList) {
            int index = lambdaOuterDotClassName.indexOf("$$Lambda$");
            lambdaOuterDotClassName = lambdaOuterDotClassName.substring(0, index);
            String classNodeName = lambdaOuterDotClassName.replace(".", File_SEPARATOR) + ".class";
            lambdaOuterDotClassNameSet.add(classNodeName);
        }
        List<String> temList = new ArrayList<String>();
        temList.addAll(lambdaOuterDotClassNameSet);
        JavaUtils.printList(temList);
        for (String modifiedClassNodeName : temList) {
            JarEntry jarEntry = Entries.get(modifiedClassNodeName);
            JarEntry backupEntry = backupEntries.get(modifiedClassNodeName);
            byte[] oldClassBytes =
                    new RobustCodeChangeChecker.ClassBytesJarEntryProvider(backupJar, backupEntry).load();

            byte[] newClassBytes =
                    new RobustCodeChangeChecker.ClassBytesJarEntryProvider(newJar, jarEntry).load();


            {
                ClassNode newClassNode = RobustCodeChangeChecker.getClassNode(newClassBytes);
                System.err.println("newClassNode : " + newClassNode.name);
                HashSet<ClassNode> newLambdaClassNodeSet = new HashSet<ClassNode>();
                String newClassNodeNameWithOutClass = newClassNode.name.replace(".class", "");
                for (JarEntry newJarEntry : Entries.values()) {
                    String newJarEntryName = newJarEntry.getName();
                    if (newJarEntryName.endsWith(".class")) {
                        if (newJarEntryName.startsWith(newClassNodeNameWithOutClass)) {
                            if (RetrolambdaUtils.isLambda(newJarEntryName)) {
                                String dotClass = newJarEntryName.replace("/", ".").replace(".class", "");
                                if (Config.lambdaDotClassNameList.contains(dotClass)) {
                                    byte[] newLambdaClassBytes = new RobustCodeChangeChecker.ClassBytesJarEntryProvider(newJar, newJarEntry).load();
                                    ClassNode newLambdaClassNode = RobustCodeChangeChecker.getClassNode(newLambdaClassBytes);
                                    newLambdaClassNodeSet.add(newLambdaClassNode);
                                    System.err.println("newClassNode : " + newLambdaClassNode.name);
                                }
                            }
                        }
                    }
                }
            }

            {
                ClassNode oldClassNode = RobustCodeChangeChecker.getClassNode(oldClassBytes);
                System.err.println("oldClassNode : " + oldClassNode.name);
                HashSet<ClassNode> oldLambdaClassNodeSet = new HashSet<ClassNode>();
                String oldClassNodeNameWithOutClass = oldClassNode.name.replace(".class", "");
                for (JarEntry oldjarEntry : backupEntries.values()) {
                    String oldjarEntryName = oldjarEntry.getName();
                    if (oldjarEntryName.endsWith(".class")) {
                        if (oldjarEntryName.startsWith(oldClassNodeNameWithOutClass)) {
                            if (RetrolambdaUtils.isLambda(oldjarEntryName)) {
                                byte[] oldLambdaClassBytes = new RobustCodeChangeChecker.ClassBytesJarEntryProvider(backupJar, oldjarEntry).load();
                                ClassNode oldLambdaClassNode = RobustCodeChangeChecker.getClassNode(oldLambdaClassBytes);
                                oldLambdaClassNodeSet.add(oldLambdaClassNode);
                                System.err.println("oldClassNode : " + oldLambdaClassNode.name);
                            }
                        }
                    }
                }

//                JavaUtils.printList();
            }
            System.err.println();


//            Config.classPool.get()

            // Used to ensure that each spun class name is unique

        }
        System.err.println("CheckCodeChangesFix: fix lambda ");
        System.err.println("===fix lambda end     ");
        System.err.println();
    }


    public static void getLambdaClassNodeFromNew(Map<String, JarEntry> Entries,
                                                 Map<String, JarEntry> backupEntries,
                                                 JarFile backupJar, JarFile newJar

    ) throws IOException {


        System.err.println("modifiedClassNameList is ：");
        JavaUtils.printList(Config.modifiedClassNameList);

        System.err.println("newlyAddedClassNameList is ：");
        JavaUtils.printList(Config.newlyAddedClassNameList);

        System.err.println("modifiedAnonymousInnerClassNameList is :");
        JavaUtils.printList(Config.modifiedAnonymousInnerClassNameList);

        for (String dotClassName : Config.modifiedClassNameList) {
            if (CheckCodeChanges.isAnonymousInnerClass_$$Lambda$1(dotClassName)) {
                Config.lambdaDotClassNameList.add(dotClassName);
            }
        }

        for (String dotClassName : Config.newlyAddedClassNameList) {
            if (CheckCodeChanges.isAnonymousInnerClass_$$Lambda$1(dotClassName)) {
                Config.lambdaDotClassNameList.add(dotClassName);
            }
        }

        for (String dotClassName : Config.modifiedAnonymousInnerClassNameList) {
            if (CheckCodeChanges.isAnonymousInnerClass_$$Lambda$1(dotClassName)) {
                Config.lambdaDotClassNameList.add(dotClassName);
            }
        }

        System.err.println("===lambda : ");
        JavaUtils.printList(Config.lambdaDotClassNameList);
//        CheckCodeChangesFix.
        // TODO: 17/9/2
        System.err.println("===fix lambda start : ");
        HashSet<String> lambdaOuterDotClassNameSet = new HashSet<String>();
        for (String lambdaOuterDotClassName : Config.lambdaDotClassNameList) {
            int index = lambdaOuterDotClassName.indexOf("$$Lambda$");
            lambdaOuterDotClassName = lambdaOuterDotClassName.substring(0, index);
            String classNodeName = lambdaOuterDotClassName.replace(".", File_SEPARATOR) + ".class";
            lambdaOuterDotClassNameSet.add(classNodeName);
        }
        List<String> temList = new ArrayList<String>();
        temList.addAll(lambdaOuterDotClassNameSet);
        JavaUtils.printList(temList);
        for (String modifiedClassNodeName : temList) {
            JarEntry jarEntry = Entries.get(modifiedClassNodeName);
            JarEntry backupEntry = backupEntries.get(modifiedClassNodeName);
            byte[] oldClassBytes =
                    new RobustCodeChangeChecker.ClassBytesJarEntryProvider(backupJar, backupEntry).load();

            byte[] newClassBytes =
                    new RobustCodeChangeChecker.ClassBytesJarEntryProvider(newJar, jarEntry).load();


            {
                ClassNode newClassNode = RobustCodeChangeChecker.getClassNode(newClassBytes);
                System.err.println("newClassNode : " + newClassNode.name);
                HashSet<ClassNode> newLambdaClassNodeSet = new HashSet<ClassNode>();
                String newClassNodeNameWithOutClass = newClassNode.name.replace(".class", "");
                for (JarEntry newJarEntry : Entries.values()) {
                    String newJarEntryName = newJarEntry.getName();
                    if (newJarEntryName.endsWith(".class")) {
                        if (newJarEntryName.startsWith(newClassNodeNameWithOutClass)) {
                            if (RetrolambdaUtils.isLambda(newJarEntryName)) {
                                String dotClass = newJarEntryName.replace("/", ".").replace(".class", "");
                                if (Config.lambdaDotClassNameList.contains(dotClass)) {
                                    byte[] newLambdaClassBytes = new RobustCodeChangeChecker.ClassBytesJarEntryProvider(newJar, newJarEntry).load();
                                    ClassNode newLambdaClassNode = RobustCodeChangeChecker.getClassNode(newLambdaClassBytes);
                                    newLambdaClassNodeSet.add(newLambdaClassNode);
                                    System.err.println("newClassNode : " + newLambdaClassNode.name);
                                }
                            }
                        }
                    }
                }
            }

            {
                ClassNode oldClassNode = RobustCodeChangeChecker.getClassNode(oldClassBytes);
                System.err.println("oldClassNode : " + oldClassNode.name);
                HashSet<ClassNode> oldLambdaClassNodeSet = new HashSet<ClassNode>();
                String oldClassNodeNameWithOutClass = oldClassNode.name.replace(".class", "");
                for (JarEntry oldjarEntry : backupEntries.values()) {
                    String oldjarEntryName = oldjarEntry.getName();
                    if (oldjarEntryName.endsWith(".class")) {
                        if (oldjarEntryName.startsWith(oldClassNodeNameWithOutClass)) {
                            if (RetrolambdaUtils.isLambda(oldjarEntryName)) {
                                byte[] oldLambdaClassBytes = new RobustCodeChangeChecker.ClassBytesJarEntryProvider(backupJar, oldjarEntry).load();
                                ClassNode oldLambdaClassNode = RobustCodeChangeChecker.getClassNode(oldLambdaClassBytes);
                                oldLambdaClassNodeSet.add(oldLambdaClassNode);
                                System.err.println("oldClassNode : " + oldLambdaClassNode.name);
                            }
                        }
                    }
                }

//                JavaUtils.printList();
            }
            System.err.println();


//            Config.classPool.get()

            // Used to ensure that each spun class name is unique

        }
        System.err.println("CheckCodeChangesFix: fix lambda ");
        System.err.println("===fix lambda end     ");
        System.err.println();
    }



}
