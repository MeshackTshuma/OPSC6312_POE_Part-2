const sql = require('mssql');

const config = {
  user: process.env.DB_USER || 'YOUR_USERNAME',
  password: process.env.DB_PASSWORD || 'YOUR_PASSWORD',
  server: process.env.DB_SERVER || 'localhost',
  database: process.env.DB_NAME || 'GoldPlateDB',
  options: {
    encrypt: true, // for Azure / secure connections; change as needed
    trustServerCertificate: true // change to false in production with proper certs
  },
  pool: {
    max: 10,
    min: 0,
    idleTimeoutMillis: 30000
  }
};

let poolPromise = null;

module.exports = {
  getPool: async function() {
    if (!poolPromise) {
      poolPromise = sql.connect(config);
      // handle connection errors
      poolPromise.catch(err => { console.error('MSSQL connection error', err); poolPromise = null; });
    }
    return poolPromise;
  },
  sql: sql
};
