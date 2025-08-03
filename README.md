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
```

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Giraffe or later
- JDK 17+
- Kotlin 1.9+
- Gradle 8.x

### Installation

1. Clone the repository:

```bash
git clone https://github.com/your-username/fitnesscheck.git
```

2. Open in Android Studio:

   ```
   File → Open → Select 'FitnessCheck copy' folder
   ```

3. Sync Gradle and run the app on an emulator or physical device.

---

## 🎨 Styling Notes (CSS-like in Jetpack Compose)

Although Android apps don’t use traditional CSS, styling is handled via Jetpack Compose like so:

```kotlin
Text(
    text = "Welcome",
    style = MaterialTheme.typography.titleLarge,
    modifier = Modifier
        .padding(16.dp)
        .background(Color.White)
        .clip(RoundedCornerShape(8.dp))
)
```

If you're documenting UI styles in the README, you can format them in a similar Kotlin code block. No need to replicate actual CSS.

---

## 👥 Contributors

- **Brenda Chelimo** – Developer & Designer  

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
