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
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// ✅ Rate limiting
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 100,
});
app.use(limiter);

app.use(helmet({
  contentSecurityPolicy: false,
}));

// ----------------- ROUTES -----------------

// 🟢 Signup route
app.post("/api/signup", async (req, res) => {
  try {
    console.log("🔵 Signup request received");
    const { username, password } = req.body;

    if (!username || !password) {
      return res.status(400).json({ error: "Username and password required" });
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
      saveCode: "",
      levelsCompleted: 0
    });

    console.log("✅ Signup successful for:", username);
    res.json({ message: "Signup successful" });
  } catch (err) {
    console.error("❌ SIGNUP ERROR:", err);
    res.status(500).json({ error: "Internal server error", details: err.message });
  }
});

// 🔵 Login route
app.post("/api/login", async (req, res) => {
  try {
    console.log("🔵 Login request received");
    const { username, password } = req.body;

    if (!username || !password) {
      return res.status(400).json({ error: "Username and password required" });
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

    console.log("✅ Login successful for:", username);
    res.json({ message: "Login successful" });
  } catch (err) {
    console.error("❌ LOGIN ERROR:", err);
    res.status(500).json({ error: "Internal server error", details: err.message });
  }
});

// 💾 Save game progress
app.post("/api/save", async (req, res) => {
  try {
    console.log("💾 Save request received");
    const { username, password, save_code, levels_completed } = req.body;

    if (!username || !password) {
      return res.status(400).json({ error: "Username and password required" });
    }

    // Verify credentials
    const userRef = db.collection("users").doc(username);
    const doc = await userRef.get();

    if (!doc.exists) {
      return res.status(400).json({ error: "User not found" });
    }

    const { passwordHash } = doc.data();
    const isMatch = await bcrypt.compare(password, passwordHash);

    if (!isMatch) {
      return res.status(400).json({ error: "Invalid credentials" });
    }

    // Update save data
    await userRef.update({
      saveCode: save_code,
      levelsCompleted: levels_completed,
      lastSaved: admin.firestore.FieldValue.serverTimestamp()
    });

    console.log(`✅ Progress saved for ${username}: ${save_code}`);
    res.json({ 
      message: "Save successful", 
      save_code: save_code,
      levels_completed: levels_completed 
    });
  } catch (err) {
    console.error("❌ SAVE ERROR:", err);
    res.status(500).json({ error: "Internal server error", details: err.message });
  }
});

// 📂 Load game progress
app.post("/api/load", async (req, res) => {
  try {
    console.log("📂 Load request received");
    const { username, password } = req.body;

    if (!username || !password) {
      return res.status(400).json({ error: "Username and password required" });
    }

    // Verify credentials
    const userRef = db.collection("users").doc(username);
    const doc = await userRef.get();

    if (!doc.exists) {
      return res.status(400).json({ error: "User not found" });
    }

    const userData = doc.data();
    const isMatch = await bcrypt.compare(password, userData.passwordHash);

    if (!isMatch) {
      return res.status(400).json({ error: "Invalid credentials" });
    }

    // Return save data
    console.log(`✅ Progress loaded for ${username}: ${userData.saveCode || 'No save'}`);
    res.json({ 
      message: "Load successful",
      save_code: userData.saveCode || "",
      levels_completed: userData.levelsCompleted || 0
    });
  } catch (err) {
    console.error("❌ LOAD ERROR:", err);
    res.status(500).json({ error: "Internal server error", details: err.message });
  }
});

// ----------------- START SERVER -----------------
app.listen(PORT, () => {
  console.log(`✅ Auth API running on http://localhost:${PORT}`);
  console.log(`📁 Service account key path: ${process.env.GOOGLE_APPLICATION_CREDENTIALS}`);
  console.log(`🔥 Firebase initialized successfully`);
});