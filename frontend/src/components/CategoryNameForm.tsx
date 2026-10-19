import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { ApiError } from "../api/client";
import { Button } from "./Button";
import { TextField } from "./TextField";

const nameSchema = z.object({
  name: z.string().trim().min(1, "Enter a name").max(50, "Keep it to 50 characters or fewer"),
});

type NameValues = z.infer<typeof nameSchema>;

type CategoryNameFormProps = {
  label: string;
  submitLabel: string;
  defaultName?: string;
  autoFocus?: boolean;
  /** Saves the name. If it throws an ApiError, the server's message appears under the field. */
  onSave: (name: string) => Promise<unknown>;
  onCancel?: () => void;
};

/** The one-field form used both to add a category and to rename one. */
export function CategoryNameForm({
  label,
  submitLabel,
  defaultName = "",
  autoFocus = false,
  onSave,
  onCancel,
}: CategoryNameFormProps) {
  const {
    register,
    handleSubmit,
    setError,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<NameValues>({
    resolver: zodResolver(nameSchema),
    defaultValues: { name: defaultName },
  });

  async function onSubmit(values: NameValues) {
    try {
      await onSave(values.name);
      reset({ name: "" });
    } catch (error) {
      setError("name", {
        message:
          error instanceof ApiError
            ? (error.fieldErrors.name ?? error.message)
            : "Couldn't reach the server. Try again.",
      });
    }
  }

  return (
    <form
      onSubmit={handleSubmit(onSubmit)}
      noValidate
      className="grid gap-3 sm:grid-cols-[minmax(0,1fr)_auto] sm:items-start"
    >
      <TextField
        label={label}
        autoComplete="off"
        autoFocus={autoFocus}
        error={errors.name?.message}
        {...register("name")}
      />
      <div className="flex gap-2 sm:mt-6">
        <Button type="submit" disabled={isSubmitting}>
          {submitLabel}
        </Button>
        {onCancel && (
          <Button variant="secondary" onClick={onCancel}>
            Cancel
          </Button>
        )}
      </div>
    </form>
  );
}
