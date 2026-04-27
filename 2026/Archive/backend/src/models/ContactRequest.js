const mongoose = require('mongoose');

const contactRequestSchema = new mongoose.Schema({
    requester: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true
    },
    donor: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true
    },
    donation: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Donation'
    },
    request: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Request'
    },
    status: {
        type: String,
        enum: ['pending', 'accepted', 'rejected'],
        default: 'pending'
    }
}, {
    timestamps: true
});

// A requester can only send one request per donation/request
contactRequestSchema.index({ requester: 1, donation: 1 }, { unique: true, sparse: true });
contactRequestSchema.index({ requester: 1, request: 1 }, { unique: true, sparse: true });

module.exports = mongoose.model('ContactRequest', contactRequestSchema);
