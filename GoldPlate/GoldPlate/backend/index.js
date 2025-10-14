const express = require('express');
const bodyParser = require('body-parser');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const cors = require('cors');
const db = require('./db');
const seed = require('./seed');

const SECRET = process.env.JWT_SECRET || 'replace_this_with_a_strong_secret_in_prod';
const app = express();
app.use(cors());
app.use(bodyParser.json());

// Run seeder on startup (idempotent)
(async () => {
  try {
    await seed.runIfNeeded();
    console.log('Seed check complete');
  } catch (e) {
    console.error('Seed error', e);
  }
})();

// helper wrappers using mssql
async function query(sql, params=[]) {
  const pool = await db.getPool();
  const ps = pool.request();
  params.forEach((p, i) => ps.input('p' + i, p));
  const result = await ps.query(sql.replace(/\?/g, (m, i) => '@p' + i));
  return result.recordset;
}

async function exec(sql, params=[]) {
  const pool = await db.getPool();
  const ps = pool.request();
  params.forEach((p, i) => ps.input('p' + i, p));
  await ps.query(sql.replace(/\?/g, (m, i) => '@p' + i));
  return true;
}

// Register
app.post('/auth/register', async (req, res) => {
  try {
    const { email, password, displayName } = req.body;
    if (!email || !password) return res.status(400).json({ error: 'Email and password required' });
    if (password.length < 6) return res.status(400).json({ error: 'Password too short' });
    const salt = await bcrypt.genSalt(10);
    const hash = await bcrypt.hash(password, salt);
    const sql = 'INSERT INTO users (email, password_hash, display_name, settings_json) VALUES (?, ?, ?, ?)';
    await exec(sql, [email, hash, displayName || '', JSON.stringify({theme:'light'})]);
    const user = (await query('SELECT id, email, display_name FROM users WHERE email = ?', [email]))[0];
    const token = jwt.sign({ id: user.id, email: user.email }, SECRET, { expiresIn: '7d' });
    res.json({ token, user });
  } catch (err) {
    if (err && err.message && err.message.includes('PRIMARY') || (err.number === 2627)) {
      return res.status(400).json({ error: 'User already exists' });
    }
    console.error(err);
    res.status(500).json({ error: 'Server error' });
  }
});

// Login
app.post('/auth/login', async (req, res) => {
  try {
    const { email, password } = req.body;
    if (!email || !password) return res.status(400).json({ error: 'Email and password required' });
    const rows = await query('SELECT id, email, password_hash, display_name FROM users WHERE email = ?', [email]);
    const user = rows && rows[0];
    if (!user) return res.status(400).json({ error: 'Invalid credentials' });
    const ok = await bcrypt.compare(password, user.password_hash);
    if (!ok) return res.status(400).json({ error: 'Invalid credentials' });
    const token = jwt.sign({ id: user.id, email: user.email }, SECRET, { expiresIn: '7d' });
    res.json({ token, user: { id: user.id, email: user.email, displayName: user.display_name } });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Server error' });
  }
});

// auth middleware
function authMiddleware(req, res, next) {
  const auth = req.headers['authorization'];
  if (!auth) return res.status(401).json({ error: 'No token' });
  const parts = auth.split(' ');
  if (parts.length !== 2) return res.status(401).json({ error: 'Malformed token' });
  const token = parts[1];
  jwt.verify(token, SECRET, (err, payload) => {
    if (err) return res.status(401).json({ error: 'Invalid token' });
    req.user = payload;
    next();
  });
}

// settings endpoints
app.get('/user/settings', authMiddleware, async (req, res) => {
  try {
    const rows = await query('SELECT settings_json FROM users WHERE id = ?', [req.user.id]);
    res.json({ settings: JSON.parse(rows[0].settings_json || '{}') });
  } catch (err) {
    res.status(500).json({ error: 'Server error' });
  }
});

app.post('/user/settings', authMiddleware, async (req, res) => {
  try {
    const settings = req.body.settings || {};
    await exec('UPDATE users SET settings_json = ? WHERE id = ?', [JSON.stringify(settings), req.user.id]);
    res.json({ settings });
  } catch (err) {
    res.status(500).json({ error: 'Server error' });
  }
});

// portfolio
app.get('/portfolio', authMiddleware, async (req, res) => {
  try {
    const rows = await query('SELECT * FROM portfolios WHERE user_id = ?', [req.user.id]);
    const withPrices = rows.map(r => ({
      id: r.id,
      symbol: r.symbol,
      shares: r.shares,
      avg_price: r.avg_price,
      current_price: (Math.random() * 200).toFixed(2)
    }));
    res.json({ portfolio: withPrices });
  } catch (err) {
    res.status(500).json({ error: 'Server error' });
  }
});

app.post('/portfolio', authMiddleware, async (req, res) => {
  try {
    const { symbol, shares, avg_price } = req.body;
    if (!symbol || !shares) return res.status(400).json({ error: 'symbol and shares required' });
    await exec('INSERT INTO portfolios (user_id, symbol, shares, avg_price) VALUES (?, ?, ?, ?)', [req.user.id, symbol.toUpperCase(), shares, avg_price || null]);
    res.json({ success: true });
  } catch (err) {
    res.status(500).json({ error: 'Server error' });
  }
});

app.delete('/portfolio/:id', authMiddleware, async (req, res) => {
  try {
    const id = req.params.id;
    await exec('DELETE FROM portfolios WHERE id = ? AND user_id = ?', [id, req.user.id]);
    res.json({ success: true });
  } catch (err) {
    res.status(500).json({ error: 'Server error' });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log('Gold Plate backend running at http://localhost:' + PORT));
