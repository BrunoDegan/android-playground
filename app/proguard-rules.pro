# Project ProGuard/R8 rules. Base optimizations come from
# proguard-android-optimize.txt (see build.logic.gradle.kts). Do not add
# -dontshrink / -dontobfuscate / -dontoptimize here: that disables the
# protection these rules exist for.

# --- Crash-report vs. obfuscation balance ---
# Keep line numbers so crash stacks stay mappable via mapping.txt, but strip
# the real source file name so a decompiled release APK doesn't leak internal
# file paths.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Required for Gson (Room type converter) generic types, kotlinx.serialization
# annotations and Parcelize.
-keepattributes *Annotation*,Signature,Exceptions,InnerClasses,EnclosingMethod

# --- Prevent sensitive data from reaching Logcat on a release build ---
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
    public static int println(int, java.lang.String, java.lang.String);
}
-assumenosideeffects class kotlin.io.ConsoleKt {
    public static void println(...);
    public static void print(...);
}

# --- Obfuscation hardening ---
# Flatten every obfuscated class into a single synthetic package so the
# original package/module structure isn't visible from a decompiled APK.
-repackageclasses ''
-allowaccessmodification

# No manual keep rules for AndroidX/Kotlin/Kotlinx, Ktor, Gson, Room or
# Coroutines: each ships its own consumer ProGuard rules, and broad manual
# keeps here would only re-expose classes/members R8 would otherwise remove
# or rename (see skills/r8-analyzer/references/REDUNDANT-RULES.md).
