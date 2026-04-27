const Notification = require('../models/Notification');

// Get all notifications for the logged-in user
exports.getUserNotifications = async (req, res) => {
    try {
        const notifications = await Notification.find({ 
            recipient: req.userId,
            isDeleted: false 
        })
        .sort({ createdAt: -1 });

        res.json({
            success: true,
            count: notifications.length,
            data: notifications
        });
    } catch (error) {
        console.error('Get notifications error:', error.message);
        res.status(500).json({ success: false, message: error.message });
    }
};

// Mark a notification as read
exports.markAsRead = async (req, res) => {
    try {
        const { id } = req.params;

        const notification = await Notification.findById(id);
        if (!notification) {
            return res.status(404).json({ success: false, message: 'Notification not found' });
        }

        if (notification.recipient.toString() !== req.userId) {
            return res.status(403).json({ success: false, message: 'Unauthorized' });
        }

        notification.isRead = true;
        await notification.save();

        res.json({ success: true, data: notification });
    } catch (error) {
        console.error('Mark notification read error:', error.message);
        res.status(500).json({ success: false, message: error.message });
    }
};

// Delete a notification (soft delete for broadcast notifications)
exports.deleteNotification = async (req, res) => {
    try {
        const { id } = req.params;

        const notification = await Notification.findById(id);
        if (!notification) {
            return res.status(404).json({ success: false, message: 'Notification not found' });
        }

        if (notification.recipient.toString() !== req.userId) {
            return res.status(403).json({ success: false, message: 'Unauthorized' });
        }

        if (notification.isBroadcast) {
            notification.isDeleted = true;
            await notification.save();
        } else {
            await notification.deleteOne();
        }

        res.json({ success: true, message: 'Notification deleted' });
    } catch (error) {
        console.error('Delete notification error:', error.message);
        res.status(500).json({ success: false, message: error.message });
    }
};

// Delete all broadcast notifications
exports.deleteAllBroadcasts = async (req, res) => {
    try {
        await Notification.updateMany(
            { recipient: req.userId, isBroadcast: true },
            { isDeleted: true }
        );

        res.json({ success: true, message: 'All broadcast notifications deleted' });
    } catch (error) {
        console.error('Delete all broadcasts error:', error.message);
        res.status(500).json({ success: false, message: error.message });
    }
};

// Mark a notification as read
exports.markAsRead = async (req, res) => {
    try {
        const { id } = req.params;

        const notification = await Notification.findById(id);
        if (!notification) {
            return res.status(404).json({ success: false, message: 'Notification not found' });
        }

        // Ensure the notification belongs to the user
        if (notification.recipient.toString() !== req.userId) {
            return res.status(403).json({ success: false, message: 'Unauthorized' });
        }

        notification.isRead = true;
        await notification.save();

        res.json({ success: true, data: notification });
    } catch (error) {
        console.error('Mark notification read error:', error.message);
        res.status(500).json({ success: false, message: error.message });
    }
};

// Delete a notification
exports.deleteNotification = async (req, res) => {
    try {
        const { id } = req.params;

        const notification = await Notification.findById(id);
        if (!notification) {
            return res.status(404).json({ success: false, message: 'Notification not found' });
        }

        if (notification.recipient.toString() !== req.userId) {
            return res.status(403).json({ success: false, message: 'Unauthorized' });
        }

        await notification.deleteOne();

        res.json({ success: true, message: 'Notification deleted' });
    } catch (error) {
        console.error('Delete notification error:', error.message);
        res.status(500).json({ success: false, message: error.message });
    }
};
