/** Statuses where a change date is not required until moving to another status. */
export const INITIAL_STATUS_VALUES = ['Not Applicable', 'Pending'];

export function isInitialStatus(status: string | null | undefined): boolean {
    return INITIAL_STATUS_VALUES.includes(status || '');
}

/** Required when transitioning from Not Applicable / Pending to any other status. */
export function requiresStatusChangeDate(originalStatus: string | null | undefined, newStatus: string | null | undefined): boolean {
    if (!newStatus || originalStatus === newStatus) {
        return false;
    }
    return isInitialStatus(originalStatus) && !isInitialStatus(newStatus);
}

/** Required on create when status is set beyond initial (e.g. Selected on schedule). */
export function requiresStatusChangeDateOnSet(status: string | null | undefined): boolean {
    return !!status && !isInitialStatus(status);
}

export function hasStatusChangeDate(value: string | null | undefined): boolean {
    return value != null && String(value).trim() !== '';
}
