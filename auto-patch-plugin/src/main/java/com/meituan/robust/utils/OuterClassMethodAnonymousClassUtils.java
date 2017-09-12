package com.meituan.robust.utils;

import com.meituan.robust.autopatch.Config;
import com.meituan.robust.change.RobustChangeInfo;
import com.meituan.robust.change.comparator.DiffLineByLine;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.util.List;
import java.util.jar.JarFile;

import javassist.CtMethod;

import static com.meituan.robust.Constants.File_SEPARATOR;
import static com.meituan.robust.Constants.INIT_ROBUST_PATCH;
import static com.meituan.robust.autopatch.Config.recordAnonymousLambdaOuterMethodMap;

/**
 * Created by hedingxu on 17/8/29.
 */

public class OuterClassMethodAnonymousClassUtils {
    private OuterClassMethodAnonymousClassUtils() {

    }

//    ======RecordOuterClassMethod
//    anonymousClass: com/meituan/sample/test/e
//    outerClass: com/meituan/sample/test/TestBadClassActivity
//    outerMethod: onCreate
//    outerMethodDesc: (Landroid/os/Bundle;)V
//    ======RecordOuterClassMethod
//    anonymousClass: com/meituan/sample/test/f
//    outerClass: com/meituan/sample/test/TestBadClassActivity
//    outerMethod: null
//    outerMethodDesc: null

    public static void recordOuterClassMethod(ClassNode anonymousClassNode,JarFile newJarFile) {
        // TODO: 17/9/1 需要记录改了lambda表达式的所包含的方法 完成一半
        if (AnonymousLambdaUtils.isAnonymousInnerClass_$1(anonymousClassNode.name.replace(".class", "").replace(File_SEPARATOR, "."))) {
            String anonymousInnerClassclassName = anonymousClassNode.name.replace(".class", "").replace(File_SEPARATOR, ".");

            String outerClass = anonymousClassNode.outerClass;
            String outerMethod = anonymousClassNode.outerMethod;
            String outerMethodDesc = anonymousClassNode.outerMethodDesc;
            System.err.println("======RecordOuterClassMethod");
            System.err.println("anonymousClass: " + anonymousClassNode.name);
            System.err.println("outerClass: " + outerClass);
            System.err.println("outerMethod: " + outerMethod);
            System.err.println("outerMethodDesc: " + outerMethodDesc);
//case
//        isAnonymousInnerClass: com/meituan/sample/TestPatchActivity$1
//        outerClass: com/meituan/sample/TestPatchActivity
//        outerMethod: onCreate
//        outerMethodDesc: (Landroid/os/Bundle;)V

//todo 解决类似于这种问题 9-1
//            private View.OnClickListener listener = new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Log.e("robust","hello click");
//                }
//            };
            if (null == outerMethod){//outerMethod sometime is null
                return;
            }
            if (!Config.recordOuterMethodModifiedAnonymousClassNameList.contains(anonymousInnerClassclassName)){
                Config.recordOuterMethodModifiedAnonymousClassNameList.add(anonymousInnerClassclassName);
            }

            if (recordAnonymousLambdaOuterMethodMap.containsKey(anonymousInnerClassclassName)){
                System.err.println("recordOuterClassMethod(ClassNode anonymousClassNode) already has invoked once ...");
            } else {
                String dotOuterClass = outerClass.replace("/", ".");
                OuterMethodInfo outerMethodInfo = new OuterMethodInfo(dotOuterClass, outerMethod, outerMethodDesc);

                try {
                    ClassNode outerClassNode = DiffLineByLine.getLambdaClassNode(anonymousClassNode.outerClass,newJarFile);
                    for (MethodNode methodNode: (List<MethodNode>) outerClassNode.methods){
                        if (methodNode.name.equals(anonymousClassNode.outerMethod) && methodNode.desc.equals(anonymousClassNode.outerMethodDesc)){
                            RobustChangeInfo.ClassChange outerClassChange = new RobustChangeInfo.ClassChange();
                            outerClassChange.classNode = outerClassNode;
                            outerClassChange.methodChange = new RobustChangeInfo.MethodChange();
                            outerClassChange.methodChange.changeList.add(methodNode);
                            RobustChangeInfo.changeClassesByAnonymousLambdaClass.add(outerClassChange);
                            break;
                        }
                    }
                } catch (IOException e) {
                    RobustLog.log("IOException 81",e);
                }
                recordAnonymousLambdaOuterMethodMap.put(anonymousInnerClassclassName,outerMethodInfo);
            }
        }
    }

    public static boolean isModifiedByAnonymous(String outerClassName, CtMethod ctMethod){
        for (OuterMethodInfo outerMethodInfo : recordAnonymousLambdaOuterMethodMap.values()){
            if (outerClassName.equals(outerMethodInfo.outerClass)){
                if (ctMethod.getName().equals(outerMethodInfo.outerMethod)){
                    if (ctMethod.getSignature().equals(outerMethodInfo.outerMethodDesc)){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public static class OuterMethodInfo {
        public String outerClass;
        public String outerMethod;
        public String outerMethodDesc;

        public OuterMethodInfo(String outerClass,
                               String outerMethod,
                               String outerMethodDesc) {
            this.outerClass = outerClass;
            this.outerMethod = outerMethod;
            if (this.outerMethod.equals("<init>")){
                this.outerMethod = INIT_ROBUST_PATCH;
            }
            this.outerMethodDesc = outerMethodDesc;
        }

        @Override
        public int hashCode() {
            return new String(outerClass + "," + outerMethod + "," + outerMethodDesc).hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof OuterMethodInfo) {
                OuterMethodInfo outerMethodInfo = (OuterMethodInfo) obj;
                if (outerMethodInfo.outerClass.equals(this.outerClass)
                        && outerMethodInfo.outerMethod.equals(this.outerMethod)
                        && outerMethodInfo.outerMethodDesc.equals(this.outerMethodDesc)
                        ) {
                    return true;

                }
            }
            return false;
        }
    }
}
