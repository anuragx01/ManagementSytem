import React from 'react';

export default function PageHeader({ kicker, title, description, actions }) {
  return (
    <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-start">
      <div className="page-header">
        {kicker && <p className="page-kicker">{kicker}</p>}
        {title && <h1 className="page-title">{title}</h1>}
        {description && <p className="mt-2 max-w-2xl text-sm leading-6 text-ink-secondary">{description}</p>}
      </div>
      {actions && <div className="flex flex-wrap gap-3">{actions}</div>}
    </div>
  );
}

