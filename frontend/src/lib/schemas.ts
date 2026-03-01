import { z } from "zod";

/** Reusable address schema (used in checkout and booking forms). */
export const addressSchema = z.object({
  street: z.string().min(1, "Street address is required"),
  city: z.string().min(1, "City is required"),
  district: z.string().optional(),
  state: z.string().optional(),
  zipCode: z.string().min(1, "ZIP/Postal code is required"),
  country: z.string().default("US"),
  additionalInfo: z.string().optional(),
});

/** Reusable phone number schema. */
export const phoneSchema = z
  .string()
  .min(7, "Phone number is too short")
  .max(20, "Phone number is too long");

/** Reusable email schema. */
export const emailSchema = z.string().email("Invalid email address");

/** Pagination schema for URL search param validation. */
export const paginationSchema = z.object({
  page: z.number().int().min(0).default(0),
  size: z.number().int().min(1).max(100).default(20),
});

/** Generic Zod schema for ApiResponse<T>. Use `.parseApiResponse<YourType>()` instead. */
export const apiResponseSchema = <T extends z.ZodTypeAny>(dataSchema: T) =>
  z.object({
    success: z.boolean(),
    data: dataSchema.nullable(),
    message: z.string().nullable(),
    error: z.string().nullable(),
    errorDetails: z
      .array(
        z.object({
          code: z.string().nullable(),
          field: z.string().nullable(),
          rejectedValue: z.unknown(),
        })
      )
      .nullable()
      .optional(),
    timestamp: z.string(),
    traceId: z.string().nullable(),
  });
