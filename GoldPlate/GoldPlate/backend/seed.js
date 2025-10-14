/*
seed.js - idempotent seeder for MSSQL
Run automatically on server start via require('./seed').runIfNeeded();
You can also run manually: node seed.js
*/
const db = require('./db');
const bcrypt = require('bcrypt');

async function tableExists(pool, name) {
  const res = await pool.request().query("SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '" + name + "'") ;
  return res.recordset.length > 0;
}

async function createTables(pool) {
  await pool.request().query(`
    CREATE TABLE users (
      id INT IDENTITY(1,1) PRIMARY KEY,
      email NVARCHAR(255) UNIQUE NOT NULL,
      password_hash NVARCHAR(255) NOT NULL,
      display_name NVARCHAR(255),
      settings_json NVARCHAR(MAX)
    );
  `);
  await pool.request().query(`
    CREATE TABLE portfolios (
      id INT IDENTITY(1,1) PRIMARY KEY,
      user_id INT NOT NULL,
      symbol NVARCHAR(32) NOT NULL,
      shares FLOAT NOT NULL,
      avg_price FLOAT,
      created_at DATETIME DEFAULT GETDATE(),
      CONSTRAINT FK_User FOREIGN KEY (user_id) REFERENCES users(id)
    );
  `);
}

async function seedData(pool) {
  const pwd = 'password123';
  const hash = await bcrypt.hash(pwd, 10);
  // insert demo user if not exists
  await pool.request().query("IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'demo@goldplate.com') BEGIN INSERT INTO users (email, password_hash, display_name, settings_json) VALUES ('demo@goldplate.com', '" + hash + "', 'Demo User', '{\"theme\":\"light\"}') END");
  await pool.request().query("DECLARE @uid INT; SELECT @uid = id FROM users WHERE email = 'demo@goldplate.com'; IF NOT EXISTS (SELECT 1 FROM portfolios WHERE user_id=@uid AND symbol='AAPL') BEGIN INSERT INTO portfolios (user_id, symbol, shares, avg_price) VALUES (@uid, 'AAPL', 10, 150) END");
  await pool.request().query("DECLARE @uid2 INT; SELECT @uid2 = id FROM users WHERE email = 'demo@goldplate.com'; IF NOT EXISTS (SELECT 1 FROM portfolios WHERE user_id=@uid2 AND symbol='TSLA') BEGIN INSERT INTO portfolios (user_id, symbol, shares, avg_price) VALUES (@uid2, 'TSLA', 5, 220) END");
}

module.exports = {
  runIfNeeded: async function() {
    try {
      const pool = await db.getPool();
      const usersExists = await tableExists(pool, 'users');
      if (!usersExists) {
        console.log('Creating tables...');
        await createTables(pool);
        console.log('Tables created.');
      } else {
        console.log('Tables already exist.');
      }
      await seedData(pool);
      console.log('Seed complete.');
    } catch (err) {
      console.error('Seeder failed', err);
      throw err;
    }
  }
};
