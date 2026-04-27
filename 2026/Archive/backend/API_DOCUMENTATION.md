# Blood Bank API Documentation

Base URL: `http://localhost:3000`

---

## 📑 Table of Contents
1. [Authentication APIs](#1-authentication-apis)
2. [Donor APIs](#2-donor-apis)
3. [Blood Request APIs](#3-blood-request-apis)

---

## 1. Authentication APIs

### 1.1 Register User

**Endpoint:** `POST /api/auth/register`

**Description:** Register a new user/donor

**Request Body:**
```json
{
  "name": "Abhay Kumar",
  "phone": "9876543210",
  "password": "password123",
  "city": "Delhi",
  "bloodGroup": "A+",
  "bio": "Happy to help save lives"
}
```

**CURL:**
```bash
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Abhay Kumar\",\"phone\":\"9876543210\",\"password\":\"password123\",\"city\":\"Delhi\",\"bloodGroup\":\"A+\",\"bio\":\"Happy to help save lives\"}"
```

**Success Response (201):**
```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "user": {
      "_id": "6733a1b2c4d5e6f7g8h9i0j1",
      "name": "Abhay Kumar",
      "phone": "9876543210",
      "city": "Delhi",
      "bloodGroup": "A+",
      "bio": "Happy to help save lives",
      "isDonor": true,
      "isAvailable": true,
      "createdAt": "2025-11-12T10:30:00.000Z"
    },
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**Error Response (400):**
```json
{
  "success": false,
  "message": "Phone number already registered"
}
```

---

### 1.2 Login User

**Endpoint:** `POST /api/auth/login`

**Description:** Login existing user

**Request Body:**
```json
{
  "phone": "9876543210",
  "password": "password123"
}
```

**CURL:**
```bash
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"phone\":\"9876543210\",\"password\":\"password123\"}"
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "user": {
      "_id": "6733a1b2c4d5e6f7g8h9i0j1",
      "name": "Abhay Kumar",
      "phone": "9876543210",
      "city": "Delhi",
      "bloodGroup": "A+",
      "bio": "Happy to help save lives",
      "isDonor": true,
      "isAvailable": true
    },
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**Error Response (401):**
```json
{
  "success": false,
  "message": "Invalid phone number or password"
}
```

---

### 1.3 Get Profile

**Endpoint:** `GET /api/auth/profile`

**Description:** Get current logged-in user profile

**Authentication:** Required (JWT Token)

**CURL:**
```bash
curl -X GET http://localhost:3000/api/auth/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "_id": "6733a1b2c4d5e6f7g8h9i0j1",
    "name": "Abhay Kumar",
    "phone": "9876543210",
    "city": "Delhi",
    "bloodGroup": "A+",
    "bio": "Happy to help save lives",
    "isDonor": true,
    "isAvailable": true,
    "lastDonation": null
  }
}
```

**Error Response (401):**
```json
{
  "success": false,
  "message": "No token, authorization denied"
}
```

---

### 1.4 Update Profile

**Endpoint:** `PUT /api/auth/profile`

**Description:** Update user profile

**Authentication:** Required (JWT Token)

**Request Body:**
```json
{
  "name": "Abhay Kumar",
  "city": "Mumbai",
  "bloodGroup": "A+",
  "bio": "Ready to donate blood",
  "isAvailable": true,
  "lastDonation": "2025-10-15"
}
```

**CURL:**
```bash
curl -X PUT http://localhost:3000/api/auth/profile \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d "{\"name\":\"Abhay Kumar\",\"city\":\"Mumbai\",\"bloodGroup\":\"A+\",\"bio\":\"Ready to donate blood\",\"isAvailable\":true}"
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Profile updated successfully",
  "data": {
    "_id": "6733a1b2c4d5e6f7g8h9i0j1",
    "name": "Abhay Kumar",
    "phone": "9876543210",
    "city": "Mumbai",
    "bloodGroup": "A+",
    "bio": "Ready to donate blood",
    "isDonor": true,
    "isAvailable": true,
    "lastDonation": "2025-10-15T00:00:00.000Z"
  }
}
```

---

## 2. Donor APIs

### 2.1 Get All Donors

**Endpoint:** `GET /api/donors`

**Description:** Get all available donors

**Authentication:** Not required

**CURL:**
```bash
curl -X GET http://localhost:3000/api/donors
```

**Success Response (200):**
```json
{
  "success": true,
  "count": 2,
  "data": [
    {
      "_id": "6733a1b2c4d5e6f7g8h9i0j1",
      "name": "Abhay Kumar",
      "phone": "9876543210",
      "city": "Delhi",
      "bloodGroup": "A+",
      "bio": "Happy to help save lives",
      "isDonor": true,
      "isAvailable": true,
      "createdAt": "2025-11-12T10:30:00.000Z"
    },
    {
      "_id": "6733a1b2c4d5e6f7g8h9i0j2",
      "name": "Rahul Sharma",
      "phone": "9876543211",
      "city": "Mumbai",
      "bloodGroup": "O+",
      "bio": "Regular blood donor",
      "isDonor": true,
      "isAvailable": true,
      "createdAt": "2025-11-11T08:20:00.000Z"
    }
  ]
}
```

---

### 2.2 Search Donors

**Endpoint:** `GET /api/donors/search`

**Description:** Search donors by city and/or blood group

**Authentication:** Not required

**Query Parameters:**
- `city` (optional) - City name
- `bloodGroup` (optional) - Blood group (A+, A-, B+, B-, O+, O-, AB+, AB-)

**CURL (Search by City):**
```bash
curl -X GET "http://localhost:3000/api/donors/search?city=Delhi"
```

**CURL (Search by Blood Group):**
```bash
curl -X GET "http://localhost:3000/api/donors/search?bloodGroup=A%2B"
```

**CURL (Search by Both):**
```bash
curl -X GET "http://localhost:3000/api/donors/search?city=Delhi&bloodGroup=A%2B"
```

**Success Response (200):**
```json
{
  "success": true,
  "count": 1,
  "data": [
    {
      "_id": "6733a1b2c4d5e6f7g8h9i0j1",
      "name": "Abhay Kumar",
      "phone": "9876543210",
      "city": "Delhi",
      "bloodGroup": "A+",
      "bio": "Happy to help save lives",
      "isDonor": true,
      "isAvailable": true
    }
  ]
}
```

---

### 2.3 Get Donor by ID

**Endpoint:** `GET /api/donors/:id`

**Description:** Get specific donor details by ID

**Authentication:** Not required

**CURL:**
```bash
curl -X GET http://localhost:3000/api/donors/6733a1b2c4d5e6f7g8h9i0j1
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "_id": "6733a1b2c4d5e6f7g8h9i0j1",
    "name": "Abhay Kumar",
    "phone": "9876543210",
    "city": "Delhi",
    "bloodGroup": "A+",
    "bio": "Happy to help save lives",
    "isDonor": true,
    "isAvailable": true
  }
}
```

**Error Response (404):**
```json
{
  "success": false,
  "message": "Donor not found"
}
```

---

## 3. Blood Request APIs

### 3.1 Create Blood Request

**Endpoint:** `POST /api/requests`

**Description:** Create a new blood request (with optional image upload)

**Authentication:** Required (JWT Token)

**Content-Type:** `multipart/form-data`

**Form Fields:**
- `bloodGroup` (required) - Blood group needed
- `city` (required) - City name
- `message` (required) - Request message
- `urgency` (optional) - low/medium/high (default: medium)
- `image` (optional) - Image file (jpeg/jpg/png, max 5MB)

**CURL (Without Image):**
```bash
curl -X POST http://localhost:3000/api/requests \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -F "bloodGroup=A+" \
  -F "city=Delhi" \
  -F "message=Urgent blood needed for accident patient" \
  -F "urgency=high"
```

**CURL (With Image):**
```bash
curl -X POST http://localhost:3000/api/requests \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -F "bloodGroup=A+" \
  -F "city=Delhi" \
  -F "message=Urgent blood needed for surgery" \
  -F "urgency=high" \
  -F "image=@/path/to/image.jpg"
```

**Success Response (201):**
```json
{
  "success": true,
  "message": "Request created successfully",
  "data": {
    "_id": "6733b2c3d4e5f6g7h8i9j0k1",
    "user": {
      "_id": "6733a1b2c4d5e6f7g8h9i0j1",
      "name": "Abhay Kumar",
      "phone": "9876543210",
      "city": "Delhi",
      "bloodGroup": "A+"
    },
    "bloodGroup": "A+",
    "city": "Delhi",
    "message": "Urgent blood needed for accident patient",
    "urgency": "high",
    "status": "active",
    "image": "/uploads/1699876543210.jpg",
    "responses": [],
    "createdAt": "2025-11-12T11:00:00.000Z"
  }
}
```

---

### 3.2 Get All Blood Requests

**Endpoint:** `GET /api/requests`

**Description:** Get all active blood requests

**Authentication:** Not required

**CURL:**
```bash
curl -X GET http://localhost:3000/api/requests
```

**Success Response (200):**
```json
{
  "success": true,
  "count": 2,
  "data": [
    {
      "_id": "6733b2c3d4e5f6g7h8i9j0k1",
      "user": {
        "_id": "6733a1b2c4d5e6f7g8h9i0j1",
        "name": "Abhay Kumar",
        "phone": "9876543210",
        "city": "Delhi",
        "bloodGroup": "A+"
      },
      "bloodGroup": "A+",
      "city": "Delhi",
      "message": "Urgent blood needed for accident patient",
      "urgency": "high",
      "status": "active",
      "image": "/uploads/1699876543210.jpg",
      "responses": [],
      "createdAt": "2025-11-12T11:00:00.000Z"
    },
    {
      "_id": "6733b2c3d4e5f6g7h8i9j0k2",
      "user": {
        "_id": "6733a1b2c4d5e6f7g8h9i0j2",
        "name": "Rahul Sharma",
        "phone": "9876543211",
        "city": "Mumbai",
        "bloodGroup": "O+"
      },
      "bloodGroup": "O+",
      "city": "Mumbai",
      "message": "Blood needed for cancer treatment",
      "urgency": "medium",
      "status": "active",
      "image": null,
      "responses": [],
      "createdAt": "2025-11-11T09:30:00.000Z"
    }
  ]
}
```

---

### 3.3 Search Blood Requests

**Endpoint:** `GET /api/requests/search`

**Description:** Search blood requests by city, blood group, and/or urgency

**Authentication:** Not required

**Query Parameters:**
- `city` (optional) - City name
- `bloodGroup` (optional) - Blood group
- `urgency` (optional) - low/medium/high

**CURL (Search by City):**
```bash
curl -X GET "http://localhost:3000/api/requests/search?city=Delhi"
```

**CURL (Search by Blood Group):**
```bash
curl -X GET "http://localhost:3000/api/requests/search?bloodGroup=A%2B"
```

**CURL (Search by Urgency):**
```bash
curl -X GET "http://localhost:3000/api/requests/search?urgency=high"
```

**CURL (Multiple Parameters):**
```bash
curl -X GET "http://localhost:3000/api/requests/search?city=Delhi&bloodGroup=A%2B&urgency=high"
```

**Success Response (200):**
```json
{
  "success": true,
  "count": 1,
  "data": [
    {
      "_id": "6733b2c3d4e5f6g7h8i9j0k1",
      "user": {
        "_id": "6733a1b2c4d5e6f7g8h9i0j1",
        "name": "Abhay Kumar",
        "phone": "9876543210"
      },
      "bloodGroup": "A+",
      "city": "Delhi",
      "message": "Urgent blood needed for accident patient",
      "urgency": "high",
      "status": "active",
      "createdAt": "2025-11-12T11:00:00.000Z"
    }
  ]
}
```

---

### 3.4 Get Blood Request by ID

**Endpoint:** `GET /api/requests/:id`

**Description:** Get specific blood request details by ID

**Authentication:** Not required

**CURL:**
```bash
curl -X GET http://localhost:3000/api/requests/6733b2c3d4e5f6g7h8i9j0k1
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "_id": "6733b2c3d4e5f6g7h8i9j0k1",
    "user": {
      "_id": "6733a1b2c4d5e6f7g8h9i0j1",
      "name": "Abhay Kumar",
      "phone": "9876543210",
      "city": "Delhi",
      "bloodGroup": "A+"
    },
    "bloodGroup": "A+",
    "city": "Delhi",
    "message": "Urgent blood needed for accident patient",
    "urgency": "high",
    "status": "active",
    "image": "/uploads/1699876543210.jpg",
    "responses": [],
    "createdAt": "2025-11-12T11:00:00.000Z"
  }
}
```

**Error Response (404):**
```json
{
  "success": false,
  "message": "Request not found"
}
```

---

### 3.5 Update Request Status

**Endpoint:** `PUT /api/requests/:id/status`

**Description:** Update blood request status (only by request creator)

**Authentication:** Required (JWT Token)

**Request Body:**
```json
{
  "status": "fulfilled"
}
```

**Status Options:** `active`, `fulfilled`, `expired`

**CURL:**
```bash
curl -X PUT http://localhost:3000/api/requests/6733b2c3d4e5f6g7h8i9j0k1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d "{\"status\":\"fulfilled\"}"
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Request status updated",
  "data": {
    "_id": "6733b2c3d4e5f6g7h8i9j0k1",
    "bloodGroup": "A+",
    "city": "Delhi",
    "message": "Urgent blood needed for accident patient",
    "urgency": "high",
    "status": "fulfilled",
    "createdAt": "2025-11-12T11:00:00.000Z"
  }
}
```

**Error Response (403):**
```json
{
  "success": false,
  "message": "Not authorized to update this request"
}
```

---

### 3.6 Delete Blood Request

**Endpoint:** `DELETE /api/requests/:id`

**Description:** Delete blood request (only by request creator)

**Authentication:** Required (JWT Token)

**CURL:**
```bash
curl -X DELETE http://localhost:3000/api/requests/6733b2c3d4e5f6g7h8i9j0k1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Request deleted successfully"
}
```

**Error Response (403):**
```json
{
  "success": false,
  "message": "Not authorized to delete this request"
}
```

---

## 🔐 Authentication Header Format

For protected routes, include the JWT token in the Authorization header:

```
Authorization: Bearer <your_jwt_token>
```

Example:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiI2NzMzYTFiMmM0ZDVlNmY3ZzhoOWkwajEiLCJpYXQiOjE2OTk4NzY1NDMsImV4cCI6MTcwMjQ2ODU0M30.abc123xyz
```

---

## 📝 Notes

1. **Blood Groups Supported:** A+, A-, B+, B-, O+, O-, AB+, AB-
2. **Urgency Levels:** low, medium, high
3. **Request Status:** active, fulfilled, expired
4. **Image Upload:** Max 5MB, formats: JPEG, JPG, PNG
5. **JWT Token:** Valid for 30 days after login/registration
6. **All timestamps are in UTC format**

---

## 🚀 Quick Test Commands

### Register a User
```bash
curl -X POST http://localhost:3000/api/auth/register -H "Content-Type: application/json" -d "{\"name\":\"Test User\",\"phone\":\"9999999999\",\"password\":\"test123\",\"city\":\"Delhi\",\"bloodGroup\":\"A+\"}"
```

### Login
```bash
curl -X POST http://localhost:3000/api/auth/login -H "Content-Type: application/json" -d "{\"phone\":\"9999999999\",\"password\":\"test123\"}"
```

### Get All Donors
```bash
curl -X GET http://localhost:3000/api/donors
```

### Get All Requests
```bash
curl -X GET http://localhost:3000/api/requests
```

---

**Generated on:** November 12, 2025  
**API Version:** 1.0.0  
**Base URL:** http://localhost:3000
