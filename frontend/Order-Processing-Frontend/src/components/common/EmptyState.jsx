import React from 'react';

/**
 * EmptyState component displayed when lists or search results are empty.
 *
 * @param {string} [title='No records found'] - Heading
 * @param {string} [description='There are no items to display at this time.'] - Explanation
 * @param {React.ReactNode} [action] - Optional CTA button
 * @param {string} [icon='📦'] - Icon or emoji
 */
export const EmptyState = ({
  title = 'No records found',
  description = 'There are no items to display at this time.',
  action = null,
  icon = '📦',
}) => {
  return (
    <div className="empty-state">
      <div className="empty-state-icon">{icon}</div>
      <h3>{title}</h3>
      <p>{description}</p>
      {action && <div style={{ marginTop: 'var(--space-2)' }}>{action}</div>}
    </div>
  );
};

export default EmptyState;
