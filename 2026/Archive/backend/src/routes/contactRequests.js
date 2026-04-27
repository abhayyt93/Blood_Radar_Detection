const express = require('express');
const router = express.Router();
const contactRequestController = require('../controllers/contactRequestController');
const auth = require('../middleware/auth');

// All routes are protected (authentication required)
router.post('/', auth, contactRequestController.sendRequest);
router.get('/received', auth, contactRequestController.getReceivedRequests);
router.put('/:id', auth, contactRequestController.respondToRequest);
router.get('/status/:itemId', auth, contactRequestController.getRequestStatus);

module.exports = router;
