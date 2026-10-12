/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        // Vermelho-framboesa: a cor da marca, em botões, links e destaques.
        brand: {
          50: '#fff1f3',
          100: '#ffe0e6',
          200: '#ffc6d2',
          300: '#ff9aae',
          400: '#fb5f80',
          500: '#e8264f',
          600: '#c81e43',
          700: '#a3183a',
          800: '#861834',
          900: '#5c0f22',
        },
        // Tons quentes de apoio para banners e promoções.
        accent: {
          coral: '#ff7a59',
          amber: '#ffb800',
          plum: '#7b2cbf',
        },
      },
      fontFamily: {
        sans: ['"Nunito Sans"', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        card: '0 1px 2px rgb(17 24 39 / 0.04), 0 4px 16px rgb(17 24 39 / 0.06)',
      },
    },
  },
  plugins: [],
}
