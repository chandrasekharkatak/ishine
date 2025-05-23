import { Component, OnInit, TemplateRef } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Employee } from 'src/app/models/employee';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { TravelDeskService } from 'src/app/services/travel-desk.service';

@Component({
  selector: 'app-travel-config',
  templateUrl: './travel-config.component.html',
  styleUrls: ['./travel-config.component.css']
})
export class TravelConfigComponent implements OnInit {

  items:any=10;
  page:any=1;
  isSearchEnabledReview: boolean = false;
  isCategoryTable: boolean = true;
  createCategoryForm:boolean =false;
  filters: any = {};
  sortColumn: any;
  sortColumnType: any;
  sortDirection = 'asc';
  isHotelCategory:boolean =false;
  isHotelSubCategory:boolean =false;
  isCity:boolean = false;
  travelReason = {
    travelReasonName: '',
    description: ''
  };
  //hotelCategory = '';
  //description = '';
  hotelCategory1 = {
    hotelCategory: '',
    description: ''
  };
  hotelSubCategoryData = {
        hotelCategory: '',            
        hotelSubCategoryName: '',        
         description: ''  ,
         cityName:''    
  }             
  currentEmployeeInfo: Employee = new Employee();
  domainSpecializationList: any[];

  modeType: string = '';
  description: string = '';
  currentUser: any;
  alertMessage: any;
  travelReasonlist: any[] = []; 
  hotelCategorylist: any[] = [];
  travelModelist: any[] = [];
  travelModelistByReason: any[] = [];
  selectedTravelReasons: string = '';
  // selectedTravelReasons: string[] = [];
  selectedTravelModes: string[] = [];
  travelClass :any ;
  hotelSubCategorylist: any;
  handlePageChange(event) {
    this.page = event;
  }
  reviewColumns: any[] = ['blank', 'reviewLabel', 'reviewFieldType', 'condition', 'quarterCycle', 'departmentName', 'employeeName', 'createdOn', 'updatedByName', 'updatedOn']

  constructor(private modalService: BsModalService,private travelDesk: TravelDeskService,
    private employeeService : EmployeeService,
    private authenticationService: AuthenticationService
  ) {this.authenticationService.currentUser.subscribe(x => this.currentUser = x) }

  ngOnInit(): void {
    this.onGetEmployeeInfo();
    this.onGetTravelReason();
    this.onGetTravelMode();
    this.onGetHotelCategory();
    this.onGetHotelSubCategory();
  }

  modalRef: BsModalRef = new BsModalRef();
  modalRef2: BsModalRef = new BsModalRef();
  openAlertMod1(template: TemplateRef<any>) {
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }

  async onGetEmployeeInfo(){
    this.domainSpecializationList = [];
    this.currentEmployeeInfo = new Employee();
    let currentEmp = new Employee();
    currentEmp.empId = this.currentUser.empId;
    currentEmp.isDraft = false;
    console.log("currentEmp :::::::::::::::::::::::: ", currentEmp);
    
    const response: any = await this.employeeService.getEmployeeByEmpId(currentEmp).toPromise();
    if (response.serviceStatus == "Success") {
      this.currentEmployeeInfo = response.serviceResponse;
    } else {
      console.error(response.serviceResponse);
    }
  }

  showQuaterTable(){
    this.isCategoryTable=true;
    this.isClass=false;
    this.istravelMode=false;
    this.travelModeForm=false;
    this.classCategoryForm=false;
    this.createCategoryForm=false;
    this.isHotelCategory = false;
    this.isHotelSubCategory=false;
    this.isCity=false;

  }
  createCategory(){
    this.isCategoryTable=false;
    this.createCategoryForm=true;
    this.travelModeForm=false;
    this.classCategoryForm=false;
    this.isClass=false;
    this.istravelMode=false;
    //this.createCategoryForm=false;
    this.isHotelCategory = false;
    this.isHotelSubCategory=false;
    this.isCity=false;
    this.hotelCategoryForm = false ;
    this.hotelSubCategoryForm = false ;
    this.cityForm =false ;

  }
  sortData(sort: Sort) {
      //console.log(sort);
      if (sort.active) {
        let sortParams: any[] = sort.active?.split("|");
        this.sortColumn = sortParams[0];
        this.sortColumnType = sortParams[1];
        this.sortDirection = sort.direction;
      }
    }

    onSearch(searchData) {
      this.filters = searchData;
      //console.log("Updated Filter : ", this.filters);
    }
toggleSearchReviewType() {
    this.isSearchEnabledReview = !this.isSearchEnabledReview;
    if (!this.isSearchEnabledReview) {
      this.filters = {};
    }
  }
  istravelMode:Boolean=false;
  subCategory(){
   this.istravelMode=true;
   this.isClass=false;
   this.isCategoryTable=false;
   this.travelModeForm=false;
    this.classCategoryForm=false;
    this.createCategoryForm=false;
    this.isHotelCategory = false;
    this.isHotelSubCategory=false;
    this.isCity=false;
    this.hotelCategoryForm = false ;
    this.hotelSubCategoryForm = false ;
    this.cityForm =false ;
  }

  isClass:boolean=false;
  classCategory(){
    this.isClass=true;
    this.istravelMode=false;
    this.isCategoryTable=false;
    this.travelModeForm=false;
    this.classCategoryForm=false;
    this.createCategoryForm=false;
    this.isHotelCategory = false;
    this.isHotelSubCategory=false;
    this.isCity=false;
    this.hotelCategoryForm = false ;
    this.hotelSubCategoryForm = false ;
    this.cityForm =false ;
  }

  hotelCategory(){
    this.isClass=false;
    this.istravelMode=false;
    this.isCategoryTable=false;
    this.travelModeForm=false;
    this.classCategoryForm=false;
    this.createCategoryForm=false;
    this.isHotelCategory = true;
    this.isHotelSubCategory=false;
    this.isCity=false;
    this.hotelCategoryForm = true ;
    this.hotelSubCategoryForm = false ;
    this.cityForm =false ;
  }

  hotelSubCategory(){
    this.isClass=false;
    this.istravelMode=false;
    this.isCategoryTable=false;
    this.travelModeForm=false;
    this.classCategoryForm=false;
    this.createCategoryForm=false;
    this.isHotelCategory = false;
    this.isHotelSubCategory=true;
    this.isCity=false;
    this.hotelCategoryForm = false ;
    this.hotelSubCategoryForm = true ;
    this.cityForm =false ;
  }

  cityCategory(){
    this.isClass=false;
    this.istravelMode=false;
    this.isCategoryTable=false;
    this.travelModeForm=false;
    this.classCategoryForm=false;
    this.createCategoryForm=false;
    this.isHotelCategory = false;
    this.isHotelSubCategory=false;
    this.isCity=true;
    this.hotelCategoryForm = false ;
    this.hotelSubCategoryForm = false ;
    this.cityForm =true ;
  }



  classCategoryForm:boolean=false;
  travelModeForm:boolean=false;
  hotelCategoryForm:boolean=false;
  hotelSubCategoryForm:boolean=false;
  cityForm:boolean=false;

  subClassCategory(){
    this.travelModeForm=false;
    this.classCategoryForm=true;
    this.createCategoryForm=false;
    this.isClass=false;
    this.istravelMode=false;
    this.isCategoryTable=false;
    this.isHotelCategory = false;
    this.isHotelSubCategory=false;
    this.isCity=false;
    this.hotelCategoryForm = false ;
    this.hotelSubCategoryForm = false ;
    this.cityForm =false ;
  }

  travelCategory(){
    this.travelModeForm=true;
    this.classCategoryForm=false;
    this.createCategoryForm=false;
    this.isClass=false;
    this.istravelMode=false;
    this.isCategoryTable=false;
    this.isHotelCategory = false;
    this.isHotelSubCategory=false;
    this.isCity=false;
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  async submitTravelReason(template: TemplateRef<any>) {
    try {
      const response: any = await this.travelDesk.saveTravelReason(this.travelReason).toPromise();
      console.log('Travel Reason saved:', response);
      if (response.serviceStatus === "Success") {
        this.openAlertMod(template, "Travel Reason submitted successfully!");
        this.modalRef.hide();
      }
      else {
        this.openAlertMod(template, "Submission failed. Try again.!");
        }

    } catch (error) {
      console.error('Error submitting reason:', error);
      this.openAlertMod(template, "Error occurred while saving the travel reason.");
    }
  }



  cancelRequest() {
    this.modalRef.hide();
    location.reload();
  }

  async onGetTravelReason(){

    const response: any = await this.travelDesk.getTravelReason().toPromise();
    if (response.serviceStatus == "Success") {
      this.travelReasonlist = response.serviceResponse;
    
      console.log("travelReasonlist   ::::::: : ", this.travelReasonlist);

    } else {
      console.error(response.serviceResponse);
    }
  }


  async submitTravelMode(template: TemplateRef<any>) {
    if (!this.selectedTravelReasons || this.selectedTravelReasons.length === 0) {
      this.openAlertMod(template, "Please select at least one travel reason.");
      return;
    }
  
    try {
      const reason = this.selectedTravelReasons ;
      //for (const reason of this.selectedTravelReasons) {
        const travelModePayload = {
          travelReason: reason, 
          modeType: this.modeType,
          description: this.description,
          createdBy: this.currentEmployeeInfo?.empId || 0
        };
  
        const response: any = await this.travelDesk.saveTravelMode(travelModePayload).toPromise();
  
        if (response.serviceStatus !== 'Success') {
          console.error(`Error saving reason: ${reason}`, response.serviceError);
          this.openAlertMod(template, `Failed to save travel mode for reason: ${reason}`);
          return;
        }
     // }
      this.openAlertMod(template, 'Travel Mode saved successfully!');
    } catch (error) {
      console.error('API error:', error);
      this.openAlertMod(template, 'Unexpected error occurred!');
    }
  }

  async onGetTravelMode(){

    const response: any = await this.travelDesk.getTravelMode().toPromise();
    if (response.serviceStatus == "Success") {
      this.travelModelist = response.serviceResponse;
    
      console.log("travelModelist   ::::::: : ", this.travelModelist);

    } else {
      console.error(response.serviceResponse);
    }
  }

  async onTravelReasonChange(selectedReason: string) {
    console.log('Selected reason:', selectedReason);
    this.travelModelistByReason = [];
    try {
      const response: any = await this.travelDesk.getTravelModeByReason(selectedReason).toPromise();
      if (response.serviceStatus === "Success") {
        this.travelModelistByReason = response.serviceResponse;
        console.log("Filtered travel modes:", this.travelModelistByReason);
      } else {
        console.error("Failed to fetch travel modes:", response.serviceResponse);
      }
     
    } catch (error) {
      console.error("Error fetching travel modes:", error);
    }
  }


  async submitTravelClass(template: TemplateRef<any>) {
    if (!this.selectedTravelReasons || !this.selectedTravelModes || !this.travelClass || !this.description) {
      this.openAlertMod(template, "All fields are required.");
      return;
    }
  
    const travelClassPayload = {
      travelReason: this.selectedTravelReasons,
      travelMode: this.selectedTravelModes,
      travelClass: this.travelClass,
      description: this.description,
      createdBy: this.currentEmployeeInfo?.empId || 0
    };
  
    try {
      const response: any = await this.travelDesk.saveTravelClass(travelClassPayload).toPromise();
      if (response.serviceStatus === 'Success') {
        this.openAlertMod(template, 'Travel Class saved successfully!');
      } else {
        alert('Error saving travel class: ' + response.serviceError);
      }
    } catch (error) {
      console.error('API error:', error);
      this.openAlertMod(template, 'Unexpected error occurred!');
    }
  }

  openDeleteModal(template:TemplateRef<any> , documentObj:any){
    this.modalRef = this.modalService.show(template , { class : 'modal-sm'});

  }

  onDeleteType(template:TemplateRef<any>){
    let doc = new Document();
    
    // doc.typeId = this.documentObj.typeId;
    // doc.typeName = this.documentObj.typeName;
    //     this.newsletterService.deleteType(doc).pipe(first()).subscribe((response : any)=>{
    //       if(response.serviceStatus == "Success"){
    //         this.openAlertMod(template, response.serviceResponse);
    //         this.getAllTypeName();
    //       }else{
    //         this.openAlertMod(template, response.serviceResponse);
    //       }
    //     })
        //console.log("yes delete !!   ",doc.typeId);
      }


      async submitHotelCategory(template: TemplateRef<any>) {
        try {


          const hotelCategoryName = {
            hotelCategory: this.hotelCategory1.hotelCategory,
            description: this.hotelCategory1.description,
            createdBy: this.currentEmployeeInfo?.empId || 0
          };

          

          const response: any = await this.travelDesk.saveHotelCategory(hotelCategoryName).toPromise();
          console.log('hotelCategoryName saved:', response);
          if (response.serviceStatus === "Success") {
            this.openAlertMod(template, "Hotel Category Name submitted successfully!");
            this.modalRef.hide();
          }
          else {
            this.openAlertMod(template, "Submission failed. Try again.!");
            }
    
        } catch (error) {
          console.error('Error submitting reason:', error);
          this.openAlertMod(template, "Error occurred while saving the hotel Category.");
        }
      }
    

    
      async onGetHotelCategory(){
    
        const response: any = await this.travelDesk.getHotelCategory().toPromise();
        if (response.serviceStatus == "Success") {
          this.hotelCategorylist = response.serviceResponse;
        
          console.log("hotelCategorylist   ::::::: : ", this.hotelCategorylist);
    
        } else {
          console.error(response.serviceResponse);
        }
      }

      async submitHotelSubCategory(template: TemplateRef<any>) {
             
        try {
          const hotelCategoryValue = this.hotelSubCategoryData.hotelCategory ;
          console.log(hotelCategoryValue) ;
          const hotelSubCategoryPayload = {
            hotelCategory: this.hotelSubCategoryData.hotelCategory, // From dropdown
            hotelSubCategoryName: this.hotelSubCategoryData.hotelSubCategoryName,
            description: this.hotelSubCategoryData.description,
            createdBy: this.currentEmployeeInfo?.empId || 0
          };
      
          const response: any = await this.travelDesk.saveHotelSubCategory(hotelSubCategoryPayload).toPromise();
      
          if (response.serviceStatus !== 'Success') {
            console.error('Error saving hotel sub-category:', response.serviceError);
            this.openAlertMod(template, "Failed to save Hotel Sub-Category.");
            return;
          }
      
          this.openAlertMod(template, "Hotel Sub-Category saved successfully!");
          this.modalRef.hide();
        } catch (error) {
          console.error("API error:", error);
          this.openAlertMod(template, "Unexpected error occurred while saving Hotel Sub-Category.");
        }
      }
      
    
    
        async onGetHotelSubCategory(){
    
          const response: any = await this.travelDesk.getHotelSubCategory().toPromise();
          if (response.serviceStatus == "Success") {
            this.hotelSubCategorylist = response.serviceResponse;
          
            console.log("hotelSubCategorylist   ::::::: : ", this.hotelSubCategorylist);
      
          } else {
            console.error(response.serviceResponse);
          }
        }


        async submitCity(template: TemplateRef<any>) {
          try {
            const payload = {
              hotelCategoryId: this.hotelSubCategoryData.hotelCategory, 
              hotelSubCategoryId: this.hotelSubCategoryData.hotelSubCategoryName,  
              cityName: this.hotelSubCategoryData.cityName,
              description: this.hotelSubCategoryData.description,
              createdBy: this.currentEmployeeInfo?.empId || 0
            };

            console.log(payload);
        
            const response: any = await this.travelDesk.saveCity(payload).toPromise();
        
            if (response.serviceStatus !== 'Success') {
              console.error('Error saving city:', response.serviceError);
              this.openAlertMod(template, "Failed to save City.");
              return;
            }
        
            this.openAlertMod(template, "City saved successfully!");
            this.modalRef.hide(); // If you're using modal
          } catch (error) {
            console.error("API error:", error);
            this.openAlertMod(template, "Unexpected error occurred while saving City.");
          }
        }
        


      

    
  


}

