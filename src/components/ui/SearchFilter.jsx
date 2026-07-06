import React from "react";
import { Search, SlidersHorizontal } from 'lucide-react';

export default function SearchFilter({
  placeholder = 'Search',
  filterLabel = 'All Departments',
  options = [],
  searchValue = '',
  onSearchChange,
  filterValue = '',
  onFilterChange,
}) {
  return (
    <div className="flex flex-col gap-4 sm:flex-row">
      <label className="relative flex-1">
        <Search className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-ink-secondary" aria-hidden="true" />
        <input
          className="field-control pl-11"
          placeholder={placeholder}
          aria-label={placeholder}
          value={searchValue}
          onChange={(event) => onSearchChange?.(event.target.value)}
        />
      </label>
      <label className="relative min-w-52">
        <SlidersHorizontal className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-ink-secondary" aria-hidden="true" />
        <select
          className="select-control pl-11"
          aria-label={filterLabel}
          value={filterValue}
          onChange={(event) => onFilterChange?.(event.target.value)}
        >
          <option value="">{filterLabel}</option>
          {options.map((option) => (
            <option key={option} value={option}>{option}</option>
          ))}
        </select>
      </label>
    </div>
  );
}

