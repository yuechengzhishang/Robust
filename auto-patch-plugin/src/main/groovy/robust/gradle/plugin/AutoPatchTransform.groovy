package robust.gradle.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.meituan.robust.Constants
import com.meituan.robust.autopatch.*
import com.meituan.robust.common.FileUtil
import com.meituan.robust.utils.JavaUtils
import javassist.*
import javassist.bytecode.AccessFlag
import javassist.expr.ExprEditor
import javassist.expr.MethodCall
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger

import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Created by mivanzhang on 16/7/21.
 *
 * AutoPatchTransform generate patch dex
 */
class AutoPatchTransform extends Transform implements Plugin<Project> {
    private
    static String dex2SmaliCommand;
    private
    static String smali2DexCommand;
    private
    static String jar2DexCommand;
    public static String ROBUST_DIR;
    Project project
    static Logger logger

    @Override
    void apply(Project target) {
        this.project = target
        logger = project.logger
        initConfig();
        project.android.registerTransform(this)
        project.afterEvaluate(new RobustResourcePatchAction())
    }

    def initConfig() {
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

    @Override
    String getName() {
        return "AutoPatchTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        def startTime = System.currentTimeMillis()
        logger.quiet '================autoPatch start================'
        copyJarToRobust()
        outputProvider.deleteAll()
        def outDir = outputProvider.getContentLocation("main", outputTypes, scopes, Format.DIRECTORY)
        project.android.bootClasspath.each {
            Config.classPool.appendClassPath((String) it.absolutePath)
        }
        def box = ReflectUtils.toCtClasses(inputs, Config.classPool)
        def cost = (System.currentTimeMillis() - startTime) / 1000
        logger.quiet "check all class cost $cost second, class count: ${box.size()}"
        autoPatch(box)
//        JavaUtils.removeJarFromLibs()
        if (Config.debug) {
            JavaUtils.printMap2File(Config.methodMap, new File(project.projectDir.path + Constants.METHOD_MAP_PATH + ".txt"))
            logger.quiet '================method signature to method id map unzip to file ================'
        }
        cost = (System.currentTimeMillis() - startTime) / 1000
        logger.quiet "autoPatch cost $cost second"
        if (Config.isResourceFix) {
            File jarFile = outputProvider.getContentLocation("main", getOutputTypes(), getScopes(),
                    Format.JAR);
            if (!jarFile.getParentFile().exists()) {
                jarFile.getParentFile().mkdirs();
            }
            if (jarFile.exists()) {
                jarFile.delete();
            }
            ResourceTaskUtils.keepCode(box, jarFile)
            return;
        }
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
                System.out.println("Warning!!!  Did not find " + libName + " ，you must add it to your project's libs ");
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

    def autoPatch(List<CtClass> box) {
        File buildDir = project.getBuildDir();
        String patchPath = buildDir.getAbsolutePath() + File.separator + Constants.ROBUST_GENERATE_DIRECTORY + File.separator;
        clearPatchPath(patchPath);
        if (false) {
            //todo 不需要注解了
            ReadAnnotation.readAnnotation(box, logger);
        } else {

            //1. get last class.jar
            File robustOutDirFile = new File(ROBUST_DIR);
            File storeMainJarFile = new File(robustOutDirFile, "robust_main.jar")
            File oldProGuardJarFile = new File(robustOutDirFile,"proguard_main.jar")
            //2. get current classes  todo 如果是proguard之后，我们插了代码，需要做兼容
            //拷贝未插桩的main.jar start todo move to RobustStoreClassAction 后面需要考虑在混淆后拷贝一下，第一版本暂时不考虑混淆
            File robustDirFile = new File(project.buildDir.path + File.separator + Constants.ROBUST_GENERATE_DIRECTORY);
            File newJarFile = new File(robustDirFile, "new_robust_main.jar")
            FileUtil.createFile(newJarFile.absolutePath)
            ZipOutputStream outStream = new JarOutputStream(new FileOutputStream(newJarFile));
            for (CtClass ctClass : box) {
                zipFile(ctClass.toBytecode(), outStream, ctClass.getName().replaceAll("\\.", "/") + ".class");
                ctClass.defrost()
            }
            outStream.close();

            //3. is changed
            JarFile originalJarFile = new JarFile(storeMainJarFile);
            JarFile currentJarFile = new JarFile(newJarFile);

            //todo 删除方法（可以忽略，但是如果是有super的调用，则需要处理一下新方法调用super方法即可)
            CheckCodeChanges.processChangedJar(originalJarFile, currentJarFile, Config.hotfixPackageList)


            println("modifiedClassNameList is ：")
            JavaUtils.printList(Config.modifiedClassNameList)

            for (String modifiedClassName : Config.modifiedClassNameList) {
                CtClass modifiedCtClass = Config.classPool.get(modifiedClassName);
                modifiedCtClass.defrost();
                Config.newlyAddedClassNameList.addAll(AnonymousInnerClassUtil.getAnonymousInnerClass(modifiedCtClass));
            }

            println("newlyAddedClassNameList is ：")
            JavaUtils.printList(Config.newlyAddedClassNameList)
        }


        if (Config.supportProGuard) {
            ReadMapping.getInstance().initMappingInfo();
        }

        generatePatch(box, patchPath);

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
                logger.quiet "dex patch file does not exists"
                return
            }
        } else {
            deleteTmpFiles()
        }
    }

    def zipPatchClassesFile() {
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(Config.robustGenerateDirectory + Constants.ZIP_FILE_NAME));
        zipAllPatchClasses(Config.robustGenerateDirectory + Config.patchPackageName.substring(0, Config.patchPackageName.indexOf(".")), "", zipOut);
        zipOut.close();

    }

    def zipAllPatchClasses(String path, String fullClassName, ZipOutputStream zipOut) {
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
            logger.debug("文件不存在!");
        }
    }

    def generatePatch(List<CtClass> box, String patchPath) {
        if (!Config.isManual) {
            if (Config.modifiedClassNameList.size() < 1) {
                if (Config.isResourceFix) {
                    logger.warn(" patch method is empty ,please check your Modify annotation or use RobustModify.modify() to mark modified methods")
                    return;
                }
                throw new RuntimeException(" patch method is empty ,please check your Modify annotation or use RobustModify.modify() to mark modified methods")
            }
//            Config.methodNeedPatchSet.addAll(Config.patchMethodSignatureSet)
            JavaUtils.printList(Config.modifiedClassNameList)
            handleSuperMethodInClass(Config.modifiedClassNameList);

            //auto generate all class
            for (String fullClassName : Config.modifiedClassNameList) {
                setAnonymousInnerClassPublic(fullClassName)
                CtClass ctClass = Config.classPool.get(fullClassName)
                CtClass patchClass = PatchesFactory.createPatch(patchPath, ctClass, false, NameManger.getInstance().getPatchName(ctClass.name), Config.patchMethodSignatureSet)
                patchClass.writeFile(patchPath)
                patchClass.defrost()
                CtClass sourceClass = Config.classPool.get(fullClassName)
                createControlClass(patchPath, sourceClass)
            }
            handleAnonymousInnerClass();
            createPatchesInfoClass(patchPath);
//            if (Config.methodNeedPatchSet.size() > 0) {
//                throw new RuntimeException(" some methods haven't patched,see unpatched method list : " + Config.methodNeedPatchSet.toListString())
//            }
        } else {
            autoPatchManually(box, patchPath);
        }

    }

    def handleAnonymousInnerClass() {
        //处理匿名内部类 todo
        //rename to ~patch
//        JavaUtils.printList(Config.newlyAddedClassNameList)
//        for (String className : Config.newlyAddedClassNameList) {
//            CtClass newAddCtClass = Config.classPool.get(className)
//            newAddCtClass.replaceClassName(className, className + "Patch")
//            newAddCtClass.writeFile(Config.robustGenerateDirectory)
//        }

        Config.classPool.appendClassPath(Config.robustGenerateDirectory)
        for(String originalClassName :Config.modifiedClassNameList){
            CtClass sourceClass = Config.classPool.get(originalClassName)
            CtClass[] ctClasses = sourceClass.getNestedClasses();
            ClassMap classMap = new ClassMap()
            for (CtClass nestedCtClass : ctClasses) {
                boolean  isAnonymousInnerClass = CheckCodeChanges.isAnonymousInnerClass(nestedCtClass.getName())
                System.err.println("nestedCtClass :" + nestedCtClass.getName())
                if (isAnonymousInnerClass){
                    nestedCtClass.defrost()
                    nestedCtClass.setModifiers(AccessFlag.setPublic(nestedCtClass.getModifiers()))
                    for (CtConstructor ctConstructor : nestedCtClass.getDeclaredConstructors()){
                        ctConstructor.setModifiers(AccessFlag.setPublic(ctConstructor.getModifiers()))
                    }
                    String oldName = nestedCtClass.getName()
                    String newName = nestedCtClass.getName().replace(originalClassName,originalClassName + "Patch")
                    nestedCtClass.replaceClassName(oldName,newName)
                    nestedCtClass.writeFile(Config.robustGenerateDirectory)
                    classMap.put(oldName,newName)
                    System.err.println("isAnonymousInnerClass :" + nestedCtClass.getName())
                }
            }

            System.err.println("replaceClassName :" + originalClassName)

            CtClass patchClass = Config.classPool.get(NameManger.getInstance().getPatchNamWithoutRecord(originalClassName))
            patchClass.defrost()
            patchClass.replaceClassName(classMap)
            patchClass.setModifiers(AccessFlag.setPublic(patchClass.getModifiers()))
            patchClass.writeFile(Config.robustGenerateDirectory)
        }
    }

    def setAnonymousInnerClassPublic(String originalClassName){
        CtClass sourceClass = Config.classPool.get(originalClassName)
        CtClass[] ctClasses = sourceClass.getNestedClasses();
        for (CtClass nestedCtClass : ctClasses) {
            boolean  isAnonymousInnerClass = CheckCodeChanges.isAnonymousInnerClass(nestedCtClass.getName())
            if (isAnonymousInnerClass){
                nestedCtClass.defrost()
                nestedCtClass.setModifiers(AccessFlag.setPublic(nestedCtClass.getModifiers()))
                for (CtConstructor ctConstructor : nestedCtClass.getDeclaredConstructors()){
                    ctConstructor.setModifiers(AccessFlag.setPublic(ctConstructor.getModifiers()))
                }
            }
        }
    }

    def deleteTmpFiles() {
        RobustPatchMerger.deleteTmpFiles()
    }

    def autoPatchManually(List<CtClass> box, String patchPath) {
        box.forEach { ctClass ->
            if (Config.isManual && ctClass.name.startsWith(Config.patchPackageName)) {
                Config.modifiedClassNameList.add(ctClass.name);
                ctClass.writeFile(patchPath);
            }
        }
    }


    def executeCommand(String commond) {
        Process output = commond.execute(null, new File(Config.robustGenerateDirectory))
        output.inputStream.eachLine { println commond + " inputStream output   " + it }
        output.errorStream.eachLine {
            println commond + " errorStream output   " + it;
            throw new RuntimeException("execute command " + commond + " error");
        }
    }


    def handleSuperMethodInClass(List originClassList) {
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


    def createControlClass(String patchPath, CtClass modifiedClass) {
        CtClass controlClass = PatchesControlFactory.createPatchesControl(modifiedClass);
        controlClass.writeFile(patchPath);
        return controlClass;
    }


    def createPatchesInfoClass(String patchPath) {
        PatchesInfoFactory.createPatchesInfo().writeFile(patchPath);
    }

    def clearPatchPath(String patchPath) {
        new File(patchPath).deleteDir();
    }

    def packagePatchDex2Apk() throws IOException {
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

    def zipFile(File inputFile, ZipOutputStream zos, String entryName) {
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

    public static void zipFile(byte[] classBytesArray, ZipOutputStream zos, String entryName) {
        try {
            ZipEntry entry = new ZipEntry(entryName);
            zos.putNextEntry(entry);
            zos.write(classBytesArray, 0, classBytesArray.length);
            zos.closeEntry();
            zos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}