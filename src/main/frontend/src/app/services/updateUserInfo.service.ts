import { HttpClient } from "@angular/common/http";
import { EventEmitter, Injectable } from "@angular/core";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { Employee } from "../models/employee";
import { User } from "../models/user";
import { AuthenticationService } from "./authentication.service";
import { ValidationService } from "./validation.service";

@Injectable({providedIn: 'root'})
export class UpdateUserInfoService{
    private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";
  
    //modal 
    alertMessage: any;
    modalRef: BsModalRef = new BsModalRef();

    //Obj 
    currentUser: User;
    private userInfoObj: Employee = new Employee();

    constructor(
        private http: HttpClient,
        private authenticationService: AuthenticationService,
        private modalService: BsModalService,
    ){ 
        this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    }

    getUserInfoObj(){
        return Object.assign({}, this.userInfoObj);
    }

    setUserInfoObj(userInfoObj: Employee){
        this.userInfoObj = userInfoObj;
    }

}