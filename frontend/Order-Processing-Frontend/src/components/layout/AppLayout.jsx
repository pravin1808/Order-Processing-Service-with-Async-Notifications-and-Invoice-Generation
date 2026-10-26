import React from 'react';
import Navbar from './Navbar';

/**
 * Common application layout with top navigation bar.
 */
export const AppLayout = ({ children, onCreateOrderClick }) => {
  return (
    <div className="app-shell" style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <Navbar onCreateOrderClick={onCreateOrderClick} />
      <main style={{ flex: 1 }}>{children}</main>
    </div>
  );
};

export default AppLayout;
