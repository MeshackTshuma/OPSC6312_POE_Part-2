Gold Plate - Prototype (MSSQL backend)

Backend (MSSQL):
1. Install SQL Server and create a database (or use an existing instance).
2. Edit environment variables or set OS env vars:
   - DB_USER (e.g. sa)
   - DB_PASSWORD
   - DB_SERVER (e.g. localhost or SERVER\\INSTANCE)
   - DB_NAME (e.g. GoldPlateDB)
   - JWT_SECRET (optional)

3. Start backend:
   cd backend
   npm install
   node index.js
   (seed.js runs automatically if tables are missing)

4. Default demo user:
   email: demo@goldplate.com
   password: password123

Android:
- Open android-client in Android Studio
- Sync Gradle and run on an emulator (Backend endpoint: http://10.0.2.2:3000)
