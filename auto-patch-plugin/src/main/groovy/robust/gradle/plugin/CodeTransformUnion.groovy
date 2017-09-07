package robust.gradle.plugin

import com.meituan.robust.Constants
import com.meituan.robust.autopatch.*
import com.meituan.robust.autopatch.AnonymousClassOuterClassMethodUtils
import com.meituan.robust.autopatch.innerclass.anonymous.AnonymousInnerClassTransform
import com.meituan.robust.change.RobustChangeInfo
import com.meituan.robust.common.FileUtil
import com.meituan.robust.utils.JavaUtils
import javassist.*
import javassist.bytecode.AccessFlag
import javassist.expr.ExprEditor
import javassist.expr.MethodCall
import org.gradle.api.Project

import java.util.jar.JarFile
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Created by mivanzhang on 16/7/21.
 *
 * AutoPatchTransform generate patch dex
 */
public class CodeTransformUnion {
    private static String dex2SmaliCommand;
    private static String smali2DexCommand;
    private static String jar2DexCommand;
    public static String ROBUST_DIR;

    public static initConfig(Project project) {
        //clear
        NameManger.init();
        InlineClassFactory.init();
        ReadMapping.init();
        Config.init();

        ROBUST_DIR = "${project.projectDir}${File.separator}robust${File.separator}"
        def baksmaliFilePath = "${ROBUST_DIR}${Constants.LIB_NAME_ARRAY[0]}"
        def smaliFilePath = "${ROBUST_DIR}${Constants.LIB_NAME_ARRAY[1]}"
        def dxFilePath = "${ROBUST_DIR}${Constants.LIB_NAME_ARRAY[2]}"
        Config.robustGenerateDirectory = "${project.buildDir}" + File.separator + "$Constants.ROBUST_GENERATE_DIRECTORY" + File.separator;
        dex2SmaliCommand = "  java -jar ${baksmaliFilePath} -o classout" + File.separator + "  $Constants.CLASSES_DEX_NAME";
        smali2DexCommand = "   java -jar ${smaliFilePath} classout" + File.separator + " -o " + Constants.PATACH_DEX_NAME;
        jar2DexCommand = "   java -jar ${dxFilePath} --dex --output=$Constants.CLASSES_DEX_NAME  " + Constants.ZIP_FILE_NAME;
        ReadXML.readXMl(project.projectDir.path);
        Config.methodMap = JavaUtils.getMapFromZippedFile(project.projectDir.path + Constants.METHOD_MAP_PATH)
    }

    public static void transform(Project project) throws IOException {
        long startTime = System.currentTimeMillis()
        System.err.println("================autoPatch start================")
        copyJarToRobust()
        autoPatch(project)
//        JavaUtils.removeJarFromLibs()
        if (Config.debug) {
            JavaUtils.printMap2File(Config.methodMap, new File(project.projectDir.path + Constants.METHOD_MAP_PATH + ".txt"))
            System.err.println("================method signature to method id map unzip to file ================");
        }
        long cost = (System.currentTimeMillis() - startTime) / 1000
        System.err.println("autoPatch cost " + cost + " second")
        throw new RuntimeException("auto patch end successfully")
    }

    static def copyJarToRobust() {
        File targetDir = new File(ROBUST_DIR);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        for (String libName : Constants.LIB_NAME_ARRAY) {
            InputStream inputStream = JavaUtils.class.getResourceAsStream("/libs/" + libName);
            if (inputStream == null) {
                System.out.println("Warning!!!  Did not find " + libName + " ，you must addClasses it to your project's libs ");
                continue;
            }
            File inputFile = new File(ROBUST_DIR + libName);
            try {
                OutputStream inputFileOut = new FileOutputStream(inputFile);
                JavaUtils.copy(inputStream, inputFileOut);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Warning!!! " + libName + " copy error " + e.getMessage());

            }
        }
    }

    public static autoPatch(Project project) {
        project.android.bootClasspath.each {
            Config.classPool.appendClassPath((String) it.absolutePath)
        }

        project.android.bootClasspath.each {
            Config.oldClassPool.appendClassPath((String) it.absolutePath)
        }

        File buildDir = project.getBuildDir();
        String patchPath = buildDir.getAbsolutePath() + File.separator + Constants.ROBUST_GENERATE_DIRECTORY + File.separator;
//        clearPatchPath(patchPath);

        //1. get last class.jar
        File oldMainJarFile = new File(ROBUST_DIR, Config.ROBUST_TRANSFORM_MAIN_JAR)
        File oldProGuardJarFile = new File(ROBUST_DIR, Config.ROBUST_PROGUARD_MAIN_JAR)

        //2. get current classes  todo 如果是proguard之后，我们插了代码，需要做兼容
        //拷贝未插桩的main.jar start todo move to RobustStoreClassAction 后面需要考虑在混淆后拷贝一下，第一版本暂时不考虑混淆
        File newMainJarFile = new File(patchPath, Config.ROBUST_TRANSFORM_MAIN_JAR)
        File newProGuradJarFile = new File(patchPath, Config.ROBUST_PROGUARD_MAIN_JAR)
        if (newMainJarFile.exists() || newProGuradJarFile.exists()) {
            //如果proguard打开了，就使用proguard的包
            //todo test proguard
            if (newProGuradJarFile.exists()) {
                newMainJarFile = newProGuradJarFile
                oldMainJarFile = oldProGuardJarFile
            }
        } else {
            throw new RuntimeException("please apply plugin: 'robust'")
        }

        //3. is changed
        JarFile originalJarFile = new JarFile(oldMainJarFile);
        JarFile currentJarFile = new JarFile(newMainJarFile);

        //将构造函数转成initRobustPatch函数
        originalJarFile = copyConstructor2InitRobustPatchMethod(project, oldMainJarFile.absolutePath, "old_" + oldMainJarFile.name)
        currentJarFile = copyConstructor2InitRobustPatchMethod(project, newMainJarFile.absolutePath, "new_" + newMainJarFile.name)

        Config.classPool.appendClassPath(currentJarFile.name)

        CheckCodeChanges.processChangedJar(originalJarFile, currentJarFile, Config.hotfixPackageList, Config.exceptPackageList)

//        println("modifiedClassNameList is ：")
//        JavaUtils.printList(Config.modifiedClassNameList)
//
//        println("newlyAddedClassNameList is ：")
//        JavaUtils.printList(Config.newlyAddedClassNameList)
//
//        println("modifiedAnonymousInnerClassNameList is :")
//        JavaUtils.printList(Config.modifiedAnonymousInnerClassNameList)

        println("fix that unchanged lambda class really: ")
        //fix start
        List<String> newModifiedClassNameList = new ArrayList<>();
        for (String className : Config.modifiedClassNameList) {
            if (Config.lambdaUnchangedReallyClassNameList.containsKey(className)) {

            } else {
                newModifiedClassNameList.add(className);
            }
        }
        Config.modifiedClassNameList = newModifiedClassNameList;

        List<String> newlyAddedClassNameList = new ArrayList<>();
        for (String className : Config.newlyAddedClassNameList) {
            if (Config.lambdaUnchangedReallyClassNameList.containsKey(className)) {

            } else {
                newlyAddedClassNameList.add(className);
            }
        }
        Config.newlyAddedClassNameList = newlyAddedClassNameList;

        List<String> modifiedAnonymousInnerClassNameList = new ArrayList<>();
        for (String className : Config.modifiedAnonymousInnerClassNameList) {
            if (Config.lambdaUnchangedReallyClassNameList.containsKey(className)) {

            } else {
                modifiedAnonymousInnerClassNameList.add(className);
            }
        }
        Config.modifiedAnonymousInnerClassNameList = modifiedAnonymousInnerClassNameList;
        //fix end

        println("modifiedClassNameList is ：")
        JavaUtils.printList(Config.modifiedClassNameList)

        println("newlyAddedClassNameList is ：")
        JavaUtils.printList(Config.newlyAddedClassNameList)

        println("modifiedAnonymousInnerClassNameList is :")
        JavaUtils.printList(Config.modifiedAnonymousInnerClassNameList)



        println("convert modifiedAnonymousInnerClassNameList to newAddClassNameList:")
        for (String anonymousClassName : Config.modifiedAnonymousInnerClassNameList) {
            if (Config.newlyAddedClassNameList.contains(anonymousClassName)) {
            } else {
                Config.newlyAddedClassNameList.add(anonymousClassName)
            }
        }

        println("merge anonymousInnerClass 's outer class and method to modifiedClassNameList :")
        for (String anonymousClassName : Config.modifiedAnonymousInnerClassNameList) {
            AnonymousClassOuterClassMethodUtils.OuterMethodInfo outerMethodInfo = AnonymousClassOuterClassMethodUtils.changedAnonymousOuterMethodInfoMap.get(anonymousClassName);
            //如果改的是field = new View.onclickListener ，这里的outerMethodInfo == null

            if (Config.modifiedClassNameList.contains(outerMethodInfo.outerClass)) {
                //修改的class已经包含了匿名内部类改动带来的class改动，还需要记录方法的改动
            } else {
                Config.modifiedClassNameList.add(outerMethodInfo.outerClass)
            }
        }

        for (String modifiedClassName : Config.modifiedClassNameList) {
            CtClass modifiedCtClass = Config.classPool.get(modifiedClassName);
            modifiedCtClass.defrost();
            //todo delete unChanged anonymousInnerClass 8-29
//            Config.newlyAddedClassNameList.addAll(AnonymousInnerClassUtil.getAnonymousInnerClass(modifiedCtClass));
        }

        println("newlyAddedClassNameList is ：")
        JavaUtils.printList(Config.newlyAddedClassNameList)



        if (Config.supportProGuard) {
            ReadMapping.getInstance().initMappingInfo();
        }

        generatePatch(patchPath);

        zipPatchClassesFile()
        executeCommand(jar2DexCommand)
        executeCommand(dex2SmaliCommand)
        com.meituan.robust.utils.SmaliTool.getInstance().dealObscureInSmali();
        executeCommand(smali2DexCommand)
        //package patch.dex to patch.apk
        packagePatchDex2Apk()

        if (Config.isResourceFix) {
            File dexPatchFile = new File(Config.robustGenerateDirectory + Constants.ZIP_FILE_NAME);
            if (dexPatchFile.exists()) {
                Config.patchHasDex = true;
            } else {
                System.err.println("dex patch file does not exists")
                return
            }
        } else {
            deleteTmpFiles()
        }
    }

    public
    static JarFile copyConstructor2InitRobustPatchMethod(Project project, String fullPath, String jarName) {
        ClassPool classPool = new ClassPool()
        project.android.bootClasspath.each {
            classPool.appendClassPath((String) it.absolutePath)
        }
        classPool.appendClassPath(fullPath)

        String jarOutDirectoryPath = Config.robustGenerateDirectory + "constructor" + Constants.File_SEPARATOR + jarName.replace(".jar", "")
        FileUtil.createDirectory(jarOutDirectoryPath)
        FileUtil.unzip(fullPath, jarOutDirectoryPath)

        HashSet<String> classesNameHashSet = CheckCodeChanges.getTargetClassesFromJar(new JarFile(fullPath))
        for (String className : classesNameHashSet) {
            CtClass ctClass = classPool.get(className)
            copyConstructor2Method(ctClass)
            ctClass.writeFile(jarOutDirectoryPath)
        }

        String outJarFilePath = Config.robustGenerateDirectory + "constructor" + Constants.File_SEPARATOR + jarName

        FileUtil.zip(outJarFilePath, jarOutDirectoryPath)

        JarFile outJarFile = new JarFile(outJarFilePath)
        return outJarFile;
    }

    public static zipPatchClassesFile() {
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(Config.robustGenerateDirectory + Constants.ZIP_FILE_NAME));
        zipAllPatchClasses(Config.robustGenerateDirectory + Config.patchPackageName.substring(0, Config.patchPackageName.indexOf(".")), "", zipOut);
        zipOut.close();

    }

    public static zipAllPatchClasses(String path, String fullClassName, ZipOutputStream zipOut) {
        File file = new File(path);
        if (file.exists()) {
            fullClassName = fullClassName + file.name;
            if (file.isDirectory()) {
                fullClassName += File.separator;
                File[] files = file.listFiles();
                if (files.length == 0) {
                    return;
                } else {
                    for (File file2 : files) {
                        zipAllPatchClasses(file2.getAbsolutePath(), fullClassName, zipOut);
                    }
                }
            } else {
                //文件
                zipFile(file, zipOut, fullClassName);
            }
        } else {
            System.err.println("文件不存在!");
        }
    }


    public static void copyConstructor2Method(CtClass patchClas) {

        //临时删除默认的构造函数，这样能够解决修改构造函数的问题
        try {
            CtConstructor[] ctConstructors = patchClas.getConstructors();
            if (null != ctConstructors) {
                for (CtConstructor ctConstructor : ctConstructors) {
                    //                if (ctConstructor.callsSuper()){
//                    //这个构造函数调用了super方法，需要考虑一下特殊处理
//                }
                    CtMethod fakeConstructor = ctConstructor.toMethod(Constants.INIT_ROBUST_PATCH, patchClas)
                    patchClas.addMethod(fakeConstructor)
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static generatePatch(String patchPath) {
        if (Config.modifiedClassNameList.size() < 1) {
            if (Config.isResourceFix) {
                System.err.println(" patch method is empty ,please check your commit ")
                return;
            }
            throw new RuntimeException(" patch method is empty ,please check your commit ")
        }
//            Config.methodNeedPatchSet.addAll(Config.patchMethodSignatureSet)

        HashMap<String, HashSet<AnonymousClassOuterClassMethodUtils.OuterMethodInfo>> changedAnonymousInfoMap =
                AnonymousClassOuterClassMethodUtils.changedAnonymousOuterMethodInfoMap;

        if (changedAnonymousInfoMap.size() > 0) {
            for (String anonymousClassName : changedAnonymousInfoMap.keySet()) {

            }
        }
        JavaUtils.printList(Config.modifiedClassNameList)
        handleSuperMethodInClass(Config.modifiedClassNameList);

        //auto generate all class
        for (String fullClassName : Config.modifiedClassNameList) {
            setAnonymousInnerClassPublic(fullClassName)//todo 在robustTransform已经做了，可以考虑删除这行代码
            CtClass ctClass = Config.classPool.get(fullClassName)
            CtClass patchClass = PatchesFactory.createPatch(patchPath, ctClass, false, NameManger.getInstance().getPatchName(ctClass.name), Config.patchMethodSignatureSet)
            patchClass.writeFile(patchPath)
            patchClass.defrost()
            CtClass sourceClass = Config.classPool.get(fullClassName)
            PatchesFactory.createPublicMethodForPrivate(patchClass)
            //create static method for private method
            createControlClass(patchPath, sourceClass)
        }
        handleAnonymousInnerAndLambdaClass();
        createPatchesInfoClass(patchPath);
        handleCustomAddClass();
        handleCustomInnerClassAccess$Method();
    }

    public static handleCustomAddClass() {
        HashSet<String> customAddClassList = new HashSet<>();
        for (String newClassName : Config.newlyAddedClassNameList) {
            boolean is_$1_or_$$lambda$1 = CheckCodeChanges.isAnonymousInnerClass(newClassName) || CheckCodeChanges.isAnonymousInnerClass_$$Lambda$1(newClassName)

            if (is_$1_or_$$lambda$1) {

            } else {
                customAddClassList.add(newClassName);
            }
        }

        for (String customAddClassName : customAddClassList) {
            CtClass customAddCtClass = Config.classPool.get(customAddClassName);
            customAddCtClass.defrost()
            CtBehavior[] ctBehaviors = customAddCtClass.getDeclaredBehaviors();
            for (CtBehavior ctBehavior : ctBehaviors) {
                if (ctBehavior.name.contains("initRobustPatch")) {
                    customAddCtClass.removeMethod((CtMethod) ctBehavior)
                } else {
                    ctBehavior.instrument(new RobustNewAddCustomClassExpr(customAddCtClass, ctBehavior));
                }
            }
            customAddCtClass.writeFile(Config.robustGenerateDirectory)
        }
    }


    public static void handleCustomInnerClassAccess$Method() {
        HashSet<CtClass> customInnerCtClassList = new HashSet<CtClass>();
        for (String customInnerClassName : Config.modifiedClassNameList) {
            boolean is_$1_or_$$lambda$1 = CheckCodeChanges.isAnonymousInnerClass(customInnerClassName) || CheckCodeChanges.isAnonymousInnerClass_$$Lambda$1(customInnerClassName)

            if (is_$1_or_$$lambda$1) {

            } else {
                if (customInnerClassName.contains("\$")){
                    String patchCustomInnerClassName = customInnerClassName + "Patch";
                    CtClass patchCustomInnerCtClass = Config.classPool.get(patchCustomInnerClassName);
                    customInnerCtClassList.add(patchCustomInnerCtClass);
                }
            }
        }
        for (CtClass customCtClass : customInnerCtClassList) {
            customCtClass.defrost();
            CtBehavior[] ctBehaviors = customCtClass.getDeclaredBehaviors();
            for (CtBehavior ctBehavior : ctBehaviors) {
                ctBehavior.instrument(new RobustHandleAccessMethodExpr());
            }
            customCtClass.writeFile(Config.robustGenerateDirectory)
        }
    }

    public static handleAnonymousInnerAndLambdaClass() {
        Config.classPool.appendClassPath(Config.robustGenerateDirectory)
        for (String originalClassName : Config.modifiedClassNameList) {
//            CtClass sourceClass = Config.classPool.get(originalClassName)
//            CtClass[] ctClasses = sourceClass.getNestedClasses();
            List<CtClass> ctClasses = new ArrayList<CtClass>()
//            ctClasses.addAll(sourceClass.getNestedClasses());//这里lambda表达式不在这里 todo 9-1

            for (String newAddClassName : Config.newlyAddedClassNameList) { //处理lambda表达式
                if (newAddClassName.startsWith(originalClassName)) {
                    boolean is_$1_or_$$lambda$1 = CheckCodeChanges.isAnonymousInnerClass(newAddClassName) || CheckCodeChanges.isAnonymousInnerClass_$$Lambda$1(newAddClassName)
                    if (is_$1_or_$$lambda$1) {
                        ctClasses.add(Config.classPool.get(newAddClassName));
                    }
                }
            }

            ClassMap classMap = new ClassMap()
            for (CtClass nestedCtClass : ctClasses) {
                boolean isAnonymousInnerClass = CheckCodeChanges.isAnonymousInnerClass(nestedCtClass.getName()) || CheckCodeChanges.isAnonymousInnerClass_$$Lambda$1(nestedCtClass.getName())
//                System.err.println("nestedCtClass :" + nestedCtClass.getName())
                if (isAnonymousInnerClass) {
                    nestedCtClass.defrost()
                    int modifiers1 = AccessFlag.setPublic(nestedCtClass.getModifiers())
                    modifiers1 = AccessFlag.clear(modifiers1, AccessFlag.SYNTHETIC);
                    nestedCtClass.setModifiers(modifiers1)
                    for (CtConstructor ctConstructor : nestedCtClass.getDeclaredConstructors()) {
                        ctConstructor.setModifiers(AccessFlag.setPublic(ctConstructor.getModifiers()))
                    }
                    String oldName = nestedCtClass.getName()
                    String newName = nestedCtClass.getName().replace(originalClassName, originalClassName + "Patch")
                    //给nestedClass改名字 MainActivity$1 -> MainActivityPatch$1
                    nestedCtClass.replaceClassName(oldName, newName)
                    nestedCtClass.writeFile(Config.robustGenerateDirectory)

                    Config.classPool.appendClassPath(Config.robustGenerateDirectory)
                    CtClass anonymousInnerClass = Config.classPool.get(nestedCtClass.getName())
                    anonymousInnerClass.defrost()
                    //handle access$100 todo 还得考虑普通内部类 比较头疼的内部类里面有内部类 8-23 需要测试
                    AnonymousInnerClassTransform.handleAccessMethodCall(anonymousInnerClass, originalClassName, originalClassName + "Patch")
//                    nestedCtClass.
                    anonymousInnerClass.writeFile(Config.robustGenerateDirectory)
                    classMap.put(oldName, newName)

//                    System.err.println("isAnonymousInnerClass:" + anonymousInnerClass.getName())

                }
            }

//            System.err.println("replaceClassName :" + originalClassName)

            CtClass patchClass = Config.classPool.get(NameManger.getInstance().getPatchNamWithoutRecord(originalClassName))
            patchClass.defrost()
            //add lambda class
            HashSet<String> lambdaHashSet = getLambdaClassChangedOrNewList()
            for (String lambdaClassName : lambdaHashSet){
                String tempPatchClassName = patchClass.getName();
                if (tempPatchClassName.endsWith("Patch")){
                    tempPatchClassName = tempPatchClassName + "ROBUST_FOR_DELETE";
                    String tempStr = "Patch" + "ROBUST_FOR_DELETE";
                    String sourceClassName  = tempPatchClassName.replace(tempStr,"");
                    String sourceClassName_prefix_lambda = sourceClassName + "\$\$Lambda\$";
                    if (lambdaClassName.startsWith(sourceClassName_prefix_lambda)){
//                        CtClass lambdaCtClass = Config.classPool.getOrNull(lambdaClassName);
//                       app/build/outputs/robust/com/meituan/sample/test/TestLambdaActivity$$Lambda$2Patch.class
                        String lambdaPatchClassName = lambdaClassName + "Patch";
                        CtClass lambdaPatchCtClass = Config.classPool.getOrNull(lambdaPatchClassName);
                        if (null != lambdaPatchCtClass){
                            classMap.put(lambdaClassName,lambdaPatchClassName)
                        }
                    }
                }
            }
            patchClass.replaceClassName(classMap)
            patchClass.setModifiers(AccessFlag.setPublic(patchClass.getModifiers()))
            if (true) {
                List<CtMethod> toDeletedCtMethods = new ArrayList<CtMethod>();
                for (CtMethod ctMethod : patchClass.getDeclaredMethods()) {
                    if (RobustChangeInfo.isInvariantMethod(ctMethod)) {
                        toDeletedCtMethods.add(ctMethod);
                    }
                    int modifiers = ctMethod.getModifiers();
                    boolean isSynthetic = modifiers & AccessFlag.SYNTHETIC
                    if (isSynthetic) {
                        int newModifiers = AccessFlag.clear(modifiers, AccessFlag.SYNTHETIC)
                        ctMethod.setModifiers(newModifiers);
                    }
                }

                for (CtMethod ctMethod : toDeletedCtMethods) {
                    patchClass.removeMethod(ctMethod);
                }
            }
            patchClass.writeFile(Config.robustGenerateDirectory)
        }
    }

    public static HashSet<String> getLambdaClassChangedOrNewList(){
        HashSet<String> lambdaDotClassNameSet = new HashSet<String>();
        for (String dotClassName : Config.modifiedClassNameList) {
            if (CheckCodeChanges.isAnonymousInnerClass_$$Lambda$1(dotClassName)) {
                lambdaDotClassNameSet.add(dotClassName);
            }
        }

        for (String dotClassName : Config.newlyAddedClassNameList) {
            if (CheckCodeChanges.isAnonymousInnerClass_$$Lambda$1(dotClassName)) {
                lambdaDotClassNameSet.add(dotClassName);
            }
        }

        for (String dotClassName : Config.modifiedAnonymousInnerClassNameList) {
            if (CheckCodeChanges.isAnonymousInnerClass_$$Lambda$1(dotClassName)) {
                lambdaDotClassNameSet.add(dotClassName);
            }
        }
        return lambdaDotClassNameSet;
    }

    public static setAnonymousInnerClassPublic(String originalClassName) {
        CtClass sourceClass = Config.classPool.get(originalClassName)
        CtClass[] ctClasses = sourceClass.getNestedClasses();
        for (CtClass nestedCtClass : ctClasses) {
            boolean isAnonymousInnerClass = CheckCodeChanges.isAnonymousInnerClass_$1(nestedCtClass.getName())
            if (isAnonymousInnerClass) {
                nestedCtClass.defrost()
                nestedCtClass.setModifiers(AccessFlag.setPublic(nestedCtClass.getModifiers()))
                for (CtConstructor ctConstructor : nestedCtClass.getDeclaredConstructors()) {
                    ctConstructor.setModifiers(AccessFlag.setPublic(ctConstructor.getModifiers()))
                }
            }
        }
    }

    public static deleteTmpFiles() {
        RobustPatchMerger.deleteTmpFiles()
    }

    public static executeCommand(String commond) {
        Process output = commond.execute(null, new File(Config.robustGenerateDirectory))
        output.inputStream.eachLine { println commond + " inputStream output   " + it }
        output.errorStream.eachLine {
            println commond + " errorStream output   " + it;
            throw new RuntimeException("execute command " + commond + " error");
        }
    }


    public static handleSuperMethodInClass(List originClassList) {
        CtClass modifiedCtClass;
        for (String modifiedFullClassName : originClassList) {
            List<CtMethod> invokeSuperMethodList = Config.invokeSuperMethodMap.getOrDefault(modifiedFullClassName, new ArrayList());
            //检查当前修改类中使用到类，并加入mapping信息
            modifiedCtClass = Config.classPool.get(modifiedFullClassName);
            modifiedCtClass.defrost();

            modifiedCtClass.declaredMethods.each { behavior ->
                behavior.instrument(new ExprEditor() {
                    @Override
                    void edit(MethodCall m) throws CannotCompileException {
                        if (m.isSuper()) {
                            System.err.println("class: " + modifiedCtClass.name + " , method :" + m.method.name)
                            if (!invokeSuperMethodList.contains(m.method)) {
                                invokeSuperMethodList.add(m.method);
                            }
                        }
                    }
                });
            }
            Config.invokeSuperMethodMap.put(modifiedFullClassName, invokeSuperMethodList);
        }
    }


    public static createControlClass(String patchPath, CtClass modifiedClass) {
        CtClass controlClass = PatchesControlFactory.createPatchesControl(modifiedClass);
        controlClass.writeFile(patchPath);
        return controlClass;
    }


    public static createPatchesInfoClass(String patchPath) {
        PatchesInfoFactory.createPatchesInfo().writeFile(patchPath);
    }

    public static clearPatchPath(String patchPath) {
        new File(patchPath).deleteDir();
    }

    public static packagePatchDex2Apk() throws IOException {
        File inputFile = new File(Config.robustGenerateDirectory, Constants.PATACH_DEX_NAME);
        if (!inputFile.exists() || !inputFile.canRead()) {
            throw new RuntimeException("patch.dex is not exists or readable")
        }
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(new File(Config.robustGenerateDirectory, Constants.PATACH_APK_NAME)))
        zipOut.setLevel(Deflater.NO_COMPRESSION)
        FileInputStream fis = new FileInputStream(inputFile)
        zipFile(inputFile, zipOut, Constants.CLASSES_DEX_NAME);
        zipOut.close()
    }

    public static zipFile(File inputFile, ZipOutputStream zos, String entryName) {
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        FileInputStream fis = new FileInputStream(inputFile)
        byte[] buffer = new byte[4092];
        int byteCount = 0;
        while ((byteCount = fis.read(buffer)) != -1) {
            zos.write(buffer, 0, byteCount);
        }
        fis.close();
        zos.closeEntry();
        zos.flush();
    }


}