# Add project specific ProGuard rules here.

# Keep ONNX Runtime classes
-keep class ai.onnxruntime.** { *; }
-keep interface ai.onnxruntime.** { *; }

# Keep model classes
-keep class com.aiwriter.free.LocalAIEngine { *; }
-keep enum com.aiwriter.free.TextTask { *; }

# Keep Android components
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
