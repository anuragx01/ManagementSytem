import React, { useEffect } from 'react';
import { X } from 'lucide-react';
import Button from './Button';

export default function Modal({ open, title, description, children, onClose, footer, size = 'lg' }) {
  useEffect(() => {
    if (!open) return undefined;
    function onKeyDown(event) {
      if (event.key === 'Escape') onClose?.();
    }
    document.body.style.overflow = 'hidden';
    window.addEventListener('keydown', onKeyDown);
    return () => {
      document.body.style.overflow = '';
      window.removeEventListener('keydown', onKeyDown);
    };
  }, [open, onClose]);

  if (!open) return null;

  const sizeClass = {
    md: 'max-w-lg',
    lg: 'max-w-2xl',
    xl: 'max-w-4xl',
  }[size] || 'max-w-2xl';

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center p-4" role="dialog" aria-modal="true" aria-labelledby="modal-title">
      <button type="button" className="absolute inset-0 bg-ink-primary/40 backdrop-blur-[2px]" aria-label="Close modal" onClick={onClose} />
      <div className={`relative z-10 w-full ${sizeClass} max-h-[90vh] overflow-hidden rounded-panel border border-line bg-white shadow-panel`}>
        <div className="flex items-start justify-between gap-4 border-b border-line px-6 py-5">
          <div>
            <h2 id="modal-title" className="text-xl font-extrabold text-ink-primary">{title}</h2>
            {description && <p className="mt-1 text-sm text-ink-secondary">{description}</p>}
          </div>
          <button
            type="button"
            onClick={onClose}
            className="grid h-10 w-10 place-items-center rounded-xl text-ink-secondary transition hover:bg-surface-muted hover:text-ink-primary focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-brand-redSoft"
            aria-label="Close"
          >
            <X className="h-5 w-5" />
          </button>
        </div>
        <div className="overflow-y-auto no-scrollbar px-6 py-5">{children}</div>
        {footer && (
          <div className="flex flex-wrap items-center justify-end gap-3 border-t border-line px-6 py-4">
            {footer}
          </div>
        )}
      </div>
    </div>
  );
}

export function ModalActions({ onCancel, onConfirm, cancelLabel = 'Cancel', confirmLabel = 'Confirm', loading = false }) {
  return (
    <>
      <Button type="button" variant="secondary" onClick={onCancel}>{cancelLabel}</Button>
      <Button type="button" onClick={onConfirm} disabled={loading}>{confirmLabel}</Button>
    </>
  );
}

