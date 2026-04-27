const User = require('../models/User');
const jwt = require('jsonwebtoken');
const https = require('https');

// In-memory OTP store: { phone: { otp, expiresAt } }
const otpStore = {};

// Send OTP via 2Factor.in
exports.sendOtp = async (req, res) => {
  try {
    const { phone } = req.body;
    if (!phone || phone.length !== 10) {
      return res.status(400).json({ success: false, message: 'Valid 10-digit phone required' });
    }

    const apiKey = process.env.TWOFACTOR_API_KEY;
    const url = `https://2factor.in/API/V1/${apiKey}/SMS/${phone}/AUTOGEN`;

    https.get(url, (response) => {
      let data = '';
      response.on('data', chunk => data += chunk);
      response.on('end', () => {
        const result = JSON.parse(data);
        if (result.Status === 'Success') {
          // Store session ID for verification
          otpStore[phone] = {
            sessionId: result.Details,
            expiresAt: Date.now() + 5 * 60 * 1000 // 5 minutes
          };
          res.json({ success: true, message: 'OTP sent successfully' });
        } else {
          res.status(500).json({ success: false, message: 'Failed to send OTP: ' + result.Details });
        }
      });
    }).on('error', (err) => {
      res.status(500).json({ success: false, message: 'OTP service error: ' + err.message });
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// Verify OTP via 2Factor.in
exports.verifyOtp = async (req, res) => {
  try {
    const { phone, otp } = req.body;
    if (!phone || !otp) {
      return res.status(400).json({ success: false, message: 'Phone and OTP required' });
    }

    const record = otpStore[phone];
    if (!record) {
      return res.status(400).json({ success: false, message: 'OTP not sent or expired. Please resend.' });
    }
    if (Date.now() > record.expiresAt) {
      delete otpStore[phone];
      return res.status(400).json({ success: false, message: 'OTP expired. Please resend.' });
    }

    const apiKey = process.env.TWOFACTOR_API_KEY;
    const url = `https://2factor.in/API/V1/${apiKey}/SMS/VERIFY/${record.sessionId}/${otp}`;

    https.get(url, (response) => {
      let data = '';
      response.on('data', chunk => data += chunk);
      response.on('end', () => {
        const result = JSON.parse(data);
        if (result.Status === 'Success' && result.Details === 'OTP Matched') {
          delete otpStore[phone]; // OTP has been used, delete it
          res.json({ success: true, message: 'OTP verified successfully' });
        } else {
          res.status(400).json({ success: false, message: 'Invalid OTP. Please try again.' });
        }
      });
    }).on('error', (err) => {
      res.status(500).json({ success: false, message: 'OTP verification error: ' + err.message });
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// Forgot Password - Send OTP to verify phone
exports.forgotPassword = async (req, res) => {
  try {
    const { phone } = req.body;
    if (!phone || phone.length !== 10) {
      return res.status(400).json({ success: false, message: 'Valid 10-digit phone required' });
    }

    // Check if user exists with this phone
    const user = await User.findOne({ phone });
    if (!user) {
      return res.status(404).json({ success: false, message: 'No account found with this phone number' });
    }

    const apiKey = process.env.TWOFACTOR_API_KEY;
    const url = `https://2factor.in/API/V1/${apiKey}/SMS/${phone}/AUTOGEN`;

    https.get(url, (response) => {
      let data = '';
      response.on('data', chunk => data += chunk);
      response.on('end', () => {
        const result = JSON.parse(data);
        if (result.Status === 'Success') {
          otpStore[`fp_${phone}`] = {
            sessionId: result.Details,
            expiresAt: Date.now() + 5 * 60 * 1000
          };
          res.json({ success: true, message: 'OTP sent successfully' });
        } else {
          res.status(500).json({ success: false, message: 'Failed to send OTP: ' + result.Details });
        }
      });
    }).on('error', (err) => {
      res.status(500).json({ success: false, message: 'OTP service error: ' + err.message });
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// Reset Password - Verify OTP then update password
exports.resetPassword = async (req, res) => {
  try {
    const { phone, otp, newPassword } = req.body;
    if (!phone || !otp || !newPassword) {
      return res.status(400).json({ success: false, message: 'Phone, OTP and new password are required' });
    }
    if (newPassword.length < 6) {
      return res.status(400).json({ success: false, message: 'Password must be at least 6 characters' });
    }

    const record = otpStore[`fp_${phone}`];
    if (!record) {
      return res.status(400).json({ success: false, message: 'OTP not sent or expired. Please try again.' });
    }
    if (Date.now() > record.expiresAt) {
      delete otpStore[`fp_${phone}`];
      return res.status(400).json({ success: false, message: 'OTP expired. Please request a new one.' });
    }

    const apiKey = process.env.TWOFACTOR_API_KEY;
    const url = `https://2factor.in/API/V1/${apiKey}/SMS/VERIFY/${record.sessionId}/${otp}`;

    https.get(url, async (response) => {
      let data = '';
      response.on('data', chunk => data += chunk);
      response.on('end', async () => {
        const result = JSON.parse(data);
        if (result.Status === 'Success' && result.Details === 'OTP Matched') {
          delete otpStore[`fp_${phone}`];
          // Update password in DB
          const bcrypt = require('bcryptjs');
          const hashed = await bcrypt.hash(newPassword, 10);
          await User.findOneAndUpdate({ phone }, { password: hashed });
          res.json({ success: true, message: 'Password reset successfully! Please login.' });
        } else {
          res.status(400).json({ success: false, message: 'Invalid OTP. Please try again.' });
        }
      });
    }).on('error', (err) => {
      res.status(500).json({ success: false, message: 'OTP service error: ' + err.message });
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};



// Generate JWT Token
const generateToken = (userId) => {
  return jwt.sign({ userId }, process.env.JWT_SECRET, {
    expiresIn: '30d'
  });
};

// Register/Sign Up
exports.register = async (req, res) => {
  try {
    const { name, phone, password, city, bloodGroup, bio } = req.body;

    // Check if user already exists
    const existingUser = await User.findOne({ phone });
    if (existingUser) {
      return res.status(400).json({
        success: false,
        message: 'Phone number already registered'
      });
    }

    // Create new user
    const user = new User({
      name,
      phone,
      password,
      city,
      bloodGroup,
      bio,
      isDonor: true
    });

    await user.save();

    // Generate token
    const token = generateToken(user._id);

    res.status(201).json({
      success: true,
      message: 'Registration successful',
      data: {
        user,
        token
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message || 'Registration failed'
    });
  }
};

// Login
exports.login = async (req, res) => {
  try {
    const { phone, password } = req.body;

    // Find user
    const user = await User.findOne({ phone });
    if (!user) {
      return res.status(401).json({
        success: false,
        message: 'Invalid phone number or password'
      });
    }

    // Check password
    const isPasswordValid = await user.comparePassword(password);
    if (!isPasswordValid) {
      return res.status(401).json({
        success: false,
        message: 'Invalid phone number or password'
      });
    }

    // Check if user is blocked by admin
    if (user.isBlocked) {
      return res.status(403).json({
        success: false,
        message: 'Your account has been blocked by admin. Please contact support.'
      });
    }

    // Generate token
    const token = generateToken(user._id);

    res.json({
      success: true,
      message: 'Login successful',
      data: {
        user,
        token
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message || 'Login failed'
    });
  }
};

// Get current user profile
exports.getProfile = async (req, res) => {
  try {
    const user = await User.findById(req.userId);

    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'User not found'
      });
    }

    res.json({
      success: true,
      data: user
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};

// Update profile
exports.updateProfile = async (req, res) => {
  try {
    const { name, city, bloodGroup, bio, isAvailable, lastDonation } = req.body;

    const user = await User.findByIdAndUpdate(
      req.userId,
      { name, city, bloodGroup, bio, isAvailable, lastDonation },
      { new: true, runValidators: true }
    );

    res.json({
      success: true,
      message: 'Profile updated successfully',
      data: user
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};

// Update profile picture
exports.updateProfilePic = async (req, res) => {
  try {
    console.log('\n📸 UPDATE PROFILE PIC CONTROLLER');
    console.log('👤 User ID:', req.userId);
    
    if (!req.file) {
      console.log('❌ No file received in request');
      return res.status(400).json({
        success: false,
        message: 'Please upload an image'
      });
    }

    console.log('🖼️ File details:', {
      fieldname: req.file.fieldname,
      originalname: req.file.originalname,
      filename: req.file.filename,
      size: req.file.size
    });

    // Get old profile pic to delete it
    const oldUser = await User.findById(req.userId);
    if (oldUser && oldUser.profilePic) {
      const fs = require('fs');
      const path = require('path');
      const oldPicPath = path.join(__dirname, '../../..', oldUser.profilePic);
      
      console.log('🗑️ Checking old profile pic:', oldPicPath);
      if (fs.existsSync(oldPicPath)) {
        fs.unlinkSync(oldPicPath);
        console.log('✅ Old profile pic deleted');
      }
    }

    const profilePicUrl = `/uploads/${req.file.filename}`;
    console.log('🔗 New URL:', profilePicUrl);

    const user = await User.findByIdAndUpdate(
      req.userId,
      { profilePic: profilePicUrl },
      { new: true }
    );

    console.log('✅ DB updated successfully');
    res.json({
      success: true,
      message: 'Profile picture updated successfully',
      data: {
        profilePic: user.profilePic
      }
    });
  } catch (error) {
    console.error('❌ Error in updateProfilePic:', error);
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};

// Remove profile picture
exports.removeProfilePic = async (req, res) => {
  try {
    console.log('\n🗑️ REMOVE PROFILE PIC CONTROLLER');
    console.log('👤 User ID:', req.userId);

    const user = await User.findByIdAndUpdate(
      req.userId,
      { profilePic: '' },
      { new: true }
    );

    console.log('✅ Profile picture removed from DB');
    res.json({
      success: true,
      message: 'Profile picture removed successfully',
      data: {
        profilePic: ''
      }
    });
  } catch (error) {
    console.error('❌ Error in removeProfilePic:', error);
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};
