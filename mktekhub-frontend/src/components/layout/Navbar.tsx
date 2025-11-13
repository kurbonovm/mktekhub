import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../contexts/AuthContext";

export const Navbar = () => {
  const { user, logout, hasRole } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const isAdminOrManager = hasRole("ADMIN") || hasRole("MANAGER");

  return (
    <nav className="bg-blue-600 shadow-lg">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex h-16 items-center justify-between">
          <div className="flex items-center">
            <Link
              to="/dashboard"
              className="text-xl font-bold text-white hover:text-blue-100"
            >
              MKTekHub Inventory
            </Link>
            <div className="ml-10 flex items-baseline space-x-4">
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
                Activity History
              </Link>
            </div>
          </div>
          <div className="flex items-center space-x-4">
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
        </div>
      </div>
    </nav>
  );
};
