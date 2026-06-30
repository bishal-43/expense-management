# Project File Directory & Documentation

This document explains the architecture of the **Travel Expense Management** application and outlines what each file in the workspace does.

---

## 🏗️ Project Architecture Overview

The project is structured as a single-page web application featuring:
1. **Spring Boot (Java) Backend**: REST API that manages users, security, trips, expenses, notifications, dashboard metrics, receipt storage, policy verification, and OCR scanning.
2. **Vanilla JS Frontend**: Clean, modular UI utilizing standard HTML, CSS, and JS components. Communication with the backend is handled asynchronously using `fetch`.

---

## 🛠️ Infrastructure & Configuration Files

| File | Path | Description |
|---|---|---|
| [pom.xml](file:///c:/Users/Admin/Desktop/expense-management/pom.xml) | `/pom.xml` | Maven project build configuration defining Java 17, Spring Boot dependencies, H2/PostgreSQL database connectors, Spring Security, JWT validation libraries, Lombok, and OCR parsing. |
| [Dockerfile](file:///c:/Users/Admin/Desktop/expense-management/Dockerfile) | `/Dockerfile` | Docker definition to compile and package the Spring Boot JAR file, configuring a lightweight container environment with required system tools (e.g., Tesseract OCR engine dependencies). |
| [docker-compose.yml](file:///c:/Users/Admin/Desktop/expense-management/docker-compose.yml) | `/docker-compose.yml` | Multi-container configuration to spin up a PostgreSQL database server alongside the application container. |
| [.dockerignore](file:///c:/Users/Admin/Desktop/expense-management/.dockerignore) | `/.dockerignore` | Tells Docker which files/folders (such as local build targets or IDE settings) to omit during build packaging. |
| [.gitignore](file:///c:/Users/Admin/Desktop/expense-management/.gitignore) | `/.gitignore` | Directs Git to ignore transient files (compiled classes, local application uploads, log files, IDE configs). |
| [.gitattributes](file:///c:/Users/Admin/Desktop/expense-management/.gitattributes) | `/.gitattributes` | Configures line endings normalization and repository settings for Git. |
| [HELP.md](file:///c:/Users/Admin/Desktop/expense-management/HELP.md) | `/HELP.md` | Auto-generated Spring Boot reference instructions. |
| [mvnw](file:///c:/Users/Admin/Desktop/expense-management/mvnw) | `/mvnw` | Maven Wrapper Unix shell script to run builds without pre-installing Maven. |
| [mvnw.cmd](file:///c:/Users/Admin/Desktop/expense-management/mvnw.cmd) | `/mvnw.cmd` | Maven Wrapper Windows batch script. |

---

## ☕ Backend Source Code (`src/main/java`)

The backend codebase is organized under the root package `com.travel.expense_management`.

### 🚀 Application Entry Point
* [ExpenseManagementApplication.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/ExpenseManagementApplication.java)
  Initializes the Spring Boot application context and launches the web server.

### ⚙️ Core Configuration (`config`)
* [DatabaseInitializer.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/config/DatabaseInitializer.java)
  Starts up automatically to seed initial user roles (`Employee`, `Manager`, `Admin`) and setups pre-configured credentials for quick local testing.
* [OpenApiConfig.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/config/OpenApiConfig.java)
  Configures Swagger OpenAPI definitions, adding visual endpoint specifications for testing routes through `/swagger-ui/index.html`.
* [PasswordConfig.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/config/PasswordConfig.java)
  Registers the `BCryptPasswordEncoder` bean used across service layers to cryptographically hash user credentials.
* [SecurityConfig.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/config/SecurityConfig.java)
  Defines web request security filter chains, CORS parameters, stateless session configuration, and routes which require authenticated access vs public visibility.

### 📡 REST Controllers (`controller`)
* [AuthController.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/controller/AuthController.java)
  Handles incoming authentication endpoints: registrations and logins, returning signed JWT keys to successful clients.
* [DashboardController.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/controller/DashboardController.java)
  Exposes analytics APIs for total spent metrics, budget utilization, and provides a CSV download endpoint for reports generation.
* [ExpenseController.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/controller/ExpenseController.java)
  Acts as the REST endpoint for creating, editing, and deleting individual expense items linked to trips, as well as uploading receipts.
* [NotificationController.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/controller/NotificationController.java)
  Exposes endpoints allowing users to query their personal notifications panel and mark events as read.
* [TripController.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/controller/TripController.java)
  Coordinates request requests, manager reviews (approving/rejecting), and listing travel claims.
* [FaviconController.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/controller/FaviconController.java)
  Prevents browser console warnings by responding with an empty payload for `/favicon.ico` requests.
* [HealthController.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/controller/HealthController.java)
  Provides a basic `/api/health` validation route indicating service status.

### 📦 Data Transfer Objects (`dto`)
DTOs act as payloads serialized to and from API endpoints:
* **Authentication**:
  * [AuthResponse.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/dto/auth/AuthResponse.java): Response carrying JWT token, token type, and User detail summaries.
  * [LoginRequest.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/dto/auth/LoginRequest.java): Payload storing email and password parameters.
  * [RegisterRequest.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/dto/auth/RegisterRequest.java): Parameters defining registration fields (name, email, password, role).
* **Dashboard Analytics**:
  * [DashboardMetrics.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/dto/dashboard/DashboardMetrics.java): Statistics including spent sums, active budgets, utilization percentages, and counters.
  * [ReportsSummary.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/dto/dashboard/ReportsSummary.java): Object mapping category-specific sums and trip-specific aggregates.
* **Expenses**:
  * [ExpenseRequest.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/dto/expense/ExpenseRequest.java): Properties for creating/editing a claim (amount, date, description, category).
  * [ExpenseResponse.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/dto/expense/ExpenseResponse.java): Returns detailed expense records including receipt URLs and automated compliance flag markers.
  * [OcrResult.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/dto/expense/OcrResult.java): Extracted attributes (amount, date, category, desc) generated during OCR receipts extraction.
* **Notifications**:
  * [NotificationResponse.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/dto/notification/NotificationResponse.java): Schema representing notification alert objects.
* **Trips**:
  * [TripRequest.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/dto/trip/TripRequest.java): Duration dates, budget, and destination properties for setting up a trip.
  * [TripResponse.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/dto/trip/TripResponse.java): Details of created trips, including computed status labels.

### 🗄️ Database Entities (`entity`)
* [BaseEntity.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/entity/BaseEntity.java)
  Mapped superclass storing shared auditable dates (`createdAt` and `updatedAt`) automatically tracked via Spring JPA auditors.
* [User.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/entity/User.java)
  Represents registered users, storing hashed password tokens, emails, full names, and roles.
* [Role.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/entity/Role.java)
  Security enum establishing authorization limits: `ROLE_Employee`, `ROLE_Manager`, `ROLE_Admin`.
* [Trip.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/entity/Trip.java)
  Maps travel plans, including start/end schedules, description, budget limitations, approval states, and the user reference.
* [TripStatus.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/entity/TripStatus.java)
  Enum representing trip status workflow: `PENDING`, `APPROVED`, or `REJECTED`.
* [Expense.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/entity/Expense.java)
  Stores individual expense claims, their classifications, costs, date, links to receipts, and compliance policy violation flags.
* [ExpenseCategory.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/entity/ExpenseCategory.java)
  Enum mapping items to `FOOD`, `LODGING`, `TRANSPORT`, or `OTHER`.
* [Receipt.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/entity/Receipt.java)
  Stores file metadata (path locations, MIME type headers, dimensions/size) for uploads associated with expenses.
* [Notification.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/entity/Notification.java)
  Represents user-specific notifications triggered by state changes.

### 🚨 Exception Handlers (`exception`)
* [GlobalExceptionHandler.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/exception/GlobalExceptionHandler.java)
  Catches application-wide exceptions and formats them into standardized JSON error responses.
* [ErrorResponse.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/exception/ErrorResponse.java)
  Standard error response payload format containing timestamps, status codes, and error details.
* **Custom Exceptions**:
  * [ResourceNotFoundException.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/exception/ResourceNotFoundException.java): Triggered when requesting missing entities.
  * [BadRequestException.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/exception/BadRequestException.java): Triggered by client validation or constraints errors.
  * [DuplicateEmailException.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/exception/DuplicateEmailException.java): Rejects registrations attempting to use an existing email.
  * [InvalidCredentialsException.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/exception/InvalidCredentialsException.java): Rejects incorrect passwords/logins.

### 📂 Spring Repositories (`repository`)
* [UserRepository.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/repository/UserRepository.java)
  JPA repository managing user lookups.
* [TripRepository.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/repository/TripRepository.java)
  JPA repository querying trip records by status or user.
* [ExpenseRepository.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/repository/ExpenseRepository.java)
  JPA repository listing expenses for individual trips.
* [NotificationRepository.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/repository/NotificationRepository.java)
  JPA repository retrieving notification history sorted by ID.

### 🛡️ Spring Security Filter Layer (`security`)
* [CustomUserDetailsService.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/security/CustomUserDetailsService.java)
  Implements Spring Security's `UserDetailsService` to fetch and verify credentials from the database.
* [UserPrincipal.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/security/UserPrincipal.java)
  Wraps the database User entity in a format compatible with Spring Security context storage.
* [JwtService.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/security/JwtService.java)
  Decodes, creates, and verifies JWT tokens, setting validity limits.
* [JwtAuthenticationFilter.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/security/JwtAuthenticationFilter.java)
  A servlet filter intercepting incoming requests to extract and validate the JWT Bearer token in the `Authorization` header.

### ⚙️ Business Service Layer (`service`)
Declares APIs and implements logical checks:
* **Authentication**:
  * [AuthService.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/service/AuthService.java) & [AuthServiceImpl.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/service/Impl/AuthServiceImpl.java): Performs logic for logins/registers, verifying constraints and hashing credentials.
* **Access Control**:
  * [AuthorizationService.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/service/AuthorizationService.java) & [AuthorizationServiceImpl.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/service/Impl/AuthorizationServiceImpl.java): Verifies if the request initiator has read/write permissions for a specific trip or resource.
* **Dashboard Analytics**:
  * [DashboardService.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/service/DashboardService.java) & [DashboardServiceImpl.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/service/Impl/DashboardServiceImpl.java): Aggregates user-specific and system-wide spent metrics, categories distribution, and outputs reports in CSV.
* **Expense Claims**:
  * [ExpenseService.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/service/ExpenseService.java) & [ExpenseServiceImpl.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/service/Impl/ExpenseServiceImpl.java): Executes validations (e.g. date must fall within trip boundaries) and triggers corporate compliance policies (e.g. single-meal caps of ₹5,000 for Food, ₹15,000 for Lodging, ₹25,000 for Transport).
* **Notifications Engine**:
  * [NotificationService.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/service/NotificationService.java) & [NotificationServiceImpl.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/service/Impl/NotificationServiceImpl.java): Saves and retrieves notification payloads.
* **OCR Scanning Service**:
  * [OcrService.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/service/OcrService.java) & [MockOcrServiceImpl.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/service/Impl/MockOcrServiceImpl.java): Parses receipt contents (extracting descriptions, amounts, and dates) using OCR heuristics.
* **Receipt Storage**:
  * [ReceiptStorageService.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/service/ReceiptStorageService.java) & [ReceiptStorageServiceImpl.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/service/Impl/ReceiptStorageServiceImpl.java): Manages physical storage directories for receipt image attachments on the filesystem.
* **Trip Planning**:
  * [TripService.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/service/TripService.java) & [TripServiceImpl.java](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/service/Impl/TripServiceImpl.java): Executes trip creation, listing, status overrides (approvals/rejections), and notifies travelers.

---

## 📂 Configuration Resources (`src/main/resources`)

* [application.properties](file:///c:/Users/Admin/Desktop/expense-management/src/main/resources/application.properties)
  Base Spring Boot configuration (server listening ports, H2 console configs, file upload limits, and JWT key parameters).
* [application-local.properties](file:///c:/Users/Admin/Desktop/expense-management/src/main/resources/application-local.properties)
  Local profile overrides (e.g. database connections, storage locations).

---

## 🎨 Frontend Application (`frontend`)

The frontend folder houses static assets compiled by the backend server static endpoints:

* [index.html](file:///c:/Users/Admin/Desktop/expense-management/frontend/index.html)
  Primary Single-Page Application interface layout framework containing sidebars, forms, and lists.
* [styles.css](file:///c:/Users/Admin/Desktop/expense-management/frontend/styles.css)
  Core styling layout configuring modern features (glassmorphism cards, responsive grids, hover feedback, custom scrollbars).

### ⚙️ Frontend Controllers (`frontend/js`)
* [api.js](file:///c:/Users/Admin/Desktop/expense-management/frontend/js/api.js)
  Unified REST API client wrapper using browser `fetch` and inserting headers such as `Authorization: Bearer <token>`.
* [charts.js](file:///c:/Users/Admin/Desktop/expense-management/frontend/js/charts.js)
  Orchestrates Chart.js instances to present spend breakdown details across categories and trips.
* [main.js](file:///c:/Users/Admin/Desktop/expense-management/frontend/js/main.js)
  The frontend bootstrapper. Manages page routes, handles menu events, and manages the view lifecycle.
* [state.js](file:///c:/Users/Admin/Desktop/expense-management/frontend/js/state.js)
  Central frontend state store carrying current user tokens, navigation parameters, and listings.
* [ui.js](file:///c:/Users/Admin/Desktop/expense-management/frontend/js/ui.js)
  Common helper functions (DOM insertions, button locking, form clearing).

### 🧩 UI Components (`frontend/js/components`)
* [Auth.js](file:///c:/Users/Admin/Desktop/expense-management/frontend/js/components/Auth.js)
  Binds login/registration submit handlers to security endpoints and handles page redirects.
* [Expenses.js](file:///c:/Users/Admin/Desktop/expense-management/frontend/js/components/Expenses.js)
  Renders the trip expenses view, manages adding/removing items, receipt uploads, OCR autofills, and displays corporate policy violation alerts.
* [Metrics.js](file:///c:/Users/Admin/Desktop/expense-management/frontend/js/components/Metrics.js)
  Triggers analytics dashboard updates to draw charts indicating spent ratios vs total budgets.
* [Navigation.js](file:///c:/Users/Admin/Desktop/expense-management/frontend/js/components/Navigation.js)
  Draws responsive navigation headers, updating visual highlights as tabs change.
* [Notifications.js](file:///c:/Users/Admin/Desktop/expense-management/frontend/js/components/Notifications.js)
  Manages the unread badge, fetches notifications, and provides options to mark notification items as read.
* [Toast.js](file:///c:/Users/Admin/Desktop/expense-management/frontend/js/components/Toast.js)
  Displays dynamic UI pop-up notifications (success, error, warning toasts).
* [Trips.js](file:///c:/Users/Admin/Desktop/expense-management/frontend/js/components/Trips.js)
  Renders trip requests list, creation modals, budget limits, and actions for managers to approve/reject pending trips.

---

## 🧪 Integration & Unit Tests (`src/test/java`)

* [ExpenseManagementApplicationTests.java](file:///c:/Users/Admin/Desktop/expense-management/src/test/java/com/travel/expense_management/ExpenseManagementApplicationTests.java)
  Asserts that the Spring Boot container loads successfully.
* [AuthControllerIntegrationTest.java](file:///c:/Users/Admin/Desktop/expense-management/src/test/java/com/travel/expense_management/AuthControllerIntegrationTest.java)
  Simulates client login and registration REST calls to ensure proper user onboarding and validation constraints.
* [DashboardAndNotificationIntegrationTest.java](file:///c:/Users/Admin/Desktop/expense-management/src/test/java/com/travel/expense_management/DashboardAndNotificationIntegrationTest.java)
  Validates dashboard calculation summaries and verifies notification creation.
* [PolicyAndReimbursementIntegrationTest.java](file:///c:/Users/Admin/Desktop/expense-management/src/test/java/com/travel/expense_management/PolicyAndReimbursementIntegrationTest.java)
  Ensures business rules (food, lodging, transport caps and budget overflows) flag policy violations.
* [ReceiptAndOcrIntegrationTest.java](file:///c:/Users/Admin/Desktop/expense-management/src/test/java/com/travel/expense_management/ReceiptAndOcrIntegrationTest.java)
  Validates receipt uploads, local storage persistence, and OCR content analysis.
* [TripAndExpenseIntegrationTest.java](file:///c:/Users/Admin/Desktop/expense-management/src/test/java/com/travel/expense_management/TripAndExpenseIntegrationTest.java)
  Verifies end-to-end trip request creation and approval states.
