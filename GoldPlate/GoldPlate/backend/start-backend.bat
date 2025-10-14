\
@echo off
IF NOT EXIST "node_modules" (
  echo Installing dependencies...
  npm install
)
echo Starting Gold Plate backend on port 3000...
node index.js
pause
