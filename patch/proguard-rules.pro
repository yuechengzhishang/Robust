-keep class com.meituan.robust.**{*;}
-keep class meituan.robust.**{*;}
-keep class com.google.gson.**{*;}
-keepattributes *Annotation*
-optimizations !method/removal/parameter
#-optimizations !method/marking/final
-keepattributes EnclosingMethod
#-dontwarn InnerClasses
#-keepclassmembers class * {
#   *** ajc$*(***);
#}
#-keepclassmembers class * {
#   *** *_*(***);
#}
#-keepclassmembers class * {
#   private static final org.aspectj* **;
#}
