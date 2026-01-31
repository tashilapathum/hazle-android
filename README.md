# Hazle - Android App

https://github.com/user-attachments/assets/28a4a0a0-ffc9-4aa6-bb5b-0e826340217d

**Hazle** is yet another AI chatbot assistant designed with Android system integration in mind. Instead of switching apps, users can highlight text anywhere in the Android system, process it through the context menu, and receive an AI-generated response via a background notification.

> **Note:** This is the Android client. The Ktor-based backend logic can be found at [hazle-backend](https://github.com/tashilapathum/hazle-backend).

---

## 🚀 Key Features

* **System-Wide Integration:** Share text directly from the Android text selection context menu.
* **Background Processing:** Utilizes asynchronous services to handle AI requests without interrupting the user's flow.
* **Notification UI:** View AI responses directly in the notification shade for quick reference.
* **Secure Auth:** JWT-based authentication to sync data and preferences.
* **Offline Support:** Local database caching for conversation history.

---

## 🛠 Tech Stack

### Core Android & UI

* **Jetpack Compose:** 100% Declarative UI with Material 3 and animations.
* **Modern Architecture:** MVVM pattern with a focus on Clean Architecture principles.
* **System Integration:** Implementation of `ProcessText` intent filters and system notifications.
* **Lifecycle Management:** Advanced usage of `androidx.lifecycle` (Process, ViewModel, Navigation).

### Networking & Data

* **Ktor Client:** Multi-platform asynchronous networking with JSON serialization, logging, and Auth0 JWT decoding.
* **Dependency Injection:** **Koin** for lightweight, thread-safe dependency management.
* **Local Persistence:** **Room Database** (with KSP) for structured data storage and **DataStore** for user preferences.
* **Security:** Implementation of `androidx.security.crypto` for encrypted shared preferences.

### Kotlin Features

* **Asynchronous Flow:** Kotlin Coroutines & Flow.
* **Image Loading:** Coil (Compose-first).
* **Date/Time:** Kotlinx Datetime.
* **Ktor:** Backend.

---

## ⚙️ Setup & Installation

1. **Clone the repo:**
```bash
git clone https://github.com/tashilapathum/hazle-android.git

```


2. **Backend Configuration:**
Ensure the [Hazle Backend](https://github.com/tashilapathum/hazle-backend) is running. Update the `BASE_URL` in the app's networking configuration.
3. **Secrets:**
The only required secret is the Sentry token located in `sentry.propertries` file. Make sure to update the gradle to match, or remove Sentry block from it.
4. **Build:**
Open in **Android Studio** (Ladybug or newer) and sync Gradle.
