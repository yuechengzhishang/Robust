package robust.gradle.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.gradle.api.Plugin
import org.gradle.api.Project
/**
 * Created by mivanzhang on 16/7/21.
 *
 * AutoPatchTransform generate patch dex
 */
class AutoPatchTransform extends Transform implements Plugin<Project> {
    Project project

    @Override
    void apply(Project target) {
        this.project = target
//        project.android.registerTransform(this)
        target.afterEvaluate(new RobustAutoPatchAction())
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

    }
}