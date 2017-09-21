package robust.gradle.plugin

import com.meituan.robust.Constants
import com.meituan.robust.utils.RobustLog
import groovy.util.slurpersupport.GPathResult
import org.gradle.api.Action
import org.gradle.api.Project
/**
 * Created by hedex on 17/2/21.
 */
public class RobustKeepResourceIdAction implements Action<Project> {
    public RobustKeepResourceIdAction() {
    }

    @Override
    void execute(Project project) {

        project.android.applicationVariants.each { variant ->
            def variantOutput = variant.outputs.first()
            //def variantName = variant.name.capitalize()
            String RDotTxtPath
            String path = project.projectDir.path;
            GPathResult robust = new XmlSlurper().parse(new File("${path}${File.separator}${Constants.ROBUST_XML}"))

            if (robust.resourceFix.RDotTxtFile.name.text() != null && !"".equals(robust.resourceFix.RDotTxtFile.name.text())) {
                RDotTxtPath = robust.resourceFix.RDotTxtFile.name.text()
            } else {
                RDotTxtPath = "${path}${Constants.DEFAULT_R_DOT_TXT_FILE}"
            }

            //keep resource id
            //def processResourcesTask = project.tasks.findByName("process${variantName}Resources")
            def resDir = variantOutput.processResources.resDir
            String resDirStr = resDir.absolutePath
            variantOutput.processResources.doFirst{
                //processResourcesTask.doFirst{
                new KeepResourceId(RDotTxtPath, resDirStr).execute()
                project.logger.quiet("robust: keep resource id applied!")
                RobustLog.log("robust: keep resource id applied!")
            }
        }
    }

}