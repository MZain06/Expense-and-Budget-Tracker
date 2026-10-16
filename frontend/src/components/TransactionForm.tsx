import { zodResolver } from "@hookform/resolvers/zod";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { ApiError } from "../api/client";
import { toIsoDate } from "../lib/dates";
import type { Category, Transaction, TransactionInput } from "../types/api";
import { Button } from "./Button";
import { SelectField } from "./SelectField";
import { TextField } from "./TextField";

const transactionSchema = z.object({
  amount: z
    .string()
    .trim()
    .regex(/^\d{1,10}(\.\d{1,2})?$/, "Enter an amount like 12.50")
    .refine((value) => Number(value) > 0, "Enter an amount above zero"),
  occurredOn: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, "Choose a date"),
  categoryId: z.string().min(1, "Choose a category"),
  note: z.string().max(280, "Keep the note to 280 characters or fewer"),
});

type TransactionValues = z.infer<typeof transactionSchema>;

type TransactionFormProps = {
  categories: Category[];
  /** The transaction being edited. Leave out to add a new one. */
  initial?: Transaction;
  onSave: (input: TransactionInput) => Promise<void>;
  onCancel: () => void;
};

export function TransactionForm({ categories, initial, onSave, onCancel }: TransactionFormProps) {
  const [serverError, setServerError] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<TransactionValues>({
    resolver: zodResolver(transactionSchema),
    defaultValues: initial
      ? {
          amount: initial.amount.toFixed(2),
          occurredOn: initial.occurredOn,
          categoryId: String(initial.categoryId),
          note: initial.note ?? "",
        }
      : { amount: "", occurredOn: toIsoDate(new Date()), categoryId: "", note: "" },
  });

  async function onSubmit(values: TransactionValues) {
    setServerError(null);
    try {
      await onSave({
        amount: Number(values.amount),
        categoryId: Number(values.categoryId),
        occurredOn: values.occurredOn,
        note: values.note.trim() || null,
      });
    } catch (error) {
      if (!(error instanceof ApiError)) {
        setServerError("Couldn't reach the server. Try again.");
        return;
      }
      for (const [field, message] of Object.entries(error.fieldErrors)) {
        if (
          field === "amount" ||
          field === "occurredOn" ||
          field === "categoryId" ||
          field === "note"
        ) {
          setError(field, { message });
        }
      }
      setServerError(error.message);
    }
  }

  return (
    <form
      onSubmit={handleSubmit(onSubmit)}
      noValidate
      className="rounded-lg border border-line bg-surface p-4 sm:p-6"
    >
      <h2 className="text-lg font-semibold">{initial ? "Edit transaction" : "Add transaction"}</h2>
      <div className="mt-4 grid gap-4 sm:grid-cols-2">
        <TextField
          label="Amount (£)"
          inputMode="decimal"
          autoComplete="off"
          autoFocus
          placeholder="0.00"
          error={errors.amount?.message}
          {...register("amount")}
        />
        <TextField
          label="Date"
          type="date"
          error={errors.occurredOn?.message}
          {...register("occurredOn")}
        />
        <SelectField
          label="Category"
          error={errors.categoryId?.message}
          {...register("categoryId")}
        >
          <option value="">Choose a category</option>
          {categories.map((category) => (
            <option key={category.id} value={category.id}>
              {category.name}
            </option>
          ))}
        </SelectField>
        <TextField
          label="Note"
          placeholder="What was it for?"
          error={errors.note?.message}
          {...register("note")}
        />
      </div>
      {serverError && (
        <p role="alert" className="mt-4 text-sm text-danger">
          {serverError}
        </p>
      )}
      <div className="mt-6 flex gap-3">
        <Button type="submit" disabled={isSubmitting}>
          {initial ? "Save changes" : "Save transaction"}
        </Button>
        <Button variant="secondary" onClick={onCancel}>
          Cancel
        </Button>
      </div>
    </form>
  );
}
