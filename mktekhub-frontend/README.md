# mktekhub Frontend

A modern warehouse inventory management system built with React, TypeScript, and Vite.

## Tech Stack

- **React 19** - UI framework
- **TypeScript** - Type-safe JavaScript
- **Vite** - Fast build tool and dev server
- **React Router v7** - Client-side routing
- **TanStack Query** - Server state management
- **Axios** - HTTP client
- **Tailwind CSS v4** - Utility-first CSS framework
- **Vitest** - Unit testing framework
- **Testing Library** - React component testing

## Features

- User authentication (login/signup)
- Inventory management with filtering and search
- Warehouse management
- Stock transfers between warehouses
- Bulk transfer operations
- Reports and analytics dashboard
- Responsive design for mobile and desktop
- Real-time toast notifications
- Protected routes with authentication

## Project Structure

```
src/
├── components/       # Reusable UI components
│   ├── common/      # Shared components (Toast, Skeleton, Breadcrumb, etc.)
│   ├── inventory/   # Inventory-specific components
│   └── layout/      # Layout components (Navbar, Layout)
├── contexts/        # React contexts (Auth, Toast)
├── pages/           # Page components
│   ├── LoginPage.tsx
│   ├── SignupPage.tsx
│   ├── InventoryPage.tsx
│   ├── WarehousesPage.tsx
│   ├── StockTransferPage.tsx
│   ├── BulkTransferPage.tsx
│   └── ReportsPage.tsx
├── services/        # API service layer
│   ├── api.ts                  # Axios instance configuration
│   ├── authService.ts          # Authentication API
│   ├── inventoryService.ts     # Inventory CRUD operations
│   ├── warehouseService.ts     # Warehouse management
│   ├── stockActivityService.ts # Stock transfer operations
│   └── dashboardService.ts     # Analytics and reports
├── types/           # TypeScript type definitions
├── utils/           # Utility functions (error handling, date formatting)
├── App.tsx          # Main application component
└── main.tsx         # Application entry point
```

## Getting Started

### Prerequisites

- Node.js (v18 or higher recommended)
- npm or yarn package manager

### Installation

1. Clone the repository and navigate to the frontend directory:
```bash
cd mktekhub-frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start the development server:
```bash
npm run dev
```

The application will start at `http://localhost:3000`

### Environment Configuration

The development server is configured to proxy API requests to the backend:
- Frontend: `http://localhost:3000`
- Backend API: `http://localhost:8080/api`

All requests to `/api/*` will be proxied to the backend server.

## Available Scripts

### Development
```bash
npm run dev          # Start development server
npm run build        # Build for production
npm run preview      # Preview production build
```

### Code Quality
```bash
npm run lint         # Run ESLint
npm run format:check # Check code formatting
npm run format:write # Format code with Prettier
```

### Testing
```bash
npm test             # Run tests in watch mode
npm run test:ui      # Run tests with UI interface
npm run test:coverage # Generate test coverage report
```

## API Integration

The frontend communicates with the backend REST API through the service layer:

- **Authentication**: JWT-based authentication with token storage
- **Inventory**: CRUD operations for inventory items
- **Warehouses**: Warehouse management and stock allocation
- **Stock Activities**: Track and manage stock transfers
- **Reports**: Analytics and reporting data

## Development Guidelines

### TypeScript

This project uses strict TypeScript configuration. Ensure all code is properly typed.

### Code Style

- ESLint is configured with React-specific rules
- Prettier handles code formatting
- Run `npm run format:write` before committing

### Testing

- Write unit tests for utilities and services
- Write component tests for UI components
- Aim for good test coverage
- Use Testing Library best practices

### Component Structure

- Use functional components with hooks
- Keep components small and focused
- Extract reusable logic into custom hooks
- Use React Context for global state (Auth, Toast)
- Use TanStack Query for server state

## Building for Production

```bash
npm run build
```

This will:
1. Run TypeScript compiler
2. Build optimized production bundle
3. Output to `dist/` directory

The production build is optimized and minified for deployment.

## Browser Support

Modern browsers that support ES modules and the latest web standards.

## Contributing

1. Follow the existing code style and structure
2. Write tests for new features
3. Run linting and formatting before committing
4. Ensure all tests pass

## License

This project is part of the SKILLSTORM training program.
