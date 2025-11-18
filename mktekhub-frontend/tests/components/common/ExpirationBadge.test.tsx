import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import {
  ExpirationBadge,
  WarrantyBadge,
} from "@/components/common/ExpirationBadge";

// Mock date utilities
vi.mock("@/utils/dateUtils", () => ({
  getExpirationStatus: vi.fn((date, isExpired, warningDays) => {
    if (!date) return "none";
    if (isExpired) return "expired";

    const today = new Date();
    const expDate = new Date(date);
    const daysUntil = Math.ceil(
      (expDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24),
    );

    if (daysUntil < 0) return "expired";
    if (daysUntil <= warningDays) return "expiring-soon";
    return "valid";
  }),
  getDaysUntilExpiration: vi.fn((date) => {
    if (!date) return null;
    const today = new Date();
    const expDate = new Date(date);
    return Math.ceil(
      (expDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24),
    );
  }),
  formatExpirationDate: vi.fn((date) => {
    if (!date) return "";
    return new Date(date).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  }),
}));

describe("ExpirationBadge", () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  describe("No Expiration Date", () => {
    it('should display "No expiration" when no date is provided', () => {
      render(<ExpirationBadge />);

      expect(screen.getByText("No expiration")).toBeInTheDocument();
    });

    it("should have gray styling for no expiration", () => {
      render(<ExpirationBadge />);

      const element = screen.getByText("No expiration");
      expect(element.className).toContain("text-gray-400");
    });
  });

  describe("Expired Items", () => {
    it('should display "Expired" badge when isExpired is true', () => {
      render(<ExpirationBadge expirationDate="2024-01-01" isExpired={true} />);

      expect(screen.getByText("Expired")).toBeInTheDocument();
    });

    it('should display "Expired" badge when date is in the past', () => {
      const pastDate = new Date(Date.now() - 10 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split("T")[0];

      render(<ExpirationBadge expirationDate={pastDate} />);

      expect(screen.getByText("Expired")).toBeInTheDocument();
    });

    it("should have red styling for expired items", () => {
      render(<ExpirationBadge expirationDate="2024-01-01" isExpired={true} />);

      const badge = screen.getByText("Expired");
      expect(badge.className).toContain("bg-red-100");
      expect(badge.className).toContain("text-red-800");
    });

    it("should display formatted expiration date for expired items", () => {
      render(<ExpirationBadge expirationDate="2024-01-15" isExpired={true} />);

      // The mock formatExpirationDate will be called
      expect(screen.getByText("Expired")).toBeInTheDocument();
    });

    it("should render icon for expired items", () => {
      const { container } = render(
        <ExpirationBadge expirationDate="2024-01-01" isExpired={true} />,
      );

      const icon = container.querySelector("svg");
      expect(icon).toBeInTheDocument();
    });
  });

  describe("Expiring Soon", () => {
    it('should display "Expiring soon" badge when within warning days', () => {
      const soonDate = new Date(Date.now() + 15 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split("T")[0];

      render(<ExpirationBadge expirationDate={soonDate} warningDays={30} />);

      // Should show days left
      const badge = screen.getByText(/\d+ days? left/);
      expect(badge).toBeInTheDocument();
    });

    it("should display days remaining for expiring soon items", () => {
      const soonDate = new Date(Date.now() + 15 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split("T")[0];

      render(<ExpirationBadge expirationDate={soonDate} warningDays={30} />);

      expect(screen.getByText(/15 days left/)).toBeInTheDocument();
    });

    it('should use singular "day" for 1 day remaining', () => {
      const tomorrow = new Date(Date.now() + 1 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split("T")[0];

      render(<ExpirationBadge expirationDate={tomorrow} warningDays={30} />);

      expect(screen.getByText(/1 day left/)).toBeInTheDocument();
    });

    it("should have yellow styling for expiring soon items", () => {
      const soonDate = new Date(Date.now() + 15 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split("T")[0];

      render(<ExpirationBadge expirationDate={soonDate} warningDays={30} />);

      const badge = screen.getByText(/days left/);
      expect(badge.className).toContain("bg-yellow-100");
      expect(badge.className).toContain("text-yellow-800");
    });

    it("should use custom warning days threshold", () => {
      const date60DaysAway = new Date(Date.now() + 60 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split("T")[0];

      render(
        <ExpirationBadge expirationDate={date60DaysAway} warningDays={90} />,
      );

      // Should show as expiring soon because it's within 90 days
      expect(screen.getByText(/days left/)).toBeInTheDocument();
    });

    it("should display formatted date for expiring soon items", () => {
      const soonDate = new Date(Date.now() + 15 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split("T")[0];

      const { container } = render(
        <ExpirationBadge expirationDate={soonDate} warningDays={30} />,
      );

      const dateText = container.querySelector(".text-gray-500");
      expect(dateText).toBeInTheDocument();
    });
  });

  describe("Valid Expiration", () => {
    it("should display valid status for dates beyond warning threshold", () => {
      const futureDate = new Date(Date.now() + 100 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split("T")[0];

      const { container } = render(
        <ExpirationBadge expirationDate={futureDate} warningDays={30} />,
      );

      // Should show checkmark icon for valid items
      const checkIcon = container.querySelector("svg");
      expect(checkIcon).toBeInTheDocument();
    });

    it("should display formatted date for valid items", () => {
      const futureDate = new Date(Date.now() + 100 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split("T")[0];

      const { container } = render(
        <ExpirationBadge expirationDate={futureDate} warningDays={30} />,
      );

      const dateText = container.querySelector(".text-gray-600");
      expect(dateText).toBeInTheDocument();
    });

    it("should have minimal styling for valid items", () => {
      const futureDate = new Date(Date.now() + 100 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split("T")[0];

      const { container } = render(
        <ExpirationBadge expirationDate={futureDate} warningDays={30} />,
      );

      const wrapper = container.querySelector(".text-gray-700");
      expect(wrapper).toBeInTheDocument();
    });
  });

  describe("Edge Cases", () => {
    it("should use default warning days of 30 when not provided", () => {
      const date25DaysAway = new Date(Date.now() + 25 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split("T")[0];

      render(<ExpirationBadge expirationDate={date25DaysAway} />);

      // Should show as expiring soon with default 30 days
      expect(screen.getByText(/days left/)).toBeInTheDocument();
    });

    it("should handle invalid date strings gracefully", () => {
      const { container } = render(
        <ExpirationBadge expirationDate="invalid-date" />,
      );

      // Should still render the component without crashing
      expect(container).toBeInTheDocument();
    });

    it("should handle dates exactly on the warning threshold", () => {
      const date30DaysAway = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split("T")[0];

      render(
        <ExpirationBadge expirationDate={date30DaysAway} warningDays={30} />,
      );

      expect(screen.getByText(/30 days left/)).toBeInTheDocument();
    });
  });
});

describe("WarrantyBadge", () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  describe("No Warranty", () => {
    it('should display "No warranty" when no date is provided', () => {
      render(<WarrantyBadge />);

      expect(screen.getByText("No warranty")).toBeInTheDocument();
    });

    it("should have gray styling for no warranty", () => {
      render(<WarrantyBadge />);

      const element = screen.getByText("No warranty");
      expect(element.className).toContain("text-gray-400");
    });
  });

  describe("Expired Warranty", () => {
    it('should display "Expired" when isWarrantyValid is false', () => {
      render(
        <WarrantyBadge warrantyEndDate="2024-01-01" isWarrantyValid={false} />,
      );

      expect(screen.getByText("Expired")).toBeInTheDocument();
    });

    it('should display "Expired" when date is in the past', () => {
      const pastDate = new Date(Date.now() - 10 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split("T")[0];

      render(<WarrantyBadge warrantyEndDate={pastDate} />);

      expect(screen.getByText("Expired")).toBeInTheDocument();
    });

    it("should have gray styling for expired warranty", () => {
      render(
        <WarrantyBadge warrantyEndDate="2024-01-01" isWarrantyValid={false} />,
      );

      const badge = screen.getByText("Expired");
      expect(badge.className).toContain("bg-gray-100");
      expect(badge.className).toContain("text-gray-800");
    });
  });

  describe("Warranty Expiring Soon", () => {
    it("should display warning when warranty expires within 60 days", () => {
      const soonDate = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split("T")[0];

      render(
        <WarrantyBadge warrantyEndDate={soonDate} isWarrantyValid={true} />,
      );

      expect(screen.getByText(/30 days left/)).toBeInTheDocument();
    });

    it('should use singular "day" for 1 day remaining', () => {
      const tomorrow = new Date(Date.now() + 1 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split("T")[0];

      render(
        <WarrantyBadge warrantyEndDate={tomorrow} isWarrantyValid={true} />,
      );

      expect(screen.getByText(/1 day left/)).toBeInTheDocument();
    });

    it("should have yellow styling for expiring warranty", () => {
      const soonDate = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split("T")[0];

      render(
        <WarrantyBadge warrantyEndDate={soonDate} isWarrantyValid={true} />,
      );

      const badge = screen.getByText(/days left/);
      expect(badge.className).toContain("bg-yellow-100");
      expect(badge.className).toContain("text-yellow-800");
    });

    it("should show warning at exactly 60 days", () => {
      const date60DaysAway = new Date(Date.now() + 60 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split("T")[0];

      render(
        <WarrantyBadge
          warrantyEndDate={date60DaysAway}
          isWarrantyValid={true}
        />,
      );

      expect(screen.getByText(/60 days left/)).toBeInTheDocument();
    });
  });

  describe("Valid Warranty", () => {
    it("should display valid status for warranty beyond 60 days", () => {
      const futureDate = new Date(Date.now() + 100 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split("T")[0];

      const { container } = render(
        <WarrantyBadge warrantyEndDate={futureDate} isWarrantyValid={true} />,
      );

      // Should show shield icon for valid warranty
      const shieldIcon = container.querySelector("svg");
      expect(shieldIcon).toBeInTheDocument();
    });

    it("should display formatted date for valid warranty", () => {
      const futureDate = new Date(Date.now() + 100 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split("T")[0];

      const { container } = render(
        <WarrantyBadge warrantyEndDate={futureDate} isWarrantyValid={true} />,
      );

      const dateText = container.querySelector(".text-gray-600");
      expect(dateText).toBeInTheDocument();
    });

    it("should have minimal styling for valid warranty", () => {
      const futureDate = new Date(Date.now() + 100 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split("T")[0];

      const { container } = render(
        <WarrantyBadge warrantyEndDate={futureDate} isWarrantyValid={true} />,
      );

      const wrapper = container.querySelector(".text-gray-700");
      expect(wrapper).toBeInTheDocument();
    });
  });

  describe("Edge Cases", () => {
    it("should handle warranty at exactly the threshold (61 days)", () => {
      const date61DaysAway = new Date(Date.now() + 61 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split("T")[0];

      const { container } = render(
        <WarrantyBadge
          warrantyEndDate={date61DaysAway}
          isWarrantyValid={true}
        />,
      );

      // Should show as valid (beyond 60 day threshold)
      const shieldIcon = container.querySelector("svg");
      expect(shieldIcon).toBeInTheDocument();
    });

    it("should prioritize isWarrantyValid prop over date calculation", () => {
      const futureDate = new Date(Date.now() + 100 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split("T")[0];

      render(
        <WarrantyBadge warrantyEndDate={futureDate} isWarrantyValid={false} />,
      );

      expect(screen.getByText("Expired")).toBeInTheDocument();
    });
  });
});
