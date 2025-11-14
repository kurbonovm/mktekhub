import React from "react";
import {
  getExpirationStatus,
  getDaysUntilExpiration,
  formatExpirationDate,
} from "../../utils/dateUtils";

/**
 * Props for ExpirationBadge component
 */
interface ExpirationBadgeProps {
  /** Expiration date string (ISO format) */
  expirationDate?: string;
  /** Whether the item is marked as expired */
  isExpired?: boolean;
  /** Number of days before expiration to show "expiring soon" (default: 30) */
  warningDays?: number;
}

/**
 * ExpirationBadge component displays the expiration status of an inventory item
 *
 * @component
 * @example
 * ```tsx
 * <ExpirationBadge
 *   expirationDate="2024-12-31"
 *   isExpired={false}
 *   warningDays={30}
 * />
 * ```
 */
export const ExpirationBadge: React.FC<ExpirationBadgeProps> = ({
  expirationDate,
  isExpired,
  warningDays = 30,
}) => {
  const status = getExpirationStatus(expirationDate, isExpired, warningDays);

  if (status === "none") {
    return <span className="text-sm text-gray-400">No expiration</span>;
  }

  const daysUntil = getDaysUntilExpiration(expirationDate);
  const formattedDate = expirationDate
    ? formatExpirationDate(expirationDate)
    : "";

  // Expired items
  if (status === "expired") {
    return (
      <div className="flex flex-col">
        <span className="inline-flex items-center rounded-full bg-red-100 px-2.5 py-0.5 text-xs font-semibold text-red-800">
          <svg
            className="mr-1 h-3 w-3"
            fill="currentColor"
            viewBox="0 0 20 20"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              fillRule="evenodd"
              d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
              clipRule="evenodd"
            />
          </svg>
          Expired
        </span>
        <span className="mt-1 text-xs text-gray-500">{formattedDate}</span>
      </div>
    );
  }

  // Expiring soon
  if (status === "expiring-soon") {
    return (
      <div className="flex flex-col">
        <span className="inline-flex items-center rounded-full bg-yellow-100 px-2.5 py-0.5 text-xs font-semibold text-yellow-800">
          <svg
            className="mr-1 h-3 w-3"
            fill="currentColor"
            viewBox="0 0 20 20"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              fillRule="evenodd"
              d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"
              clipRule="evenodd"
            />
          </svg>
          {daysUntil !== null && daysUntil > 0
            ? `${daysUntil} day${daysUntil !== 1 ? "s" : ""} left`
            : "Expiring soon"}
        </span>
        <span className="mt-1 text-xs text-gray-500">{formattedDate}</span>
      </div>
    );
  }

  // Valid expiration date - minimal styling for normal state
  return (
    <div className="flex items-center text-sm text-gray-700">
      <svg
        className="mr-1.5 h-4 w-4 text-gray-400"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M5 13l4 4L19 7"
        />
      </svg>
      <span className="text-xs text-gray-600">{formattedDate}</span>
    </div>
  );
};

/**
 * WarrantyBadge component displays the warranty status of an inventory item
 */
interface WarrantyBadgeProps {
  /** Warranty end date string (ISO format) */
  warrantyEndDate?: string;
  /** Whether warranty is valid */
  isWarrantyValid?: boolean;
}

/**
 * WarrantyBadge component displays warranty status
 *
 * @component
 * @example
 * ```tsx
 * <WarrantyBadge
 *   warrantyEndDate="2025-12-31"
 *   isWarrantyValid={true}
 * />
 * ```
 */
export const WarrantyBadge: React.FC<WarrantyBadgeProps> = ({
  warrantyEndDate,
  isWarrantyValid,
}) => {
  if (!warrantyEndDate) {
    return <span className="text-sm text-gray-400">No warranty</span>;
  }

  const formattedDate = formatExpirationDate(warrantyEndDate);
  const daysUntil = getDaysUntilExpiration(warrantyEndDate);

  if (!isWarrantyValid || (daysUntil !== null && daysUntil < 0)) {
    return (
      <div className="flex flex-col">
        <span className="inline-flex items-center rounded-full bg-gray-100 px-2.5 py-0.5 text-xs font-semibold text-gray-800">
          <svg
            className="mr-1 h-3 w-3"
            fill="currentColor"
            viewBox="0 0 20 20"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              fillRule="evenodd"
              d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
              clipRule="evenodd"
            />
          </svg>
          Expired
        </span>
        <span className="mt-1 text-xs text-gray-500">{formattedDate}</span>
      </div>
    );
  }

  // Warranty expiring within 60 days
  if (daysUntil !== null && daysUntil <= 60) {
    return (
      <div className="flex flex-col">
        <span className="inline-flex items-center rounded-full bg-yellow-100 px-2.5 py-0.5 text-xs font-semibold text-yellow-800">
          <svg
            className="mr-1 h-3 w-3"
            fill="currentColor"
            viewBox="0 0 20 20"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              fillRule="evenodd"
              d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z"
              clipRule="evenodd"
            />
          </svg>
          {daysUntil} day{daysUntil !== 1 ? "s" : ""} left
        </span>
        <span className="mt-1 text-xs text-gray-500">{formattedDate}</span>
      </div>
    );
  }

  // Valid warranty - minimal styling for normal state
  return (
    <div className="flex items-center text-sm text-gray-700">
      <svg
        className="mr-1.5 h-4 w-4 text-gray-400"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"
        />
      </svg>
      <span className="text-xs text-gray-600">{formattedDate}</span>
    </div>
  );
};
