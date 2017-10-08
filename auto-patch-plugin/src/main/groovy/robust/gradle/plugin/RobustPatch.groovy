package robust.gradle.plugin

import com.android.build.gradle.api.BaseVariant
import com.meituan.robust.Constants
import com.meituan.robust.autopatch.*
import com.meituan.robust.autopatch.innerclass.anonymous.AnonymousInnerClassTransform
import com.meituan.robust.change.AspectJUtils
import com.meituan.robust.change.RobustChangeInfo
import com.meituan.robust.common.FileUtil
import com.meituan.robust.utils.*
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
 * generate patch apk
 */
public class RobustPatch {
    private static String dex2SmaliCommand;
    private static String smali2DexCommand;
    private static String jar2DexCommand;
    public static String ROBUST_DIR;

    public static initConfig(Project project, BaseVariant variant) {
        //clear
        NameManger.init();
//        InlineClassFactory.init();
        Config.init();

        ROBUST_DIR = "${project.projectDir}${File.separator}robust${File.separator}"
        def baksmaliFilePath = "${ROBUST_DIR}${Constants.LIB_NAME_ARRAY[0]}"
        def smaliFilePath = "${ROBUST_DIR}${Constants.LIB_NAME_ARRAY[1]}"
        def dxFilePath = "${ROBUST_DIR}${Constants.LIB_NAME_ARRAY[2]}"
        Config.robustGenerateDirectory = "${project.buildDir}" + File.separator + "$Constants.ROBUST_GENERATE_DIRECTORY" + File.separator;
        RobustLog.setRobustLogFilePath(Config.robustGenerateDirectory + Constants.ROBUST_LOG)
        dex2SmaliCommand = "  java -jar ${baksmaliFilePath} -o classout" + File.separator + "  $Constants.CLASSES_DEX_NAME";
        smali2DexCommand = "   java -jar ${smaliFilePath} classout" + File.separator + " -o " + Constants.PATACH_DEX_NAME;
        jar2DexCommand = "   java -jar ${dxFilePath} --dex --output=$Constants.CLASSES_DEX_NAME  " + Constants.ZIP_FILE_NAME;
        ReadXML.readXMl(project.projectDir.path);
        String variantPath = "";
        String flavor = variant.flavorName;
        String type = variant.buildType.name;
        if (null == flavor || "".equals(flavor)) {
        } else {
            variantPath = variantPath + flavor + File.separator
        }
        variantPath = variantPath + type + File.separator
        //使用new mapping.txt才能知道一些新增的class混淆前是否是匿名内部类等等
        Config.newMappingFilePath = "${project.buildDir}" + File.separator + Constants.PROGUARD_MAPPING_TXT + File.separator + variantPath + "mapping.txt";

        copyJarToRobust()

        project.android.bootClasspath.each {
            Config.classPool.appendClassPath((String) it.absolutePath)
        }

        project.android.bootClasspath.each {
            Config.oldClassPool.appendClassPath((String) it.absolutePath)
        }
    }

    public static void patch(Project project) throws IOException {
        long startTime = System.currentTimeMillis()
        com.meituan.robust.utils.RobustLog.log("================autoPatch start================")
        autoPatch(project)
//        JavaUtils.removeJarFromLibs()
        long cost = (System.currentTimeMillis() - startTime) / 1000
        com.meituan.robust.utils.RobustLog.log("autoPatch cost " + cost + " second")
        throw new RuntimeException("*** auto patch end successfully! ***, patch path is : " + new File(Config.robustGenerateDirectory, Constants.PATACH_APK_NAME).toPath())
    }

    static def copyJarToRobust() {
        File targetDir = new File(ROBUST_DIR);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        for (String libName : Constants.LIB_NAME_ARRAY) {
            InputStream inputStream = JavaUtils.class.getResourceAsStream("/libs/" + libName);
            if (inputStream == null) {
                RobustLog.log("Warning!!!  Did not find " + libName + " ，you must addClasses it to your project's libs ");
                continue;
            }
            File inputFile = new File(ROBUST_DIR + libName);
            try {
                OutputStream inputFileOut = new FileOutputStream(inputFile);
                JavaUtils.copy(inputStream, inputFileOut);
            } catch (Exception e) {
                RobustLog.log("NotFoundException ", e);
                RobustLog.log("Warning!!! " + libName + " copy error " + e.getMessage());

            }
        }
    }

    static File oldMainJarFile,newMainJarFile;
    public static void setRobustMainJar(Project project){
        String oldRobustMap = project.projectDir.path + Constants.METHOD_MAP_PATH;
        if (!new File(oldRobustMap).exists()){
            oldRobustMap = oldRobustMap.replace(File.separator+"robust"+File.separator,File.separator+"robust"+File.separator+"old"+File.separator)
        }
        if (!new File(oldRobustMap).exists()){
            throw RuntimeException("robust method map should be in dir : project/robust or project/robust/old")
        }
        Config.methodMap = JavaUtils.getMapFromZippedFile(oldRobustMap)

        File buildDir = project.getBuildDir();
        String patchPath = buildDir.getAbsolutePath() + File.separator + Constants.ROBUST_GENERATE_DIRECTORY + File.separator;
//        clearPatchPath(patchPath);

        //1. get last class.jar
        oldMainJarFile = new File(ROBUST_DIR, Constants.ROBUST_TRANSFORM_MAIN_JAR)
        if (!oldMainJarFile.exists()){
            oldMainJarFile = new File(ROBUST_DIR + "old"+File.separator,Constants.ROBUST_TRANSFORM_MAIN_JAR)
        }

        File oldProGuardJarFile = new File(ROBUST_DIR, Constants.ROBUST_PROGUARD_MAIN_JAR)
        if (!oldProGuardJarFile.exists()){
            oldProGuardJarFile = new File(ROBUST_DIR + "old"+File.separator,Constants.ROBUST_PROGUARD_MAIN_JAR)
        }

        //2. get current classes 如果是proguard之后，我们插了代码，需要做兼容
        newMainJarFile = new File(patchPath, Constants.ROBUST_TRANSFORM_MAIN_JAR)
        File newProGuradJarFile = new File(patchPath, Constants.ROBUST_PROGUARD_MAIN_JAR)
        if (newMainJarFile.exists() || newProGuradJarFile.exists()) {
            //如果proguard打开了，就使用proguard的包
            if (newProGuradJarFile.exists()) {
                if (null == oldProGuardJarFile || !oldProGuardJarFile.exists()) {
                    throw new RuntimeException("you are use proguard, please copy your last build/outputs/robust/" + Constants.ROBUST_PROGUARD_MAIN_JAR + " to app/robust dir ")
                }
                newMainJarFile = newProGuradJarFile
                oldMainJarFile = oldProGuardJarFile
                //read mapping 使用新的mapping文件
                RobustProguardMapping.readMapping(Config.newMappingFilePath);
            }
        } else {
            throw new RuntimeException("please apply plugin: 'robust'")
        }
    }

    public static autoPatch(Project project) {
        //3. is changed
        JarFile originalJarFile/* = new JarFile(oldMainJarFile)*/;
        JarFile currentJarFile /*= new JarFile(newMainJarFile)*/;

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
//        println("recordOuterMethodModifiedAnonymousClassNameList is :")
//        JavaUtils.printList(Config.recordOuterMethodModifiedAnonymousClassNameList)

//        println("fix that unchanged lambda class really: ")
        //fix start lambdaUnchangedReallyClassNameHashMap
//        List<String> newModifiedClassNameList = new ArrayList<>();
//        for (String className : Config.modifiedClassNameList) {
//            if (Config.lambdaUnchangedReallyClassNameHashMap.containsKey(className)) {
//
//            } else {
//                newModifiedClassNameList.add(className);
//            }
//        }
//        Config.modifiedClassNameList = newModifiedClassNameList;
//
//        List<String> newlyAddedClassNameList = new ArrayList<>();
//        for (String className : Config.newlyAddedClassNameList) {
//            if (Config.lambdaUnchangedReallyClassNameHashMap.containsKey(className)) {
//
//            } else {
//                newlyAddedClassNameList.add(className);
//            }
//        }
//        Config.newlyAddedClassNameList = newlyAddedClassNameList;

//        List<String> recordOuterMethodModifiedAnonymousClassNameList = new ArrayList<>();
//        for (String className : Config.recordOuterMethodModifiedAnonymousClassNameList) {
//            if (Config.lambdaUnchangedReallyClassNameHashMap.containsKey(className)) {
//
//            } else {
//                recordOuterMethodModifiedAnonymousClassNameList.add(className);
//            }
//        }
//        Config.recordOuterMethodModifiedAnonymousClassNameList = recordOuterMethodModifiedAnonymousClassNameList;
        //fix end

        com.meituan.robust.utils.RobustLog.log("modifiedClassNameList is ：")
        JavaUtils.printList(Config.modifiedClassNameList)

        com.meituan.robust.utils.RobustLog.log("newlyAddedClassNameList is ：")
        JavaUtils.printList(Config.newlyAddedClassNameList)

        com.meituan.robust.utils.RobustLog.log("modifiedAnonymousClassNameList is ：")
        JavaUtils.printList(Config.modifiedAnonymousClassNameList)

        com.meituan.robust.utils.RobustLog.log("modifiedLambdaClassNameList is ：")
        JavaUtils.printList(Config.modifiedLambdaClassNameList)

        //从modifiedClassNameList & newlyAddedClassNameList移除
        Config.newlyAddedClassNameList.removeAll(Config.modifiedAnonymousClassNameList)
        Config.newlyAddedClassNameList.removeAll(Config.modifiedLambdaClassNameList)

        Config.modifiedClassNameList.removeAll(Config.modifiedAnonymousClassNameList)
        Config.modifiedClassNameList.removeAll(Config.modifiedLambdaClassNameList)

        List<String> ajcClosureClassList = new ArrayList<String>()
        for (String tempClassString :Config.newlyAddedClassNameList){
            if (AnonymousLambdaUtils.isAnonymousInnerClass_$AjcClosure1(tempClassString)){
                ajcClosureClassList.add(tempClassString)
            }
        }

        Config.newlyAddedClassNameList.removeAll(ajcClosureClassList)

//        println("merge anonymousInnerClass 's outer class and method to modifiedClassNameList :")
        for (String anonymousClassName : Config.recordOuterMethodModifiedAnonymousClassNameList) {
            OuterClassMethodAnonymousClassUtils.OuterMethodInfo outerMethodInfo = Config.recordAnonymousLambdaOuterMethodMap.get(anonymousClassName);
            //如果改的是field = new View.onclickListener ，这里的outerMethodInfo == null
            if (null != outerMethodInfo) {
                if (Config.modifiedClassNameList.contains(outerMethodInfo.outerClass)) {
                    //修改的class已经包含了匿名内部类改动带来的class改动
                } else {
                    Config.modifiedClassNameList.add(outerMethodInfo.outerClass)
                }
            }
        }

        for (String modifiedClassName : Config.modifiedClassNameList) {
            CtClass modifiedCtClass = Config.classPool.get(modifiedClassName);
            modifiedCtClass.defrost();
//            Config.newlyAddedClassNameList.addAll(AnonymousInnerClassUtil.getAnonymousInnerClass(modifiedCtClass));
        }

        com.meituan.robust.utils.RobustLog.log("newlyAddedClassNameList is ：")
        Config.newlyAddedClassNameList.addAll(Config.modifiedLambdaClassNameList)
        Config.newlyAddedClassNameList.addAll(Config.modifiedAnonymousClassNameList)
        JavaUtils.printList(Config.newlyAddedClassNameList)

        File buildDir = project.getBuildDir();
        String patchPath = buildDir.getAbsolutePath() + File.separator + Constants.ROBUST_GENERATE_DIRECTORY + File.separator;
        generatePatch(patchPath);

        zipPatchClassesFile()
        executeCommand(jar2DexCommand)
        executeCommand(dex2SmaliCommand)
        SmaliTool.dealObscureInSmali();
        executeCommand(smali2DexCommand)
        //package patch.dex to patch.apk
        packagePatchDex2Apk()

        if (Config.isResourceFix) {
            File dexPatchFile = new File(Config.robustGenerateDirectory + Constants.ZIP_FILE_NAME);
            if (dexPatchFile.exists()) {
                Config.patchHasDex = true;
            } else {
                com.meituan.robust.utils.RobustLog.log("dex patch file does not exists")
                return
            }
        } else {
            deleteTmpFiles()
        }
    }

    public static JarFile copyConstructor2InitRobustPatchMethod(Project project, String fullPath, String jarName) {
        ClassPool classPool = new ClassPool()
        project.android.bootClasspath.each {
            classPool.appendClassPath((String) it.absolutePath)
        }
        classPool.appendClassPath(fullPath)

        String jarOutDirectoryPath = Config.robustGenerateDirectory + "constructor" + Constants.File_SEPARATOR + jarName.replace(".jar", "")
        FileUtil.createDirectory(jarOutDirectoryPath)
        FileUtil.unzip(fullPath, jarOutDirectoryPath)

        HashSet<String> classesNameHashSet = CheckCodeChanges.get_ShouldAddInitRobustPatchMethod_ClassesFromJar(new JarFile(fullPath))
        for (String className : classesNameHashSet) {
            CtClass ctClass = classPool.get(className)
            try {
                if (null != ctClass.getAnnotation(Class.forName("org.aspectj.lang.annotation.Aspect"))){
                    AspectJUtils.recordAspectAnnotationCtClass(ctClass)
                }
            } catch (Exception e){

            }
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
            com.meituan.robust.utils.RobustLog.log("文件不存在!");
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
            RobustLog.log("Exception ", e);
        }
    }

    public static generatePatch(String patchPath) {
        if (Config.modifiedClassNameList.size() < 1) {
            if (Config.isResourceFix) {
                com.meituan.robust.utils.RobustLog.log(" patch method is empty ,please check your commit ")
                return;
            }
            throw new RuntimeException(" patch method is empty ,please check your commit ")
        }

        HashMap<String, HashSet<OuterClassMethodAnonymousClassUtils.OuterMethodInfo>> changedAnonymousInfoMap =
                Config.recordAnonymousLambdaOuterMethodMap;

        if (changedAnonymousInfoMap.size() > 0) {
            for (String anonymousClassName : changedAnonymousInfoMap.keySet()) {

            }
        }
        handleSuperMethodInClass(Config.modifiedClassNameList);

        //auto generate all class
        for (String fullClassName : Config.modifiedClassNameList) {
            setAnonymousInnerClassPublic(fullClassName)//在robustTransform已经做了，可以考虑删除这行代码
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
            boolean is_$1_or_$$lambda$1 = AnonymousLambdaUtils.isAnonymousInnerClass_$1(newClassName) || AnonymousLambdaUtils.isAnonymousInnerClass_$$Lambda$1(newClassName)

            if (is_$1_or_$$lambda$1) {
                CtClass $1_or_$$lambda$1_ctClass = Config.classPool.getOrNull(newClassName)
                if (null != $1_or_$$lambda$1_ctClass) {
                    $1_or_$$lambda$1_ctClass.writeFile(Config.robustGenerateDirectory)
                }
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
            boolean is_$1_or_$$lambda$1 = AnonymousLambdaUtils.isAnonymousInnerClass_$1(customInnerClassName) || AnonymousLambdaUtils.isAnonymousInnerClass_$$Lambda$1(customInnerClassName)

            if (is_$1_or_$$lambda$1) {

            } else {
                if (ProguardUtils.isClassNameHas$(customInnerClassName)) {
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
//            ctClasses.addAll(sourceClass.getNestedClasses());//这里lambda表达式不在这里

            for (String newAddClassName : Config.modifiedAnonymousClassNameList) { //处理Anonymous表达式
                if (ProguardUtils.isSubClass(newAddClassName, originalClassName)) {
                    ctClasses.add(Config.classPool.get(newAddClassName));
                }
            }

            for (String newAddClassName : Config.modifiedLambdaClassNameList) { //处理lambda表达式
                if (ProguardUtils.isSubClass(newAddClassName, originalClassName)) {
                    ctClasses.add(Config.classPool.get(newAddClassName));
                }
            }

            ClassMap classMap = new ClassMap()
            for (CtClass tempLambdaOrAnonymousCtClass : ctClasses) {

                tempLambdaOrAnonymousCtClass.defrost()
                int modifiers1 = AccessFlag.setPublic(tempLambdaOrAnonymousCtClass.getModifiers())
//                    modifiers1 = AccessFlag.clear(modifiers1, AccessFlag.SYNTHETIC);
                tempLambdaOrAnonymousCtClass.setModifiers(modifiers1)
                for (CtConstructor ctConstructor : tempLambdaOrAnonymousCtClass.getDeclaredConstructors()) {
                    ctConstructor.setModifiers(AccessFlag.setPublic(ctConstructor.getModifiers()))
                }
                String oldName = tempLambdaOrAnonymousCtClass.getName()

                String newName;

                String unProguardOldName = ProguardUtils.getUnProguardClassName(oldName);
                String unProguardOriginalClassName = ProguardUtils.getUnProguardClassName(originalClassName);
                if (oldName.contains(originalClassName) /*|| unProguardOldName.contains(unProguardOriginalClassName)*/) {
                    newName = oldName.replace(originalClassName, originalClassName + "Patch")
                } else {
                    newName = oldName + "Patch";
                }

                classMap.put(oldName, newName)
                if (null != Config.classPool.getOrNull(newName)) {
                    //patch is already in patch dir
                    continue;
                }
                //给nestedClass改名字 MainActivity$1 -> MainActivityPatch$1
                tempLambdaOrAnonymousCtClass.defrost()
                tempLambdaOrAnonymousCtClass.replaceClassName(oldName, newName)
                tempLambdaOrAnonymousCtClass.writeFile(Config.robustGenerateDirectory)

                Config.classPool.appendClassPath(Config.robustGenerateDirectory)
                CtClass anonymousInnerClass = Config.classPool.get(tempLambdaOrAnonymousCtClass.getName())
                anonymousInnerClass.defrost()
                //handle access$100
                AnonymousInnerClassTransform.handleAccessMethodCall(anonymousInnerClass, originalClassName, originalClassName + "Patch")
//                    nestedCtClass.
                anonymousInnerClass.writeFile(Config.robustGenerateDirectory)
                classMap.put(oldName, newName)

            }


            //handle AjcClosure classes
            HashSet<CtClass> AjcClosureSet = AspectJUtils.getAjcClosureSet(originalClassName, Config.classPool)
            if (null == AjcClosureSet) {

            } else {
                for(CtClass ajcClosureSetCtClass : AjcClosureSet){
                    ajcClosureSetCtClass.defrost()
                    String oldName = ajcClosureSetCtClass.getName()
                    int index = oldName.indexOf(AspectJUtils.AJC_CLOSURE_KEY)
                    String outerClassName = oldName.subSequence(0,index)
                    String newName = oldName.replace(outerClassName,outerClassName + "Patch")
//                    String unProguardOldName = ProguardUtils.getUnProguardClassName(oldName);
//                    String unProguardOriginalClassName = ProguardUtils.getUnProguardClassName(originalClassName);
//                    if (oldName.contains(originalClassName) /*|| unProguardOldName.contains(unProguardOriginalClassName)*/) {
//                        newName = oldName.replace(originalClassName, originalClassName + "Patch")
//                    } else {
//                        newName = oldName + "Patch";
//                    }

                    classMap.put(oldName, newName)
                    if (null != Config.classPool.getOrNull(newName)) {
                        //patch is already in patch dir
                        continue;
                    }

//                    String proguardOuterClassName =  ProguardUtils.getUnProguardClassName(outerClassName);

                    //给nestedClass改名字 MainActivity$1 -> MainActivityPatch$1
                    ajcClosureSetCtClass.defrost()
                    ajcClosureSetCtClass.replaceClassName(oldName, newName)
                    ajcClosureSetCtClass.replaceClassName(originalClassName,originalClassName + "Patch")
                    ajcClosureSetCtClass.writeFile(Config.robustGenerateDirectory)

                    Config.classPool.appendClassPath(Config.robustGenerateDirectory)
                    classMap.put(oldName, newName)
                }
            }


//            System.err.println("replaceClassName :" + originalClassName)

            CtClass patchClass = Config.classPool.get(NameManger.getInstance().getPatchNamWithoutRecord(originalClassName))
            patchClass.defrost()
//            //add lambda class
//            HashSet<String> lambdaHashSet = getLambdaClassChangedOrNewList()
//            for (String lambdaClassName : lambdaHashSet){
//                String tempPatchClassName = patchClass.getName();
//                if (tempPatchClassName.endsWith("Patch")){
//                    tempPatchClassName = tempPatchClassName + "ROBUST_FOR_DELETE";
//                    String tempStr = "Patch" + "ROBUST_FOR_DELETE";
//                    String sourceClassName  = tempPatchClassName.replace(tempStr,"");
//                    String sourceClassName_prefix_lambda = sourceClassName + "\$\$Lambda\$";
//                    if (lambdaClassName.startsWith(sourceClassName_prefix_lambda)){
////                        CtClass lambdaCtClass = Config.classPool.getOrNull(lambdaClassName);
////                       app/build/outputs/robust/com/meituan/sample/test/TestLambdaActivity$$Lambda$2Patch.class
//                        String lambdaPatchClassName = lambdaClassName + "Patch";
//                        CtClass lambdaPatchCtClass = Config.classPool.getOrNull(lambdaPatchClassName);
//                        if (null != lambdaPatchCtClass){
//                            classMap.put(lambdaClassName,lambdaPatchClassName)
//                        }
//                    }
//                }
//            }

            patchClass.replaceClassName(classMap)
            patchClass.setModifiers(AccessFlag.setPublic(patchClass.getModifiers()))
            if (true) {
                List<CtMethod> toDeletedCtMethods = new ArrayList<CtMethod>();
                for (CtMethod ctMethod : patchClass.getDeclaredMethods()) {
                    if (AspectJUtils.isAjc$preClinitMethod(ctMethod.name)
                    || AspectJUtils.isAroundBodyMethod(ctMethod.name)){
                        continue;
                    }
                    if (RobustChangeInfo.isInvariantMethod(ctMethod) && !RobustChangeInfo.isChangedMethod(ctMethod)) {
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

    public static HashSet<String> getLambdaClassChangedOrNewList() {
        HashSet<String> lambdaDotClassNameSet = new HashSet<String>();
        for (String dotClassName : Config.modifiedLambdaClassNameList) {
            if (AnonymousLambdaUtils.isAnonymousInnerClass_$$Lambda$1(dotClassName)) {
                lambdaDotClassNameSet.add(dotClassName);
            }
        }
        return lambdaDotClassNameSet;
    }

    public static setAnonymousInnerClassPublic(String originalClassName) {
        CtClass sourceClass = Config.classPool.get(originalClassName)
        CtClass[] ctClasses = sourceClass.getNestedClasses();
        for (CtClass nestedCtClass : ctClasses) {
            boolean isAnonymousInnerClass = AnonymousLambdaUtils.isAnonymousInnerClass_$1(nestedCtClass.getName())
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
                        if (m.isSuper() && m.getClassName().equals(modifiedCtClass.getSuperclass().getName())) {
                            com.meituan.robust.utils.RobustLog.log("class: " + modifiedCtClass.name + " , method :" + m.method.name)
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