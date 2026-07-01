import React from "react";
export default function Card({ children, className = '' }) {
  return <section className={`card premium-hover ${className}`}>{children}</section>;
}
