# Build Issues with Java 26

## Problem
The Kotlin compiler (v1.9.x and 2.0.x) has compatibility issues with Java 26.
When building with `./gradlew build`, you may encounter:
```
java.lang.IllegalArgumentException: 26
  at org.jetbrains.kotlin.com.intellij.util.lang.JavaVersion.parse(JavaVersion.java:307)
```

## Solution
Use Java 11, 17, or 21 for building. Java 26 support may be added in future Kotlin releases.

### Option 1: Set JAVA_HOME to a compatible JDK
```bash
# Windows
set JAVA_HOME=C:\Program Files\Java\jdk-21

# macOS/Linux  
export JAVA_HOME=/usr/libexec/java_home -v 21
```

### Option 2: Downgrade to Java 21
Install JDK 21 from https://adoptium.net/ or Oracle's website.

## Files Modified
- `gradle/wrapper/gradle-wrapper.properties` - Updated to Gradle 8.4
- `gradle/libs.versions.toml` - Updated AGP to 8.5.0, Kotlin to 1.9.23
- `gradle.properties` - Added JVM options for compatibility

These changes are backwards compatible and won't affect your development once you have a compatible Java version.
