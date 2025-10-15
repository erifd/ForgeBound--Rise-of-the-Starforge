const express = require("express");
const admin = require("firebase-admin");
const bcrypt = require("bcryptjs");
const cors = require("cors");
const helmet = require("helmet");
const rateLimit = require("express-rate-limit");

// ✅ Initialize Firebase with Application Default Credentials
admin.initializeApp({
  credential: admin.credential.applicationDefault(),
});

const db = admin.firestore();
const app = express();
const PORT = 3000;

// ✅ Middleware - ORDER MATTERS!
app.use(cors());           // Allow requests from your Java app - FIRST
app.use(express.json());   // Parse JSON - SECOND
app.use(express.urlencoded({ extended: true })); // Handle form-style data - THIRD
app.use(helmet());         // Security headers - AFTER parsers

// ✅ Rate limiting: max 100 requests per 15 minutes per IP
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 100,
});
app.use(limiter);

// Add a debug middleware to see what's being received
app.use((req, res, next) => {
  console.log("📥 Received request:", req.method, req.path);
  console.log("📦 Body:", req.body);
  console.log("📋 Headers:", req.headers);
  next();
});

// ----------------- ROUTES -----------------

// 🟢 Signup route
app.post("/api/signup", async (req, res) => {
  try {
    console.log("Signup request body:", req.body); // Debug log
    const { username, password } = req.body;

    if (!username || !password) {
      return res
        .status(400)
        .json({ error: "Username and password required" });
    }

    const userRef = db.collection("users").doc(username);
    const doc = await userRef.get();

    if (doc.exists) {
      return res.status(400).json({ error: "User already exists" });
    }

    const passwordHash = await bcrypt.hash(password, 12);
    await userRef.set({
      passwordHash,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    res.json({ message: "Signup successful" });
  } catch (err) {
    console.error("❌ Signup error:", err);
    res.status(500).json({ error: "Internal server error" });
  }
});

// 🔵 Login route
app.post("/api/login", async (req, res) => {
  try {
    console.log("Login request body:", req.body); // Debug log
    const { username, password } = req.body;

    if (!username || !password) {
      return res
        .status(400)
        .json({ error: "Username and password required" });
    }

    const userRef = db.collection("users").doc(username);
    const doc = await userRef.get();

    if (!doc.exists) {
      return res.status(400).json({ error: "Invalid credentials" });
    }

    const { passwordHash } = doc.data();
    const isMatch = await bcrypt.compare(password, passwordHash);

    if (!isMatch) {
      return res.status(400).json({ error: "Invalid credentials" });
    }

    res.json({ message: "Login successful" });
  } catch (err) {
    console.error("❌ Login error:", err);
    res.status(500).json({ error: "Internal server error" });
  }
});

// ----------------- START SERVER -----------------
app.listen(PORT, () =>
  console.log(`✅ Auth API running on http://localhost:${PORT}`)
);