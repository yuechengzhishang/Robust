package com.meituan.robust.autopatch;

import org.objectweb.asm.tree.ClassNode;

import java.util.HashMap;

import javassist.CtMethod;

import static com.meituan.robust.Constants.File_SEPARATOR;
import static com.meituan.robust.Constants.INIT_ROBUST_PATCH;

/**
 * Created by hedingxu on 17/8/29.
 */

public class AnonymousClassOuterClassMethodUtils {
    private AnonymousClassOuterClassMethodUtils() {

    }

    public static void recordOuterClassMethod(ClassNode anonymousClassNode) {
        if (CheckCodeChanges.isAnonymousInnerClass(anonymousClassNode.name.replace(".class", "").replace(File_SEPARATOR, "."))) {
            String anonymousInnerClassclassName = anonymousClassNode.name.replace(".class", "").replace(File_SEPARATOR, ".");
            if (!Config.modifiedAnonymousInnerClassNameList.contains(anonymousInnerClassclassName)){
                Config.modifiedAnonymousInnerClassNameList.add(anonymousInnerClassclassName);
            }
            String outerClass = anonymousClassNode.outerClass;
            String outerMethod = anonymousClassNode.outerMethod;
            String outerMethodDesc = anonymousClassNode.outerMethodDesc;
            System.err.println("======MMMMM");
            System.err.println("isAnonymousInnerClass: " + anonymousClassNode.name);
            System.err.println("outerClass: " + outerClass);
            System.err.println("outerMethod: " + outerMethod);
            System.err.println("outerMethodDesc: " + outerMethodDesc);
            System.err.println("======NNNNN");
//case
//        isAnonymousInnerClass: com/meituan/sample/TestPatchActivity$1
//        outerClass: com/meituan/sample/TestPatchActivity
//        outerMethod: onCreate
//        outerMethodDesc: (Landroid/os/Bundle;)V

//todo 解决类似于这种问题
//            private View.OnClickListener listener = new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Log.e("robust","hello click");
//                }
//            };
            if (null == outerMethod){
                return;
            }
            if (changedAnonymousOuterMethodInfoMap.containsKey(anonymousInnerClassclassName)){
                System.err.println("recordOuterClassMethod(ClassNode anonymousClassNode) already has invoked once ...");
            } else {
                String dotOuterClass = outerClass.replace(File_SEPARATOR, ".");
                OuterMethodInfo outerMethodInfo = new OuterMethodInfo(dotOuterClass, outerMethod, outerMethodDesc);
                changedAnonymousOuterMethodInfoMap.put(anonymousInnerClassclassName,outerMethodInfo);
            }
        }
    }

    public static HashMap<String, OuterMethodInfo> changedAnonymousOuterMethodInfoMap = new HashMap<String, OuterMethodInfo>();


    public static boolean isModifiedByAnonymous(String outerClassName, CtMethod ctMethod){
        for (OuterMethodInfo outerMethodInfo : changedAnonymousOuterMethodInfoMap.values()){
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
