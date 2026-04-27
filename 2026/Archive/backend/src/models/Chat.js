const mongoose = require('mongoose');

const chatSchema = new mongoose.Schema({
  conversationId: {
    type: String,
    required: true,
    index: true
  },
  senderId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  receiverId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  message: {
    type: String,
    required: true,
    trim: true
  },
  isRead: {
    type: Boolean,
    default: false
  },
  timestamp: {
    type: Date,
    default: Date.now
  },
  isReported: {
    type: Boolean,
    default: false
  },
  reportReason: {
    type: String
  }
}, {
  timestamps: true
});

// Index for faster queries
chatSchema.index({ conversationId: 1, timestamp: -1 });
chatSchema.index({ senderId: 1, receiverId: 1 });

// Helper method to generate conversation ID
chatSchema.statics.getConversationId = function (userId1, userId2) {
  // Sort IDs to ensure same conversation ID regardless of order
  const ids = [userId1.toString(), userId2.toString()].sort();
  return `${ids[0]}_${ids[1]}`;
};

module.exports = mongoose.model('Chat', chatSchema);
