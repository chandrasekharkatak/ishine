# Travel Desk Requests, Travel-based Reimbursements & General Reimbursements/Allowances - Module Analysis

## 1️⃣ STORY POINTS / FUNCTIONAL SCOPE

### **Travel Desk Requests Module**

#### **Employee Features (Story Points)**
- **SP-1**: Employee can create travel requests with details (travel mode, class, dates, locations, purpose)
- **SP-2**: Employee can upload supporting documents (KYC documents, travel documents) for travel requests
- **SP-3**: Employee can view their own travel requests with status tracking
- **SP-4**: Employee can update pending travel requests before approval
- **SP-5**: Employee can revoke/cancel their travel requests
- **SP-6**: Employee can apply for travel-based reimbursements linked to approved travel requests
- **SP-7**: Employee can submit multiple invoices for a single travel request
- **SP-8**: Employee can upload invoice documents for travel-based reimbursement claims
- **SP-9**: Employee can view all invoices submitted for their travel requests
- **SP-10**: Employee can update/resubmit rejected invoices

#### **Manager/Approver Features (Story Points)**
- **SP-11**: Level 1 Approver (Reporting Manager) can view pending travel requests assigned to them
- **SP-12**: Level 1 Approver can approve/reject travel requests with remarks
- **SP-13**: Level 2 Approver (HOD) can view travel requests approved by Level 1
- **SP-14**: Level 2 Approver can approve/reject travel requests with remarks
- **SP-15**: Approvers receive email notifications for pending approvals
- **SP-16**: Approvers can view travel request details and supporting documents

#### **Admin/HR Features (Story Points)**
- **SP-17**: Admin can view all travel requests across the organization
- **SP-18**: Admin can upload travel tickets/accommodation documents after approval
- **SP-19**: Admin can configure travel master data (Travel Reasons, Travel Modes, Travel Classes)
- **SP-20**: Admin can configure hotel categories, sub-categories, and cities
- **SP-21**: Admin receives notifications when travel requests are approved for ticket booking

#### **Accounts Team Features (Story Points)**
- **SP-22**: Accounts team can view all travel-based reimbursement invoices
- **SP-23**: Accounts team can approve/reject invoices with rejection reasons
- **SP-24**: Accounts team can mark reimbursements as paid
- **SP-25**: Accounts team receives notifications for approved travel requests requiring payment processing

### **General Reimbursements/Allowances Module**

#### **Employee Features (Story Points)**
- **SP-26**: Employee can create general reimbursement requests (Travel, Food, Other expenses)
- **SP-27**: Employee can select expenditure type (Travel, Food, etc.)
- **SP-28**: Employee can enter travel-related reimbursements (mode, distance, vehicle type)
- **SP-29**: Employee can enter food allowance reimbursements (food type, date)
- **SP-30**: Employee can upload multiple supporting documents for reimbursement claims
- **SP-31**: Employee can view their reimbursement request history
- **SP-32**: Employee can update pending reimbursement requests
- **SP-33**: Employee can revoke/cancel reimbursement requests

#### **Manager/Approver Features (Story Points)**
- **SP-34**: Level 1 Approver can view pending reimbursement requests
- **SP-35**: Level 1 Approver can approve/reject reimbursement requests with remarks
- **SP-36**: Level 2 Approver (HOD) can view reimbursement requests approved by Level 1
- **SP-37**: Level 2 Approver can approve/reject reimbursement requests with remarks
- **SP-38**: Approvers receive email notifications for pending reimbursements

#### **Admin/HR Features (Story Points)**
- **SP-39**: Admin can view all reimbursement requests across the organization
- **SP-40**: Admin can configure reimbursement master data (Expenditure Types, Travel Modes, Vehicle Types, Food Types)
- **SP-41**: Admin can manage reimbursement approval workflows

#### **Accounts Team Features (Story Points)**
- **SP-42**: Accounts team can view all approved reimbursement requests
- **SP-43**: Accounts team can validate and approve/reject reimbursements for payment
- **SP-44**: Accounts team can mark reimbursements as paid
- **SP-45**: Accounts team receives notifications for approved reimbursements requiring payment

### **Business Use Cases Covered**
1. **Travel Request Management**: Complete lifecycle from request creation to ticket booking
2. **Travel-based Reimbursement**: Invoice submission and processing linked to travel requests
3. **General Reimbursement**: Multiple expenditure types (Travel, Food, Others)
4. **Multi-level Approval Workflow**: 2-level approval for travel requests, 2-level for reimbursements
5. **Document Management**: Upload, preview, and manage supporting documents
6. **Email Notifications**: Automated notifications at each stage of approval
7. **Master Data Configuration**: Admin configuration for travel and reimbursement settings
8. **Invoice Management**: Multiple invoices per travel request with validation
9. **Payment Processing**: Accounts team workflow for reimbursement payment

### **User Roles & Access**
- **Employee**: Create, view, update, revoke own requests
- **Manager (Level 1 Approver)**: Approve/reject direct reports' requests
- **HOD (Level 2 Approver)**: Final approval authority
- **Admin**: View all requests, upload tickets, configure master data
- **Accounts Team**: Process invoices and mark payments
- **HR**: View all requests for reporting and compliance

---

## 2️⃣ FRONTEND COMPONENTS (ANGULAR)

### **Travel Desk Module Components**

| Component Name | Path | Purpose / Responsibility | APIs Consumed |
|---------------|------|-------------------------|---------------|
| **TravelAllowanceComponent** | `src/app/travel-allowance/travel-allowance.component.ts` | Parent container component for travel desk module | N/A (Container) |
| **MyTravelrequestComponent** | `src/app/travel-allowance/my-travelrequest/my-travelrequest.component.ts` | Employee view: Create, view, update travel requests | `fetchTravelData`, `saveTravelData`, `updateTravelData`, `revokeTravel`, `uploadFile`, `uploadKycDocument`, `getTravelReason`, `getTravelMode`, `getTravelClassByMode`, `getHotelCategory`, `getHotelSubCategory`, `getCityBySubCategory` |
| **ViewTravelrequestComponent** | `src/app/travel-allowance/view-travelrequest/view-travelrequest.component.ts` | Employee view: View travel request details and apply reimbursement | `fetchTravelData`, `getAllDocumentsThroughRequestId`, `submitReimbursmentBasedOnTravelRequest`, `uploadFileTravelBased`, `checkInvoiceNumberPresentorNot`, `previewDocument`, `getAllInvoicesByEmpId`, `updateReimbursmentBasedOnTravelRequest`, `updateUploadedFile`, `checkInvoiceNumberAgainstResubmit` |
| **TravelrequestapprovalComponent** | `src/app/travel-allowance/travelrequestapproval/travelrequestapproval.component.ts` | Manager/Approver view: Approve/reject travel requests | `fetchTravelDataForApproval`, `approveOrRejectTraveldesk`, `getAllDocumentsThroughRequestId` |
| **TotalTravelrequestComponent** | `src/app/travel-allowance/total-travelrequest/total-travelrequest.component.ts` | Admin view: View all travel requests across organization | `totalTravelData`, `getAllDocumentsThroughRequestId` |
| **TravelConfigComponent** | `src/app/configuration/travel-config/travel-config.component.ts` | Admin view: Configure travel master data (Reasons, Modes, Classes, Hotels, Cities) | `saveTravelReason`, `getTravelReason`, `saveTravelMode`, `getTravelMode`, `saveTravelClass`, `onGetTravelCass`, `saveHotelCategory`, `getHotelCategory`, `saveHotelSubCategory`, `getHotelSubCategory`, `saveCity`, `getCity` |

### **Reimbursement Module Components**

| Component Name | Path | Purpose / Responsibility | APIs Consumed |
|---------------|------|-------------------------|---------------|
| **ReimbursementComponent** | `src/app/reimbursement/reimbursement.component.ts` | Parent container component for reimbursement module | N/A (Container) |
| **MyReimbursementComponent** | `src/app/reimbursement/my-reimbursement/my-reimbursement.component.ts` | Employee view: Create, view, update general reimbursement requests | `fetchReimbursementData`, `saveReimbursementData`, `updateReimbursementData`, `revokeReimbursement`, `uploadFileReimbursement`, `onGetExpenditureType`, `getTravelMode`, `onGetVehicleType`, `onGetFoodType` |
| **ViewReimbursementComponent** | `src/app/reimbursement/view-reimbursement/view-reimbursement.component.ts` | Employee view: View reimbursement request details and documents | `fetchReimbursementData`, `getAllDocumentsReimbursmentThroughRequestId`, `previewDocumentReimbursment` |
| **ReimbursementapprovalComponent** | `src/app/reimbursement/reimbursementapproval/reimbursementapproval.component.ts` | Manager/Approver view: Approve/reject reimbursement requests | `fetchReimbursementDataforApproval`, `approveOrRejectReimbursement`, `getAllDocumentsReimbursmentThroughRequestId` |
| **TotalReimbursementrequestComponent** | `src/app/reimbursement/total-reimbursementrequest/total-reimbursementrequest.component.ts` | Admin/Accounts view: View all reimbursement requests | `fetchTotalReimbursementData`, `getAllDocumentsReimbursmentThroughRequestId`, `previewDocumentReimbursment`, `updateReimbursementDetailsByAccountsTeam` |
| **ReimbursmentConfigComponent** | `src/app/configuration/reimbursment-config/reimbursment-config.component.ts` | Admin view: Configure reimbursement master data | `saveExpenditureType`, `onGetExpenditureType`, `saveTravelMode`, `getTravelMode`, `saveVehicleType`, `onGetVehicleType`, `saveFoodType`, `onGetFoodType` |

### **Shared Services**

| Service Name | Path | Purpose |
|-------------|------|---------|
| **TravelDeskService** | `src/app/services/travel-desk.service.ts` | HTTP service for all travel desk API calls |
| **ReimbursementService** | `src/app/services/reimbursement.service.ts` | HTTP service for all reimbursement API calls |

### **Models/Interfaces**

| Model Name | Path | Purpose |
|-----------|------|---------|
| **MyTravelDesk** | `src/app/models/travelDesk.ts` | TypeScript interface for travel desk requests |
| **MyReimbursement** | `src/app/models/reimbursement.ts` | TypeScript interface for reimbursement requests |
| **TravelBasedReimbursement** | `src/app/models/travelBasedReimbursement.ts` | TypeScript interface for travel-based reimbursement invoices |

### **Routing Configuration**

| Module | Routing File | Routes |
|--------|-------------|--------|
| **Travel Desk** | `src/app/module-routing/travel-desk/travel-desk-routing.module.ts` | `/my-travelrequest`, `/view-travelrequest`, `/approve-travelrequest`, `/total-travelrequest` |
| **Reimbursement** | `src/app/module-routing/reimbursement/reimbursement-routing.module.ts` | `/my-reimbursement`, `/view-reimbursement`, `/approve-reimbursement`, `/total-reimbursement` |

### **Component Relationships**
```
TravelAllowanceComponent (Parent)
├── MyTravelrequestComponent (Create/View)
├── ViewTravelrequestComponent (Details/Reimbursement)
├── TravelrequestapprovalComponent (Approval)
└── TotalTravelrequestComponent (Admin View)

ReimbursementComponent (Parent)
├── MyReimbursementComponent (Create/View)
├── ViewReimbursementComponent (Details)
├── ReimbursementapprovalComponent (Approval)
└── TotalReimbursementrequestComponent (Admin/Accounts View)
```

---

## 3️⃣ BACKEND APIS (SPRING BOOT)

### **Travel Desk Controller APIs**

| API Name | Endpoint | Method | Description | Controller → Service → Repository Flow |
|----------|----------|--------|-------------|------------------------------------------|
| **Fetch User Travel Data** | `/api/fetchTravelData` | POST | Get all travel requests for an employee | `TravelDeskController` → `TravelDeskService.fetchUserTravel()` → `TravelDeskRepository.findByEmpId()` |
| **Fetch Travel Data for Approval** | `/api/fetchTravelDataForApproval` | POST | Get pending travel requests for approver | `TravelDeskController` → `TravelDeskService.fetchUserTravelForApproval()` → `TravelDeskRepository.findByApprover1()` / `findByApprover2()` |
| **Save Travel Data** | `/api/saveTravelData` | POST | Create new travel request | `TravelDeskController` → `TravelDeskService.saveTravelData()` → `TravelDeskRepository.save()` |
| **Update Travel Data** | `/api/updateTravelData` | POST | Update existing travel request | `TravelDeskController` → `TravelDeskService.updateTravelData()` → `TravelDeskRepository.findByRequestId()` → `save()` |
| **Revoke Travel** | `/api/revokeTravel` | POST | Cancel/revoke travel request | `TravelDeskController` → `TravelDeskService.revokeTravel()` → `TravelDeskRepository.findByRequestId()` → `save()` |
| **Approve/Reject Travel** | `/api/approveOrRejectTravel` | POST | Approve or reject travel request | `TravelDeskController` → `TravelDeskService.approveRejectTravel()` → `TravelDeskRepository.findByRequestId()` → `save()` |
| **Upload File** | `/api/uploadFile` | POST | Upload supporting documents | `TravelDeskController` → `TravelDeskService.uploadFile()` → `NewsletterRepository.save()` |
| **Upload Ticket** | `/api/uploadTicket` | POST | Upload travel ticket/accommodation document | `TravelDeskController` → `TravelDeskService.uploadTicket()` → `NewsletterRepository.save()` → `TravelDeskRepository.save()` |
| **Upload KYC Document** | `/api/uploadKycDocument` | POST | Upload KYC document for travel | `TravelDeskController` → `TravelDeskService.uploadKycDocument()` → `NewsletterRepository.save()` |
| **Total Travel Data** | `/api/totalTravelData` | POST | Get all travel requests (Admin) | `TravelDeskController` → `TravelDeskService.totalTravelData()` → `TravelDeskRepository.findAll()` |
| **Get All Documents by Request ID** | `/api/getAllDocumentsThroughRequestId` | GET | Get document IDs for a travel request | `TravelDeskController` → `TravelDeskService.getAllDocsThroughReqId()` → `TravelDeskRepository.findByRequestId()` |
| **Save Travel Reason** | `/api/travel-reason/create` | POST | Create travel reason (Master Data) | `TravelDeskController` → `TravelDeskService.saveTravelReason()` → `TravelReasonRepository.save()` |
| **Get Travel Reasons** | `/api/getTravelReason` | GET | Get all travel reasons | `TravelDeskController` → `TravelDeskService.getAllTravelReasons()` → `TravelReasonRepository.findAll()` |
| **Save Travel Mode** | `/api/saveTravelMode` | POST | Create travel mode (Master Data) | `TravelDeskController` → `TravelDeskService.saveTravelMode()` → `TravelModeRepository.save()` |
| **Get Travel Modes** | `/api/getTravelMode` | GET | Get all travel modes | `TravelDeskController` → `TravelDeskService.getAllgetTravelModes()` → `TravelModeRepository.findAllData()` |
| **Get Travel Mode by Reason** | `/api/getTravelModeByReason` | POST | Get travel modes filtered by reason | `TravelDeskController` → `TravelDeskService.getTravelModeByReason()` → `TravelModeRepository.findByTravelReasonId()` |
| **Save Travel Class** | `/api/saveTravelClass` | POST | Create travel class (Master Data) | `TravelDeskController` → `TravelDeskService.saveTravelClass()` → `TravelClassRepository.save()` |
| **Get Travel Class by Mode** | `/api/getTravelClassByMode` | POST | Get travel classes filtered by mode | `TravelDeskController` → `TravelDeskService.getTravelClassByMode()` → `TravelClassRepository.findByTravelModeId()` |
| **Get Travel Classes** | `/api/onGetTravelCass` | GET | Get all travel classes | `TravelDeskController` → `TravelDeskService.onGetTravelCass()` → `TravelClassRepository.findAll()` |
| **Save Hotel Category** | `/api/saveHotelCategory` | POST | Create hotel category (Master Data) | `TravelDeskController` → `TravelDeskService.saveHotelCategory()` → `HotelCategoryRepository.save()` |
| **Get Hotel Categories** | `/api/getHotelCategory` | GET | Get all hotel categories | `TravelDeskController` → `TravelDeskService.getHotelCategory()` → `HotelCategoryRepository.findAll()` |
| **Save Hotel Sub Category** | `/api/saveHotelSubCategory` | POST | Create hotel sub-category (Master Data) | `TravelDeskController` → `TravelDeskService.saveHotelSubCategory()` → `HotelSubCategoryRepository.save()` |
| **Get Hotel Sub Categories** | `/api/getHotelSubCategory` | GET | Get all hotel sub-categories | `TravelDeskController` → `TravelDeskService.getHotelSubCategory()` → `HotelSubCategoryRepository.findAll()` |
| **Save City** | `/api/saveCity` | POST | Create city (Master Data) | `TravelDeskController` → `TravelDeskService.saveCity()` → `CityRepository.save()` |
| **Get City by Sub Category** | `/api/getCityBySubCategory` | POST | Get cities filtered by sub-category | `TravelDeskController` → `TravelDeskService.getCityBySubCategory()` → `CityRepository.findBySubCategoryIds()` |
| **Get Cities** | `/api/getCity` | GET | Get all cities | `TravelDeskController` → `TravelDeskService.getCity()` → `CityRepository.findAll()` |

### **Reimbursement Controller APIs**

| API Name | Endpoint | Method | Description | Controller → Service → Repository Flow |
|----------|----------|--------|-------------|------------------------------------------|
| **Fetch Reimbursement Data** | `/api/fetchReimbursementData` | POST | Get all reimbursement requests for an employee | `ReimbursementController` → `ReimbursementService.fetchReimbursementData()` → `ReimbursementDataRepository.findByEmpId()` |
| **Fetch Reimbursement Data for Approval** | `/api/fetchReimbursementDataforApproval` | POST | Get pending reimbursement requests for approver | `ReimbursementController` → `ReimbursementService.fetchReimbursementDataforApproval()` → `ReimbursementDataRepository.findByApprover1()` / `findByApprover2()` |
| **Save Reimbursement Data** | `/api/saveReimbursementData` | POST | Create new reimbursement request | `ReimbursementController` → `ReimbursementService.saveReimbursementData()` → `ReimbursementDataRepository.save()` |
| **Update Reimbursement Data** | `/api/updateReimbursementData` | POST | Update existing reimbursement request | `ReimbursementController` → `ReimbursementService.updateReimbursementData()` → `ReimbursementDataRepository.findByRequestId()` → `save()` |
| **Revoke Reimbursement** | `/api/revokeReimbursement` | POST | Cancel/revoke reimbursement request | `ReimbursementController` → `ReimbursementService.revokeReimbursement()` → `ReimbursementDataRepository.findByRequestId()` → `save()` |
| **Approve/Reject Reimbursement** | `/api/approveOrRejectReimbursement` | POST | Approve or reject reimbursement request | `ReimbursementController` → `ReimbursementService.approveOrRejectReimbursement()` → `ReimbursementDataRepository.findByRequestId()` → `save()` |
| **Upload File Reimbursement** | `/api/uploadFileReimbursement` | POST | Upload supporting documents for reimbursement | `ReimbursementController` → `ReimbursementService.uploadFile()` → `NewsletterRepository.save()` |
| **Fetch Total Reimbursement Data** | `/api/fetchTotalReimbursementData` | POST | Get all reimbursement requests (Admin) | `ReimbursementController` → `ReimbursementService.fetchTotalReimbursementData()` → `ReimbursementDataRepository.findAll()` |
| **Get All Documents by Request ID** | `/api/getAllDocumentsReimbursmentThroughRequestId` | GET | Get document IDs for a reimbursement request | `ReimbursementController` → `ReimbursementService.getAllDocumentsReimbursmentThroughRequestId()` → `ReimbursementDataRepository.findByRequestId()` |
| **Preview Document Reimbursement** | `/api/previewDocumentReimbursment` | POST | Preview reimbursement document | `ReimbursementController` → `ReimbursementService.previewDocumentReimbursment()` → `NewsletterRepository.findByDocId()` |
| **Update Reimbursement by Accounts Team** | `/api/updateReimbursementDetailsByAccountsTeam` | POST | Accounts team approve/reject reimbursement | `ReimbursementController` → `ReimbursementService.updateReimbursementDetailsByAccountsTeam()` → `ReimbursementDataRepository.findByRequestId()` → `save()` |
| **Save Expenditure Type** | `/api/saveExpenditureType` | POST | Create expenditure type (Master Data) | `ReimbursementController` → `ReimbursementService.saveExpenditureType()` → `ExpenditureTypeRepository.save()` |
| **Get Expenditure Types** | `/api/onGetExpenditureType` | GET | Get all expenditure types | `ReimbursementController` → `ReimbursementService.getAllExpenditureType()` → `ExpenditureTypeRepository.findAll()` |
| **Save Reimbursement Travel Mode** | `/api/saveReimbursementTravelMode` | POST | Create travel mode for reimbursement (Master Data) | `ReimbursementController` → `ReimbursementService.saveTravelMode()` → `ReimbursementTravelModeRepository.save()` |
| **Get Reimbursement Travel Modes** | `/api/getReimbursementTravelMode` | GET | Get all reimbursement travel modes | `ReimbursementController` → `ReimbursementService.getAllgetTravelModes()` → `ReimbursementTravelModeRepository.findAll()` |
| **Save Vehicle Type** | `/api/saveVehicleType` | POST | Create vehicle type (Master Data) | `ReimbursementController` → `ReimbursementService.saveVehicleType()` → `VehicleTypeRepository.save()` |
| **Get Vehicle Types** | `/api/onGetVehicleType` | GET | Get all vehicle types | `ReimbursementController` → `ReimbursementService.getAllVehicleType()` → `VehicleTypeRepository.findAll()` |
| **Save Food Type** | `/api/saveFoodType` | POST | Create food type (Master Data) | `ReimbursementController` → `ReimbursementService.saveFoodType()` → `FoodTypeRepository.save()` |
| **Get Food Types** | `/api/onGetFoodType` | GET | Get all food types | `ReimbursementController` → `ReimbursementService.getAllFoodType()` → `FoodTypeRepository.findAll()` |

### **Travel-Based Reimbursement Controller APIs**

| API Name | Endpoint | Method | Description | Controller → Service → Repository Flow |
|----------|----------|--------|-------------|------------------------------------------|
| **Submit Reimbursement Based on Travel Request** | `/api/submitReimbursmentBasedOnTravelRequest` | POST | Submit invoice(s) for travel-based reimbursement | `ReimbursmentBasedOnTravelRequestController` → `TravelBasedReimbursementRequestService.submitReimbursmentBasedOnTravelRequest()` → `TravelBasedReimbursementRequestRepository.save()` |
| **Upload File Travel Based** | `/api/uploadFiletravelBased` | POST | Upload invoice document for travel-based reimbursement | `ReimbursmentBasedOnTravelRequestController` → `TravelBasedReimbursementRequestService.uploadFile()` → `NewsletterRepository.save()` → `TravelBasedReimbursementRequestRepository.save()` |
| **Check Invoice Number Present or Not** | `/api/checkInvoiceNumberPresentorNot` | POST | Validate if invoice number already exists | `ReimbursmentBasedOnTravelRequestController` → `TravelBasedReimbursementRequestService.checkInvoiceNumberPresentorNot()` → `TravelBasedReimbursementRequestRepository.findInvoiceDetails()` |
| **Check Invoice Number Against Resubmit** | `/api/checkInvoiceNumberAgainstResubmit` | POST | Validate invoice number for update/resubmit | `ReimbursmentBasedOnTravelRequestController` → `TravelBasedReimbursementRequestService.checkInvoiceNumberAgainstResubmit()` → `TravelBasedReimbursementRequestRepository.canUpdateInvoiceSerial()` |
| **Preview Document** | `/api/previewDocument` | POST | Preview invoice document | `ReimbursmentBasedOnTravelRequestController` → `TravelBasedReimbursementRequestService.previewDocument()` → `NewsletterRepository.findByDocId()` |
| **Get All Invoices** | `/api/getAllInvoices` | GET | Get all invoices (Accounts Team) | `ReimbursmentBasedOnTravelRequestController` → `TravelBasedReimbursementRequestService.getAllInvoices()` → `TravelBasedReimbursementRequestRepository.findAllByTravel()` |
| **Get All Invoices by Employee ID** | `/api/getAllInvoicesByEmpId` | POST | Get all invoices for an employee | `ReimbursmentBasedOnTravelRequestController` → `TravelBasedReimbursementRequestService.getAllInvoicesByEmpId()` → `TravelBasedReimbursementRequestRepository.findAllByTravelByEmpId()` |
| **Update Invoices Details by Accounts Team** | `/api/updateInvoicesDetailsByAccountsTeam` | POST | Accounts team approve/reject invoice | `ReimbursmentBasedOnTravelRequestController` → `TravelBasedReimbursementRequestService.updateInvoicesDetailsByAccountsTeam()` → `TravelBasedReimbursementRequestRepository.findInvoiceDetails()` → `save()` |
| **Update Reimbursement Based on Travel Request** | `/api/updateReimbursmentBasedOnTravelRequest` | POST | Update invoice details | `ReimbursmentBasedOnTravelRequestController` → `TravelBasedReimbursementRequestService.updateReimbursmentBasedOnTravelRequest()` → `TravelBasedReimbursementRequestRepository.findInvoiceBySerialNo()` → `save()` |
| **Update Uploaded File** | `/api/updateUploadedFile` | POST | Update invoice document | `ReimbursmentBasedOnTravelRequestController` → `TravelBasedReimbursementRequestService.updateUploadedFile()` → `NewsletterRepository.findByDocId()` → `save()` |
| **Mark as Paid** | `/api/markAsPaid` | POST | Mark all invoices for a travel request as paid | `ReimbursmentBasedOnTravelRequestController` → `TravelBasedReimbursementRequestService.markAsPaid()` → `TravelBasedReimbursementRequestRepository.findAllByTravelId()` → `save()` |

---

## 4️⃣ DATABASE TABLES & REPOSITORIES

### **Transaction Tables**

| Table Name | Entity Name | Repository Name | Usage in Module | Type |
|-----------|------------|----------------|-----------------|------|
| **travel_desk** | `TravelDesk` | `TravelDeskRepository` | Main table storing all travel requests. Contains request details, approval status, approver information, dates, locations, travel mode/class, hotel/city info, document IDs | Transaction |
| **reimbursement_data** | `ReimbursementData` | `ReimbursementDataRepository` | Main table storing all general reimbursement requests. Contains employee info, expenditure type, amount, dates, travel details (if applicable), approval status, document IDs | Transaction |
| **travel_based_reimbursement_request** | `TravelBasedReimbursementRequest` | `TravelBasedReimbursementRequestRepository` | Stores invoices submitted for travel-based reimbursements. Links to travel_desk via travel_id. Contains invoice number, date, amount, document ID, approval status | Transaction |

### **Master Data Tables**

| Table Name | Entity Name | Repository Name | Usage in Module | Type |
|-----------|------------|----------------|-----------------|------|
| **travel_reason** | `TravelReason` | `TravelReasonRepository` | Master data for travel reasons (e.g., Business Trip, Training). Used to categorize travel requests | Master |
| **travel_mode** | `TravelMode` | `TravelModeRepository` | Master data for travel modes (e.g., Flight, Train, Bus). Linked to travel_reason. Used in travel requests | Master |
| **travel_class** | `TravelClass` | `TravelClassRepository` | Master data for travel classes (e.g., Economy, Business). Linked to travel_reason and travel_mode. Used in travel requests | Master |
| **hotel_category** | `HotelCategory` | `HotelCategoryRepository` | Master data for hotel categories. Used for accommodation requests | Master |
| **hotel_sub_category** | `HotelSubCategory` | `HotelSubCategoryRepository` | Master data for hotel sub-categories. Linked to hotel_category | Master |
| **city** | `City` | `CityRepository` | Master data for cities. Linked to hotel_category and hotel_sub_category. Used for accommodation location | Master |
| **expenditure_type** | `ExpenditureType` | `ExpenditureTypeRepository` | Master data for expenditure types (e.g., Travel, Food, Other). Used in reimbursement requests | Master |
| **reimbursement_travel_mode** | `ReimbursementTravelMode` | `ReimbursementTravelModeRepository` | Master data for travel modes in reimbursement context. Linked to expenditure_type. May require vehicle type | Master |
| **vehicle_type** | `VehicleType` | `VehicleTypeRepository` | Master data for vehicle types (e.g., Car, Bike). Used when travel mode is Personal Vehicle | Master |
| **food_type** | `FoodType` | `FoodTypeRepository` | Master data for food types. Used in food allowance reimbursement requests | Master |

### **Supporting Tables**

| Table Name | Entity Name | Repository Name | Usage in Module | Type |
|-----------|------------|----------------|-----------------|------|
| **newsletter** | `Newsletter` | `NewsletterRepository` | Stores document metadata (file name, display name, type). Used for travel documents, reimbursement documents, invoices, tickets, KYC documents. Type field distinguishes: "TRAVEL ALLOWANCE", "Reimbursement", "TRAVEL KYC DOCUMENT" | Supporting/Reference |
| **employee** | `Employee` | `EmployeeRepository` | Reference table for employee information. Used to fetch employee details, reporting manager, approver information | Reference |

### **Key Relationships**

1. **travel_desk** → **travel_based_reimbursement_request**: One-to-Many (one travel request can have multiple invoices)
   - Join: `travel_desk.request_id = travel_based_reimbursement_request.travel_id`

2. **travel_mode** → **travel_reason**: Many-to-One (multiple modes per reason)
   - Join: `travel_mode.travel_reason_id = travel_reason.id`

3. **travel_class** → **travel_mode**: Many-to-One (multiple classes per mode)
   - Join: `travel_class.travel_mode_id = travel_mode.travel_mode_id`

4. **travel_class** → **travel_reason**: Many-to-One (multiple classes per reason)
   - Join: `travel_class.travel_reason_id = travel_reason.id`

5. **hotel_sub_category** → **hotel_category**: Many-to-One (multiple sub-categories per category)
   - Join: `hotel_sub_category.hotel_category_id = hotel_category.id`

6. **city** → **hotel_sub_category**: Many-to-One (multiple cities per sub-category)
   - Join: `city.hotel_sub_category_id = hotel_sub_category.id`

7. **city** → **hotel_category**: Many-to-One (multiple cities per category)
   - Join: `city.hotel_category_id = hotel_category.id`

8. **reimbursement_travel_mode** → **expenditure_type**: Many-to-One (multiple modes per expenditure type)
   - Join: `reimbursement_travel_mode.expenditure_type_id = expenditure_type.id`

### **Repository Query Methods**

#### **TravelDeskRepository**
- `findByEmpId(BigInteger empId)` - Get all travel requests for an employee
- `findByRequestId(BigInteger requestId)` - Get travel request by ID
- `findAll()` - Get all travel requests
- `findByApprover1(BigInteger empId)` - Get requests pending Level 1 approval
- `findByApprover2(BigInteger empId)` - Get requests pending Level 2 approval

#### **ReimbursementDataRepository**
- `findByEmpId(BigInteger empId)` - Get all reimbursement requests for an employee
- `findByRequestId(BigInteger requestId)` - Get reimbursement request by ID
- `findAll()` - Get all reimbursement requests
- `findByApprover1(BigInteger empId)` - Get requests pending Level 1 approval
- `findByApprover2(String empId)` - Get requests pending Level 2 approval

#### **TravelBasedReimbursementRequestRepository**
- `findInvoiceDetails(String invoiceNo)` - Get invoice by invoice number
- `findAllByTravel()` - Get all invoices with travel request details (JOIN query)
- `findAllByTravelByEmpId(String empId)` - Get all invoices for an employee (JOIN query)
- `findInvoiceBySerialNo(Integer serialNo)` - Get invoice by serial number
- `findAllByTravelId(Integer travelId)` - Get all invoices for a travel request
- `canUpdateInvoiceSerial(String invoiceNo, Integer serialNo)` - Check if invoice number can be updated (duplicate validation)

---

## 5️⃣ FINAL SUMMARY

### **Overall Complexity Assessment**

**Complexity Level**: **HIGH**

The Travel Desk Requests, Travel-based Reimbursements, and General Reimbursements/Allowances module is a comprehensive, enterprise-grade solution with the following characteristics:

#### **Functional Complexity**
- **Multi-level Approval Workflows**: 2-level approval for both travel requests and reimbursements
- **Multiple Request Types**: Travel requests, travel-based reimbursements, general reimbursements (Travel, Food, Others)
- **Document Management**: Multiple document types (KYC, travel docs, invoices, tickets, supporting documents)
- **Invoice Management**: Multiple invoices per travel request with duplicate validation
- **Master Data Configuration**: Extensive master data setup (10+ master tables)
- **Email Notifications**: Automated notifications at every stage of the workflow

#### **Technical Complexity**
- **Frontend**: 11 Angular components with complex form validations and document handling
- **Backend**: 3 main controllers with 50+ API endpoints
- **Database**: 13 tables (3 transaction, 10 master/reference)
- **Business Logic**: Complex approval state management, invoice validation, document linking

### **Read vs Write Ratio**

**Read Operations**: ~60%
- Fetching travel/reimbursement requests (employee, approver, admin views)
- Fetching master data (reasons, modes, classes, etc.)
- Document preview and retrieval
- Invoice listing and viewing

**Write Operations**: ~40%
- Creating/updating travel requests
- Creating/updating reimbursement requests
- Submitting invoices
- Approval/rejection actions
- Document uploads
- Master data configuration

### **Performance Sensitivity**

#### **High Performance Impact Areas**

1. **JOIN Queries**:
   - `findAllByTravel()` - Joins `travel_based_reimbursement_request` with `travel_desk` (used by Accounts team)
   - `findAllByTravelByEmpId()` - Same JOIN filtered by employee ID
   - These queries return large result sets with multiple columns

2. **Document Retrieval**:
   - Multiple document lookups per request
   - File system I/O for document preview
   - Base64 encoding for document transmission

3. **Approval Workflow Queries**:
   - Filtering by approver ID and status
   - Multiple status checks (Pending, Approved, Rejected)
   - Active record filtering (`isActive != 0`)

4. **Master Data Cascading**:
   - Hierarchical master data (Reason → Mode → Class, Category → Sub-Category → City)
   - Multiple queries to build dropdown options

#### **Performance Recommendations**

1. **Database Indexing**:
   - Index on `travel_desk.emp_id`, `travel_desk.approver1`, `travel_desk.approver2`
   - Index on `reimbursement_data.emp_id`, `reimbursement_data.approver1`, `reimbursement_data.approver2`
   - Index on `travel_based_reimbursement_request.travel_id`, `invoice_no`
   - Composite indexes on status and isActive columns

2. **Query Optimization**:
   - Consider pagination for `totalTravelData()` and `fetchTotalReimbursementData()`
   - Use DTO projections instead of full entity fetches for list views
   - Cache master data (Travel Reasons, Modes, Classes, etc.)

3. **Document Management**:
   - Consider cloud storage (S3, Azure Blob) instead of file system
   - Implement document CDN for faster access
   - Lazy load documents (load on demand, not with request)

4. **Caching Strategy**:
   - Cache master data (TTL: 1 hour)
   - Cache employee/approver information (TTL: 30 minutes)
   - Consider Redis for session-based caching

### **Design Risks & Improvement Opportunities**

#### **Identified Risks**

1. **Document ID Storage**:
   - `travel_desk.doc_id` stores comma-separated document IDs as string
   - `reimbursement_data.doc_id` also uses comma-separated string
   - **Risk**: Difficult to query, no referential integrity, parsing overhead
   - **Recommendation**: Create junction tables (`travel_desk_documents`, `reimbursement_documents`)

2. **Approval Level Management**:
   - Level stored as integer (1, 2) with status fields at each level
   - **Risk**: Complex state management, potential for inconsistent states
   - **Recommendation**: Consider state machine pattern or workflow engine

3. **Email Notification Failures**:
   - Email sending wrapped in try-catch but errors are silently swallowed
   - **Risk**: Users may not receive notifications without knowing
   - **Recommendation**: Implement email queue with retry mechanism, log failures

4. **Invoice Duplicate Validation**:
   - Validation happens at application level
   - **Risk**: Race conditions in concurrent submissions
   - **Recommendation**: Add unique constraint on `invoice_no` or use database-level validation

5. **File Storage**:
   - Files stored on local file system
   - **Risk**: Scalability issues, backup complexity, single point of failure
   - **Recommendation**: Migrate to cloud storage

6. **Master Data Relationships**:
   - Complex hierarchical relationships (Reason → Mode → Class)
   - **Risk**: Cascading deletes/updates may break referential integrity
   - **Recommendation**: Implement soft deletes, add foreign key constraints

#### **Improvement Opportunities**

1. **API Design**:
   - Some endpoints use POST for GET operations (e.g., `totalTravelData`)
   - **Improvement**: Use proper HTTP methods, implement RESTful conventions

2. **Error Handling**:
   - Generic error messages, limited error context
   - **Improvement**: Implement structured error responses with error codes

3. **Validation**:
   - Business validations scattered across service layer
   - **Improvement**: Centralize validation logic, use Bean Validation annotations

4. **Audit Trail**:
   - Limited audit logging for approval actions
   - **Improvement**: Implement comprehensive audit logging for compliance

5. **Testing**:
   - No visible unit tests or integration tests
   - **Improvement**: Add comprehensive test coverage

6. **Code Organization**:
   - Large service classes (2000+ lines in TravelDeskService)
   - **Improvement**: Break down into smaller, focused services

7. **DTO Usage**:
   - Some entities exposed directly in APIs
   - **Improvement**: Use DTOs consistently to prevent data leakage

8. **Pagination**:
   - List endpoints return all records
   - **Improvement**: Implement pagination for better performance

### **Module Dependencies**

- **Employee Module**: For employee information, reporting manager lookup
- **Authentication Module**: For user authentication and authorization
- **Mail Service**: For email notifications
- **Document/Newsletter Module**: For document storage and retrieval
- **Log Service**: For API logging

### **Technology Stack**

- **Frontend**: Angular (TypeScript), Bootstrap, NgBootstrap
- **Backend**: Spring Boot, JPA/Hibernate
- **Database**: MySQL/MariaDB (inferred from native queries)
- **File Storage**: Local file system
- **Email**: SMTP (via MailService)

---

**Analysis Completed**: Comprehensive end-to-end analysis of Travel Desk Requests, Travel-based Reimbursements, and General Reimbursements/Allowances module.


