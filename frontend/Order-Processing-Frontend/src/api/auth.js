import api from './axios';

/**
 * Auth API endpoints matching com.pravinmali.orderprocessing.controller.AuthController
 */

export const login = async (email, password) => {
  const response = await api.post('/auth/login', { email, password });
  return response.data; // AuthResponse: { token, tokenType, customerId, name, email, role }
};

export const register = async (userData) => {
  // userData: { name, email, password, phone, address }
  const response = await api.post('/auth/register', userData);
  return response.data;
};
