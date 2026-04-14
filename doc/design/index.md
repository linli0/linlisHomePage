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

### New Components

#### KLineChart
Candlestick chart component using lightweight-charts library for financial data visualization.

- **Color Scheme**: 
  - Green (`#10b981`) for bullish candles (price increase)
  - Red (`#ef4444`) for bearish candles (price decrease)
- **Dark Mode Support**: Automatically adapts colors for dark mode with appropriate contrast
- **Responsive Design**: Adapts to container size with mobile-first approach
- **Interactivity**: Hover tooltips, zoom functionality, time period selection
- **Performance**: Optimized for real-time data updates

#### IndicatorOverlay
Non-visual overlay component for adding technical indicator lines to KLineChart.

- **Integration**: Works seamlessly with KLineChart component
- **Multiple Indicator Types**: Supports SMA, EMA, RSI, MACD, Bollinger Bands
- **Customizable**: Configurable colors, line styles, and parameters
- **Layer Management**: Multiple overlays can be stacked and toggled independently
- **Performance**: Lightweight implementation with minimal overhead

#### TweetCard
Social media post display card with platform-specific styling.

- **Platform Styling**:
  - Twitter/X: Blue accent (`#1d9bf0`) with white/light backgrounds
  - Truth Social: Red accent (`#ff4500`) with appropriate theming
- **Content Structure**:
  - Avatar: Circular user profile image
  - Header: Username, handle, timestamp, platform indicator
  - Body: Post content with proper text formatting
  - Metrics: Likes, retweets/shares, comments counts
- **Mobile-First Responsive**: Optimized for all screen sizes with touch-friendly interactions
- **Accessibility**: Proper ARIA labels and keyboard navigation support

#### TweetMetricsChart
Bar chart component for visualizing social media engagement statistics.

- **Engagement Metrics**: Displays likes, shares, comments, impressions
- **Platform Comparison**: Side-by-side comparison between different platforms
- **Time Series**: Historical performance tracking over selected periods
- **Interactive**: Clickable bars for detailed metrics, hover tooltips
- **Responsive**: Adapts layout for mobile (vertical) vs desktop (horizontal)

## ✨ Animations and Transitions

### Custom Animations
- **Float**: Gentle floating animation for hero elements (`float 6s ease-in-out infinite`)
- **Float-slow**: Slower floating variant (`float 8s ease-in-out infinite`)
- **Gradient**: Animated gradient backgrounds for visual interest (`gradient 8s ease infinite`)
- **Shimmer**: Loading skeleton animations (`shimmer 2s linear infinite`)
- **Slide-up**: Smooth entrance animations for content (`slideUp 0.5s ease-out`)
- **Slide-down**: Smooth exit animations for content (`slideDown 0.5s ease-out`)
- **Fade-in**: Opacity-based transitions (`fadeIn 0.5s ease-out`)
- **Scale-in**: Size-based entrance animations (`scaleIn 0.3s ease-out`)
- **Bounce-subtle**: Subtle bounce effects for interactions (`bounceSubtle 2s ease-in-out infinite`)
- **Spin-slow**: Slow rotation animation (`spin 3s linear infinite`)
- **Wiggle**: Playful wiggle animations for feedback (`wiggle 1s ease-in-out infinite`)

### Transition Timing
- **Default**: 300ms ease-in-out
- **Fast**: 150ms for immediate feedback
- **Slow**: 500ms for dramatic entrances
- **Bounce-in**: `cubic-bezier(0.68, -0.55, 0.265, 1.55)` for elastic effects
- **Smooth**: `cubic-bezier(0.4, 0, 0.2, 1)` for natural motion

## 🌑 Custom Shadows

### Soft Shadows
- **soft**: Subtle drop shadow (`0 2px 15px -3px rgba(0, 0, 0, 0.07), 0 10px 20px -2px rgba(0, 0, 0, 0.04)`)
- **soft-lg**: Larger soft shadow (`0 10px 40px -10px rgba(0, 0, 0, 0.1), 0 2px 10px -2px rgba(0, 0, 0, 0.04)`)
- **soft-xl**: Extra large soft shadow (`0 20px 60px -15px rgba(0, 0, 0, 0.15)`)

### Glow Effects
- **glow**: Primary color glow (`0 0 20px rgba(14, 165, 233, 0.3)`)
- **glow-gold**: Gold accent glow (`0 0 20px rgba(245, 158, 11, 0.3)`)
- **glow-accent**: Accent color glow (`0 0 20px rgba(217, 70, 239, 0.3)`)

### Inner Shadows
- **inner-soft**: Subtle inner shadow for depth (`inset 0 2px 4px 0 rgba(0, 0, 0, 0.05)`)

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
- **Library**: Chart.js 4.4 + vue-chartjs wrapper (existing)
- **New Library**: lightweight-charts for KLineChart component
- **Styles**: Custom color schemes matching brand palette
- **Interactivity**: Hover tooltips, click events
- **Responsiveness**: Charts adapt to container size

### Price Display
- **Current Price**: Large, prominent display with gold accent
- **Change Indicators**: Color-coded arrows (green/red)
- **Statistics**: High/low/average/volatility metrics
- **Historical Data**: Interactive time period selection
- **Candlestick Charts**: KLineChart for detailed price action analysis

## 📱 Social Media Card Design Guidelines

### Platform-Specific Design Principles

#### Twitter/X Cards
- **Primary Color**: Twitter blue (`#1d9bf0`)
- **Background**: White/light gray with subtle borders
- **Typography**: Clean, readable font with proper line height
- **Metrics**: Heart (likes), Retweet, Comment icons with counts
- **Verification**: Verified badge styling when applicable

#### Truth Social Cards  
- **Primary Color**: Truth Social red (`#ff4500`)
- **Background**: Light background with red accents
- **Typography**: Bold, attention-grabbing styling
- **Metrics**: Like, Repost, Comment with platform-specific icons
- **Branding**: Truth Social logo integration

### Universal Card Requirements
- **Avatar**: Circular, minimum 40px diameter on mobile
- **Content Truncation**: Smart text truncation with "Read more" expand
- **Media Support**: Image/video preview with proper aspect ratios
- **Timestamp**: Relative time display (e.g., "2h ago")
- **Accessibility**: Sufficient color contrast, keyboard navigable
- **Performance**: Lazy loading of images and media

### Responsive Behavior
- **Mobile**: Single column, vertical layout, touch-friendly tap targets
- **Tablet**: Enhanced layout with better spacing and larger elements  
- **Desktop**: Multi-column potential, hover states, detailed metrics

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
      'float': 'float 6s ease-in-out infinite',
      'float-slow': 'float 8s ease-in-out infinite',
      'gradient': 'gradient 8s ease infinite',
      'shimmer': 'shimmer 2s linear infinite',
      'slide-up': 'slideUp 0.5s ease-out',
      'slide-down': 'slideDown 0.5s ease-out',
      'fade-in': 'fadeIn 0.5s ease-out',
      'scale-in': 'scaleIn 0.3s ease-out',
      'bounce-subtle': 'bounceSubtle 2s ease-in-out infinite',
      'spin-slow': 'spin 3s linear infinite',
      'wiggle': 'wiggle 1s ease-in-out infinite',
    },
    boxShadow: {
      'soft': '0 2px 15px -3px rgba(0, 0, 0, 0.07), 0 10px 20px -2px rgba(0, 0, 0, 0.04)',
      'soft-lg': '0 10px 40px -10px rgba(0, 0, 0, 0.1), 0 2px 10px -2px rgba(0, 0, 0, 0.04)',
      'soft-xl': '0 20px 60px -15px rgba(0, 0, 0, 0.15)',
      'glow': '0 0 20px rgba(14, 165, 233, 0.3)',
      'glow-gold': '0 0 20px rgba(245, 158, 11, 0.3)',
      'glow-accent': '0 0 20px rgba(217, 70, 239, 0.3)',
      'inner-soft': 'inset 0 2px 4px 0 rgba(0, 0, 0, 0.05)',
    }
  }
}
```

This ensures consistency across the entire application and makes it easy to update the design system globally.