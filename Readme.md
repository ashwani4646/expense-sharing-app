Group Expense Splitter API Documentation (cURL) 💵
This document outlines the usage of the key API endpoints for managing users, groups, expenses, and balances using cURL commands.

1. User Management
   1.1 Create User
   Creates a new user account.

Endpoint	Method	Description
/users	POST	Creates a new user.

Export to Sheets
cURL Command:

Bash

curl --location 'http://localhost:8080/users' \
--header 'Content-Type: application/json' \
--data-raw '{
"firstName" : "Ajay",
"lastName" : "Kumar",
"userName" : "Ajay2",
"emailId" : "aajay@test.com",
"password" : "dfsdjfhsjfhjk",
"role" : "USER"
}'
2. Group Management
   2.1 Create Group
   Creates a new expense group.

Endpoint	Method	Description
/groups	POST	Creates a new group.

Export to Sheets
cURL Command:

Bash

curl --location 'http://localhost:8080/groups' \
--header 'Content-Type: application/json' \
--data '{"name":"Dubai Mates"}'
2.2 Get All Groups
Retrieves a list of all existing groups.

Endpoint	Method	Description
/groups	GET	Retrieves all groups.

Export to Sheets
cURL Command:

Bash

curl --location --request GET 'http://localhost:8080/groups' \
--header 'Content-Type: application/json'
2.3 Add Members to Group
Adds an existing user to an existing group.

Endpoint	Method	Description
/groups/add-user	POST	Adds a user to a specified group.

Export to Sheets
Request Body Parameters:

Parameter	Type	Description
userId	Long	The ID of the user to add.
groupId	Long	The ID of the group to add the user to.

Export to Sheets
cURL Command:

Bash

curl --location 'http://localhost:8080/groups/add-user' \
--header 'Content-Type: application/json' \
--data '{
"userId": 1,
"groupId": 1
}'
3. Expense Management
   The /expenses endpoint is used to record new expenses and trigger the balance calculation.

Endpoint	Method	Description
/expenses	POST	Records a new expense and updates balances.

Export to Sheets
3.1 Record Unequal Expense
Records an expense where the share for each user is explicitly defined.

Request Body Parameters:

Parameter	Type	Description
description	String	Brief description of the expense.
amount	Decimal	Total amount of the expense. Must equal the sum of userShares.
groupId	Long	ID of the group the expense belongs to.
paidByUserId	Long	ID of the user who paid the full amount.
splitType	String	Must be UNEQUAL.
userShares	List<Object>	List of user IDs and their respective shares.

Export to Sheets
cURL Command:

Bash

curl --location 'http://localhost:8080/expenses' \
--header 'Content-Type: application/json' \
--data '{
"description": "Shopping expenses",
"amount": 200.00,
"groupId": 1,
"paidByUserId": 2,
"splitType": "UNEQUAL",
"userShares": [
{"userId": 1, "share": 80.00},
{"userId": 2, "share": 70.00},
{"userId": 3, "share": 50.00}
]
}'
3.2 Record Equal Expense
Records an expense that will be split equally among all members of the group.

Request Body Parameters:

Parameter	Type	Description
description	String	Brief description of the expense.
amount	Decimal	Total amount of the expense.
groupId	Long	ID of the group the expense belongs to.
paidByUserId	Long	ID of the user who paid the full amount.
splitType	String	Must be EQUAL.
groupUserIds	List<Long>	(Implicitly required for split calculation) A list of user IDs involved in the split. If not provided, it usually defaults to all group members. Note: The provided cURL is missing this array, but the service logic will likely infer or require it.

Export to Sheets
cURL Command:

Bash

curl --location 'http://localhost:8080/expenses' \
--header 'Content-Type: application/json' \
--data '{
"description": "Dinner at restaurant",
"amount": 120.00,
"groupId": 1,
"paidByUserId": 2,
"splitType": "EQUAL"
}'
4. Balance Management
   4.1 Update Balance from Expense (Manual/Reconciliation)
   This dedicated endpoint appears to manually trigger or re-trigger a balance calculation based on an existing or new expense data structure. It uses detailed expense data to update the balances between users in a group.

Endpoint	Method	Description
/balances/update-from-expense	POST	Updates user balances based on expense details.

Export to Sheets
Request Body Parameters:

Parameter	Type	Description
expenseId	Long	ID of the related expense (may be optional or used for logging/lookup).
groupId	Long	ID of the group.
paidByUserId	Long	ID of the user who paid.
totalAmount	Decimal	Total amount of the expense.
splitType	String	EQUAL or UNEQUAL.
groupUserIds	List<Long>	List of all user IDs participating in the split (required for EQUAL split).
userShares	List<Object>	List of user IDs and shares (required for UNEQUAL split).
description	String	Description for the balance update.

Export to Sheets
cURL Command:

Bash

curl --location 'http://localhost:8080/balances/update-from-expense' \
--header 'Content-Type: application/json' \
--data '{
"expenseId": 1,
"groupId": 1,
"paidByUserId": 2,
"totalAmount": 120,
"splitType": "UNEQUAL",
"groupUserIds": [
1,
2
],
"userShares": [
{
"userId": "1",
"share": "40"
},
{
"userId": "2",
"share": "40"
},
{
"userId": "3",
"share": "40"
}
],
"description": "Balance update for group 1"
}'