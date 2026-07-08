\# LeanMassCalculator — Android Security Hardening Project



LeanMassCalculator is an Android application developed in Kotlin that calculates and tracks a user's Lean Body Mass (LBM) using the Boer formula.



The project was later extended with a mobile security hardening phase based on OWASP MASVS principles. The goal was to identify common Android security weaknesses and implement practical defensive measures around storage, network configuration, authentication, input validation, and client-side resilience.



\## Features



\- User registration and login with Firebase Authentication

\- Lean Body Mass calculation using the Boer formula

\- Visual feedback based on the calculated result

\- Local persistence with SQLite

\- Cloud persistence with Firebase Firestore

\- Calculation history with deletion support

\- User-specific data isolation using Firebase UID



\## Tech Stack



\- Kotlin

\- Android SDK

\- Material Design 3

\- ViewBinding

\- Firebase Authentication

\- Firebase Firestore

\- SQLite via SQLiteOpenHelper

\- Gradle



\## Security Hardening



The application was reviewed against several OWASP MASVS-inspired security areas.



Implemented security measures include:



\- Disabled Android backup to reduce local database extraction risk

\- HTTPS-only network configuration

\- Restricted trust anchors to system CAs

\- Session timeout after prolonged inactivity

\- Stronger validation of physiological input ranges

\- Generic authentication error messages to reduce account enumeration risk

\- Root detection warning for risky device environments



\## Project Structure



```text

.

├── app/

│   ├── src/main/java/com/example/leanmasscalculator/

│   │   ├── data/

│   │   ├── model/

│   │   └── ui/

│   ├── src/main/res/

│   ├── build.gradle

│   └── google-services.example.json

├── docs/

│   ├── security-writeup.md

│   └── portfolio-summary.md

├── build.gradle

├── gradle.properties

├── settings.gradle

└── README.md

