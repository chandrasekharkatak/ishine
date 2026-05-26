# Travel Desk User Manual

**Document Type:** Business User Manual  
**Module:** Travel Desk  
**Audience:** Employee, Approver, Travel Admin, Configuration Admin  
**Scope:** Current implemented application behavior

This document is a business user manual for the Travel Desk module as it is currently implemented in the application. It covers the full flow from configuration through request creation, approval, Travel Admin booking, request tracking, and analytics.

## 1. Purpose

Travel Desk is used to manage employee travel and hotel booking requests inside iShine. The module supports:

- Travel master configuration
- Approval matrix setup
- Employee ticket creation
- Manager / approver review
- Travel Admin booking and proof upload
- Employee request tracking and history
- Travel spend analytics

## 2. Main roles

The Travel Desk flow usually involves these roles:

- `Configuration Admin`: maintains travel masters and approval matrix
- `Employee`: raises travel and hotel requests
- `Approver`: approves or rejects requests based on the matrix
- `Travel Admin`: books approved requests, enters booking reference, amount, and uploads proof

## 3. Navigation

### Configuration side

Go to:

- `Configuration -> Travel`

This screen contains:

- `Travel-Reason`
- `Travel Mode`
- `Travel Classes`
- `Hotel-Booking Category`
- `Hotel-Booking SubCategory`
- `Add City`
- `Approval Matrix`

### Travel Desk side

Go to:

- `Travel Desk -> Apply Travel Request`
- `Travel Desk -> View Travel Request`
- `Travel Desk -> Approve Travel Request`
- `Travel Desk -> Total Travel Request`

## 4. Travel configuration

Before employees start using Travel Desk, the master data should be configured correctly.

### 4.1 Travel Reason

Use this section to create and maintain the business purpose of travel, for example:

- Client Meeting
- Project Deployment
- Conference / Seminar
- Hotel & Lodging

You can:

- add a travel reason
- edit a travel reason
- delete a travel reason

### 4.2 Travel Mode

Travel mode is linked to a travel reason.

Examples:

- Flight
- Train
- Bus
- Cab / Taxi
- Hotel Booking

You can:

- add a mode
- edit a mode
- delete a mode

### 4.3 Travel Classes

Travel class is linked to the selected travel mode.

Examples:

- Economy
- AC 2 Tier
- AC 3 Tier
- Sedan
- Standard

You can:

- add a class
- edit a class
- delete a class

### 4.4 Hotel Booking Category

Use this to maintain hotel stay categories.

Examples:

- Metro - TL and Below
- Metro - Above TL
- Tier A Cities
- Tier B Cities
- Guest House / Company Facility
- Friends / Relatives Stay

You can:

- add a hotel category
- edit a hotel category
- delete a hotel category

### 4.5 Hotel Booking SubCategory

Each hotel sub-category is mapped under a hotel category.

Examples:

- Single Occupancy
- Twin Sharing
- Deluxe Room
- Self-Booked (Admin Unavailable)

You can:

- add a hotel sub-category
- edit a hotel sub-category
- delete a hotel sub-category

### 4.6 Add City

Cities are maintained against the hotel category and hotel sub-category combinations.

You can:

- add a city
- edit a city
- delete a city

These city masters are used in the employee hotel request screen and also support city suggestions.

### 4.7 Approval Matrix

Use the `Approval Matrix` tab to define who approves Travel Desk tickets.

The matrix supports:

- applicability by department
- applicability by job role
- multiple approval levels
- routing to reporting manager
- routing to HOD
- routing to approver pool
- routing to a named approver
- Travel Admin assignment

If no matrix matches a submitter, the system falls back to:

- `HOD -> Travel Admin`

## 5. Employee process: Apply Travel Request

Go to:

- `Travel Desk -> Apply Travel Request`

This is the main employee entry point for creating a ticket.

### 5.1 Key concept

The screen works as a ticket builder:

- an employee can prepare one or more lines
- each line can be a travel line or a hotel line
- all lines are added into a single draft ticket
- the ticket is submitted once all required lines are ready

This allows a single ticket to include:

- one-way travel
- round-trip travel
- multi-city travel
- hotel booking lines
- a mix of travel and hotel requests

### 5.2 Sections in the apply screen

The apply screen contains:

- `Travel` tab
- `Hotels` tab
- trip planning controls
- ticket draft / `My Trips`
- `Submit Ticket`

### 5.3 Travel request types

On the Travel side, the employee can create:

- `One way`
- `Round trip`
- `Multi-city`

For multi-city:

- minimum 2 legs are required
- maximum 5 legs are supported

### 5.4 Travel request details

For travel requests, the employee typically provides:

- travel reason
- travel mode
- travel class
- from city
- to city
- travel date
- return date if applicable
- project
- client if required
- purpose

### 5.5 Hotel request details

For hotel requests, the employee typically provides:

- hotel category
- hotel sub-category
- city
- check-in date
- check-out date
- project
- client if required
- purpose

### 5.6 KYC document

The current ticket flow supports ticket-level KYC upload.

Important points:

- KYC is uploaded at ticket level
- KYC is required before submission
- file validation applies on size and allowed type

### 5.7 Add to ticket

After entering one line:

- click `Add to ticket`

The line is then added to the draft section (`My Trips`).

The employee can:

- add more travel lines
- add hotel lines
- edit draft lines before final submission
- remove draft lines before final submission

### 5.8 Hotel itinerary linking

If a travel line already exists, the Hotels tab can show the related itinerary summary and reuse relevant trip context, such as:

- latest destination city
- dates
- project
- purpose

This helps the employee create hotel requests connected to the travel route.

### 5.9 City suggestions

The form supports city suggestions for:

- `From`
- `To`
- `Hotel city`
- multi-city leg fields

Users can:

- select from suggestion list
- manually type if needed

### 5.10 Validations

Important validations implemented in the current flow include:

- travel should be planned at least 7 days in advance
- future travel limit is controlled
- return date cannot be before departure date
- hotel check-out cannot be before check-in
- multi-city legs must stay in date order
- mandatory fields must be filled before adding to ticket or submitting
- project is mandatory
- if project is `Others`, project name and client are required
- purpose is mandatory

### 5.11 Submit ticket

Once all required lines are added:

- click `Submit Ticket`

The system creates a ticket number in this format:

- `APM-TRV-YYYYMMDD-####`

The ticket then moves into the approval workflow.

## 6. Approval process

Go to:

- `Travel Desk -> Approve Travel Request`

This screen is used by managers / approvers and also supports the Travel Admin flow in later stage.

### 6.1 Main sections

The page includes:

- `Pending Actions`
- `All Assigned Tickets`

### 6.2 How approvers review a ticket

Approvers open a ticket and review each request line inside the ticket.

The request may include:

- travel lines
- hotel lines
- multiple lines in one ticket

### 6.3 Approver actions

The approver can:

- approve line by line
- reject line by line
- use bulk action such as approve all or reject all
- enter remarks

### 6.4 Approval routing

Based on matrix configuration, a ticket moves through:

- reporting manager
- HOD
- other configured approval levels
- Travel Admin

### 6.5 Approval outcome

Possible outcomes after approver review:

- approved lines move to next level
- rejected lines stop at that level
- if all lines are rejected, the ticket closes as rejected
- if at least one line is approved, the approved lines continue in workflow

## 7. Travel Admin process: booking and fulfillment

Once a ticket reaches Travel Admin stage, the Travel Admin handles actual booking.

This happens from:

- `Travel Desk -> Approve Travel Request`
- or the assigned admin queue within the same ticket flow

### 7.1 Travel Admin tasks

For each pending line, Travel Admin can enter:

- booking reference
- booking amount
- proof document

Examples:

- flight PNR
- hotel confirmation number
- booking voucher
- e-ticket or proof upload

### 7.2 Booking amount

Booking amount is captured line by line.

This amount is later used in:

- ticket summary
- employee history
- approver views
- travel analytics dashboard

### 7.3 Proof documents

Travel Admin uploads proof documents separately for the booked line.

This helps users understand which proof belongs to which request line.

### 7.4 Travel Admin actions

Travel Admin can:

- `Mark as booked`
- reject if booking cannot be completed

### 7.5 Conditions to mark booked

To complete booking properly, each pending admin line should have:

- booking reference
- booking amount greater than zero
- proof document

### 7.6 Booking outcome

After successful admin action:

- the line becomes booked / fulfilled
- once all actionable lines are completed, the ticket closes as booked / completed

## 8. Employee tracking and history

Go to:

- `Travel Desk -> View Travel Request`

This screen helps employees track submitted tickets and older requests.

### 8.1 What employees can view

Employees can see:

- ticket number
- request summary
- travel lines
- hotel lines
- total cost
- status
- approval level
- proof information

### 8.2 Total cost

If multiple lines are booked in one ticket, the system shows the total booked cost by summing the booked line amounts.

### 8.3 Grouped display

For newer ticket-based flow:

- one ticket is shown as one grouped record
- multiple travel or hotel lines are shown together under the same ticket

### 8.4 Booking proof

Booking proofs are shown per line so that the user can identify which proof belongs to which request.

## 9. Total Travel Request page

Go to:

- `Travel Desk -> Total Travel Request`

This page is mainly used as a broader request view and still includes legacy behavior. Depending on role and request type, it can show:

- request details
- approval details
- booking information
- uploaded ticket / proof data

For some admin users, this screen may still show legacy upload behavior for older requests.

## 10. Dashboard and analytics

Travel spend analytics is available inside the reimbursement dashboard.

Go to:

- `Reimbursement -> Dashboard -> Travel Desk Analytics`

This section shows metrics based on booked travel amounts, such as:

- travel tickets
- requests
- booked requests
- total spend
- average spend
- pending travel admin
- monthly spend trend
- project-wise spend
- client-wise spend
- top employees by travel spend

## 11. Common statuses and labels

Common user-visible statuses in the current flow include:

- `Draft`
- `Pending approval`
- `Pending travel admin`
- `Booked`
- `Rejected`
- `Approved`
- `Partial`

Meaning:

- `Draft`: ticket is being prepared and not yet submitted
- `Pending approval`: waiting for one of the configured approvers
- `Pending travel admin`: approval is complete and booking is pending with Travel Admin
- `Booked`: booking is completed and proof is uploaded
- `Rejected`: request or ticket was rejected
- `Partial`: some lines are approved or completed while others are rejected

## 12. Recommended operating sequence

To use the module successfully, follow this order:

1. Configure travel masters.
2. Configure hotel categories, sub-categories, and cities.
3. Configure approval matrix and Travel Admin mapping.
4. Employee creates ticket from `Apply Travel Request`.
5. Approver reviews from `Approve Travel Request`.
6. Travel Admin books from the admin stage in the same ticket flow.
7. Employee tracks status in `View Travel Request`.
8. Admin or management reviews spend in `Travel Desk Analytics`.

## 13. Quick role-wise summary

### Configuration Admin

- maintain travel masters
- maintain hotel masters
- maintain city mapping
- maintain approval matrix

### Employee

- create one or more travel / hotel lines
- upload KYC
- submit ticket
- track status
- view booked cost and proof

### Approver

- review assigned tickets
- approve or reject line items
- provide remarks

### Travel Admin

- receive approved requests
- book travel or hotel
- enter booking reference
- enter amount
- upload booking proof
- complete or reject booking action

## 14. Employee user manual

This section is written for employees who raise travel and hotel requests.

### 14.1 When to use Travel Desk

Use Travel Desk when you need:

- flight booking
- train booking
- bus booking
- cab-related travel request
- hotel booking
- a combination of travel and hotel in one trip

### 14.2 Before raising a request

Make sure:

- your reporting hierarchy is correctly mapped
- your HOD details are available in the system
- your project details are known
- your purpose of travel is clear
- your KYC document is ready

### 14.3 Open the apply screen

Go to:

- `Travel Desk -> Apply Travel Request`

You will see:

- `Travel` tab
- `Hotels` tab
- draft ticket area
- add to ticket action
- submit ticket action

### 14.4 How to raise a travel line

1. Open the `Travel` tab.
2. Choose trip type:
   - `One way`
   - `Round trip`
   - `Multi-city`
3. Select:
   - travel reason
   - travel mode
   - travel class
4. Enter:
   - from city
   - to city
   - travel date
   - return date if applicable
5. Select project.
6. If project is `Others`, also enter project name and client.
7. Enter purpose.
8. Upload or confirm KYC document.
9. Click `Add to ticket`.

### 14.5 How to raise a hotel line

1. Open the `Hotels` tab.
2. Select:
   - hotel category
   - hotel sub-category
   - city
3. Enter:
   - check-in date
   - check-out date
4. Select project.
5. If project is `Others`, enter project name and client.
6. Enter purpose.
7. Click `Add to ticket`.

### 14.6 Multi-city request

For multi-city trips:

- add at least 2 legs
- maximum 5 legs are supported
- keep dates in logical sequence
- each leg is stored as a separate request line under one ticket

### 14.7 Draft ticket behavior

Before submission, the ticket remains in draft state.

You can:

- add multiple travel lines
- add hotel lines
- edit a line
- remove a line
- review all lines under `My Trips`

### 14.8 City suggestion behavior

The form shows city suggestions while typing in:

- from city
- to city
- hotel city

You may:

- choose a suggested city
- manually enter a city if needed

### 14.9 Important validations for employees

The system checks:

- mandatory fields must be filled
- travel should normally be planned in advance
- return date cannot be before departure date
- check-out cannot be before check-in
- multi-city dates must be in order
- KYC must be available before submission

### 14.10 Submit the ticket

After all required lines are added:

1. review the draft lines
2. click `Submit Ticket`

After submission:

- the system generates a ticket number
- the ticket goes to the approval workflow
- you can no longer treat it as a draft

### 14.11 How to track your request

Go to:

- `Travel Desk -> View Travel Request`

You can check:

- ticket number
- status
- current approval stage
- booked amount
- proof documents
- grouped travel and hotel lines

### 14.12 What employees should expect after approval

Once approved:

- the ticket moves to Travel Admin
- Travel Admin books the line
- booking reference is entered
- amount is entered
- proof document is uploaded
- the final booked record becomes visible in history

## 15. Approver user manual

This section is for managers, HODs, and other configured approvers.

### 15.1 Open the approval screen

Go to:

- `Travel Desk -> Approve Travel Request`

Main areas:

- `Pending Actions`
- `All Assigned Tickets`

### 15.2 What an approver sees

An approver receives tickets based on:

- approval matrix
- reporting manager mapping
- HOD mapping
- approver pool configuration
- fallback routing if no matrix exists

Each ticket may contain:

- one or more travel lines
- one or more hotel lines
- mixed request types in one ticket

### 15.3 Review process

1. Open the ticket from the pending list.
2. Review employee details.
3. Review each request line.
4. Check:
   - travel reason
   - route or hotel city
   - dates
   - project and purpose
   - KYC and available documents
5. Decide line by line.

### 15.4 Approval actions

Approver actions include:

- approve line
- reject line
- approve all lines
- reject all lines
- provide remarks

### 15.5 Approval rules

Approvers should verify:

- the request is business-related
- dates are appropriate
- route or hotel city is correct
- project is valid
- cost reasonability aligns with policy or expectation
- remarks are entered where required

### 15.6 What happens after approver action

- approved lines move to the next configured approval level
- if the current level is the last one, the ticket moves to Travel Admin
- rejected lines stop in the workflow
- if all lines are rejected, the ticket closes as rejected

### 15.7 All Assigned Tickets view

This view is useful for:

- checking previously assigned tickets
- tracking decision progress
- reviewing total booked cost where available
- monitoring pending and completed action records

## 16. Travel Admin user manual

This section is for the Travel Admin or booking team.

### 16.1 When Travel Admin gets a ticket

Travel Admin receives a ticket after:

- all required approvals are completed
- the workflow stage becomes pending for admin booking

### 16.2 Open the admin work queue

Go to:

- `Travel Desk -> Approve Travel Request`

Travel Admin uses the assigned ticket view in the same flow to process booking-stage tickets.

### 16.3 What Travel Admin must do

For each pending line, Travel Admin should:

- review the approved request
- book the travel or hotel
- enter booking reference
- enter booking amount
- upload proof document

### 16.4 Booking reference examples

Booking reference may be:

- flight PNR
- train reference
- hotel confirmation number
- booking voucher number

### 16.5 Booking amount entry

Booking amount is mandatory for proper booking completion.

Important points:

- amount is entered per line
- multiple lines can have different booked amounts
- total ticket cost is derived from booked line amounts

### 16.6 Proof upload

Upload the correct proof document for each booked line.

Examples:

- e-ticket PDF
- hotel voucher
- booking confirmation
- ticket screenshot or attached proof file

### 16.7 Mark as booked

Use `Mark as booked` after completing all necessary details.

Before marking booked, ensure:

- booking reference is entered
- booking amount is entered
- proof document is uploaded

### 16.8 Reject booking

If a request cannot be booked, Travel Admin can reject it.

In that case:

- enter clear remarks
- use rejection only when booking cannot be completed or should not proceed

### 16.9 After Travel Admin completion

After successful booking:

- the ticket becomes booked / completed
- employee can see proof and amount in history
- booked amount contributes to analytics and summaries

## 17. Configuration Admin user manual

This section is for users who manage Travel configuration and workflow setup.

### 17.1 Open configuration

Go to:

- `Configuration -> Travel`

### 17.2 Travel reason setup

Use `Travel-Reason` to maintain valid reasons for travel desk requests.

Recommended admin actions:

- create clean and business-appropriate names
- avoid duplicate reasons
- keep descriptions meaningful

### 17.3 Travel mode setup

Use `Travel Mode` to map available mode choices to the selected travel reason.

Recommended checks:

- mode should be relevant to the reason
- avoid duplicate combinations
- keep naming consistent across business usage

### 17.4 Travel class setup

Use `Travel Classes` to define the available booking classes under the selected mode.

Recommended checks:

- class should match policy usage
- class names should remain standardized

### 17.5 Hotel category setup

Use `Hotel-Booking Category` to define the major hotel grouping used in employee selection.

Recommended checks:

- keep categories aligned with business policy
- description can carry policy information or limit hints

### 17.6 Hotel sub-category setup

Use `Hotel-Booking SubCategory` to create room-type or booking-type selections under each hotel category.

Recommended checks:

- map each sub-category to the correct category
- avoid duplicates

### 17.7 City setup

Use `Add City` to assign cities to hotel category and hotel sub-category combinations.

Recommended checks:

- add only valid cities
- maintain consistent spelling
- ensure major business travel cities are covered

### 17.8 Approval matrix setup

Use `Approval Matrix` to define who approves which employees' tickets.

Matrix setup should cover:

- applicability departments
- applicability job roles
- approval level count
- routing type per level
- admin department or admin assignee

### 17.9 Approval routing options

The system supports:

- reporting manager routing
- HOD routing
- approver pool routing
- named approver routing

### 17.10 Best practice for Configuration Admin

Before releasing the module for users:

1. configure all master data
2. configure city mapping
3. configure approval matrix
4. test one employee submission
5. test one approval cycle
6. test one Travel Admin booking cycle
7. confirm the request appears correctly in history and analytics

## 18. Important implementation notes

- The Travel Desk flow supports both travel and hotel requests in one ticket.
- Approval is matrix-driven where configured.
- If no matrix applies, the fallback route is HOD to Travel Admin.
- Total cost is calculated from booked line amounts.
- Travel Desk Analytics is based on booked travel spend.
- This manual reflects the current implemented application behavior.

