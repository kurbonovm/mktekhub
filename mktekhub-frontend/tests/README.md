# Frontend Tests

This directory contains all tests for the mktekhub-frontend application.

## Test Structure

```
tests/
├── setup.ts                          # Test setup and configuration
└── services/                         # Service layer tests
    ├── authService.test.ts          # Authentication service tests (18 tests)
    ├── dashboardService.test.ts     # Dashboard service tests (13 tests)
    ├── inventoryService.test.ts     # Inventory service tests (27 tests)
    ├── warehouseService.test.ts     # Warehouse service tests (26 tests)
    └── stockActivityService.test.ts # Stock activity service tests (30 tests)
```

**Total: 114 tests**

## Running Tests

### Run all tests
```bash
npm test
```

### Run tests with UI
```bash
npm run test:ui
```

### Run tests with coverage
```bash
npm run test:coverage
```

### Run tests in watch mode
```bash
npm test
```

## Test Coverage

All service functions are tested including:
- Happy path scenarios
- Error handling (404, 500, network errors)
- Edge cases (empty data, special characters, validation errors)
- Authentication and authorization flows
- CRUD operations
- Query parameters and filters

## Technologies Used

- **Vitest** - Fast unit test framework
- **@testing-library/react** - React testing utilities
- **@testing-library/jest-dom** - Custom Jest matchers
- **axios-mock-adapter** - Mock axios requests
- **jsdom** - DOM implementation for testing

## Test Organization

Tests are organized in a separate `tests/` directory at the project root, mirroring the source structure. This approach provides:
- Clear separation between source code and tests
- Easy navigation and organization
- Centralized test configuration

## Writing New Tests

When adding new tests:
1. Place test files in the appropriate subdirectory under `tests/`
2. Name test files with the pattern `*.test.ts` or `*.test.tsx`
3. Import from source using relative paths: `../../src/...`
4. Follow existing test patterns for consistency
5. Ensure both success and error cases are covered
