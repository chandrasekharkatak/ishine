"use strict";
(self["webpackChunkemployee_portal_revamp"] = self["webpackChunkemployee_portal_revamp"] || []).push([["src_app_module-routing_employee360_employee360_module_ts"],{

/***/ 689:
/*!******************************************************************!*\
  !*** ./src/app/module-routing/employee360/employee360.module.ts ***!
  \******************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   Employee360Module: () => (/* binding */ Employee360Module)
/* harmony export */ });
/* harmony import */ var _angular_common__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/common */ 84460);
/* harmony import */ var _employee360_routing_module__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./employee360-routing.module */ 81920);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ 37580);



class Employee360Module {
  static {
    this.ɵfac = function Employee360Module_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || Employee360Module)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineNgModule"]({
      type: Employee360Module
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineInjector"]({
      imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _employee360_routing_module__WEBPACK_IMPORTED_MODULE_0__.Employee360RoutingModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵsetNgModuleScope"](Employee360Module, {
    imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _employee360_routing_module__WEBPACK_IMPORTED_MODULE_0__.Employee360RoutingModule]
  });
})();

/***/ }),

/***/ 60539:
/*!****************************************************!*\
  !*** ./src/app/employee360/Employee360Resolver.ts ***!
  \****************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   Employee360Resolver: () => (/* binding */ Employee360Resolver)
/* harmony export */ });
/* harmony import */ var rxjs__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! rxjs */ 59452);
/* harmony import */ var rxjs__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! rxjs */ 95429);
/* harmony import */ var rxjs_operators__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! rxjs/operators */ 98764);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! @angular/core */ 37580);
/* harmony import */ var _services_utility_service__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../services/utility.service */ 25190);
/* harmony import */ var _services_EncryptionService__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ../services/EncryptionService */ 23535);





class Employee360Resolver {
  constructor(utility, encryptionService) {
    this.utility = utility;
    this.encryptionService = encryptionService;
  }
  resolve(route, state) {
    const employeeId = route.paramMap.get('id');
    let encryptedEmployeeData = sessionStorage.getItem('employee360Data');
    let employeeData = null;
    if (encryptedEmployeeData) {
      const decryptedString = this.encryptionService.decrypt(encryptedEmployeeData);
      if (decryptedString) {
        try {
          employeeData = JSON.parse(decryptedString);
        } catch (error) {
          console.error('Failed to parse decrypted session user:', decryptedString, error);
          employeeData = null;
        }
      } else {
        console.warn('Decryption returned empty string.');
        employeeData = null;
      }
    } else {
      console.warn('No currentUser found in sessionStorage');
      employeeData = null;
    }
    const employee360Data = employeeData;
    if (employee360Data) {
      return (0,rxjs__WEBPACK_IMPORTED_MODULE_2__.of)(employee360Data); // Return cached data
    } else {
      return (0,rxjs__WEBPACK_IMPORTED_MODULE_3__.from)(this.utility.getEmployeeDetailsFor360ViewNewImple(employeeId)).pipe((0,rxjs_operators__WEBPACK_IMPORTED_MODULE_4__.tap)(data => {
        let encryptedData = this.encryptionService.encrypt(JSON.stringify(data));
        sessionStorage.setItem('employee360Data', encryptedData); // Store fetched data
      }));
    }
  }
  static {
    this.ɵfac = function Employee360Resolver_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || Employee360Resolver)(_angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵinject"](_services_utility_service__WEBPACK_IMPORTED_MODULE_0__.UtilityService), _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵinject"](_services_EncryptionService__WEBPACK_IMPORTED_MODULE_1__.EncryptionService));
    };
  }
  static {
    this.ɵprov = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵdefineInjectable"]({
      token: Employee360Resolver,
      factory: Employee360Resolver.ɵfac,
      providedIn: 'root'
    });
  }
}

/***/ }),

/***/ 81920:
/*!**************************************************************************!*\
  !*** ./src/app/module-routing/employee360/employee360-routing.module.ts ***!
  \**************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   Employee360RoutingModule: () => (/* binding */ Employee360RoutingModule)
/* harmony export */ });
/* harmony import */ var _angular_router__WEBPACK_IMPORTED_MODULE_11__ = __webpack_require__(/*! @angular/router */ 18431);
/* harmony import */ var src_app_employee360_employee360_appreciation_employee360_appreciation_component__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! src/app/employee360/employee360-appreciation/employee360-appreciation.component */ 13432);
/* harmony import */ var src_app_employee360_employee360_biomax_employee360_biomax_component__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! src/app/employee360/employee360-biomax/employee360-biomax.component */ 35974);
/* harmony import */ var src_app_employee360_employee360_leave_employee360_leave_component__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! src/app/employee360/employee360-leave/employee360-leave.component */ 28104);
/* harmony import */ var src_app_employee360_employee360_profile_employee360_profile_component__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! src/app/employee360/employee360-profile/employee360-profile.component */ 64576);
/* harmony import */ var src_app_employee360_employee360_project_employee360_project_component__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! src/app/employee360/employee360-project/employee360-project.component */ 28232);
/* harmony import */ var src_app_employee360_employee360_rewards_employee360_rewards_component__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! src/app/employee360/employee360-rewards/employee360-rewards.component */ 4624);
/* harmony import */ var src_app_employee360_employee360_timesheet_employee360_timesheet_component__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! src/app/employee360/employee360-timesheet/employee360-timesheet.component */ 61712);
/* harmony import */ var src_app_employee360_employee360_component__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! src/app/employee360/employee360.component */ 51176);
/* harmony import */ var src_app_employee360_Employee360Resolver__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! src/app/employee360/Employee360Resolver */ 60539);
/* harmony import */ var src_app_employee360_lms_lms_component__WEBPACK_IMPORTED_MODULE_9__ = __webpack_require__(/*! src/app/employee360/lms/lms.component */ 84508);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_10__ = __webpack_require__(/*! @angular/core */ 37580);













const routes = [{
  path: '',
  component: src_app_employee360_employee360_component__WEBPACK_IMPORTED_MODULE_7__.Employee360Component,
  resolve: {
    employeeData: src_app_employee360_Employee360Resolver__WEBPACK_IMPORTED_MODULE_8__.Employee360Resolver
  },
  children: [{
    path: 'profile',
    component: src_app_employee360_employee360_profile_employee360_profile_component__WEBPACK_IMPORTED_MODULE_3__.Employee360ProfileComponent
  }, {
    path: 'leave',
    component: src_app_employee360_employee360_leave_employee360_leave_component__WEBPACK_IMPORTED_MODULE_2__.Employee360LeaveComponent
  }, {
    path: 'project',
    component: src_app_employee360_employee360_project_employee360_project_component__WEBPACK_IMPORTED_MODULE_4__.Employee360ProjectComponent
  }, {
    path: 'timesheet',
    component: src_app_employee360_employee360_timesheet_employee360_timesheet_component__WEBPACK_IMPORTED_MODULE_6__.Employee360TimesheetComponent
  }, {
    path: 'biomax',
    component: src_app_employee360_employee360_biomax_employee360_biomax_component__WEBPACK_IMPORTED_MODULE_1__.Employee360BiomaxComponent
  }, {
    path: 'rewards',
    component: src_app_employee360_employee360_rewards_employee360_rewards_component__WEBPACK_IMPORTED_MODULE_5__.Employee360RewardsComponent
  }, {
    path: 'appreciation',
    component: src_app_employee360_employee360_appreciation_employee360_appreciation_component__WEBPACK_IMPORTED_MODULE_0__.Employee360AppreciationComponent
  }, {
    path: 'lms',
    component: src_app_employee360_lms_lms_component__WEBPACK_IMPORTED_MODULE_9__.LMSComponent
  }]
}];
class Employee360RoutingModule {
  static {
    this.ɵfac = function Employee360RoutingModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || Employee360RoutingModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵdefineNgModule"]({
      type: Employee360RoutingModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵdefineInjector"]({
      imports: [_angular_router__WEBPACK_IMPORTED_MODULE_11__.RouterModule.forChild(routes), _angular_router__WEBPACK_IMPORTED_MODULE_11__.RouterModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵsetNgModuleScope"](Employee360RoutingModule, {
    imports: [_angular_router__WEBPACK_IMPORTED_MODULE_11__.RouterModule],
    exports: [_angular_router__WEBPACK_IMPORTED_MODULE_11__.RouterModule]
  });
})();

/***/ })

}]);
//# sourceMappingURL=src_app_module-routing_employee360_employee360_module_ts.js.map