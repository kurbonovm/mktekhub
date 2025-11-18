import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { renderHook, act } from "@testing-library/react";
import { ToastProvider, useToast } from "../../src/contexts/ToastContext";

describe("ToastContext", () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  describe("ToastProvider initialization", () => {
    it("should provide toast context to children", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      expect(result.current).toBeDefined();
      expect(result.current.toasts).toEqual([]);
    });

    it("should have all required methods", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      expect(result.current.showToast).toBeDefined();
      expect(result.current.removeToast).toBeDefined();
      expect(result.current.success).toBeDefined();
      expect(result.current.error).toBeDefined();
      expect(result.current.warning).toBeDefined();
      expect(result.current.info).toBeDefined();
    });
  });

  describe("useToast hook", () => {
    it("should throw error when used outside ToastProvider", () => {
      // Suppress console.error for this test
      const consoleError = vi
        .spyOn(console, "error")
        .mockImplementation(() => {});

      expect(() => {
        renderHook(() => useToast());
      }).toThrow("useToast must be used within ToastProvider");

      consoleError.mockRestore();
    });
  });

  describe("showToast", () => {
    it("should add a toast to the list", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      act(() => {
        result.current.showToast("Test message", "success");
      });

      expect(result.current.toasts).toHaveLength(1);
      expect(result.current.toasts[0].message).toBe("Test message");
      expect(result.current.toasts[0].type).toBe("success");
    });

    it("should generate unique IDs for toasts", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      act(() => {
        result.current.showToast("First", "success");
        result.current.showToast("Second", "error");
      });

      expect(result.current.toasts).toHaveLength(2);
      expect(result.current.toasts[0].id).not.toBe(result.current.toasts[1].id);
    });

    it("should set default duration to 5000ms", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      act(() => {
        result.current.showToast("Test", "info");
      });

      expect(result.current.toasts[0].duration).toBe(5000);
    });

    it("should accept custom duration", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      act(() => {
        result.current.showToast("Test", "info", 3000);
      });

      expect(result.current.toasts[0].duration).toBe(3000);
    });

    it("should auto-remove toast after duration", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      act(() => {
        result.current.showToast("Test", "success", 2000);
      });

      expect(result.current.toasts).toHaveLength(1);

      act(() => {
        vi.advanceTimersByTime(2000);
      });

      expect(result.current.toasts).toHaveLength(0);
    });

    it("should not auto-remove toast when duration is 0", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      act(() => {
        result.current.showToast("Persistent", "info", 0);
      });

      expect(result.current.toasts).toHaveLength(1);

      act(() => {
        vi.advanceTimersByTime(10000);
      });

      expect(result.current.toasts).toHaveLength(1);
    });

    it("should handle multiple toasts with different durations", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      act(() => {
        result.current.showToast("First", "success", 1000);
        result.current.showToast("Second", "error", 3000);
        result.current.showToast("Third", "warning", 2000);
      });

      expect(result.current.toasts).toHaveLength(3);

      // After 1000ms, first toast should be removed
      act(() => {
        vi.advanceTimersByTime(1000);
      });
      expect(result.current.toasts).toHaveLength(2);

      // After 2000ms total, third toast should be removed
      act(() => {
        vi.advanceTimersByTime(1000);
      });
      expect(result.current.toasts).toHaveLength(1);

      // After 3000ms total, second toast should be removed
      act(() => {
        vi.advanceTimersByTime(1000);
      });
      expect(result.current.toasts).toHaveLength(0);
    });
  });

  describe("removeToast", () => {
    it("should remove a specific toast by id", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      act(() => {
        result.current.showToast("First", "success");
        result.current.showToast("Second", "error");
      });

      const toastId = result.current.toasts[0].id;

      act(() => {
        result.current.removeToast(toastId);
      });

      expect(result.current.toasts).toHaveLength(1);
      expect(result.current.toasts[0].message).toBe("Second");
    });

    it("should handle removing non-existent toast", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      act(() => {
        result.current.showToast("Test", "info");
      });

      act(() => {
        result.current.removeToast("non-existent-id");
      });

      expect(result.current.toasts).toHaveLength(1);
    });

    it("should handle removing from empty toast list", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      act(() => {
        result.current.removeToast("any-id");
      });

      expect(result.current.toasts).toHaveLength(0);
    });

    it("should remove all toasts when called for each", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      act(() => {
        result.current.showToast("First", "success");
        result.current.showToast("Second", "error");
        result.current.showToast("Third", "warning");
      });

      const ids = result.current.toasts.map((t) => t.id);

      act(() => {
        ids.forEach((id) => result.current.removeToast(id));
      });

      expect(result.current.toasts).toHaveLength(0);
    });
  });

  describe("success helper", () => {
    it("should create success toast", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      act(() => {
        result.current.success("Success message");
      });

      expect(result.current.toasts).toHaveLength(1);
      expect(result.current.toasts[0].type).toBe("success");
      expect(result.current.toasts[0].message).toBe("Success message");
    });

    it("should use default duration", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      act(() => {
        result.current.success("Success");
      });

      expect(result.current.toasts[0].duration).toBe(5000);
    });

    it("should accept custom duration", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      act(() => {
        result.current.success("Success", 3000);
      });

      expect(result.current.toasts[0].duration).toBe(3000);
    });
  });

  describe("error helper", () => {
    it("should create error toast", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      act(() => {
        result.current.error("Error message");
      });

      expect(result.current.toasts).toHaveLength(1);
      expect(result.current.toasts[0].type).toBe("error");
      expect(result.current.toasts[0].message).toBe("Error message");
    });

    it("should accept custom duration", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      act(() => {
        result.current.error("Error", 10000);
      });

      expect(result.current.toasts[0].duration).toBe(10000);
    });
  });

  describe("warning helper", () => {
    it("should create warning toast", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      act(() => {
        result.current.warning("Warning message");
      });

      expect(result.current.toasts).toHaveLength(1);
      expect(result.current.toasts[0].type).toBe("warning");
      expect(result.current.toasts[0].message).toBe("Warning message");
    });

    it("should accept custom duration", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      act(() => {
        result.current.warning("Warning", 7000);
      });

      expect(result.current.toasts[0].duration).toBe(7000);
    });
  });

  describe("info helper", () => {
    it("should create info toast", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      act(() => {
        result.current.info("Info message");
      });

      expect(result.current.toasts).toHaveLength(1);
      expect(result.current.toasts[0].type).toBe("info");
      expect(result.current.toasts[0].message).toBe("Info message");
    });

    it("should accept custom duration", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      act(() => {
        result.current.info("Info", 4000);
      });

      expect(result.current.toasts[0].duration).toBe(4000);
    });
  });

  describe("multiple toast types", () => {
    it("should handle all toast types simultaneously", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      act(() => {
        result.current.success("Success");
        result.current.error("Error");
        result.current.warning("Warning");
        result.current.info("Info");
      });

      expect(result.current.toasts).toHaveLength(4);
      expect(result.current.toasts[0].type).toBe("success");
      expect(result.current.toasts[1].type).toBe("error");
      expect(result.current.toasts[2].type).toBe("warning");
      expect(result.current.toasts[3].type).toBe("info");
    });

    it("should maintain FIFO order of toasts", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      act(() => {
        result.current.showToast("First", "success");
        result.current.showToast("Second", "error");
        result.current.showToast("Third", "warning");
      });

      expect(result.current.toasts[0].message).toBe("First");
      expect(result.current.toasts[1].message).toBe("Second");
      expect(result.current.toasts[2].message).toBe("Third");
    });
  });

  describe("edge cases", () => {
    it("should handle empty message", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      act(() => {
        result.current.success("");
      });

      expect(result.current.toasts).toHaveLength(1);
      expect(result.current.toasts[0].message).toBe("");
    });

    it("should handle very long messages", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      const longMessage = "A".repeat(1000);

      act(() => {
        result.current.info(longMessage);
      });

      expect(result.current.toasts[0].message).toBe(longMessage);
    });

    it("should handle special characters in message", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      act(() => {
        result.current.error("<script>alert('XSS')</script>");
      });

      expect(result.current.toasts[0].message).toBe(
        "<script>alert('XSS')</script>",
      );
    });

    it("should handle rapid toast creation", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      act(() => {
        for (let i = 0; i < 100; i++) {
          result.current.success(`Toast ${i}`);
        }
      });

      expect(result.current.toasts).toHaveLength(100);
    });

    it("should handle negative duration gracefully", () => {
      const { result } = renderHook(() => useToast(), {
        wrapper: ToastProvider,
      });

      act(() => {
        result.current.showToast("Test", "info", -1000);
      });

      // Negative duration should not auto-remove (treated as 0 or less)
      expect(result.current.toasts).toHaveLength(1);
    });
  });
});
