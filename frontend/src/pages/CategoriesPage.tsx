import { useState } from "react";
import { ApiError } from "../api/client";
import { Button } from "../components/Button";
import { CategoryNameForm } from "../components/CategoryNameForm";
import { PencilIcon, TrashIcon } from "../components/icons";
import {
  useCategories,
  useCreateCategory,
  useDeleteCategory,
  useRenameCategory,
} from "../hooks/useCategories";
import type { Category } from "../types/api";

export function CategoriesPage() {
  const categories = useCategories();
  const createCategory = useCreateCategory();
  const renameCategory = useRenameCategory();
  const deleteCategory = useDeleteCategory();
  const [renamingId, setRenamingId] = useState<number | null>(null);

  function handleDelete(category: Category) {
    if (window.confirm(`Delete ${category.name}?`)) {
      deleteCategory.mutate(category.id);
    }
  }

  // The server explains a refused delete, e.g. "Food still has 3 transactions. …"
  const deleteError =
    deleteCategory.error instanceof ApiError
      ? deleteCategory.error.message
      : deleteCategory.isError
        ? "Couldn't delete that category. Try again."
        : null;

  return (
    <div className="space-y-6">
      <section>
        <h1 className="text-3xl font-semibold tracking-tight">Categories</h1>
        <p className="mt-2 text-muted">
          Add your own categories or rename the ones you have. A category can only be deleted once
          nothing is filed under it.
        </p>
      </section>

      <div className="rounded-lg border border-line bg-surface p-4 sm:p-6">
        <CategoryNameForm
          label="New category"
          submitLabel="Add category"
          onSave={(name) => createCategory.mutateAsync(name)}
        />
      </div>

      {deleteError && (
        <p role="alert" className="text-sm text-danger">
          {deleteError}
        </p>
      )}

      {categories.isPending ? (
        <p className="text-muted">Loading categories…</p>
      ) : categories.isError ? (
        <div role="alert" className="space-y-3 rounded-lg border border-line bg-surface p-6">
          <p>Couldn't load your categories.</p>
          <Button variant="secondary" onClick={() => categories.refetch()}>
            Try again
          </Button>
        </div>
      ) : categories.data.length === 0 ? (
        <p className="rounded-lg border border-dashed border-line px-6 py-10 text-center text-muted">
          You don't have any categories yet. Add one above to start sorting your spending.
        </p>
      ) : (
        <ul className="divide-y divide-line rounded-lg border border-line bg-surface px-4">
          {categories.data.map((category) => (
            <li key={category.id} className="py-3">
              {renamingId === category.id ? (
                <CategoryNameForm
                  label={`Rename ${category.name}`}
                  submitLabel="Save"
                  defaultName={category.name}
                  autoFocus
                  onSave={async (name) => {
                    await renameCategory.mutateAsync({ id: category.id, name });
                    setRenamingId(null);
                  }}
                  onCancel={() => setRenamingId(null)}
                />
              ) : (
                <div className="flex items-center gap-2">
                  <span className="min-w-0 flex-1 truncate font-medium">{category.name}</span>
                  <Button
                    variant="quiet"
                    size="icon"
                    aria-label={`Rename ${category.name}`}
                    title="Rename"
                    onClick={() => setRenamingId(category.id)}
                  >
                    <PencilIcon />
                  </Button>
                  <Button
                    variant="quietDanger"
                    size="icon"
                    aria-label={`Delete ${category.name}`}
                    title="Delete"
                    onClick={() => handleDelete(category)}
                  >
                    <TrashIcon />
                  </Button>
                </div>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
