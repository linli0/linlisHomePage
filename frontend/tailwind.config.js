/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        // Espresso bar: foam / caramel / roast (not SaaS amber)
        brand: {
          50:  '#F5EDE3',
          100: '#E8DFD4',
          200: '#D4C4B0',
          300: '#B8956C',
          400: '#C4873A',
          500: '#A66B2E',
          600: '#6B4423',
          700: '#4A2E18',
          800: '#2C1810',
          900: '#24160F',
          950: '#1A120B',
        },
        ink: {
          50:  '#F7F4F0',
          100: '#E8E2DA',
          200: '#D0C6B8',
          300: '#A89A8A',
          400: '#7A6E62',
          500: '#5C5248',
          600: '#453E38',
          700: '#342F2B',
          800: '#262220',
          900: '#1A1614',
          950: '#120F0D',
        },
      },
      fontFamily: {
        display: ['"Fraunces"', 'Georgia', 'serif'],
        sans:    ['"DM Sans"', 'system-ui', 'sans-serif'],
        mono:    ['"DM Mono"', 'ui-monospace', 'monospace'],
      },
      boxShadow: {
        'xs':   '0 1px 2px 0 rgb(26 18 11 / 0.04)',
        'sm':   '0 1px 3px 0 rgb(26 18 11 / 0.06)',
        'md':   '0 8px 24px -6px rgb(26 18 11 / 0.12)',
        'lg':   '0 16px 40px -8px rgb(26 18 11 / 0.18)',
        'glow': '0 0 0 3px rgb(196 135 58 / 0.25)',
        'glow-sm': '0 0 0 2px rgb(196 135 58 / 0.18)',
      },
      animation: {
        'fade-up': 'fadeUp 0.7s cubic-bezier(0.22, 1, 0.36, 1) both',
        'fade-up-delay': 'fadeUp 0.7s cubic-bezier(0.22, 1, 0.36, 1) 0.12s both',
        'fade-up-delay-2': 'fadeUp 0.7s cubic-bezier(0.22, 1, 0.36, 1) 0.24s both',
        'chalk-in': 'chalkIn 0.9s cubic-bezier(0.22, 1, 0.36, 1) 0.2s both',
      },
      keyframes: {
        fadeUp: {
          '0%': { opacity: '0', transform: 'translateY(14px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        chalkIn: {
          '0%': { opacity: '0', transform: 'translateY(10px) scale(0.98)' },
          '100%': { opacity: '1', transform: 'translateY(0) scale(1)' },
        },
      },
    },
  },
  plugins: [],
}
