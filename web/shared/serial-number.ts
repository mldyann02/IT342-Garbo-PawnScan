export const SERIAL_NUMBER_MAX_LENGTH = 255;
export const SERIAL_NUMBER_ALLOWED_TEXT =
  "Serial number can only contain letters, numbers, spaces, and - _ . / : # + =";
export const SERIAL_NUMBER_HTML_PATTERN = "[A-Za-z0-9 _./:#=+\\-]+";

const INVALID_SERIAL_CHARACTER_PATTERN = /[^A-Za-z0-9 _./:#=+\-]/g;
const SERIAL_NUMBER_PATTERN = /^[A-Za-z0-9 _./:#=+\-]+$/;

export function sanitizeSerialNumberInput(value: string): string {
  return value
    .replace(INVALID_SERIAL_CHARACTER_PATTERN, "")
    .slice(0, SERIAL_NUMBER_MAX_LENGTH);
}

export function validateSerialNumber(value: string): string {
  const trimmed = value.trim();

  if (!trimmed) {
    return "Serial number is required";
  }

  if (trimmed.length > SERIAL_NUMBER_MAX_LENGTH) {
    return `Serial number must not exceed ${SERIAL_NUMBER_MAX_LENGTH} characters`;
  }

  if (!SERIAL_NUMBER_PATTERN.test(trimmed)) {
    return SERIAL_NUMBER_ALLOWED_TEXT;
  }

  return "";
}
