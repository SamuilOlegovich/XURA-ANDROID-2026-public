# =============================================================================
# XURA — ProGuard / R8 rules
# =============================================================================


# --- Собственные модели и перечисления ----------------------------------------

# DTO — сериализуются Jackson/Gson, поля нельзя переименовывать
-keep class com.samuilolegovich.dto.** { *; }

# StringEnum использует setValue через reflection-подобный паттерн
-keep enum com.samuilolegovich.enums.** { *; }

# ViewModels — создаются через ViewModelProvider с reflection
-keep class * extends androidx.lifecycle.ViewModel { <init>(...); }


# --- XRPL4J + Immutables ------------------------------------------------------

# Все классы библиотеки (API-интерфейсы + Immutable-реализации)
-keep class org.xrpl.** { *; }
-keepclassmembers class org.xrpl.** { *; }

# Immutables генерирует классы Immutable* в том же пакете
-keep class **Immutable* { *; }
-keepclassmembers class **Immutable* { *; }


# --- Jackson ------------------------------------------------------------------

-keep class com.fasterxml.jackson.** { *; }
-keepclassmembers class * {
    @com.fasterxml.jackson.annotation.JsonProperty *;
    @com.fasterxml.jackson.annotation.JsonCreator <init>(...);
}
-dontwarn com.fasterxml.jackson.**


# --- Retrofit2 ----------------------------------------------------------------

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-dontwarn retrofit2.**


# --- OkHttp3 + Okio -----------------------------------------------------------

-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase


# --- OpenFeign (xrpl4j-client) ------------------------------------------------

-keep class feign.** { *; }
-keepclassmembers class feign.** { *; }
-dontwarn feign.**
-dontwarn sun.misc.Unsafe


# --- Java-WebSocket -----------------------------------------------------------

-keep class org.java_websocket.** { *; }
-dontwarn org.java_websocket.**


# --- Gson ---------------------------------------------------------------------

-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn com.google.gson.**


# --- Hilt / Dagger ------------------------------------------------------------
# Hilt включает собственные consumer-правила через AAR,
# но дублируем ключевые на случай edge-cases

-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
    @javax.inject.Inject <fields>;
}
-dontwarn dagger.**


# --- AndroidX Security (EncryptedSharedPreferences / Tink) --------------------

-keep class androidx.security.crypto.** { *; }
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**


# --- CameraX + ML Kit (сканирование QR) --------------------------------------

-dontwarn androidx.camera.**
-dontwarn com.google.mlkit.**


# --- ZXing --------------------------------------------------------------------

-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**


# --- Calligraphy3 / ViewPump --------------------------------------------------

-keep class io.github.inflationx.** { *; }
-dontwarn io.github.inflationx.**


# --- MaterialEditText ---------------------------------------------------------

-keep class com.rengwuxian.materialedittext.** { *; }
-dontwarn com.rengwuxian.**


# --- Firebase (подключён через BOM, классы закомментированы) ------------------

-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**


# --- Компиляторные/annotation-processor классы, попавшие в classpath ----------
# Immutables (xrpl4j) и logback тянут javax.lang.model/javax.tools/javax.naming/
# org.eclipse.jdt/org.codehaus.janino как compile-time-only зависимости — они не
# вызываются в runtime на Android, R8 должен просто не предупреждать о них.

-dontwarn javax.lang.model.**
-dontwarn javax.tools.**
-dontwarn javax.naming.**
-dontwarn javax.servlet.**
-dontwarn javax.annotation.processing.**
-dontwarn org.eclipse.jdt.**
-dontwarn org.codehaus.janino.**
-dontwarn sun.security.x509.**
-dontwarn com.sun.tools.javac.**


# --- Атрибуты для читаемых crash-стектрейсов ----------------------------------
# Оставляем номера строк — имена файлов скрыты, но стек можно расшифровать
# через mapping.txt из артефактов сборки.

-keepattributes LineNumberTable
-renamesourcefileattribute SourceFile


# --- Удаление debug-вывода из release-сборки -----------------------------------
# В коде вместо android.util.Log используются System.out.println(...) и
# e.printStackTrace() (хеши транзакций, суммы, внутренние ошибки сети) — оба
# пишут в logcat, который доступен через adb или приложениям с READ_LOGS на
# старых Android. R8 вырезает эти вызовы только в release (debug их сохраняет).

-assumenosideeffects class java.io.PrintStream {
    public void println(...);
    public void print(...);
}
-assumenosideeffects class java.lang.Throwable {
    public void printStackTrace();
}
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}
