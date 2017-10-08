package robust.gradle.plugin

import com.meituan.robust.Constants
import com.meituan.robust.common.FileUtil
import org.gradle.api.Action
import org.gradle.api.Project
/**
 * Created by hedex on 17/2/14.
 */
public class RobustStoreClassAction implements Action<Project> {
    @Override
    void execute(Project project) {
        project.android.applicationVariants.all { variant ->

            if (isProguardOpen(variant)) {

                def proGuardTask = project.tasks.findByName(GradleUtils.getProGuardTaskName(project, variant));
                def dexTask = project.tasks.findByName(GradleUtils.getDexTaskName(project, variant));

                def jarMergingTask = null;
                if (!GradleUtils.getJarMergingTaskName(project, variant).isEmpty()) {
                    jarMergingTask = project.tasks.findByName(GradleUtils.getJarMergingTaskName(project, variant));
                }

                proGuardTask.doLast {
                    File proGuardJar;
                    if (proGuardTask != null) {
                        proGuardJar = GradleUtils.getProGuardTaskOutputJar(project, variant, dexTask);
                    } else if (jarMergingTask != null) {
                        proGuardJar = GradleUtils.getJarMergingOutputJar(project, variant, dexTask);
                    }

                    if (proGuardJar != null && proGuardJar.exists()) {
                        File robustOutDirFile = new File(project.buildDir.path + File.separator + Constants.ROBUST_GENERATE_DIRECTORY);
                        File storeMainJarFile = new File(robustOutDirFile, Constants.ROBUST_PROGUARD_MAIN_JAR)
                        FileUtil.copyFile(proGuardJar, storeMainJarFile)
//                        if (storeMainJarFile.exists()) {
//                            File storeM = new File(robustOutDirFile,Constants.ROBUST_TRANSFORM_MAIN_JAR)
//                            if (storeM.exists()) {
//                                try {
//                                    storeM.delete()
//                                } catch (IOException ioe){
//
//                                }
//                            }
//                        }
                    } else {
                        println("The proguard's jar doesn't exist.");
                    }
                }

            }
        }
    }

    boolean isProguardOpen(def variant) {
        return variant.getBuildType().buildType.minifyEnabled;
    }
}