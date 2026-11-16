import { zodResolver } from "@hookform/resolvers/zod";
import { type ChangeEvent, useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { ApiError } from "../api/client";
import { toIsoDate } from "../lib/dates";
import type { Category, Transaction, TransactionInput } from "../types/api";
import { Button } from "./Button";
import { SelectField } from "./SelectField";
import { TextField } from "./TextField";

const transactionSchema = z.object({
  direction: z.enum(["OUT", "IN"]),
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
  const [directionChosen, setDirectionChosen] = useState(initial !== undefined);
  const {
    register,
    handleSubmit,
    setError,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<TransactionValues>({
    resolver: zodResolver(transactionSchema),
    defaultValues: initial
      ? {
          direction: initial.direction,
          amount: initial.amount.toFixed(2),
          occurredOn: initial.occurredOn,
          categoryId: String(initial.categoryId),
          note: initial.note ?? "",
        }
      : {
          direction: "OUT",
          amount: "",
          occurredOn: toIsoDate(new Date()),
          categoryId: "",
          note: "",
        },
  });

  const spending = categories.filter((category) => category.kind === "SPENDING");
  const income = categories.filter((category) => category.kind === "INCOME");

  // Until money in or out has been picked by hand, it follows the category: in for income, out
  // for spending. A refund is money in on a spending category, so it can still be changed.
  const categoryField = register("categoryId", {
    onChange: (event: ChangeEvent<HTMLSelectElement>) => {
      const picked = categories.find((category) => String(category.id) === event.target.value);
      if (picked && !directionChosen) {
        setValue("direction", picked.kind === "INCOME" ? "IN" : "OUT");
      }
    },
  });
  const directionField = register("direction", { onChange: () => setDirectionChosen(true) });

  async function onSubmit(values: TransactionValues) {
    setServerError(null);
    try {
      await onSave({
        amount: Number(values.amount),
        direction: values.direction,
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
      <fieldset className="mt-4">
        <legend className="text-sm font-medium">Type</legend>
        <div className="mt-2 flex gap-6">
          <label className="flex items-center gap-2">
            <input type="radio" value="OUT" className="size-4 accent-brand" {...directionField} />
            Money out
          </label>
          <label className="flex items-center gap-2">
            <input type="radio" value="IN" className="size-4 accent-brand" {...directionField} />
            Money in
          </label>
        </div>
      </fieldset>
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
        <SelectField label="Category" error={errors.categoryId?.message} {...categoryField}>
          <option value="">Choose a category</option>
          <optgroup label="Spending">
            {spending.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </optgroup>
          {income.length > 0 && (
            <optgroup label="Income">
              {income.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </optgroup>
          )}
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
