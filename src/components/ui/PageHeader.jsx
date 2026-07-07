import React from 'react';
import { Moon, Sun, Sunrise, Sunset } from 'lucide-react';

function GreetingIcon({ title }) {
  if (!/^Good (Morning|Afternoon|Evening|Night),/.test(title || '')) return null;

  const Icon = title.startsWith('Good Morning')
    ? Sunrise
    : title.startsWith('Good Afternoon')
      ? Sun
      : title.startsWith('Good Evening')
        ? Sunset
        : Moon;

  return (
    <span className="grid h-10 w-10 shrink-0 place-items-center rounded-2xl bg-brand-blueAccent text-brand-secondary">
      <Icon className="h-5 w-5" aria-hidden="true" />
    </span>
  );
}

export default function PageHeader({ kicker, title, description, actions }) {
  return (
    <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-start">
      <div className="page-header">
        {kicker && <p className="page-kicker">{kicker}</p>}
        {title && (
          <div className="mt-2 flex items-center gap-3">
            <GreetingIcon title={title} />
            <h1 className="page-title mt-0">{title}</h1>
          </div>
        )}
        {description && <p className="mt-2 max-w-2xl text-sm leading-6 text-ink-secondary">{description}</p>}
      </div>
      {actions && <div className="flex flex-wrap gap-3">{actions}</div>}
    </div>
  );
}

