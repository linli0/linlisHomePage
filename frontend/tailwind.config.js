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
        primary: {
          50: '#f0f9ff',
          100: '#e0f2fe',
          200: '#bae6fd',
          300: '#7dd3fc',
          400: '#38bdf8',
          500: '#0ea5e9',
          600: '#0284c7',
          700: '#0369a1',
          800: '#075985',
          900: '#0c4a6e',
          950: '#082f49',
        },
        gold: {
          50: '#fffbeb',
          100: '#fef3c7',
          200: '#fde68a',
          300: '#fcd34d',
          400: '#fbbf24',
          500: '#f59e0b',
          600: '#d97706',
          700: '#b45309',
          800: '#92400e',
          900: '#78350f',
          950: '#451a03',
        },
        accent: {
          50: '#fdf4ff',
          100: '#fae8ff',
          200: '#f5d0fe',
          300: '#f0abfc',
          400: '#e879f9',
          500: '#d946ef',
          600: '#c026d3',
          700: '#a21caf',
          800: '#86198f',
          900: '#701a75',
          950: '#4a044e',
        },
        surface: {
          50: '#fafafa',
          100: '#f4f4f5',
          200: '#e4e4e7',
          300: '#d4d4d8',
          400: '#a1a1aa',
          500: '#71717a',
          600: '#52525b',
          700: '#3f3f46',
          800: '#27272a',
          900: '#18181b',
          950: '#09090b',
        },
        cyber: {
          cyan: {
            400: '#22f7ff',
            500: '#00f0ff',
            600: '#00c4cc',
          },
          magenta: {
            400: '#ff5a8c',
            500: '#ff2a6d',
            600: '#d91a5a',
          },
          purple: {
            400: '#a855f7',
            500: '#8a2be2',
            600: '#7c3aed',
          },
          yellow: {
            400: '#fef34d',
            500: '#fcee0a',
            600: '#d9c909',
          },
          green: {
            400: '#3bffb8',
            500: '#05ffa1',
            600: '#04cc81',
          },
          void: {
            700: '#1a1a2e',
            800: '#12121f',
            900: '#0d0d14',
            950: '#0a0a0f',
          },
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
        mono: ['JetBrains Mono', 'Fira Code', 'monospace'],
      },
      boxShadow: {
        'soft': '0 2px 15px -3px rgba(0, 0, 0, 0.07), 0 10px 20px -2px rgba(0, 0, 0, 0.04)',
        'soft-lg': '0 10px 40px -10px rgba(0, 0, 0, 0.1), 0 2px 10px -2px rgba(0, 0, 0, 0.04)',
        'soft-xl': '0 20px 60px -15px rgba(0, 0, 0, 0.15)',
        'glow': '0 0 20px rgba(14, 165, 233, 0.3)',
        'glow-gold': '0 0 20px rgba(245, 158, 11, 0.3)',
        'glow-accent': '0 0 20px rgba(217, 70, 239, 0.3)',
        'inner-soft': 'inset 0 2px 4px 0 rgba(0, 0, 0, 0.05)',
        'neon-cyan': '0 0 5px rgba(0, 240, 255, 0.7), 0 0 10px rgba(0, 240, 255, 0.5), 0 0 20px rgba(0, 240, 255, 0.3), 0 0 40px rgba(0, 240, 255, 0.2)',
        'neon-magenta': '0 0 5px rgba(255, 42, 109, 0.7), 0 0 10px rgba(255, 42, 109, 0.5), 0 0 20px rgba(255, 42, 109, 0.3), 0 0 40px rgba(255, 42, 109, 0.2)',
        'neon-purple': '0 0 5px rgba(138, 43, 226, 0.7), 0 0 10px rgba(138, 43, 226, 0.5), 0 0 20px rgba(138, 43, 226, 0.3), 0 0 40px rgba(138, 43, 226, 0.2)',
        'neon-green': '0 0 5px rgba(5, 255, 161, 0.7), 0 0 10px rgba(5, 255, 161, 0.5), 0 0 20px rgba(5, 255, 161, 0.3), 0 0 40px rgba(5, 255, 161, 0.2)',
        'neon-yellow': '0 0 5px rgba(252, 238, 10, 0.7), 0 0 10px rgba(252, 238, 10, 0.5), 0 0 20px rgba(252, 238, 10, 0.3), 0 0 40px rgba(252, 238, 10, 0.2)',
      },
      backgroundImage: {
        'gradient-radial': 'radial-gradient(var(--tw-gradient-stops))',
        'gradient-conic': 'conic-gradient(from 180deg at 50% 50%, var(--tw-gradient-stops))',
        'mesh-gradient': 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        'hero-pattern': "url(\"data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E\")",
      },
      animation: {
        'float': 'float 6s ease-in-out infinite',
        'float-slow': 'float 8s ease-in-out infinite',
        'pulse-slow': 'pulse 4s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'gradient': 'gradient 8s ease infinite',
        'shimmer': 'shimmer 2s linear infinite',
        'slide-up': 'slideUp 0.5s ease-out',
        'slide-down': 'slideDown 0.5s ease-out',
        'fade-in': 'fadeIn 0.5s ease-out',
        'scale-in': 'scaleIn 0.3s ease-out',
        'bounce-subtle': 'bounceSubtle 2s ease-in-out infinite',
        'spin-slow': 'spin 3s linear infinite',
        'wiggle': 'wiggle 1s ease-in-out infinite',
        'glitch': 'glitch 2s infinite',
        'scanline': 'scanline 8s linear infinite',
        'pulse-neon': 'pulseNeon 2s ease-in-out infinite',
      },
      keyframes: {
        float: {
          '0%, 100%': { transform: 'translateY(0)' },
          '50%': { transform: 'translateY(-20px)' },
        },
        gradient: {
          '0%, 100%': { backgroundPosition: '0% 50%' },
          '50%': { backgroundPosition: '100% 50%' },
        },
        shimmer: {
          '0%': { backgroundPosition: '-200% 0' },
          '100%': { backgroundPosition: '200% 0' },
        },
        slideUp: {
          '0%': { transform: 'translateY(20px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        slideDown: {
          '0%': { transform: 'translateY(-20px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        scaleIn: {
          '0%': { transform: 'scale(0.9)', opacity: '0' },
          '100%': { transform: 'scale(1)', opacity: '1' },
        },
        bounceSubtle: {
          '0%, 100%': { transform: 'translateY(0)' },
          '50%': { transform: 'translateY(-5px)' },
        },
        wiggle: {
          '0%, 100%': { transform: 'rotate(-3deg)' },
          '50%': { transform: 'rotate(3deg)' },
        },
        glitch: {
          '0%, 100%': { 
            transform: 'translate(0)',
            textShadow: '0.05em 0 0 rgba(0, 240, 255, 0.75), -0.05em -0.025em 0 rgba(255, 42, 109, 0.75)'
          },
          '14%': { 
            transform: 'translate(-0.05em, -0.025em)',
            textShadow: '0.05em 0 0 rgba(0, 240, 255, 0.75), -0.05em -0.025em 0 rgba(255, 42, 109, 0.75)'
          },
          '15%': { 
            transform: 'translate(0.05em, 0.025em)',
            textShadow: '-0.05em 0 0 rgba(0, 240, 255, 0.75), 0.05em 0.025em 0 rgba(255, 42, 109, 0.75)'
          },
          '49%': { 
            transform: 'translate(0.05em, -0.025em)',
            textShadow: '-0.05em 0 0 rgba(0, 240, 255, 0.75), 0.05em 0.025em 0 rgba(255, 42, 109, 0.75)'
          },
          '50%': { 
            transform: 'translate(-0.05em, 0.025em)',
            textShadow: '0.05em 0 0 rgba(0, 240, 255, 0.75), -0.05em -0.025em 0 rgba(255, 42, 109, 0.75)'
          },
          '99%': { 
            transform: 'translate(-0.05em, -0.025em)',
            textShadow: '0.05em 0 0 rgba(0, 240, 255, 0.75), -0.05em -0.025em 0 rgba(255, 42, 109, 0.75)'
          },
        },
        scanline: {
          '0%': { transform: 'translateY(-100%)' },
          '100%': { transform: 'translateY(100vh)' },
        },
        pulseNeon: {
          '0%, 100%': { 
            filter: 'brightness(1) drop-shadow(0 0 5px rgba(0, 240, 255, 0.5))',
          },
          '50%': { 
            filter: 'brightness(1.3) drop-shadow(0 0 20px rgba(0, 240, 255, 0.8))',
          },
        },
      },
      transitionTimingFunction: {
        'bounce-in': 'cubic-bezier(0.68, -0.55, 0.265, 1.55)',
        'smooth': 'cubic-bezier(0.4, 0, 0.2, 1)',
      },
      backdropBlur: {
        xs: '2px',
      },
    },
  },
  plugins: [],
}
