const ContactRequest = require('../models/ContactRequest');
const Donation = require('../models/Donation');
const Request = require('../models/Request');
const User = require('../models/User');
const Notification = require('../models/Notification');

// POST /api/contact-requests — Send a contact/number request to the donor/requester
exports.sendRequest = async (req, res) => {
    try {
        const requesterId = req.userId;
        const { donationId, requestId } = req.body;

        console.log('\n📨 CONTACT REQUEST RECEIVED');
        console.log('donationId:', donationId);
        console.log('requestId:', requestId);
        console.log('requesterId:', requesterId);

        // Either donationId or requestId required
        if (!donationId && !requestId) {
            console.log('❌ Error: Neither donationId nor requestId provided');
            return res.status(400).json({ success: false, message: 'donationId or requestId required' });
        }

        let targetUserId, targetItem, targetType;

        if (donationId) {
            // Fetch donation and extract donor ID
            console.log('📋 Processing DONATION request');
            targetItem = await Donation.findById(donationId);
            if (!targetItem) {
                console.log('❌ Donation not found:', donationId);
                return res.status(404).json({ success: false, message: 'Donation not found' });
            }
            targetUserId = targetItem.user;
            targetType = 'donation';
        } else {
            // Fetch request and extract requester ID
            console.log('📋 Processing BLOOD REQUEST');
            targetItem = await Request.findById(requestId);
            if (!targetItem) {
                console.log('❌ Request not found:', requestId);
                return res.status(404).json({ success: false, message: 'Request not found' });
            }
            targetUserId = targetItem.user;
            targetType = 'request';
        }

        console.log('✅ Target user:', targetUserId);
        console.log('✅ Type:', targetType);

        // Cannot request your own item
        if (targetUserId.toString() === requesterId.toString()) {
            return res.status(400).json({ success: false, message: 'You cannot request your own item.' });
        }

        // If a request already exists, return it
        const query = { requester: requesterId };
        if (donationId) query.donation = donationId;
        if (requestId) query.request = requestId;

        const existing = await ContactRequest.findOne(query);
        if (existing) {
            console.log('⚠️ Request already exists');
            return res.json({ success: true, data: existing, message: 'Request already sent' });
        }

        const contactRequest = await ContactRequest.create({
            requester: requesterId,
            donor: targetUserId,
            donation: donationId || null,
            request: requestId || null
        });

        console.log(`📨 Contact request sent: ${requesterId} → ${targetType} owner ${targetUserId}`);

        // Create notification
        const notificationMessage = targetType === 'donation' 
            ? 'Someone requested your blood donation'
            : 'Someone requested your contact for blood request';

        await Notification.create({
            recipient: targetUserId,
            sender: requesterId,
            type: 'contact_request',
            message: notificationMessage,
            relatedId: contactRequest._id
        });

        console.log('✅ Contact request created successfully');
        res.status(201).json({ success: true, data: contactRequest });
    } catch (error) {
        console.error('❌ sendRequest error:', error.message);
        console.error('Stack:', error.stack);
        res.status(500).json({ success: false, message: error.message });
    }
};

// GET /api/contact-requests/received — Get incoming requests for the donor
exports.getReceivedRequests = async (req, res) => {
    try {
        const donorId = req.userId;

        const requests = await ContactRequest.find({ donor: donorId })
            .populate('requester', 'name bloodGroup city')
            .populate('donation', 'bloodGroup city message')
            .sort({ createdAt: -1 });

        res.json({ success: true, count: requests.length, data: requests });
    } catch (error) {
        console.error('❌ getReceivedRequests error:', error.message);
        res.status(500).json({ success: false, message: error.message });
    }
};

// PUT /api/contact-requests/:id — Accept or reject a contact request
exports.respondToRequest = async (req, res) => {
    try {
        const donorId = req.userId;
        const { id } = req.params;
        const { action } = req.body; // 'accepted' or 'rejected'

        if (!['accepted', 'rejected'].includes(action)) {
            return res.status(400).json({ success: false, message: "Action must be 'accepted' or 'rejected'" });
        }

        const request = await ContactRequest.findById(id);
        if (!request) {
            return res.status(404).json({ success: false, message: 'Request not found' });
        }

        // Only the donor can respond to this request
        if (request.donor.toString() !== donorId.toString()) {
            return res.status(403).json({ success: false, message: 'Unauthorized' });
        }

        request.status = action;
        await request.save();

        console.log(`✅ Contact request ${action}: ID ${id}`);

        // Create notification for requester
        await Notification.create({
            recipient: request.requester,
            sender: donorId,
            type: action === 'accepted' ? 'request_accepted' : 'request_rejected',
            message: `Your contact request was ${action}`,
            relatedId: request._id
        });

        res.json({ success: true, data: request });
    } catch (error) {
        console.error('❌ respondToRequest error:', error.message);
        res.status(500).json({ success: false, message: error.message });
    }
};

// GET /api/contact-requests/status/:itemId — Check the status of a contact request (works for both donation and request)
exports.getRequestStatus = async (req, res) => {
    try {
        const requesterId = req.userId;
        const { itemId } = req.params; // Can be donationId or requestId

        // Try to find in both donation and request
        let request = await ContactRequest.findOne({ 
            requester: requesterId, 
            $or: [
                { donation: itemId },
                { request: itemId }
            ]
        });

        if (!request) {
            return res.json({ success: true, data: { status: 'none' } });
        }

        let phone = null;
        // If accepted, also include the phone number
        if (request.status === 'accepted') {
            const donor = await User.findById(request.donor).select('phone');
            phone = donor ? donor.phone : null;
        }

        res.json({ success: true, data: { status: request.status, requestId: request._id, phone } });
    } catch (error) {
        console.error('❌ getRequestStatus error:', error.message);
        res.status(500).json({ success: false, message: error.message });
    }
};
