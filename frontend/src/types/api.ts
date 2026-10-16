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
