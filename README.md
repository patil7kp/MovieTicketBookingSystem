# 🎬 Movie Ticket Booking System

A Spring Boot–based backend project designed to handle **movie management**, **ticket booking**, and **promotion eligibility** with **role-based access control (RBAC)** and **safe concurrent seat allocation**.

---

## 📘 Overview

This system allows:

* **Admins** to manage movies, shows, and view bookings.
* **Customers** to browse shows, book tickets, and avail promotions.

It ensures that:

* No seat is overbooked under high concurrency.
* Promotions are validated correctly.
* Role permissions are strictly enforced.

---

## 🧠 Problem Summary

The project addresses the following business requirements:

| Area             | Description                                                                                                           |
| ---------------- | --------------------------------------------------------------------------------------------------------------------- |
| **User Roles**   | Admin (manage movies/shows/bookings) and Customer (search and book shows).                                            |
| **Seat Booking** | Supports single and multiple seat bookings with real-time seat availability checks.                                   |
| **Concurrency**  | Prevents the same seat from being booked twice via database locking and transactional consistency.                    |
| **Promotions**   | Customers with >5 bookings or total spend >1500 become eligible for special promo codes (free seat or ₹250 discount). |
| **RBAC**         | Implemented using Spring Security and JWT to separate admin and customer privileges.                                  |

---

## ⚙️ System Architecture

### 1️⃣ Layers

* **Controller Layer** — Handles REST endpoints for user, admin, and booking operations.
* **Service Layer** — Contains business logic (validation, concurrency safety, promo handling).
* **Repository Layer** — Uses Spring Data JPA for CRUD operations and custom queries.
* **Security Layer** — Implements JWT-based authentication and role-based access.

### 2️⃣ Flow Diagram (Conceptual)

```
Customer/Admin → Controller → Service → Repository → Database
                          ↑
                      Security (JWT)
```

### 3️⃣ Concurrency Management

* **Transactional methods** ensure atomic operations.
* **Pessimistic Locking** (via `@Lock(LockModeType.PESSIMISTIC_WRITE)`) used in seat repository.
* **Version field** (Optimistic Lock) can also be implemented as an alternative for distributed scaling.

---

## 🗄️ Database Design

### Core Tables

| Table             | Key Columns                                                  | Description                                    |
| ----------------- | ------------------------------------------------------------ | ---------------------------------------------- |
| **users**         | id, name, email, password, role                              | Stores user information                        |
| **movies**        | id, title, description, duration                             | Movie details                                  |
| **shows**         | id, movie_id, show_time, total_seats, available_seats, price | Represents show timings                        |
| **seats**         | id, show_id, seat_number, status                             | Tracks seat availability                       |
| **bookings**      | id, user_id, show_id, total_amount, booking_date             | Customer bookings                              |
| **booking_seats** | booking_id, seat_id                                          | Many-to-many mapping between booking and seats |
| **promo_codes**   | id, code, type, expiry_date                                  | Defines available promo codes                  |

---

## 💡 Key Features

### 🎥 Movie & Show Management

* Admins can add, update, and remove movies or shows.
* Each show has defined timing, total seats, and ticket price.

### 🪑 Seat Allocation

* Booking fails if requested seats are unavailable.
* Seats transition between `AVAILABLE → BOOKED → CANCELLED`.

### 🎁 Promotions

* Promo code logic checks both **booking count** and **spending threshold**.
* Two reward types supported:

  * **FREE_SEAT** — one seat free
  * **FLAT250** — ₹250 discount

### 🔐 Security

* JWT tokens generated on login.
* Admin and Customer routes protected separately using roles.

### 🧾 Error Handling

* Standardized API error responses (e.g., seat unavailable, invalid promo, expired promo).
* Custom exceptions and global exception handler with Spring `@RestControllerAdvice`.

---

## 🛠️ Technology Stack

| Layer          | Technology               |
| -------------- | ------------------------ |
| **Language**   | Java 17                  |
| **Framework**  | Spring Boot 3.x          |
| **Database**   | MySQL                    |
| **ORM**        | JPA / Hibernate          |
| **Security**   | Spring Security with JWT |
| **Build Tool** | Maven                    |

---

## 🧩 Design Decisions

| Concern              | Decision                             | Reason                                              |
| -------------------- | ------------------------------------ | --------------------------------------------------- |
| **Seat Concurrency** | Pessimistic Locking                  | Ensures no double-booking under concurrent requests |
| **Promo Validation** | Service Layer Validation + DB lookup | Ensures business rules are enforced consistently    |
| **Error Handling**   | Centralized Exception Handling       | Improves consistency of responses                   |
| **Scalability**      | Layered architecture                 | Easy to extend to distributed services              |
| **Security**         | JWT + Role-based routes              | Lightweight and stateless authentication            |

---

## 🔄 Booking Workflow

1. Customer requests booking with selected seat IDs.
2. Backend validates:

   * Seat availability
   * Show timing validity
   * Promo eligibility (if provided)
3. Seats are locked and transaction begins.
4. Booking is saved and payment simulated.
5. Seats marked as **BOOKED**.
6. Confirmation returned to the customer.

---

## 🧰 Setup Instructions

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/yourusername/movie-booking-system.git
cd movie-booking-system
```

### 2️⃣ Configure Database

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/movie_booking
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
server.port=9091
```

### 3️⃣ Build and Run

```bash
mvn clean install
mvn spring-boot:run
```

---

## 👨‍💻 Author

**Kiran Patil**
💼 Java | Spring Boot | REST APIs | Microservices
📧 [kiranpatil7svp@gmail.com](mailto:kiranpatil7svp@gmail.com)
