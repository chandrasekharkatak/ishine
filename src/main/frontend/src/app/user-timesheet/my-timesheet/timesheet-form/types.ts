import { SafeResourceUrl } from "@angular/platform-browser";

export interface TimesheetDocumentDataI {
    docId: number | null;
    projectId: number;
    docName: string | null;
    finalFlag: boolean;
    bulkApprovedDocId: number | null;
    uniqueIdentifier: string | null;
    docType: 'Filled' | 'Approved';
    previewUrl: SafeResourceUrl | null;
    rawObjectUrl: string | null;
    fileError: string | null;
    fileType: 'pdf' | 'image' | null;
    fileSize: number | null;
}