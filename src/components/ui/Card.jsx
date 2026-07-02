import React from "react";
export default function Card({ children, className = '', interactive = true }) {
  return <section className={`card ${interactive ? 'premium-hover' : ''} ${className}`}>{children}</section>;
}
