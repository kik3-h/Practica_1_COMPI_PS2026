# Keep CUP runtime
-keep class java_cup.runtime.** { *; }
-dontwarn java_cup.runtime.**

# Keep generated lexer/parser
-keep class com.compiladores.practica1.generated.** { *; }
