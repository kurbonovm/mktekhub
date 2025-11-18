import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { SearchBar } from "@/components/common/SearchBar";

describe("SearchBar", () => {
  const mockOnChange = vi.fn();

  afterEach(() => {
    mockOnChange.mockClear();
  });

  describe("Rendering", () => {
    it("should render with default placeholder", () => {
      render(<SearchBar value="" onChange={mockOnChange} />);

      const input = screen.getByPlaceholderText("Search...");
      expect(input).toBeInTheDocument();
    });

    it("should render with custom placeholder", () => {
      render(
        <SearchBar
          value=""
          onChange={mockOnChange}
          placeholder="Search by name or SKU"
        />,
      );

      const input = screen.getByPlaceholderText("Search by name or SKU");
      expect(input).toBeInTheDocument();
    });

    it("should render with custom className", () => {
      const { container } = render(
        <SearchBar value="" onChange={mockOnChange} className="custom-class" />,
      );

      const wrapper = container.querySelector(".custom-class");
      expect(wrapper).toBeInTheDocument();
    });

    it("should display the current value", () => {
      render(<SearchBar value="test search" onChange={mockOnChange} />);

      const input = screen.getByDisplayValue("test search");
      expect(input).toBeInTheDocument();
    });

    it("should render search icon", () => {
      const { container } = render(
        <SearchBar value="" onChange={mockOnChange} />,
      );

      const searchIcon = container.querySelector("svg");
      expect(searchIcon).toBeInTheDocument();
    });
  });

  describe("Clear Button", () => {
    it("should not show clear button when value is empty", () => {
      render(<SearchBar value="" onChange={mockOnChange} />);

      const clearButton = screen.queryByLabelText("Clear search");
      expect(clearButton).not.toBeInTheDocument();
    });

    it("should show clear button when value is not empty", () => {
      render(<SearchBar value="test" onChange={mockOnChange} />);

      const clearButton = screen.getByLabelText("Clear search");
      expect(clearButton).toBeInTheDocument();
    });

    it("should call onChange with empty string when clear button is clicked", async () => {
      const user = userEvent.setup();
      render(<SearchBar value="test search" onChange={mockOnChange} />);

      const clearButton = screen.getByLabelText("Clear search");
      await user.click(clearButton);

      expect(mockOnChange).toHaveBeenCalledWith("");
      expect(mockOnChange).toHaveBeenCalledTimes(1);
    });
  });

  describe("User Interactions", () => {
    it("should call onChange when user types in the input", async () => {
      const user = userEvent.setup();
      render(<SearchBar value="" onChange={mockOnChange} />);

      const input = screen.getByPlaceholderText("Search...");
      await user.type(input, "laptop");

      expect(mockOnChange).toHaveBeenCalled();
      // userEvent.type calls onChange for each character
      expect(mockOnChange.mock.calls.length).toBeGreaterThan(0);
    });

    it("should call onChange when user deletes text", async () => {
      const user = userEvent.setup();
      render(<SearchBar value="test" onChange={mockOnChange} />);

      const input = screen.getByDisplayValue("test");
      await user.clear(input);

      expect(mockOnChange).toHaveBeenCalled();
    });

    it("should update displayed value as user types", async () => {
      const user = userEvent.setup();
      const { rerender } = render(
        <SearchBar value="" onChange={mockOnChange} />,
      );

      const input = screen.getByPlaceholderText(
        "Search...",
      ) as HTMLInputElement;

      await user.type(input, "new");
      rerender(<SearchBar value="new" onChange={mockOnChange} />);

      expect(input.value).toBe("new");
    });
  });

  describe("Accessibility", () => {
    it("should have proper input type", () => {
      render(<SearchBar value="" onChange={mockOnChange} />);

      const input = screen.getByPlaceholderText("Search...");
      expect(input).toHaveAttribute("type", "text");
    });

    it("should have aria-label on clear button", () => {
      render(<SearchBar value="test" onChange={mockOnChange} />);

      const clearButton = screen.getByLabelText("Clear search");
      expect(clearButton).toHaveAttribute("aria-label", "Clear search");
    });

    it("should have button type on clear button to prevent form submission", () => {
      render(<SearchBar value="test" onChange={mockOnChange} />);

      const clearButton = screen.getByLabelText("Clear search");
      expect(clearButton).toHaveAttribute("type", "button");
    });
  });

  describe("Edge Cases", () => {
    it("should handle rapid typing", async () => {
      const user = userEvent.setup();
      render(<SearchBar value="" onChange={mockOnChange} />);

      const input = screen.getByPlaceholderText("Search...");
      await user.type(input, "quicksearch", { delay: 1 });

      expect(mockOnChange).toHaveBeenCalled();
      expect(mockOnChange.mock.calls.length).toBeGreaterThan(0);
    });

    it("should handle special characters in search", async () => {
      const user = userEvent.setup();
      render(<SearchBar value="" onChange={mockOnChange} />);

      const input = screen.getByPlaceholderText("Search...");
      await user.type(input, "@#$%");

      expect(mockOnChange).toHaveBeenCalled();
    });

    it("should handle long search strings", async () => {
      const longString = "a".repeat(100);
      render(<SearchBar value={longString} onChange={mockOnChange} />);

      const input = screen.getByDisplayValue(longString);
      expect(input).toBeInTheDocument();
    });
  });
});
