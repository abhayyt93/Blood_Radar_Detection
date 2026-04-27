const express = require('express');
const router = express.Router();
const chatController = require('../controllers/chatController');
const auth = require('../middleware/auth');

// All routes require authentication
router.use(auth);

// Send message
router.post('/send', chatController.sendMessage);

// Get all conversations
router.get('/conversations', chatController.getConversations);

// Get messages with specific user
router.get('/messages/:userId', chatController.getMessages);

// Mark messages as read
router.post('/mark-read', chatController.markAsRead);

// Delete conversation
router.delete('/conversation/:userId', chatController.deleteConversation);

// Report a message
router.patch('/:id/report', chatController.reportMessage);

module.exports = router;
