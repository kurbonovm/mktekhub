import { describe, it, expect, vi } from "vitest";
import {
  getErrorMessage,
  createMutationErrorHandler,
} from "../../src/utils/errorHandler";

describe("errorHandler", () => {
  describe("getErrorMessage", () => {
    it("should return default message when error is null", () => {
      expect(getErrorMessage(null)).toBe("An error occurred");
    });

    it("should return default message when error is undefined", () => {
      expect(getErrorMessage(undefined)).toBe("An error occurred");
    });

    it("should return custom default message when provided", () => {
      expect(getErrorMessage(null, "Custom error")).toBe("Custom error");
    });

    it("should extract message from Axios error response.data.message", () => {
      const error = {
        response: {
          data: {
            message: "API error message",
          },
        },
      };
      expect(getErrorMessage(error)).toBe("API error message");
    });

    it("should extract message from error.message (standard Error format)", () => {
      const error = new Error("Standard error message");
      expect(getErrorMessage(error)).toBe("Standard error message");
    });

    it("should prioritize response.data.message over error.message", () => {
      const error = {
        response: {
          data: {
            message: "API error message",
          },
        },
        message: "Error object message",
      };
      expect(getErrorMessage(error)).toBe("API error message");
    });

    it("should fallback to error.message when response.data.message is not available", () => {
      const error = {
        response: {
          data: {},
        },
        message: "Fallback error message",
      };
      expect(getErrorMessage(error)).toBe("Fallback error message");
    });

    it("should return default message when error has no extractable message", () => {
      const error = { someProperty: "value" };
      expect(getErrorMessage(error)).toBe("An error occurred");
    });

    it("should handle error with empty response.data.message", () => {
      const error = {
        response: {
          data: {
            message: "",
          },
        },
      };
      expect(getErrorMessage(error)).toBe("An error occurred");
    });

    it("should handle error with response but no data", () => {
      const error = {
        response: {},
        message: "Has message property",
      };
      expect(getErrorMessage(error)).toBe("Has message property");
    });

    it("should handle error with data but no message", () => {
      const error = {
        response: {
          data: {
            errors: ["Some error"],
          },
        },
        message: "Error message",
      };
      expect(getErrorMessage(error)).toBe("Error message");
    });

    it("should handle string errors", () => {
      const error = "String error";
      expect(getErrorMessage(error)).toBe("An error occurred");
    });

    it("should handle number errors", () => {
      const error = 404;
      expect(getErrorMessage(error)).toBe("An error occurred");
    });

    it("should handle nested error structures", () => {
      const error = {
        response: {
          data: {
            message: "Validation failed",
          },
        },
        message: "Request failed",
      };
      expect(getErrorMessage(error, "Default")).toBe("Validation failed");
    });

    it("should handle Error instances with custom messages", () => {
      const error = new TypeError("Type error occurred");
      expect(getErrorMessage(error)).toBe("Type error occurred");
    });

    it("should handle Axios 401 error format", () => {
      const error = {
        response: {
          status: 401,
          data: {
            message: "Unauthorized access",
          },
        },
      };
      expect(getErrorMessage(error)).toBe("Unauthorized access");
    });

    it("should handle Axios 500 error format", () => {
      const error = {
        response: {
          status: 500,
          data: {
            message: "Internal server error",
          },
        },
      };
      expect(getErrorMessage(error)).toBe("Internal server error");
    });

    it("should handle network errors", () => {
      const error = new Error("Network Error");
      expect(getErrorMessage(error)).toBe("Network Error");
    });
  });

  describe("createMutationErrorHandler", () => {
    it("should create an error handler function", () => {
      const mockToast = { error: vi.fn() };
      const handler = createMutationErrorHandler(mockToast, "Default error");

      expect(typeof handler).toBe("function");
    });

    it("should call toast.error with extracted error message", () => {
      const mockToast = { error: vi.fn() };
      const handler = createMutationErrorHandler(mockToast, "Default error");

      const error = {
        response: {
          data: {
            message: "API error occurred",
          },
        },
      };

      handler(error);

      expect(mockToast.error).toHaveBeenCalledWith("API error occurred");
      expect(mockToast.error).toHaveBeenCalledTimes(1);
    });

    it("should use default message when error has no message", () => {
      const mockToast = { error: vi.fn() };
      const handler = createMutationErrorHandler(mockToast, "Operation failed");

      handler({});

      expect(mockToast.error).toHaveBeenCalledWith("Operation failed");
    });

    it("should handle standard Error instances", () => {
      const mockToast = { error: vi.fn() };
      const handler = createMutationErrorHandler(mockToast, "Default error");

      handler(new Error("Something went wrong"));

      expect(mockToast.error).toHaveBeenCalledWith("Something went wrong");
    });

    it("should handle null errors", () => {
      const mockToast = { error: vi.fn() };
      const handler = createMutationErrorHandler(
        mockToast,
        "Unknown error occurred",
      );

      handler(null);

      expect(mockToast.error).toHaveBeenCalledWith("Unknown error occurred");
    });

    it("should handle undefined errors", () => {
      const mockToast = { error: vi.fn() };
      const handler = createMutationErrorHandler(mockToast, "Error occurred");

      handler(undefined);

      expect(mockToast.error).toHaveBeenCalledWith("Error occurred");
    });

    it("should create independent handlers with different toast instances", () => {
      const mockToast1 = { error: vi.fn() };
      const mockToast2 = { error: vi.fn() };

      const handler1 = createMutationErrorHandler(mockToast1, "Error 1");
      const handler2 = createMutationErrorHandler(mockToast2, "Error 2");

      const error = new Error("Test error");

      handler1(error);
      handler2(error);

      expect(mockToast1.error).toHaveBeenCalledWith("Test error");
      expect(mockToast2.error).toHaveBeenCalledWith("Test error");
      expect(mockToast1.error).toHaveBeenCalledTimes(1);
      expect(mockToast2.error).toHaveBeenCalledTimes(1);
    });

    it("should handle multiple error calls", () => {
      const mockToast = { error: vi.fn() };
      const handler = createMutationErrorHandler(mockToast, "Default");

      handler(new Error("First error"));
      handler(new Error("Second error"));
      handler(new Error("Third error"));

      expect(mockToast.error).toHaveBeenCalledTimes(3);
      expect(mockToast.error).toHaveBeenNthCalledWith(1, "First error");
      expect(mockToast.error).toHaveBeenNthCalledWith(2, "Second error");
      expect(mockToast.error).toHaveBeenNthCalledWith(3, "Third error");
    });

    it("should prioritize API error messages", () => {
      const mockToast = { error: vi.fn() };
      const handler = createMutationErrorHandler(mockToast, "Mutation failed");

      const error = {
        response: {
          data: {
            message: "Validation error: Name is required",
          },
        },
        message: "Request failed",
      };

      handler(error);

      expect(mockToast.error).toHaveBeenCalledWith(
        "Validation error: Name is required",
      );
    });
  });
});
