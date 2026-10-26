import React, { createContext, useContext, useState, useEffect } from 'react';
import * as authApi from '../api/auth';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    try {
      const savedUser = localStorage.getItem('user');
      const token = localStorage.getItem('token');
      if (savedUser && token) {
        return JSON.parse(savedUser);
      }
    } catch {
      localStorage.removeItem('user');
      localStorage.removeItem('token');
    }
    return null;
  });

  const [loading, setLoading] = useState(false);

  const login = async (email, password) => {
    setLoading(true);
    try {
      const data = await authApi.login(email, password);
      // data: { token, tokenType, customerId, name, email, role }
      const authUser = {
        token: data.token,
        customerId: data.customerId,
        name: data.name,
        email: data.email,
        role: data.role,
      };
      localStorage.setItem('token', data.token);
      localStorage.setItem('user', JSON.stringify(authUser));
      setUser(authUser);
      return authUser;
    } finally {
      setLoading(false);
    }
  };

  const register = async (userData) => {
    setLoading(true);
    try {
      const data = await authApi.register(userData);
      const authUser = {
        token: data.token,
        customerId: data.customerId,
        name: data.name,
        email: data.email,
        role: data.role,
      };
      localStorage.setItem('token', data.token);
      localStorage.setItem('user', JSON.stringify(authUser));
      setUser(authUser);
      return authUser;
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  const value = {
    user,
    token: user?.token || null,
    role: user?.role || null,
    isAuthenticated: !!user?.token,
    isAdmin: user?.role === 'ADMIN',
    isCustomer: user?.role === 'CUSTOMER',
    loading,
    login,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
