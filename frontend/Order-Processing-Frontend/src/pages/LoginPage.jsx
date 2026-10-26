import React, { useState } from 'react';
import { Link, useNavigate, useLocation, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import '../styles/auth.css';

export const LoginPage = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const sessionExpired = searchParams.get('expired') === 'true';
  const from = location.state?.from?.pathname;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!email || !password) {
      setError('Please enter both email and password.');
      return;
    }

    setError('');
    setLoading(true);

    try {
      const user = await login(email, password);
      // Navigate to intended destination or role-based home
      if (from) {
        navigate(from, { replace: true });
      } else if (user.role === 'ADMIN') {
        navigate('/dashboard', { replace: true });
      } else {
        navigate('/orders', { replace: true });
      }
    } catch (err) {
      const serverMessage = err.response?.data?.message || err.response?.data?.error;
      setError(serverMessage || 'Invalid email or password. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleQuickFill = (demoEmail, demoPassword) => {
    setEmail(demoEmail);
    setPassword(demoPassword);
    setError('');
  };

  return (
    <div className="auth-container">
      <div className="auth-header">
        <div className="auth-logo">
          <span>OrderFlow</span>
          <span className="auth-logo-badge">PRO</span>
        </div>
        <p className="text-secondary text-sm">Enterprise Order Processing & Fulfillment</p>
      </div>

      <div className="auth-card">
        <h1 className="auth-title">Sign In</h1>
        <p className="auth-subtitle">Enter your account credentials to access your dashboard</p>

        {sessionExpired && (
          <div className="auth-alert auth-alert-info">
            Your session has expired. Please sign in again.
          </div>
        )}

        {error && (
          <div className="auth-alert auth-alert-error" role="alert">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="auth-form" noValidate>
          <div className="form-group">
            <label className="form-label" htmlFor="email">Email Address</label>
            <input
              id="email"
              type="email"
              className="form-input"
              placeholder="name@company.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              autoFocus
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              className="form-input"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary btn-lg"
            disabled={loading}
            style={{ width: '100%', justifyContent: 'center', marginTop: 'var(--space-2)' }}
          >
            {loading ? 'Signing in...' : 'Sign In →'}
          </button>
        </form>

        <div className="auth-demo-box">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <strong>Demo Accounts</strong>
            <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Click to fill</span>
          </div>
          <div className="auth-demo-grid">
            <button
              type="button"
              className="auth-demo-btn"
              onClick={() => handleQuickFill('admin@orderflow.com', 'Admin@1234')}
            >
              <span className="auth-demo-role">Admin Account</span>
              <span className="auth-demo-email">admin@orderflow.com</span>
            </button>
            <button
              type="button"
              className="auth-demo-btn"
              onClick={() => handleQuickFill('pravin@example.com', 'Pass@1234')}
            >
              <span className="auth-demo-role">Customer Account</span>
              <span className="auth-demo-email">pravin@example.com</span>
            </button>
          </div>
        </div>

        <div className="auth-footer">
          Don't have an account? <Link to="/register">Create customer account</Link>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
