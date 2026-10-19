/// <reference types="vitest/config" />
import tailwindcss from "@tailwindcss/vite";
import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    // During development, send /api requests to the Spring Boot backend.
    proxy: {
      "/api": "http://localhost:8080",
    },
  },
  test: {
    environment: "jsdom",
    setupFiles: ["./src/test/setup.ts"],
    clearMocks: true,
    coverage: {
      include: ["src/**/*.{ts,tsx}"],
      exclude: ["src/test/**", "src/main.tsx", "src/lib/zodConfig.ts"],
      reporter: ["text", "html", "json-summary"],
    },
  },
});
