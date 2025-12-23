export interface Domain {
    domain: string;
    isActive: boolean;
    isOpen: boolean;
    subDomains: SubDomain[];
    services: Service[];
}

export interface SubDomain {
    subDomain: string;
    isOpen: boolean;
    isActive: boolean;
    children: SubDomain[];
    services: Service[];
}

export interface Service {
    service: string;
    isActive: boolean;
    isOpen: boolean;
    subServices: SubService[];
}

export interface SubService {
    subService: string;
    isActive: boolean;
    children: SubService[];
}