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

  // Valid expiration date
  return (
    <div className="flex flex-col">
      <span className="inline-flex items-center rounded-full bg-green-100 px-2.5 py-0.5 text-xs font-semibold text-green-800">
        <svg
          className="mr-1 h-3 w-3"
          fill="currentColor"
          viewBox="0 0 20 20"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            fillRule="evenodd"
            d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
            clipRule="evenodd"
          />
        </svg>
        Valid
      </span>
      <span className="mt-1 text-xs text-gray-500">{formattedDate}</span>
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

  // Valid warranty
  return (
    <div className="flex flex-col">
      <span className="inline-flex items-center rounded-full bg-blue-100 px-2.5 py-0.5 text-xs font-semibold text-blue-800">
        <svg
          className="mr-1 h-3 w-3"
          fill="currentColor"
          viewBox="0 0 20 20"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            fillRule="evenodd"
            d="M2.166 4.999A11.954 11.954 0 0010 1.944 11.954 11.954 0 0017.834 5c.11.65.166 1.32.166 2.001 0 5.225-3.34 9.67-8 11.317C5.34 16.67 2 12.225 2 7c0-.682.057-1.35.166-2.001zm11.541 3.708a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
            clipRule="evenodd"
          />
        </svg>
        Under Warranty
      </span>
      <span className="mt-1 text-xs text-gray-500">{formattedDate}</span>
    </div>
  );
};
