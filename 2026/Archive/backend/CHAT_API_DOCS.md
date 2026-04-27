# Chat API Documentation

## Endpoints

### 1. Send Message
```
POST /api/chat/send
Authorization: Bearer <token>

Body:
{
  "receiverId": "user_id",
  "message": "Hello"
}

Response:
{
  "success": true,
  "message": "Message sent successfully",
  "data": { chatMessage }
}
```

### 2. Get All Conversations
```
GET /api/chat/conversations
Authorization: Bearer <token>

Response:
{
  "success": true,
  "data": [
    {
      "conversationId": "userId1_userId2",
      "otherUser": { name, phone, bloodGroup, city },
      "lastMessage": "Last message text",
      "timestamp": "2024-01-01T10:00:00",
      "unreadCount": 3
    }
  ]
}
```

### 3. Get Messages with User
```
GET /api/chat/messages/:userId
Authorization: Bearer <token>

Response:
{
  "success": true,
  "data": [ array of messages ]
}
```

### 4. Mark as Read
```
POST /api/chat/mark-read
Authorization: Bearer <token>

Body:
{
  "userId": "user_id"
}
```

### 5. Delete Conversation
```
DELETE /api/chat/conversation/:userId
Authorization: Bearer <token>
```

## Features Implemented:
✅ Real-time message sending
✅ Auto-refresh messages (5 seconds)
✅ Unread message count
✅ Beautiful message bubbles (sent/received)
✅ Conversation list
✅ Mark as read functionality
✅ User info in chat header
