/** Column definition for dynamic approval-level table headers. */
export interface RmbApprovalLevelColumn {
  order: number;
  levelLabel: string;
  financeStep?: boolean;
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
      levelLabel: r.levelLabel || `Level ${r.order}`,
      financeStep: !!r.financeStep
    }));
  }
  return fallback;
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
