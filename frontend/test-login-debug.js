const axios = require('axios');

async function testLogin() {
  try {
    console.log('Testing login endpoint...');
    const response = await axios.post('http://localhost:8080/api/v1/auth/login', {
      email: 'admin@test.com',
      password: 'admin123'
    });
    console.log('Success:', response.status, response.data);
  } catch (error) {
    console.error('Error:', error.response?.status, error.response?.data || error.message);
  }
}

testLogin();
