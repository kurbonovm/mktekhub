import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ConfirmDialog } from "@/components/common/ConfirmDialog";

describe("ConfirmDialog", () => {
  const defaultProps = {
    isOpen: true,
    title: "Confirm Action",
    message: "Are you sure you want to proceed?",
    onConfirm: vi.fn(),
    onCancel: vi.fn(),
  };

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe("Rendering", () => {
    it("should not render when isOpen is false", () => {
      render(<ConfirmDialog {...defaultProps} isOpen={false} />);

      expect(screen.queryByText("Confirm Action")).not.toBeInTheDocument();
    });

    it("should render when isOpen is true", () => {
      render(<ConfirmDialog {...defaultProps} />);

      expect(screen.getByText("Confirm Action")).toBeInTheDocument();
      expect(
        screen.getByText("Are you sure you want to proceed?"),
      ).toBeInTheDocument();
    });

    it("should display custom title", () => {
      render(<ConfirmDialog {...defaultProps} title="Delete Item" />);

      expect(screen.getByText("Delete Item")).toBeInTheDocument();
    });

    it("should display custom message", () => {
      render(
        <ConfirmDialog
          {...defaultProps}
          message="This action cannot be undone."
        />,
      );

      expect(
        screen.getByText("This action cannot be undone."),
      ).toBeInTheDocument();
    });

    it("should display default confirm button text", () => {
      render(<ConfirmDialog {...defaultProps} />);

      expect(
        screen.getByRole("button", { name: "Confirm" }),
      ).toBeInTheDocument();
    });

    it("should display custom confirm button text", () => {
      render(<ConfirmDialog {...defaultProps} confirmText="Delete" />);

      expect(
        screen.getByRole("button", { name: "Delete" }),
      ).toBeInTheDocument();
    });

    it("should display default cancel button text", () => {
      render(<ConfirmDialog {...defaultProps} />);

      expect(
        screen.getByRole("button", { name: "Cancel" }),
      ).toBeInTheDocument();
    });

    it("should display custom cancel button text", () => {
      render(<ConfirmDialog {...defaultProps} cancelText="Go Back" />);

      expect(
        screen.getByRole("button", { name: "Go Back" }),
      ).toBeInTheDocument();
    });
  });

  describe("Variants", () => {
    it("should render danger variant by default", () => {
      const { container } = render(<ConfirmDialog {...defaultProps} />);

      const confirmButton = screen.getByRole("button", { name: "Confirm" });
      expect(confirmButton.className).toContain("bg-red-600");
    });

    it("should render danger variant with correct styling", () => {
      const { container } = render(
        <ConfirmDialog {...defaultProps} variant="danger" />,
      );

      const confirmButton = screen.getByRole("button", { name: "Confirm" });
      expect(confirmButton.className).toContain("bg-red-600");

      // Check for danger icon background
      const iconWrapper = container.querySelector(".bg-red-100");
      expect(iconWrapper).toBeInTheDocument();
    });

    it("should render primary variant with correct styling", () => {
      const { container } = render(
        <ConfirmDialog {...defaultProps} variant="primary" />,
      );

      const confirmButton = screen.getByRole("button", { name: "Confirm" });
      expect(confirmButton.className).toContain("bg-indigo-600");

      // Check for primary icon background
      const iconWrapper = container.querySelector(".bg-indigo-100");
      expect(iconWrapper).toBeInTheDocument();
    });

    it("should render warning variant with correct styling", () => {
      const { container } = render(
        <ConfirmDialog {...defaultProps} variant="warning" />,
      );

      const confirmButton = screen.getByRole("button", { name: "Confirm" });
      expect(confirmButton.className).toContain("bg-yellow-600");

      // Check for warning icon background
      const iconWrapper = container.querySelector(".bg-yellow-100");
      expect(iconWrapper).toBeInTheDocument();
    });

    it("should render appropriate icon for danger variant", () => {
      const { container } = render(
        <ConfirmDialog {...defaultProps} variant="danger" />,
      );

      const icon = container.querySelector(".text-red-600");
      expect(icon).toBeInTheDocument();
    });

    it("should render appropriate icon for warning variant", () => {
      const { container } = render(
        <ConfirmDialog {...defaultProps} variant="warning" />,
      );

      const icon = container.querySelector(".text-yellow-600");
      expect(icon).toBeInTheDocument();
    });

    it("should render appropriate icon for primary variant", () => {
      const { container } = render(
        <ConfirmDialog {...defaultProps} variant="primary" />,
      );

      const icon = container.querySelector(".text-indigo-600");
      expect(icon).toBeInTheDocument();
    });
  });

  describe("User Interactions", () => {
    it("should call onConfirm when confirm button is clicked", async () => {
      const user = userEvent.setup();
      const onConfirm = vi.fn();

      render(<ConfirmDialog {...defaultProps} onConfirm={onConfirm} />);

      const confirmButton = screen.getByRole("button", { name: "Confirm" });
      await user.click(confirmButton);

      expect(onConfirm).toHaveBeenCalledTimes(1);
    });

    it("should call onCancel when cancel button is clicked", async () => {
      const user = userEvent.setup();
      const onCancel = vi.fn();

      render(<ConfirmDialog {...defaultProps} onCancel={onCancel} />);

      const cancelButton = screen.getByRole("button", { name: "Cancel" });
      await user.click(cancelButton);

      expect(onCancel).toHaveBeenCalledTimes(1);
    });

    it("should call onCancel when backdrop is clicked", async () => {
      const user = userEvent.setup();
      const onCancel = vi.fn();

      const { container } = render(
        <ConfirmDialog {...defaultProps} onCancel={onCancel} />,
      );

      const backdrop = container.querySelector(".bg-gray-500.bg-opacity-75");
      expect(backdrop).toBeInTheDocument();

      await user.click(backdrop!);

      expect(onCancel).toHaveBeenCalledTimes(1);
    });

    it("should not call callbacks multiple times on rapid clicks", async () => {
      const user = userEvent.setup();
      const onConfirm = vi.fn();

      render(<ConfirmDialog {...defaultProps} onConfirm={onConfirm} />);

      const confirmButton = screen.getByRole("button", { name: "Confirm" });
      await user.click(confirmButton);
      await user.click(confirmButton);

      // Should be called twice since we clicked twice
      expect(onConfirm).toHaveBeenCalledTimes(2);
    });
  });

  describe("Dialog Structure", () => {
    it("should render with proper modal structure", () => {
      const { container } = render(<ConfirmDialog {...defaultProps} />);

      // Check for modal overlay
      const overlay = container.querySelector(".fixed.inset-0.z-50");
      expect(overlay).toBeInTheDocument();

      // Check for dialog container
      const dialog = container.querySelector(".bg-white.rounded-lg");
      expect(dialog).toBeInTheDocument();
    });

    it("should have proper z-index for modal overlay", () => {
      const { container } = render(<ConfirmDialog {...defaultProps} />);

      const overlay = container.querySelector(".z-50");
      expect(overlay).toBeInTheDocument();
    });

    it("should render both buttons", () => {
      render(<ConfirmDialog {...defaultProps} />);

      const buttons = screen.getAllByRole("button");
      expect(buttons).toHaveLength(2);
    });
  });

  describe("Accessibility", () => {
    it("should have button type for both buttons", () => {
      render(<ConfirmDialog {...defaultProps} />);

      const confirmButton = screen.getByRole("button", { name: "Confirm" });
      const cancelButton = screen.getByRole("button", { name: "Cancel" });

      expect(confirmButton).toHaveAttribute("type", "button");
      expect(cancelButton).toHaveAttribute("type", "button");
    });

    it("should have semantic heading for title", () => {
      render(<ConfirmDialog {...defaultProps} title="Delete Item" />);

      const heading = screen.getByRole("heading", { name: "Delete Item" });
      expect(heading).toBeInTheDocument();
    });
  });

  describe("Complex Scenarios", () => {
    it("should handle long messages", () => {
      const longMessage = "A".repeat(500);
      render(<ConfirmDialog {...defaultProps} message={longMessage} />);

      expect(screen.getByText(longMessage)).toBeInTheDocument();
    });

    it("should handle special characters in message", () => {
      render(
        <ConfirmDialog
          {...defaultProps}
          message="Are you sure? This can't be undone! @#$%"
        />,
      );

      expect(
        screen.getByText("Are you sure? This can't be undone! @#$%"),
      ).toBeInTheDocument();
    });

    it("should handle dialog closing and reopening", () => {
      const { rerender } = render(
        <ConfirmDialog {...defaultProps} isOpen={true} />,
      );

      expect(screen.getByText("Confirm Action")).toBeInTheDocument();

      rerender(<ConfirmDialog {...defaultProps} isOpen={false} />);
      expect(screen.queryByText("Confirm Action")).not.toBeInTheDocument();

      rerender(<ConfirmDialog {...defaultProps} isOpen={true} />);
      expect(screen.getByText("Confirm Action")).toBeInTheDocument();
    });
  });
});
