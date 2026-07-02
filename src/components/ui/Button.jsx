import React from "react";
export default function Button({ children, variant = 'primary', className = '', ...props }) {
  const variants = {
    primary: 'bg-brand-accent text-white shadow-glow hover:bg-red-700 focus-visible:ring-brand-redSoft',
    secondary: 'border border-line bg-white text-brand-secondary shadow-sm hover:border-slate-300 hover:bg-brand-blueAccent focus-visible:ring-brand-blueAccent',
    blue: 'bg-brand-primary text-white shadow-blue hover:bg-brand-secondary focus-visible:ring-brand-blueAccent',
    info: 'bg-info text-white shadow-info hover:bg-brand-secondary focus-visible:ring-brand-blueAccent',
    ghost: 'bg-transparent text-ink-secondary shadow-none hover:bg-brand-blueAccent hover:text-brand-secondary focus-visible:ring-brand-blueAccent',
  };

  return (
    <button
      className={`inline-flex min-h-11 items-center justify-center gap-2 rounded-2xl px-4 py-2.5 text-sm font-bold transition duration-200 ease-out hover:-translate-y-0.5 active:translate-y-0 active:scale-[0.98] disabled:cursor-not-allowed disabled:translate-y-0 disabled:opacity-60 focus-visible:outline-none focus-visible:ring-4 ${variants[variant]} ${className}`}
      {...props}
    >
      {children}
    </button>
  );
}
