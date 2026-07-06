import React from "react";
export default function ProgressBar({ value }) {
  return (
    <div
      className="h-2.5 overflow-hidden rounded-full bg-slate-100"
      role="progressbar"
      aria-valuenow={value}
      aria-valuemin={0}
      aria-valuemax={100}
    >
      <div
        className="h-full rounded-full bg-gradient-to-r from-brand-primary via-brand-secondary to-brand-accent transition-all duration-500"
        style={{ width: `${value}%` }}
      />
    </div>
  );
}

