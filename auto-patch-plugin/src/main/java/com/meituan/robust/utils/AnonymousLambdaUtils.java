package com.meituan.robust.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hedingxu on 17/8/9.
 */

public class AnonymousLambdaUtils {
    public static void main(String[] args) {
        String case1 = "com.meituan.sample.MainActivity$3";
        String case2 = "com.dianping.ad.view.BannerAdView$$Lambda$1";
        String case3 = "com.meituan.android.baby.poi.agent.BabyPoiPromoAgent$AjcClosure1";
        String case4 = "android.support.design.widget.AppBarLayout$Behavior$SavedState$1";
        String case5 = "android.support.design.widget.BaseTransientBottomBar$5$1";
        List<String> cases = new ArrayList<>();
        cases.add(case1);
        cases.add(case2);
        cases.add(case3);
        cases.add(case4);
        cases.add(case5);
        for (String case11 : cases) {
            System.err.println();
            System.err.println(case11 + " like $1 : " + isAnonymousInnerClass_$1(case11));
            System.err.println(case11 + " like $$Lambda$1 : " + isAnonymousInnerClass_$$Lambda$1(case11));
            System.err.println(case11 + " like $AjcClosure1 : " + isAnonymousInnerClass_$AjcClosure1(case11));
        }
    }

    public static boolean isAnonymousInnerClass_$AjcClosure1(String className) {
        /*            case: com.dianping.ad.view.BannerAdView$$Lambda$1 第2种case
              case: com.meituan.sample.MainActivity$3 第1种case
              case: com.meituan.android.baby.poi.agent.BabyPoiPromoAgent$AjcClosure1 第3种case
              case: android.support.design.widget.AppBarLayout$Behavior$SavedState$1 跟普通匿名内部类一样
              case: android.support.design.widget.BaseTransientBottomBar$5$1 匿名内部类的匿名内部类
*/
        String newExprClassName = className;
        if (RobustProguardMapping.isProguard()) {
            newExprClassName = RobustProguardMapping.getUnProguardName(className);
        }
        if (newExprClassName.contains("$AjcClosure")) {
            String[] splits = newExprClassName.split("\\$");
            int length = splits.length;
            String checkStr = splits[length - 1];

            String patternString = "AjcClosure[0-9]*";
            boolean isAnonymousInnerClass = false;
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(checkStr);
            boolean matches = matcher.matches();
            isAnonymousInnerClass = matches;
            return isAnonymousInnerClass;

        }
        return false;
    }

    public static boolean isAnonymousInnerClass_$$Lambda$1(String className) {
        /*            case: com.dianping.ad.view.BannerAdView$$Lambda$1 第2种case
              case: com.meituan.sample.MainActivity$3 第1种case
              case: com.meituan.android.baby.poi.agent.BabyPoiPromoAgent$AjcClosure1 第3种case
              case: android.support.design.widget.AppBarLayout$Behavior$SavedState$1 跟普通匿名内部类一样
              case: android.support.design.widget.BaseTransientBottomBar$5$1 匿名内部类的匿名内部类
*/
        String newExprClassName = className;
        if (RobustProguardMapping.isProguard()) {
            newExprClassName = RobustProguardMapping.getUnProguardName(className);
        }
        if (newExprClassName.contains("$$Lambda$")) {
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

            return isAnonymousInnerClass;
        }
        return false;
    }

    public static boolean isAnonymousInnerClass_$1(String className) {
/*            case: com.dianping.ad.view.BannerAdView$$Lambda$1 第2种case
              case: com.meituan.sample.MainActivity$3 第1种case
              case: com.meituan.android.baby.poi.agent.BabyPoiPromoAgent$AjcClosure1 第3种case
              case: android.support.design.widget.AppBarLayout$Behavior$SavedState$1 跟普通匿名内部类一样
              case: android.support.design.widget.BaseTransientBottomBar$5$1 匿名内部类的匿名内部类
*/
        if (isAnonymousInnerClass_$$Lambda$1(className)){
            return false;
        }
        if (isAnonymousInnerClass_$AjcClosure1(className)){
            return false;
        }

        String newExprClassName = className;
        if (RobustProguardMapping.isProguard()) {
            newExprClassName = RobustProguardMapping.getUnProguardName(className);
        }
        if (newExprClassName.contains("$")) {
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

//    public static boolean isAnonymousInnerClass(String className){
//        return isAnonymousInnerClass_$1(className);
////        return
////                isAnonymousInnerClass_$$Lambda$1(className)
////                || isAnonymousInnerClass_$1(className)
////                || isAnonymousInnerClass_$AjcClosure1(className);
//    }

}
