package robust.gradle.plugin

import com.meituan.robust.Constants
import com.meituan.robust.autopatch.Config
import com.meituan.robust.utils.JavaUtils
import com.meituan.robust.utils.RobustProguardMapping
import org.gradle.api.Action
import org.gradle.api.Project
/**
 * Created by hedex on 17/2/21.
 */
public class RobustAutoPatchAction implements Action<Project> {
    public RobustAutoPatchAction() {
    }

    @Override
    void execute(Project project) {

        project.android.applicationVariants.all { variant ->

            if (isOldAndNewDirExists(project)){
                RobustPatch.initConfig(project,variant);
                setRobustMainJarFromRobustOldAndNew(project)
                RobustPatch.patch(project)
            } else {
//            if (isProguardOpen(variant)) {

                def dexTask = project.tasks.findByName(GradleUtils.getDexTaskName(project, variant));
                dexTask.doFirst {
                    RobustPatch.initConfig(project,variant);
                    RobustPatch.setRobustMainJar(project)
                    RobustPatch.patch(project)
                }

//            }
            }
        }
    }

//    boolean isProguardOpen(def variant) {
//        return variant.getBuildType().buildType.minifyEnabled;
//    }

    public static boolean isOldAndNewDirExists(Project project){
        def robustDir = "${project.projectDir}${File.separator}robust${File.separator}"
        String oldDir = robustDir + File.separator + "old" + File.separator;
        String newDir = robustDir + File.separator + "new" + File.separator;
        if (new File(oldDir).exists() && new File(newDir).exists()){
            return true;
        } else {
            return false;
        }
    }

    public static void setRobustMainJarFromRobustOldAndNew(Project project){
        def robustDir = "${project.projectDir}${File.separator}robust${File.separator}"
        String oldDir = robustDir + File.separator + "old" + File.separator;
        String newDir = robustDir + File.separator + "new" + File.separator;

        Config.methodMap = JavaUtils.getMapFromZippedFile(oldDir+"methodsMap.robust");

        //1. get last class.jar
        File oldMainJarFile = new File(oldDir, Constants.ROBUST_TRANSFORM_MAIN_JAR)
        File oldProGuardJarFile = new File(oldDir, Constants.ROBUST_PROGUARD_MAIN_JAR)

        //2. get current classes 如果是proguard之后，我们插了代码，需要做兼容
        File newMainJarFile = new File(newDir, Constants.ROBUST_TRANSFORM_MAIN_JAR)
        File newProGuradJarFile = new File(newDir, Constants.ROBUST_PROGUARD_MAIN_JAR)
        if (newMainJarFile.exists() || newProGuradJarFile.exists()) {
            if (newProGuradJarFile.exists()) {
                //如果proguard打开了，就使用proguard的包
                if (null == oldProGuardJarFile || !oldProGuardJarFile.exists()) {
                    throw new RuntimeException("you are use proguard, please copy your last build/outputs/robust/" + Constants.ROBUST_PROGUARD_MAIN_JAR + " to app/robust dir ")
                }
                RobustPatch.newMainJarFile = newProGuradJarFile
                RobustPatch.oldMainJarFile = oldProGuardJarFile
                //read mapping 使用新的mapping文件
                RobustProguardMapping.readMapping(newDir + "mapping.txt");
            } else {
                //没有proguard
                RobustPatch.newMainJarFile = newMainJarFile;
                RobustPatch.oldMainJarFile = oldMainJarFile;
            }
        } else {
            throw RuntimeException("robust/new/ dir must contains " + Constants.ROBUST_TRANSFORM_MAIN_JAR + " or " + Constants.ROBUST_PROGUARD_MAIN_JAR)
        }
    }
}