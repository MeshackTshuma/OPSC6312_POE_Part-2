#!/usr/bin/env bash
# start-backend.sh - installs deps if missing and starts the backend (MSSQL version)
if [ ! -d "node_modules" ]; then
  echo "Installing dependencies..."
  npm install
fi
echo "Starting Gold Plate backend on port 3000..."
node index.js
