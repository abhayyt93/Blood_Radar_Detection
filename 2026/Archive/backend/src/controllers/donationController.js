const Donation = require('../models/Donation');

// Create new donation post
exports.createDonation = async (req, res) => {
  console.log('\n========== CREATE DONATION POST START ==========');
  console.log('📥 Received request to create donation post');
  console.log('User ID from token:', req.userId);

  try {
    console.log('\n📋 Request Body:', req.body);

    let { bloodGroup, city, message, availableUntil } = req.body;

    // Convert date format if needed (DD/MM/YYYY to YYYY-MM-DD)
    if (availableUntil && availableUntil.includes('/')) {
      const parts = availableUntil.split('/');
      if (parts.length === 3) {
        // Convert DD/MM/YYYY to YYYY-MM-DD
        availableUntil = `${parts[2]}-${parts[1]}-${parts[0]}`;
      }
    }

    console.log('\n🩸 Extracted Data:');
    console.log('  - Blood Group:', bloodGroup);
    console.log('  - City:', city);
    console.log('  - Message:', message);
    console.log('  - Available Until (original):', req.body.availableUntil);
    console.log('  - Available Until (converted):', availableUntil);
    console.log('  - User ID:', req.userId);

    const donationData = {
      user: req.userId,
      bloodGroup,
      city,
      message,
      availableUntil
    };

    console.log('\n💾 Data to be saved in DB:', JSON.stringify(donationData, null, 2));

    const donation = new Donation(donationData);

    console.log('\n⏳ Saving to database...');
    const savedDonation = await donation.save();

    console.log('✅ Donation post saved successfully!');
    console.log('📝 Saved Donation ID:', savedDonation._id);

    await savedDonation.populate('user', '-password');

    console.log('✅ Donation populated with user data');
    console.log('👤 User Name:', savedDonation.user.name);
    console.log('📱 User Phone:', savedDonation.user.phone);

    console.log('\n🎉 Sending success response to client');
    console.log('========== CREATE DONATION POST END ==========\n');

    res.status(201).json({
      success: true,
      message: 'Donation post created successfully',
      data: savedDonation
    });
  } catch (error) {
    console.log('\n❌ ERROR in createDonation:');
    console.log('Error message:', error.message);
    console.log('Error stack:', error.stack);
    console.log('========== CREATE DONATION POST ERROR ==========\n');

    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};

// Get all active donation posts
exports.getAllDonations = async (req, res) => {
  console.log('\n========== GET ALL DONATIONS ==========');
  console.log('📥 Fetching all donation posts from database...');

  try {
    const donations = await Donation.find({ status: 'active' })
      .populate('user', '-password -phone')
      .sort({ createdAt: -1 });

    console.log('✅ Found', donations.length, 'active donation posts');

    // ✅ FIX: Filter out donations with null user (whose accounts have been deleted)
    const validDonations = donations.filter(d => d.user !== null);
    const skipped = donations.length - validDonations.length;
    if (skipped > 0) {
      console.log(`⚠️  ${skipped} donation(s) skipped — their user accounts have been deleted`);
    }

    if (validDonations.length > 0) {
      console.log('\n📋 Donations Summary:');
      validDonations.forEach((donation, index) => {
        console.log(`  ${index + 1}. ${donation.bloodGroup} - ${donation.city} by ${donation.user.name}`);
      });
    } else {
      console.log('⚠️  No valid donation posts found in the database');
    }

    console.log('========== GET ALL DONATIONS END ==========\n');

    res.json({
      success: true,
      count: validDonations.length,
      data: validDonations
    });
  } catch (error) {
    console.log('\n🔴🔴🔴 ERROR CAUGHT! 🔴🔴🔴');
    console.log('❌ Error Type:', error.name);
    console.log('❌ Error Message:', error.message);
    console.log('❌ Full Stack:\n', error.stack);
    console.log('========== GET ALL DONATIONS ERROR ==========\n');

    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};

// Search donation posts
exports.searchDonations = async (req, res) => {
  try {
    const { city, bloodGroup } = req.query;

    const query = { status: 'active' };

    if (city) {
      query.city = new RegExp(city, 'i');
    }

    if (bloodGroup) {
      query.bloodGroup = bloodGroup;
    }

    const donations = await Donation.find(query)
      .populate('user', '-password -phone')
      .sort({ createdAt: -1 });

    // ✅ FIX: Filter out donations with null user (same as getAllDonations)
    const validDonations = donations.filter(d => d.user !== null);

    res.json({
      success: true,
      count: validDonations.length,
      data: validDonations
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};

// Get donation post by ID
exports.getDonationById = async (req, res) => {
  try {
    const donation = await Donation.findById(req.params.id)
      .populate('user', '-password')
      .populate('responses.user', '-password');

    if (!donation) {
      return res.status(404).json({
        success: false,
        message: 'Donation post not found'
      });
    }

    res.json({
      success: true,
      data: donation
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};

// Update donation status
exports.updateDonationStatus = async (req, res) => {
  try {
    const { status } = req.body;

    const donation = await Donation.findById(req.params.id);

    if (!donation) {
      return res.status(404).json({
        success: false,
        message: 'Donation post not found'
      });
    }

    // Check if user owns this donation post
    if (donation.user.toString() !== req.userId) {
      return res.status(403).json({
        success: false,
        message: 'Not authorized to update this donation post'
      });
    }

    donation.status = status;
    await donation.save();

    res.json({
      success: true,
      message: 'Donation status updated',
      data: donation
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};

// Delete donation post
exports.deleteDonation = async (req, res) => {
  try {
    const donation = await Donation.findById(req.params.id);

    if (!donation) {
      return res.status(404).json({
        success: false,
        message: 'Donation post not found'
      });
    }

    // Check if user owns this donation post
    if (donation.user.toString() !== req.userId) {
      return res.status(403).json({
        success: false,
        message: 'Not authorized to delete this donation post'
      });
    }

    await donation.deleteOne();

    res.json({
      success: true,
      message: 'Donation post deleted successfully'
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};
