/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    '../backend/src/main/resources/templates/**/*.html',
  ],
  safelist: [
    'bg-emerald-600', 'hover:bg-emerald-700',
    'bg-slate-600', 'hover:bg-slate-700',
    'bg-teal-600', 'hover:bg-teal-700',
    'bg-blue-600', 'hover:bg-blue-700',
    'from-green-50', 'to-emerald-100',
    'from-slate-50', 'to-gray-100',
    'from-teal-50', 'to-cyan-100',
    'from-blue-50', 'to-sky-100',
    'text-emerald-600', 'text-slate-600', 'text-teal-600',
    'focus:ring-emerald-500', 'focus:ring-slate-500', 'focus:ring-teal-500',
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}
