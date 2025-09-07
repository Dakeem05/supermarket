## Supermarket Inventory System

## Overview
The Supermarket Inventory System is a Java-based desktop application designed to manage inventory and point-of-sale (POS) operations for retail supermarkets.
Built using Java Swing for the graphical user interface and PostgreSQL for data storage, 
it provides a user-friendly interface for administrators and attendants to manage products, process sales, and track sales history. 
The system supports multi-user access with role-based permissions (admin and attendant).

## Features
# User Management:
- Admin users can add new users (admins or attendants).
- Secure login with username and password authentication.
# Product Management:
- Add, edit, and delete products with details such as name, price, quantity, category, and optional image.
- View all products in a tabular format with refresh functionality.
# Point of Sale (POS):
- Select products, specify quantities, and add to a shopping cart.
- Process sales with automatic stock updates and sales recording.
# Sales History:
- View a history of all sales with details including sale ID, admin ID, total amount, and date.
# User Interface:
- Modern gradient background on login and dashboard screens.
- Centered, compact login fields and smaller, concise dashboard buttons.
- Intuitive navigation with role-based access control.
# Database Integration:
- PostgreSQL database for reliable storage of admins, products, sales, and sale items.
- Automatic table creation and initialization with a default admin user.

# Technologies Used
- Java: Core programming language for application logic and GUI (Java Swing).
- PostgreSQL: Relational database for data persistence.
- JDBC: For database connectivity.
- Java AWT/Swing: For building the graphical user interface.

# Prerequisites
- To run the Supermarket Inventory System, ensure you have the following installed:
- Java Development Kit (JDK)
- PostgreSQL:
- PostgreSQL JDBC Driver: Ensure the JDBC driver is included in the project dependencies.

  
## Usage Guide
# Login:
- Enter credentials on the login screen.
- Admins can access all features; attendants cannot add users.
# Dashboard:
- Access various features via buttons: Add User (admin only), View Products, Add Product, POS, Sales History, and About.
# Product Management:
- Add new products with details and optional images.
- View, edit, or delete products in a table.
# Point of Sale:
- Select products from the available list, specify quantities, and add to the cart.
- Confirm and process sales, which updates stock and records the transaction.
# Sales History:
- View all past sales with details in a table.
# User Management (Admin Only):
- Add new users with a username, password, and role (admin or attendant).
