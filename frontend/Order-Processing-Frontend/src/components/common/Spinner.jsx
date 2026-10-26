import React from 'react';

/**
 * Standard spinner component.
 *
 * @param {'sm'|'md'|'lg'} [size='md'] - Spinner size
 * @param {string} [message] - Optional message below spinner
 * @param {boolean} [center=true] - Whether to center inside container
 */
export const Spinner = ({ size = 'md', message = '', center = true }) => {
  const sizeClass = size === 'lg' ? 'spinner-lg' : size === 'sm' ? 'spinner-sm' : '';

  const spinnerElement = (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 'var(--space-2)' }}>
      <div className={`spinner ${sizeClass}`} />
      {message && <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{message}</span>}
    </div>
  );

  if (center) {
    return <div className="loading-center">{spinnerElement}</div>;
  }

  return spinnerElement;
};

export default Spinner;
