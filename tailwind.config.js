/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          primary: '#1B2A4A',
          secondary: '#101828',
          accent: '#E1141C',
          redSoft: '#FDE7E8',
          blueAccent: '#E7EAF0',
          successSoft: '#DCFCE7',
          warningSoft: '#FEF3C7',
          info: '#1B2A4A',
          infoSoft: '#E7EAF0',
        },
        ink: {
          primary: '#101828',
          secondary: '#667085',
        },
        surface: {
          DEFAULT: '#FFFFFF',
          muted: '#F7F8FA',
        },
        line: '#E2E6ED',
        success: '#22C55E',
        warning: '#F59E0B',
        info: '#1B2A4A',
      },
      borderRadius: {
        card: '16px',
      },
      boxShadow: {
        soft: '0 14px 40px rgba(17, 24, 39, 0.08)',
        glow: '0 18px 50px rgba(225, 20, 28, 0.18)',
        blue: '0 18px 50px rgba(27, 42, 74, 0.18)',
        info: '0 18px 50px rgba(27, 42, 74, 0.14)',
      },
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui'],
      },
    },
  },
  plugins: [],
};
