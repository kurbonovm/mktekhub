import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Tooltip } from "@/components/common/Tooltip";

describe("Tooltip", () => {
  describe("Rendering", () => {
    it("should render children", () => {
      render(
        <Tooltip content="Tooltip text">
          <button>Hover me</button>
        </Tooltip>,
      );

      expect(
        screen.getByRole("button", { name: "Hover me" }),
      ).toBeInTheDocument();
    });

    it("should not show tooltip by default", () => {
      render(
        <Tooltip content="Tooltip text">
          <button>Hover me</button>
        </Tooltip>,
      );

      expect(screen.queryByRole("tooltip")).not.toBeInTheDocument();
    });

    it("should apply custom className to wrapper", () => {
      const { container } = render(
        <Tooltip content="Tooltip text" className="custom-class">
          <button>Hover me</button>
        </Tooltip>,
      );

      const wrapper = container.querySelector(".custom-class");
      expect(wrapper).toBeInTheDocument();
    });
  });

  describe("Hover Interactions", () => {
    it("should show tooltip on mouse enter", async () => {
      const user = userEvent.setup();
      render(
        <Tooltip content="Helpful information">
          <button>Hover me</button>
        </Tooltip>,
      );

      const button = screen.getByRole("button");
      await user.hover(button);

      expect(screen.getByRole("tooltip")).toBeInTheDocument();
      expect(screen.getByText("Helpful information")).toBeInTheDocument();
    });

    it("should hide tooltip on mouse leave", async () => {
      const user = userEvent.setup();
      render(
        <Tooltip content="Helpful information">
          <button>Hover me</button>
        </Tooltip>,
      );

      const button = screen.getByRole("button");
      await user.hover(button);
      expect(screen.getByRole("tooltip")).toBeInTheDocument();

      await user.unhover(button);
      expect(screen.queryByRole("tooltip")).not.toBeInTheDocument();
    });

    it("should toggle tooltip multiple times", async () => {
      const user = userEvent.setup();
      render(
        <Tooltip content="Tooltip text">
          <button>Hover me</button>
        </Tooltip>,
      );

      const button = screen.getByRole("button");

      // First hover
      await user.hover(button);
      expect(screen.getByRole("tooltip")).toBeInTheDocument();

      await user.unhover(button);
      expect(screen.queryByRole("tooltip")).not.toBeInTheDocument();

      // Second hover
      await user.hover(button);
      expect(screen.getByRole("tooltip")).toBeInTheDocument();
    });
  });

  describe("Positions", () => {
    it("should position tooltip on top by default", async () => {
      const user = userEvent.setup();
      const { container } = render(
        <Tooltip content="Top tooltip">
          <button>Hover me</button>
        </Tooltip>,
      );

      await user.hover(screen.getByRole("button"));

      const tooltip = container.querySelector('[role="tooltip"]');
      expect(tooltip?.className).toContain("bottom-full");
    });

    it("should position tooltip on bottom when specified", async () => {
      const user = userEvent.setup();
      const { container } = render(
        <Tooltip content="Bottom tooltip" position="bottom">
          <button>Hover me</button>
        </Tooltip>,
      );

      await user.hover(screen.getByRole("button"));

      const tooltip = container.querySelector('[role="tooltip"]');
      expect(tooltip?.className).toContain("top-full");
    });

    it("should position tooltip on left when specified", async () => {
      const user = userEvent.setup();
      const { container } = render(
        <Tooltip content="Left tooltip" position="left">
          <button>Hover me</button>
        </Tooltip>,
      );

      await user.hover(screen.getByRole("button"));

      const tooltip = container.querySelector('[role="tooltip"]');
      expect(tooltip?.className).toContain("right-full");
    });

    it("should position tooltip on right when specified", async () => {
      const user = userEvent.setup();
      const { container } = render(
        <Tooltip content="Right tooltip" position="right">
          <button>Hover me</button>
        </Tooltip>,
      );

      await user.hover(screen.getByRole("button"));

      const tooltip = container.querySelector('[role="tooltip"]');
      expect(tooltip?.className).toContain("left-full");
    });
  });

  describe("Tooltip Content", () => {
    it("should display provided content", async () => {
      const user = userEvent.setup();
      render(
        <Tooltip content="This is helpful information">
          <button>Hover me</button>
        </Tooltip>,
      );

      await user.hover(screen.getByRole("button"));

      expect(
        screen.getByText("This is helpful information"),
      ).toBeInTheDocument();
    });

    it("should handle long content", async () => {
      const user = userEvent.setup();
      const longContent =
        "This is a very long tooltip message that contains a lot of information";

      render(
        <Tooltip content={longContent}>
          <button>Hover me</button>
        </Tooltip>,
      );

      await user.hover(screen.getByRole("button"));

      expect(screen.getByText(longContent)).toBeInTheDocument();
    });

    it("should handle special characters in content", async () => {
      const user = userEvent.setup();
      render(
        <Tooltip content='Special chars: @#$% & <>"'>
          <button>Hover me</button>
        </Tooltip>,
      );

      await user.hover(screen.getByRole("button"));

      expect(screen.getByText(/Special chars:/)).toBeInTheDocument();
    });

    it("should not show tooltip when content is empty", async () => {
      const user = userEvent.setup();
      render(
        <Tooltip content="">
          <button>Hover me</button>
        </Tooltip>,
      );

      await user.hover(screen.getByRole("button"));

      expect(screen.queryByRole("tooltip")).not.toBeInTheDocument();
    });
  });

  describe("Accessibility", () => {
    it('should have role="tooltip"', async () => {
      const user = userEvent.setup();
      render(
        <Tooltip content="Tooltip text">
          <button>Hover me</button>
        </Tooltip>,
      );

      await user.hover(screen.getByRole("button"));

      const tooltip = screen.getByRole("tooltip");
      expect(tooltip).toBeInTheDocument();
    });

    it("should be non-interactive (pointer-events-none)", async () => {
      const user = userEvent.setup();
      const { container } = render(
        <Tooltip content="Tooltip text">
          <button>Hover me</button>
        </Tooltip>,
      );

      await user.hover(screen.getByRole("button"));

      const tooltip = container.querySelector('[role="tooltip"]');
      expect(tooltip?.className).toContain("pointer-events-none");
    });

    it("should have proper z-index for layering", async () => {
      const user = userEvent.setup();
      const { container } = render(
        <Tooltip content="Tooltip text">
          <button>Hover me</button>
        </Tooltip>,
      );

      await user.hover(screen.getByRole("button"));

      const tooltip = container.querySelector('[role="tooltip"]');
      expect(tooltip?.className).toContain("z-50");
    });
  });

  describe("Styling", () => {
    it("should have dark background", async () => {
      const user = userEvent.setup();
      const { container } = render(
        <Tooltip content="Tooltip text">
          <button>Hover me</button>
        </Tooltip>,
      );

      await user.hover(screen.getByRole("button"));

      const tooltipContent = container.querySelector(".bg-gray-900");
      expect(tooltipContent).toBeInTheDocument();
    });

    it("should have white text", async () => {
      const user = userEvent.setup();
      const { container } = render(
        <Tooltip content="Tooltip text">
          <button>Hover me</button>
        </Tooltip>,
      );

      await user.hover(screen.getByRole("button"));

      const tooltipContent = container.querySelector(".text-white");
      expect(tooltipContent).toBeInTheDocument();
    });

    it("should render arrow for tooltip", async () => {
      const user = userEvent.setup();
      const { container } = render(
        <Tooltip content="Tooltip text">
          <button>Hover me</button>
        </Tooltip>,
      );

      await user.hover(screen.getByRole("button"));

      const arrow = container.querySelector(".border-4");
      expect(arrow).toBeInTheDocument();
    });

    it("should prevent text wrapping", async () => {
      const user = userEvent.setup();
      const { container } = render(
        <Tooltip content="Tooltip text">
          <button>Hover me</button>
        </Tooltip>,
      );

      await user.hover(screen.getByRole("button"));

      const tooltipContent = container.querySelector(".whitespace-nowrap");
      expect(tooltipContent).toBeInTheDocument();
    });
  });

  describe("Complex Children", () => {
    it("should work with complex child elements", async () => {
      const user = userEvent.setup();
      render(
        <Tooltip content="Complex tooltip">
          <div>
            <span>Icon</span>
            <button>Button</button>
          </div>
        </Tooltip>,
      );

      const wrapper = screen.getByText("Icon").parentElement;
      await user.hover(wrapper!);

      expect(screen.getByRole("tooltip")).toBeInTheDocument();
    });

    it("should work with icon buttons", async () => {
      const user = userEvent.setup();
      render(
        <Tooltip content="Help information">
          <button aria-label="Help">
            <svg>
              <title>Help Icon</title>
            </svg>
          </button>
        </Tooltip>,
      );

      await user.hover(screen.getByRole("button", { name: "Help" }));

      expect(screen.getByText("Help information")).toBeInTheDocument();
    });
  });
});
