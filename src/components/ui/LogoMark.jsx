import React, { useState } from 'react';

export default function LogoMark({ size = 'md' }) {
  const [hasImage, setHasImage] = useState(true);
  const sizes = {
    sm: 'h-11 w-11 text-lg',
    md: 'h-12 w-12 text-xl',
  };

  if (hasImage) {
    return (
      <img
        src="/logo.png"
        alt="NEXSTAR Logo"
        className={`${sizes[size]} rounded-2xl object-contain shadow-glow`}
        onError={() => setHasImage(false)}
      />
    );
  }

  return (
    <div className={`grid place-items-center rounded-2xl bg-brand-primary font-black text-white shadow-glow ${sizes[size]}`}>
      N
    </div>
  );
}

