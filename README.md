# 🏋️‍♀️ FitnessCheck

**FitnessCheck** is a Kotlin-based Android application designed to help users track their fitness progress, set workout goals, and stay motivated with structured routines and performance metrics.

---

## 📱 Features

- Personalized fitness challenges
- Daily and weekly workout logging
- Health metrics tracking (e.g., weight, steps)
- Dark and light theme support
- Jetpack Compose-based UI
- Navigation component architecture

---

## 🛠️ Tech Stack

| Layer         | Technologies                                      |
|---------------|---------------------------------------------------|
| Language      | Kotlin                                             |
| UI            | Jetpack Compose, Material 3                        |
| Architecture  | MVVM (Model-View-ViewModel)                        |
| Backend       | Local storage (Room DB or SharedPreferences)      |
| Build Tool    | Gradle (KTS-based)                                 |
| Testing       | JUnit, AndroidX Test                               |
| Others        | Git, GitHub, Android Studio                       |

---

## 📂 Project Structure

```bash
FitnessCheck copy/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/brenda/fitnesscheck/
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
