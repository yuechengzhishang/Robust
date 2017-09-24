-keep class com.meituan.robust.**{*;}
-keep class meituan.robust.**{*;}
-keep class com.google.gson.**{*;}
-keepattributes *Annotation*
-optimizations !method/removal/parameter
-keepattributes EnclosingMethod
-keepclassmembers class * {
    private static final org.aspectj.lang.JoinPoint** *;
    *** *_aroundBody*(...);
    *** ajc$preClinit();
}
-keep class org.aspectj.** {*;}
-keep class * extends org.aspectj.runtime.internal.AroundClosure{*;}
