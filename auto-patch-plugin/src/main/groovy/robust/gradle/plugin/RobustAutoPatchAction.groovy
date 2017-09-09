package robust.gradle.plugin

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

//            if (isProguardOpen(variant)) {

                def dexTask = project.tasks.findByName(GradleUtils.getDexTaskName(project, variant));
                dexTask.doFirst {
                    CodeTransformUnion.initConfig(project,variant);
                    CodeTransformUnion.transform(project)
                }

//            }
        }
    }

    boolean isProguardOpen(def variant) {
        return variant.getBuildType().buildType.minifyEnabled;
    }
}