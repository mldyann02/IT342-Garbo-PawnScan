import type { Config } from 'tailwindcss';

const config: Config = {
  content: [
    './app/**/*.{js,ts,jsx,tsx,mdx}',
    './components/**/*.{js,ts,jsx,tsx,mdx}',
    './lib/**/*.{js,ts,jsx,tsx,mdx}'
  ],
  theme: {
    extend: {
      colors: {
        brand: 'var(--color-brand)',
        'bg-main': 'var(--color-bg-main)',
        'status-clean': 'var(--color-status-clean)',
        'status-stolen': 'var(--color-status-stolen)',
        'border-muted': 'var(--color-border-muted)'
      }
    }
  },
  plugins: []
};

export default config;
