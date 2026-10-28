import React, { useState } from 'react';
import { Link, useNavigate, useLocation, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { extractApiError } from '../api/errorUtils';
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
      await login(email, password);
      // Unconditionally navigate directly to dashboard for both customer and admin
      navigate('/dashboard', { replace: true });
    } catch (err) {
      const parsed = await extractApiError(err, 'Invalid email or password. Please try again.');
      setError(parsed.message);
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
          <div className="auth-logo-icon">⚡</div>
          <span>OrderFlow</span>
          <span className="auth-logo-badge">PRO</span>
        </div>
        <p>Enterprise Order Processing & Fulfillment Service</p>
      </div>

      <div className="auth-card">
        <h1 className="auth-title">Welcome Back</h1>
        <p className="auth-subtitle">Sign in to manage orders, tracking events, and PDF invoices</p>

        {sessionExpired && (
          <div className="auth-alert auth-alert-info">
            <span>ℹ️</span>
            <span>Your session has expired. Please sign in again.</span>
          </div>
        )}

        {error && (
          <div className="auth-alert auth-alert-error" role="alert" style={{ whiteSpace: 'pre-line' }}>
            <span>⚠️</span>
            <span>{error}</span>
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
          <div className="auth-demo-box-header">
            <strong>⚡ Demo Accounts</strong>
            <span className="auth-demo-badge">Click to fill</span>
          </div>
          <div className="auth-demo-grid">
            <button
              type="button"
              className="auth-demo-btn"
              onClick={() => handleQuickFill('admin@orderflow.com', 'Admin@1234')}
            >
              <span className="auth-demo-tag admin">Admin</span>
              <span className="auth-demo-role">System Admin</span>
              <span className="auth-demo-email">admin@orderflow.com</span>
            </button>
            <button
              type="button"
              className="auth-demo-btn"
              onClick={() => handleQuickFill('pravin@example.com', 'Pass@1234')}
            >
              <span className="auth-demo-tag customer">Customer</span>
              <span className="auth-demo-role">Pravin Mali</span>
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
