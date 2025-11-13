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

// ✅ Middleware - CRITICAL ORDER!
app.use(cors());           // FIRST - Allow requests
app.use(express.json());   // SECOND - Parse JSON
app.use(express.urlencoded({ extended: true })); // THIRD - Parse form data

// ✅ Rate limiting: max 100 requests per 15 minutes per IP
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 100,
});
app.use(limiter);

// IMPORTANT: Helmet AFTER json parsers, but disable contentSecurityPolicy for local dev
app.use(helmet({
  contentSecurityPolicy: false,
}));

// Add a debug middleware to see what's being received
app.use((req, res, next) => {
  console.log("📥 Received request:", req.method, req.path);
  
  // Capture raw body to see what's actually being sent
  let rawBody = '';
  req.on('data', chunk => {
    rawBody += chunk.toString();
  });
  req.on('end', () => {
    console.log("📦 Raw body data:", rawBody);
    console.log("📦 Parsed body:", req.body);
    console.log("📋 Headers:", req.headers);
    
    // If body is empty but raw data exists, manually parse it
    if (!req.body && rawBody) {
      try {
        req.body = JSON.parse(rawBody);
        console.log("✅ Manually parsed body:", req.body);
      } catch (e) {
        console.log("❌ Failed to parse raw body:", e.message);
      }
    }
    next();
  });
});

// ----------------- ROUTES -----------------

// 🟢 Signup route
app.post("/api/signup", async (req, res) => {
  try {
    console.log("🔵 Signup request received");
    console.log("📦 Request body:", req.body);
    
    const { username, password } = req.body;

    if (!username || !password) {
      console.log("❌ Missing username or password");
      return res
        .status(400)
        .json({ error: "Username and password required" });
    }

    console.log("🔍 Checking if user exists:", username);
    const userRef = db.collection("users").doc(username);
    const doc = await userRef.get();

    if (doc.exists) {
      console.log("⚠️ User already exists:", username);
      return res.status(400).json({ error: "User already exists" });
    }

    console.log("🔐 Hashing password...");
    const passwordHash = await bcrypt.hash(password, 12);
    
    console.log("💾 Saving user to database...");
    await userRef.set({
      passwordHash,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    console.log("✅ Signup successful for:", username);
    res.json({ message: "Signup successful" });
  } catch (err) {
    console.error("❌❌❌ SIGNUP ERROR ❌❌❌");
    console.error("Error type:", err.constructor.name);
    console.error("Error message:", err.message);
    console.error("Error code:", err.code);
    console.error("Full error:", err);
    console.error("Stack trace:", err.stack);
    res.status(500).json({ error: "Internal server error", details: err.message });
  }
});

// 🔵 Login route
app.post("/api/login", async (req, res) => {
  try {
    console.log("🔵 Login request received");
    console.log("📦 Request body:", req.body);
    
    const { username, password } = req.body;

    if (!username || !password) {
      console.log("❌ Missing username or password");
      return res
        .status(400)
        .json({ error: "Username and password required" });
    }

    console.log("🔍 Looking up user:", username);
    const userRef = db.collection("users").doc(username);
    const doc = await userRef.get();

    if (!doc.exists) {
      console.log("⚠️ User not found:", username);
      return res.status(400).json({ error: "Invalid credentials" });
    }

    console.log("🔐 Verifying password...");
    const { passwordHash } = doc.data();
    const isMatch = await bcrypt.compare(password, passwordHash);

    if (!isMatch) {
      console.log("❌ Password mismatch for:", username);
      return res.status(400).json({ error: "Invalid credentials" });
    }

    console.log("✅ Login successful for:", username);
    res.json({ message: "Login successful" });
  } catch (err) {
    console.error("❌❌❌ LOGIN ERROR ❌❌❌");
    console.error("Error type:", err.constructor.name);
    console.error("Error message:", err.message);
    console.error("Error code:", err.code);
    console.error("Full error:", err);
    console.error("Stack trace:", err.stack);
    res.status(500).json({ error: "Internal server error", details: err.message });
  }
});

// ----------------- START SERVER -----------------
app.listen(PORT, () => {
  console.log(`✅ Auth API running on http://localhost:${PORT}`);
  console.log(`📁 Service account key path: ${process.env.GOOGLE_APPLICATION_CREDENTIALS}`);
  console.log(`🔥 Firebase initialized successfully`);
});