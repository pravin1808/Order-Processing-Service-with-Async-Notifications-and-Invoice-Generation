import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const NotFoundPage = () => {
  const { isAuthenticated, isAdmin } = useAuth();
  const homePath = isAuthenticated ? (isAdmin ? '/dashboard' : '/orders') : '/login';

  return (
    <div style={{
      minHeight: '80vh',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      textAlign: 'center',
      padding: 'var(--space-6)',
    }}>
      <h1 style={{ fontSize: '72px', fontWeight: 800, color: 'var(--text-muted)', marginBottom: 'var(--space-2)' }}>404</h1>
      <h2 style={{ marginBottom: 'var(--space-2)' }}>Page Not Found</h2>
      <p style={{ color: 'var(--text-secondary)', marginBottom: 'var(--space-6)', maxWidth: '400px' }}>
        The page you are looking for does not exist, or you don't have permission to view it.
      </p>
      <Link to={homePath} className="btn btn-primary">
        Back to Home
      </Link>
    </div>
  );
};

export default NotFoundPage;
