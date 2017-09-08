package com.meituan.robust.autopatch;

import com.meituan.robust.Constants;
import com.meituan.robust.change.ChangeLog;
import com.meituan.robust.change.RobustChangeInfo;
import com.meituan.robust.change.RobustCodeChangeChecker;
import com.meituan.robust.utils.RobustProguardMapping;

import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javassist.CtClass;
import javassist.CtField;
import javassist.bytecode.AccessFlag;

import static com.meituan.robust.Constants.File_SEPARATOR;

/**
 * Created by hedingxu on 17/7/28.
 */

public class CheckCodeChanges {
//    public static void main(String[] args) throws IOException, NotFoundException, CannotCompileException {
//        ClassPool classPool = new ClassPool();
//        String androidJar = "/Users/hedingxu/android-sdk-mac_x86/platforms/android-25/android.jar";
//        String androidCompatJar = "/Users/hedingxu/robust-github/Robust/app/build/intermediates/exploded-aar/com.android.support/appcompat-v7/25.1.0/jars/classes.jar";
//        classPool.appendClassPath("/Users/hedingxu/robust-github/Robust/app/build/intermediates/transforms/robust/release/jars/1/1f/main.jar");
////        String classDir = "/Users/hedingxu/robust-github/Robust/app/robust/retrolambda/release";
////        String autopatchDir = "/Users/hedingxu/robust-github/Robust/autopatchbase/build/classes/main";
//        String patchClassDir = "/Users/hedingxu/robust-github/Robust/app/robust/";
//        try {
//            classPool.appendClassPath(androidJar);
//            classPool.appendClassPath(androidCompatJar);
//
////            classPool.appendClassPath(classDir);
////            classPool.appendClassPath(autopatchDir);
////            classPool.appendClassPath("/Users/hedingxu/robust-github/Robust/patch/build/intermediates/classes/release");
//        } catch (NotFoundException e) {
//            e.printStackTrace();
//        }
//
//        CtClass sourceClass1 = null;
//        try {
//            sourceClass1 = classPool.get("com.meituan.sample.MainActivity");
//        } catch (NotFoundException e) {
//            e.printStackTrace();
//        }
//        sourceClass1.setName(sourceClass1.getName() + "$RobustPatch");
//        try {
//
//            sourceClass1.writeFile(patchClassDir);
//        } catch (CannotCompileException e) {
//            e.printStackTrace();
//        }
//
//
//        classPool.appendClassPath(patchClassDir);
//
//        final CtClass sourceClass = classPool.get("com.meituan.sample.MainActivity");
//        final CtClass patchClass = classPool.get("com.meituan.sample.MainActivity$RobustPatch");
//
//        patchClass.defrost();
////        if (true) {
////            CtClass[] ctClasses = anonymousInnerClass.getNestedClasses();
////            for (CtClass ctClass : ctClasses) {
//////              case: com.dianping.ad.view.BannerAdView$$Lambda$1
//////              case: com.meituan.sample.MainActivity$3
//////              case: com.meituan.android.baby.poi.agent.BabyPoiPromoAgent$AjcClosure1 匿名内部类
//////              case: android.support.design.widget.AppBarLayout$Behavior$SavedState$1 跟普通匿名内部类一样
//////              case: android.support.design.widget.BaseTransientBottomBar$5$1 匿名内部类的匿名内部类
////                String nestedClassName = ctClass.getName();
////                String shortClassName = nestedClassName.replace(anonymousInnerClass.getName(), "");
////                String numberStr = shortClassName.replace("$$Lambda", "");
//////                numberStr = numberStr.replace("","");
////                numberStr = numberStr.replace("$", "");
////
////                boolean isAnonymousInnerClass_$1 = false;
////                String patternString = "[0-9]*";
////                {
////                    Pattern pattern = Pattern.compile(patternString);
////                    Matcher matcher = pattern.matcher(numberStr);
////                    boolean matches = matcher.matches();
////                    isAnonymousInnerClass_$1 = matches;
////                }
////                boolean isAjcClosureAnonymousInnerClass = false;
////                {
////                    String ajcClosureAnonymousInnerClass = "AjcClosure" + patternString;
////                    Pattern ajcClosurePattern = Pattern.compile(ajcClosureAnonymousInnerClass);
////                    Matcher ajcClosureMatcher = ajcClosurePattern.matcher(numberStr);
////                    boolean ajcClosureMatches = ajcClosureMatcher.matches();
////                    isAjcClosureAnonymousInnerClass = ajcClosureMatches;
////                }
////
////                if (isAnonymousInnerClass_$1 || isAjcClosureAnonymousInnerClass) {
////                    System.err.println("isAnonymousInnerClass_$1 :" + ctClass.getName());
////                } else {
////                    System.err.println("notAnonymousInnerClass :" + ctClass.getName());
////                }
////            }
////            return;
////        }
//
////        JavaUtils.addField_OriginClass(outerClass, anonymousInnerClass);
//
//        CtField originField = new CtField(sourceClass, ORIGINCLASS, patchClass);
//        originField.setModifiers(AccessFlag.setPublic(originField.getModifiers()));
//
//        //不支持interface，所以不用管interface这种情况
//        patchClass.setModifiers(AccessFlag.clear(patchClass.getModifiers(), AccessFlag.ABSTRACT));
//
////        匿名内部类的换不换？
////        new CallBack(this){
////
////        }
//
////        匿名内部类
////        考虑这些this的事情，需要处理this参数，哪些this可以替换成OriginClass ,哪些不能替换??
////        MainActivity$RobustPatch.class
////        private void runRobust() {
////            this.testAdd("nn");
////            (new PatchExecutor(this.getApplicationContext(), new PatchManipulateImp(), new Callback(this))).start();
////        }
////      // TODO: 17/8/7 如果是匿名内部类，传进去的参数需要保持不变，其他的全部换成originalClass
//
//        for (final CtMethod method : patchClass.getDeclaredMethods()) {
//            if (!Config.addedSuperMethodList.contains(method) && !method.getName().startsWith(Constants.ROBUST_PUBLIC_SUFFIX)) {
//                method.instrument(
//                        new ExprEditor() {
//                            public void edit(FieldAccess f) throws CannotCompileException {
//
//                                if (true) {
//                                    return;
//                                }
//
//                                if (Config.newlyAddedClassNameList.contains(f.getClassName())) {
//                                    return;
//                                }
//                                try {
//                                    if (f.isWriter()) {
//
//                                        f.replace(setFieldString(f.getField(), patchClass, sourceClass));
//                                    }
//                                } catch (NotFoundException e) {
//                                    e.printStackTrace();
//                                    throw new RuntimeException(e.getMessage());
//                                }
//                            }
//
//                            @Override
//                            public void edit(NewExpr e) throws CannotCompileException {
//                                //inner class in the patched class ,not all inner class
//                                if (Config.newlyAddedClassNameList.contains(e.getClassName()) || Config.noNeedReflectClassSet.contains(e.getClassName())) {
//                                    return;
//                                }
//                                //需要处理参数为this的情况
//
//                                //其他情况不用处理(// TODO: 17/8/3 需要将所有新增的class都设置成public的
//                                //需要处理 package访问属性的method,直接在插桩的时候改成public好了(同样的，把那个字段的也改了，这里就可以少很多代码了）
//                            }
//
//                            @Override
//                            public void edit(MethodCall m) throws CannotCompileException {
//
//                                //super方法使用Assistant class解决
//                                if (m.isSuper()) {
//                                    System.err.println(m.getClassName() + "," + m.getMethodName() + ", is super: " + m.isSuper());
//                                    return;
//                                }
//                                if (true) {
//                                    try {
//                                        CtClass methodTargetClass = m.getMethod().getDeclaringClass();
//
////                                        System.err.println("is sub class of  " + methodTargetClass.getName() + ", " + anonymousInnerClass.getName());
//                                        if (sourceClass.subclassOf(methodTargetClass)) {
//                                            //// TODO: 17/8/7 判断是否父类方法 或者本类方法
//                                            System.err.println("*** " + m.getMethod().getName() + " , " + sourceClass.getName() + " is sub class Of : " + methodTargetClass.getName());
//
//                                        } else {
//                                            System.err.println("" + methodTargetClass.getName() + "#" + m.getMethod().getName() /* + anonymousInnerClass.getName() + " is not sub class of "*/);
//
//                                        }
//                                    } catch (NotFoundException e) {
//                                        e.printStackTrace();
//
//                                        System.err.println("error: " + m.getClassName() + "," + m.getClass().getName() + ", ");
//                                    }
////                                    System.err.println(m.getClassName() + "," + m.getMethodName() + "");
//                                    return;
//                                }
//                                //如果是新增的class就不用替换了
//                                try {
//                                    if (Config.newlyAddedClassNameList.contains(m.getMethod().getDeclaringClass().getName())) {
//                                        return;
//                                    }
//                                } catch (NotFoundException e) {
//                                    e.printStackTrace();
//                                }
//
//
//                                // 大部分情况下是需要替换this为originalClass的
//                                {
//                                    try {
//                                        boolean isContainThis = false;
//                                        m.getMethod().getReturnType();//// TODO: 17/8/7 处理builder 除了匿名内部类，都把this换成originalClass
//                                        CtClass[] paramTypes = m.getMethod().getParameterTypes();
//                                        StringBuilder stringBuilder = new StringBuilder();
//
//                                        if (null != paramTypes) {
//                                            int index = 0;
//                                            for (CtClass ctClass : paramTypes) {
//                                                index++;
//                                                //MainActivityPatch contains MainActivity and MainActivityPatch
//                                                if (ctClass.getName().startsWith(sourceClass.getName())) {
//                                                    //this ????// TODO: 17/8/7
//                                                    isContainThis = true;
//                                                    stringBuilder.append("$" + index + "= $" + index + "." + Constants.ORIGINCLASS + ";");
//                                                }
//                                            }
//
//                                        }
//
//                                        if (isContainThis) {
//                                            stringBuilder.append("$_ = $proceed($$);");
//                                            stringBuilder.toString();
//                                        }
//
//                                    } catch (NotFoundException e) {
//                                        e.printStackTrace();
//                                    }
//
//                                }
//
////                                try {
////                                    if (m.getMethod().getName().contains("getApplicationContext")) {
////                                        System.err.println("getApplicationContext method");
////
////                                    }
////                                    String methodDeclaringClassName = m.getMethod().getDeclaringClass().getName();
////                                    if (isAnonymousInnerClass_$1(methodDeclaringClassName)) {
////                                        HashSet<String> anonymousInnerClasses = changedClassAndItsAnonymousInnerClass.get(outerClass.getName());
////                                        if (anonymousInnerClasses == null) {
////                                            anonymousInnerClasses = new LinkedHashSet<String>();
////                                        }
////                                        anonymousInnerClasses.addClasses(methodDeclaringClassName);
////                                        //记录需要变更的匿名内部类，需要处理匿名内部类里面的匿名内部类?
////                                        changedClassAndItsAnonymousInnerClass.put(outerClass.getName(), anonymousInnerClasses);
////                                        return;
////                                    }
////                                } catch (NotFoundException e) {
////                                    e.printStackTrace();
////                                }
//
//                                //可能会出现一个class，被修改多次，这时候如何预警？？？
//                                //有一种问题，就是
//                                // class A {
//                                //   class InnerB{
//                                //    //如果访问了A的私有方法，则A可能会出现Method的新增(access$200),对这部分的改动需要过滤掉，使用反射处理
//                                //   }
//                                // }
////                                if (m.getMethodName().contains("access$")) {
////                                    //method contain
////
////                                    m.replace(ReflectUtils.getNewInnerClassString(m.getSignature(), outerClass.getName(), ReflectUtils.isStatic(method.getModifiers()), /*getClassValue(*/m.getClassName()));
////                                    return;
////                                }
//
//                                //处理内联？  proguard之后做，保存之前打包的jar，与现在对比;
//                                //就不用处理内联了?
//                                //做合成？
//                                //old.jar(same diff1)
//                                //new.jar(same diff2)
//                                //changed.jar(same diff1/diff2)
//                                //combined.jar(same
////                                try {
////                                    if (!repalceInlineMethod(m, method, false)) {
////                                        Map memberMappingInfo = new HashMap();
////                                        m.replace(ReflectUtils.getMethodCallString(m, memberMappingInfo, outerClass, ReflectUtils.isStatic(method.getModifiers()), false));
////                                    }
////                                } catch (Throwable e) {
////                                    e.printStackTrace();
////                                }
//                            }
//                        });
//            }
//        }
//
//        patchClass.writeFile(patchClassDir);
//    }


    public static HashSet<String> getTargetClassesFromJar(JarFile newJar){
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

            String dotClassName = className.replace(".class", "").replace(File_SEPARATOR, ".");

            // is in except package list
            if (null != exceptPackageList) {
//                className.startsWith("com/meituan/robust")
                boolean isExceptPackage = false;
                for (String exceptPackage : exceptPackageList) {
                    if (dotClassName.startsWith(exceptPackage.trim()) || dotClassName.startsWith(exceptPackage.trim().replace(".", File_SEPARATOR))) {
                        isExceptPackage = true;
                    }
                }
                if (isExceptPackage) {
                    continue;
                }
            }

            // is in except package list
            if (null != hotfixPackageList) {
                for (String packageName : hotfixPackageList) {
                    if (dotClassName.startsWith(packageName.trim()) || dotClassName.startsWith(packageName.trim().replace(".", File_SEPARATOR))) {
                        //yes it is , class in hotfix package list
                        classNames.add(dotClassName);
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

            String dotClassName = className.replace(".class", "").replace(File_SEPARATOR, ".");

            // is in except package list
            if (null != exceptPackageList) {
//                className.startsWith("com/meituan/robust")
                boolean isExceptPackage = false;
                for (String exceptPackage : exceptPackageList) {
                    if (dotClassName.startsWith(exceptPackage.trim()) || dotClassName.startsWith(exceptPackage.trim().replace(".", File_SEPARATOR))) {
                        isExceptPackage = true;
                    }
                }
                if (isExceptPackage) {
                    continue;
                }
            }

            // is in except package list
            if (null != hotfixPackageList) {
                for (String packageName : hotfixPackageList) {
                    if (dotClassName.startsWith(packageName.trim()) || dotClassName.startsWith(packageName.trim().replace(".", File_SEPARATOR))) {
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
                                    //field change or method change
                                    Config.modifiedClassNameList.add(className.replace(".class", "").replace(File_SEPARATOR, "."));
                                    RobustChangeInfo.changeClasses.add(classChange);
//                                  //todo 这里File_SEPARATOR需要考虑windows里面的是否兼容
                                    if (CheckCodeChanges.isAnonymousInnerClass_$1(className.replace(".class", "").replace(File_SEPARATOR, "."))){
                                        ClassNode newAnonymousInnerClass = newClassNode;
                                        AnonymousClassOuterClassMethodUtils.recordOuterClassMethod(newAnonymousInnerClass);
                                    }

                                    if (CheckCodeChanges.isAnonymousInnerClass_$$Lambda$1(className.replace(".class", "").replace(File_SEPARATOR, "."))){
                                        //// TODO: 17/9/7 记录改了lambda表达式的所包含的方法
                                    }
                                }
                            }
                        } else {
                            RobustChangeInfo.addClasses.add(className.replace(File_SEPARATOR, "."));
                            Config.newlyAddedClassNameList.add(className.replace(".class", "").replace(File_SEPARATOR, "."));

//                            //todo just for test only
//                            if (CheckCodeChanges.isAnonymousInnerClass(className.replace(".class", "").replace(File_SEPARATOR, "."))){
//                                byte[] newClassBytes = new RobustCodeChangeChecker.ClassBytesJarEntryProvider(newJar, jarEntry).load();
//                                ClassNode newClassNode = RobustCodeChangeChecker.getClassNode(newClassBytes);
//                                ClassNode newAnonymousInnerClass = newClassNode;
//                                AnonymousClassOuterClassMethodUtils.recordOuterClassMethod(newAnonymousInnerClass);
//                            }
                        }
                        //end

                    }
                }
            }
        }

        ChangeLog.print();
//        RetrolambdaUtils.handleLambda(newEntries,backupEntries,backupJar,newJar);
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
                //如果是新增的字段，需要重新处理一下 // TODO: 17/8/2
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
                //如果是新增的字段，需要重新处理一下 // TODO: 17/8/2
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


    public static void main(String[] args) {
        String case1 = "com.meituan.sample.MainActivity$3";
        String case2 = "com.dianping.ad.view.BannerAdView$$Lambda$1";
        String case3 = "com.meituan.android.baby.poi.agent.BabyPoiPromoAgent$AjcClosure1";
        String case4 = "android.support.design.widget.AppBarLayout$Behavior$SavedState$1";
        String case5 = "android.support.design.widget.BaseTransientBottomBar$5$1";
        List<String> cases = new ArrayList<>();
        cases.add(case1);
        cases.add(case2);
        cases.add(case3);
        cases.add(case4);
        cases.add(case5);
        for (String case11 : cases) {
            System.err.println();
            System.err.println(case11 + " like $1 : " + isAnonymousInnerClass_$1(case11));
            System.err.println(case11 + " like $$Lambda$1 : " + isAnonymousInnerClass_$$Lambda$1(case11));
            System.err.println(case11 + " like $AjcClosure1 : " + isAnonymousInnerClass_$AjcClosure1(case11));
        }
    }

    public static boolean isAnonymousInnerClass_$AjcClosure1(String className) {
        /*            case: com.dianping.ad.view.BannerAdView$$Lambda$1 第2种case
              case: com.meituan.sample.MainActivity$3 第1种case
              case: com.meituan.android.baby.poi.agent.BabyPoiPromoAgent$AjcClosure1 第3种case
              case: android.support.design.widget.AppBarLayout$Behavior$SavedState$1 跟普通匿名内部类一样
              case: android.support.design.widget.BaseTransientBottomBar$5$1 匿名内部类的匿名内部类
*/
        String newExprClassName = className;
        if (RobustProguardMapping.isProguard()) {
            newExprClassName = RobustProguardMapping.getUnProguardName(className);
        }
        if (newExprClassName.contains("$AjcClosure")) {
            String[] splits = newExprClassName.split("\\$");
            int length = splits.length;
            String checkStr = splits[length - 1];

            String patternString = "AjcClosure[0-9]*";
            boolean isAnonymousInnerClass = false;
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(checkStr);
            boolean matches = matcher.matches();
            isAnonymousInnerClass = matches;
            return isAnonymousInnerClass;

        }
        return false;
    }

    public static boolean isAnonymousInnerClass_$$Lambda$1(String className) {
        /*            case: com.dianping.ad.view.BannerAdView$$Lambda$1 第2种case
              case: com.meituan.sample.MainActivity$3 第1种case
              case: com.meituan.android.baby.poi.agent.BabyPoiPromoAgent$AjcClosure1 第3种case
              case: android.support.design.widget.AppBarLayout$Behavior$SavedState$1 跟普通匿名内部类一样
              case: android.support.design.widget.BaseTransientBottomBar$5$1 匿名内部类的匿名内部类
*/
        String newExprClassName = className;
        if (RobustProguardMapping.isProguard()) {
            newExprClassName = RobustProguardMapping.getUnProguardName(className);
        }
        if (newExprClassName.contains("$$Lambda$")) {
            String[] splits = newExprClassName.split("\\$");
            int length = splits.length;
            String checkStr = splits[length - 1];

            boolean isAnonymousInnerClass = false;
            String patternString = "[0-9]*";
            {
                Pattern pattern = Pattern.compile(patternString);
                Matcher matcher = pattern.matcher(checkStr);
                boolean matches = matcher.matches();
                isAnonymousInnerClass = matches;
            }

            return isAnonymousInnerClass;
        }
        return false;
    }

    public static boolean isAnonymousInnerClass_$1(String className) {
/*            case: com.dianping.ad.view.BannerAdView$$Lambda$1 第2种case
              case: com.meituan.sample.MainActivity$3 第1种case
              case: com.meituan.android.baby.poi.agent.BabyPoiPromoAgent$AjcClosure1 第3种case
              case: android.support.design.widget.AppBarLayout$Behavior$SavedState$1 跟普通匿名内部类一样
              case: android.support.design.widget.BaseTransientBottomBar$5$1 匿名内部类的匿名内部类
*/
        if (isAnonymousInnerClass_$$Lambda$1(className)){
            return false;
        }
        if (isAnonymousInnerClass_$AjcClosure1(className)){
            return false;
        }

        String newExprClassName = className;
        if (RobustProguardMapping.isProguard()) {
            newExprClassName = RobustProguardMapping.getUnProguardName(className);
        }
        if (newExprClassName.contains("$")) {
            String[] splits = newExprClassName.split("\\$");
            int length = splits.length;
            String checkStr = splits[length - 1];


            boolean isAnonymousInnerClass = false;
            String patternString = "[0-9]*";
            {
                Pattern pattern = Pattern.compile(patternString);
                Matcher matcher = pattern.matcher(checkStr);
                boolean matches = matcher.matches();
                isAnonymousInnerClass = matches;
            }
            boolean isAjcClosureAnonymousInnerClass = false;
            {
                String ajcClosureAnonymousInnerClass = "AjcClosure" + patternString;
                Pattern ajcClosurePattern = Pattern.compile(ajcClosureAnonymousInnerClass);
                Matcher ajcClosureMatcher = ajcClosurePattern.matcher(checkStr);
                boolean ajcClosureMatches = ajcClosureMatcher.matches();
                isAjcClosureAnonymousInnerClass = ajcClosureMatches;
            }

            if (isAjcClosureAnonymousInnerClass || isAnonymousInnerClass) {

                return true;
            }
        }
        return false;
    }

    public static boolean isAnonymousInnerClass(String className){
        return isAnonymousInnerClass_$1(className);
//        return
//                isAnonymousInnerClass_$$Lambda$1(className)
//                || isAnonymousInnerClass_$1(className)
//                || isAnonymousInnerClass_$AjcClosure1(className);
    }

}
