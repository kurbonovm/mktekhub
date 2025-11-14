interface SkeletonProps {
  className?: string;
  variant?: "text" | "card" | "table" | "circle";
  count?: number;
}

export const Skeleton = ({
  className = "",
  variant = "text",
  count = 1,
}: SkeletonProps) => {
  const baseClasses = "animate-pulse bg-gray-300 rounded";

  const variantClasses = {
    text: "h-4 w-full",
    card: "h-48 w-full",
    table: "h-12 w-full",
    circle: "h-12 w-12 rounded-full",
  };

  const skeletonClass = `${baseClasses} ${variantClasses[variant]} ${className}`;

  if (count === 1) {
    return <div className={skeletonClass} />;
  }

  return (
    <>
      {Array.from({ length: count }).map((_, index) => (
        <div key={index} className={skeletonClass} />
      ))}
    </>
  );
};

export const TableSkeleton = ({ rows = 5 }: { rows?: number }) => {
  return (
    <div className="space-y-2">
      {Array.from({ length: rows }).map((_, index) => (
        <Skeleton key={index} variant="table" />
      ))}
    </div>
  );
};

export const CardSkeleton = ({ cards = 3 }: { cards?: number }) => {
  return (
    <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
      {Array.from({ length: cards }).map((_, index) => (
        <div key={index} className="rounded-lg bg-white p-6 shadow">
          <div className="space-y-4">
            <Skeleton variant="text" className="h-6 w-3/4" />
            <Skeleton variant="text" className="h-4 w-1/2" />
            <Skeleton variant="text" className="h-4 w-full" />
            <Skeleton variant="text" className="h-4 w-full" />
            <Skeleton variant="text" className="h-2 w-full" />
          </div>
        </div>
      ))}
    </div>
  );
};
