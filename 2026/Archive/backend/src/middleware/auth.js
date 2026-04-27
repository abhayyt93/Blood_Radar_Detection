const jwt = require('jsonwebtoken');

const auth = async (req, res, next) => {
  console.log('\n🔐 AUTH MIDDLEWARE CHECK');
  console.log('URL:', req.method, req.originalUrl);
  
  try {
    const authHeader = req.header('Authorization');
    console.log('Authorization Header:', authHeader ? 'Present' : 'Missing');
    
    const token = authHeader?.replace('Bearer ', '');
    
    if (!token) {
      console.log('❌ No token provided');
      return res.status(401).json({
        success: false,
        message: 'No authentication token provided'
      });
    }
    
    console.log('🔑 Token received (first 20 chars):', token.substring(0, 20) + '...');
    console.log('🔒 Verifying token with JWT_SECRET...');
    
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    req.userId = decoded.userId;
    
    console.log('✅ Token verified successfully');
    console.log('👤 User ID from token:', req.userId);
    console.log('🔐 AUTH MIDDLEWARE PASSED\n');
    
    next();
  } catch (error) {
    console.log('❌ Token verification failed:', error.message);
    res.status(401).json({
      success: false,
      message: 'Invalid or expired token'
    });
  }
};

module.exports = auth;
