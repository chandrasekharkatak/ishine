import { Pipe, PipeTransform } from '@angular/core';

const DASH = '—';

function tokenize(input: string): string[] {
  const trimmed = input.trim();
  if (!trimmed) {
    return [];
  }
  const normalized = trimmed
    .replace(/_+/g, ' ')
    .replace(/([a-z0-9])([A-Z])/g, '$1 $2');
  return normalized.split(/[^a-zA-Z0-9]+/).filter(Boolean);
}

/**
 * Turns arbitrary API / human labels into a single camelCase token for display
 * (e.g. PENDING_HR → pendingHr, "Approved by HOD" → approvedByHod).
 */
export function toRmbCamelCaseDisplay(value: unknown): string {
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
    return value ? 'true' : 'false';
  }
  const s = String(value).trim();
  if (!s) {
    return DASH;
  }
  const parts = tokenize(s);
  if (!parts.length) {
    return DASH;
  }
  const head = parts[0].toLowerCase();
  const tail = parts
    .slice(1)
    .map((p) => p.charAt(0).toUpperCase() + p.slice(1).toLowerCase())
    .join('');
  return head + tail;
}

@Pipe({
  standalone: false,
  name: 'rmbCamelDisplay'
})
export class RmbCamelCaseDisplayPipe implements PipeTransform {
  transform(value: unknown): string {
    return toRmbCamelCaseDisplay(value);
  }
}
