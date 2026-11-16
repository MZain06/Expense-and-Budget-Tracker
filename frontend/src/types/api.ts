// Shapes of the JSON the backend sends and receives. They mirror the Java records.

export type User = {
  id: number;
  email: string;
};

export type TokenResponse = {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
};

export type CategoryKind = "SPENDING" | "INCOME";

export type Category = {
  id: number;
  name: string;
  kind: CategoryKind;
};

export type Direction = "OUT" | "IN";

export type Transaction = {
  id: number;
  amount: number;
  direction: Direction;
  occurredOn: string;
  note: string | null;
  categoryId: number;
  categoryName: string;
};

export type TransactionInput = {
  amount: number;
  direction: Direction;
  categoryId: number;
  occurredOn: string;
  note: string | null;
};

export type Budget = {
  categoryId: number;
  categoryName: string;
  limit: number;
};

export type SuggestedBudget = Budget & {
  usualSpending: number;
};

export type BudgetSuggestions = {
  basedOn: string[];
  usualSpending: number;
  usualIncome: number;
  total: number;
  budgets: SuggestedBudget[];
};

export type CategorySummary = {
  categoryId: number;
  categoryName: string;
  spent: number;
  budget: number | null;
  remaining: number | null;
};

export type CategoryIncome = {
  categoryId: number;
  categoryName: string;
  received: number;
};

export type MonthlySummary = {
  totalSpent: number;
  totalBudget: number;
  totalIncome: number;
  categories: CategorySummary[];
  income: CategoryIncome[];
};

export type MonthTotal = {
  month: string;
  spent: number;
};

export type CategoryChange = {
  categoryId: number;
  categoryName: string;
  thisMonth: number;
  lastMonth: number;
  change: number;
};

export type Insights = {
  spent: number;
  spentLastMonth: number;
  averagePerDay: number;
  projectedTotal: number | null;
  threeMonthAverage: number;
  trend: MonthTotal[];
  biggestChanges: CategoryChange[];
};
