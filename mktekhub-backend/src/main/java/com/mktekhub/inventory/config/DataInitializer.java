package com.mktekhub.inventory.config;

import com.mktekhub.inventory.model.InventoryItem;
import com.mktekhub.inventory.model.Role;
import com.mktekhub.inventory.model.Warehouse;
import com.mktekhub.inventory.repository.InventoryItemRepository;
import com.mktekhub.inventory.repository.RoleRepository;
import com.mktekhub.inventory.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Initialize default data on application startup.
 * Creates roles, warehouses, and sample inventory items with various scenarios.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Override
    public void run(String... args) throws Exception {
        // Create default roles if they don't exist
        createRoleIfNotExists("ADMIN", "Full system access with all privileges");
        createRoleIfNotExists("MANAGER", "Warehouse and inventory management access");
        createRoleIfNotExists("VIEWER", "Read-only access to view data");

        // Only seed sample data if database is empty
        if (warehouseRepository.count() == 0) {
            System.out.println("Seeding sample data...");
            seedWarehouses();
            seedInventoryItems();
            System.out.println("Sample data seeded successfully!");
        }
    }

    private void createRoleIfNotExists(String name, String description) {
        if (!roleRepository.existsByName(name)) {
            Role role = new Role(name, description);
            roleRepository.save(role);
            System.out.println("Created role: " + name);
        }
    }

    private void seedWarehouses() {
        Warehouse warehouse1 = new Warehouse();
        warehouse1.setName("Main Warehouse");
        warehouse1.setLocation("123 Main St, New York, NY 10001");
        warehouse1.setMaxCapacity(new BigDecimal("10000.00"));
        warehouse1.setCurrentCapacity(BigDecimal.ZERO);
        warehouse1.setCapacityAlertThreshold(new BigDecimal("80.00"));
        warehouseRepository.save(warehouse1);

        Warehouse warehouse2 = new Warehouse();
        warehouse2.setName("West Coast Distribution Center");
        warehouse2.setLocation("456 Pacific Ave, Los Angeles, CA 90001");
        warehouse2.setMaxCapacity(new BigDecimal("15000.00"));
        warehouse2.setCurrentCapacity(BigDecimal.ZERO);
        warehouse2.setCapacityAlertThreshold(new BigDecimal("75.00"));
        warehouseRepository.save(warehouse2);

        Warehouse warehouse3 = new Warehouse();
        warehouse3.setName("East Coast Hub");
        warehouse3.setLocation("789 Atlantic Blvd, Boston, MA 02101");
        warehouse3.setMaxCapacity(new BigDecimal("8000.00"));
        warehouse3.setCurrentCapacity(BigDecimal.ZERO);
        warehouse3.setCapacityAlertThreshold(new BigDecimal("85.00"));
        warehouseRepository.save(warehouse3);

        System.out.println("Created 3 warehouses");
    }

    private void seedInventoryItems() {
        Warehouse mainWarehouse = warehouseRepository.findByName("Main Warehouse").orElseThrow();
        Warehouse westCoast = warehouseRepository.findByName("West Coast Distribution Center").orElseThrow();
        Warehouse eastCoast = warehouseRepository.findByName("East Coast Hub").orElseThrow();

        // Electronics - Various scenarios (with volume in cubic feet)
        createItem("ELEC-001", "MacBook Pro 16\"", "High-performance laptop for professionals",
                "Electronics", "Apple", 25, 5, 2499.99, 0.60, mainWarehouse,
                LocalDate.now().plusYears(2), LocalDate.now().plusMonths(3), "194253911821");

        createItem("ELEC-002", "Dell XPS 13", "Ultra-portable laptop",
                "Electronics", "Dell", 3, 10, 1299.99, 0.45, mainWarehouse,
                LocalDate.now().plusYears(1), LocalDate.now().minusMonths(2), "884116376422");

        createItem("ELEC-003", "iPhone 15 Pro", "Latest flagship smartphone",
                "Electronics", "Apple", 50, 15, 999.99, 0.08, westCoast,
                LocalDate.now().plusMonths(18), LocalDate.now().plusMonths(8), "194253911838");

        createItem("ELEC-004", "Samsung Galaxy S24", "Android smartphone",
                "Electronics", "Samsung", 8, 10, 899.99, 0.08, westCoast,
                null, LocalDate.now().plusMonths(10), "887276761688");

        createItem("ELEC-005", "iPad Air", "Versatile tablet for work and play",
                "Electronics", "Apple", 30, 10, 599.99, 0.15, eastCoast,
                LocalDate.now().plusYears(2), LocalDate.now().plusMonths(6), "194253911845");

        // Office Supplies - Some expired, some expiring soon
        createItem("OFF-001", "Ergonomic Office Chair", "Adjustable lumbar support",
                "Office Supplies", "Herman Miller", 12, 5, 899.99, 8.0, mainWarehouse,
                null, LocalDate.now().plusYears(5), null);

        createItem("OFF-002", "Standing Desk", "Electric height-adjustable desk",
                "Office Supplies", "FlexiSpot", 8, 3, 499.99, 25.0, eastCoast,
                null, LocalDate.now().plusYears(3), null);

        createItem("OFF-003", "Printer Paper (500 sheets)", "Premium white paper",
                "Office Supplies", "HP", 200, 50, 8.99, 0.5, mainWarehouse,
                LocalDate.now().plusYears(3), null, "085698334297");

        createItem("OFF-004", "Toner Cartridge (Black)", "High-yield toner",
                "Office Supplies", "HP", 45, 20, 89.99, 0.3, westCoast,
                LocalDate.now().plusYears(2), null, "885631409527");

        // More Electronics & Accessories
        createItem("ELEC-006", "USB-C Hub", "7-in-1 USB-C multiport adapter",
                "Electronics", "Anker", 75, 20, 49.99, 0.05, mainWarehouse,
                null, LocalDate.now().plusYears(1), "194644037352");

        createItem("ELEC-007", "Wireless Mouse", "Ergonomic wireless mouse",
                "Electronics", "Logitech", 120, 30, 29.99, 0.1, westCoast,
                null, LocalDate.now().plusMonths(18), "097855125095");

        createItem("ELEC-008", "Mechanical Keyboard", "RGB backlit gaming keyboard",
                "Electronics", "Corsair", 40, 15, 149.99, 0.5, eastCoast,
                null, LocalDate.now().plusYears(2), "840006604174");

        createItem("ELEC-009", "4K Monitor 27\"", "IPS display with HDR",
                "Electronics", "LG", 15, 5, 399.99, 1.5, mainWarehouse,
                null, LocalDate.now().plusYears(3), "195174038895");

        createItem("ELEC-010", "Webcam 1080p", "Full HD webcam with microphone",
                "Electronics", "Logitech", 60, 20, 79.99, 0.15, westCoast,
                null, LocalDate.now().plusMonths(24), "097855154880");

        // Computer Components
        createItem("COMP-001", "SSD 1TB NVMe", "High-speed internal SSD",
                "Computer Components", "Samsung", 45, 15, 129.99, 0.02, mainWarehouse,
                null, LocalDate.now().plusYears(5), "887276660035");

        createItem("COMP-002", "RAM 16GB DDR4", "Desktop memory module",
                "Computer Components", "Corsair", 80, 25, 79.99, 0.01, eastCoast,
                null, LocalDate.now().plusYears(10), "843591098625");

        createItem("COMP-003", "Graphics Card RTX 4060", "8GB GDDR6 GPU",
                "Computer Components", "NVIDIA", 8, 3, 329.99, 0.4, westCoast,
                null, LocalDate.now().plusYears(3), "812674028125");

        createItem("COMP-004", "CPU Ryzen 5", "6-core desktop processor",
                "Computer Components", "AMD", 12, 5, 199.99, 0.2, mainWarehouse,
                null, LocalDate.now().plusYears(3), "730143313018");

        // Audio Equipment
        createItem("AUDIO-001", "Wireless Headphones", "Noise-canceling over-ear",
                "Audio Equipment", "Sony", 35, 10, 349.99, 0.3, westCoast,
                null, LocalDate.now().plusYears(2), "027242920025");

        createItem("AUDIO-002", "Bluetooth Speaker", "Portable waterproof speaker",
                "Audio Equipment", "JBL", 65, 20, 129.99, 0.2, eastCoast,
                null, LocalDate.now().plusMonths(18), "050036379830");

        createItem("AUDIO-003", "Gaming Headset", "7.1 surround sound headset",
                "Audio Equipment", "Razer", 40, 15, 99.99, 0.4, mainWarehouse,
                null, LocalDate.now().plusYears(1), "811659036063");

        createItem("AUDIO-004", "USB Microphone", "Studio-quality USB mic",
                "Audio Equipment", "Blue Yeti", 25, 10, 129.99, 0.3, westCoast,
                null, LocalDate.now().plusMonths(24), "988063002184");

        // Networking Equipment
        createItem("NET-001", "WiFi 6 Router", "Dual-band wireless router",
                "Networking", "TP-Link", 30, 10, 89.99, 0.5, mainWarehouse,
                null, LocalDate.now().plusYears(3), "840030703294");

        createItem("NET-002", "Mesh WiFi System", "3-pack mesh network system",
                "Networking", "Google", 18, 5, 299.99, 1.5, eastCoast,
                null, LocalDate.now().plusYears(2), "193575009049");

        createItem("NET-003", "Network Switch 8-Port", "Gigabit ethernet switch",
                "Networking", "Netgear", 25, 8, 49.99, 0.3, westCoast,
                null, LocalDate.now().plusYears(5), "606449115987");

        // Cameras & Photography
        createItem("CAM-001", "Mirrorless Camera", "24MP full-frame camera body",
                "Cameras", "Sony", 10, 3, 1999.99, 0.8, mainWarehouse,
                null, LocalDate.now().plusYears(2), "027242922082");

        createItem("CAM-002", "Action Camera 4K", "Waterproof action camera",
                "Cameras", "GoPro", 30, 10, 399.99, 0.3, westCoast,
                null, LocalDate.now().plusMonths(24), "818279020922");

        createItem("CAM-003", "Ring Light", "18\" LED ring light with stand",
                "Cameras", "Neewer", 45, 15, 79.99, 1.2, eastCoast,
                null, LocalDate.now().plusYears(1), "607770001751");

        // Smart Home Devices
        createItem("SMART-001", "Smart Speaker", "Voice-controlled speaker",
                "Smart Home", "Amazon Echo", 70, 20, 99.99, 0.4, mainWarehouse,
                null, LocalDate.now().plusYears(1), "841667159867");

        createItem("SMART-002", "Smart Light Bulbs (4pk)", "WiFi-enabled LED bulbs",
                "Smart Home", "Philips Hue", 50, 15, 59.99, 0.15, westCoast,
                LocalDate.now().plusYears(10), LocalDate.now().plusYears(2), "046677551179");

        createItem("SMART-003", "Smart Thermostat", "Learning thermostat",
                "Smart Home", "Nest", 20, 5, 249.99, 0.3, eastCoast,
                null, LocalDate.now().plusYears(2), "193575002934");

        // Mobile Accessories
        createItem("MOB-001", "Phone Case iPhone 15", "Protective silicone case",
                "Mobile Accessories", "OtterBox", 100, 30, 39.99, 0.05, mainWarehouse,
                null, null, "660543556992");

        createItem("MOB-002", "Wireless Charger", "Fast charging pad",
                "Mobile Accessories", "Anker", 85, 25, 24.99, 0.2, westCoast,
                null, LocalDate.now().plusMonths(18), "194644107369");

        createItem("MOB-003", "Power Bank 20000mAh", "Portable battery pack",
                "Mobile Accessories", "RAVPower", 55, 20, 49.99, 0.15, eastCoast,
                null, LocalDate.now().plusYears(1), "848378004774");

        // Gaming
        createItem("GAME-001", "Gaming Console", "Next-gen gaming system",
                "Gaming", "PlayStation 5", 5, 2, 499.99, 0.8, mainWarehouse,
                null, LocalDate.now().plusYears(1), "711719556008");

        createItem("GAME-002", "Controller Wireless", "Bluetooth gaming controller",
                "Gaming", "Xbox", 40, 15, 59.99, 0.3, westCoast,
                null, LocalDate.now().plusMonths(12), "889842640488");

        createItem("GAME-003", "VR Headset", "Virtual reality gaming headset",
                "Gaming", "Meta Quest", 12, 4, 299.99, 1.0, eastCoast,
                null, LocalDate.now().plusMonths(18), "815820021438");

        System.out.println("Created 30+ electronics inventory items with various warranty scenarios");
    }

    private void createItem(String sku, String name, String description,
                            String category, String brand, int quantity, int reorderLevel,
                            double unitPrice, double volumePerUnit, Warehouse warehouse,
                            LocalDate expirationDate, LocalDate warrantyEndDate, String barcode) {
        InventoryItem item = new InventoryItem();
        item.setSku(sku);
        item.setName(name);
        item.setDescription(description);
        item.setCategory(category);
        item.setBrand(brand);
        item.setQuantity(quantity);
        item.setReorderLevel(reorderLevel);
        item.setUnitPrice(new BigDecimal(unitPrice));
        item.setVolumePerUnit(new BigDecimal(volumePerUnit));
        item.setWarehouse(warehouse);
        item.setExpirationDate(expirationDate);
        item.setWarrantyEndDate(warrantyEndDate);
        item.setBarcode(barcode);
        inventoryItemRepository.save(item);
    }
}
