# Firebase Rules
-keep class com.fabio.eagleeyes.usuario.Usuario { *; }
-keep class com.fabio.eagleeyes.usuario.UsuarioPai { *; }
-keep class com.fabio.eagleeyes.usuario.UsuarioFilho { *; }
-keep class com.fabio.eagleeyes.chat.ChatMessage { *; }
-keepclassmembers class com.google.firebase.** { *; }
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# OkHttp Rules
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# Coroutines Rules
-keep class kotlinx.coroutines.** { *; }
-keep interface kotlinx.coroutines.** { *; }
-keepclassmembers class kotlinx.coroutines.** {
    public static <methods>;
}
-dontwarn kotlinx.coroutines.**

# jBCrypt Rules
-keep class org.mindrot.jbcrypt.** { *; }
-dontwarn org.mindrot.jbcrypt.**

# Generative AI (Gemini) Rules
-keep class com.google.ai.client.generativeai.** { *; }
-keep interface com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**

# ViewBinding Rules
-keep class com.fabio.eagleeyes.databinding.** { *; }
-keep class * implements android.os.Parcelable { public static final android.os.Parcelable$Creator *; }

# Lifecycle Rules
-keep class androidx.lifecycle.** { *; }
-keep interface androidx.lifecycle.** { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * implements androidx.lifecycle.LifecycleObserver { *; }
-dontwarn androidx.lifecycle.**

# Material Components Rules
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# Activity e Fragment Rules
-keep public class * extends android.app.Activity
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends androidx.appcompat.app.AppCompatActivity

# Serialização Rules
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
}
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
