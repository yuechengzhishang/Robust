package robust.gradle.plugin

import com.android.SdkConstants
import com.android.build.gradle.api.BaseVariant
import com.android.builder.model.AndroidProject
import com.google.common.collect.Sets
import org.apache.commons.io.FileUtils
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task

import java.lang.reflect.Field

class GradleUtils {
    static String getProGuardTaskName(Project project, BaseVariant variant) {
        if (isGradle140orAbove(project)) {
            return "transformClassesAndResourcesWithProguardFor${variant.name.capitalize()}"
        } else {
            return "proguard${variant.name.capitalize()}"
        }
    }

    static String getDexTaskName(Project project, BaseVariant variant) {
        if (isGradle140orAbove(project)) {
            return "transformClassesWithDexFor${variant.name.capitalize()}"
        } else {
            return "dex${variant.name.capitalize()}"
        }
    }

    static String getJarMergingTaskName(Project project, BaseVariant variant) {
        if (isGradle140orAbove(project)) {
            return "transformClassesWithJarMergingFor${variant.name.capitalize()}"
        } else {
            return ""
        }
    }

    static File getJarMergingOutputJar(Project project, BaseVariant variant, Task dexTask) {
        if (isGradle140orAbove(project)) {
            File proGuardOutputJar = null
            Set<File> files = Sets.newHashSet();

            def jarMergingFolder = new File("${project.buildDir}${File.separator}${AndroidProject.FD_INTERMEDIATES}${File.separator}transforms${File.separator}jarMerging${File.separator}${variant.dirName}")
            if (jarMergingFolder.exists() && jarMergingFolder.isDirectory()) {
                def extensions = [SdkConstants.EXT_JAR] as String[]
                Collection<File> jars = FileUtils.listFiles(jarMergingFolder, extensions, true);
                files.addAll(jars)

                files.each {
                    if (!it.path.contains("main-dex") && !it.path.contains("secondary-dex") && !it.path.contains("inline-dex") && (it.name.equals("main.jar") || it.name.equals("classes.jar") || it.name.equals("combined.jar"))) {
                        proGuardOutputJar = it
                    }
                }

                if (proGuardOutputJar == null) {
                    throw new GradleException("Fail to get JarMerging output folder")
                } else {
                    return proGuardOutputJar
                }
            }
            return null
        } else {
            return new File("${project.buildDir}${File.separator}${AndroidProject.FD_INTERMEDIATES}${File.separator}multi-dex${File.separator}${variant.dirName}${File.separator}allclasses.jar")
        }
    }

    static File getProGuardTaskOutputJar(Project project, BaseVariant variant, Task dexTask) {
        if (dexTask == null) {
            dexTask = project.tasks.findByName(getDexTaskName(project, variant));
        }

        if (isGradle140orAbove(project)) {
            File proGuardOutputJar = null
            Set<File> files = Sets.newHashSet();

            dexTask.inputs.files.files.each {
                def extensions = [SdkConstants.EXT_JAR] as String[]
                if (it.exists()) {
                    if (it.isDirectory()) {
                        Collection<File> jars = FileUtils.listFiles(it, extensions, true);
                        files.addAll(jars)
                    } else if (it.name.endsWith(SdkConstants.DOT_JAR)) {
                        files.add(it)
                    }
                }
            }

            files.each {
                if (!it.path.contains("main-dex") && !it.path.contains("secondary-dex") && !it.path.contains("inline-dex") && (it.name.equals("main.jar") || it.name.equals("classes.jar"))) {
                    proGuardOutputJar = it
                }
            }

            if (proGuardOutputJar == null) {
                throw new GradleException("Fail to get ProGuard output folder")
            } else {
                return proGuardOutputJar
            }
        } else {
            return new File("${project.buildDir}${File.separator}${AndroidProject.FD_INTERMEDIATES}${File.separator}classes-proguard${File.separator}${variant.dirName}${File.separator}classes.jar")
        }
    }

    public static boolean isGradle140orAbove(Project project) {
        Class<?> versionClazz = null
        try {
            versionClazz = Class.forName("com.android.builder.Version")
        } catch (Exception e) {
        }
        if (versionClazz == null) {
            return false
        } else {
            Field pluginVersionField = versionClazz.getField("ANDROID_GRADLE_PLUGIN_VERSION")
            pluginVersionField.setAccessible(true)
            String version = pluginVersionField.get(null)

            return versionCompare(version, "1.4.0") >= 0;
        }
    }

    /**
     * Compares two version strings.
     *
     * Use this instead of String.compareTo() for a non-lexicographical
     * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
     *
     * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
     *
     * @param str1 a string of ordinal numbers separated by decimal points.
     * @param str2 a string of ordinal numbers separated by decimal points.
     * @return The result is a negative integer if str1 is _numerically_ less than str2.
     *         The result is a positive integer if str1 is _numerically_ greater than str2.
     *         The result is zero if the strings are _numerically_ equal.
     */
    private static int versionCompare(String str1, String str2) {
        String[] vals1 = str1.split("-")[0].split("\\.");
        String[] vals2 = str2.split("-")[0].split("\\.");
        int i = 0;
        // set index to first non-equal ordinal or length of shortest version string
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
            i++;
        }

        // compare first non-equal ordinal number
        if (i < vals1.length && i < vals2.length) {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        }

        // the strings are equal or one string is a substring of the other
        // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
        else {
            return Integer.signum(vals1.length - vals2.length);
        }
    }

}
