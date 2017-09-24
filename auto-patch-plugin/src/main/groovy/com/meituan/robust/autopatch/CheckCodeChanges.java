package com.meituan.robust.autopatch;

import com.meituan.robust.Constants;
import com.meituan.robust.change.ChangeLog;
import com.meituan.robust.change.RobustChangeInfo;
import com.meituan.robust.change.RobustCodeChangeChecker;
import com.meituan.robust.utils.AnonymousLambdaUtils;
import com.meituan.robust.utils.OuterClassMethodAnonymousClassUtils;
import com.meituan.robust.utils.ProguardUtils;
import com.meituan.robust.utils.RobustLog;

import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javassist.CtClass;
import javassist.CtField;
import javassist.bytecode.AccessFlag;

/**
 * Created by hedingxu on 17/7/28.
 */

public class CheckCodeChanges {
    public static HashSet<String> get_ShouldAddInitRobustPatchMethod_ClassesFromJar(JarFile newJar){
        //只考虑newClass 与 changedClass即可，删除的class不用管（不需要处理)
        // go through the jar file, entry by entry.
        List<String> hotfixPackageList = Config.hotfixPackageList;
        List<String> exceptPackageList = Config.exceptPackageList;

        HashSet<String> classNames = new HashSet<String>();
        Enumeration<JarEntry> jarEntries = newJar.entries();
        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            if (null == jarEntry){
                continue;
            }
            String className = jarEntry.getName();

            if (!className.endsWith(".class")) {
                continue;
            }

            // is R.class or R$xml.class
            boolean isRSubClass = false;
            int index = className.lastIndexOf('/');
            if (index != -1 &&
                    className.startsWith("R$", index + 1)) {
                isRSubClass = true;
            }
            String RClassStr2 = "R.class";
            if (isRSubClass || className.endsWith(RClassStr2) || className.endsWith("/r.class") || className.endsWith(".r.class")) {
//                System.err.println("is R dot class : " + className);
                continue;
            }

            String dotClassName = className.replace(".class", "").replace("/", ".");

            // is in except package list
            if (null != exceptPackageList) {
//                className.startsWith("com/meituan/robust")
                boolean isExceptPackage = false;
                for (String exceptPackage : exceptPackageList) {
                    isExceptPackage = ProguardUtils.isInExceptPackage(dotClassName,exceptPackage.trim());
                    if (isExceptPackage){
                        break;
                    }
                }
                if (isExceptPackage) {
                    continue;
                }
            }

            // is in except package list
            if (null != hotfixPackageList) {
                for (String packageName : hotfixPackageList) {
                    if (ProguardUtils.isInHotfixPackage(dotClassName,packageName.trim())) {
                        //yes it is , class in hotfix package list
                        if (AnonymousLambdaUtils.isAnonymousInnerClass_$1(dotClassName)
                                || AnonymousLambdaUtils.isAnonymousInnerClass_$$Lambda$1(dotClassName)
                                || AnonymousLambdaUtils.isAnonymousInnerClass_$AjcClosure1(dotClassName))
                        {
                            // TODO: 17/9/24  delete
                            RobustLog.log("dot need add robustPatchInit method : " + dotClassName);
                        } else {
                            classNames.add(dotClassName);
                        }
                    }
                }
            }
        }

        return classNames;
    }

    public static void processChangedJar(JarFile backupJar, JarFile newJar, List<String> hotfixPackageList, List<String> exceptPackageList)
            throws IOException {
        Config.oldJar = backupJar;
        Config.newJar = newJar;
        Map<String, JarEntry> backupEntries = new HashMap<String, JarEntry>();
        Map<String, JarEntry> newEntries = new HashMap<String, JarEntry>();
        Enumeration<JarEntry> backupJarEntries = backupJar.entries();
        while (backupJarEntries.hasMoreElements()) {
            JarEntry jarEntry = backupJarEntries.nextElement();
            backupEntries.put(jarEntry.getName(), jarEntry);
        }
        //只考虑newClass 与 changedClass即可，删除的class不用管（不需要处理)
        // go through the jar file, entry by entry.
        Enumeration<JarEntry> jarEntries = newJar.entries();
        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            String className = jarEntry.getName();

            newEntries.put(className,jarEntry);
            if (!className.endsWith(".class")) {
                continue;
            }

            // is R.class or R$xml.class
            boolean isRSubClass = false;
            int index = className.lastIndexOf('/');
            if (index != -1 &&
                    className.startsWith("R$", index + 1)) {
                isRSubClass = true;
            }
            String RClassStr2 = "R.class";
            if (isRSubClass || className.endsWith(RClassStr2) || className.endsWith("/r.class") || className.endsWith(".r.class")) {
//                System.err.println("is R dot class : " + className);
                continue;
            }

            String dotClassName = className.replace(".class", "").replace("/", ".");

            // is in except package list
            if (null != exceptPackageList) {
//                className.startsWith("com/meituan/robust")
                boolean isExceptPackage = false;
                for (String exceptPackage : exceptPackageList) {
                    isExceptPackage = ProguardUtils.isInExceptPackage(dotClassName,exceptPackage);
                    if (isExceptPackage) {
                        break;
                    }
                }
                if (isExceptPackage) {
                    continue;
                }
            }

            //这里是混淆之后的className
            // is in except package list
            if (null != hotfixPackageList) {
                for (String packageName : hotfixPackageList) {
                    boolean isInHotfixPackage = ProguardUtils.isInHotfixPackage(dotClassName,packageName);
//                            dotClassName.startsWith(packageName.trim()) || dotClassName.startsWith(packageName.trim().replace(".", File_SEPARATOR));
                    if (isInHotfixPackage) {
                        //yes it is , class in hotfix package list

                        //start

                        JarEntry backupEntry = backupEntries.get(jarEntry.getName());
                        if (backupEntry != null) {
                            byte[] oldClassBytes =
                                    new RobustCodeChangeChecker.ClassBytesJarEntryProvider(backupJar, backupEntry).load();

                            byte[] newClassBytes =
                                    new RobustCodeChangeChecker.ClassBytesJarEntryProvider(newJar, jarEntry).load();

                            ClassNode oldClassNode = RobustCodeChangeChecker.getClassNode(oldClassBytes);
                            ClassNode newClassNode = RobustCodeChangeChecker.getClassNode(newClassBytes);

                            RobustChangeInfo.ClassChange classChange =
                                    RobustCodeChangeChecker.diffClass(oldClassNode
                                            , newClassNode);

                            if (null == classChange) {
                            } else {
                                if (null == classChange.fieldChange && null == classChange.methodChange) {

                                } else {
                                    String dotClassNameTemp = className.replace(".class", "").replace("/", ".");
                                    //如果lambda表达式&匿名内部类的变更，当做是新增的
                                    if (AnonymousLambdaUtils.isAnonymousInnerClass_$1(dotClassNameTemp)){
                                        ClassNode anonymousClassNode = newClassNode;
                                        OuterClassMethodAnonymousClassUtils.recordOuterClassMethod(anonymousClassNode,newJar);

                                        RobustChangeInfo.changeClasses.add(classChange);
                                        if (!Config.modifiedAnonymousClassNameList.contains(dotClassNameTemp)){
                                            Config.modifiedAnonymousClassNameList.add(dotClassNameTemp);
                                        }
                                    } else if (AnonymousLambdaUtils.isAnonymousInnerClass_$$Lambda$1(dotClassNameTemp)){
//                                        ClassNode lambdaClassNode = newClassNode;
//                                        OuterClassMethodLambdaClassUtils.recordOuterClassMethod(lambdaClassNode);

                                        RobustChangeInfo.changeClasses.add(classChange);
                                        if (!Config.modifiedLambdaClassNameList.contains(dotClassNameTemp)){
                                            Config.modifiedLambdaClassNameList.add(dotClassNameTemp);
                                        }
                                    } else {
                                        //field change or method change
                                        Config.modifiedClassNameList.add(className.replace(".class", "").replace("/", "."));
                                        RobustChangeInfo.changeClasses.add(classChange);
                                    }
                                }
                            }
                        } else {

                            String dotClassNameTemp = className.replace(".class", "").replace("/", ".");
                            //如果lambda表达式&匿名内部类的变更，当做是新增的
                            if (AnonymousLambdaUtils.isAnonymousInnerClass_$1(dotClassNameTemp)) {
                                RobustChangeInfo.addClasses.add(dotClassNameTemp);
                                if (!Config.modifiedAnonymousClassNameList.contains(dotClassNameTemp)){
                                    Config.modifiedAnonymousClassNameList.add(dotClassNameTemp);
                                }
                            } else if (AnonymousLambdaUtils.isAnonymousInnerClass_$$Lambda$1(dotClassNameTemp)) {
                                RobustChangeInfo.addClasses.add(dotClassNameTemp);
                                if (!Config.modifiedLambdaClassNameList.contains(dotClassNameTemp)){
                                    Config.modifiedLambdaClassNameList.add(dotClassNameTemp);
                                }
                            } else {
                                RobustChangeInfo.addClasses.add(className.replace("/", "."));
                                Config.newlyAddedClassNameList.add(className.replace(".class", "").replace("/", "."));
                            }

//                            //just for test only
//                            if (CheckCodeChanges.isAnonymousInnerClass(className.replace(".class", "").replace(File_SEPARATOR, "."))){
//                                byte[] newClassBytes = new RobustCodeChangeChecker.ClassBytesJarEntryProvider(newJar, jarEntry).load();
//                                ClassNode newClassNode = RobustCodeChangeChecker.getClassNode(newClassBytes);
//                                ClassNode newAnonymousInnerClass = newClassNode;
//                                OuterClassMethodAnonymousClassUtils.recordOuterClassMethod(newAnonymousInnerClass);
//                            }
                        }
                        //end

                    }
                }
            }
        }

        ChangeLog.print();
//        LambdaUtils.handleLambda(newEntries,backupEntries,backupJar,newJar);
    }

    public
    static String setFieldString(CtField field, CtClass patchClass, CtClass sourceClass) {


        CtClass fieldDeclaringClass = field.getDeclaringClass();
        boolean isWriteSuperClassField = patchClass.subclassOf(fieldDeclaringClass);

        boolean isStatic = isStatic(field.getModifiers());
        StringBuilder stringBuilder = new StringBuilder("{");


        String patchClassName = patchClass.getName();
        String originalClassName = sourceClass.getName();
        String declaringClassName = field.getDeclaringClass().getName();
        //静态字段
        if (isStatic) {
//            System.err.println("setFieldString static field " + field.getName() + "  declaringClass   " + declaringClassName);

            if (declaringClassName.equals(patchClassName)) {
                //如果是本patch类的field
                //如果是新增的字段，需要重新处理一下
                stringBuilder.append("$_ = $proceed($$);");
                stringBuilder.append(Constants.ROBUST_UTILS_FULL_NAME + ".setStaticFieldValue(\"" + field.getName() + "\"," + originalClassName + ".class,$1);");
            } else if (declaringClassName.equals(originalClassName)) {
                //如果是本patch类的field
                stringBuilder.append("$_ = $proceed($$);");
                stringBuilder.append(Constants.ROBUST_UTILS_FULL_NAME + ".setStaticFieldValue(\"" + field.getName() + "\"," + patchClassName + ".class,$1);");
            } else if (isWriteSuperClassField) {
                stringBuilder.append("$_ = $proceed($$);");
                //如果是父类的field,静态字段无需处理
            } else if (AccessFlag.isPackage(field.getModifiers())) {
                //package
//                stringBuilder.append("$_ = $proceed($$);");
                stringBuilder.append(Constants.ROBUST_UTILS_FULL_NAME + ".setStaticFieldValue(\"" + field.getName() + "\"," + declaringClassName + ".class,$1);");
            } else {
                //保持原样
                stringBuilder.append("$_ = $proceed($$);");
            }

            if (Constants.isLogging) {
                stringBuilder.append("  android.util.Log.d(\"robust\",\"set static  value is \" +\"" + (field.getName()) + " ${getCoutNumber()}\");");
            }
        } else {
            //非静态字段
//            System.err.println("setFieldString field " + field.getName() + "  declaringClass   " + declaringClassName);

            if (declaringClassName.equals(patchClassName)) {
                //如果是新增的字段，需要重新处理一下
                //如果是本patch类的field
                stringBuilder.append("$_ = $proceed($$);");

                stringBuilder.append("{");
                stringBuilder.append(originalClassName + " instance;");
                stringBuilder.append("instance=((" + patchClassName + ")$0)." + Constants.ORIGINCLASS + ";");
                stringBuilder.append(Constants.ROBUST_UTILS_FULL_NAME + ".setFieldValue(\"" + (field.getName()) + "\",instance,$1," + originalClassName + ".class);");
                stringBuilder.append("}");
//                stringBuilder.append(Constants.ROBUST_UTILS_FULL_NAME + ".setFieldValue(\"" + field.getName() + "\"," + originalClassName + ".class,$1);");
            } else if (declaringClassName.equals(originalClassName)) {
                //如果是本patch类的field
                stringBuilder.append("$_ = $proceed($$);");

                stringBuilder.append("{");
                stringBuilder.append(originalClassName + " instance;");
                stringBuilder.append("instance=((" + patchClassName + ")$0)." + Constants.ORIGINCLASS + ";");
                stringBuilder.append(Constants.ROBUST_UTILS_FULL_NAME + ".setFieldValue(\"" + (field.getName()) + "\",instance,$1," + originalClassName + ".class);");
                stringBuilder.append("}");

//                stringBuilder.append(Constants.ROBUST_UTILS_FULL_NAME + ".setFieldValue(\"" + field.getName() + "\"," + patchClassName + ".class,$1);");
            } else if (isWriteSuperClassField) {
                stringBuilder.append("$_ = $proceed($$);");

                stringBuilder.append("{");
                stringBuilder.append(originalClassName + " instance;");
                stringBuilder.append("instance = ((" + patchClassName + ")$0)." + Constants.ORIGINCLASS + ";");
                stringBuilder.append(Constants.ROBUST_UTILS_FULL_NAME + ".setFieldValue(\"" + (field.getName()) + "\",instance,$1," + declaringClassName + ".class);");
                stringBuilder.append("}");

//                stringBuilder.append(Constants.ROBUST_UTILS_FULL_NAME + ".setFieldValue(\"" + field.getName() + "\"," + originalClassName + ".class,$1);");
            } else if (AccessFlag.isPackage(field.getModifiers())) {
                //package
//                stringBuilder.append("$_ = $proceed($$);");
                stringBuilder.append("{");
                stringBuilder.append(declaringClassName + " instance;");
                stringBuilder.append("instance = (" + declaringClassName + ") $0;");
                stringBuilder.append(Constants.ROBUST_UTILS_FULL_NAME + ".setFieldValue(\"" + (field.getName()) + "\",instance,$1," + declaringClassName + ".class);");
                stringBuilder.append("}");

            } else {
                //保持原样
                stringBuilder.append("$_ = $proceed($$);");
            }

//            if (Constants.isLogging) {
//                stringBuilder.append("  android.util.Log.d(\"robust\",\"set static  value is \" +\"" + (field.getName()) + " ${getCoutNumber()}\");");
//            }
//
//            if (Constants.isLogging) {
//                stringBuilder.append("  android.util.Log.d(\"robust\",\"set value is \" + \"" + (field.getName()) + "    ${getCoutNumber()}\");");
//            }
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    static boolean isStatic(int modifiers) {
        return (modifiers & AccessFlag.STATIC) != 0;
    }
}
