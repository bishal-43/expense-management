# SOLID Principles & Design Patterns in VentureSpend

This document outlines how the **VentureSpend** architecture applies key software design principles (**SOLID**) and classic **Design Patterns** to ensure clean, maintainable, and testable code.

---

## 📐 SOLID Principles

### 1. Single Responsibility Principle (SRP)
*Every class should have one, and only one, reason to change.*
* **Service/Repository Decoupling**: Business logic is separated from database operations. For instance, [TripServiceImpl](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/service/Impl/TripServiceImpl.java) focuses purely on workflows and rules, delegating persistence queries to [TripRepository](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/repository/TripRepository.java).
* **Storage Isolation**: [ReceiptStorageServiceImpl](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/service/Impl/ReceiptStorageServiceImpl.java) is solely responsible for saving and deleting files on the physical storage device, keeping other service components free of file-handling logic.
* **Separation of Concerns**: DTOs like [RegisterRequest](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/dto/auth/RegisterRequest.java) carry data across the API network boundary, leaving domain entities like [User](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/entity/User.java) solely in charge of database schema mappings.

### 2. Open/Closed Principle (OCP)
*Software entities should be open for extension, but closed for modification.*
* **OCR System Strategy**: The OCR component is defined by the [OcrService](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/service/OcrService.java) interface. The application currently implements [MockOcrServiceImpl](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/service/Impl/MockOcrServiceImpl.java). If we decide to upgrade to a cloud service (e.g., AWS Textract or Azure Document Intelligence), we can add a new class implementing `OcrService` and switch the bean implementation without modifying the existing controller or service clients.

### 3. Liskov Substitution Principle (LSP)
*Subtypes must be substitutable for their base types.*
* **Spring Security Principal**: [UserPrincipal](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/security/UserPrincipal.java) implements Spring Security's standard `UserDetails` interface. This allows Spring Security to handle authentication, authorization, and session tracking uniformly, substituting the custom `UserPrincipal` implementation wherever a `UserDetails` type is required.
* **Repository Architecture**: Repositories extend `JpaRepository`, inheriting standardized database operations (save, find, delete) that satisfy LSP constraints during runtime polymorphism.

### 4. Interface Segregation Principle (ISP)
*Clients should not be forced to depend on methods they do not use.*
* **Domain Specificity**: Instead of having a single massive service interface, the application separates APIs into distinct interfaces like [ExpenseService](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/service/ExpenseService.java), [TripService](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/service/TripService.java), [NotificationService](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/service/NotificationService.java), and [DashboardService](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/service/DashboardService.java). Clients consuming these services only import and depend on the APIs relevant to their specific module.

### 5. Dependency Inversion Principle (DIP)
*Depend on abstractions, not on concretions.*
* **Dependency Injection**: Controllers depend directly on service interfaces (e.g., [TripController](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/controller/TripController.java) consumes the `TripService` interface).
* **IoC Containers**: Spring automatically injects the appropriate concrete implementation class (e.g. `TripServiceImpl`) at runtime. This allows mock services to be swapped in easily during testing.

---

## 🎨 Design Patterns

### 1. Builder Pattern (Creational)
*Separates the construction of a complex object from its representation.*
* **Lombok Builder**: Entities like [Expense](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/entity/Expense.java), [Trip](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/entity/Trip.java), and [User](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/entity/User.java) use the `@Builder` annotation. This enables step-by-step object assembly and makes tests and domain transformations highly readable:
  ```java
  Notification notification = Notification.builder()
          .user(user)
          .message(message)
          .isRead(false)
          .build();
  ```

### 2. Strategy Pattern (Behavioral)
*Defines a family of algorithms, encapsulates each one, and makes them interchangeable.*
* **Receipt Data Extraction**: The [OcrService](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/service/OcrService.java) acts as the strategy context interface. Depending on the active environment, different extraction algorithms (e.g., local mock parsing, cloud integrations, or neural models) can be supplied dynamically.

### 3. Chain of Responsibility Pattern (Behavioral)
*Passes request along a chain of handlers.*
* **Security Filter Chain**: Spring Security's filter chain (defined in [SecurityConfig](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/config/SecurityConfig.java)) represents a Chain of Responsibility. Each filter (like [JwtAuthenticationFilter](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/security/JwtAuthenticationFilter.java)) evaluates one security concern (e.g. token extraction, validation, authorization context setup) before optionally passing the request context to the next handler in line.

### 4. Repository Pattern (Data Access)
*Mediates between the domain and data mapping layers using a collection-like interface.*
* **JPA Repositories**: Interfaces like [ExpenseRepository](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/repository/ExpenseRepository.java) isolate business services from underlying SQL and transaction logic.

### 5. DTO & Factory Conversion Pattern (Structural)
*Transfer data between processes and encapsulate conversions.*
* **Entities to Responses**: Classes like [ExpenseResponse](file:///c:/Users/Admin/Desktop/expense-management/src/main/java/com/travel/expense_management/dto/expense/ExpenseResponse.java) provide a static `.from()` builder method. This converts database records to network transfer objects, preventing serialization leaks:
  ```java
  public static ExpenseResponse from(Expense expense) {
      return new ExpenseResponse(
          expense.getId(),
          expense.getDescription(),
          expense.getAmount(),
          expense.getCategory(),
          expense.getDate(),
          expense.isPolicyViolated(),
          expense.getPolicyViolationMessage(),
          expense.getReceipt() != null ? expense.getReceipt().getFileName() : null
      );
  }
  ```
