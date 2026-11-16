import { useState } from "react";
import { ApiError } from "../api/client";
import { Button } from "../components/Button";
import { CategoryForm } from "../components/CategoryForm";
import { PencilIcon, TrashIcon } from "../components/icons";
import {
  useCategories,
  useCreateCategory,
  useDeleteCategory,
  useUpdateCategory,
} from "../hooks/useCategories";
import type { Category, CategoryKind } from "../types/api";

export function CategoriesPage() {
  const categories = useCategories();
  const createCategory = useCreateCategory();
  const updateCategory = useUpdateCategory();
  const deleteCategory = useDeleteCategory();
  const [editingId, setEditingId] = useState<number | null>(null);

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

  function list(heading: string, kind: CategoryKind) {
    const items = categories.data?.filter((category) => category.kind === kind) ?? [];
    const headingId = `${kind.toLowerCase()}-heading`;
    return (
      <section aria-labelledby={headingId} className="space-y-2">
        <h2 id={headingId} className="font-semibold">
          {heading}
        </h2>
        {items.length === 0 ? (
          <p className="text-sm text-muted">None yet.</p>
        ) : (
          <ul className="divide-y divide-line rounded-lg border border-line bg-surface px-4">
            {items.map((category) => (
              <li key={category.id} className="py-3">
                {editingId === category.id ? (
                  <CategoryForm
                    title={`Edit ${category.name}`}
                    nameLabel="Name"
                    submitLabel="Save"
                    defaultValues={{ name: category.name, kind: category.kind }}
                    autoFocus
                    onSave={async (values) => {
                      await updateCategory.mutateAsync({ id: category.id, ...values });
                      setEditingId(null);
                    }}
                    onCancel={() => setEditingId(null)}
                  />
                ) : (
                  <div className="flex items-center gap-2">
                    <span className="min-w-0 flex-1 truncate font-medium">{category.name}</span>
                    <Button
                      variant="quiet"
                      size="icon"
                      aria-label={`Edit ${category.name}`}
                      title="Edit"
                      onClick={() => setEditingId(category.id)}
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
      </section>
    );
  }

  return (
    <div className="space-y-6">
      <section>
        <h1 className="text-3xl font-semibold tracking-tight">Categories</h1>
        <p className="mt-2 text-muted">
          Spending categories can have budgets. Income categories are for money coming in, like your
          pay. A category can only be deleted once nothing is filed under it.
        </p>
      </section>

      <div className="rounded-lg border border-line bg-surface p-4 sm:p-6">
        <CategoryForm
          title="Add a category"
          nameLabel="New category"
          submitLabel="Add category"
          onSave={(values) => createCategory.mutateAsync(values)}
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
        <>
          {list("Spending", "SPENDING")}
          {list("Income", "INCOME")}
        </>
      )}
    </div>
  );
}
