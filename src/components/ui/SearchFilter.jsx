import React from "react";
import { Search, SlidersHorizontal } from 'lucide-react';

export default function SearchFilter({ placeholder = 'Search', filterLabel = 'All Departments', options = [] }) {
  return (
    <div className="flex flex-col gap-3 sm:flex-row">
      <label className="relative flex-1">
        <Search className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-ink-secondary" />
        <input
          className="w-full rounded-2xl border border-line bg-white py-3 pl-11 pr-4 text-sm outline-none transition focus:border-brand-secondary focus:ring-4 focus:ring-blue-100"
          placeholder={placeholder}
        />
      </label>
      <label className="relative min-w-52">
        <SlidersHorizontal className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-ink-secondary" />
        <select className="w-full appearance-none rounded-2xl border border-line bg-white py-3 pl-11 pr-4 text-sm font-medium text-ink-primary outline-none transition focus:border-info focus:ring-4 focus:ring-blue-100">
          <option>{filterLabel}</option>
          {options.map((option) => (
            <option key={option}>{option}</option>
          ))}
        </select>
      </label>
    </div>
  );
}
