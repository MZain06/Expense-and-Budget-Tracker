import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { ApiError } from "../api/client";
import { Button } from "./Button";
import { SelectField } from "./SelectField";
import { TextField } from "./TextField";

const categorySchema = z.object({
  name: z.string().trim().min(1, "Enter a name").max(50, "Keep it to 50 characters or fewer"),
  kind: z.enum(["SPENDING", "INCOME"]),
});

type CategoryValues = z.infer<typeof categorySchema>;

type CategoryFormProps = {
  /** The form's accessible name, like "Add a category". */
  title: string;
  nameLabel: string;
  submitLabel: string;
  defaultValues?: CategoryValues;
  autoFocus?: boolean;
  /** Saves the category. If it throws an ApiError, the server's message appears under the name. */
  onSave: (values: CategoryValues) => Promise<unknown>;
  onCancel?: () => void;
};

/** The form used both to add a category and to edit one. */
export function CategoryForm({
  title,
  nameLabel,
  submitLabel,
  defaultValues = { name: "", kind: "SPENDING" },
  autoFocus = false,
  onSave,
  onCancel,
}: CategoryFormProps) {
  const {
    register,
    handleSubmit,
    setError,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<CategoryValues>({
    resolver: zodResolver(categorySchema),
    defaultValues,
  });

  async function onSubmit(values: CategoryValues) {
    try {
      await onSave(values);
      reset({ name: "", kind: values.kind });
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
      aria-label={title}
      onSubmit={handleSubmit(onSubmit)}
      noValidate
      className="grid gap-3 sm:grid-cols-[minmax(0,1fr)_9rem_auto] sm:items-start"
    >
      <TextField
        label={nameLabel}
        autoComplete="off"
        autoFocus={autoFocus}
        error={errors.name?.message}
        {...register("name")}
      />
      <SelectField label="Type" {...register("kind")}>
        <option value="SPENDING">Spending</option>
        <option value="INCOME">Income</option>
      </SelectField>
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
