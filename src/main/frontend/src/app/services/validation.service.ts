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
    const regex = /^[6-9]\d{9}$/;
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
    const regex = /[A-Z]{5}[0-9]{4}[A-Z]{1}/;
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

  validateEmployeementId(text: any): boolean {
    const regex = /^[1-9]\d{1,6}$/;
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

 
    const regex = /^[A-Za-z0-9@#$%!+*÷=\/_\-'":;,()^{}~\[\]]{8,}$/;

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

  validateActivityTimesheetDiscription(text:string): boolean {
    const regex = (/^[A-Za-z][A-Za-z\s,/'&"-]*$/) ;
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
    const regex = (/^[A-Za-z][A-Za-z\s]*$/) ;
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

  validateSurveyName(text:string): boolean {
    const regex = (/^[a-z][a-z\s]*$/i) ;
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

  validateQuestion(text:string): boolean {
    const regex = (/^[a-z][a-z\s?]*$/i) ;
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
    const regex = (/[a-zA-Z][a-zA-Z ]+[a-zA-Z]$/gm) ;
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
