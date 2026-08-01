# 🚀 BlueAnt CRM ERP

A production-ready Enterprise CRM & ERP platform built with **Java 21**, **Spring Boot**, **Spring Security**, **JWT Authentication**, **MySQL**, and **Maven**.

The system is designed to manage the complete customer lifecycle starting from Lead Management to Meeting Workflow, Client Onboarding, CRM Operations, Reports, and future ERP modules.

---

# 📌 Project Overview

BlueAnt CRM ERP is an enterprise application developed to streamline sales, CRM operations, meeting workflows, customer management, and internal business processes.

The application follows a modular, scalable, and production-ready architecture.

---

# ✨ Features

## Authentication & Security

- JWT Authentication
- Spring Security
- Role Based Access Control (RBAC)
- Refresh Token Support
- Secure REST APIs

---

## Lead Management

- Create Lead
- Update Lead
- Assign Lead
- Search & Filter
- Lead Pipeline
- Lead Details

---

## Meeting Workflow

- Intro Meeting
- 1st Meeting
- 2nd Meeting
- 3rd Meeting
- Dynamic Meeting Sequence
- Meeting Draft
- Active Meetings
- Completed Meetings
- Meeting History
- Meeting Timeline
- Follow-up Workflow
- Meeting Scheduling
- Meeting Outcome Management

---

## Follow-up Management

- Follow-up Required
- Follow-up Queue
- Reschedule
- Complete Follow-up
- Previous Meeting History

---

## Dashboard

- Sales Dashboard
- Meeting Statistics
- Lead Statistics
- Business Analytics

---

# 🛠 Technology Stack

## Backend

- Java 21
- Spring Boot 3
- Spring Security
- Spring Data JPA
- Hibernate
- JWT
- Maven

## Database

- MySQL

## Build Tool

- Maven

---

# 📂 Project Structure

```
src
 ├── main
 │   ├── java
 │   ├── resources
 │
 ├── test
```

---

# ⚙️ Getting Started

## Clone Repository

```bash
git clone https://github.com/Blueantfinserv/blueant-crm-erp.git
```

## Move into project

```bash
cd blueant-crm-erp
```

## Configure Database

Update

```
src/main/resources/application.properties
```

Example

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/blueant_crm
spring.datasource.username=root
spring.datasource.password=your_password
```

---

## Build Project

```bash
mvn clean install
```

---

## Run Project

```bash
mvn spring-boot:run
```

Application will start at

```
http://localhost:8080
```

---

# 📦 API Documentation

Swagger UI

```
http://localhost:8080/swagger-ui/index.html
```

---

# 📈 Workflow

```
Lead Created
      │
      ▼
Update Meeting
      │
      ▼
Intro Meeting
      │
      ▼
1st Meeting
      │
      ▼
2nd Meeting
      │
      ▼
3rd Meeting
      │
      ▼
Client Conversion
      │
      ▼
CRM
```

---

# 📋 Current Modules

- Authentication
- User Management
- Role Management
- Lead Management
- Meeting Management
- Follow-up Management
- Dashboard

---

# 🚧 Upcoming Modules

- Client Management
- CRM
- HR
- Reports
- Analytics
- Campaign
- Insurance
- Shares
- Loans
- Notification Center
- Helpdesk

---

# 👨💻 Developed By

**BlueAnt Finserv**

---

# 📄 License

This project is proprietary software developed for BlueAnt Finserv.

All Rights Reserved.