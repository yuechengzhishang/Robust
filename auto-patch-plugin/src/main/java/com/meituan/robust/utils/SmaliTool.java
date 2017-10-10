package com.meituan.robust.utils;

import com.meituan.robust.Constants;
import com.meituan.robust.autopatch.Config;
import com.meituan.robust.autopatch.NameManger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import javassist.CtMethod;
import javassist.CtPrimitiveType;

import static com.meituan.robust.Constants.PACKNAME_END;
import static com.meituan.robust.Constants.PACKNAME_START;
import static com.meituan.robust.autopatch.Config.invokeSuperMethodMap;

/**
 * Created by mivanzhang on 17/2/8.
 */

public class SmaliTool {
    private SmaliTool() {

    }

    public static void dealObscureInSmali() {
        File diretory = new File(Config.robustGenerateDirectory + "classout");
        if (!diretory.isDirectory() || diretory == null) {
            throw new RuntimeException(Config.robustGenerateDirectory + Config.patchPackageName.replaceAll(".", Matcher.quoteReplacement(File.separator)) + " contains no smali file error!! ");
        }
        final List<File> smaliFileList = new ArrayList<File>();

        try {
            Files.walkFileTree(diretory.toPath(), new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toFile().getName().contains("RobustAssist")){
                        com.meituan.robust.utils.RobustLog.log("RobustAssist File : " + file);
                        smaliFileList.add(file.toFile());
                    }
                    return FileVisitResult.CONTINUE;
                }

            });
        } catch (IOException e) {
            RobustLog.log("NotFoundException ",e);
        }
        for (File file : smaliFileList) {
            BufferedWriter writer = null;
            BufferedReader reader = null;
            StringBuilder fileContent = new StringBuilder();
            try {
                reader = new BufferedReader(new FileReader(file));
                String line;
                int lineNo = 1;
                // 一次读入一行，直到读入null为文件结束
                while ((line = reader.readLine()) != null) {
                    // 显示行号
                    fileContent.append(dealWithSmaliLine(line, JavaUtils.getFullClassNameFromFile(file.getPath())) + "\n");
                    lineNo++;
                }
                writer = new BufferedWriter(new FileWriter(file));
                writer.write(fileContent.toString());
                writer.flush();
            } catch (IOException e) {
                RobustLog.log("NotFoundException ",e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e1) {
                        RobustLog.log("NotFoundException ",e1);
                    }
                }
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e1) {
                        RobustLog.log("NotFoundException ",e1);
                    }
                }
            }
        }
    }

    private static String dealWithSmaliLine(final String line, String fullClassName) {

        if (null == line || line.length() < 1 || line.startsWith("#")) {
            return line;
        }
        String result = invokeSuperMethodInSmali(line, fullClassName);
        return result;
    }

    private static boolean isInStaticRobustMethod = false;

    private static String invokeSuperMethodInSmali(final String line, String fullClassName) {

        if (line.startsWith(".method public static staticRobust")) {
            isInStaticRobustMethod = true;
        }
        String result = line;
        String returnType;
        List<CtMethod> invokeSuperMethodList = invokeSuperMethodMap.get(NameManger.getInstance().getPatchNameMap().get(fullClassName));
        if (isInStaticRobustMethod && line.contains(Constants.SMALI_INVOKE_VIRTUAL_COMMAND)) {
            for (CtMethod ctMethod : invokeSuperMethodList) {
                //java method signure
                if ((ctMethod.getName().replaceAll("\\.", "/") + ctMethod.getSignature().subSequence(0, ctMethod.getSignature().indexOf(")") + 1)).equals(getMethodSignureInSmaliLine(line))) {
                    result = line.replace(Constants.SMALI_INVOKE_VIRTUAL_COMMAND, Constants.SMALI_INVOKE_SUPER_COMMAND);
                    try {
                        if (!ctMethod.getReturnType().isPrimitive()) {
                            returnType = "L" + ctMethod.getReturnType().getName().replaceAll("\\.", "/");
                        } else {
                            returnType = String.valueOf(((CtPrimitiveType) ctMethod.getReturnType()).getDescriptor());
                        }
                        if (NameManger.getInstance().getPatchNameMap().get(fullClassName).equals(fullClassName)) {
                            result = result.replace("p0", "p1");
                        }
                        String fullClassNameInSmali = ctMethod.getDeclaringClass().getClassPool().get(fullClassName).getSuperclass().getName().replaceAll("\\.", "/");
                        result = result.replace(result.substring(result.indexOf(PACKNAME_START) + 1, result.indexOf(PACKNAME_END)), fullClassNameInSmali);
                        result = result.substring(0, result.indexOf(")") + 1) + returnType;
                        if (!ctMethod.getReturnType().isPrimitive()) {
                            result += ";";
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        if (isInStaticRobustMethod && line.startsWith(".end method")) {
            isInStaticRobustMethod = false;
        }
        return result;
    }

    private static String getMethodSignureInSmaliLine(String s) {
        return s.substring(s.indexOf("->") + 2, s.indexOf(")") + 1);
    }
}
