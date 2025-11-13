/**
 * Date utility functions for expiration and warranty date handling
 */

/**
 * Status type for expiration badge
 */
export type ExpirationStatus = "expired" | "expiring-soon" | "valid" | "none";

/**
 * Calculate expiration status based on date
 */
export const getExpirationStatus = (
  expirationDate: string | undefined,
  isExpired: boolean | undefined,
  warningDays: number = 30,
): ExpirationStatus => {
  if (!expirationDate) return "none";
  if (isExpired) return "expired";

  const expDate = new Date(expirationDate);
  const today = new Date();
  const daysUntilExpiration = Math.ceil(
    (expDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24),
  );

  if (daysUntilExpiration < 0) return "expired";
  if (daysUntilExpiration <= warningDays) return "expiring-soon";
  return "valid";
};

/**
 * Calculate days until expiration
 */
export const getDaysUntilExpiration = (
  expirationDate: string | undefined,
): number | null => {
  if (!expirationDate) return null;

  const expDate = new Date(expirationDate);
  const today = new Date();
  return Math.ceil(
    (expDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24),
  );
};

/**
 * Format date to readable string
 */
export const formatExpirationDate = (dateString: string): string => {
  const date = new Date(dateString);
  return date.toLocaleDateString("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
};
