// api.js
const API_BASE_URL = "http://localhost:5000/api"; // your backend base URL

// ---------------------
// User APIs
// ---------------------

// Register a new user
async function registerUser(userData) {
  try {
    const response = await fetch(`${API_BASE_URL}/users/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(userData),
    });
    return await response.json();
  } catch (err) {
    console.error("Register User Error:", err);
  }
}

// Login user
async function loginUser(loginData) {
  try {
    const response = await fetch(`${API_BASE_URL}/users/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(loginData),
    });
    return await response.json();
  } catch (err) {
    console.error("Login User Error:", err);
  }
}

// ---------------------
// Athlete APIs
// ---------------------

// Add a new athlete (admin only, pass JWT token)
async function addAthlete(athleteData, token) {
  try {
    const response = await fetch(`${API_BASE_URL}/athletes`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(athleteData),
    });
    return await response.json();
  } catch (err) {
    console.error("Add Athlete Error:", err);
  }
}

// Get all athletes (any authenticated user)
async function getAthletes(token) {
  try {
    const response = await fetch(`${API_BASE_URL}/athletes`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return await response.json();
  } catch (err) {
    console.error("Get Athletes Error:", err);
  }
}

// Register athlete to event (athlete or admin)
async function registerToEvent(regData, token) {
  try {
    const response = await fetch(`${API_BASE_URL}/register`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(regData),
    });
    return await response.json();
  } catch (err) {
    console.error("Register to Event Error:", err);
  }
}
