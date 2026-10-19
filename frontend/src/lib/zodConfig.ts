import { z } from "zod";

// nginx sends a Content-Security-Policy that blocks eval(). Zod tests whether eval() works before
// using it to speed up validation, and the browser reports that test as a CSP violation, so turn
// it off. This must run before any schema is created, which is why main.tsx imports it first.
z.config({ jitless: true });
