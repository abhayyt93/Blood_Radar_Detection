const User = require('../models/User');

// Get all donors
exports.getAllDonors = async (req, res) => {
  try {
    const donors = await User.find({ isDonor: true, isAvailable: true })
      .select('-password -phone')
      .sort({ createdAt: -1 });

    res.json({
      success: true,
      count: donors.length,
      data: donors
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};

// Search donors by city and blood group
exports.searchDonors = async (req, res) => {
  try {
    const { city, bloodGroup } = req.query;

    const query = { isDonor: true, isAvailable: true };

    if (city) {
      query.city = new RegExp(city, 'i'); // Case-insensitive search
    }

    if (bloodGroup) {
      query.bloodGroup = bloodGroup;
    }

    const donors = await User.find(query)
      .select('-password -phone')
      .sort({ createdAt: -1 });

    res.json({
      success: true,
      count: donors.length,
      data: donors
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};

// Get donor by ID
exports.getDonorById = async (req, res) => {
  try {
    const donor = await User.findById(req.params.id).select('-password -phone');

    if (!donor) {
      return res.status(404).json({
        success: false,
        message: 'Donor not found'
      });
    }

    res.json({
      success: true,
      data: donor
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};
