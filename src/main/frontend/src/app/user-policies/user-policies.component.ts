import { Component, OnInit } from '@angular/core';
import { User } from 'src/app/models/user';
import { PoliciesService } from '../services/policies.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { saveAs } from "file-saver";
import { Sort } from '@angular/material/sort';





@Component({
  selector: 'app-user-policies',
  templateUrl: './user-policies.component.html',
  styleUrls: ['./user-policies.component.css']
})
export class UserPoliciesComponent implements OnInit {
  currentUser: User;

  constructor(private policiesService : PoliciesService,
    private authenticationService: AuthenticationService,
    private modalService: BsModalService,

  ) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);

  }
  document:any[] = [];
  data:string;
  ngOnInit(): void {

    this.getAllDocuments();
  }


  getAllDocuments(){
    this.data='';
    this.document = [];
    this.policiesService.getAllDocument().pipe(first()).subscribe((response:any) => {
      if (response.serviceStatus == "Success") {
        this.document =  response.serviceResponse;
        console.log("DocumentList : ", this.document);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }
  
  downloadFile(doc: any) {
    this.policiesService.downloadDocument( doc.policyID).subscribe(blob => saveAs(blob,doc.fileName));
  }
  page = 1;
  handlePageChange(event) {
    this.page = event;
  }
  sortData(sort:Sort){	
    console.log(sort);	
    	
    const data=this.document;	
   	
    if(!sort.active || sort.direction==='')	
    {	
      this.document=data;	
      return;	
    }	
    else {	
      this.document=data.sort(	
        (a,b)=>{	
          const isAsc =sort.direction==='asc';	
          switch(sort.active){	
            // case 'i':	
            // return compare(a.index , b.index , isAsc)	
            case 'fileName':	
              return compare(a.fileName.toLowerCase() , b.fileName.toLowerCase() , isAsc)	
              case 'policyName':	
                return compare(a.policyName.toLowerCase() , b.policyName.toLowerCase() , isAsc)	
                case 'createdByName':	
                  return compare(a.createdByName.toLowerCase() , b.createdByName.toLowerCase() , isAsc)	
                  case 'createdOn':	
                    return compare(a.createdOn , b.createdOn , isAsc)	
                default:	
                 return 0;	
          }	
        }	
      )	
    }	
    	
    	
  }	

}
function compare(a: number | string, b: number | string, isAsc: boolean) {	
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);

}