/** Flatten ticket + lines into table rows for legacy travel list columns. */
import {
  isHotelTravelReasonName,
  lineTravelClassDisplay,
  lineTravelModeDisplay,
  tripTypeDisplayLabel
} from './travel-policy.helper';

export function flattenTravelTicketsToRows(tickets: any[]): any[] {
  const rows: any[] = [];
  for (const t of tickets || []) {
    const lines = Array.isArray(t?.lines) ? t.lines : [];
    const travelSegmentCount = lines.filter((ln: any) => !isHotelTravelReasonName(ln?.requestType)).length;
    if (!lines.length) {
      rows.push(legacyRowFromTicket(t, null, travelSegmentCount));
      continue;
    }
    for (const ln of lines) {
      rows.push(legacyRowFromTicket(t, ln, travelSegmentCount));
    }
  }
  return rows;
}

/** Summarize each ticket into a single row while preserving per-line values for stacked display. */
export function summarizeTravelTicketsToRows(tickets: any[]): any[] {
  const rows: any[] = [];
  for (const t of tickets || []) {
    const lines = Array.isArray(t?.lines) ? t.lines : [];
    const travelSegmentCount = lines.filter((ln: any) => !isHotelTravelReasonName(ln?.requestType)).length;
    const requestTypeItems = lines.length ? lines.map((ln: any) => ln?.requestType || '—') : ['—'];
    const travelModeDisplayItems = lines.length
      ? lines.map((ln: any) => lineTravelModeDisplay(ln || {}))
      : ['NA'];
    const travelClassDisplayItems = lines.length
      ? lines.map((ln: any) => lineTravelClassDisplay(ln || {}))
      : ['NA'];
    const tripTypeDisplayItems = lines.length
      ? lines.map((ln: any) => tripTypeDisplayLabel(ln?.tripType, ln || {}, travelSegmentCount))
      : ['NA'];
    const fromDateItems = lines.length ? lines.map((ln: any) => ln?.fromDate || null) : [null];
    const toDateItems = lines.length ? lines.map((ln: any) => ln?.toDate || null) : [null];
    const hotelCategoryItems = lines.length ? lines.map((ln: any) => ln?.hotelCategory || 'NA') : ['NA'];
    const cityCategoryItems = lines.length
      ? lines.map((ln: any) => ln?.hotelSubCategory || ln?.cityCategory || 'NA')
      : ['NA'];
    const cityItems = lines.length ? lines.map((ln: any) => ln?.city || 'NA') : ['NA'];
    const fromLocationItems = lines.length ? lines.map((ln: any) => ln?.fromLocation || 'NA') : ['NA'];
    const toLocationItems = lines.length ? lines.map((ln: any) => ln?.toLocation || 'NA') : ['NA'];
    const purposeItems = lines.length ? lines.map((ln: any) => ln?.purpose || '—') : ['—'];
    const lineStatusDisplayItems = lines.length
      ? lines.map((ln: any) => ln?.lineStatusDisplay || mapLineStatus(ln?.lineStatus))
      : [mapStatusForDisplay(t?.finalStatus || t?.displayStatus)];
    const adminProofDocIds = lines.flatMap((ln: any) => Array.isArray(ln?.adminProofDocIds) ? ln.adminProofDocIds : []);
    const bookingReferences = lines
      .map((ln: any) => ln?.bookingReference)
      .filter((ref: any) => ref != null && ref !== '');
    const ref = t?.ticketNo || t?.ticketId;

    rows.push({
      requestId: ref,
      ticketId: t?.ticketId,
      ticketNo: t?.ticketNo,
      name: t?.fullName || t?.name,
      requestType: requestTypeItems.join(' | '),
      requestTypeItems,
      travelModeDisplay: travelModeDisplayItems.join(' | '),
      travelModeDisplayItems,
      travelClassDisplay: travelClassDisplayItems.join(' | '),
      travelClassDisplayItems,
      tripTypeDisplay: tripTypeDisplayItems.join(' | '),
      tripTypeDisplayItems,
      fromDate: fromDateItems.find((d: any) => !!d) || null,
      fromDateItems,
      toDate: toDateItems.find((d: any) => !!d) || null,
      toDateItems,
      hotelCategory: hotelCategoryItems.join(' | '),
      hotelCategoryItems,
      cityCategory: cityCategoryItems.join(' | '),
      cityCategoryItems,
      city: cityItems.join(' | '),
      cityItems,
      fromLocation: fromLocationItems.join(' | '),
      fromLocationItems,
      toLocation: toLocationItems.join(' | '),
      toLocationItems,
      empId: t?.empId,
      appliedOn: t?.submittedOn || t?.appliedOn,
      purpose: purposeItems.join(' | '),
      purposeItems,
      level: t?.level,
      currentApprovalLevel: t?.currentApprovalLevel || t?.currentLevelLabel,
      hodName: t?.hodName,
      status: mapStatusForDisplay(t?.status),
      level2approverName: t?.level2approverName,
      level2approverStatus: mapStatusForDisplay(t?.level2approverStatus),
      finalStatus: mapStatusForDisplay(t?.finalStatus || t?.displayStatus),
      displayStatus: t?.displayStatus,
      workflowStage: t?.workflowStage,
      bookingReference: bookingReferences.join(' | '),
      adminProofDocIds,
      lineStatusDisplay: lineStatusDisplayItems.join(' | '),
      lineStatusDisplayItems,
      approvalLevels: t?.approvalLevels,
      lines,
      matrixTicket: true,
      recordType: 'TICKET'
    });
  }
  return rows;
}

function legacyRowFromTicket(t: any, ln: any | null, travelSegmentCount: number): any {
  const ref = t?.ticketNo || t?.ticketId;
  const line = ln || {};
  return {
    requestId: ref,
    ticketId: t?.ticketId,
    ticketNo: t?.ticketNo,
    lineId: ln?.lineId,
    lineNo: ln?.lineNo,
    name: t?.fullName || t?.name,
    requestType: ln?.requestType || '—',
    travelMode: ln?.travelMode,
    travelClass: ln?.travelClass,
    tripType: ln?.tripType,
    travelModeDisplay: lineTravelModeDisplay(line),
    travelClassDisplay: lineTravelClassDisplay(line),
    tripTypeDisplay: tripTypeDisplayLabel(ln?.tripType, line, travelSegmentCount),
    fromDate: ln?.fromDate,
    toDate: ln?.toDate,
    hotelCategory: ln?.hotelCategory || 'NA',
    cityCategory: ln?.hotelSubCategory || ln?.cityCategory || 'NA',
    city: ln?.city || 'NA',
    fromLocation: ln?.fromLocation || 'NA',
    toLocation: ln?.toLocation || 'NA',
    empId: t?.empId,
    appliedOn: t?.submittedOn || t?.appliedOn,
    purpose: ln?.purpose || '—',
    level: t?.level,
    currentApprovalLevel: t?.currentApprovalLevel || t?.currentLevelLabel,
    hodName: t?.hodName,
    status: mapStatusForDisplay(t?.status),
    level1approverRemarks: ln?.approverRemarks || 'NA',
    level2approverName: t?.level2approverName,
    level2approverStatus: mapStatusForDisplay(t?.level2approverStatus),
    level2approverRemarks: t?.level2approverRemarks || 'NA',
    finalStatus: mapStatusForDisplay(t?.finalStatus || t?.displayStatus),
    displayStatus: t?.displayStatus,
    workflowStage: t?.workflowStage,
    bookingReference: ln?.bookingReference,
    adminProofDocIds: ln?.adminProofDocIds || [],
    lineStatusDisplay: ln?.lineStatusDisplay || mapLineStatus(ln?.lineStatus),
    approvalLevels: t?.approvalLevels,
    lines: t?.lines,
    matrixTicket: true,
    recordType: 'TICKET_LINE'
  };
}

function mapLineStatus(s: any): string {
  if (s == null || s === '') {
    return 'NA';
  }
  const t = String(s);
  if (t === 'FULFILLED') {
    return 'Booked';
  }
  if (t === 'PENDING_ADMIN') {
    return 'Pending travel admin';
  }
  if (t === 'PENDING_APPROVAL') {
    return 'Pending approval';
  }
  if (t.includes('REJECTED')) {
    return 'Rejected';
  }
  return t;
}

function mapStatusForDisplay(s: any): string {
  if (s == null || s === '' || s === '—') {
    return 'NA';
  }
  const t = String(s);
  if (t === 'Pending approval' || t === 'PENDING_APPROVAL') {
    return 'Pending';
  }
  if (t === 'Booked' || t === 'FULFILLED' || t === 'Fulfilled') {
    return 'Booked';
  }
  if (t === 'Approved') {
    return 'Approved';
  }
  if (t === 'Rejected' || t.includes('REJECTED')) {
    return 'Rejected';
  }
  return t;
}
