/**
 * Utility functions for extracting clear, descriptive error messages
 * from backend API responses (including Bean Validation errors, Blob responses, and HTTP status codes).
 */

/**
 * Parses any Axios error and returns a human-readable message and an optional map of field-level errors.
 *
 * @param {any} err - The error caught in try/catch
 * @param {string} [fallback='An unexpected error occurred.'] - Default fallback message
 * @returns {Promise<{ message: string, fieldErrors: Record<string, string>, title: string }>}
 */
export const extractApiError = async (err, fallback = 'An unexpected error occurred.') => {
  if (!err) {
    return { message: fallback, fieldErrors: {}, title: 'Error' };
  }

  // Handle network / server unreachable errors
  if (!err.response) {
    if (err.message === 'Network Error' || err.code === 'ERR_NETWORK') {
      return {
        message: 'Cannot connect to backend server. Please verify the Spring Boot service is running on port 8080.',
        fieldErrors: {},
        title: 'Connection Refused',
      };
    }
    return {
      message: err.message || fallback,
      fieldErrors: {},
      title: 'Request Failed',
    };
  }

  let data = err.response.data;

  // Handle Blob response errors (common in file download endpoints like PDF invoices)
  if (data instanceof Blob) {
    try {
      const text = await data.text();
      data = JSON.parse(text);
    } catch {
      data = null;
    }
  }

  const status = err.response.status;
  const fieldErrorsMap = {};

  // Extract Bean Validation fieldErrors array if present
  if (data?.fieldErrors && Array.isArray(data.fieldErrors) && data.fieldErrors.length > 0) {
    const errorBullets = [];
    data.fieldErrors.forEach((fe) => {
      if (fe.field && fe.message) {
        fieldErrorsMap[fe.field] = fe.message;
        errorBullets.push(`${fe.field}: ${fe.message}`);
      }
    });

    return {
      title: data.error || 'Validation Failed',
      message: errorBullets.length > 0
        ? `Please fix the following: \n• ${errorBullets.join('\n• ')}`
        : (data.message || 'One or more fields have invalid values.'),
      fieldErrors: fieldErrorsMap,
    };
  }

  // Check custom backend ErrorResponse message or error title
  if (data?.message) {
    return {
      title: data.error || 'Request Error',
      message: data.message,
      fieldErrors: {},
    };
  }

  if (data?.error && typeof data.error === 'string') {
    return {
      title: data.error,
      message: data.error,
      fieldErrors: {},
    };
  }

  // HTTP status fallbacks
  switch (status) {
    case 400:
      return { title: 'Bad Request', message: 'The request was invalid or incomplete.', fieldErrors: {} };
    case 401:
      return { title: 'Unauthorized', message: 'Your session has expired or is invalid. Please sign in again.', fieldErrors: {} };
    case 403:
      return { title: 'Access Denied', message: 'You do not have permission to view or perform this action (HTTP 403 Forbidden).', fieldErrors: {} };
    case 404:
      return { title: 'Not Found', message: 'The requested order or resource was not found (HTTP 404).', fieldErrors: {} };
    case 409:
      return { title: 'Conflict', message: 'The action conflicts with the current order state.', fieldErrors: {} };
    case 422:
      return { title: 'Unprocessable', message: 'The requested status transition is not permitted.', fieldErrors: {} };
    case 500:
      return { title: 'Server Error', message: 'An internal server error occurred on the backend. Please try again later.', fieldErrors: {} };
    default:
      return { title: 'Error', message: fallback, fieldErrors: {} };
  }
};
