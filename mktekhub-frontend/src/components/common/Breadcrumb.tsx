import React from "react";
import { Link, useLocation } from "react-router-dom";

/**
 * Breadcrumb item interface
 */
export interface BreadcrumbItem {
  label: string;
  path?: string;
}

/**
 * Props for Breadcrumb component
 */
interface BreadcrumbProps {
  /** Array of breadcrumb items */
  items?: BreadcrumbItem[];
  /** Auto-generate breadcrumbs from current path */
  autoGenerate?: boolean;
}

/**
 * Breadcrumb Navigation Component
 * Provides hierarchical navigation for better UX
 *
 * @example
 * ```tsx
 * // Auto-generate from URL path
 * <Breadcrumb autoGenerate />
 *
 * // Manual breadcrumb items
 * <Breadcrumb
 *   items={[
 *     { label: "Dashboard", path: "/dashboard" },
 *     { label: "Inventory", path: "/inventory" },
 *     { label: "Item Details" }
 *   ]}
 * />
 * ```
 */
export const Breadcrumb: React.FC<BreadcrumbProps> = ({
  items,
  autoGenerate = false,
}) => {
  const location = useLocation();

  // Generate breadcrumbs from current path
  const generateBreadcrumbs = (): BreadcrumbItem[] => {
    const pathnames = location.pathname.split("/").filter((x) => x);

    const breadcrumbs: BreadcrumbItem[] = [
      { label: "Home", path: "/dashboard" },
    ];

    // Map path segments to readable labels
    const pathLabels: Record<string, string> = {
      dashboard: "Dashboard",
      inventory: "Inventory",
      warehouses: "Warehouses",
      users: "Users",
      reports: "Reports",
      settings: "Settings",
    };

    let currentPath = "";
    pathnames.forEach((segment, index) => {
      currentPath += `/${segment}`;
      const isLast = index === pathnames.length - 1;

      breadcrumbs.push({
        label:
          pathLabels[segment] ||
          segment.charAt(0).toUpperCase() + segment.slice(1),
        path: isLast ? undefined : currentPath,
      });
    });

    return breadcrumbs;
  };

  const breadcrumbItems = autoGenerate ? generateBreadcrumbs() : items || [];

  if (breadcrumbItems.length === 0) {
    return null;
  }

  return (
    <nav className="mb-4 flex" aria-label="Breadcrumb">
      <ol className="inline-flex items-center space-x-1 md:space-x-3">
        {breadcrumbItems.map((item, index) => (
          <li key={index} className="inline-flex items-center">
            {index > 0 && (
              <svg
                className="mx-1 h-3 w-3 text-gray-400"
                aria-hidden="true"
                fill="none"
                viewBox="0 0 6 10"
              >
                <path
                  stroke="currentColor"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="m1 9 4-4-4-4"
                />
              </svg>
            )}
            {item.path ? (
              <Link
                to={item.path}
                className="inline-flex items-center text-sm font-medium text-gray-700 hover:text-indigo-600"
              >
                {index === 0 && (
                  <svg
                    className="mr-2 h-4 w-4"
                    fill="currentColor"
                    viewBox="0 0 20 20"
                    xmlns="http://www.w3.org/2000/svg"
                  >
                    <path d="M10.707 2.293a1 1 0 00-1.414 0l-7 7a1 1 0 001.414 1.414L4 10.414V17a1 1 0 001 1h2a1 1 0 001-1v-2a1 1 0 011-1h2a1 1 0 011 1v2a1 1 0 001 1h2a1 1 0 001-1v-6.586l.293.293a1 1 0 001.414-1.414l-7-7z" />
                  </svg>
                )}
                {item.label}
              </Link>
            ) : (
              <span className="inline-flex items-center text-sm font-medium text-gray-500">
                {index === 0 && (
                  <svg
                    className="mr-2 h-4 w-4"
                    fill="currentColor"
                    viewBox="0 0 20 20"
                    xmlns="http://www.w3.org/2000/svg"
                  >
                    <path d="M10.707 2.293a1 1 0 00-1.414 0l-7 7a1 1 0 001.414 1.414L4 10.414V17a1 1 0 001 1h2a1 1 0 001-1v-2a1 1 0 011-1h2a1 1 0 011 1v2a1 1 0 001 1h2a1 1 0 001-1v-6.586l.293.293a1 1 0 001.414-1.414l-7-7z" />
                  </svg>
                )}
                {item.label}
              </span>
            )}
          </li>
        ))}
      </ol>
    </nav>
  );
};
