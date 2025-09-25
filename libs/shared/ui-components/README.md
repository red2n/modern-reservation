# Modern Reservation UI Components

A comprehensive React component library built with Tailwind CSS for the Modern Reservation System. This library provides reusable, accessible, and customizable UI components designed specifically for hospitality and reservation management applications.

## Features

- 🎨 **Tailwind CSS Integration**: Utility-first styling with consistent design tokens
- ♿ **Accessibility First**: Built with Radix UI primitives for excellent accessibility
- 🎯 **TypeScript Support**: Full type safety and excellent developer experience
- 📱 **Responsive Design**: Mobile-first components that work across all devices
- 🎪 **Storybook Documentation**: Interactive component documentation and testing
- 🧪 **Comprehensive Testing**: Unit tests with Jest and React Testing Library
- 🔧 **Customizable**: Easy theming and variant system with class-variance-authority

## Installation

```bash
npm install @modern-reservation/ui-components
```

## Usage

```tsx
import { Button, Card, Input } from '@modern-reservation/ui-components';
import '@modern-reservation/ui-components/styles';

function App() {
  return (
    <Card>
      <Input placeholder="Guest name" />
      <Button variant="primary">Make Reservation</Button>
    </Card>
  );
}
```

## Available Components

### Form Components
- `Button` - Primary, secondary, outline, ghost variants
- `Input` - Text, email, password, number inputs
- `Textarea` - Multi-line text input
- `Select` - Dropdown selection component
- `Checkbox` - Binary selection component
- `RadioGroup` - Single selection from multiple options
- `Switch` - Toggle component
- `Label` - Form field labels

### Layout Components
- `Card` - Container component with elevation
- `Separator` - Visual divider component
- `Tabs` - Tabbed interface component
- `Accordion` - Collapsible content sections

### Feedback Components
- `Toast` - Notification component
- `Alert` - Status and informational messages
- `Progress` - Progress indication
- `Spinner` - Loading indicator

### Navigation Components
- `Dropdown` - Context menu component
- `Tooltip` - Hover information component
- `Popover` - Floating content container

### Data Display
- `Avatar` - User profile images
- `Badge` - Status and category indicators
- `Table` - Data table component (coming soon)
- `Pagination` - Page navigation (coming soon)

## Component Variants

Most components support multiple variants through the `variant` prop:

```tsx
<Button variant="primary">Primary Action</Button>
<Button variant="secondary">Secondary Action</Button>
<Button variant="outline">Outline Button</Button>
<Button variant="ghost">Ghost Button</Button>
```

## Theming

The component library uses Tailwind CSS custom properties for theming. You can customize the appearance by overriding the CSS custom properties in your application:

```css
:root {
  --primary-50: #eff6ff;
  --primary-500: #3b82f6;
  --primary-900: #1e3a8a;
  /* ... other color tokens */
}
```

## Development

### Setup
```bash
npm install
```

### Build
```bash
npm run build
```

### Development
```bash
npm run dev
```

### Testing
```bash
npm test
npm run test:watch
```

### Storybook
```bash
npm run storybook
```

## Design System

This component library follows the Modern Reservation System design principles:

- **Hospitality-Focused**: Components designed for hotel and accommodation workflows
- **Professional**: Clean, modern aesthetic suitable for business applications
- **Consistent**: Unified spacing, typography, and color systems
- **Scalable**: Components that work from mobile to desktop

## Contributing

1. Create components in `src/components/`
2. Add Storybook stories in `src/stories/`
3. Include unit tests in `src/__tests__/`
4. Update this README with new components
5. Follow the existing TypeScript and styling patterns

## License

MIT License - see LICENSE file for details
