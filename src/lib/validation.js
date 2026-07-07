export const namePattern = '[A-Za-z][A-Za-z .\'-]{1,49}';
export const employeeIdPattern = '[A-Za-z0-9_-]{2,20}';
export const phonePattern = '\\+?[0-9][0-9 ()-]{6,19}';
export const cityPattern = '[A-Za-z][A-Za-z .\'-]{1,79}';

export function isValidName(value) {
  return /^[A-Za-z][A-Za-z .'-]{1,49}$/.test(String(value || '').trim());
}

export function isValidEmail(value) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(value || '').trim());
}

export function isValidPhone(value) {
  const phone = String(value || '').trim();
  return !phone || /^\+?[0-9][0-9 ()-]{6,19}$/.test(phone);
}

export function isValidCity(value) {
  const city = String(value || '').trim();
  return !city || /^[A-Za-z][A-Za-z .'-]{1,79}$/.test(city);
}

export function isValidAddress(value) {
  const address = String(value || '').trim();
  return !address || (address.length >= 5 && /[A-Za-z]/.test(address));
}

export function isValidDate(value) {
  return Boolean(value) && !Number.isNaN(new Date(value).getTime());
}

export function isNotFutureDate(value) {
  if (!isValidDate(value)) return false;
  const selected = new Date(value);
  const today = new Date();
  selected.setHours(0, 0, 0, 0);
  today.setHours(0, 0, 0, 0);
  return selected <= today;
}
