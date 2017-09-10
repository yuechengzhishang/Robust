package com.meituan.robust.utils;

import com.meituan.robust.autopatch.Config;

import java.util.HashMap;

/**
 * Created by hedingxu on 17/9/10.
 */

public class CustomModifiedClassUtils {
    public static HashMap<String,String> customModifiedClasses = null;
    public static HashMap<String,String> getCustomModifiedClasses(){
        if (null == customModifiedClasses){
            customModifiedClasses= new HashMap<String,String>();
            for (String className : Config.modifiedClassNameList){
                boolean is_$1_or_$$lambda$1 = AnonymousLambdaUtils.isAnonymousInnerClass_$1(className) || AnonymousLambdaUtils.isAnonymousInnerClass_$$Lambda$1(className);
                if (is_$1_or_$$lambda$1){

                } else {
                    String modifiedClassName = className;
                    String patchClassName = className + "Patch";
                    customModifiedClasses.put(modifiedClassName,patchClassName);
                    String modifiedClassNameAsm = modifiedClassName.replace(".","/");
                    String patchClassNameAsm = patchClassName.replace(".","/");
                    customModifiedClasses.put(modifiedClassNameAsm,patchClassNameAsm);
                }
            }
        }
        return customModifiedClasses;
    }
}
