const express = require('express');
const router = express.Router();
const donorController = require('../controllers/donorController');
const auth = require('../middleware/auth');

// Get all donors
router.get('/', donorController.getAllDonors);

// Search donors
router.get('/search', donorController.searchDonors);

// Get donor by ID
router.get('/:id', donorController.getDonorById);

module.exports = router;
