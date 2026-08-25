-dontwarn org.**
-keep class org.** { *; }
-keep class com.meituy.app.** { *; }
-keepclassmembers class com.meituy.app.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}