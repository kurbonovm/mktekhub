import { describe, it, expect } from "vitest";
import { render } from "@testing-library/react";
import {
  Skeleton,
  TableSkeleton,
  CardSkeleton,
} from "@/components/common/Skeleton";

describe("Skeleton", () => {
  describe("Basic Rendering", () => {
    it("should render a single skeleton by default", () => {
      const { container } = render(<Skeleton />);

      const skeletons = container.querySelectorAll(".animate-pulse");
      expect(skeletons).toHaveLength(1);
    });

    it("should apply animation class", () => {
      const { container } = render(<Skeleton />);

      const skeleton = container.querySelector(".animate-pulse");
      expect(skeleton).toBeInTheDocument();
    });

    it("should apply background color class", () => {
      const { container } = render(<Skeleton />);

      const skeleton = container.querySelector(".bg-gray-300");
      expect(skeleton).toBeInTheDocument();
    });

    it("should apply rounded class", () => {
      const { container } = render(<Skeleton />);

      const skeleton = container.querySelector(".rounded");
      expect(skeleton).toBeInTheDocument();
    });

    it("should apply custom className", () => {
      const { container } = render(<Skeleton className="custom-class" />);

      const skeleton = container.querySelector(".custom-class");
      expect(skeleton).toBeInTheDocument();
    });
  });

  describe("Variants", () => {
    it("should render text variant by default", () => {
      const { container } = render(<Skeleton />);

      const skeleton = container.querySelector(".h-4.w-full");
      expect(skeleton).toBeInTheDocument();
    });

    it("should render text variant with correct classes", () => {
      const { container } = render(<Skeleton variant="text" />);

      const skeleton = container.querySelector(".h-4.w-full");
      expect(skeleton).toBeInTheDocument();
    });

    it("should render card variant with correct classes", () => {
      const { container } = render(<Skeleton variant="card" />);

      const skeleton = container.querySelector(".h-48.w-full");
      expect(skeleton).toBeInTheDocument();
    });

    it("should render table variant with correct classes", () => {
      const { container } = render(<Skeleton variant="table" />);

      const skeleton = container.querySelector(".h-12.w-full");
      expect(skeleton).toBeInTheDocument();
    });

    it("should render circle variant with correct classes", () => {
      const { container } = render(<Skeleton variant="circle" />);

      const skeleton = container.querySelector(".h-12.w-12.rounded-full");
      expect(skeleton).toBeInTheDocument();
    });
  });

  describe("Count Prop", () => {
    it("should render single skeleton when count is 1", () => {
      const { container } = render(<Skeleton count={1} />);

      const skeletons = container.querySelectorAll(".animate-pulse");
      expect(skeletons).toHaveLength(1);
    });

    it("should render multiple skeletons when count is greater than 1", () => {
      const { container } = render(<Skeleton count={3} />);

      const skeletons = container.querySelectorAll(".animate-pulse");
      expect(skeletons).toHaveLength(3);
    });

    it("should render 5 skeletons", () => {
      const { container } = render(<Skeleton count={5} />);

      const skeletons = container.querySelectorAll(".animate-pulse");
      expect(skeletons).toHaveLength(5);
    });

    it("should render 10 skeletons", () => {
      const { container } = render(<Skeleton count={10} />);

      const skeletons = container.querySelectorAll(".animate-pulse");
      expect(skeletons).toHaveLength(10);
    });
  });

  describe("Combined Props", () => {
    it("should combine variant and custom className", () => {
      const { container } = render(
        <Skeleton variant="card" className="mb-4" />,
      );

      const skeleton = container.querySelector(".h-48.mb-4");
      expect(skeleton).toBeInTheDocument();
    });

    it("should combine count and variant", () => {
      const { container } = render(<Skeleton variant="circle" count={3} />);

      const skeletons = container.querySelectorAll(".rounded-full");
      expect(skeletons).toHaveLength(3);
    });

    it("should combine all props", () => {
      const { container } = render(
        <Skeleton variant="table" count={2} className="my-2" />,
      );

      const skeletons = container.querySelectorAll(".h-12.my-2");
      expect(skeletons).toHaveLength(2);
    });
  });
});

describe("TableSkeleton", () => {
  describe("Rendering", () => {
    it("should render 5 rows by default", () => {
      const { container } = render(<TableSkeleton />);

      const rows = container.querySelectorAll(".animate-pulse");
      expect(rows).toHaveLength(5);
    });

    it("should render custom number of rows", () => {
      const { container } = render(<TableSkeleton rows={10} />);

      const rows = container.querySelectorAll(".animate-pulse");
      expect(rows).toHaveLength(10);
    });

    it("should render single row", () => {
      const { container } = render(<TableSkeleton rows={1} />);

      const rows = container.querySelectorAll(".animate-pulse");
      expect(rows).toHaveLength(1);
    });

    it("should have spacing between rows", () => {
      const { container } = render(<TableSkeleton />);

      const wrapper = container.querySelector(".space-y-2");
      expect(wrapper).toBeInTheDocument();
    });

    it("should render table variant skeletons", () => {
      const { container } = render(<TableSkeleton />);

      const tableSkeletons = container.querySelectorAll(".h-12");
      expect(tableSkeletons.length).toBeGreaterThan(0);
    });
  });

  describe("Edge Cases", () => {
    it("should handle 0 rows", () => {
      const { container } = render(<TableSkeleton rows={0} />);

      const rows = container.querySelectorAll(".animate-pulse");
      expect(rows).toHaveLength(0);
    });

    it("should handle large number of rows", () => {
      const { container } = render(<TableSkeleton rows={50} />);

      const rows = container.querySelectorAll(".animate-pulse");
      expect(rows).toHaveLength(50);
    });
  });
});

describe("CardSkeleton", () => {
  describe("Rendering", () => {
    it("should render 3 cards by default", () => {
      const { container } = render(<CardSkeleton />);

      const cards = container.querySelectorAll(
        ".rounded-lg.bg-white.p-6.shadow",
      );
      expect(cards).toHaveLength(3);
    });

    it("should render custom number of cards", () => {
      const { container } = render(<CardSkeleton cards={5} />);

      const cards = container.querySelectorAll(
        ".rounded-lg.bg-white.p-6.shadow",
      );
      expect(cards).toHaveLength(5);
    });

    it("should render single card", () => {
      const { container } = render(<CardSkeleton cards={1} />);

      const cards = container.querySelectorAll(
        ".rounded-lg.bg-white.p-6.shadow",
      );
      expect(cards).toHaveLength(1);
    });

    it("should use grid layout", () => {
      const { container } = render(<CardSkeleton />);

      const grid = container.querySelector(".grid");
      expect(grid).toBeInTheDocument();
    });

    it("should have responsive grid columns", () => {
      const { container } = render(<CardSkeleton />);

      const grid = container.querySelector(
        ".md\\:grid-cols-2.lg\\:grid-cols-3",
      );
      expect(grid).toBeInTheDocument();
    });

    it("should render multiple skeleton lines per card", () => {
      const { container } = render(<CardSkeleton cards={1} />);

      const skeletons = container.querySelectorAll(".animate-pulse");
      expect(skeletons.length).toBeGreaterThan(1);
    });

    it("should have varied skeleton widths", () => {
      const { container } = render(<CardSkeleton cards={1} />);

      const width34 = container.querySelector(".w-3\\/4");
      const width12 = container.querySelector(".w-1\\/2");
      const widthFull = container.querySelector(".w-full");

      expect(width34).toBeInTheDocument();
      expect(width12).toBeInTheDocument();
      expect(widthFull).toBeInTheDocument();
    });

    it("should have varied skeleton heights", () => {
      const { container } = render(<CardSkeleton cards={1} />);

      const height6 = container.querySelector(".h-6");
      const height4 = container.querySelector(".h-4");
      const height2 = container.querySelector(".h-2");

      expect(height6).toBeInTheDocument();
      expect(height4).toBeInTheDocument();
      expect(height2).toBeInTheDocument();
    });
  });

  describe("Card Structure", () => {
    it("should have proper card styling", () => {
      const { container } = render(<CardSkeleton cards={1} />);

      const card = container.querySelector(".rounded-lg.bg-white.p-6.shadow");
      expect(card).toBeInTheDocument();
    });

    it("should have spacing inside cards", () => {
      const { container } = render(<CardSkeleton cards={1} />);

      const spacing = container.querySelector(".space-y-4");
      expect(spacing).toBeInTheDocument();
    });
  });

  describe("Edge Cases", () => {
    it("should handle 0 cards", () => {
      const { container } = render(<CardSkeleton cards={0} />);

      const cards = container.querySelectorAll(
        ".rounded-lg.bg-white.p-6.shadow",
      );
      expect(cards).toHaveLength(0);
    });

    it("should handle large number of cards", () => {
      const { container } = render(<CardSkeleton cards={12} />);

      const cards = container.querySelectorAll(
        ".rounded-lg.bg-white.p-6.shadow",
      );
      expect(cards).toHaveLength(12);
    });
  });
});
