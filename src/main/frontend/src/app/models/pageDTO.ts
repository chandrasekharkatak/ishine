export class PageDTO {

    page: any = 1;
    size: any = 10;
    sortDirection: any = 'asc';
    sortColumn: any;
    sortColumnType: any;
    searchKeyword: any = '';
    filterIdList: any[] = [];

}