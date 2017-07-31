package robust.gradle.plugin

import org.gradle.api.Action
import org.gradle.api.Project
/**
 * Created by hedex on 17/2/14.
 */
class RobustStoreClassAction implements Action<Project> {
    @Override
    void execute(Project project) {
        project.android.applicationVariants.each { variant ->

//            def dexTask = project.tasks.findByName("transformClassesWithDexFor${variant.name.capitalize()}")
//            if (dexTask == null) {
//                return
//            }
//
//            dexTask.doFirst {
//                TaskInputs taskInputs = dexTask.getInputs()
//                // Gather a full list of all inputs.
//                List<JarInput> jarInputs = Lists.newArrayList();
//                List<DirectoryInput> directoryInputs = Lists.newArrayList();
//                for (TransformInput input : taskInputs) {
//                    jarInputs.addAll(input.getJarInputs());
//                    directoryInputs.addAll(input.getDirectoryInputs());
//                }
//                project.logger.info("JarInputs %s", Joiner.on(",").join(jarInputs));
//                project.logger.info("DirInputs %s", Joiner.on(",").join(directoryInputs));
//
////                project.logger.quiet("===start compute robust apk hash===")
////                def startTime = System.currentTimeMillis()
//
////                def cost = (System.currentTimeMillis() - startTime) / 1000
////                logger.quiet "robust apk hash is $robustHash"
////                logger.quiet "compute robust apk hash cost $cost second"
////                project.logger.quiet("===compute robust apk hash end===")
//            }
        }
    }
}