const express = require('express');
const router = express.Router();
const requestController = require('../controllers/requestController');
const auth = require('../middleware/auth');

// Public routes
router.get('/', requestController.getAllRequests);
router.get('/search', requestController.searchRequests);
router.get('/:id', requestController.getRequestById);

// Protected routes
router.post('/', auth, requestController.createRequest);
router.put('/:id/status', auth, requestController.updateRequestStatus);
router.delete('/:id', auth, requestController.deleteRequest);

module.exports = router;
