import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../contexts/AuthContext";
import mkth from "./mkth.png";

export const Navbar = () => {
  const { user, logout, hasRole } = useAuth();
  const navigate = useNavigate();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate("/login");
    setIsMobileMenuOpen(false);
  };

  const closeMobileMenu = () => {
    setIsMobileMenuOpen(false);
  };

  const isAdminOrManager = hasRole("ADMIN") || hasRole("MANAGER");

  return (
    <nav className="relative z-50 bg-blue-600 shadow-lg">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex h-16 items-center justify-between">
          {/* Logo */}
          <div className="flex items-center">
            <Link
              to="/dashboard"
              className="text-lg font-bold text-white hover:text-blue-100 sm:text-xl"
              onClick={closeMobileMenu}
            >
              <img src={mkth} alt="" width={"50px"} />
            </Link>
          </div>

          {/* Desktop Navigation */}
          <div className="hidden items-baseline space-x-4 md:flex">
            <Link
              to="/dashboard"
              className="rounded-md px-3 py-2 text-sm font-medium text-white hover:bg-blue-700"
            >
              Dashboard
            </Link>
            <Link
              to="/warehouses"
              className="rounded-md px-3 py-2 text-sm font-medium text-white hover:bg-blue-700"
            >
              Warehouses
            </Link>
            <Link
              to="/inventory"
              className="rounded-md px-3 py-2 text-sm font-medium text-white hover:bg-blue-700"
            >
              Inventory
            </Link>
            {isAdminOrManager && (
              <Link
                to="/stock-transfer"
                className="rounded-md px-3 py-2 text-sm font-medium text-white hover:bg-blue-700"
              >
                Stock Transfer
              </Link>
            )}
            <Link
              to="/stock-activity"
              className="rounded-md px-3 py-2 text-sm font-medium text-white hover:bg-blue-700"
            >
              Activity
            </Link>
            <Link
              to="/reports"
              className="rounded-md px-3 py-2 text-sm font-medium text-white hover:bg-blue-700"
            >
              Reports
            </Link>
            <Link
              to="/custom-reports"
              className="rounded-md px-3 py-2 text-sm font-medium text-white hover:bg-blue-700"
            >
              Custom Reports
            </Link>
          </div>

          {/* Desktop User Info */}
          <div className="hidden items-center space-x-4 md:flex">
            <div className="text-sm text-white">
              <span className="font-medium">
                {user?.firstName} {user?.lastName}
              </span>
              <span className="ml-2 text-blue-200">
                ({user?.roles.join(", ")})
              </span>
            </div>
            <button
              onClick={handleLogout}
              className="rounded-md bg-blue-700 px-4 py-2 text-sm font-medium text-white hover:bg-blue-800 focus:outline-none focus:ring-2 focus:ring-white focus:ring-offset-2 focus:ring-offset-blue-600"
            >
              Logout
            </button>
          </div>

          {/* Mobile menu button */}
          <div className="flex md:hidden">
            <button
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
              className="inline-flex items-center justify-center rounded-md p-2 text-white hover:bg-blue-700 hover:text-white focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white"
              aria-expanded="false"
            >
              <span className="sr-only">Open main menu</span>
              {!isMobileMenuOpen ? (
                <svg
                  className="block h-6 w-6"
                  fill="none"
                  viewBox="0 0 24 24"
                  strokeWidth="1.5"
                  stroke="currentColor"
                  aria-hidden="true"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5"
                  />
                </svg>
              ) : (
                <svg
                  className="block h-6 w-6"
                  fill="none"
                  viewBox="0 0 24 24"
                  strokeWidth="1.5"
                  stroke="currentColor"
                  aria-hidden="true"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    d="M6 18L18 6M6 6l12 12"
                  />
                </svg>
              )}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile menu with backdrop */}
      {isMobileMenuOpen && (
        <>
          {/* Backdrop overlay - closes menu when clicked */}
          <div
            className="fixed inset-0 top-16 z-40 bg-black/30 md:hidden"
            onClick={closeMobileMenu}
          />

          {/* Mobile menu content */}
          <div className="relative z-50 md:hidden">
            <div className="space-y-1 px-2 pb-3 pt-2 sm:px-3">
              {/* User Info Mobile */}
              <div className="mb-4 border-b border-blue-500 pb-3">
                <div className="text-base font-medium text-white">
                  {user?.firstName} {user?.lastName}
                </div>
                <div className="text-sm text-blue-200">
                  {user?.roles.join(", ")}
                </div>
              </div>

              {/* Navigation Links Mobile */}
              <Link
                to="/dashboard"
                className="block rounded-md px-3 py-2 text-base font-medium text-white hover:bg-blue-700"
                onClick={closeMobileMenu}
              >
                Dashboard
              </Link>
              <Link
                to="/warehouses"
                className="block rounded-md px-3 py-2 text-base font-medium text-white hover:bg-blue-700"
                onClick={closeMobileMenu}
              >
                Warehouses
              </Link>
              <Link
                to="/inventory"
                className="block rounded-md px-3 py-2 text-base font-medium text-white hover:bg-blue-700"
                onClick={closeMobileMenu}
              >
                Inventory
              </Link>
              {isAdminOrManager && (
                <Link
                  to="/stock-transfer"
                  className="block rounded-md px-3 py-2 text-base font-medium text-white hover:bg-blue-700"
                  onClick={closeMobileMenu}
                >
                  Stock Transfer
                </Link>
              )}
              <Link
                to="/stock-activity"
                className="block rounded-md px-3 py-2 text-base font-medium text-white hover:bg-blue-700"
                onClick={closeMobileMenu}
              >
                Activity History
              </Link>
              <Link
                to="/reports"
                className="block rounded-md px-3 py-2 text-base font-medium text-white hover:bg-blue-700"
                onClick={closeMobileMenu}
              >
                Reports
              </Link>
              <Link
                to="/custom-reports"
                className="block rounded-md px-3 py-2 text-base font-medium text-white hover:bg-blue-700"
                onClick={closeMobileMenu}
              >
                Custom Reports
              </Link>

              {/* Logout Button Mobile */}
              <button
                onClick={handleLogout}
                className="mt-4 w-full rounded-md bg-blue-700 px-3 py-2 text-base font-medium text-white hover:bg-blue-800"
              >
                Logout
              </button>
            </div>
          </div>
        </>
      )}
    </nav>
  );
};
