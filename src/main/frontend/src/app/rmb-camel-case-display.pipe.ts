import { Pipe, PipeTransform } from '@angular/core';

const DASH = '—';

/** Public ticket reference — leave unchanged. */
const TICKET_NO_PATTERN = /^APM-RMB-\d{8}-\d{4}$/i;

const ACRONYMS: Record<string, string> = {
  hod: 'HOD',
  hr: 'HR',
  bd: 'BD',
  it: 'IT',
  gst: 'GST',
  pdf: 'PDF',
  rmb: 'RMB',
  apm: 'APM',
  otp: 'OTP',
  ceo: 'CEO',
  cfo: 'CFO',
  fin: 'Finance',
};

function splitIntoWords(raw: string): string[] {
  const s = raw
    .trim()
    .replace(/_+/g, ' ')
    .replace(/([a-z0-9])([A-Z])/g, '$1 $2');
  return s.split(/\s+/).filter(Boolean);
}

const STOPWORDS = new Set(['by', 'or', 'and', 'to', 'of', 'in', 'at', 'as', 'on', 'for', 'the', 'a']);

function formatWord(word: string): string {
  if (!word) {
    return '';
  }
  const lower = word.toLowerCase();
  if (STOPWORDS.has(lower)) {
    return lower.charAt(0).toUpperCase() + lower.slice(1);
  }
  if (ACRONYMS[lower]) {
    return ACRONYMS[lower];
  }
  // Short all-caps token (e.g. API status fragments)
  if (/^[A-Z]{2,5}$/.test(word) && word.length <= 5) {
    return word;
  }
  // Long SHOUTCASE → Title
  if (/^[A-Z][A-Z0-9]+$/.test(word) && word.length > 5) {
    return word.charAt(0) + word.slice(1).toLowerCase();
  }
  return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase();
}

/**
 * Human-readable labels for reimbursement UI (spaces + title-style words).
 * Replaces the old behaviour that forced camelCase tokens.
 */
export function formatRmbReadableLabel(value: unknown): string {
  if (value == null) {
    return DASH;
  }
  if (typeof value === 'number') {
    if (!Number.isFinite(value)) {
      return DASH;
    }
    return String(value);
  }
  if (typeof value === 'boolean') {
    return value ? 'Yes' : 'No';
  }
  const s = String(value).trim();
  if (!s) {
    return DASH;
  }
  if (TICKET_NO_PATTERN.test(s)) {
    return s.toUpperCase();
  }
  const words = splitIntoWords(s);
  if (!words.length) {
    return DASH;
  }
  return words.map(formatWord).join(' ');
}

/** @deprecated Use {@link formatRmbReadableLabel}; kept for any TS imports. */
export function toRmbCamelCaseDisplay(value: unknown): string {
  return formatRmbReadableLabel(value);
}

@Pipe({
  standalone: false,
  name: 'rmbCamelDisplay'
})
export class RmbCamelCaseDisplayPipe implements PipeTransform {
  transform(value: unknown): string {
    return formatRmbReadableLabel(value);
  }
}
