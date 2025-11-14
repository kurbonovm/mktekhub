import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { stockActivityService } from "../services/stockActivityService";
import type { ActivityType } from "../types";

export const StockActivityPage = () => {
  const [filterType, setFilterType] = useState<string>("all");
  const [searchSku, setSearchSku] = useState("");

  const {
    data: activities,
    isLoading,
    error,
  } = useQuery({
    queryKey: ["stock-activity"],
    queryFn: () => stockActivityService.getAll(),
  });

  const getActivityBadgeColor = (type: ActivityType) => {
    switch (type) {
      case "RECEIVE":
        return "bg-green-100 text-green-800";
      case "TRANSFER":
        return "bg-blue-100 text-blue-800";
      case "SALE":
        return "bg-purple-100 text-purple-800";
      case "ADJUSTMENT":
        return "bg-yellow-100 text-yellow-800";
      case "UPDATE":
        return "bg-indigo-100 text-indigo-800";
      case "DELETE":
        return "bg-red-100 text-red-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  const filteredActivities = activities
    ?.filter((activity) => {
      const matchesType =
        filterType === "all" || activity.activityType === filterType;
      const matchesSku =
        searchSku === "" ||
        activity.itemSku.toLowerCase().includes(searchSku.toLowerCase()) ||
        activity.itemName.toLowerCase().includes(searchSku.toLowerCase());
      return matchesType && matchesSku;
    })
    .sort((a, b) => {
      // Sort by timestamp descending (newest first)
      return new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime();
    });

  if (isLoading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-xl">Loading activity history...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-xl text-red-600">
          Error loading activities: {(error as Error).message}
        </div>
      </div>
    );
  }

  return (
    <div className="p-8">
      <div className="mb-8 flex items-center justify-between">
        <h1 className="text-3xl font-bold text-gray-900">
          Stock Activity History
        </h1>
        <div className="text-sm text-gray-600">
          Total Activities: {activities?.length || 0}
        </div>
      </div>

      {/* Filters */}
      <div className="mb-6 flex gap-4">
        <div className="flex-1">
          <input
            type="text"
            placeholder="Search by SKU or item name..."
            className="w-full rounded-md border border-gray-300 px-4 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
            value={searchSku}
            onChange={(e) => setSearchSku(e.target.value)}
          />
        </div>
        <div>
          <select
            className="rounded-md border border-gray-300 px-4 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
            value={filterType}
            onChange={(e) => setFilterType(e.target.value)}
          >
            <option value="all">All Types</option>
            <option value="RECEIVE">Receive</option>
            <option value="TRANSFER">Transfer</option>
            <option value="SALE">Sale</option>
            <option value="ADJUSTMENT">Adjustment</option>
            <option value="UPDATE">Update</option>
            <option value="DELETE">Delete</option>
          </select>
        </div>
      </div>

      {/* Activity List */}
      <div className="space-y-4">
        {filteredActivities && filteredActivities.length > 0 ? (
          filteredActivities.map((activity) => (
            <div
              key={activity.id}
              className="rounded-lg bg-white p-6 shadow hover:shadow-md"
            >
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <div className="mb-2 flex items-center gap-3">
                    <span
                      className={`rounded-full px-3 py-1 text-xs font-medium ${getActivityBadgeColor(activity.activityType)}`}
                    >
                      {activity.activityType}
                    </span>
                    <h3 className="text-lg font-semibold text-gray-900">
                      {activity.itemName}
                    </h3>
                    <span className="text-sm text-gray-500">
                      SKU: {activity.itemSku}
                    </span>
                  </div>

                  <div className="mb-3 grid grid-cols-3 gap-4 text-sm">
                    <div>
                      <span className="text-gray-600">Quantity Change:</span>
                      <span
                        className={`ml-2 font-medium ${
                          activity.quantityChange > 0
                            ? "text-green-600"
                            : activity.quantityChange < 0
                              ? "text-red-600"
                              : "text-gray-900"
                        }`}
                      >
                        {activity.quantityChange > 0 ? "+" : ""}
                        {activity.quantityChange}
                      </span>
                    </div>
                    <div>
                      <span className="text-gray-600">Previous:</span>
                      <span className="ml-2 font-medium text-gray-900">
                        {activity.previousQuantity}
                      </span>
                    </div>
                    <div>
                      <span className="text-gray-600">New:</span>
                      <span className="ml-2 font-medium text-gray-900">
                        {activity.newQuantity}
                      </span>
                    </div>
                  </div>

                  {activity.activityType === "TRANSFER" && (
                    <div className="mb-3 flex gap-6 text-sm">
                      <div>
                        <span className="text-gray-600">From:</span>
                        <span className="ml-2 font-medium text-gray-900">
                          {activity.sourceWarehouseName}
                        </span>
                      </div>
                      <div>
                        <span className="text-gray-600">To:</span>
                        <span className="ml-2 font-medium text-gray-900">
                          {activity.destinationWarehouseName}
                        </span>
                      </div>
                    </div>
                  )}

                  {activity.notes && (
                    <div className="mb-3 text-sm">
                      <span className="text-gray-600">Notes:</span>
                      <span className="ml-2 text-gray-900">
                        {activity.notes}
                      </span>
                    </div>
                  )}

                  <div className="flex items-center gap-4 text-xs text-gray-500">
                    <div>
                      <span>Performed by:</span>
                      <span className="ml-1 font-medium">
                        {activity.performedBy}
                      </span>
                    </div>
                    <div>
                      <span>Time:</span>
                      <span className="ml-1">
                        {formatDate(activity.timestamp)}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          ))
        ) : (
          <div className="rounded-lg bg-gray-50 p-12 text-center">
            <p className="text-gray-600">
              No activities found matching your criteria
            </p>
          </div>
        )}
      </div>
    </div>
  );
};
