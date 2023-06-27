import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ValidationService {

  constructor() { }

  validateAlphaNumeric(text: string): boolean {

    const regex = /^[a-zA-Z0-9]+$/;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }

  }
  validateCapitalAlphaNumeric(text: string): boolean {

    const regex = /^[A-Z0-9]+$/;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }

  }

  validateAlphaNumericWithSpace(text: string): boolean {

    const regex = /^[a-zA-Z0-9 ]+$/;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }

  }


  validateNumber(text: string): boolean {

    const regex = /^[0-9]*$/;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }
  }

  validatePFNumber(text: string): boolean {
    const regex =  /^[A-Z]{5}[0-9]{17}$/;  //  /^[A-Za-z]{5}[0-9]{17}$/;     //  /^\w{5}\d{17}$/ ;  // (/^[A-Za-z]{5}[0-9]{17}$/);
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }
  }

  validateUANNumber(text: string): boolean {
    const regex = /^[0-9]{12}$/;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }

  }


  
  validateOrgName(text: string): boolean {
    const regex = /^[a-zA-Z-.,&()\s]+$/; // eg. NKGSB Co-operative & scheduled Bank Ltd. (Ghansoli, Navi Mumbai)
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }


  }

  validateEmployerName(text: string): boolean {
    const regex = /^[a-zA-Z0-9-.,&()\s]+$/; 
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }
  }

  // /^(?:[0-9]{11}|[0-9]{2}-[0-9]{3}-[0-9]{6})$/
  validateAccountNumber(text: string): boolean {

    const regex = /^(?:[0-9]{9,17}|[0-9]{2}-[0-9]{1}-[0-9]{6,14})$/;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }


  }

  validateCompletionTime(text :string):boolean{
    const regex = /^[.Ee][-+]|[+-][eE.]*$/;   ///^(?:1|0?\.[1-9])$/      \\//     0\.1-23\.59
    if (regex.test(text)) {
      return true;
    }
    else {
      return false;
    }
  }

  validateTimesheetCompletionTime(text: string): boolean {

    const regex = /^(?:\d{0,2}[0-9]|0?\.[0-9]|[0-9]?\.[0-9]\d{0,2})*$/;   ///^(?:1|0?\.[1-9])$/      \\//     0\.1-23\.59
    if (regex.test(text)) {
      return true;
    }
    else {
      return false;
    }
  }

  //^[1-9][0-9]?$
  validateExperiencedNumber(text: string): boolean {

    const regex = /^[0-9]{0,2}(\.([1-9]|1[0-1]){0,2})?$/;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }


  }

   validateApmosysEmail(text: string): boolean {
    //const regex = /^(?:[0-9]+[a-z_.]|[a-z_.])[a-z0-9_.]+@apmosys\.com$/i;
    const regex = /^[a-z]{3}[a-z0-9_.]+@apmosys\.com$/i;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }
   }
  validateEmail(text: string): boolean {

    const regex = /^[_a-z0-9-]+(\.[_a-z0-9-]+)*@[a-z0-9-]+(\.[a-z0-9-]+)*(\.[a-z]{2,4})$/;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }
  }

  validateMobileNumber(text: string): boolean {
    const regex = /^[6-9]{1}[0-9]{9}$/;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }
  }

  validateNullUndefinedEmptyString(text: any): boolean {

    if (text === undefined || text === null || text === "") {
      return false;
    }
    return true;
  }

  validateAlphaWithSpace(text: string): boolean {

    const regex = /^[a-zA-Z ]+$/;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }

  }

  validateViewsOnOrganisation(text: string): boolean {

    // const regex = /^[a-zA-Z.,& ]+$/;
    const regex = /^[a-zA-Z0-9+\-\(\)'"\?.,&!\s]+$/
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }

  }

  //   /^[ A-Za-z0-9()[]+-*/%]*$/

  validateProjectName(text: string): boolean {

    const regex = /^[ A-Za-z0-9()-\/_\/]*$/;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }

  }

  validateActivityName(text: string): boolean {

    const regex = /^[ A-Za-z0-9()-\/_\/]{3,}\w*$/;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }

  }

  validateLoginRegex(text: string): boolean {

    const regex = /^[0-9]*$/i
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }

  }
  // /^[A-Z@~`!@#$%^&*()_=+\\\\';:\"\\/?>.<,-]*$/i

  validateUppercaseAlpha(text: string): boolean {

    const regex = /^[A-Z]+$/;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }

  }

  validatePincodeNumber(text: string): boolean {
    const regex = /^[1-9][0-9]{5}$/;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }

  }

  validatePassportNumber(text: string): boolean {
    const regex = /^[A-PR-WYa-pr-wy][1-9]\\d\\s?\\d{4}[1-9]$/;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }

  }

  validatePancardNumber(text: string): boolean {
    const regex = /^[A-Z]{5}[0-9]{4}[A-Z]{1}$/;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }

  }

  validateAadharCardNumber(text: string): boolean {
    const regex = /^\d{12}$/;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }

  }

  validateNoticePeriod(text: string): boolean {
    const regex = /^[0-9]*$/;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }

  }

  validateEmployeementId(text: any): boolean {
    const regex = /^[1-9]\d{3,6}$/;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }
  }

  validateYear(text: any): boolean {
    const regex = /^(19[5-9]\d|20[0-4]\d|2050)$/;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }
  }

  validateAlphaNumericSpecialCharacters(text: string): boolean {


    // const regex = /^[A-Za-z0-9@#$%!+*÷=\/_\-'":;,()^{}~\[\]]{8,}$/;
    // const regex = /(?=^.{8,}$)(?=.*\d)(?=.*[!@#$%^&*]+)(?![.\n])(?=.*[A-Z])(?=.*[a-z]).*$/;
    const regex = /^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{8,16}$/;

    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }

  }

  validateBloodGroup(text: string): boolean {
    const regex = /(A|B|AB|O)[+-]/;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }

  }

  validateAlphabeticCharacters(text: string): boolean {
    const regex = /^[a-zA-Z ]*$/;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }

  }


  //

  validateAboutMeUserProfile(text:string): boolean {
    const regex = (/^[A-Za-z ]*$/) ;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }

  }

  validateDiscriptionUserProfile(text:string): boolean {
    const regex = (/^[A-Za-z\s]*$/) ;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }

  }

  // validateActivityTimesheetDiscription(text:string): boolean {
  //   const regex = (/^[A-Za-z0-9]{2}[\s]*$/) ;
  //   if (text !== "" || text !== undefined || text !== null) {
  //     if (regex.test(text)) {
  //       return true;
  //     }
  //     else {
  //       return false;
  //     }
  //   }
  //   else {
  //     return false;
  //   }

  // }

  validateActivityTimesheetDiscription(text:string): boolean {
    // const regex = /^(?=.*\s).{2,}$/gm;
    const regex = /^[^.\s].{1,}$/gm;
    //  /^[#-@{}!()\/_'",.a-z?]|[A-Za-z0-9]{2}[#-@{}!()\/_'",.a-z\s?]*$/ ;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }

  }



  validateTeamActivity(text:string): boolean {
    const regex = (/^[A-Za-z]{3}[A-Za-z\s]*$/);
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }

  }

  // ^[a-z][a-z\s]*$/i

  validateStringWithNoSpaceAtBeginAndNoSingleCharacter(text:string): boolean {
    const regex = (/^[a-z]{2}[a-z\s?]*$/i) ;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }

  }

  // validateIFSCCodeRegexWithBankName(data: string ,bankName: string): boolean {
  //   let bankNameValue = bankName?.trim().toUpperCase();
  //    if(data.toUpperCase().slice(1,5) == bankNameValue)
  //    {
  //     return true;
  //    }
  //    return false;
  // }




  // ^[A-Z]{4}0[A-Z0-9]{6}$

  validateIFSCCodeRegex(text:string): boolean {

    const regex = (/^[A-Z]{4}0[A-Z0-9]{6}$/) ;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }
  }

  validateAlphaWithSpaceInbetween(text:string):boolean{
    const regex = (/[^-\s][a-zA-Z][a-zA-Z ]+[a-zA-Z]$/gm) ;     // minor changes in Regex
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }
  }

  validateUrl(url:any){
    try {
      return Boolean(new URL(url));
    }
    catch(e){
      return false;
    }
  }

  validateMonthDays(text:string):boolean{
    const regex = (/^[1-9]$|^[1-2][0-9]$|^3[0-1]$/gm);
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }
  }

  validateAlphabetAtLeastTwoCharacter(text:string): boolean {
    const regex = (/^[A-Za-z]{2}[A-Za-z\s?]*$/i) ;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }

  }

  validateTeamName(text:string): boolean {
    // const regex = /^[a-zA-Z0-9 . \- _ \( \) \/ \\ \s]{2,}+$/;
    const regex = /^[a-zA-Z0-9.\-_()\/\s]{2,}[a-zA-Z0-9.-_()\/\s]*$/;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }

  }

  validatePfAccountNumber(text:string): boolean {
    // const regex = /^[A-Z]{5}[0-9]{17}$/;  // eg. MHMUM12345670001234567
    const regex = (/^[A-Z]{2}[\s\/]?[A-Z]{3}[\s\/]?[0-9]{7}[\s\/]?[0-9]{3}[\s\/]?[0-9]{7}$/);
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }
  }

  validateUAN(text:string): boolean {
    const regex = /^\d{12}$/; // eg.123456789012
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }
  }

  validateESICNumber(text:string): boolean {
    const regex = /^(\d{2})[-–\s]?(\d{2})[-–\s]?(\d{1,6})[-–\s]?(\d{3})[-–\s]?(\d{4})$/gm;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }
  }

  validatePassingGrade(text:string): boolean {
    const regex = /^[a-zA-Z0-9][a-zA-Z0-9.\s%]*$/gm;
    if (text !== "" || text !== undefined || text !== null) {
      if (regex.test(text)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }
  }
}
