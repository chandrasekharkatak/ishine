export enum Status{
    UN_ASSIGNED="Unassigned",
    PENDING='Pending',
    NOT_STARTED="Not Started",
    IN_PROGRESS='In Progress',
    ON_HOLD='On Hold',
    READY= 'Ready For Billing',
    INVOICE_RAISED='Invoice Raised',
    BILL_RECEIVED='Bill Received',
    COMPLETED='Completed',
    RENEWED='Renewed',
    RENEWED_UNASSIGNED="Renewed Unassigned",
    REJECTED="Rejected",
    RENEWED_REJECTED="Renewed Rejected"
}

export const StatusList: {
    key: string;
    value: string;
  }[] = Object.entries(Status)
    .map(([key, value]) => ({ key, value }));