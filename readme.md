# Attendify — Face Recognition Attendance System

An Android attendance app with **Admin** and **Staff** roles. Staff mark attendance using a selfie, which is verified against their enrolled face before an attendance record is created.

Built as a **hiring assignment** to demonstrate Android development, CameraX, ML Kit, TensorFlow Lite, Room, Compose, and location integration.

## Downloads

- **APK:** [Download Attendify APK](https://drive.google.com/file/d/1zk3cStB0cezc4wY4ULIf62xOg8yHQijJ/view?usp=sharing)
- **AI Conversation Export:** [View AI Conversation JSON](https://drive.google.com/file/d/1vy2N-ASeMtV3KE4F-m8ZqwG7gFKwZ-dt/view?usp=sharing)

## Features

* Admin & Staff role-based login
* Add and manage staff
* Face enrollment and re-enrollment
* Selfie-based face verification
* On-device face detection with ML Kit
* Face recognition using FaceNet + TensorFlow Lite
* Attendance history with selfie thumbnails
* Timestamp, GPS location and match confidence
* Local Room database
* Graceful handling of permissions and invalid face input

## Tech Stack

* **Kotlin**
* **Jetpack Compose + Material 3**
* **MVVM + StateFlow**
* **Room**
* **CameraX**
* **Google ML Kit Face Detection**
* **TensorFlow Lite / FaceNet**
* **Fused Location Provider**
* **Kotlin Coroutines + Flow**

## Architecture

```text
Compose UI
    ↓
ViewModel + StateFlow
    ↓
Repository
    ↓
Room / SQLite
```

Face verification is handled locally using:

```text
Camera → ML Kit Face Detection
       → Face Alignment & Preprocessing
       → FaceNet TFLite Embedding
       → Cosine Similarity
       → Attendance
```

A similarity score of **0.6 or higher** is treated as a match.

## Demo Credentials

| Role  | Email            | Password   |
| ----- | ---------------- | ---------- |
| Admin | `admin@demo.com` | `admin123` |
| Staff | `staff@demo.com` | `staff123` |

## How to Run

### Requirements

* Android Studio compatible with AGP 9.2.1
* JDK 21
* Android SDK API 37
* Physical Android device recommended
* Minimum Android version: **Android 10 (API 29)**

### Steps

1. Clone and open the project in Android Studio.
2. Let Gradle sync.
3. Ensure `app/src/main/assets/facenet.tflite` is present.
4. Run the app on a physical device.
5. Login using the demo credentials above.

### Generate APK

`Build → Generate Signed Bundle / APK → APK`

## Limitations

This is a local demo and intentionally has simplified security:

* No backend or cloud sync
* Demo credentials are hardcoded
* Face data and selfies are stored locally
* No liveness detection
* Face matching threshold is not production-calibrated
* No persistent authentication/session management
* Release build is not minified

For production, this would require backend authentication, encrypted storage, liveness detection, secure biometric handling, synchronization, audit logging, and proper threshold validation.



## License

The bundled `facenet.tflite` model is sourced from **FaceRecognition_With_FaceNet_Android** and is licensed under Apache License 2.0.

Application code is submitted as part of a hiring assignment.
