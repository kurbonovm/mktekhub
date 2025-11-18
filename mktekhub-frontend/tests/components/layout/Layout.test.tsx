import { describe, it, expect, vi } from "vitest";
import { screen } from "@testing-library/react";
import { renderWithProviders } from "../../utils/testUtils";
import { Layout } from "@/components/layout/Layout";

// Mock Navbar since Layout depends on it
vi.mock("@/components/layout/Navbar", () => ({
  Navbar: () => <nav data-testid="navbar">Navbar</nav>,
}));

describe("Layout", () => {
  describe("Rendering", () => {
    it("should render children", () => {
      renderWithProviders(
        <Layout>
          <div>Test Content</div>
        </Layout>,
      );

      expect(screen.getByText("Test Content")).toBeInTheDocument();
    });

    it("should render Navbar component", () => {
      renderWithProviders(
        <Layout>
          <div>Content</div>
        </Layout>,
      );

      expect(screen.getByTestId("navbar")).toBeInTheDocument();
    });

    it("should render main element", () => {
      const { container } = renderWithProviders(
        <Layout>
          <div>Content</div>
        </Layout>,
      );

      const main = container.querySelector("main");
      expect(main).toBeInTheDocument();
    });

    it("should wrap children in main element", () => {
      const { container } = renderWithProviders(
        <Layout>
          <div data-testid="child">Content</div>
        </Layout>,
      );

      const main = container.querySelector("main");
      const child = screen.getByTestId("child");

      expect(main).toContainElement(child);
    });
  });

  describe("Styling", () => {
    it("should have min-h-screen class", () => {
      const { container } = renderWithProviders(
        <Layout>
          <div>Content</div>
        </Layout>,
      );

      const wrapper = container.querySelector(".min-h-screen");
      expect(wrapper).toBeInTheDocument();
    });

    it("should have bg-gray-50 background", () => {
      const { container } = renderWithProviders(
        <Layout>
          <div>Content</div>
        </Layout>,
      );

      const wrapper = container.querySelector(".bg-gray-50");
      expect(wrapper).toBeInTheDocument();
    });
  });

  describe("Children", () => {
    it("should render multiple children", () => {
      renderWithProviders(
        <Layout>
          <div>First Child</div>
          <div>Second Child</div>
        </Layout>,
      );

      expect(screen.getByText("First Child")).toBeInTheDocument();
      expect(screen.getByText("Second Child")).toBeInTheDocument();
    });

    it("should render complex child components", () => {
      renderWithProviders(
        <Layout>
          <div>
            <h1>Page Title</h1>
            <p>Page Content</p>
            <button>Action</button>
          </div>
        </Layout>,
      );

      expect(
        screen.getByRole("heading", { name: "Page Title" }),
      ).toBeInTheDocument();
      expect(screen.getByText("Page Content")).toBeInTheDocument();
      expect(
        screen.getByRole("button", { name: "Action" }),
      ).toBeInTheDocument();
    });

    it("should handle null children", () => {
      const { container } = renderWithProviders(<Layout>{null}</Layout>);

      const main = container.querySelector("main");
      expect(main).toBeInTheDocument();
      expect(main).toBeEmptyDOMElement();
    });

    it("should handle conditional children", () => {
      const showContent = true;

      renderWithProviders(
        <Layout>{showContent && <div>Conditional Content</div>}</Layout>,
      );

      expect(screen.getByText("Conditional Content")).toBeInTheDocument();
    });
  });

  describe("Structure", () => {
    it("should have correct component hierarchy", () => {
      const { container } = renderWithProviders(
        <Layout>
          <div data-testid="content">Content</div>
        </Layout>,
      );

      const wrapper = container.firstChild;
      const navbar = screen.getByTestId("navbar");
      const main = container.querySelector("main");

      expect(wrapper).toContainElement(navbar);
      expect(wrapper).toContainElement(main!);
    });

    it("should render Navbar before main content", () => {
      const { container } = renderWithProviders(
        <Layout>
          <div>Content</div>
        </Layout>,
      );

      const wrapper = container.firstChild as HTMLElement;
      const children = Array.from(wrapper.children);

      expect(children[0]).toContainElement(screen.getByTestId("navbar"));
      expect(children[1].tagName).toBe("MAIN");
    });
  });
});
