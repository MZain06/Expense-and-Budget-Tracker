import { type ComponentProps, useId } from "react";

type TextFieldProps = ComponentProps<"input"> & {
  label: string;
  error?: string;
};

/** A labelled input that shows its validation message underneath. */
export function TextField({ label, error, id, className = "", ...inputProps }: TextFieldProps) {
  const generatedId = useId();
  const inputId = id ?? generatedId;
  const errorId = `${inputId}-error`;

  return (
    <div className={className}>
      <label htmlFor={inputId} className="block text-sm font-medium">
        {label}
      </label>
      <input
        id={inputId}
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? errorId : undefined}
        className="mt-1 block w-full rounded-md border border-line bg-surface px-3 py-2 text-base aria-invalid:border-danger"
        {...inputProps}
      />
      {error && (
        <p id={errorId} className="mt-1 text-sm text-danger">
          {error}
        </p>
      )}
    </div>
  );
}
