# Keep kotlinx.serialization serializers
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.ada.mudra.**$$serializer { *; }
-keepclassmembers class com.ada.mudra.** {
    *** Companion;
}
-keepclasseswithmembers class com.ada.mudra.** {
    kotlinx.serialization.KSerializer serializer(...);
}
