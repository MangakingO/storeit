Store-It �
Smart Inventory Management for Small Businesses and Student Projects
Store-It is an Android application that helps users track and manage inventory items from their phone. It supports real-time sync with Firebase, offline access, and CSV/Excel export for reporting and backups.
Built as a team project for a software engineering course.
 Features
Inventory CRUD
Add, edit, delete items
Track name, quantity, category, price, and optional notes
Real-time Sync (Firebase Firestore)
Inventory changes are synced across devices using Firestore
Live updates when items are created/edited/deleted
Offline Support + Background Sync
View and edit your inventory while offline
Changes are queued locally and synchronized when the device reconnects
CSV / Excel Export
Export inventory data to .csv (and/or .xlsx) for:
Accounting
Backup
Import into spreadsheets (Excel, Google Sheets)
Authentication 
User login/sign-up with Firebase Authentication
Each user sees only their own inventory
Notifications (optional)
Low-stock alerts and app notifications using Firebase Cloud Messaging (FCM) / Cloud Functions
 Tech Stack
Platform: Android (Kotlin/Java)
Backend:
Firebase Firestore (cloud NoSQL database)
Firebase Authentication (for user accounts, if enabled)
Firebase Cloud Functions (for server-side logic / notifications)
Firebase Cloud Messaging (push notifications)
Architecture: MVVM / repository pattern (depending on your codebase)
Other:
RecyclerView for inventory list
ViewModel + LiveData (or similar) for state management
Gradle for build
📐 High-Level Architecture
UI Layer
Activities/Fragments (e.g., MainActivity, AddItemActivity)
RecyclerView + Adapter for displaying the inventory list
Domain / Data Layer
InventoryRepository handling reads/writes to Firestore
Local caching layer (e.g., Room or in-memory structures) for offline usage
Firebase Integration
Firestore collections (e.g., /users/{userId}/items)
Cloud Functions (optional) for:
Low stock triggers
Notification sending via FCM
Export Module
Utility class that:
Reads all items from Firestore/local cache
Generates a CSV (and/or Excel) file
Shares/saves the file using Android’s Storage / Share Sheet
� Getting Started
1. Prerequisites
Android Studio (latest stable)
Android SDK 24+ 
A Firebase project
2. Clone the Repository
git clone https://github.com/your-username/store-it.git
cd store-it
3. Firebase Setup
Go to Firebase Console, create a new project (or use an existing one).
Add an Android app to the project with your app’s package name.
Download the generated google-services.json file.
Place google-services.json in:
app/src/google-services.json
Enable the following in Firebase:
Firestore Database
Authentication 
Cloud Functions + Cloud Messaging (if using notifications)
Update Firestore rules as needed (for a demo app, something like):
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
4. Build & Run
Open the project in Android Studio.
Let Gradle sync.
Select a device/emulator.
Click Run ▶.
📊 Using Store-It
Create an account / sign in (if Auth is enabled).
Add items with name, quantity, and other details.
Edit or delete items from the main list.
Go offline and try modifying your inventory – changes will sync when you reconnect.
Export your inventory:
Open the menu (⋮)
Tap “Export as CSV/Excel”
Choose a location/app to save or share the file.
🧪 Testing
Manual testing on emulator and physical device:
Create/edit/delete items
Network on/off scenarios (airplane mode)
Export file and open in Excel/Google Sheets
