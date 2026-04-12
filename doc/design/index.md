# Design Documentation

## 🎨 Design System Overview

CoffeeCookie's HomePage implements a cohesive design system based on Tailwind CSS with custom extensions for a unique brand identity.

### Color Palette

| Color | Hex | Usage |
|-------|-----|-------|
| **Primary** | `#0ea5e9` | Links, buttons, active states |
| **Gold** | `#f59e0b` | Highlights, pricing, important elements |
| **Accent** | `#d946ef` | Special features, AI elements |
| **Surface** | Various grays | Backgrounds, cards, surfaces |

### Typography

- **Primary Font**: Inter (Google Fonts)
- **Code Font**: JetBrains Mono
- **Font Weights**: 400 (regular), 500 (medium), 600 (semibold), 700 (bold)

### Spacing System

Based on Tailwind's spacing scale with custom additions:
- **Base Unit**: 4px
- **Common Spacings**: 2px, 4px, 8px, 12px, 16px, 24px, 32px, 48px, 64px

### Breakpoints

- **Mobile**: 320px - 767px
- **Tablet**: 768px - 1023px
- **Desktop**: 1024px+

## 🧱 Component Library

### Buttons
- `.btn`: Base button styling
- `.btn-primary`: Primary action buttons (blue)
- `.btn-gold`: Secondary/gold buttons (amber)
- `.btn-outline`: Outline variant
- `.btn-sm`, `.btn-lg`: Size variants

### Cards
- `.card`: Base card with subtle shadow
- `.card-hover`: Hover effects with elevation
- `.card-glass`: Glassmorphism effect with backdrop blur
- Responsive padding and spacing

### Inputs
- `.input`: Base input styling
- `.input-glass`: Glassmorphism input variant
- Validation states (success, error, warning)
- Consistent height and spacing

### Badges
- `.badge`: Small informational tags
- `.badge-primary`: Primary color variant
- `.badge-gold`: Gold color variant
- Used for status indicators, tags, categories

## ✨ Animations and Transitions

### Custom Animations
- **Float**: Gentle floating animation for hero elements
- **Gradient**: Animated gradient backgrounds for visual interest
- **Shimmer**: Loading skeleton animations
- **Slide-up**: Smooth entrance animations for content
- **Fade-in**: Opacity-based transitions
- **Scale-in**: Size-based entrance animations
- **Bounce-subtle**: Subtle bounce effects for interactions
- **Wiggle**: Playful wiggle animations for feedback

### Transition Timing
- **Default**: 300ms ease-in-out
- **Fast**: 150ms for immediate feedback
- **Slow**: 500ms for dramatic entrances

## 📱 Responsive Design Patterns

### Mobile-First Approach
All components are designed mobile-first with progressive enhancement for larger screens.

### Layout Patterns
- **Single Column**: Mobile layout (stacked elements)
- **Two Column**: Tablet layout (sidebar + main content)
- **Multi Column**: Desktop layout (complex grid arrangements)

### Navigation
- **Mobile**: Hamburger menu with slide-in drawer
- **Desktop**: Horizontal navigation bar with dropdown menus
- **Responsive**: Smooth transition between states

## 🌙 Dark Mode Support

The application supports dark mode through Tailwind's `dark:` prefix:

- **Light Mode**: Default color scheme
- **Dark Mode**: Inverted colors with reduced brightness
- **Toggle**: User can switch between modes
- **System Preference**: Respects OS-level dark mode setting

Dark mode classes are applied to the `<html>` element via JavaScript.

## 📊 Data Visualization

### Charts
- **Library**: Chart.js 4.4 + vue-chartjs wrapper
- **Styles**: Custom color schemes matching brand palette
- **Interactivity**: Hover tooltips, click events
- **Responsiveness**: Charts adapt to container size

### Price Display
- **Current Price**: Large, prominent display with gold accent
- **Change Indicators**: Color-coded arrows (green/red)
- **Statistics**: High/low/average/volatility metrics
- **Historical Data**: Interactive time period selection

## 🔧 Development Guidelines

### CSS Architecture
- **Utility-First**: Primarily Tailwind utility classes
- **Custom Classes**: For complex, reusable patterns
- **Component Scope**: Scoped styles within Vue components
- **Global Styles**: Minimal global CSS in `main.css`

### Naming Conventions
- **Classes**: kebab-case (e.g., `.gold-price-card`)
- **Components**: PascalCase (e.g., `PriceChart.vue`)
- **Variables**: camelCase (e.g., `currentPrice`)

### Performance Considerations
- **Bundle Size**: Tree-shaking enabled for unused utilities
- **Animations**: Hardware-accelerated where possible
- **Images**: Optimized and lazy-loaded
- **Fonts**: Preloaded critical fonts

## 🎯 Accessibility

### WCAG Compliance
- **Color Contrast**: Minimum 4.5:1 ratio for text
- **Keyboard Navigation**: Full tab support
- **Screen Reader**: ARIA labels and semantic HTML
- **Focus States**: Visible focus indicators

### Testing
- Manual keyboard navigation testing
- Screen reader compatibility verification
- Color contrast validation tools

## 📸 Visual References

Design mockups and visual references are maintained in the `design/` directory of the project repository.

## 🔄 Design Tokens

All design decisions are codified as design tokens in the Tailwind configuration:

```javascript
// tailwind.config.js
theme: {
  extend: {
    colors: {
      primary: '#0ea5e9',
      gold: '#f59e0b', 
      accent: '#d946ef',
      surface: {
        DEFAULT: '#f8fafc',
        dark: '#0f172a'
      }
    },
    animation: {
      'float': 'float 3s ease-in-out infinite',
      'gradient': 'gradient 8s linear infinite',
      // ... other animations
    }
  }
}
```

This ensures consistency across the entire application and makes it easy to update the design system globally.