import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { first } from 'rxjs/operators';
import { Certificate } from 'src/app/models/certificate';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { DepartmentService } from 'src/app/services/department.service';
import { EmployeeService } from 'src/app/services/employee.service';

@Component({
  selector: 'app-certificate-modal',
  templateUrl: './certificate-modal.component.html',
  styleUrls: ['./certificate-modal.component.css']
})
export class CertificateModalComponent implements OnInit {

  constructor(private departmentService: DepartmentService, private employeeService: EmployeeService,
    private sanitizer: DomSanitizer, private authenticationService: AuthenticationService
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x);}

  ngOnInit(): void {
    this.getAllDepartmentList();
    this.getAllProficiency();
    this.getAllPredefinedSkills();
  }

  @Input() isOpen: boolean = false;
  @Output() closed = new EventEmitter<void>();
  @Output() submittedCertificate = new EventEmitter<any>();

  formData: Certificate = new Certificate();
  currentUser: any;

  close() {
    this.closed.emit();
   this.reset();
  }

  add() {
    this.submittedCertificate.emit(this.formData);
    this.close();
  }

  reset(){
     
  this.formData = new Certificate();
  this.previewImage = null;
  this.pdfFileName = null;
  this.selectedFileName = null;
  this.selectedfile = null;
  this.skillSearch = "";
  this.errors = {};
  }

  allDeptList: any[] = [];
  proficiencyLevels: any[] = [];
  predefinedSkills: any[] = [];
  filteredDeptList: any[] = [];
  deptSearch: any;
  showDeptDropdown = false;
  getAllDepartmentList() {

    this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDeptList = response.serviceResponse;
        this.filteredDeptList = this.allDeptList;
        console.log("this.allDeptList");
      } else {
        alert(response.serviceResponse)
      }
    });
  }

  getAllProficiency() {

    this.employeeService.getAllProficiency().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.proficiencyLevels = response.serviceResponse;
        console.log("this.proficiencyLevels");
      } else {
        console.error(response.serviceResponse);
      }
    });

  }

  filteredSkills: any[] = [];
  skillSearch: any;

  getAllPredefinedSkills() {
    this.employeeService.getAllPredefinedSkills().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.predefinedSkills = response.serviceResponse;
        this.filteredSkills = [];
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  filterSkills() {
    const search = this.skillSearch.toLowerCase();
    if (!search) {
      this.filteredSkills = [];
      return;
    }

    this.filteredSkills = this.predefinedSkills
      .filter(s => s.skillName.toLowerCase().includes(search))
      .filter(s => !this.formData.skills.some(sel => sel.skillId === s.skillId));
  }


  selectSkill(skill: any) {
    this.formData.skills.push({
      skillId: skill.skillId,
      additionalSkill: null
    });
    this.skillSearch = "";
    this.filteredSkills = [];
  }


  addSkillFromInput() {
    const name = this.skillSearch.trim();
    if (name &&
      !this.formData.skills.some(s =>
        (s.additionalSkill && s.additionalSkill.toLowerCase() === name.toLowerCase()) ||
        (s.skillId && this.getSkillName(s.skillId).toLowerCase() === name.toLowerCase())
      )
    ) {
      this.formData.skills.push({
        skillId: null,
        additionalSkill: name
      });
    }
    this.skillSearch = "";
    this.filteredSkills = [];
  }


  removeSkill(index: number) {
    this.formData.skills.splice(index, 1);
  }


  getSkillName(skillId: number): string {
    const found = this.predefinedSkills.find(s => s.skillId === skillId);
    return found ? found.skillName : "";
  }

  filterDepartments() {
    if (!this.formData.departmentName) {
      this.filteredDeptList = this.allDeptList;
    } else {
      const search = this.formData.departmentName.toLowerCase();
      this.filteredDeptList = this.allDeptList.filter(d =>
        d.name.toLowerCase().includes(search)
      );
    }
    this.showDeptDropdown = true;
  }


  selectDepartment(dept: any) {
    this.formData.deptId = dept.deptId;
    this.formData.departmentName = dept.name;
    this.showDeptDropdown = false;
  }

  selectedFileName: any;
  selectedfile: File;
  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (!file) return;

    if (file.size > 3 * 1024 * 1024) {
      this.errors.file = "File size must not exceed 3 MB";
      this.removeFile();
      return;
    }
    this.selectedfile = file;
    this.selectedFileName = file.name;

    // Validate file type
    const validTypes = ["application/pdf", "image/jpeg", "image/png"];
    if (!validTypes.includes(file.type)) {
      alert("Only PDF, JPG, and PNG files are allowed.");
      return;
    }


    this.previewImage = null;
    this.pdfFileName = null;

    if (file.type === "application/pdf") {

      this.pdfFileName = file.name;
    } else {

      this.showPreviewImage(file);
    }
  }

  previewImage: any;
  pdfFileName: any;

  showPreviewImage(image: File) {
    this.previewImage = null;

    const formData = new FormData();
    formData.append("image", image);

    this.employeeService.previewImage(formData).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === 'Success') {
        this.loadProfileImage(response.serviceResponse);
      } else {
        alert(response.serviceResponse);
      }
    });
  }

  loadProfileImage(imageByte: any) {
    if (imageByte) {
      let objectURL = 'data:image/*;base64,' + imageByte;
      this.previewImage = this.sanitizer.bypassSecurityTrustResourceUrl(objectURL);
    } else {
      this.previewImage = "assets/Images/default-user-image.jpeg";
    }
  }


  removeFile() {
    this.previewImage = null;
    this.pdfFileName = null;
    this.selectedFileName = null;
    this.selectedfile = null;
  }


  errors: any = {};
  isFormValid: boolean = false;
  submitted: boolean = false;
  touchedFields: any = {};

  validateForm(): boolean {
  this.errors = {};
  let valid = true;

  
  if (!this.formData.certificationName || this.formData.certificationName.trim() === '') {
    this.errors.certificationName = "Please enter certification name";
    valid = false;
  }

  
  if (!this.formData.specialization || this.formData.specialization.trim() === '') {
    this.errors.specialization = "Please enter specialization";
    valid = false;
  }

  
  if (!this.formData.departmentName || this.formData.departmentName.trim() === '') {
    this.errors.departmentName = "Please select department";
    valid = false;
  }

  
  if (!this.formData.proficiencyId) {
    this.errors.proficiency = "Please select proficiency";
    valid = false;
  }

  if (!this.formData.skills || this.formData.skills.length === 0) {
  this.errors.skills = "Please add at least one skill";
  valid = false;
}

  
  if (!this.formData.issuingAuthority || this.formData.issuingAuthority.trim() === '') {
    this.errors.issuingAuthority = "Please enter issuing authority";
    valid = false;
  }

  
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  if (!this.formData.validFrom) {
    this.errors.validFrom = "Please select valid from date";
    valid = false;
  }

  if (!this.formData.expiresOn) {
    this.errors.expiresOn = "Please select expiry date";
    valid = false;
  } else {
    const expiresOnDate = new Date(this.formData.expiresOn);
    const validFromDate = this.formData.validFrom ? new Date(this.formData.validFrom) : null;

    if (expiresOnDate < today) {
      this.errors.expiresOn = "Expiry date cannot be in the past";
      valid = false;
    }

    if (validFromDate && expiresOnDate <= validFromDate) {
      this.errors.expiresOn = "Expiry date must be after Valid From date";
      valid = false;
    }
  }


  if (!this.selectedfile) {
    this.errors.file = "Please upload certificate file";
    valid = false;
  } else if (this.selectedfile.size > 3 * 1024 * 1024) {
    this.errors.file = "File size must not exceed 3 MB";
    valid = false;
  }

  return valid;
}

  onFieldChange(field: string) {
    this.touchedFields[field] = true;
    // this.validateForm();
  }

  addCertificate() {
    this.submitted = true;
    if (!this.validateForm()) {
    return; 
  }
      
    this.formData.empId= this.currentUser.empId;

    console.log(this.formData,this.selectedfile,"aailiskills");

    this.employeeService.addCertificate(this.formData, this.selectedfile).pipe(first()).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
            this.showMessageModal(response.serviceResponse);
          //  this.openAlertMod(template, response.serviceResponse);
          this.reset();
          this.closed.emit();
           


        } else {

        }
      },
      error: (err) => {
        console.error(err);

      }
    });

    console.log("Certificate added:", this.formData);
  }

    showMessageModal(message: string) {

    this.submittedCertificate.emit({ type: 'message', text: message });
  }


  validateExpiryDate() {
  this.errors.expiresOn = ""; // reset first

  if (!this.formData.expiresOn) {
    return; // don’t show anything until user selects
  }

  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const expiresOnDate = new Date(this.formData.expiresOn);
  const validFromDate = this.formData.validFrom ? new Date(this.formData.validFrom) : null;

  if (expiresOnDate < today) {
    this.errors.expiresOn = "Expiry date cannot be in the past";
  } else if (validFromDate && expiresOnDate <= validFromDate) {
    this.errors.expiresOn = "Expiry date must be after Valid From date";
  }
}





}
