import React, { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { exportOrdersCsv } from '../../api/orders';
import '../../styles/navbar.css';

export const Navbar = ({ onCreateOrderClick }) => {
  const { user, isAdmin, isCustomer, logout } = useAuth();
  const navigate = useNavigate();
  const [exporting, setExporting] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  const handleExportCsv = async () => {
    try {
      setExporting(true);
      await exportOrdersCsv();
    } catch (err) {
      alert('Failed to export CSV. Please try again.');
    } finally {
      setExporting(false);
    }
  };

  const initial = user?.name ? user.name.charAt(0).toUpperCase() : 'U';

  return (
    <header className="navbar">
      <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-6)' }}>
        <Link to={isAdmin ? '/dashboard' : '/orders'} className="navbar-brand">
          <span>OrderFlow</span>
          <span className="navbar-badge">PRO</span>
        </Link>

        <nav className="navbar-nav">
          {isAdmin && (
            <NavLink
              to="/dashboard"
              className={({ isActive }) => `navbar-link ${isActive ? 'active' : ''}`}
            >
              Dashboard
            </NavLink>
          )}

          <NavLink
            to="/orders"
            className={({ isActive }) => `navbar-link ${isActive ? 'active' : ''}`}
          >
            {isAdmin ? 'All Orders' : 'My Orders'}
          </NavLink>
        </nav>
      </div>

      <div className="navbar-actions">
        {/* Customer CTA to open drawer */}
        {isCustomer && onCreateOrderClick && (
          <button
            type="button"
            className="btn btn-primary btn-sm"
            onClick={onCreateOrderClick}
          >
            + Create Order
          </button>
        )}

        {/* Admin CSV Export shortcut */}
        {isAdmin && (
          <button
            type="button"
            className="btn btn-sm"
            style={{
              background: 'rgba(255, 255, 255, 0.1)',
              color: '#FFFFFF',
              borderColor: 'rgba(255, 255, 255, 0.2)',
            }}
            onClick={handleExportCsv}
            disabled={exporting}
            title="Export all orders to CSV"
          >
            {exporting ? 'Exporting...' : '📥 Export CSV'}
          </button>
        )}

        {/* User Info Pill */}
        <div className="navbar-user">
          <div className="navbar-user-avatar">{initial}</div>
          <div className="navbar-user-info">
            <span className="navbar-user-name">{user?.name || 'User'}</span>
            <span className="navbar-user-role">{user?.role || 'User'}</span>
          </div>
        </div>

        {/* Logout Button */}
        <button
          type="button"
          onClick={handleLogout}
          className="btn-navbar-logout"
          title="Sign out of your account"
        >
          Sign Out
        </button>
      </div>
    </header>
  );
};

export default Navbar;
