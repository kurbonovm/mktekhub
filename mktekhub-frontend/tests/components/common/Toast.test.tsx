import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import "@testing-library/jest-dom/vitest";
import { render, screen, waitFor, act } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ToastContainer } from "../../../src/components/common/Toast";
import { ToastProvider, useToast } from "../../../src/contexts/ToastContext";

// Test component to trigger toasts
const ToastTrigger = () => {
  const toast = useToast();

  return (
    <div>
      <button onClick={() => toast.success("Success message")}>Success</button>
      <button onClick={() => toast.error("Error message")}>Error</button>
      <button onClick={() => toast.warning("Warning message")}>Warning</button>
      <button onClick={() => toast.info("Info message")}>Info</button>
      <button onClick={() => toast.showToast("Custom", "success", 1000)}>
        Custom Duration
      </button>
      <button onClick={() => toast.showToast("Persistent", "info", 0)}>
        Persistent
      </button>
    </div>
  );
};

const renderToastSystem = () => {
  return render(
    <ToastProvider>
      <ToastTrigger />
      <ToastContainer />
    </ToastProvider>,
  );
};

describe("Toast Components", () => {
  afterEach(() => {
    vi.clearAllTimers();
    vi.useRealTimers();
  });

  describe("ToastContainer", () => {
    it("should render without errors", () => {
      render(
        <ToastProvider>
          <ToastContainer />
        </ToastProvider>,
      );

      expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    });

    it("should have aria-live region for accessibility", () => {
      const { container } = render(
        <ToastProvider>
          <ToastContainer />
        </ToastProvider>,
      );

      const liveRegion = container.querySelector('[aria-live="assertive"]');
      expect(liveRegion).toBeInTheDocument();
    });

    it("should not display any toasts initially", () => {
      renderToastSystem();

      expect(screen.queryByText(/Success message/i)).not.toBeInTheDocument();
    });
  });

  describe("Success Toast", () => {
    it("should display success toast when triggered", async () => {
      const user = userEvent.setup();
      renderToastSystem();

      await user.click(screen.getByText("Success"));

      expect(screen.getByText("Success message")).toBeInTheDocument();
    });

    it("should have success styling classes", async () => {
      const user = userEvent.setup();
      renderToastSystem();

      await user.click(screen.getByText("Success"));

      const message = screen.getByText("Success message");
      const toast = message.closest('[class*="bg-green-50"]');
      expect(toast).toBeInTheDocument();
      expect(toast?.className).toMatch(/text-green-800/);
    });

    it("should display success icon", async () => {
      const user = userEvent.setup();
      renderToastSystem();

      await user.click(screen.getByText("Success"));

      const svgs = screen.getAllByRole("img", { hidden: true });
      const successIcon = svgs.find((svg) =>
        svg.getAttribute("class")?.includes("text-green-400"),
      );
      expect(successIcon).toBeInTheDocument();
    });
  });

  describe("Error Toast", () => {
    it("should display error toast when triggered", async () => {
      const user = userEvent.setup();
      renderToastSystem();

      await user.click(screen.getByText("Error"));

      expect(screen.getByText("Error message")).toBeInTheDocument();
    });

    it("should have error styling classes", async () => {
      const user = userEvent.setup();
      renderToastSystem();

      await user.click(screen.getByText("Error"));

      const message = screen.getByText("Error message");
      const toast = message.closest('[class*="rounded-lg"]');
      expect(toast?.className).toMatch(/bg-red-50/);
      expect(toast?.className).toMatch(/text-red-800/);
    });

    it("should display error icon", async () => {
      const user = userEvent.setup();
      renderToastSystem();

      await user.click(screen.getByText("Error"));

      const svgs = screen.getAllByRole("img", { hidden: true });
      const errorIcon = svgs.find((svg) =>
        svg.getAttribute("class")?.includes("text-red-400"),
      );
      expect(errorIcon).toBeInTheDocument();
    });
  });

  describe("Warning Toast", () => {
    it("should display warning toast when triggered", async () => {
      const user = userEvent.setup();
      renderToastSystem();

      await user.click(screen.getByText("Warning"));

      expect(screen.getByText("Warning message")).toBeInTheDocument();
    });

    it("should have warning styling classes", async () => {
      const user = userEvent.setup();
      renderToastSystem();

      await user.click(screen.getByText("Warning"));

      const message = screen.getByText("Warning message");
      const toast = message.closest('[class*="rounded-lg"]');
      expect(toast?.className).toMatch(/bg-yellow-50/);
      expect(toast?.className).toMatch(/text-yellow-800/);
    });

    it("should display warning icon", async () => {
      const user = userEvent.setup();
      renderToastSystem();

      await user.click(screen.getByText("Warning"));

      const svgs = screen.getAllByRole("img", { hidden: true });
      const warningIcon = svgs.find((svg) =>
        svg.getAttribute("class")?.includes("text-yellow-400"),
      );
      expect(warningIcon).toBeInTheDocument();
    });
  });

  describe("Info Toast", () => {
    it("should display info toast when triggered", async () => {
      const user = userEvent.setup();
      renderToastSystem();

      await user.click(screen.getByText("Info"));

      expect(screen.getByText("Info message")).toBeInTheDocument();
    });

    it("should have info styling classes", async () => {
      const user = userEvent.setup();
      renderToastSystem();

      await user.click(screen.getByText("Info"));

      const message = screen.getByText("Info message");
      const toast = message.closest('[class*="rounded-lg"]');
      expect(toast?.className).toMatch(/bg-blue-50/);
      expect(toast?.className).toMatch(/text-blue-800/);
    });

    it("should display info icon", async () => {
      const user = userEvent.setup();
      renderToastSystem();

      await user.click(screen.getByText("Info"));

      const svgs = screen.getAllByRole("img", { hidden: true });
      const infoIcon = svgs.find((svg) =>
        svg.getAttribute("class")?.includes("text-blue-400"),
      );
      expect(infoIcon).toBeInTheDocument();
    });
  });

  describe("Close Button", () => {
    it("should display close button", async () => {
      const user = userEvent.setup();
      renderToastSystem();

      await user.click(screen.getByText("Success"));

      expect(screen.getByText("Close")).toBeInTheDocument();
    });

    it("should remove toast when close button is clicked", async () => {
      const user = userEvent.setup();
      renderToastSystem();

      await user.click(screen.getByText("Success"));
      expect(screen.getByText("Success message")).toBeInTheDocument();

      await user.click(screen.getByText("Close"));

      // Wait for exit animation
      await waitFor(
        () => {
          expect(screen.queryByText("Success message")).not.toBeInTheDocument();
        },
        { timeout: 1000 },
      );
    });

    it("should have accessibility label", async () => {
      const user = userEvent.setup();
      renderToastSystem();

      await user.click(screen.getByText("Success"));

      expect(screen.getByText("Close")).toBeInTheDocument();
    });
  });

  describe("Auto-dismiss", () => {
    it("should auto-dismiss toast after default duration", async () => {
      vi.useFakeTimers();
      renderToastSystem();

      act(() => {
        const successButton = screen.getByText("Success");
        successButton.click();
      });

      expect(screen.getByText("Success message")).toBeInTheDocument();

      // Fast forward past the duration (5000ms)
      await act(async () => {
        await vi.advanceTimersByTimeAsync(5000);
      });

      expect(screen.queryByText("Success message")).not.toBeInTheDocument();

      vi.useRealTimers();
    });

    it("should auto-dismiss toast after custom duration", async () => {
      vi.useFakeTimers();
      renderToastSystem();

      act(() => {
        const customButton = screen.getByText("Custom Duration");
        customButton.click();
      });

      expect(screen.getByText("Custom")).toBeInTheDocument();

      // Should still be visible before duration
      await act(async () => {
        await vi.advanceTimersByTimeAsync(500);
      });
      expect(screen.getByText("Custom")).toBeInTheDocument();

      // Should be gone after duration
      await act(async () => {
        await vi.advanceTimersByTimeAsync(500);
      });

      expect(screen.queryByText("Custom")).not.toBeInTheDocument();

      vi.useRealTimers();
    });

    it("should not auto-dismiss when duration is 0", async () => {
      vi.useFakeTimers();
      renderToastSystem();

      act(() => {
        const persistentButton = screen.getByRole("button", {
          name: "Persistent",
        });
        persistentButton.click();
      });

      expect(screen.getAllByText("Persistent").length).toBeGreaterThan(0);

      // Advance time significantly
      await act(async () => {
        await vi.advanceTimersByTimeAsync(10000);
      });

      // Should still be visible
      expect(screen.getAllByText("Persistent").length).toBeGreaterThan(0);

      vi.useRealTimers();
    });

    it("should start exit animation before removal", async () => {
      vi.useFakeTimers();
      renderToastSystem();

      act(() => {
        const successButton = screen.getByText("Success");
        successButton.click();
      });

      const message = screen.getByText("Success message");
      const toastElement = message.closest('[class*="pointer-events-auto"]');

      // Before exit animation
      expect(toastElement?.className).toMatch(/translate-x-0/);
      expect(toastElement?.className).toMatch(/opacity-100/);

      // Advance to exit animation time (duration - 300ms)
      await act(async () => {
        await vi.advanceTimersByTimeAsync(4700);
      });

      // Should have exit classes
      expect(toastElement?.className).toMatch(/translate-x-full/);
      expect(toastElement?.className).toMatch(/opacity-0/);

      vi.useRealTimers();
    });
  });

  describe("Multiple Toasts", () => {
    it("should display multiple toasts simultaneously", async () => {
      const user = userEvent.setup();
      renderToastSystem();

      await user.click(screen.getByText("Success"));
      await user.click(screen.getByText("Error"));
      await user.click(screen.getByText("Warning"));

      expect(screen.getByText("Success message")).toBeInTheDocument();
      expect(screen.getByText("Error message")).toBeInTheDocument();
      expect(screen.getByText("Warning message")).toBeInTheDocument();
    });

    it("should stack toasts vertically", async () => {
      const user = userEvent.setup();
      const { container } = renderToastSystem();

      await user.click(screen.getByText("Success"));
      await user.click(screen.getByText("Error"));

      await waitFor(() => {
        const toastContainer = container.querySelector(
          '[aria-live="assertive"] > div',
        );
        expect(toastContainer?.className).toMatch(/flex-col/);
      });
    });

    it("should remove individual toasts independently", async () => {
      const user = userEvent.setup();
      renderToastSystem();

      await user.click(screen.getByText("Success"));
      await user.click(screen.getByText("Error"));
      await user.click(screen.getByText("Warning"));

      // Close the error toast
      const closeButtons = screen.getAllByText("Close");
      await user.click(closeButtons[1]); // Second close button (Error toast)

      await waitFor(
        () => {
          expect(screen.getByText("Success message")).toBeInTheDocument();
          expect(screen.queryByText("Error message")).not.toBeInTheDocument();
          expect(screen.getByText("Warning message")).toBeInTheDocument();
        },
        { timeout: 1000 },
      );
    });
  });

  describe("Animation and Transitions", () => {
    it("should apply transition classes", async () => {
      const user = userEvent.setup();
      renderToastSystem();

      await user.click(screen.getByText("Success"));

      const message = screen.getByText("Success message");
      const toastElement = message.closest('[class*="pointer-events-auto"]');
      expect(toastElement?.className).toMatch(/transition-all/);
      expect(toastElement?.className).toMatch(/duration-300/);
    });

    it("should have initial visible state", async () => {
      const user = userEvent.setup();
      renderToastSystem();

      await user.click(screen.getByText("Success"));

      const message = screen.getByText("Success message");
      const toastElement = message.closest('[class*="pointer-events-auto"]');
      expect(toastElement?.className).toMatch(/translate-x-0/);
      expect(toastElement?.className).toMatch(/opacity-100/);
    });

    it("should transition to exit state when closing", async () => {
      const user = userEvent.setup();
      renderToastSystem();

      await user.click(screen.getByText("Success"));

      const message = screen.getByText("Success message");
      const toastElement = message.closest('[class*="pointer-events-auto"]');

      await user.click(screen.getByText("Close"));

      expect(toastElement?.className).toMatch(/translate-x-full/);
      expect(toastElement?.className).toMatch(/opacity-0/);
    });
  });

  describe("Edge Cases", () => {
    it("should handle empty messages", async () => {
      const EmptyToastTrigger = () => {
        const toast = useToast();
        return <button onClick={() => toast.success("")}>Empty Message</button>;
      };

      render(
        <ToastProvider>
          <EmptyToastTrigger />
          <ToastContainer />
        </ToastProvider>,
      );

      const user = userEvent.setup();
      await user.click(screen.getByText("Empty Message"));

      // Toast should still render
      const closeButton = screen.getByText("Close");
      expect(closeButton).toBeInTheDocument();
    });

    it("should handle very long messages", async () => {
      const LongToastTrigger = () => {
        const toast = useToast();
        const longMessage = "A".repeat(500);
        return (
          <button onClick={() => toast.info(longMessage)}>Long Message</button>
        );
      };

      render(
        <ToastProvider>
          <LongToastTrigger />
          <ToastContainer />
        </ToastProvider>,
      );

      const user = userEvent.setup();
      await user.click(screen.getByText("Long Message"));

      expect(screen.getByText("A".repeat(500))).toBeInTheDocument();
    });

    it("should handle rapid toast creation", async () => {
      const RapidToastTrigger = () => {
        const toast = useToast();
        return (
          <button
            onClick={() => {
              for (let i = 0; i < 5; i++) {
                toast.success(`Toast ${i + 1}`);
              }
            }}
          >
            Rapid Toasts
          </button>
        );
      };

      render(
        <ToastProvider>
          <RapidToastTrigger />
          <ToastContainer />
        </ToastProvider>,
      );

      const user = userEvent.setup();
      await user.click(screen.getByText("Rapid Toasts"));

      expect(screen.getByText("Toast 1")).toBeInTheDocument();
      expect(screen.getByText("Toast 2")).toBeInTheDocument();
      expect(screen.getByText("Toast 3")).toBeInTheDocument();
      expect(screen.getByText("Toast 4")).toBeInTheDocument();
      expect(screen.getByText("Toast 5")).toBeInTheDocument();
    });
  });

  describe("Styling", () => {
    it("should apply correct border styles for success", async () => {
      const user = userEvent.setup();
      renderToastSystem();

      await user.click(screen.getByText("Success"));

      const message = screen.getByText("Success message");
      const toast = message.closest('[class*="rounded-lg"]');
      expect(toast?.className).toMatch(/border-green-200/);
    });

    it("should apply correct border styles for error", async () => {
      const user = userEvent.setup();
      renderToastSystem();

      await user.click(screen.getByText("Error"));

      const message = screen.getByText("Error message");
      const toast = message.closest('[class*="rounded-lg"]');
      expect(toast?.className).toMatch(/border-red-200/);
    });

    it("should apply correct border styles for warning", async () => {
      const user = userEvent.setup();
      renderToastSystem();

      await user.click(screen.getByText("Warning"));

      const message = screen.getByText("Warning message");
      const toast = message.closest('[class*="rounded-lg"]');
      expect(toast?.className).toMatch(/border-yellow-200/);
    });

    it("should apply correct border styles for info", async () => {
      const user = userEvent.setup();
      renderToastSystem();

      await user.click(screen.getByText("Info"));

      const message = screen.getByText("Info message");
      const toast = message.closest('[class*="rounded-lg"]');
      expect(toast?.className).toMatch(/border-blue-200/);
    });

    it("should have rounded corners", async () => {
      const user = userEvent.setup();
      renderToastSystem();

      await user.click(screen.getByText("Success"));

      const messageElement = screen.getByText("Success message");
      const toastContainer = messageElement.closest('[class*="rounded-lg"]');
      expect(toastContainer).toBeInTheDocument();
    });

    it("should have shadow", async () => {
      const user = userEvent.setup();
      renderToastSystem();

      await user.click(screen.getByText("Success"));

      const messageElement = screen.getByText("Success message");
      const toastContainer = messageElement.closest('[class*="shadow-lg"]');
      expect(toastContainer).toBeInTheDocument();
    });
  });
});
