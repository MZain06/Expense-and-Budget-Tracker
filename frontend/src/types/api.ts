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

export type Category = {
  id: number;
  name: string;
};

export type Transaction = {
  id: number;
  amount: number;
  occurredOn: string;
  note: string | null;
  categoryId: number;
  categoryName: string;
};

export type TransactionInput = {
  amount: number;
  categoryId: number;
  occurredOn: string;
  note: string | null;
};

export type Budget = {
  categoryId: number;
  categoryName: string;
  limit: number;
};

export type CategorySummary = {
  categoryId: number;
  categoryName: string;
  spent: number;
  budget: number | null;
  remaining: number | null;
};

export type MonthlySummary = {
  totalSpent: number;
  totalBudget: number;
  categories: CategorySummary[];
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
