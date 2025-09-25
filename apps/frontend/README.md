# Frontend Applications

## Architecture Overview
- **Angular 17+** with PWA capabilities
- **Angular Material** for consistent UI components
- **GraphQL** with Apollo Client for data fetching
- **NgRx** for state management
- **Socket.IO** for real-time updates

## Applications

### 1. Guest Portal (`guest-portal/`)
- **Purpose**: Public-facing reservation system
- **Features**: Room search, booking, guest profile, feedback
- **Tech Stack**: Angular 17, Material Design, PWA
- **Performance**: Optimized for mobile-first experience

### 2. Staff Portal (`staff-portal/`)
- **Purpose**: Front desk and operational staff interface
- **Features**: Check-in/out, reservations, housekeeping, POS
- **Tech Stack**: Angular 17, Material Design, WebSocket integration
- **Performance**: Real-time updates for operational efficiency

### 3. Admin Portal (`admin-portal/`)
- **Purpose**: Property management and system administration
- **Features**: Configuration, reports, user management, analytics
- **Tech Stack**: Angular 17, Material Design, Advanced data visualization
- **Performance**: Dashboard optimization for complex data sets

### 4. Mobile App (`mobile-app/`)
- **Purpose**: Native-like mobile experience using PWA
- **Features**: Responsive design, offline capabilities, push notifications
- **Tech Stack**: Angular 17 PWA, Service Workers
- **Performance**: Optimized for mobile networks and offline usage

## Shared Features Across All Apps
- **PWA Capabilities**: Offline support, push notifications, app-like experience
- **Real-time Updates**: WebSocket integration for live data
- **Multi-language Support**: i18n for 10+ languages
- **Accessibility**: WCAG 2.1 Level AA compliance
- **Theme Support**: Dark/light mode switching
- **Security**: JWT authentication, role-based access control
