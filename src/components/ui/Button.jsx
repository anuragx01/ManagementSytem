import React from "react";
export default function Button({ children, variant = 'primary', className = '', ...props }) {
  const variants = {
    primary: 'bg-brand-accent text-white hover:bg-red-700 shadow-glow',
    secondary: 'bg-brand-primary text-white hover:bg-brand-secondary shadow-blue',
    blue: 'bg-brand-primary text-white hover:bg-brand-secondary shadow-blue',
    info: 'bg-info text-white hover:bg-blue-700 shadow-info',
    ghost: 'bg-transparent text-ink-secondary hover:bg-brand-blueAccent hover:text-brand-secondary',
  };

  return (
    <button
      className={`inline-flex items-center justify-center gap-2 rounded-2xl px-4 py-2.5 text-sm font-semibold transition duration-200 ${variants[variant]} ${className}`}
      {...props}
    >
      {children}
    </button>
  );
}
