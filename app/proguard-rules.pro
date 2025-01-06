# Silence warnings about missing classes from these providers:
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.bouncycastle.jsse.provider.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn com.android.org.conscrypt.SSLParametersImpl
-dontwarn org.apache.harmony.xnet.provider.jsse.SSLParametersImpl
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider

# Keep model classes (if you're using reflection or runtime type checks on them)
-keep class uz.rttm.support.models.** { *; }

# Keep generic signatures to avoid runtime ClassCastExceptions with ParameterizedType
-keepattributes Signature
