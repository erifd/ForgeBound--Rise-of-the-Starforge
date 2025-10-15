const fs = require("fs");
const admin = require("firebase-admin");
const bcrypt = require("bcryptjs");

// Initialize Firebase
admin.initializeApp({
  credential: admin.credential.applicationDefault(),
});

const db = admin.firestore();

// Load users.txt
const file = "users.txt";
const lines = fs.readFileSync(file, "utf-8").trim().split("\n");

(async () => {
  for (const line of lines) {
    const [username, password] = line.split(":");
    if (!username || !password) continue;

    const userRef = db.collection("users").doc(username.trim());
    const doc = await userRef.get();
    if (doc.exists) {
      console.log(`Skipping ${username}, already exists.`);
      continue;
    }

    const passwordHash = await bcrypt.hash(password.trim(), 12);
    await userRef.set({
      passwordHash,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    console.log(`✅ Migrated ${username}`);
  }
  console.log("Migration complete.");
})();
