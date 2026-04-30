import { Pipe, PipeTransform } from '@angular/core';

/**
 * Converts numeric years (e.g. 0.1, 2.6) into a friendly "months / years+months" label.
 * - 0.1  -> 1m
 * - 0.3  -> 4m
 * - 2.6  -> 2y 7m
 */
@Pipe({ name: 'experienceDuration', standalone: true })
export class ExperienceDurationPipe implements PipeTransform {
  transform(value: any): string {
    if (value === null || value === undefined || value === '') return '0m';

    // Your data is in years.months format (NOT decimal years):
    //  - 0.3  => 0y 3m
    //  - 1.9  => 1y 9m
    //  - 2.11 => 2y 11m
    const raw = String(value).trim();
    if (!raw) return '0m';

    const negative = raw.startsWith('-');
    const normalized = negative ? raw.slice(1) : raw;

    const parts = normalized.split('.');
    let years = 0;
    let months = 0;

    const yearsNum = Number(parts[0] || '0');
    if (!Number.isFinite(yearsNum) || yearsNum < 0) return '0m';
    years = Math.floor(yearsNum);

    const frac = (parts[1] || '').replace(/\D/g, '');
    if (frac.length > 0) {
      // .9 means 9 months, .11 means 11 months
      months = Number(frac.length === 1 ? frac : frac.slice(0, 2));
      if (!Number.isFinite(months) || months < 0) months = 0;
    }

    // Normalize months overflow (e.g. 1.12 => 2y 0m)
    if (months >= 12) {
      years += Math.floor(months / 12);
      months = months % 12;
    }

    if (negative || (years === 0 && months === 0)) return '0m';
    if (years > 0 && months > 0) return `${years}y ${months}m`;
    if (years > 0) return `${years}y`;
    return `${months}m`;
  }
}

