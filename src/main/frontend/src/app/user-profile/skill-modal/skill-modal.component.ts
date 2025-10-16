import { Component, EventEmitter, Input, Output, OnInit, TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { Skills } from 'src/app/models/skills';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';

@Component({
  selector: 'app-skill-modal',
  templateUrl: './skill-modal.component.html',
  styleUrls: ['./skill-modal.component.css']
})
export class SkillModalComponent implements OnInit {

  constructor(private employeeService: EmployeeService,
    private authenticationService: AuthenticationService,
    private modalService: BsModalService
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    if(!this.isEdit){
    
    this.getAllPredefinedSkills();
    }
    this.getAllProficiency();
  }

  @Input() skill: any;
  @Input() isEdit: boolean = false;
  @Output() saveSkill = new EventEmitter<any>();
  @Output() closeModal = new EventEmitter<void>();
  @Input() existingSkills: any[] = [];
  duplicateError: any;

  proficiencyLevels: any[] = [];
  predefinedSkills: any;
  filteredSkills: any[] = [];
  skillsObj: Skills = new Skills();
  skillInput: any;
  currentUser: any;
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();
 originalProficiencyId:any;
  // addOrUpdateSkill() {
  //   if (!this.skill.name.trim()) return;
  //   this.saveSkill.emit(this.skill);
  // }
  ngOnChanges() {
    console.log("dupli",this.existingSkills);

    if (this.isEdit) {
    
      console.log("skill while editing",this.skill);
      this.skillInput = this.skill.skillName;
      this.skillsObj = this.skill;
      this.originalProficiencyId = this.skill.proficiencyId;
      this.canUpdateSkill();
    }
  }

  hasProficiencyChanged(): boolean {
  return this.skillsObj.proficiencyId !== this.originalProficiencyId;
}

  daysLeftToUpdate: any;
  canUpdate: boolean = true;

  // canUpdateSkill(){
  //     if (this.skill.createdOn) {
  //     const createdDate = new Date(this.skill.createdOn);
  //     const today = new Date();

  //     const diffTime = today.getTime() - createdDate.getTime();
  //     const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));

  //     if (diffDays < 30) {
  //       this.canUpdate = false;
  //       this.daysLeftToUpdate = 30 - diffDays;
  //     } else {
  //       this.canUpdate = true;
  //       this.daysLeftToUpdate = 0;
  //     }
  //   }
  // }

  canUpdateSkill() {
  let referenceDate: Date | null = null;

  
  if (this.skill.updatedOn) {
    referenceDate = new Date(this.skill.updatedOn);
  } else if (this.skill.createdOn) {
    referenceDate = new Date(this.skill.createdOn);
  }

  if (referenceDate) {
    const today = new Date();
    const diffTime = today.getTime() - referenceDate.getTime();
    const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays < 30) {
      this.canUpdate = false;
      this.daysLeftToUpdate = 30 - diffDays;
    } else {
      this.canUpdate = true;
      this.daysLeftToUpdate = 0;
    }
  } else {
    // If no dates at all, allow update by default
    this.canUpdate = true;
    this.daysLeftToUpdate = 0;
  }
}


  close() {
    this.closeModal.emit();
  }

  cancelRequest() {
    this.modalRef.hide();
  }


  getAllProficiency() {

    this.employeeService.getAllProficiency().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.proficiencyLevels = response.serviceResponse;
      } else {
        console.error(response.serviceResponse);
      }
    });

  }

  getAllPredefinedSkills() {
    this.employeeService.getAllPredefinedSkills().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.predefinedSkills = response.serviceResponse;
      } else {
        console.error(response.serviceResponse);
      }
    });
  }


  filterSkills() {
    this.duplicateError = '';
    const query = this.skillInput.trim().toLowerCase();


    this.filteredSkills = this.predefinedSkills.filter(s =>
      s.skillName.toLowerCase().includes(query)
    );


    const exactMatch = this.predefinedSkills.find(
      s => s.skillName.toLowerCase() === query
    );

    if (exactMatch) {

      this.skillsObj.skillId = exactMatch.skillId;
      this.skillsObj.additionalSkill = null;
    } else {

      this.skillsObj.skillId = null;
      this.skillsObj.additionalSkill = this.skillInput;
    }
    
    this.checkDuplicate();
   
  }

  isSkillFormValid(): boolean {

    const hasSkill = (this.skillsObj.skillId != null) || (this.skillInput && this.skillInput.trim() !== '');


    const hasProficiency = this.skillsObj.proficiencyId != null;

    return hasSkill && hasProficiency;
  }

  selectSkill(skill: any) {
    this.skillInput = skill.skillName;
    this.skillsObj.skillId = skill.skillId;
    this.skillsObj.additionalSkill = null;
    this.filteredSkills = [];

    this.checkDuplicate();

  }


  checkDuplicate() {
  this.duplicateError = '';
  const alreadyExists = this.existingSkills.some(s =>
    (s.skillId != null && Number(s.skillId) === Number(this.skillsObj.skillId)) ||
    (!s.skillId && s.additionalSkill?.toLowerCase() === this.skillsObj.additionalSkill?.toLowerCase())
  );

  if (alreadyExists) {
    this.duplicateError = 'This skill already exists in your skill set.';
  }
}


  addSkill(template: TemplateRef<any>) {

    if (!this.skillsObj.skillId) {
      this.skillsObj.additionalSkill = this.skillInput;
    }
    this.skillsObj.empId = this.currentUser.empId;

    console.log(this.skillsObj);
    this.employeeService.addSkillOfEmployee(this.skillsObj).pipe(first()).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.showMessageModal(response.serviceResponse);
          //  this.openAlertMod(template, response.serviceResponse);
          this.closeModal.emit();


        } else {
          this.showMessageModal(response.serviceResponse);
        }
      },
      error: (err) => {
        console.error(err);
        this.showMessageModal("Something went wrong while adding skill");
      }
    });
  }

  updateSkill(){
    console.log(this.skillsObj,"updatwa");
    this.skillsObj.empId = this.currentUser.empId;
    this.employeeService.updateSkillOfEmployee(this.skillsObj).pipe(first()).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.showMessageModal(response.serviceResponse);
          //  this.openAlertMod(template, response.serviceResponse);
          this.closeModal.emit();


        } else {
          this.showMessageModal(response.serviceResponse);
        }
      },
      error: (err) => {
        console.error(err);
        this.showMessageModal("Something went wrong while updating skill");
      }
    });
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }



  showMessageModal(message: string) {

    this.saveSkill.emit({ type: 'message', text: message });
  }
}
