import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

/**
 * Route guard component that enforces authentication and RBAC roles.
 *
 * @param {string[]} [allowedRoles] - Optional list of required roles (e.g. ['ADMIN'])
 * @param {React.ReactNode} children - Child components to render if authorized
 */
export const PrivateRoute = ({ allowedRoles, children }) => {
  const { isAuthenticated, role } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    // Redirect to login page and preserve requested path for post-login redirect
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // If specific roles are required, check role membership
  if (allowedRoles && allowedRoles.length > 0 && !allowedRoles.includes(role)) {
    // Role mismatch: redirect to appropriate home
    const fallbackPath = role === 'ADMIN' ? '/dashboard' : '/orders';
    return <Navigate to={fallbackPath} replace />;
  }

  return children;
};

export default PrivateRoute;
