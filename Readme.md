# Expense Sharing App

A Spring Boot application for managing groups, expenses, balances, and settlements between users.

## Requirements

- Java 17+ (recommended)
- Maven 3.8+
- (Optional) MySQL / PostgreSQL if you want persistence

## Getting Started

### 1. Clone the repo
```bash
git clone https://github.com/ashwani4646/expense-sharing-app.git
cd expense-sharing-app
```

### 2. Build the project
```bash
mvn clean install
```

### 3. Run the application
```bash
mvn spring-boot:run
```

By default the server will start at:
👉 `http://localhost:8080`

You can customize configs in:
```
src/main/resources/application.properties
```

Example:
```properties
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/expense_db
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
```

---

## API Testing with Postman

### Step 1: Import collection
1. Open Postman
2. Click **Import** → choose API collection file (or manually add endpoints).
   Expense Sharing App API.postman_collection.json
### Step 2: Example endpoints

#### User APIs
- `POST /users` → Create new user
- `GET /users/{id}` → Fetch user by ID

#### Group APIs
- `POST /groups` → Create group
- `POST /groups/{id}/add-user` → Add user to group

#### Expense APIs
- `POST /expenses` → Add expense to group
- `GET /expenses/group/{id}` → Get group expenses

#### Balance APIs
- `GET /balances/group/{id}` → Get balances for a group
- `POST /balances/settle` → Settle balances

Example request (Add expense):
```json
POST http://localhost:8080/expenses
{
  "groupId": 1,
  "description": "Dinner",
  "amount": 1200,
  "paidBy": 2,
  "splitBetween": [1, 2, 3]
}
```

Response:
```json
{
  "expenseId": 5,
  "status": "RECORDED",
  "balancesUpdated": true
}
```

---


