const express = require('express');
const router = express.Router();
const authMiddleware = require('../middleware/auth');
const adminMiddleware = require('../middleware/adminMiddleware');
const admin = require('../controllers/adminController');

// All admin routes — pehle authMiddleware, phir adminMiddleware
router.use(authMiddleware, adminMiddleware);

// 📊 Dashboard
router.get('/dashboard', admin.getDashboardStats);

// 📈 Analytics
router.get('/analytics', admin.getAnalytics);

// 👥 User Management
router.get('/users', admin.getAllUsers);
router.get('/users/:id', admin.getUserById);
router.patch('/users/:id/toggle-block', admin.toggleBlockUser);
router.delete('/users/:id', admin.deleteUser);

// 🩸 Blood Request Management
router.get('/requests', admin.getAllRequests);
router.patch('/requests/:id/status', admin.adminUpdateRequestStatus);
router.delete('/requests/:id', admin.adminDeleteRequest);

// 🏥 Donation Management
router.get('/donations', admin.getAllDonations);
router.delete('/donations/:id', admin.adminDeleteDonation);

// 🤝 Contact Request Management
router.get('/contact-requests', admin.getAllContactRequests);

// 💬 Reported Chats
router.get('/chats/reported', admin.getReportedChats);
router.patch('/chats/:id/clear-report', admin.clearChatReport);

// 📢 Broadcast Notifications
router.post('/broadcast/all', admin.broadcastToAll);
router.post('/broadcast/city', admin.broadcastToCity);
router.post('/broadcast/blood-group', admin.broadcastToBloodGroup);

module.exports = router;
