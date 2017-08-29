package com.meituan.robust.autopatch;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javassist.CtClass;
import javassist.NotFoundException;

/**
 * Created by hedingxu on 17/8/11.
 */

public class AnonymousInnerClassUtil {
    public static boolean isAnonymousInnerClass(String className) {
        String newExprClassName = className;
        if (newExprClassName.endsWith("$")) {
            String[] splits = newExprClassName.split("\\$");
            int length = splits.length;
            String checkStr = splits[length - 1];


            boolean isAnonymousInnerClass = false;
            String patternString = "[0-9]*";
            {
                Pattern pattern = Pattern.compile(patternString);
                Matcher matcher = pattern.matcher(checkStr);
                boolean matches = matcher.matches();
                isAnonymousInnerClass = matches;
            }
            boolean isAjcClosureAnonymousInnerClass = false;
            {
                String ajcClosureAnonymousInnerClass = "AjcClosure" + patternString;
                Pattern ajcClosurePattern = Pattern.compile(ajcClosureAnonymousInnerClass);
                Matcher ajcClosureMatcher = ajcClosurePattern.matcher(checkStr);
                boolean ajcClosureMatches = ajcClosureMatcher.matches();
                isAjcClosureAnonymousInnerClass = ajcClosureMatches;
            }

            if (isAjcClosureAnonymousInnerClass || isAnonymousInnerClass) {

                return true;
            }
        }
        return false;
    }

    public static List<String> getAnonymousInnerClass(CtClass sourceClass) throws NotFoundException {
        List<String> anonymousInnerClasses = new ArrayList<String>();
        CtClass[] ctClasses = sourceClass.getNestedClasses();
            for (CtClass ctClass : ctClasses) {
//              case: com.dianping.ad.view.BannerAdView$$Lambda$1
//              case: com.meituan.sample.MainActivity$3
//              case: com.meituan.android.baby.poi.agent.BabyPoiPromoAgent$AjcClosure1 AspectJ产生的内部类
//              case: android.support.design.widget.AppBarLayout$Behavior$SavedState$1 跟普通匿名内部类一样
//              case: android.support.design.widget.BaseTransientBottomBar$5$1 匿名内部类的匿名内部类
                String nestedClassName = ctClass.getName();
                String shortClassName = nestedClassName.replace(sourceClass.getName(), "");
                if (CheckCodeChanges.isAnonymousInnerClass(shortClassName)){
                    anonymousInnerClasses.add(ctClass.getName());
                }
//                String numberStr = shortClassName.replace("$$Lambda", "");
////                numberStr = numberStr.replace("","");
//                numberStr = numberStr.replace("$", "");
//
//                boolean isAnonymousInnerClass = false;
//                String patternString = "[0-9]*";
//                {
//                    Pattern pattern = Pattern.compile(patternString);
//                    Matcher matcher = pattern.matcher(numberStr);
//                    boolean matches = matcher.matches();
//                    isAnonymousInnerClass = matches;
//                }
//                boolean isAjcClosureAnonymousInnerClass = false;
//                {
//                    String ajcClosureAnonymousInnerClass = "AjcClosure" + patternString;
//                    Pattern ajcClosurePattern = Pattern.compile(ajcClosureAnonymousInnerClass);
//                    Matcher ajcClosureMatcher = ajcClosurePattern.matcher(numberStr);
//                    boolean ajcClosureMatches = ajcClosureMatcher.matches();
//                    isAjcClosureAnonymousInnerClass = ajcClosureMatches;
//                }
//
//                if (isAnonymousInnerClass || isAjcClosureAnonymousInnerClass) {
//                    System.err.println("isAnonymousInnerClass_$1 :" + ctClass.getName());
//
//                } else {
//                    System.err.println("notAnonymousInnerClass :" + ctClass.getName());
//                }
            }
            return anonymousInnerClasses;
    }

}
