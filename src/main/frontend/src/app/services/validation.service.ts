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
    const regex = /^\d{10}$/;
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

}
