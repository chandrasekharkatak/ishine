export interface Domain{
    domain:string;
    subDomainList:SubDomain[]
    serviceList:Service[]
}

export interface SubDomain{
    isOpen:boolean;
    subdomain:string;
    subDomainChildrenList:SubDomain[] | null;
    serviceList:Service[];
}

export interface Service{
    isOpen:boolean;
    service:string;
    subServiceList:SubService[] | null;
}

export interface SubService{
    isOpen:boolean;
    subService:string;
    subServiceChildren: SubService[] | null;
}