// api.js
const API_BASE_URL = "http://localhost:8080/api"; // Spring Boot base URL

// ---------------------
// Auth APIs
// ---------------------

// Register a new user
async function registerUser(userData) {
  try {
    const response = await fetch(`${API_BASE_URL}/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(userData),
    });

    return await response.json();

  } catch (err) {
    return { success: false, message: err.message };
  }
}

// Login user
async function loginUser(loginData) {
  try {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(loginData),
    });

    return await response.json();

  } catch (err) {
    return { success: false, message: err.message };
  }
}


// ---------------------
// Event APIs
// ---------------------

// Get all events
async function getEvents() {
  try {
    const response = await fetch(`${API_BASE_URL}/events/all`);
    if (!response.ok) {
      const errorText = await response.text();
      console.error("Get Events failed:", errorText);
      return [];
    }
    return await response.json(); // returns list of events
  } catch (err) {
    console.error("Get Events Error:", err);
    return [];
  }
}

// Create a new event
async function createEvent(eventData) {
  try {
    const response = await fetch(`${API_BASE_URL}/events/create`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(eventData),
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error("Create Event failed:", errorText);
      return { success: false, error: errorText };
    }

    const data = await response.json();
    return { success: true, event: data };
  } catch (err) {
    console.error("Create Event Error:", err);
    return { success: false, error: err.message };
  }
}

// export { registerUser, loginUser, getEvents, createEvent };
