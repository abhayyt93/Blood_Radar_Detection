# Blood Bank API - Project Overview

## 📋 Table of Contents
1. [Project Description](#1-project-description)
2. [Tech Stack](#2-tech-stack)
3. [Project Structure](#3-project-structure)
4. [Features](#4-features)
5. [Database Schema](#5-database-schema)
6. [API Endpoints](#6-api-endpoints)
7. [Middleware](#7-middleware)
8. [Environment Variables](#8-environment-variables)
9. [How to Run](#9-how-to-run)
10. [Future Enhancements](#10-future-enhancements)

---

## 1. Project Description

**Blood Bank API** ek RESTful backend service hai jo blood donation management ke liye design kiya gaya hai. Yeh application donors, blood requests, donations ko manage karta hai aur users ke beech communication facilitate karta hai.

### Key Highlights:
- User registration aur authentication with JWT
- Donor search by city aur blood group
- Blood request creation with image upload support
- Real-time chat between users
- Admin panel for system management
- Notification system for broadcasts

---

## 2. Tech Stack

| Component | Technology |
|-----------|------------|
| **Runtime** | Node.js |
| **Framework** | Express.js |
| **Database** | MongoDB with Mongoose ODM |
| **Authentication** | JWT (JSON Web Tokens) |
| **Password Hashing** | bcryptjs |
| **File Upload** | Multer |
| **Validation** | express-validator |
| **CORS** | cors |
| **Environment** | dotenv |

### Version Information:
```json
{
  "node": ">=14.0.0",
  "express": "^4.18.2",
  "mongoose": "^8.0.3",
  "jsonwebtoken": "^9.0.2"
}
```

---

## 3. Project Structure

```
Archive/backend/
├── src/
│   ├── index.js                    # Main server entry point
│   ├── config/
│   │   └── database.js             # MongoDB connection config
│   ├── middleware/
│   │   ├── auth.js                 # JWT authentication middleware
│   │   └── adminMiddleware.js      # Admin role verification
│   ├── models/
│   │   ├── User.js                 # User/Donor schema
│   │   ├── Request.js              # Blood request schema
│   │   ├── Donation.js             # Donation records schema
│   │   ├── Chat.js                 # Chat messages schema
│   │   ├── Notification.js         # Notifications schema
│   │   └── ContactRequest.js       # Contact request schema
│   ├── controllers/
│   │   ├── authController.js       # Auth logic
│   │   ├── donorController.js      # Donor management
│   │   ├── requestController.js    # Request management
│   │   ├── donationController.js   # Donation tracking
│   │   ├── chatController.js       # Chat functionality
│   │   ├── adminController.js      # Admin operations
│   │   ├── notificationController.js
│   │   └── contactRequestController.js
│   └── routes/
│       ├── auth.js                 # /api/auth routes
│       ├── donors.js               # /api/donors routes
│       ├── requests.js             # /api/requests routes
│       ├── donations.js            # /api/donations routes
│       ├── chat.js                 # /api/chat routes
│       ├── admin.js                # /api/admin routes
│       ├── notifications.js        # /api/notifications routes
│       └── contactRequests.js      # /api/contact-requests routes
├── uploads/                        # Uploaded images directory
├── .env                            # Environment variables
├── package.json                    # Dependencies
├── API_DOCUMENTATION.md            # Detailed API docs
└── CHAT_API_DOCS.md                # Chat API docs
```

---

## 4. Features

### 4.1 Authentication & Authorization
- User registration with phone number
- Login with phone & password
- JWT-based authentication
- OTP support for password reset
- Profile management

### 4.2 Donor Management
- View all available donors
- Search donors by city
- Search donors by blood group
- Search by both city & blood group
- View donor profile

### 4.3 Blood Request System
- Create blood request (with/without image)
- Set urgency level (low/medium/high/critical)
- View all active requests
- Search requests by city/bloodgroup/urgency
- Update request status (active/fulfilled/cancelled)
- Delete own requests

### 4.4 Donation Tracking
- Log new donations
- View donation history
- Search donations
- Update donation status

### 4.5 Chat System
- Send messages to other users
- View conversation list
- View message history with specific user
- Mark messages as read
- Delete conversations
- Report inappropriate messages

### 4.6 Admin Panel
- Dashboard with statistics
- Analytics data
- User management (view/block/delete)
- Request management
- Donation management
- Contact request management
- Reported chat management
- Broadcast notifications (all/city/bloodgroup)

### 4.7 Notifications
- Personal notifications
- Broadcast alerts
- Notification read status

---

## 5. Database Schema

### 5.1 User Model
```javascript
{
  name: String,           // Required
  phone: String,          // Required, unique, 10 digits
  password: String,       // Required, min 6 chars
  city: String,           // Required
  bloodGroup: String,     // Required, enum: A+/A-/B+/B-/AB+/AB-/O+/O-
  isDonor: Boolean,       // Default: true
  lastDonation: Date,
  bio: String,            // Max 500 chars
  isAvailable: Boolean,   // Default: true
  role: String,           // enum: user/admin, default: user
  isBlocked: Boolean      // Default: false
}
```

### 5.2 Request Model
```javascript
{
  user: ObjectId,         // Ref: User, required
  bloodGroup: String,    // Required
  city: String,          // Required
  message: String,       // Required, max 1000 chars
  image: String,         // Image URL
  urgency: String,       // enum: low/medium/high/critical, default: medium
  status: String,       // enum: active/fulfilled/cancelled, default: active
  responses: [{
    user: ObjectId,
    message: String,
    createdAt: Date
  }]
}
```

### 5.3 Donation Model
```javascript
{
  donor: ObjectId,       // Ref: User
  bloodGroup: String,
  units: Number,
  hospital: String,
  city: String,
  date: Date,
  status: String,       // pending/completed/cancelled
  notes: String
}
```

### 5.4 Chat Model
```javascript
{
  sender: ObjectId,     // Ref: User
  receiver: ObjectId,   // Ref: User
  message: String,
  isRead: Boolean,
  isReported: Boolean,
  createdAt: Date
}
```

### 5.5 Notification Model
```javascript
{
  user: ObjectId,       // Ref: User
  title: String,
  message: String,
  type: String,         // individual/broadcast
  isRead: Boolean,
  createdAt: Date
}
```

### 5.6 ContactRequest Model
```javascript
{
  fromUser: ObjectId,   // Ref: User
  toUser: ObjectId,     // Ref: User
  message: String,
  status: String,       // pending/accepted/rejected
  createdAt: Date
}
```

---

## 6. API Endpoints

### 6.1 Authentication Routes (`/api/auth`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/register` | Register new user | No |
| POST | `/login` | Login user | No |
| POST | `/send-otp` | Send OTP | No |
| POST | `/verify-otp` | Verify OTP | No |
| POST | `/forgot-password` | Forgot password | No |
| POST | `/reset-password` | Reset password | No |
| GET | `/profile` | Get user profile | Yes |
| PUT | `/profile` | Update profile | Yes |

### 6.2 Donor Routes (`/api/donors`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/` | Get all donors | No |
| GET | `/search` | Search donors | No |
| GET | `/:id` | Get donor by ID | No |

### 6.3 Request Routes (`/api/requests`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/` | Get all requests | No |
| GET | `/search` | Search requests | No |
| GET | `/:id` | Get request by ID | No |
| POST | `/` | Create request | Yes |
| PUT | `/:id/status` | Update status | Yes |
| DELETE | `/:id` | Delete request | Yes |

### 6.4 Donation Routes (`/api/donations`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/` | Get all donations | No |
| GET | `/search` | Search donations | No |
| GET | `/:id` | Get donation by ID | No |
| POST | `/` | Create donation | Yes |
| PUT | `/:id/status` | Update status | Yes |
| DELETE | `/:id` | Delete donation | Yes |

### 6.5 Chat Routes (`/api/chat`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/send` | Send message | Yes |
| GET | `/conversations` | Get all conversations | Yes |
| GET | `/messages/:userId` | Get messages | Yes |
| POST | `/mark-read` | Mark as read | Yes |
| DELETE | `/conversation/:userId` | Delete conversation | Yes |
| PATCH | `/:id/report` | Report message | Yes |

### 6.6 Admin Routes (`/api/admin`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/dashboard` | Get dashboard stats | Yes (Admin) |
| GET | `/analytics` | Get analytics | Yes (Admin) |
| GET | `/users` | Get all users | Yes (Admin) |
| GET | `/users/:id` | Get user by ID | Yes (Admin) |
| PATCH | `/users/:id/toggle-block` | Toggle block user | Yes (Admin) |
| DELETE | `/users/:id` | Delete user | Yes (Admin) |
| GET | `/requests` | Get all requests | Yes (Admin) |
| PATCH | `/requests/:id/status` | Update status | Yes (Admin) |
| DELETE | `/requests/:id` | Delete request | Yes (Admin) |
| GET | `/donations` | Get all donations | Yes (Admin) |
| DELETE | `/donations/:id` | Delete donation | Yes (Admin) |
| GET | `/contact-requests` | Get contact requests | Yes (Admin) |
| GET | `/chats/reported` | Get reported chats | Yes (Admin) |
| PATCH | `/chats/:id/clear-report` | Clear report | Yes (Admin) |
| POST | `/broadcast/all` | Broadcast to all | Yes (Admin) |
| POST | `/broadcast/city` | Broadcast to city | Yes (Admin) |
| POST | `/broadcast/blood-group` | Broadcast to blood group | Yes (Admin) |

### 6.7 Notification Routes (`/api/notifications`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/` | Get notifications | Yes |
| PATCH | `/:id/read` | Mark as read | Yes |
| DELETE | `/:id` | Delete notification | Yes |

---

## 7. Middleware

### 7.1 Auth Middleware (`src/middleware/auth.js`)
- Verifies JWT token from Authorization header
- Extracts user ID from token
- Returns 401 if token is invalid or missing

### 7.2 Admin Middleware (`src/middleware/adminMiddleware.js`)
- Checks if user has admin role
- Returns 403 if not authorized

---

## 8. Environment Variables

`.env` file mein yeh variables honi chahiye:

```env
PORT=3000
MONGODB_URI=mongodb://localhost:27017/bloodbank
JWT_SECRET=your_jwt_secret_key_here
```

---

## 9. How to Run

### Prerequisites:
- Node.js installed (v14+)
- MongoDB installed and running

### Installation:

```bash
# Navigate to backend directory
cd Archive/backend

# Install dependencies
npm install

# Start the server
npm start
```

### Development Mode:
```bash
npm run dev
```

### Server URLs:
- Local: `http://localhost:3000`
- Network: `http://192.168.59.4:3000`

---

## 10. Future Enhancements

1. **Real-time Chat** - Socket.io integration for real-time messaging
2. **Push Notifications** - Firebase Cloud Messaging
3. **Email Verification** - OTP via email
4. **Blood Stock Management** - Track blood inventory
5. **Analytics Dashboard** - Charts and graphs for admin
6. **Mobile App Integration** - React Native/Flutter apps
7. **Payment Integration** - For premium features
8. **Location Services** - GPS-based donor matching

---

## 📝 Notes

- All timestamps are in UTC format
- JWT token validity: 30 days
- Image upload: Max 5MB, formats: JPEG, JPG, PNG
- Phone number format: 10 digits (India)
- All passwords are hashed using bcrypt

---

**Last Updated:** April 2026  
**Version:** 1.0.0  
**Author:** Abhay Kumar