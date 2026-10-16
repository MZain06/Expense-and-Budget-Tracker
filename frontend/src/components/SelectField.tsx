import { type ComponentProps, useId } from "react";

type SelectFieldProps = ComponentProps<"select"> & {
  label: string;
  error?: string;
};

/** A labelled dropdown that shows its validation message underneath. */
export function SelectField({
  label,
  error,
  id,
  className = "",
  children,
  ...selectProps
}: SelectFieldProps) {
  const generatedId = useId();
  const selectId = id ?? generatedId;
  const errorId = `${selectId}-error`;

  return (
    <div className={className}>
      <label htmlFor={selectId} className="block text-sm font-medium">
        {label}
      </label>
      <select
        id={selectId}
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? errorId : undefined}
        className="mt-1 block w-full rounded-md border border-line bg-surface px-3 py-2 text-base aria-invalid:border-danger"
        {...selectProps}
      >
        {children}
      </select>
      {error && (
        <p id={errorId} className="mt-1 text-sm text-danger">
          {error}
        </p>
      )}
    </div>
  );
}
