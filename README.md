# 🎓 EduTrack - Smart Campus Management System

**EduTrack** is a comprehensive Android application designed to streamline college operations. Built entirely with **Kotlin** and **Jetpack Compose**, it connects Administrators, Teachers, and Students in a single digital ecosystem.

The standout feature is **Smart Attendance**, which uses Google ML Kit to automate student headcounts via Face Recognition.

## 🚀 Key Features

### 🔐 Role-Based Access Control
* **Admin:** Secure user management (Create Teacher/Student accounts).
* **Teacher:** Manage academic data, post notices, upload assignments (PDF), and enter results.
* **Student:** View attendance stats, download assignments, submit work, and check results.

### 🤖 AI-Powered Attendance
* Integrated **Google ML Kit (Face Detection)**.
* Teachers can scan the classroom using the camera to automatically count and mark present students.

### 📂 Academic Workflow
* **Assignments:** Teachers upload problem statements; Students upload PDF solutions.
* **Notices:** Real-time digital notice board.
* **Results & Timetable:** Organized academic records and schedule tracking.

## 🛠️ Tech Stack

* **Language:** Kotlin (100%)
* **UI Toolkit:** Jetpack Compose (Material 3 Design)
* **Architecture:** MVVM + Clean Architecture
* **Database:** Room Database (Offline-first source of truth)
* **Dependency Injection:** Dagger Hilt
* **Asynchronous:** Coroutines & Flow
* **AI/ML:** CameraX + Google ML Kit

## 📱 Screenshots
