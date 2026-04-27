const User = require('../models/User');
const Request = require('../models/Request');
const Donation = require('../models/Donation');
const ContactRequest = require('../models/ContactRequest');
const Chat = require('../models/Chat');
const Notification = require('../models/Notification');

// =============================================
// 📊 1. DASHBOARD — Live Stats
// =============================================
exports.getDashboardStats = async (req, res) => {
    try {
        const [
            totalUsers,
            totalDonors,
            blockedUsers,
            activeRequests,
            criticalRequests,
            fulfilledRequests,
            activeDonations,
            pendingContactRequests,
            acceptedContactRequests,
            reportedChats,
            unreadNotifications
        ] = await Promise.all([
            User.countDocuments({ role: { $ne: 'admin' } }),
            User.countDocuments({ isDonor: true, role: { $ne: 'admin' } }),
            User.countDocuments({ isBlocked: true, role: { $ne: 'admin' } }),
            Request.countDocuments({ status: 'active' }),
            Request.countDocuments({ status: 'active', urgency: 'critical' }),
            Request.countDocuments({ status: 'fulfilled' }),
            Donation.countDocuments({ status: 'active' }),
            ContactRequest.countDocuments({ status: 'pending' }),
            ContactRequest.countDocuments({ status: 'accepted' }),
            Chat.countDocuments({ isReported: true }),
            Notification.countDocuments({ isRead: false })
        ]);

        res.json({
            success: true,
            data: {
                users: { total: totalUsers, donors: totalDonors, blocked: blockedUsers },
                requests: { active: activeRequests, critical: criticalRequests, fulfilled: fulfilledRequests },
                donations: { active: activeDonations },
                contactRequests: { pending: pendingContactRequests, accepted: acceptedContactRequests },
                chats: { reported: reportedChats },
                notifications: { unread: unreadNotifications }
            }
        });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

// =============================================
// 👥 2. USER MANAGEMENT
// =============================================

// Saare users list (filter: city, bloodGroup, role, isBlocked)
exports.getAllUsers = async (req, res) => {
    try {
        const { city, bloodGroup, isBlocked, search } = req.query;
        const query = { role: { $ne: 'admin' } }; // admin ke alawa sabhi

        if (city) query.city = new RegExp(city, 'i');
        if (bloodGroup) query.bloodGroup = bloodGroup;
        if (isBlocked !== undefined) query.isBlocked = isBlocked === 'true';
        if (search) {
            query.$or = [
                { name: new RegExp(search, 'i') },
                { phone: new RegExp(search, 'i') }
            ];
        }

        const users = await User.find(query)
            .select('-password')
            .sort({ createdAt: -1 });

        res.json({ success: true, count: users.length, data: users });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

// Specific user detail
exports.getUserById = async (req, res) => {
    try {
        const user = await User.findById(req.params.id).select('-password');
        if (!user) return res.status(404).json({ success: false, message: 'User not found' });
        res.json({ success: true, data: user });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

// Block / Unblock user
exports.toggleBlockUser = async (req, res) => {
    try {
        const user = await User.findById(req.params.id);
        if (!user) return res.status(404).json({ success: false, message: 'User not found' });
        if (user.role === 'admin') {
            return res.status(400).json({ success: false, message: 'Cannot block an admin' });
        }

        user.isBlocked = !user.isBlocked;
        await user.save();

        res.json({
            success: true,
            message: `User ${user.isBlocked ? 'blocked' : 'unblocked'} successfully`,
            data: { isBlocked: user.isBlocked }
        });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

// Delete user account
exports.deleteUser = async (req, res) => {
    try {
        const user = await User.findById(req.params.id);
        if (!user) return res.status(404).json({ success: false, message: 'User not found' });
        if (user.role === 'admin') {
            return res.status(400).json({ success: false, message: 'Cannot delete an admin account' });
        }

        await user.deleteOne();
        res.json({ success: true, message: 'User deleted successfully' });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

// =============================================
// 🩸 3. BLOOD REQUEST MANAGEMENT
// =============================================

// All requests (any status, with filters)
exports.getAllRequests = async (req, res) => {
    try {
        const { status, urgency, city, bloodGroup } = req.query;
        const query = {};

        if (status) query.status = status;
        if (urgency) query.urgency = urgency;
        if (city) query.city = new RegExp(city, 'i');
        if (bloodGroup) query.bloodGroup = bloodGroup;

        const requests = await Request.find(query)
            .populate('user', 'name phone city bloodGroup')
            .sort({ urgency: -1, createdAt: -1 });

        res.json({ success: true, count: requests.length, data: requests });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

// Admin delete any request (spam/fake)
exports.adminDeleteRequest = async (req, res) => {
    try {
        const request = await Request.findByIdAndDelete(req.params.id);
        if (!request) return res.status(404).json({ success: false, message: 'Request not found' });
        res.json({ success: true, message: 'Request deleted by admin' });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

// Admin update any request status (e.g., mark fulfilled)
exports.adminUpdateRequestStatus = async (req, res) => {
    try {
        const { status } = req.body;
        const request = await Request.findByIdAndUpdate(
            req.params.id,
            { status },
            { new: true, runValidators: true }
        );
        if (!request) return res.status(404).json({ success: false, message: 'Request not found' });
        res.json({ success: true, message: 'Request status updated', data: request });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

// =============================================
// 🏥 4. DONATION POST MANAGEMENT
// =============================================

// All donations (any status, with filters)
exports.getAllDonations = async (req, res) => {
    try {
        const { status, city, bloodGroup } = req.query;
        const query = {};

        if (status) query.status = status;
        if (city) query.city = new RegExp(city, 'i');
        if (bloodGroup) query.bloodGroup = bloodGroup;

        const donations = await Donation.find(query)
            .populate('user', 'name phone city bloodGroup')
            .sort({ createdAt: -1 });

        res.json({ success: true, count: donations.length, data: donations });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

// Admin delete any donation (spam/fake)
exports.adminDeleteDonation = async (req, res) => {
    try {
        const donation = await Donation.findByIdAndDelete(req.params.id);
        if (!donation) return res.status(404).json({ success: false, message: 'Donation not found' });
        res.json({ success: true, message: 'Donation deleted by admin' });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

// =============================================
// 🤝 5. CONTACT REQUEST MANAGEMENT
// =============================================

exports.getAllContactRequests = async (req, res) => {
    try {
        const { status } = req.query;
        const query = status ? { status } : {};

        const contactRequests = await ContactRequest.find(query)
            .populate('requester', 'name phone city bloodGroup')
            .populate('donor', 'name phone')
            .populate('donation', 'bloodGroup city message')
            .sort({ createdAt: -1 });

        res.json({ success: true, count: contactRequests.length, data: contactRequests });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

// =============================================
// 💬 6. REPORTED CHAT MANAGEMENT
// =============================================

exports.getReportedChats = async (req, res) => {
    try {
        const reportedChats = await Chat.find({ isReported: true })
            .populate('senderId', 'name phone')
            .populate('receiverId', 'name phone')
            .sort({ createdAt: -1 });

        res.json({ success: true, count: reportedChats.length, data: reportedChats });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

// Mark chat as reviewed (clear report)
exports.clearChatReport = async (req, res) => {
    try {
        const chat = await Chat.findByIdAndUpdate(
            req.params.id,
            { isReported: false, reportReason: null },
            { new: true }
        );
        if (!chat) return res.status(404).json({ success: false, message: 'Chat not found' });
        res.json({ success: true, message: 'Chat report cleared', data: chat });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

// =============================================
// 📢 7. BROADCAST NOTIFICATION
// =============================================

// Send notification to ALL users
exports.broadcastToAll = async (req, res) => {
    try {
        const { title, message } = req.body;
        if (!message) return res.status(400).json({ success: false, message: 'Message is required' });

        const adminName = req.user ? 'Admin' : 'System';

        const users = await User.find({ role: { $ne: 'admin' }, isBlocked: false }).select('_id');

        if (users.length === 0) {
            return res.status(404).json({ success: false, message: 'No users found' });
        }

        const notifications = users.map(u => ({
            recipient: u._id,
            sender: req.userId || null,
            type: 'broadcast',
            title: title || '📢 Admin Broadcast',
            message: `📢 ${adminName}: ${message}`,
            isBroadcast: true,
            isRead: false
        }));

        await Notification.insertMany(notifications);

        res.json({
            success: true,
            message: `Broadcast sent to ${notifications.length} users`
        });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

// Send notification to specific city users
exports.broadcastToCity = async (req, res) => {
    try {
        const { city, message, title } = req.body;
        if (!city || !message) return res.status(400).json({ success: false, message: 'city and message required' });

        const users = await User.find({ city: new RegExp(city, 'i'), role: { $ne: 'admin' }, isBlocked: false }).select('_id');
        if (users.length === 0) return res.status(404).json({ success: false, message: `No users found in ${city}` });

        const fullMessage = `📍 ${city}: ${message}`;
        const notificationTitle = title || '📍 City Alert';

        const notifications = users.map(u => ({
            recipient: u._id,
            sender: req.userId || null,
            type: 'broadcast',
            title: notificationTitle,
            message: fullMessage,
            isBroadcast: true,
            isRead: false
        }));

        await Notification.insertMany(notifications);

        res.json({
            success: true,
            message: `Broadcast sent to ${notifications.length} users in ${city}`
        });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

// Send notification to specific blood group donors
exports.broadcastToBloodGroup = async (req, res) => {
    try {
        const { bloodGroup, message, title } = req.body;
        if (!bloodGroup || !message) return res.status(400).json({ success: false, message: 'bloodGroup and message required' });

        const users = await User.find({ bloodGroup, role: { $ne: 'admin' }, isBlocked: false }).select('_id');
        if (users.length === 0) return res.status(404).json({ success: false, message: `No ${bloodGroup} users found` });

        const fullMessage = `🩸 ${bloodGroup}: ${message}`;
        const notificationTitle = title || '🩸 Blood Group Alert';

        const notifications = users.map(u => ({
            recipient: u._id,
            sender: req.userId || null,
            type: 'broadcast',
            title: notificationTitle,
            message: fullMessage,
            isBroadcast: true,
            isRead: false
        }));

        await Notification.insertMany(notifications);

        res.json({
            success: true,
            message: `Broadcast sent to ${notifications.length} ${bloodGroup} users`
        });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

// =============================================
// 📈 8. ANALYTICS
// =============================================

exports.getAnalytics = async (req, res) => {
    try {
        // Blood group wise donors
        const bloodGroupStats = await User.aggregate([
            { $match: { isDonor: true, role: 'user' } },
            { $group: { _id: '$bloodGroup', count: { $sum: 1 } } },
            { $sort: { count: -1 } }
        ]);

        // City wise donors
        const cityStats = await User.aggregate([
            { $match: { isDonor: true, role: 'user' } },
            { $group: { _id: '$city', count: { $sum: 1 } } },
            { $sort: { count: -1 } },
            { $limit: 10 }
        ]);

        // Request urgency distribution
        const urgencyStats = await Request.aggregate([
            { $match: { status: 'active' } },
            { $group: { _id: '$urgency', count: { $sum: 1 } } }
        ]);

        // Contact request acceptance rate
        const totalContactReqs = await ContactRequest.countDocuments();
        const acceptedContactReqs = await ContactRequest.countDocuments({ status: 'accepted' });
        const acceptanceRate = totalContactReqs > 0
            ? ((acceptedContactReqs / totalContactReqs) * 100).toFixed(1)
            : 0;

        res.json({
            success: true,
            data: {
                bloodGroupStats,
                cityStats,
                urgencyStats,
                contactRequestAcceptanceRate: `${acceptanceRate}%`
            }
        });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};
