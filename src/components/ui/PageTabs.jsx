import React from 'react';

export default function PageTabs({ tabs, activeTab, onChange }) {
  return (
    <div className="border-b border-line" role="tablist" aria-label="Section tabs">
      <div className="flex gap-1 overflow-x-auto pb-px">
        {tabs.map((tab) => {
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              type="button"
              role="tab"
              aria-selected={isActive}
              className={`whitespace-nowrap rounded-t-2xl px-4 py-3 text-sm font-bold transition duration-200 focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-brand-redSoft ${
                isActive
                  ? 'border border-b-white border-line bg-white text-brand-primary shadow-sm'
                  : 'text-ink-secondary hover:bg-white/70 hover:text-ink-primary'
              }`}
              onClick={() => onChange(tab.id)}
            >
              {tab.label}
            </button>
          );
        })}
      </div>
    </div>
  );
}

