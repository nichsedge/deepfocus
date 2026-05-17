# 🌌 DeepFocus

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white&style=for-the-badge)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?logo=kotlin&logoColor=white&style=for-the-badge)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?logo=jetpackcompose&logoColor=white&style=for-the-badge)](https://developer.android.com/jetpack/compose)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](LICENSE)

**DeepFocus** is a high-fidelity, premium Pomodoro and Stopwatch focus timer app designed specifically for Android. With a gorgeous, modern glassmorphic interface and deep ambient audio integration, DeepFocus elevates your productivity session by keeping you focused, calm, and distraction-free.

---

## ✨ Features

- **⚡ Dual Session Modes**: Seamlessly switch between a classic **Pomodoro Timer** and a precise **Stopwatch** depending on your workflow.
- **⏱️ Adjustable Intervals**: Instantly select custom durations (15m, 25m, 45m, 60m) with high-fidelity sweeping circular indicators.
- **🏷️ Dynamic Session Tagging**: Categorize your focus sessions with built-in tags (`Work`, `Study`, `Exercise`, `Other`) or easily add your own.
- **🎵 Immersive Soundscapes**: Enhance focus with integrated ambient soundscapes (white noise, rain, and customized focus audio) that run smoothly in the background.
- **📊 Real-time Analytics & Stats**: Gain direct insights into your productivity habits with a rich statistics dashboard displaying session history and tag breakdowns.
- **🌙 Breath-taking Glassmorphic UI**: Experience a sleek, dark-themed, glassmorphic layout adorned with glowing breathing animations and smooth gradient sweeps.

---

## 🛠️ Technology Stack

DeepFocus is built using modern Android development practices:

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) & [Material 3](https://m3.material.io/) for fluid, declarative animations and components.
- **State & ViewModel**: Kotlin Coroutines & Flow coupled with `ViewModel` to ensure robust, reactive architectural state.
- **Background Operations & Playback**:
  - **Media3 ExoPlayer**: High-performance audio streaming/looping for ambient soundtracks.
  - **Foreground Service**: Ensures focus state and audio playback persist when the screen is off or in the background.
  - **WorkManager**: Background task handling and deferred session reporting.
- **Local Persistence**: [Room Database](https://developer.android.com/training/data-storage/room) for tracking user session logs, tags, and sound selector history.
- **Target SDK**: Built and optimized for **Android API 36 (Android 15 / 16 Preview)**.

---

## 🏛️ Directory Architecture

```
deepfocus/
├── app/
│   ├── src/main/
│   │   ├── java/com/sans/deepfocus/
│   │   │   ├── analytics/       # Simple screen & custom session analytics
│   │   │   ├── data/            # Room DB local entities, DAOs, & AppDatabase
│   │   │   ├── domain/          # Core TimerManager & FocusAudioManager business logic
│   │   │   ├── service/         # Foreground FocusService for background operations
│   │   │   ├── ui/              # Composable Screens (TimerScreen, StatsScreen) & Theme
│   │   │   └── MainActivity.kt  # Root Single-Activity Entrypoint
│   │   └── AndroidManifest.xml  # App configuration, services, and permissions
│   └── build.gradle.kts         # App-level dependencies and SDK configuration
└── build.gradle.kts             # Top-level Gradle configuration
```

---

## 🚀 Setup & Installation

### Prerequisites
- [Android Studio Koala / Ladybug (or newer)](https://developer.android.com/studio)
- [JDK 17 or higher](https://www.oracle.com/java/technologies/downloads/)
- An Android device or Emulator running Android API 36 or newer (compatible down to minSdk 36)

### Building the Project
1. **Clone the repository**:
   ```bash
   git clone https://github.com/nichsedge/deepfocus.git
   cd deepfocus
   ```

2. **Open in Android Studio**:
   - Select **File > Open** and choose the root `deepfocus` folder.
   - Let Gradle sync completely.

3. **Run on Device**:
   - Connect your Android device or start an emulator.
   - Press **Run** (`Shift + F10` / Play button) in Android Studio.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
