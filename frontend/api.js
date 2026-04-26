const API_BASE_URL = "http://localhost:8080/api";

/* =========================
   🔐 SAFE FETCH HELPER
========================= */
async function safeFetch(url, options = {}) {
  try {
    const response = await fetch(url, options);

    let data = null;
    try {
      data = await response.json();
    } catch (e) {
      // backend returned empty or invalid JSON
      data = null;
    }

    if (!response.ok) {
      return {
        success: false,
        message: data?.message || "Request failed",
      };
    }

    return data;

  } catch (err) {
    return {
      success: false,
      message: err.message,
    };
  }
}

/* =========================
   🔐 AUTH APIs
========================= */

// Register
async function registerUser(userData) {
  return safeFetch(`${API_BASE_URL}/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(userData),
  });
}

// Login (Step 1 → sends OTP)
async function loginUser(loginData) {
  return safeFetch(`${API_BASE_URL}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(loginData),
  });
}

// Verify email OTP (optional flow)
async function verifyOtp(data) {
  return safeFetch(`${API_BASE_URL}/auth/verify-otp`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
}

// Verify LOGIN OTP (Step 2 → returns token + user)
async function verifyLoginOtp(data) {
  return safeFetch(`${API_BASE_URL}/auth/verify-login-otp`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
}

// Resend OTP
async function resendLoginOtp(data) {
  return safeFetch(`${API_BASE_URL}/auth/resend-login-otp`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
}

/* =========================
   🏟 EVENT APIs (FIXED)
========================= */

// Get all events
async function getEvents() {
  return safeFetch(`${API_BASE_URL}/events`, {
    method: "GET",
  });
}

// Create event (ADMIN ONLY)
async function createEvent(eventData, token) {
  return safeFetch(`${API_BASE_URL}/events`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(eventData),
  });
}

/* =========================
   🧑 ATHLETE APIs (optional if used)
========================= */

async function getAthletes(token) {
  return safeFetch(`${API_BASE_URL}/athletes`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
}

/* =========================
   📦 EXPORTS (if needed)
========================= */