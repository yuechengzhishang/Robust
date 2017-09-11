//package com.meituan.robust.autopatch;
//
//import com.meituan.robust.utils.AnonymousLambdaUtils;
//import com.meituan.robust.utils.ProguardUtils;
//
//import org.objectweb.asm.tree.ClassNode;
//
//import javassist.CtMethod;
//
//import static com.meituan.robust.Constants.File_SEPARATOR;
//import static com.meituan.robust.autopatch.OuterClassMethodAnonymousClassUtils.anonymousLambdaOuterMethodMap;
//
///**
// * lambda不用记录outerClass，只要lambda改了，对应的outerClass肯定改了
// * Created by hedingxu on 17/8/29.
// */
//
//public class OuterClassMethodLambdaClassUtils {
//    private OuterClassMethodLambdaClassUtils() {
//
//    }
//
//    public static void recordOuterClassMethod(ClassNode lambdaClassNode) {
//        //TODO: 17/9/1 需要记录改了lambda表达式的所包含的方法 未完成
//        String dotClassName = lambdaClassNode.name.replace(".class", "").replace("/", ".");
//        if (AnonymousLambdaUtils.isAnonymousInnerClass_$$Lambda$1(dotClassName)) {
//            String lambdaClassName = lambdaClassNode.name.replace(".class", "").replace(File_SEPARATOR, ".");
//            if (!Config.recordOuterMethodModifiedLambdaClassNameList.contains(lambdaClassName)){
//                Config.recordOuterMethodModifiedLambdaClassNameList.add(lambdaClassName);
//            }
//
//
//            String outerClass = lambdaClassNode.outerClass;
//            String outerMethod = lambdaClassNode.outerMethod;
//            String outerMethodDesc = lambdaClassNode.outerMethodDesc;
//            System.err.println("======RecordOuterClassMethod");
//            System.err.println("lambdaClass: " + lambdaClassNode.name);
//            System.err.println("outerClass: " + outerClass);
//            System.err.println("outerClass2: " + ProguardUtils.getLambdaClassOuterClassDotName(dotClassName));
//            System.err.println("outerMethod: " + outerMethod);
//            System.err.println("outerMethodDesc: " + outerMethodDesc);
//            System.err.println();
////case
////        isAnonymousInnerClass: com/meituan/sample/TestPatchActivity$1
////        outerClass: com/meituan/sample/TestPatchActivity
////        outerMethod: onCreate
////        outerMethodDesc: (Landroid/os/Bundle;)V
//
////todo 解决类似于这种问题 9-9
////            private View.OnClickListener listener = new View.OnClickListener() {
////                @Override
////                public void onClick(View v) {
////                    Log.e("robust","hello click");
////                }
////            };
//            if (null == outerMethod){//outerMethod sometime is null
//                return;
//            }
//            if (anonymousLambdaOuterMethodMap.containsKey(lambdaClassName)){
//                System.err.println("recordOuterClassMethod(ClassNode anonymousClassNode) already has invoked once ...");
//            } else {
//                String dotOuterClass = outerClass.replace("/", ".");
//                OuterClassMethodAnonymousClassUtils.OuterMethodInfo outerMethodInfo = new OuterClassMethodAnonymousClassUtils.OuterMethodInfo(dotOuterClass, outerMethod, outerMethodDesc);
//                anonymousLambdaOuterMethodMap.put(lambdaClassName,outerMethodInfo);
//            }
//        }
//    }
//
//    public static boolean isModifiedByAnonymous(String outerClassName, CtMethod ctMethod){
//        for (OuterClassMethodAnonymousClassUtils.OuterMethodInfo outerMethodInfo : anonymousLambdaOuterMethodMap.values()){
//            if (outerClassName.equals(outerMethodInfo.outerClass)){
//                if (ctMethod.getName().equals(outerMethodInfo.outerMethod)){
//                    if (ctMethod.getSignature().equals(outerMethodInfo.outerMethodDesc)){
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }
//}
