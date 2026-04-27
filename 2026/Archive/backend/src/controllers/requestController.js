const Request = require('../models/Request');
const multer = require('multer');
const path = require('path');

// Configure multer for image upload
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, 'uploads/');
  },
  filename: (req, file, cb) => {
    cb(null, Date.now() + path.extname(file.originalname));
  }
});

const upload = multer({
  storage: storage,
  limits: { fileSize: 5 * 1024 * 1024 }, // 5MB limit
  fileFilter: (req, file, cb) => {
    const filetypes = /jpeg|jpg|png/;
    const mimetype = filetypes.test(file.mimetype);
    const extname = filetypes.test(path.extname(file.originalname).toLowerCase());

    if (mimetype && extname) {
      return cb(null, true);
    }
    cb(new Error('Only image files are allowed!'));
  }
}).single('image');

// Create new request
exports.createRequest = async (req, res) => {
  console.log('\n========== CREATE REQUEST START ==========');
  console.log('📥 Received request to create blood request');
  console.log('User ID from token:', req.userId);

  upload(req, res, async (err) => {
    if (err) {
      console.log('❌ Upload error:', err.message);
      return res.status(400).json({
        success: false,
        message: err.message
      });
    }

    try {
      console.log('\n📋 Request Body:', req.body);
      console.log('📁 Uploaded File:', req.file ? req.file.filename : 'No file uploaded');

      const { bloodGroup, city, message, urgency } = req.body;

      console.log('\n🩸 Extracted Data:');
      console.log('  - Blood Group:', bloodGroup);
      console.log('  - City:', city);
      console.log('  - Message:', message);
      console.log('  - Urgency:', urgency || 'medium');
      console.log('  - User ID:', req.userId);

      const requestData = {
        user: req.userId,
        bloodGroup,
        city,
        message,
        urgency: urgency || 'medium',
        image: req.file ? `/uploads/${req.file.filename}` : null
      };

      console.log('\n💾 Data to be saved in DB:', JSON.stringify(requestData, null, 2));

      const request = new Request(requestData);

      console.log('\n⏳ Saving to database...');
      const savedRequest = await request.save();

      console.log('✅ Request saved successfully!');
      console.log('📝 Saved Request ID:', savedRequest._id);

      await savedRequest.populate('user', '-password');

      console.log('✅ Request populated with user data');
      console.log('👤 User Name:', savedRequest.user.name);
      console.log('📱 User Phone:', savedRequest.user.phone);

      console.log('\n🎉 Sending success response to client');
      console.log('========== CREATE REQUEST END ==========\n');

      res.status(201).json({
        success: true,
        message: 'Request created successfully',
        data: savedRequest
      });
    } catch (error) {
      console.log('\n❌ ERROR in createRequest:');
      console.log('Error message:', error.message);
      console.log('Error stack:', error.stack);
      console.log('========== CREATE REQUEST ERROR ==========\n');

      res.status(500).json({
        success: false,
        message: error.message
      });
    }
  });
};

// Get all active requests
exports.getAllRequests = async (req, res) => {
  console.log('\n========== GET ALL REQUESTS ==========');
  console.log('📥 Request received! IP:', req.ip, '| Method:', req.method);
  console.log('🕐 Time:', new Date().toLocaleString());

  try {
    console.log('🔌 Step 1: Connecting to MongoDB...');
    const mongoose = require('mongoose');
    console.log('📡 MongoDB State:', mongoose.connection.readyState, '(1=connected, 0=disconnected)');

    console.log('🔍 Step 2: Running Request.find()...');
    const requests = await Request.find({ status: 'active' })
      .populate('user', '-password')
      .sort({ createdAt: -1 });

    console.log('✅ Step 3: Query complete! Total requests found:', requests.length);

    // ✅ FIX: Filter out requests with null user (whose accounts have been deleted)
    const validRequests = requests.filter(r => r.user !== null);
    const skipped = requests.length - validRequests.length;
    if (skipped > 0) {
      console.log(`⚠️  ${skipped} request(s) skipped — their user accounts have been deleted`);
    }

    if (validRequests.length > 0) {
      console.log('\n📋 Requests Summary:');
      validRequests.forEach((r, index) => {
        console.log(`  ${index + 1}. ${r.bloodGroup} - ${r.city} by ${r.user.name} (${r.urgency})`);
      });
    } else {
      console.log('⚠️  No valid active requests found in the database');
    }

    console.log('\n📤 Step 4: Sending response to client...');
    console.log('========== GET ALL REQUESTS END ==========\n');

    res.json({
      success: true,
      count: validRequests.length,
      data: validRequests
    });
  } catch (error) {
    console.log('\n🔴🔴🔴 ERROR CAUGHT! 🔴🔴🔴');
    console.log('❌ Error Type:', error.name);
    console.log('❌ Error Message:', error.message);
    console.log('❌ Full Stack:\n', error.stack);
    console.log('========== GET ALL REQUESTS ERROR ==========\n');

    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};

// Search requests
exports.searchRequests = async (req, res) => {
  try {
    const { city, bloodGroup, urgency } = req.query;

    const query = { status: 'active' };

    if (city) {
      query.city = new RegExp(city, 'i');
    }

    if (bloodGroup) {
      query.bloodGroup = bloodGroup;
    }

    if (urgency) {
      query.urgency = urgency;
    }

    const requests = await Request.find(query)
      .populate('user', '-password')
      .sort({ urgency: -1, createdAt: -1 });

    res.json({
      success: true,
      count: requests.length,
      data: requests
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};

// Get request by ID
exports.getRequestById = async (req, res) => {
  try {
    const request = await Request.findById(req.params.id)
      .populate('user', '-password')
      .populate('responses.user', '-password');

    if (!request) {
      return res.status(404).json({
        success: false,
        message: 'Request not found'
      });
    }

    res.json({
      success: true,
      data: request
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};

// Update request status
exports.updateRequestStatus = async (req, res) => {
  try {
    const { status } = req.body;

    const request = await Request.findById(req.params.id);

    if (!request) {
      return res.status(404).json({
        success: false,
        message: 'Request not found'
      });
    }

    // Check if user owns this request
    if (request.user.toString() !== req.userId) {
      return res.status(403).json({
        success: false,
        message: 'Not authorized to update this request'
      });
    }

    request.status = status;
    await request.save();

    res.json({
      success: true,
      message: 'Request status updated',
      data: request
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};

// Delete request
exports.deleteRequest = async (req, res) => {
  try {
    const request = await Request.findById(req.params.id);

    if (!request) {
      return res.status(404).json({
        success: false,
        message: 'Request not found'
      });
    }

    // Check if user owns this request
    if (request.user.toString() !== req.userId) {
      return res.status(403).json({
        success: false,
        message: 'Not authorized to delete this request'
      });
    }

    await request.deleteOne();

    res.json({
      success: true,
      message: 'Request deleted successfully'
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};
