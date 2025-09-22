export class MilestoneToBeExpired {


    id: number;                  // Milestone ID
    poId: number;
    projectId: number;
    name: string;                // Milestone name
    startDate: Date;
    endDate: Date;
    description: string;
    remarks: string;
    status: string;
    lienItemId: number;
    lineItemName: string;
    projectName: string;
    poNo: string;
    updatedBy: number;
    extendedDate: Date; // Date when the milestone was extended


}