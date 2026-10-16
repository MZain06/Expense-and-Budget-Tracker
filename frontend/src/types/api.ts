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
