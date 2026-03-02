import { format, formatDistanceToNow, parseISO, isValid } from "date-fns";

/** Format a monetary value to a locale currency string. */
export function formatCurrency(
  amount: number,
  currency = "USD",
  locale = "en-US"
): string {
  return new Intl.NumberFormat(locale, {
    style: "currency",
    currency,
    minimumFractionDigits: 2,
  }).format(amount);
}

/** Format an ISO timestamp to a readable date-time string. */
export function formatDateTime(isoString: string | null | undefined): string {
  if (!isoString) return "—";
  const date = parseISO(isoString);
  return isValid(date) ? format(date, "MMM d, yyyy · h:mm a") : "Invalid date";
}

/** Format an ISO timestamp to a short date. */
export function formatDate(isoString: string | null | undefined): string {
  if (!isoString) return "—";
  const date = parseISO(isoString);
  return isValid(date) ? format(date, "MMM d, yyyy") : "Invalid date";
}

/** Format an ISO timestamp to a short time, e.g. "2:30 PM". */
export function formatTime(isoString: string | null | undefined): string {
  if (!isoString) return "—";
  const date = parseISO(isoString);
  return isValid(date) ? format(date, "h:mm a") : "Invalid time";
}

/** Return relative time string, e.g. "3 minutes ago". */
export function formatRelativeTime(isoString: string | null | undefined): string {
  if (!isoString) return "—";
  const date = parseISO(isoString);
  return isValid(date) ? formatDistanceToNow(date, { addSuffix: true }) : "Invalid date";
}

/** Format a full street address object into a single display string. */
export function formatAddress(parts: {
  street?: string;
  city?: string;
  district?: string;
  state?: string;
  zipCode?: string;
  country?: string;
}): string {
  return [parts.street, parts.district, parts.city, parts.state, parts.zipCode]
    .filter(Boolean)
    .join(", ");
}

/** Truncate a long string with ellipsis. */
export function truncate(text: string, maxLength: number): string {
  return text.length > maxLength ? `${text.slice(0, maxLength)}…` : text;
}

/** Format a phone number to a standard display format. */
export function formatPhone(phone: string): string {
  const digits = phone.replace(/\D/g, "");
  if (digits.length === 10) {
    return `(${digits.slice(0, 3)}) ${digits.slice(3, 6)}-${digits.slice(6)}`;
  }
  return phone;
}
