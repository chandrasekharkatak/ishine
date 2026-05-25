/** ApMoSys Travel Policy v0.1 (25-Jan-2023) — shared UI helpers */

export const TRAVEL_POLICY_ADVANCE_DAYS = 7;
export const TRAVEL_POLICY_MAX_FUTURE_DAYS = 60;
export const HOTEL_REASON_NAME = 'Hotel & Lodging';

export interface HotelCategoryRow {
  id?: number;
  hotelCategory?: string;
  hotelCategoryName?: string;
  description?: string;
  isActive?: string;
}

export function isActiveMasterRow(row: { isActive?: string } | null | undefined): boolean {
  const v = (row?.isActive || 'Y').toString().toUpperCase();
  return v === 'Y' || v === 'YES' || v === 'TRUE' || v === '1';
}

export function hotelCategoryLabel(cat: HotelCategoryRow): string {
  const name = cat.hotelCategory || cat.hotelCategoryName || '';
  const limit = parseHotelLimitInr(cat.description);
  if (limit != null) {
    return `${name} (max ₹${limit.toLocaleString('en-IN')}/night)`;
  }
  return name;
}

export function parseHotelLimitInr(description?: string | null): number | null {
  if (!description) {
    return null;
  }
  const m = description.match(/INR\s*([\d,]+)/i) || description.match(/₹\s*([\d,]+)/i);
  if (!m) {
    return null;
  }
  const n = parseInt(m[1].replace(/,/g, ''), 10);
  return isNaN(n) ? null : n;
}

export function isHotelTravelReasonName(name: string | null | undefined): boolean {
  if (!name) {
    return false;
  }
  const n = name.toLowerCase().trim();
  if (n === HOTEL_REASON_NAME.toLowerCase()) {
    return true;
  }
  return n.includes('hotel') && (n.includes('lodg') || n.includes('accommod') || n.includes('stay'));
}

/** Suggest Metro tier from designation text (policy: TL and Below vs Above TL). */
export function suggestedMetroHotelCategory(designation?: string | null): string | null {
  const d = (designation || '').toLowerCase();
  if (!d) {
    return null;
  }
  const aboveTl =
    d.includes('director') ||
    d.includes('vp') ||
    d.includes('vice president') ||
    d.includes('general manager') ||
    d.includes('gm ') ||
    d.includes('head') ||
    d.includes('chief') ||
    d.includes('president') ||
    d.includes('avp') ||
    d.includes('assistant vice');
  return aboveTl ? 'Metro — Above TL' : 'Metro — TL and Below';
}

export function addDaysIso(base: Date, days: number): string {
  const d = new Date(base);
  d.setDate(d.getDate() + days);
  return d.toISOString().split('T')[0];
}

export function parseIsoDate(s: string | null | undefined): Date | null {
  if (!s) {
    return null;
  }
  const d = new Date(s);
  return isNaN(d.getTime()) ? null : d;
}

export type TravelTripTypeCode = 'ONE_WAY' | 'ROUND' | 'MULTI_CITY' | 'HOTEL';

export function tripTypeCodeFromPlanner(
  tab: 'travel' | 'hotel',
  tripType: 'oneway' | 'round' | 'multicity'
): TravelTripTypeCode {
  if (tab === 'hotel') {
    return 'HOTEL';
  }
  if (tripType === 'oneway') {
    return 'ONE_WAY';
  }
  if (tripType === 'multicity') {
    return 'MULTI_CITY';
  }
  return 'ROUND';
}

export function tripTypeDisplayLabel(
  code: string | null | undefined,
  line?: { fromDate?: any; toDate?: any; requestType?: string },
  travelSegmentCount = 1
): string {
  const c = (code || '').toUpperCase().trim();
  if (c === 'HOTEL') {
    return 'Hotel stay';
  }
  if (c === 'ONE_WAY') {
    return 'One way';
  }
  if (c === 'ROUND') {
    return 'Round trip';
  }
  if (c === 'MULTI_CITY') {
    return 'Multi-city';
  }
  if (line && isHotelTravelReasonName(line.requestType)) {
    return 'Hotel stay';
  }
  if (travelSegmentCount > 1) {
    return 'Multi-city';
  }
  const from = line?.fromDate ? new Date(line.fromDate) : null;
  const to = line?.toDate ? new Date(line.toDate) : null;
  if (from && to && !isNaN(from.getTime()) && !isNaN(to.getTime())) {
    const sameDay =
      from.getFullYear() === to.getFullYear() &&
      from.getMonth() === to.getMonth() &&
      from.getDate() === to.getDate();
    return sameDay ? 'One way' : 'Round trip';
  }
  return '—';
}

export function lineTravelModeDisplay(line: {
  requestType?: string;
  travelMode?: string;
  hotelCategory?: string;
} | null | undefined): string {
  if (!line) {
    return '—';
  }
  if (isHotelTravelReasonName(line.requestType) || line.hotelCategory) {
    return 'Hotel';
  }
  return (line.travelMode || '').trim() || '—';
}

export function lineTravelClassDisplay(line: {
  requestType?: string;
  travelClass?: string;
  hotelCategory?: string;
} | null | undefined): string {
  if (!line) {
    return '—';
  }
  if (isHotelTravelReasonName(line.requestType) || line.hotelCategory) {
    return (line.hotelCategory || '').trim() || '—';
  }
  return (line.travelClass || '').trim() || '—';
}
