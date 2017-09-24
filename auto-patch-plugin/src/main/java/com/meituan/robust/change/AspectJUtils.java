package com.meituan.robust.change;

import java.util.HashSet;

import javassist.ClassPool;
import javassist.CtClass;

/**
 * Created by hedingxu on 17/9/1.
 */

public class AspectJUtils {
    private static final String AJC_CLOSURE_KEY = "$AjcClosure";
    public static final String AJC$PRE_CLINIT = "ajc$preClinit";
    //dot.class
    private static HashSet<String> subAjcClosureSet = new HashSet<String>();

    //todo 注意 $1 也会有 aspectClosure DebugAgentConfigDetailActivity$1$AjcClosure1
    public static HashSet<CtClass> getAjcClosureSet(String sourceClassName, ClassPool classPool) {
        if (null == sourceClassName || null == classPool) {
            return null;
        }

        HashSet<CtClass> ajcClosureSet = new HashSet<CtClass>();
        int index = 1;
        while (true) {
            CtClass ctClass = classPool.getOrNull(sourceClassName + AJC_CLOSURE_KEY + index);
            if (null == ctClass) {
                break;
            } else {
                ajcClosureSet.add(ctClass);
            }
            index += 2;
        }

        if (ajcClosureSet.isEmpty()) {
            return null;
        }

        return ajcClosureSet;
    }

//    public static void main(String[] args){
//        String sourceClassName = "DebugAgentConfigDetailActivity$1";
//        int index = 1;
//        System.err.println(sourceClassName+AJC_CLOSURE_KEY + index );
//    }


    public static boolean isAroundBodyMethod(String methodName) {
        if (methodName.contains("_aroundBody")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isAjc$preClinitMethod(String methodName) {
        if (methodName.equals("ajc$preClinit")) {
            return true;
        } else {
            return false;
        }
    }

//    ajc$preClinit

//    @Aspect annotation ;
    private static HashSet<CtClass> AspectAnnotationCtClassSet = new HashSet<CtClass>();
    private static HashSet<String> AspectAnnotationCtClassNameSet = new HashSet<String>();
    public static void recordAspectAnnotationCtClass(CtClass ctClass){
        AspectAnnotationCtClassSet.add(ctClass);
        AspectAnnotationCtClassNameSet.add(ctClass.getName());
    }

    public static boolean isAspectJPackageOrAnnotationOrAjcClosure(String className){
        if (className.contains(AJC_CLOSURE_KEY)) {
            return true;
        } else if (AspectAnnotationCtClassNameSet.contains(className)){
            return true;
        } else if (className.startsWith("org.aspectj.")){
            return true;
        }
        return false;
    }

//    private static final JoinPoint.StaticPart ajc$tjp_0;
//    private static final JoinPoint.StaticPart ajc$tjp_1;
    public static boolean isAspectJField(String fieldType,String fieldName){
        if (fieldName.startsWith("ajc$")){
            return true;
        } else {
            return false;
        }
    }

//        public static void main(String[] args){
//        String sourceClassName = "DebugAgentConfigDetailActivity$1";
//        int index = 1;
//            System.err.println(isAspectJField("ajctjp_0","ajctjp_0"));
//    }

}
