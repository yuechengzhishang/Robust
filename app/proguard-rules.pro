# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/wukun/android-sdk-mac_x86/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
#-applymapping /Users/hedingxu/robust-github/Robust/app/robust/mapping.txt
-dontwarn
-keepattributes Signature,SourceFile,LineNumberTable
-keepattributes *Annotation*
-keeppackagenames
-ignorewarnings
-dontwarn android.support.v4.**,**CompatHoneycomb,com.tenpay.android.**
-optimizations !class/unboxing/enum,!code/simplification/arithmetic
-keepclassmembers class * {
#    private static final org.aspectj.lang.JoinPoint.StaticPart *;
#    private static final org.aspectj.lang.JoinPoint$StaticPart *;
    private static final org.aspectj.lang.JoinPoint** *;
    *** *_aroundBody*(...);
    *** ajc$preClinit();
}
-keep class org.aspectj.lang.JoinPoint** {*;}
-keep class org.aspectj.lang.JoinPoint** {*;}
-keep class * extends org.aspectj.runtime.internal.AroundClosure{*;}
