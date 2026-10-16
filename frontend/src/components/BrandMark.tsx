/** The app's logo: a pound sign on a banknote-green tile. */
export function BrandMark({ className = "size-7" }: { className?: string }) {
  return (
    <svg viewBox="0 0 32 32" className={className} aria-hidden="true">
      <rect width="32" height="32" rx="8" className="fill-brand" />
      <text
        x="16"
        y="23"
        textAnchor="middle"
        fontSize="20"
        fontWeight="700"
        className="fill-white font-sans"
      >
        £
      </text>
    </svg>
  );
}
