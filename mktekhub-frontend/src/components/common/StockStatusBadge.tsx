interface StockStatusBadgeProps {
  quantity: number;
  reorderLevel?: number;
}

export const StockStatusBadge = ({
  quantity,
  reorderLevel = 0,
}: StockStatusBadgeProps) => {
  // Determine stock status based on quantity and reorder level
  const getStockStatus = () => {
    if (quantity === 0) {
      return {
        label: "Out of Stock",
        icon: (
          <svg
            className="h-4 w-4"
            fill="currentColor"
            viewBox="0 0 20 20"
            aria-hidden="true"
          >
            <path
              fillRule="evenodd"
              d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z"
              clipRule="evenodd"
            />
          </svg>
        ),
        bgColor: "bg-red-100",
        textColor: "text-red-800",
      };
    }

    if (quantity <= reorderLevel) {
      return {
        label: "Low Stock",
        icon: (
          <svg
            className="h-4 w-4"
            fill="currentColor"
            viewBox="0 0 20 20"
            aria-hidden="true"
          >
            <path
              fillRule="evenodd"
              d="M8.485 2.495c.673-1.167 2.357-1.167 3.03 0l6.28 10.875c.673 1.167-.17 2.625-1.516 2.625H3.72c-1.347 0-2.189-1.458-1.515-2.625L8.485 2.495zM10 5a.75.75 0 01.75.75v3.5a.75.75 0 01-1.5 0v-3.5A.75.75 0 0110 5zm0 9a1 1 0 100-2 1 1 0 000 2z"
              clipRule="evenodd"
            />
          </svg>
        ),
        bgColor: "bg-amber-100",
        textColor: "text-amber-800",
      };
    }

    // Good stock level
    return {
      label: "In Stock",
      icon: (
        <svg
          className="h-4 w-4"
          fill="currentColor"
          viewBox="0 0 20 20"
          aria-hidden="true"
        >
          <path
            fillRule="evenodd"
            d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z"
            clipRule="evenodd"
          />
        </svg>
      ),
      bgColor: "bg-green-100",
      textColor: "text-green-800",
    };
  };

  const status = getStockStatus();

  return (
    <span
      className={`inline-flex items-center gap-1 rounded-full ${status.bgColor} px-2.5 py-0.5 text-xs font-semibold ${status.textColor}`}
    >
      {status.icon}
      {status.label}
    </span>
  );
};
