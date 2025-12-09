const express = require('express');
const cors = require('cors');
const db = require('./db');
const dotenv = require('dotenv');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcrypt');

dotenv.config();

const app = express();
app.use(cors());
app.use(express.json());

// Secret key for JWT
const JWT_SECRET = process.env.JWT_SECRET || 'supersecretkey';

// -------------------------
// Middleware to verify JWT
// -------------------------
function authenticateToken(req, res, next) {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1]; // Bearer TOKEN

  if (!token) return res.status(401).json({ message: 'Access token missing' });

  jwt.verify(token, JWT_SECRET, (err, user) => {
    if (err) return res.status(403).json({ message: 'Invalid token' });
    req.user = user; // user = { id, role }
    next();
  });
}

// -------------------------
// Middleware for Role-Based Access
// -------------------------
function authorizeRoles(...roles) {
  return (req, res, next) => {
    if (!roles.includes(req.user.role)) {
      return res.status(403).json({ message: 'Forbidden: Insufficient role' });
    }
    next();
  };
}

// ✅ Test Route
app.get('/', (req, res) => {
  res.send('AthleticaX Backend Running 🚀');
});

// -------------------------
// Users
// -------------------------

// User Register (with bcrypt)
app.post('/api/users/register', async (req, res) => {
  const { name, email, password, role } = req.body;
  if (!name || !email || !password) return res.status(400).json({ message: 'name, email, password required' });

  const userRole = role || 'athlete';

  db.query('SELECT id FROM users WHERE email = ?', [email], async (err, rows) => {
    if (err) return res.status(500).json({ error: err });
    if (rows.length > 0) return res.status(409).json({ message: 'Email already exists' });

    const hashedPassword = await bcrypt.hash(password, 10);
    const sql = 'INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)';
    db.query(sql, [name, email, hashedPassword, userRole], (err, result) => {
      if (err) return res.status(500).json({ error: err });
      res.status(201).json({ message: '✅ User registered', userId: result.insertId });
    });
  });
});

// User Login (with JWT)
app.post('/api/users/login', (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) return res.status(400).json({ message: 'email and password required' });

  db.query('SELECT id, name, email, password, role FROM users WHERE email = ?', [email], async (err, rows) => {
    if (err) return res.status(500).json({ error: err });
    if (rows.length === 0) return res.status(401).json({ message: 'Invalid credentials' });

    const user = rows[0];
    const match = await bcrypt.compare(password, user.password);
    if (!match) return res.status(401).json({ message: 'Invalid credentials' });

    const token = jwt.sign({ id: user.id, role: user.role }, JWT_SECRET, { expiresIn: '8h' });
    res.json({ message: '✅ Login successful', token, user: { id: user.id, name: user.name, role: user.role } });
  });
});

// -------------------------
// Athletes
// -------------------------

// Fetch All Athletes (any authenticated user)
app.get('/api/athletes', authenticateToken, (req, res) => {
  db.query('SELECT * FROM athletes', (err, results) => {
    if (err) return res.status(500).json({ error: err });
    res.json(results);
  });
});

// Add New Athlete (admin only)
app.post('/api/athletes', authenticateToken, authorizeRoles('admin'), (req, res) => {
  const { name, age, sport } = req.body;
  if (!name || !age || !sport) return res.status(400).json({ message: "All fields are required" });

  const sql = "INSERT INTO athletes (name, age, sport) VALUES (?, ?, ?)";
  db.query(sql, [name, age, sport], (err, result) => {
    if (err) return res.status(500).json({ error: err });
    res.status(201).json({ message: "✅ Athlete added successfully", athleteId: result.insertId });
  });
});

// Get All Events of an Athlete (any authenticated user)
app.get('/api/athletes/:athleteId/events', authenticateToken, (req, res) => {
  const { athleteId } = req.params;
  const sql = `
    SELECT events.id, events.event_name, events.sport, events.event_date, events.location
    FROM registrations
    JOIN events ON registrations.event_id = events.id
    WHERE registrations.athlete_id = ?
  `;
  db.query(sql, [athleteId], (err, results) => {
    if (err) return res.status(500).json(err);
    res.json(results);
  });
});

// -------------------------
// Events
// -------------------------

// Get All Events (any authenticated user)
app.get('/api/events', authenticateToken, (req, res) => {
  db.query('SELECT * FROM events', (err, results) => {
    if (err) return res.status(500).json(err);
    res.json(results);
  });
});

// Add New Event (admin only)
app.post('/api/events', authenticateToken, authorizeRoles('admin'), (req, res) => {
  const { event_name, sport, event_date, location } = req.body;
  if (!event_name || !sport || !event_date || !location) return res.status(400).json({ message: "All fields required" });

  const sql = `
    INSERT INTO events (event_name, sport, event_date, location)
    VALUES (?, ?, ?, ?)
  `;
  db.query(sql, [event_name, sport, event_date, location], (err, result) => {
    if (err) return res.status(500).json(err);
    res.status(201).json({ message: "✅ Event created successfully", eventId: result.insertId });
  });
});

// Get All Athletes in an Event (any authenticated user)
app.get('/api/events/:eventId/athletes', authenticateToken, (req, res) => {
  const { eventId } = req.params;
  const sql = `
    SELECT athletes.id, athletes.name, athletes.age, athletes.sport
    FROM registrations
    JOIN athletes ON registrations.athlete_id = athletes.id
    WHERE registrations.event_id = ?
  `;
  db.query(sql, [eventId], (err, results) => {
    if (err) return res.status(500).json(err);
    res.json(results);
  });
});

// -------------------------
// Register Athlete to Event
// -------------------------

// Only athlete or admin can register; prevent duplicates
app.post('/api/register', authenticateToken, authorizeRoles('athlete', 'admin'), (req, res) => {
  const { athlete_id, event_id } = req.body;
  if (!athlete_id || !event_id) return res.status(400).json({ message: "Athlete ID and Event ID required" });

  // Check for duplicate
  db.query('SELECT * FROM registrations WHERE athlete_id = ? AND event_id = ?', [athlete_id, event_id], (err, rows) => {
    if (err) return res.status(500).json(err);
    if (rows.length > 0) return res.status(409).json({ message: 'Athlete already registered for this event' });

    db.query('INSERT INTO registrations (athlete_id, event_id) VALUES (?, ?)', [athlete_id, event_id], (err, result) => {
      if (err) return res.status(500).json(err);
      res.status(201).json({ message: '✅ Athlete registered to event', registrationId: result.insertId });
    });
  });
});

// -------------------------
// Start Server
// -------------------------
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
  console.log(`✅ Server running on port ${PORT}`);
});
