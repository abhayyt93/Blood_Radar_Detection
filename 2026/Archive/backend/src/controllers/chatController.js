const Chat = require('../models/Chat');
const User = require('../models/User');

// Send a message
exports.sendMessage = async (req, res) => {
  try {
    const { receiverId, message } = req.body;
    const senderId = req.userId;

    if (!receiverId || !message) {
      return res.status(400).json({ 
        success: false, 
        message: 'Receiver ID and message are required' 
      });
    }

    // Check if receiver exists
    const receiver = await User.findById(receiverId);
    if (!receiver) {
      return res.status(404).json({ 
        success: false, 
        message: 'Receiver not found' 
      });
    }

    // Generate conversation ID
    const conversationId = Chat.getConversationId(senderId, receiverId);

    // Create new message
    const chat = new Chat({
      conversationId,
      senderId,
      receiverId,
      message
    });

    await chat.save();

    // Populate sender and receiver info
    await chat.populate('senderId', 'name phone bloodGroup');
    await chat.populate('receiverId', 'name phone bloodGroup');

    res.status(201).json({
      success: true,
      message: 'Message sent successfully',
      data: chat
    });
  } catch (error) {
    console.error('Send message error:', error);
    res.status(500).json({ 
      success: false, 
      message: 'Failed to send message',
      error: error.message 
    });
  }
};

// Get all conversations for a user
exports.getConversations = async (req, res) => {
  try {
    const userId = req.userId;

    // Get all unique conversations
    const messages = await Chat.find({
      $or: [{ senderId: userId }, { receiverId: userId }]
    })
    .sort({ timestamp: -1 })
    .populate('senderId', 'name phone bloodGroup city')
    .populate('receiverId', 'name phone bloodGroup city');

    // Group by conversation and get latest message
    const conversationsMap = new Map();

    messages.forEach(msg => {
      // Agar sender ya receiver delete ho gaya toh skip karo (null check)
      if (!msg.senderId || !msg.receiverId) return;

      const senderIdStr = msg.senderId._id.toString();
      const receiverIdStr = msg.receiverId._id.toString();
      const otherUserId = senderIdStr === userId ? receiverIdStr : senderIdStr;

      if (!conversationsMap.has(otherUserId)) {
        conversationsMap.set(otherUserId, {
          conversationId: msg.conversationId,
          otherUser: senderIdStr === userId ? msg.receiverId : msg.senderId,
          lastMessage: msg.message,
          timestamp: msg.timestamp,
          isRead: msg.isRead,
          unreadCount: 0
        });
      }

      // Count unread messages
      if (!msg.isRead && receiverIdStr === userId) {
        conversationsMap.get(otherUserId).unreadCount++;
      }
    });

    const conversations = Array.from(conversationsMap.values());

    res.json({
      success: true,
      data: conversations
    });
  } catch (error) {
    console.error('Get conversations error:', error);
    res.status(500).json({ 
      success: false, 
      message: 'Failed to fetch conversations',
      error: error.message 
    });
  }
};

// Get messages with specific user
exports.getMessages = async (req, res) => {
  try {
    const { userId } = req.params;
    const currentUserId = req.userId;

    // Generate conversation ID
    const conversationId = Chat.getConversationId(currentUserId, userId);

    // Get all messages in conversation
    const messages = await Chat.find({ conversationId })
      .sort({ timestamp: 1 })
      .populate('senderId', 'name phone bloodGroup')
      .populate('receiverId', 'name phone bloodGroup');

    // Mark messages as read
    await Chat.updateMany(
      { 
        conversationId, 
        receiverId: currentUserId, 
        isRead: false 
      },
      { isRead: true }
    );

    res.json({
      success: true,
      data: messages
    });
  } catch (error) {
    console.error('Get messages error:', error);
    res.status(500).json({ 
      success: false, 
      message: 'Failed to fetch messages',
      error: error.message 
    });
  }
};

// Mark messages as read
exports.markAsRead = async (req, res) => {
  try {
    const { userId } = req.body;
    const currentUserId = req.userId;

    const conversationId = Chat.getConversationId(currentUserId, userId);

    await Chat.updateMany(
      { 
        conversationId, 
        receiverId: currentUserId, 
        isRead: false 
      },
      { isRead: true }
    );

    res.json({
      success: true,
      message: 'Messages marked as read'
    });
  } catch (error) {
    console.error('Mark as read error:', error);
    res.status(500).json({ 
      success: false, 
      message: 'Failed to mark messages as read',
      error: error.message 
    });
  }
};

// Delete conversation
exports.deleteConversation = async (req, res) => {
  try {
    const { userId } = req.params;
    const currentUserId = req.userId;

    const conversationId = Chat.getConversationId(currentUserId, userId);

    await Chat.deleteMany({ conversationId });

    res.json({
      success: true,
      message: 'Conversation deleted successfully'
    });
  } catch (error) {
    console.error('Delete conversation error:', error);
    res.status(500).json({ 
      success: false, 
      message: 'Failed to delete conversation',
      error: error.message 
    });
  }
};

// Report a message
exports.reportMessage = async (req, res) => {
  try {
    const { reportReason } = req.body;

    const chat = await Chat.findByIdAndUpdate(
      req.params.id,
      { 
        isReported: true, 
        reportReason: reportReason || 'Reported by user' 
      },
      { new: true }
    );

    if (!chat) {
      return res.status(404).json({ success: false, message: 'Message not found' });
    }

    res.json({ success: true, message: 'Message reported successfully' });
  } catch (error) {
    console.error('Report message error:', error);
    res.status(500).json({ success: false, message: error.message });
  }
};

