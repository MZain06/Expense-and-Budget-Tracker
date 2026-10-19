import { z } from "zod";

// Stop Zod testing eval(): the nginx Content-Security-Policy reports the test as a violation.
z.config({ jitless: true });
