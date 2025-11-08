# Inventory Management System - Project Requirements

## Overview

In this challenge, you will develop and present a development project. Below you'll be given project requirements and will need to design and develop an enterprise solution. This exercise will better prepare you for the real-world project!

**Note:** You must upload your project artifacts in a ZIP file or a link to your publicly-accessible GitHub URL on this assignment page to earn credit.

## Evaluation Criteria

- Technical Architecture and Design
- User Interface and Experience
- Functional and Non-Functional Requirements Met
- Structured Development Approach

---

## Objective

Develop a comprehensive inventory management solution to empower administrators with full control over warehouse entities across multiple locations. Ensure the solution features robust capabilities for viewing, adding, removing, and modifying inventory, all within an intuitive, user-friendly interface. Prioritize clear and concise UI/UX to streamline administrative workflows and ensure ease of use. Integrate advanced functionality to handle edge cases, such as monitoring warehouse capacity limits and preventing overstocking, while providing alerts and recommendations for optimal warehouse utilization.

---

## Functional Requirements

### 1. Warehouse Management

#### Add New Warehouse
- Admins should be able to create a new warehouse by specifying its name, location, and maximum capacity.

#### View Warehouse Details
- Provide a dashboard where admins can view all warehouses, their current capacity, and other key metrics.

#### Edit Warehouse Information
- Allow admins to update warehouse details like capacity, location, or other relevant information.

#### Delete Warehouse
- Admins should be able to delete a warehouse. Implement confirmation dialogs to prevent accidental deletions.

### 2. Inventory Management

#### Add Inventory Items
- Admins should be able to add items to a warehouse, specifying item details such as name, SKU, description, quantity, and storage location within the warehouse.

#### Edit Inventory Items
- Provide functionality to update existing item details, such as adjusting quantity or editing descriptions.

#### Delete Inventory Items
- Implement a feature for admins to remove items from inventory. Like warehouse deletion, this should include a confirmation step.

#### View Inventory Items
- Admins should be able to view a list of all items within a warehouse, with search and filter capabilities based on item name, SKU, category, or other attributes.

#### Transfer Inventory Between Warehouses
- Enable the transfer of items between warehouses. Ensure that during the transfer, both the source and destination warehouse capacities are considered.

### 3. User Interface (UI) & User Experience (UX)

#### Dashboard Overview
- Create a dashboard that provides an overview of all warehouses, including current capacity, recent activity, and any alerts.

#### Intuitive Navigation
- Ensure that the UI is easy to navigate, with clearly labeled sections and buttons. Utilize breadcrumbs, collapsible menus, and tabs where appropriate.

#### Responsive Design
- The application should be accessible and fully functional on various devices, including desktops, tablets, and mobile phones.

#### Search and Filter
- Provide advanced search and filter options across the inventory and warehouse lists to help users quickly find what they need.

#### Error Handling
- Implement user-friendly error messages for edge cases like attempting to add more items than a warehouse can accommodate.

### 4. Edge Case Handling

#### Warehouse Full Capacity
- Decide how to handle scenarios where a warehouse's capacity is exceeded when adding new inventory. Should the action be prevented? Should the inventory be added to a different warehouse? Something else?

#### Duplicate Entries
- Decide how to handle duplicate inventory records with the same product information. Should the action be prevented? Should an existing entry be updated? Something else?

---

## Stretch Goals

Stretch goals are features you want to add to an application, but they aren't required. For this project, Stretch Goals are a way to go above and beyond the minimum requirements and I look forward to seeing what unique features you will add to your project. Here are some examples you might consider:

### Capacity Alerts
- Provide alerts when a warehouse is nearing or has exceeded its capacity, with options to view details and take corrective actions.

### Capacity Reports
- Generate reports on warehouse utilization, showing trends in capacity usage over time.

### Item Expiration or Obsolescence
- Implement features to manage items with expiration dates, including alerts and automatic flagging for review when an item is nearing expiration.

---

## Technical Requirements

Must be a full-stack solution consisting of:

- **Java with Spring Boot**
- **PostgreSQL**
- **HTML/CSS/JS or React**
- Code should be available to a public GitHub repository
- Possesses all required CRUD functionality
- Handles edge cases effectively

---

## Non-Functional Requirements

- Well documented code (JavaDocs/JSDoc)
- Code upholds industry best practices (SOLID/DRY)
- Industry-Grade UI (User Interface)
- Intuitive UX (User Experience) 