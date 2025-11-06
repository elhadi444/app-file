# Android File Exchange App

This project is an **Android application** for **secure file exchange** between devices through a lightweight **Node.js server**.  
It allows users to upload, download, list, and delete files, with **AES encryption** ensuring data confidentiality.

---

## Features

### 🖥️ Server (Node.js + Express)
- **Upload files** (`POST /upload`) — files are received and stored locally.
- **Download files** (`GET /download/:filename`) — encrypted files can be retrieved.
- **Delete files** (`DELETE /delete/:filename`) — remove specific files from the server.
- **List files** (`GET /files`) — returns all available files.
- **Duplicate handling** — automatically renames files (e.g. `file(1).txt`).
- **CORS enabled** — allows cross-origin requests from the Android app.

### Android App (Java)
- **File upload** — select a file, encrypt it with AES, then upload it via HTTP.
- **File download** — retrieve and decrypt files locally.
- **File deletion** — delete files directly from the server.
- **File listing** — view all available files stored on the server.

---

## Security

- Uses **AES encryption** for all file transfers.  
- Files are **encrypted client-side** before being sent.  
- Files are **decrypted locally** after download.  
- The AES key is hard-coded for now but can be replaced with a secure key exchange mechanism.

---

## Technologies Used

### Server
- **Node.js** and **Express.js**
- **Multer** — file upload handling
- **CORS** — cross-origin access
- **fs**, **path** — file system utilities
- Hosted on **Render** with **GitHub CI/CD**

### Android App
- **Java**
- **Retrofit** — REST API communication
- **Gson Converter** — JSON to Java objects
- **OkHttp + Logging Interceptor** — HTTP management
- **RecyclerView** — file list UI
- **Bouncy Castle** — AES encryption/decryption

---

