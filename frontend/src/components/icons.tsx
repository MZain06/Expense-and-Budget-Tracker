// Small line icons. They are decorative, so each button that uses one needs an aria-label.

type IconProps = { className?: string };

const stroke = {
  fill: "none",
  stroke: "currentColor",
  strokeWidth: 1.6,
  strokeLinecap: "round" as const,
  strokeLinejoin: "round" as const,
};

export function ChevronLeftIcon({ className = "size-4" }: IconProps) {
  return (
    <svg viewBox="0 0 20 20" className={className} aria-hidden="true">
      <path d="M12.5 15 7.5 10l5-5" {...stroke} />
    </svg>
  );
}

export function ChevronRightIcon({ className = "size-4" }: IconProps) {
  return (
    <svg viewBox="0 0 20 20" className={className} aria-hidden="true">
      <path d="m7.5 5 5 5-5 5" {...stroke} />
    </svg>
  );
}

export function PencilIcon({ className = "size-4" }: IconProps) {
  return (
    <svg viewBox="0 0 20 20" className={className} aria-hidden="true">
      <path d="M13.5 3.5a2 2 0 0 1 3 3L7.5 15.5 3.5 16.5l1-4 9-9Z" {...stroke} />
    </svg>
  );
}

export function TrashIcon({ className = "size-4" }: IconProps) {
  return (
    <svg viewBox="0 0 20 20" className={className} aria-hidden="true">
      <path d="M3.5 5.5h13M8 5.5v-2h4v2m-6.5 0 .8 11h7.4l.8-11" {...stroke} />
    </svg>
  );
}

export function ArrowUpIcon({ className = "size-4" }: IconProps) {
  return (
    <svg viewBox="0 0 20 20" className={className} aria-hidden="true">
      <path d="M10 16V4m-5 5 5-5 5 5" {...stroke} />
    </svg>
  );
}

export function ArrowDownIcon({ className = "size-4" }: IconProps) {
  return (
    <svg viewBox="0 0 20 20" className={className} aria-hidden="true">
      <path d="M10 4v12m-5-5 5 5 5-5" {...stroke} />
    </svg>
  );
}

export function AlertIcon({ className = "size-4" }: IconProps) {
  return (
    <svg viewBox="0 0 20 20" className={className} aria-hidden="true">
      <circle cx="10" cy="10" r="7.25" {...stroke} />
      <path d="M10 6.5v4M10 13.5h.01" {...stroke} />
    </svg>
  );
}
