export interface ProjectBasedBulkUploadPayload {
    empIds: number[];
    projectId: number;
    fromDate: string;
    toDate: string;
    createdBy: number;
    isBulkUploadBySelf: boolean;
}