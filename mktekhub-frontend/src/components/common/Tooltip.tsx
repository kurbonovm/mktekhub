import React, { useState } from "react";

/**
 * Props for Tooltip component
 */
interface TooltipProps {
  /** The content to display in the tooltip */
  content: string;
  /** Children elements that trigger the tooltip */
  children: React.ReactNode;
  /** Tooltip position (default: "top") */
  position?: "top" | "bottom" | "left" | "right";
  /** Additional CSS classes */
  className?: string;
}

/**
 * Tooltip Component
 * Displays helpful information on hover
 *
 * @example
 * ```tsx
 * <Tooltip content="Click to add new item" position="top">
 *   <button>Add Item</button>
 * </Tooltip>
 * ```
 */
export const Tooltip: React.FC<TooltipProps> = ({
  content,
  children,
  position = "top",
  className = "",
}) => {
  const [isVisible, setIsVisible] = useState(false);

  const getPositionStyles = () => {
    switch (position) {
      case "top":
        return "bottom-full left-1/2 -translate-x-1/2 mb-2";
      case "bottom":
        return "top-full left-1/2 -translate-x-1/2 mt-2";
      case "left":
        return "right-full top-1/2 -translate-y-1/2 mr-2";
      case "right":
        return "left-full top-1/2 -translate-y-1/2 ml-2";
      default:
        return "bottom-full left-1/2 -translate-x-1/2 mb-2";
    }
  };

  const getArrowStyles = () => {
    switch (position) {
      case "top":
        return "top-full left-1/2 -translate-x-1/2 border-t-gray-900 border-l-transparent border-r-transparent border-b-transparent";
      case "bottom":
        return "bottom-full left-1/2 -translate-x-1/2 border-b-gray-900 border-l-transparent border-r-transparent border-t-transparent";
      case "left":
        return "left-full top-1/2 -translate-y-1/2 border-l-gray-900 border-t-transparent border-b-transparent border-r-transparent";
      case "right":
        return "right-full top-1/2 -translate-y-1/2 border-r-gray-900 border-t-transparent border-b-transparent border-l-transparent";
      default:
        return "top-full left-1/2 -translate-x-1/2 border-t-gray-900 border-l-transparent border-r-transparent border-b-transparent";
    }
  };

  return (
    <div
      className={`relative inline-block ${className}`}
      onMouseEnter={() => setIsVisible(true)}
      onMouseLeave={() => setIsVisible(false)}
    >
      {children}
      {isVisible && content && (
        <div
          className={`pointer-events-none absolute z-50 ${getPositionStyles()}`}
          role="tooltip"
        >
          <div className="relative whitespace-nowrap rounded-md bg-gray-900 px-3 py-2 text-sm text-white shadow-lg">
            {content}
            <div className={`absolute h-0 w-0 border-4 ${getArrowStyles()}`} />
          </div>
        </div>
      )}
    </div>
  );
};
