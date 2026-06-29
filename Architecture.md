# SecurePay Wallet API

SecurePay Wallet API adalah aplikasi backend berbasis Spring Boot yang mensimulasikan sistem e-wallet sederhana dengan fitur autentikasi, wallet management, transfer saldo, audit trail, dan keamanan transaksi.

Tujuan utama project ini adalah:

- Belajar Spring Boot secara mendalam
- Memahami desain aplikasi finansial
- Membangun portfolio backend yang mendekati standar production
- Mempelajari best practice JPA, Security, Transaction Management, dan Audit Logging

---

# Architecture Overview

```text
Client (Mobile/Web)
        |
        v
+------------------+
|   REST API       |
|  Spring Boot     |
+------------------+
        |
        v
+------------------+
| Service Layer    |
| Business Logic   |
+------------------+
        |
        v
+------------------+
| Repository Layer |
| Spring Data JPA  |
+------------------+
        |
        v
+------------------+
| PostgreSQL       |
+------------------+
```

Additional Components:

```text
JWT Authentication
Refresh Token
Flyway Migration
Swagger/OpenAPI
Audit Logging
Global Exception Handler
Validation
Scheduled Jobs
```

---

# Technology Stack

## Backend

- Java 21
- Spring Boot 3
- Spring Security
- Spring Data JPA
- Hibernate

## Database

- PostgreSQL

## Authentication

- JWT
- Refresh Token

## Documentation

- Swagger/OpenAPI

## Migration

- Flyway

## Testing

- JUnit 5
- Mockito
- Testcontainers

## Build Tool

- Maven

---

# Project Structure

Feature-based architecture digunakan agar project mudah berkembang.

```text
src/main/java/com/securepay

├── common
│   ├── config
│   ├── exception
│   ├── security
│   ├── constants
│   ├── util
│   ├── validation
│   └── audit
│
├── auth
│   ├── controller
│   ├── service
│   ├── repository
│   ├── dto
│   └── entity
│
├── user
│   ├── controller
│   ├── service
│   ├── repository
│   ├── dto
│   └── entity
│
├── wallet
│   ├── controller
│   ├── service
│   ├── repository
│   ├── dto
│   └── entity
│
├── transaction
│   ├── controller
│   ├── service
│   ├── repository
│   ├── dto
│   └── entity
│
├── notification
│   ├── service
│   └── dto
│
└── SecurePayApplication.java
```

---

# Domain Features

## Authentication

- Register
- Login
- Refresh Token
- Change Password
- Logout

## User

- View Profile
- Update Profile

## Wallet

- Wallet Creation
- Wallet Information
- Check Balance

## Transaction

- Top Up
- Transfer
- Transaction History
- Transaction Detail

## Audit

- Login History
- Balance Mutation History
- Transaction Log

## Admin

- Freeze Wallet
- Unfreeze Wallet
- View Users

---

# Transaction Types

```java
public enum TransactionType {
    TOPUP,
    TRANSFER,
    WITHDRAW,
    PAYMENT
}
```

---

# Transaction Status

```java
public enum TransactionStatus {
    PENDING,
    SUCCESS,
    FAILED,
    CANCELLED
}
```

---

# Wallet Status

```java
public enum WalletStatus {
    ACTIVE,
    BLOCKED
}
```

---

# Database Design

## users

```sql
id UUID PRIMARY KEY
email VARCHAR(255) UNIQUE NOT NULL
phone_number VARCHAR(20) UNIQUE
password VARCHAR(255) NOT NULL
role VARCHAR(50)
created_at TIMESTAMP
updated_at TIMESTAMP
```

---

## wallets

```sql
id UUID PRIMARY KEY
user_id UUID UNIQUE NOT NULL
balance NUMERIC(19,2)
status VARCHAR(50)

created_at TIMESTAMP
updated_at TIMESTAMP
```

---

## transactions

```sql
id UUID PRIMARY KEY

sender_wallet_id UUID
receiver_wallet_id UUID

reference_number VARCHAR(50)

type VARCHAR(50)
status VARCHAR(50)

amount NUMERIC(19,2)
fee NUMERIC(19,2)

description TEXT

created_at TIMESTAMP
updated_at TIMESTAMP
```

---

## wallet_mutations

```sql
id UUID PRIMARY KEY

wallet_id UUID
transaction_id UUID

balance_before NUMERIC(19,2)
balance_after NUMERIC(19,2)

amount NUMERIC(19,2)

created_at TIMESTAMP
```

---

# Entity Relationship

```text
User
 |
 | 1 : 1
 |
Wallet
 |
 | 1 : N
 |
WalletMutation

Transaction
 |
 | N : 1
 |
Sender Wallet

Transaction
 |
 | N : 1
 |
Receiver Wallet
```

---

# Base Entity

Seluruh entity akan mewarisi field audit.

```java
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}
```

Aktifkan auditing:

```java
@EnableJpaAuditing
@SpringBootApplication
public class SecurePayApplication {
}
```

---

# ID Strategy

Gunakan UUID untuk seluruh entity.

```java
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;
```

Alasan:

- Tidak mudah ditebak
- Aman untuk API publik
- Cocok untuk sistem finansial
- Cocok untuk microservice

---

# Money Handling

Jangan gunakan:

```java
double
float
```

Gunakan:

```java
BigDecimal
```

Contoh:

```java
private BigDecimal balance;
```

Karena tidak mengalami floating point precision issue.

---

# API Endpoints

## Authentication

```http
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh-token
POST /api/auth/logout
```

---

## User

```http
GET /api/users/me
PUT /api/users/me
```

---

## Wallet

```http
GET /api/wallets/me
GET /api/wallets/me/balance
```

---

## Transaction

```http
POST /api/transactions/topup

POST /api/transactions/transfer

GET /api/transactions

GET /api/transactions/{id}
```

---

# Transfer Flow

```text
Transfer Request
        |
        v
Validate User
        |
        v
Validate Wallet
        |
        v
Validate Balance
        |
        v
Create Transaction
        |
        v
Deduct Sender Balance
        |
        v
Add Receiver Balance
        |
        v
Create Mutation Log
        |
        v
Commit Transaction
```

Gunakan:

```java
@Transactional
public void transfer(...) {
}
```

agar seluruh proses bersifat atomic.

---

# Validation Rules

## Register

- Email wajib unik
- Password minimal 8 karakter
- Nomor telepon wajib unik

## Transfer

- Wallet aktif
- Saldo mencukupi
- Nominal > 0
- Tidak boleh transfer ke diri sendiri

## Top Up

- Nominal > 0
- Tidak melebihi limit harian

---

# Global Exception Handling

Gunakan:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
}
```

Exception yang perlu dibuat:

```text
UserNotFoundException
WalletNotFoundException
InsufficientBalanceException
WalletBlockedException
UnauthorizedException
```

---

# Logging

Gunakan SLF4J.

```java
private static final Logger log =
        LoggerFactory.getLogger(TransactionService.class);
```

Contoh:

```java
log.info("Transfer success. Reference: {}", referenceNumber);
```

---

# Scheduled Jobs

Expire transaksi pending.

```java
@Scheduled(cron = "0 */5 * * * *")
public void expirePendingTransaction() {
}
```

Flow:

```text
PENDING > 15 menit
       ↓
EXPIRED
```

---

# Future Enhancements

## Notification

- Email Notification
- Push Notification

## Fraud Detection

```text
> 10 transaksi dalam 1 menit
=> suspicious
```

## Wallet Freeze

```text
Admin Freeze Wallet
Admin Unfreeze Wallet
```

## Redis

Untuk:

- Refresh Token
- Session Blacklist
- Cache

---

# Development Roadmap

## Phase 1

- Register
- Login
- JWT
- User Profile

## Phase 2

- Wallet Creation
- Balance Inquiry
- Top Up

## Phase 3

- Transfer
- Transaction History
- Wallet Mutation

## Phase 4

- Refresh Token
- Audit Trail
- Global Exception Handling

## Phase 5

- Scheduled Jobs
- Notification
- Fraud Detection

---

# Production-Level Concepts Covered

- Spring Security
- JWT Authentication
- Refresh Token
- JPA Auditing
- Entity Relationship
- Transaction Management
- Flyway Migration
- Validation
- Exception Handling
- Audit Trail
- Logging
- Scheduled Job
- Clean Architecture Principles