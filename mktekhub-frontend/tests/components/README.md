# Component Tests Documentation

This directory contains comprehensive unit tests for all React components in the mktekhub-frontend application.

## Test Coverage

### ✅ Completed Component Tests (175 tests)

#### Common Components

- **SearchBar** (17 tests) - Text search input with clear functionality
- **ConfirmDialog** (27 tests) - Modal confirmation dialogs with variants
- **ExpirationBadge** (33 tests) - Expiration status badges for inventory items
- **Breadcrumb** (23 tests) - Navigation breadcrumbs with auto-generation
- **Tooltip** (23 tests) - Hover tooltips with multiple positions
- **Skeleton** (36 tests) - Loading placeholders with variants
- **ProtectedRoute** (16 tests) - Route guards with role-based access

## Test Structure

```
tests/
├── components/
│   └── common/
│       ├── Breadcrumb.test.tsx      (23 tests)
│       ├── ConfirmDialog.test.tsx   (27 tests)
│       ├── ExpirationBadge.test.tsx (33 tests)
│       ├── ProtectedRoute.test.tsx  (16 tests)
│       ├── SearchBar.test.tsx       (17 tests)
│       ├── Skeleton.test.tsx        (36 tests)
│       └── Tooltip.test.tsx         (23 tests)
├── utils/
│   ├── testUtils.tsx    (Custom render with providers)
│   └── mockData.ts      (Mock data generators)
└── README.md
```

## Testing Utilities

### Custom Render Function

We've created a custom `renderWithProviders` function that wraps components with necessary providers:

```typescript
import { renderWithProviders } from '../../utils/testUtils';

renderWithProviders(<MyComponent />, {
  initialRoute: '/inventory',
  authValue: { isAuthenticated: true }
});
```

**Features:**

- Wraps components with `MemoryRouter` for routing tests
- Includes `AuthProvider` for authentication context
- Includes `ToastProvider` for toast notifications
- Supports custom initial routes
- Supports custom auth values

### Mock Data

The `mockData.ts` file provides:

- Mock users (admin, manager, employee)
- Mock warehouses
- Mock inventory items
- Mock stock activities
- Mock dashboard summaries
- Helper functions to create custom mock data

```typescript
import {
  mockInventoryItems,
  createMockInventoryItem,
} from "../../utils/mockData";

const customItem = createMockInventoryItem({
  name: "Custom Item",
  quantity: 50,
});
```

## Test Patterns

### 1. Basic Rendering Tests

```typescript
it('should render component with props', () => {
  render(<SearchBar value="test" onChange={mockFn} />);
  expect(screen.getByDisplayValue('test')).toBeInTheDocument();
});
```

### 2. User Interaction Tests

```typescript
it('should call handler on user action', async () => {
  const user = userEvent.setup();
  const handleClick = vi.fn();

  render(<Button onClick={handleClick}>Click me</Button>);
  await user.click(screen.getByRole('button'));

  expect(handleClick).toHaveBeenCalledTimes(1);
});
```

### 3. Conditional Rendering Tests

```typescript
it('should show element when condition is true', () => {
  render(<Component showElement={true} />);
  expect(screen.getByText('Element')).toBeInTheDocument();
});

it('should hide element when condition is false', () => {
  render(<Component showElement={false} />);
  expect(screen.queryByText('Element')).not.toBeInTheDocument();
});
```

### 4. Variant/Style Tests

```typescript
it('should apply danger variant styling', () => {
  render(<Button variant="danger">Delete</Button>);

  const button = screen.getByRole('button');
  expect(button.className).toContain('bg-red-600');
});
```

### 5. Accessibility Tests

```typescript
it('should have proper ARIA attributes', () => {
  render(<Dialog isOpen={true} title="Confirm" />);

  const dialog = screen.getByRole('dialog');
  expect(dialog).toHaveAttribute('aria-label', 'Confirm');
});
```

### 6. Context/Provider Tests

```typescript
it('should use authentication context', () => {
  mockUseAuth.mockReturnValue({
    isAuthenticated: true,
    user: mockUser
  });

  render(<ProtectedRoute><Content /></ProtectedRoute>);
  expect(screen.getByText('Content')).toBeInTheDocument();
});
```

## Running Tests

### Run all component tests:

```bash
npm test -- tests/components
```

### Run specific component test file:

```bash
npm test -- tests/components/common/SearchBar.test.tsx
```

### Run tests in watch mode:

```bash
npm test -- --watch tests/components
```

### Run tests with UI:

```bash
npm run test:ui
```

### Generate coverage report:

```bash
npm run test:coverage
```

## Component Test Details

### SearchBar Component (17 tests)

- ✅ Renders with default and custom placeholders
- ✅ Displays current value
- ✅ Shows/hides clear button based on value
- ✅ Calls onChange on user input
- ✅ Clears value when clear button clicked
- ✅ Handles rapid typing and special characters
- ✅ Proper accessibility attributes

### ConfirmDialog Component (27 tests)

- ✅ Shows/hides based on isOpen prop
- ✅ Displays custom title and message
- ✅ Renders with danger, primary, warning variants
- ✅ Shows appropriate icons for each variant
- ✅ Calls onConfirm and onCancel handlers
- ✅ Closes on backdrop click
- ✅ Proper modal structure and z-index

### ExpirationBadge Component (33 tests)

- ✅ Shows "No expiration" when no date provided
- ✅ Displays "Expired" for past dates
- ✅ Shows "Expiring soon" within warning threshold
- ✅ Displays valid status for future dates
- ✅ Uses custom warning days threshold
- ✅ Handles singular/plural days text
- ✅ Proper color coding (red/yellow/gray)

### WarrantyBadge Component (included in ExpirationBadge tests)

- ✅ Shows "No warranty" when no date provided
- ✅ Displays expired status
- ✅ Shows warning for warranties expiring within 60 days
- ✅ Proper styling and icons

### Breadcrumb Component (23 tests)

- ✅ Renders manual breadcrumb items
- ✅ Auto-generates breadcrumbs from URL
- ✅ Creates clickable links for intermediate items
- ✅ Renders separator icons
- ✅ Capitalizes path segments
- ✅ Uses predefined labels for known paths
- ✅ Proper ARIA labels and semantic HTML

### Tooltip Component (23 tests)

- ✅ Shows on hover, hides on unhover
- ✅ Supports top, bottom, left, right positions
- ✅ Displays custom content
- ✅ Handles long content and special characters
- ✅ Doesn't show when content is empty
- ✅ Non-interactive (pointer-events-none)
- ✅ Proper z-index and styling

### Skeleton Component (36 tests)

- ✅ Renders text, card, table, circle variants
- ✅ Supports count prop for multiple skeletons
- ✅ Applies custom className
- ✅ Has animation and proper styling
- ✅ TableSkeleton with configurable rows
- ✅ CardSkeleton with responsive grid
- ✅ Varied skeleton sizes within cards

### ProtectedRoute Component (16 tests)

- ✅ Shows loading indicator when loading
- ✅ Redirects to login when not authenticated
- ✅ Renders children when authenticated
- ✅ Checks required roles
- ✅ Shows access denied for insufficient roles
- ✅ Handles state transitions (loading → authenticated)
- ✅ Renders complex child components

## Best Practices

### 1. Test Organization

- Group related tests using `describe` blocks
- Use descriptive test names starting with "should"
- Organize by feature/behavior, not implementation

### 2. Test Independence

- Each test should be independent
- Use `beforeEach` and `afterEach` for setup/cleanup
- Clear mocks between tests

### 3. User-Centric Testing

- Use Testing Library queries that match user behavior
- Prefer `getByRole`, `getByLabelText` over `getByTestId`
- Test what users see and do, not implementation details

### 4. Async Testing

- Use `userEvent` for realistic user interactions
- Always `await` async operations
- Use `screen.findBy*` for elements that appear asynchronously

### 5. Accessibility

- Test ARIA attributes and roles
- Verify keyboard navigation
- Check semantic HTML usage

## Mocking Strategies

### Mocking Hooks

```typescript
vi.mock("@/contexts/AuthContext", () => ({
  useAuth: () => mockUseAuth(),
}));
```

### Mocking Utilities

```typescript
vi.mock("@/utils/dateUtils", () => ({
  formatDate: vi.fn((date) => date),
  getExpirationStatus: vi.fn(),
}));
```

### Mocking API Calls

```typescript
const mock = new MockAdapter(api);
mock.onGet("/api/items").reply(200, mockItems);
```

## Coverage Goals

Current component test coverage:

- **175 component tests** across 7 test files
- **100% pass rate** (all tests passing)
- Comprehensive coverage of:
  - Rendering logic
  - User interactions
  - Conditional rendering
  - Variants and styling
  - Accessibility
  - Edge cases

## Future Component Tests

Components that could be tested in the future:

- Toast components (ToastContainer, ToastItem)
- InventoryFilters component
- Layout and Navbar components
- InventoryTable component
- InventoryMobileCard component
- Page components

## Troubleshooting

### Common Issues

**Issue: Tests timeout**

- Increase timeout in vitest.config.ts
- Check for missing `await` on async operations

**Issue: Element not found**

- Use `screen.debug()` to see rendered HTML
- Check if element is conditionally rendered
- Verify query selectors match actual DOM

**Issue: Mock not working**

- Ensure mock is defined before import
- Clear mocks in `afterEach`
- Check mock implementation matches actual API

**Issue: Router-related errors**

- Use `renderWithProviders` instead of plain `render`
- Set `initialRoute` option for path-dependent tests

## Resources

- [Vitest Documentation](https://vitest.dev/)
- [Testing Library React](https://testing-library.com/docs/react-testing-library/intro/)
- [Testing Library User Events](https://testing-library.com/docs/user-event/intro)
- [Jest DOM Matchers](https://github.com/testing-library/jest-dom)

## Contributing

When adding new component tests:

1. Create test file in appropriate directory
2. Follow existing naming convention: `ComponentName.test.tsx`
3. Import from test utils: `import { render } from '../../utils/testUtils'`
4. Organize tests with `describe` blocks
5. Write descriptive test names
6. Cover happy path, edge cases, and errors
7. Ensure all tests pass before committing
8. Update this README with new test counts

## Test Statistics

```
Total Tests:        289
Component Tests:    175 (60.6%)
Service Tests:      114 (39.4%)
Pass Rate:          100%
Average Duration:   ~3s
```

## Author

Generated for mktekhub-frontend project
Date: 2025-01-17
