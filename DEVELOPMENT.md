# Development Guide

## Environment Setup

### Required Tools

1. **Android Studio**: Hedgehog (2023.1.1) or later
   - Download from: https://developer.android.com/studio

2. **Java Development Kit (JDK)**: Version 17
   - Included with Android Studio or download separately

3. **Android SDK**:
   - Minimum SDK: API 29 (Android 10)
   - Target SDK: API 36
   - Compile SDK: API 36

4. **Git**: For version control
   - Download from: https://git-scm.com/

### Initial Setup

1. **Clone the Repository**
   ```bash
   git clone https://github.com/I-slam404/Movie_App.git
   cd Movie_App
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory
   - Wait for Gradle sync to complete

3. **Configure API Key**
   
   Create `local.properties` in the project root:
   ```properties
   ## This file must *NOT* be checked into Version Control Systems
   # Location of the Android SDK
   sdk.dir=/path/to/your/Android/sdk
   
   # TMDB API Key
   TMDB_API_KEY=your_api_key_here
   ```

4. **Sync Gradle**
   ```bash
   ./gradlew sync
   ```

## Project Structure

```
Movie_App/
├── app/                          # Application module
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/             # Kotlin source files
│   │   │   ├── res/              # Resources (layouts, drawables, etc.)
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                 # Unit tests
│   │   └── androidTest/          # Instrumented tests
│   ├── build.gradle.kts          # Module build configuration
│   └── proguard-rules.pro        # ProGuard rules
├── gradle/                       # Gradle wrapper
├── build.gradle.kts              # Project build configuration
├── settings.gradle.kts           # Project settings
├── gradle.properties             # Gradle properties
└── local.properties              # Local configuration (not in VCS)
```

## Building the Project

### Using Android Studio

1. **Build Menu**
   - `Build > Make Project` (Ctrl+F9 / Cmd+F9)
   - `Build > Rebuild Project`
   - `Build > Clean Project`

2. **Run Configurations**
   - Select `app` configuration
   - Choose emulator or connected device
   - Click Run (Shift+F10 / Ctrl+R)

### Using Command Line

**Build Debug APK:**
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

**Build Release APK:**
```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

**Install on Device:**
```bash
./gradlew installDebug
```

**Build and Install:**
```bash
./gradlew installDebug && adb shell am start -n com.islam404.movieapp/.MainActivity
```

## Running the Application

### On Emulator

1. **Create AVD (Android Virtual Device)**
   - Open AVD Manager in Android Studio
   - Create new device (recommended: Pixel 5, API 29+)
   - Start the emulator

2. **Run the App**
   ```bash
   ./gradlew installDebug
   ```

### On Physical Device

1. **Enable Developer Options**
   - Go to Settings > About Phone
   - Tap "Build Number" 7 times

2. **Enable USB Debugging**
   - Go to Settings > Developer Options
   - Enable "USB Debugging"

3. **Connect Device**
   ```bash
   adb devices  # Verify device is connected
   ./gradlew installDebug
   ```

## Testing

### Running Tests

**All Unit Tests:**
```bash
./gradlew test
```

**Specific Test Class:**
```bash
./gradlew test --tests "com.islam404.movieapp.data.repository.MovieRepositoryImplTest"
```

**All Instrumented Tests:**
```bash
./gradlew connectedAndroidTest
```

**Generate Test Coverage Report:**
```bash
./gradlew testDebugUnitTest jacocoTestReport
# Report: app/build/reports/jacoco/test/html/index.html
```

### Writing Tests

**Unit Test Example:**
```kotlin
@Test
fun `test use case returns success`() = runTest {
    // Given
    val expectedData = mockData
    coEvery { repository.getData() } returns Result.success(expectedData)
    
    // When
    val result = useCase()
    
    // Then
    assertTrue(result.isSuccess)
    assertEquals(expectedData, result.getOrNull())
}
```

**Test Location:**
- Unit tests: `app/src/test/java/`
- Instrumented tests: `app/src/androidTest/java/`

## Code Style

### Kotlin Style Guide

The project follows [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).

**Key Points:**
- 4 spaces for indentation
- Max line length: 120 characters
- Use trailing commas for multi-line statements
- Prefer expression functions for simple functions
- Use type inference when possible

**Example:**
```kotlin
// Good
fun calculateTotal(items: List<Item>): Double =
    items.sumOf { it.price }

// Avoid
fun calculateTotal(items: List<Item>): Double {
    return items.sumOf { it.price }
}
```

### Compose Guidelines

**State Management:**
```kotlin
// Good - State hoisting
@Composable
fun MyScreen(
    state: MyState,
    onAction: (MyAction) -> Unit
) {
    // UI implementation
}

// Avoid - State in composable
@Composable
fun MyScreen() {
    var state by remember { mutableStateOf(MyState()) }
    // ...
}
```

**Naming Conventions:**
- ViewModels: `FeatureViewModel`
- Screens: `FeatureScreen`
- Components: Descriptive names like `MovieCard`, `SearchBar`

## Gradle Configuration

### Dependencies

Dependencies are managed in `libs.versions.toml`:

```toml
[versions]
kotlin = "1.9.0"
compose = "2024.04.00"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "core-ktx" }
# ... more dependencies
```

### Adding a New Dependency

1. Add to `gradle/libs.versions.toml`
2. Reference in `app/build.gradle.kts`:
   ```kotlin
   dependencies {
       implementation(libs.new.library)
   }
   ```

### Build Variants

**Debug:**
- Includes logging
- No code obfuscation
- Debuggable

**Release:**
- ProGuard/R8 enabled
- Code minification
- Resource shrinking
- Optimized

## Debugging

### Logging

Use `Timber` or Android's `Log`:
```kotlin
import android.util.Log

Log.d("TAG", "Debug message")
Log.e("TAG", "Error message")
```

### Debugging Tools

**Layout Inspector:**
- Tools > Layout Inspector
- View Compose hierarchy

**Network Inspector:**
- View > Tool Windows > App Inspection > Network Inspector
- Monitor API calls

**Database Inspector:**
- View > Tool Windows > App Inspection > Database Inspector
- Inspect Room database

### Common Issues

**Issue: Gradle sync fails**
```bash
# Clean and rebuild
./gradlew clean
./gradlew build --refresh-dependencies
```

**Issue: Build cache issues**
```bash
# Clear Gradle cache
rm -rf ~/.gradle/caches/
```

**Issue: Emulator not starting**
- Check available disk space
- Increase RAM allocation in AVD settings
- Disable antivirus temporarily

## Performance Optimization

### Build Performance

**Enable Gradle Daemon:**
```properties
# gradle.properties
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.configureondemand=true
```

**Increase Heap Size:**
```properties
# gradle.properties
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=512m
```

### Runtime Performance

**Use R8 Optimization:**
```kotlin
// build.gradle.kts
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

## Continuous Integration

### GitHub Actions (Example)

Create `.github/workflows/android.yml`:
```yaml
name: Android CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK 17
      uses: actions/setup-java@v2
      with:
        java-version: '17'
        distribution: 'adopt'
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Build with Gradle
      run: ./gradlew build
    
    - name: Run tests
      run: ./gradlew test
```

## Code Review Checklist

Before submitting a PR:

- [ ] Code follows Kotlin style guide
- [ ] All tests pass
- [ ] New features have tests
- [ ] Documentation updated
- [ ] No hardcoded strings (use string resources)
- [ ] No API keys in code
- [ ] ProGuard rules updated if needed
- [ ] Screenshot for UI changes

## Useful Commands

**Check dependency updates:**
```bash
./gradlew dependencyUpdates
```

**List all tasks:**
```bash
./gradlew tasks
```

**Clean build:**
```bash
./gradlew clean build
```

**Generate APK:**
```bash
./gradlew assembleRelease
```

**Analyze APK size:**
```bash
./gradlew :app:analyzeDebugBundle
```

## Resources

### Official Documentation
- [Android Developers](https://developer.android.com/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)

### Libraries Documentation
- [Hilt](https://dagger.dev/hilt/)
- [Retrofit](https://square.github.io/retrofit/)
- [Room](https://developer.android.com/training/data-storage/room)
- [Coil](https://coil-kt.github.io/coil/)
- [Compose Destinations](https://composedestinations.rafaelcosta.xyz/)

### Community
- [Android on Reddit](https://www.reddit.com/r/androiddev/)
- [Stack Overflow](https://stackoverflow.com/questions/tagged/android)
- [Kotlin Slack](https://kotlinlang.slack.com/)

## Troubleshooting

### Build Issues

**Problem**: "Could not find method compile()"  
**Solution**: Use `implementation()` instead of `compile()`

**Problem**: "Manifest merger failed"  
**Solution**: Add tools namespace and use tools:replace

**Problem**: "Duplicate class found"  
**Solution**: Exclude duplicate dependencies:
```kotlin
implementation("library") {
    exclude(group = "group", module = "module")
}
```

### Runtime Issues

**Problem**: App crashes on startup  
**Solution**: Check logcat for stack trace, verify ProGuard rules

**Problem**: Images not loading  
**Solution**: Check internet permission, verify API key

**Problem**: Database migration errors  
**Solution**: Implement proper Room migration or fallback to destructive migration

## Getting Help

If you encounter issues:

1. Check existing documentation
2. Search [Stack Overflow](https://stackoverflow.com/)
3. Create an issue on GitHub with:
   - Android Studio version
   - Gradle version
   - Device/emulator details
   - Steps to reproduce
   - Relevant logs/stack traces
