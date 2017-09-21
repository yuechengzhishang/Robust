package robust.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

public class KeepResourceIdPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.afterEvaluate(new RobustKeepResourceIdAction())
    }
}