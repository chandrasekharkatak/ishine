export interface Domain{
    id?:number;
    name:string;
    type:string;
    isActive:boolean;
    isOpen:boolean;
    subDomains:SubDomain[]
    services:Service[]
}

export interface SubDomain{
    id?:number;
    isOpen:boolean;
    name:string;
    type:string;
    isActive:boolean;
    subDomains:SubDomain[] | null;
    services:Service[];
}

export interface Service{
    id?:number;
    isOpen:boolean;
    name:string;
    type:string;
    isActive:boolean;
    subServices:SubService[] | null;
}

export interface SubService{
    id?:number;
    isOpen:boolean;
    name:string;
    isActive:boolean;
    type:string;
    subServices: SubService[] | null;
}