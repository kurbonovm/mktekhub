/**
 * Utility for handling API errors consistently across the application
 */

export interface ApiError {
  response?: {
    data?: {
      message?: string;
    };
  };
  message?: string;
}

/**
 * Extracts error message from various error formats
 */
export const getErrorMessage = (
  error: unknown,
  defaultMessage: string = "An error occurred",
): string => {
  if (!error) return defaultMessage;

  const apiError = error as ApiError;

  // Try to get message from response.data.message (Axios error format)
  if (apiError.response?.data?.message) {
    return apiError.response.data.message;
  }

  // Try to get message from error.message (standard Error format)
  if (apiError.message) {
    return apiError.message;
  }

  return defaultMessage;
};

/**
 * Creates a mutation error handler with toast notification
 */
export const createMutationErrorHandler = (
  toast: { error: (message: string) => void },
  defaultMessage: string,
) => {
  return (error: unknown) => {
    const message = getErrorMessage(error, defaultMessage);
    toast.error(message);
  };
};
