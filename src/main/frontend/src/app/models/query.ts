export class Query {
    column: string;
    operator: string;
    value: any;
    conjunction: string = "";
    empId: any;
    customQuery: string;
    queryList: any;
    queryList1: any = [];
    page: number;
    size: number;
    sort: string;
    valueOptionList: any[] = [];
}