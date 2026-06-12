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


# --- Атрибуты для читаемых crash-стектрейсов ----------------------------------
# Оставляем номера строк — имена файлов скрыты, но стек можно расшифровать
# через mapping.txt из артефактов сборки.

-keepattributes LineNumberTable
-renamesourcefileattribute SourceFile
