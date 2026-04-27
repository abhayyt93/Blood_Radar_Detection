const express = require('express');
const router = express.Router();
const donationController = require('../controllers/donationController');
const auth = require('../middleware/auth');

// Public routes
router.get('/', donationController.getAllDonations);
router.get('/search', donationController.searchDonations);
router.get('/:id', donationController.getDonationById);

// Protected routes
router.post('/', auth, donationController.createDonation);
router.put('/:id/status', auth, donationController.updateDonationStatus);
router.delete('/:id', auth, donationController.deleteDonation);


module.exports = router;
