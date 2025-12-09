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
![Image](https://github.com/user-attachments/assets/38e80fb4-1c90-4042-958a-6ef8e9cacb33)

## 📱 App Screenshots

<table align="center">
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/22acbfdf-c449-4409-b4bf-de8da5e75351" width="200" />
      <br />
      <b>Login & Splash</b>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/949b01a2-e486-4d7c-847d-589da9598312" width="200" />
      <br />
      <b>Teacher Dashboard</b>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/c34ca245-c51a-4b92-b954-d7fee30f23f7" width="200" />
      <br />
      <b>Smart Attendance (AI)</b>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/21607339-6795-4a82-98c1-b7c973d4507e" width="200" />
      <br />
      <b>Student Results</b>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/bbdc1638-e13a-426b-8860-62a6b8602a25" width="200" />
      <br />
      <b>Student Results</b>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/0f2bde6f-33b1-4bbb-bb78-b4b11f100c74" width="200" />
      <br />
      <b>Login & Splash</b>
    </td><td align="center">
      <img src="https://github.com/user-attachments/assets/6507bb62-3718-4e18-b6ce-510ef8dfb45a" width="200" />
      <br />
      <b>Login & Splash</b>
    </td><td align="center">
      <img src="https://github.com/user-attachments/assets/9aca662f-681d-47c6-a321-26d196d4f985" width="200" />
      <br />
      <b>Login & Splash</b>
    </td><td align="center">
      <img src="https://github.com/user-attachments/assets/6548282d-4b31-4948-9ca0-b18c1a32c242" width="200" />
      <br />
      <b>Login & Splash</b>
    </td>
  </tr>
</table>
