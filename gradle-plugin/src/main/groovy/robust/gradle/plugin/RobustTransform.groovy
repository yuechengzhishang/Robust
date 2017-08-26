package robust.gradle.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.meituan.robust.Constants
import com.meituan.robust.common.FileUtil
import javassist.ClassPool
import javassist.CtClass
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import robust.gradle.plugin.asm.AsmInsertImpl
import robust.gradle.plugin.javaassist.JavaAssistInsertImpl

import java.util.jar.JarOutputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.ZipOutputStream
/**
 * Created by mivanzhang on 16/11/3.
 *
 * insert code
 *
 */

class RobustTransform extends Transform implements Plugin<Project> {
    Project project
    static Logger logger
    private static List<String> hotfixPackageList = new ArrayList<>();
    private static List<String> hotfixMethodList = new ArrayList<>();
    private static List<String> exceptPackageList = new ArrayList<>();
    private static List<String> exceptMethodList = new ArrayList<>();
    private static boolean isHotfixMethodLevel = false;
    private static boolean isExceptMethodLevel = false;
//    private static boolean isForceInsert = true;
    private static boolean isForceInsert = false;
//    private static boolean useASM = false;
    private static boolean useASM = true;
    def robust
    InsertcodeStrategy insertcodeStrategy;

    @Override
    void apply(Project target) {
        project = target
        robust = new XmlSlurper().parse(new File("${project.projectDir}/${Constants.ROBUST_XML}"))
        logger = project.logger
        initConfig()
        //turnOnDevelopModel 是true的话，则强制执行插入
        if (!isForceInsert) {
            def taskNames = project.gradle.startParameter.taskNames
            def isDebugTask = false;
            for (int index = 0; index < taskNames.size(); ++index) {
                def taskName = taskNames[index]
                logger.debug "input start parameter task is ${taskName}"
                //FIXME: assembleRelease下屏蔽Prepare，这里因为还没有执行Task，没法直接通过当前的BuildType来判断，所以直接分析当前的startParameter中的taskname，
                //另外这里有一个小坑task的名字不能是缩写必须是全称 例如assembleDebug不能是任何形式的缩写输入
                if (taskName.endsWith("Debug") && taskName.contains("Debug")) {
//                    logger.warn " Don't register robust transform for debug model !!! task is：${taskName}"
                    isDebugTask = true
                    break;
                }
            }
            if (!isDebugTask) {
                project.android.registerTransform(this)
                project.afterEvaluate(new RobustApkHashAction())
                project.afterEvaluate(new RobustStoreClassAction())
                logger.quiet "Register robust transform successful !!!"
            }
            if (null != robust.switch.turnOnRobust && !"true".equals(String.valueOf(robust.switch.turnOnRobust))) {
                return;
            }
        } else {
            project.android.registerTransform(this)
            project.afterEvaluate(new RobustApkHashAction())
            project.afterEvaluate(new RobustStoreClassAction())
        }
    }

    def initConfig() {
        hotfixPackageList = new ArrayList<>()
        hotfixMethodList = new ArrayList<>()
        exceptPackageList = new ArrayList<>()
        exceptMethodList = new ArrayList<>()
        isHotfixMethodLevel = false;
        isExceptMethodLevel = false;
        /*对文件进行解析*/
        for (name in robust.packname.name) {
            hotfixPackageList.add(name.text());
        }
        for (name in robust.exceptPackname.name) {
            exceptPackageList.add(name.text());
        }
        for (name in robust.hotfixMethod.name) {
            hotfixMethodList.add(name.text());
        }
        for (name in robust.exceptMethod.name) {
            exceptMethodList.add(name.text());
        }

        if (null != robust.switch.filterMethod && "true".equals(String.valueOf(robust.switch.turnOnHotfixMethod.text()))) {
            isHotfixMethodLevel = true;
        }

        if (null != robust.switch.useAsm && "false".equals(String.valueOf(robust.switch.useAsm.text()))) {
            useASM = false;
        }else {
            //默认使用asm
            useASM = true;
        }

        if (null != robust.switch.filterMethod && "true".equals(String.valueOf(robust.switch.turnOnExceptMethod.text()))) {
            isExceptMethodLevel = true;
        }

        if (robust.switch.forceInsert != null && "true".equals(String.valueOf(robust.switch.forceInsert.text())))
            isForceInsert = true
        else
            isForceInsert = false

    }

    @Override
    String getName() {
        return "robust"
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
        logger.quiet '================robust start================'
        def startTime = System.currentTimeMillis()
        outputProvider.deleteAll()
        File jarFile = outputProvider.getContentLocation("main", getOutputTypes(), getScopes(),
                Format.JAR);
        if(!jarFile.getParentFile().exists()){
            jarFile.getParentFile().mkdirs();
        }
        if(jarFile.exists()){
            jarFile.delete();
        }

        ClassPool classPool = new ClassPool()
        project.android.bootClasspath.each {
            classPool.appendClassPath((String) it.absolutePath)
        }

        def box = ConvertUtils.toCtClasses(inputs, classPool)

        //todo 在混淆task后，dex task之前 :aimeituan:transformClassesWithDexForPreloadedRelease
        //拷贝未插桩的main.jar start todo move to RobustStoreClassAction 后面需要考虑在混淆后拷贝一下，第一版本暂时不考虑混淆
        File robustOutDirFile = new File(project.buildDir.path + File.separator + Constants.ROBUST_GENERATE_DIRECTORY);
        File storeMainJarFile = new File(robustOutDirFile,"robust_main.jar")
        FileUtil.createFile(storeMainJarFile.absolutePath)
        ZipOutputStream outStream= new JarOutputStream(new FileOutputStream(storeMainJarFile));
        for(CtClass ctClass:box) {
            InsertcodeStrategy.zipFile(ctClass.toBytecode(), outStream, ctClass.getName().replaceAll("\\.", "/") + ".class");
            ctClass.defrost()
        }
        outStream.close();
//        FileUtil.copyFile(jarFile,storeMainJarFile)
        //拷贝未插桩的main.jar完成 end

        def cost = (System.currentTimeMillis() - startTime) / 1000
//        logger.quiet "check all class cost $cost second, class count: ${box.size()}"
        if(useASM){
            insertcodeStrategy=new AsmInsertImpl(hotfixPackageList,hotfixMethodList,exceptPackageList,exceptMethodList,isHotfixMethodLevel,isExceptMethodLevel);
        }else {
            insertcodeStrategy=new JavaAssistInsertImpl(hotfixPackageList,hotfixMethodList,exceptPackageList,exceptMethodList,isHotfixMethodLevel,isExceptMethodLevel);
        }
        insertcodeStrategy.insertCode(box, jarFile);
        writeMap2File(insertcodeStrategy.methodMap, Constants.METHOD_MAP_OUT_PATH)


        //todo 在混淆task后，dex task之前 :aimeituan:transformClassesWithDexForPreloadedRelease
        //拷贝未插桩的main.jar start todo move to RobustStoreClassAction 后面需要考虑在混淆后拷贝一下，第一版本暂时不考虑混淆
        String robustOutDir = project.buildDir.path + File.separator + Constants.ROBUST_GENERATE_DIRECTORY
        File robustMainJar = new File(robustOutDir,"robust_transform_main.jar")
        FileUtil.copyFile(jarFile,robustMainJar)


        cost = (System.currentTimeMillis() - startTime) / 1000
        logger.quiet "robust cost $cost second"
        logger.quiet '================robust   end================'
    }

    private void writeMap2File(Map map, String path) {
        File file = new File(project.buildDir.path + path);
        if (!file.exists() && (!file.parentFile.mkdirs() || !file.createNewFile())) {
//            logger.error(path + " file create error!!")
        }
        FileOutputStream fileOut = new FileOutputStream(file);

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
        objOut.writeObject(map)
        //gzip压缩
        GZIPOutputStream gzip = new GZIPOutputStream(fileOut);
        gzip.write(byteOut.toByteArray())
        objOut.close();
        gzip.flush();
        gzip.close();
        fileOut.flush()
        fileOut.close()

        boolean  methodIdDuplicated = checkMethodIdDuplicated(map)
        if (methodIdDuplicated){
            throw RuntimeException("robust : method id is duplicated! (MD5 collision)")
        }
    }

    //check MD5 collision
    private static boolean checkMethodIdDuplicated(HashMap<String, String> robustMethodsMap) {
        if (null == robustMethodsMap || robustMethodsMap.size() == 0) {
            return false;
        }
        HashSet<String> md5s = new HashSet<>();
        for (String key : robustMethodsMap.keySet()) {
            String value = robustMethodsMap.get(key);
            if (md5s.contains(value)) {
                return true;
            } else {
                md5s.add(value);
            }
        }
        return false;
    }

}