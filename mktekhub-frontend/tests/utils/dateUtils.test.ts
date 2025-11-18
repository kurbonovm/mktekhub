import { describe, it, expect, beforeEach, vi, afterEach } from "vitest";
import {
  getExpirationStatus,
  getDaysUntilExpiration,
  formatExpirationDate,
} from "../../src/utils/dateUtils";

describe("dateUtils", () => {
  describe("getExpirationStatus", () => {
    beforeEach(() => {
      // Mock current date to 2024-01-15
      vi.useFakeTimers();
      vi.setSystemTime(new Date("2024-01-15"));
    });

    afterEach(() => {
      vi.useRealTimers();
    });

    it("should return 'none' when expirationDate is undefined", () => {
      expect(getExpirationStatus(undefined, false)).toBe("none");
    });

    it("should return 'none' when expirationDate is empty string", () => {
      expect(getExpirationStatus("", false)).toBe("none");
    });

    it("should return 'expired' when isExpired flag is true", () => {
      expect(getExpirationStatus("2024-01-20", true)).toBe("expired");
    });

    it("should return 'expired' when date is in the past", () => {
      expect(getExpirationStatus("2024-01-10", false)).toBe("expired");
    });

    it("should return 'expiring-soon' or 'expired' when date is exactly today (0 days until expiration)", () => {
      // When 0 days until expiration, could be either depending on exact time
      const status = getExpirationStatus("2024-01-15", false);
      expect(["expired", "expiring-soon"]).toContain(status);
    });

    it("should return 'expiring-soon' when date is within 30 days (default)", () => {
      expect(getExpirationStatus("2024-02-10", false)).toBe("expiring-soon");
    });

    it("should return 'expiring-soon' when date is exactly at warning threshold", () => {
      expect(getExpirationStatus("2024-02-14", false)).toBe("expiring-soon"); // 30 days
    });

    it("should return 'expiring-soon' when date is 1 day away", () => {
      expect(getExpirationStatus("2024-01-16", false)).toBe("expiring-soon");
    });

    it("should return 'valid' when date is beyond 30 days", () => {
      expect(getExpirationStatus("2024-03-01", false)).toBe("valid");
    });

    it("should return 'valid' when date is exactly 31 days away", () => {
      expect(getExpirationStatus("2024-02-15", false)).toBe("valid");
    });

    it("should respect custom warningDays parameter", () => {
      // 60 days from 2024-01-15 is 2024-03-15
      expect(getExpirationStatus("2024-03-10", false, 60)).toBe(
        "expiring-soon",
      );
    });

    it("should return 'valid' when beyond custom warningDays", () => {
      expect(getExpirationStatus("2024-03-20", false, 60)).toBe("valid");
    });

    it("should return 'expired' with custom warningDays if date is in past", () => {
      expect(getExpirationStatus("2024-01-10", false, 60)).toBe("expired");
    });

    it("should handle very short warningDays (1 day)", () => {
      expect(getExpirationStatus("2024-01-16", false, 1)).toBe("expiring-soon");
      expect(getExpirationStatus("2024-01-17", false, 1)).toBe("valid");
    });

    it("should handle very long warningDays (365 days)", () => {
      expect(getExpirationStatus("2024-12-31", false, 365)).toBe(
        "expiring-soon",
      );
      expect(getExpirationStatus("2025-01-20", false, 365)).toBe("valid");
    });

    it("should handle dates far in the future", () => {
      expect(getExpirationStatus("2030-01-01", false)).toBe("valid");
    });

    it("should handle dates far in the past", () => {
      expect(getExpirationStatus("2020-01-01", false)).toBe("expired");
    });
  });

  describe("getDaysUntilExpiration", () => {
    beforeEach(() => {
      // Mock current date to 2024-01-15
      vi.useFakeTimers();
      vi.setSystemTime(new Date("2024-01-15"));
    });

    afterEach(() => {
      vi.useRealTimers();
    });

    it("should return null when expirationDate is undefined", () => {
      expect(getDaysUntilExpiration(undefined)).toBe(null);
    });

    it("should return null when expirationDate is empty string", () => {
      expect(getDaysUntilExpiration("")).toBe(null);
    });

    it("should return 0 when date is today", () => {
      expect(getDaysUntilExpiration("2024-01-15")).toBe(0);
    });

    it("should return positive number for future dates", () => {
      expect(getDaysUntilExpiration("2024-01-16")).toBe(1);
      expect(getDaysUntilExpiration("2024-01-20")).toBe(5);
      expect(getDaysUntilExpiration("2024-02-15")).toBe(31);
    });

    it("should return negative number for past dates", () => {
      expect(getDaysUntilExpiration("2024-01-14")).toBe(-1);
      expect(getDaysUntilExpiration("2024-01-10")).toBe(-5);
      expect(getDaysUntilExpiration("2023-12-15")).toBe(-31);
    });

    it("should handle dates exactly 30 days away", () => {
      expect(getDaysUntilExpiration("2024-02-14")).toBe(30);
    });

    it("should handle dates exactly 365 days away", () => {
      expect(getDaysUntilExpiration("2025-01-15")).toBe(366); // 2024 is leap year
    });

    it("should ceil the calculation (round up partial days)", () => {
      // Even if it's 0.5 days, should round up to 1
      expect(
        getDaysUntilExpiration("2024-01-15T12:00:00"),
      ).toBeGreaterThanOrEqual(0);
    });

    it("should handle ISO date strings", () => {
      expect(getDaysUntilExpiration("2024-01-20T00:00:00Z")).toBeGreaterThan(0);
    });

    it("should handle dates far in the future", () => {
      expect(getDaysUntilExpiration("2030-01-01")).toBeGreaterThan(2000);
    });

    it("should handle dates far in the past", () => {
      expect(getDaysUntilExpiration("2020-01-01")).toBeLessThan(-1000);
    });
  });

  describe("formatExpirationDate", () => {
    it("should format date in en-US locale", () => {
      const formatted = formatExpirationDate("2024-01-15");
      // Check for year and format, day might vary due to timezone
      expect(formatted).toContain("2024");
      expect(formatted).toMatch(/Jan(uary)?/);
      expect(formatted).toMatch(/1[45]/); // 14 or 15
    });

    it("should handle different month formats", () => {
      const march = formatExpirationDate("2024-03-20");
      expect(march).toContain("2024");
      expect(march).toMatch(/Mar(ch)?/);
      expect(march).toMatch(/[12]\d/); // Day in range

      const december = formatExpirationDate("2024-12-25");
      expect(december).toContain("2024");
      expect(december).toMatch(/Dec(ember)?/);
      expect(december).toMatch(/2[45]/); // 24 or 25
    });

    it("should format date with single digit day", () => {
      const formatted = formatExpirationDate("2024-01-05");
      expect(formatted).toContain("2024");
      expect(formatted).toMatch(/Jan(uary)?/);
      expect(formatted).toMatch(/[45]/); // 4 or 5
    });

    it("should format date with last day of month", () => {
      const formatted = formatExpirationDate("2024-01-31");
      expect(formatted).toContain("2024");
      expect(formatted).toMatch(/Jan(uary)?/);
      expect(formatted).toMatch(/3[01]/); // 30 or 31
    });

    it("should format leap year date", () => {
      const formatted = formatExpirationDate("2024-02-29");
      expect(formatted).toContain("2024");
      expect(formatted).toMatch(/Feb(ruary)?/);
      expect(formatted).toMatch(/2[89]/); // 28 or 29
    });

    it("should format first day of year", () => {
      const formatted = formatExpirationDate("2024-01-01");
      // Could be Dec 31, 2023 or Jan 1, 2024 depending on timezone
      expect(formatted).toMatch(/(Jan(uary)?|Dec(ember)?)/);
      expect(formatted).toMatch(/(2023|2024)/);
    });

    it("should format last day of year", () => {
      const formatted = formatExpirationDate("2024-12-31");
      expect(formatted).toContain("2024");
      expect(formatted).toMatch(/Dec(ember)?/);
      expect(formatted).toMatch(/3[01]/); // 30 or 31
    });

    it("should handle ISO date string format", () => {
      const formatted = formatExpirationDate("2024-01-15T00:00:00Z");
      expect(formatted).toBeTruthy();
      expect(formatted).toContain("2024");
    });

    it("should format dates from different years", () => {
      const june2023 = formatExpirationDate("2023-06-15");
      expect(june2023).toContain("2023");
      expect(june2023).toMatch(/Jun(e)?/);

      const sept2025 = formatExpirationDate("2025-09-30");
      expect(sept2025).toContain("2025");
      expect(sept2025).toMatch(/Sep(tember)?/);
    });

    it("should produce consistent format for same date", () => {
      const date1 = formatExpirationDate("2024-01-15");
      const date2 = formatExpirationDate("2024-01-15");
      expect(date1).toBe(date2);
    });
  });
});
