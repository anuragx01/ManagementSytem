import React from "react";
export default function ProgressBar({ value }) {
  return (
    <div className="h-2.5 overflow-hidden rounded-full bg-slate-100">
      <div
        className="h-full rounded-full bg-gradient-to-r from-brand-primary via-info to-brand-secondary transition-all duration-500"
        style={{ width: `${value}%` }}
      />
    </div>
  );
}
