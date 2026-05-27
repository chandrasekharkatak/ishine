/** Column definition for dynamic approval-level table headers. */
export interface RmbApprovalLevelColumn {
  order: number;
  levelLabel: string;
  financeStep?: boolean;
}

/** Normalize legacy generic labels until API returns department names. */
export function displayApprovalLevelLabel(label: string | undefined | null): string {
  if (!label) {
    return '';
  }
  const t = String(label).trim();
  if (t === 'Specific approver' || t === 'Specific person in scope' || t === 'Department Selected for approve') {
    return 'Department';
  }
  return t;
}

export function approvalLevelApproverColumnTitle(col: RmbApprovalLevelColumn): string {
  return `${displayApprovalLevelLabel(col.levelLabel)} Approver`;
}

export function approvalLevelStatusColumnTitle(col: RmbApprovalLevelColumn): string {
  return `${displayApprovalLevelLabel(col.levelLabel)} Status`;
}

/** Pick the widest {@code approvalLevels} shape from loaded tickets for table headers. */
export function deriveTableLevelColumns(
  tickets: any[],
  fallback: RmbApprovalLevelColumn[] = []
): RmbApprovalLevelColumn[] {
  let widest: any[] = [];
  for (const t of tickets || []) {
    const levels = t?.approvalLevels;
    if (Array.isArray(levels) && levels.length > widest.length) {
      widest = levels;
    }
  }
  if (widest.length) {
    return widest.map((r) => ({
      order: r.order,
      levelLabel: displayApprovalLevelLabel(r.levelLabel || `Level ${r.order}`),
      financeStep: !!r.financeStep
    }));
  }
  return normalizeMatrixLevelColumns(fallback);
}

/** Normalize matrix API {@code levelColumns} for table headers. */
export function normalizeMatrixLevelColumns(columns: RmbApprovalLevelColumn[] = []): RmbApprovalLevelColumn[] {
  return (columns || []).map((c) => ({
    order: c.order,
    levelLabel: displayApprovalLevelLabel(c.levelLabel || (c.order != null ? `Level ${c.order}` : '')),
    financeStep: !!c.financeStep
  }));
}

/**
 * Prefer configured approval-matrix columns when present; otherwise widest ticket {@code approvalLevels}.
 */
export function resolveTableLevelColumns(
  tickets: any[],
  matrixLevelColumns: RmbApprovalLevelColumn[] = []
): RmbApprovalLevelColumn[] {
  const fromMatrix = normalizeMatrixLevelColumns(matrixLevelColumns);
  if (fromMatrix.length) {
    return fromMatrix;
  }
  return deriveTableLevelColumns(tickets, []);
}

/** Build {@code app-column-filter-bar} keys for dynamic level approver/status columns. */
export function buildTicketFilterColumns(staticCols: string[], levelCount: number): string[] {
  const cols = [...staticCols];
  for (let i = 1; i <= levelCount; i++) {
    cols.push(`level${i}ApproverName`, `level${i}ApproverStatus`);
  }
  return cols;
}

export function approvalLevelCell(ticket: any, index: number, field: 'approverName' | 'approverStatus'): string {
  const row = ticket?.approvalLevels?.[index];
  if (!row) {
    return '—';
  }
  const v = row[field];
  return v != null && String(v).trim() !== '' ? String(v) : '—';
}
