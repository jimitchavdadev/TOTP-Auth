# 🔐 TOTP Authentication System

A complete Java-based Two-Factor Authentication (2FA) system that integrates with Google Authenticator and PostgreSQL database for secure user registration and authentication.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technologies Used](#technologies-used)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Database Setup](#database-setup)
- [Project Structure](#project-structure)
- [How It Works](#how-it-works)
- [Usage](#usage)
- [Configuration](#configuration)
- [Security Features](#security-features)
- [Contributing](#contributing)

## 🎯 Overview

This TOTP (Time-based One-Time Password) Authentication System provides a robust 2FA implementation for Java applications. It generates QR codes for easy setup with Google Authenticator, securely stores user credentials in PostgreSQL, and validates time-based authentication codes.

The system is designed for applications requiring enhanced security through two-factor authentication, making it suitable for enterprise applications, secure portals, and any system where user authentication security is paramount.

## ✨ Features

- **Complete User Registration Flow**: Email validation, username/password creation with secure hashing
- **TOTP Secret Generation**: Cryptographically secure secret generation for each user
- **QR Code Generation**: Terminal-based QR code display for easy Google Authenticator setup
- **PostgreSQL Integration**: Persistent user data storage with proper database schema
- **Password Security**: SHA-256 hashing with salt for password storage
- **Input Validation**: Comprehensive validation for email, username, password, and TOTP codes
- **Interactive CLI**: User-friendly command-line interface with colored output
- **Login Testing**: Built-in functionality to test the complete authentication flow
- **Time Window Tolerance**: Accepts codes from current and previous 30-second windows

## 🛠 Technologies Used

- **Java 11+**: Core application development
- **PostgreSQL**: Database for user data persistence
- **ZXing Library**: QR code generation and encoding
- **JDBC**: Database connectivity
- **Maven**: Dependency management and build tool
- **HMAC-SHA1**: TOTP algorithm implementation
- **Base32 Encoding**: Secret key encoding for authenticator apps

## 📋 Prerequisites

Before running this application, ensure you have:

- **Java Development Kit (JDK) 11 or higher**
- **PostgreSQL 12 or higher** installed and running
- **Maven 3.6+** for dependency management
- **Google Authenticator** app on your mobile device
- **Terminal/Command Prompt** with color support (recommended)

## 🚀 Installation

### 1. Clone the Repository
```bash
git clone <repository-url>
cd TOTP-Auth
```

### 2. Maven Dependencies
Add the following dependencies to your `pom.xml`:

```xml
<dependencies>
   <!-- ZXing for QR Code generation -->
   <dependency>
      <groupId>com.google.zxing</groupId>
      <artifactId>core</artifactId>
      <version>3.5.1</version>
   </dependency>
   <dependency>
      <groupId>com.google.zxing</groupId>
      <artifactId>javase</artifactId>
      <version>3.5.1</version>
   </dependency>

   <!-- PostgreSQL JDBC Driver -->
   <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <version>42.6.0</version>
   </dependency>
</dependencies>
```

### 3. Install Dependencies
```bash
mvn clean install
```

## 🗄 Database Setup

### 1. Create PostgreSQL Database
```sql
CREATE DATABASE totp_auth;
```

### 2. Configure Database Connection
Update the database configuration in `src/main/java/com/totp/config/DatabaseConfig.java`:

```java
private static final String DB_URL = "jdbc:postgresql://localhost:5432/totp_auth";
private static final String DB_USER = "your_username";
private static final String DB_PASSWORD = "your_password";
```

### 3. Database Schema
The application automatically creates the required table on first run:

```sql
CREATE TABLE users (
                      id SERIAL PRIMARY KEY,
                      email VARCHAR(255) UNIQUE NOT NULL,
                      username VARCHAR(100) NOT NULL,
                      password VARCHAR(255) NOT NULL,
                      totp_secret VARCHAR(255),
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 📁 Project Structure

```
src/main/java/com/totp/
├── app/
│   └── TOTPRegistrationApp.java     # Main application entry point
├── config/
│   └── DatabaseConfig.java         # Database configuration and setup
├── model/
│   └── User.java                    # User entity model
├── service/
│   ├── TOTPService.java            # TOTP generation and verification
│   ├── QRCodeService.java          # QR code display functionality
│   └── UserService.java            # Database operations for users
└── util/
    ├── ConsoleColors.java          # Terminal color constants
    ├── InputValidator.java         # Input validation utilities
    └── PasswordUtil.java           # Password hashing and verification
```

### Key Components

#### 🎯 TOTPRegistrationApp.java
- **Main application class** orchestrating the entire registration flow
- Handles user interaction through command-line interface
- Coordinates between services for complete user registration
- Provides login testing functionality

#### 🔐 TOTPService.java
- **Core TOTP implementation** following RFC 6238 standard
- Generates cryptographically secure secrets
- Creates Google Authenticator-compatible URLs
- Validates time-based authentication codes with tolerance window

#### 🗃 UserService.java
- **Database operations** for user management
- Secure password hashing and verification
- User authentication and credential validation
- Email uniqueness checking

#### 📱 QRCodeService.java
- **QR code generation** for terminal display
- Creates visual representation using ASCII characters
- Provides manual entry fallback option

## ⚙ How It Works

### Registration Process

1. **User Input Collection**
   - Validates email format and uniqueness
   - Ensures username meets minimum requirements
   - Verifies password strength (minimum 6 characters)

2. **TOTP Secret Generation**
   - Creates 160-bit cryptographically secure random secret
   - Encodes secret using Base32 for authenticator compatibility

3. **QR Code Generation**
   - Generates `otpauth://` URL with user email and secret
   - Displays QR code in terminal using block characters
   - Provides manual entry option as fallback

4. **2FA Verification**
   - User scans QR code with Google Authenticator
   - System validates first TOTP code to ensure proper setup
   - Allows 3 attempts with current/previous time window tolerance

5. **Database Storage**
   - Saves user with hashed password and TOTP secret
   - Returns success confirmation with user details

### Authentication Flow

1. **Credential Verification**: Email and password validation
2. **TOTP Code Generation**: User provides current 6-digit code
3. **Time-based Validation**: System accepts current and previous 30-second window codes
4. **Access Grant**: Successful authentication on valid credentials + TOTP code

## 🎮 Usage

### Running the Application

```bash
# Compile the project
mvn compile

# Run the main application
mvn exec:java -Dexec.mainClass="com.totp.TOTPRegistrationApp"

# Or run directly with Java
java -cp target/classes com.totp.TOTPRegistrationApp
```

### Registration Process

1. **Start the application**
   ```
   ╔═══════════════════════════════════════╗
   ║        🔐 2FA Registration System      ║
   ║      Google Authenticator Required     ║
   ║         PostgreSQL Database            ║
   ╚═══════════════════════════════════════╝
   ```

2. **Enter user details**
   - Email address (must be unique and valid format)
   - Username (minimum 3 characters)
   - Password (minimum 6 characters)

3. **Scan QR Code**
   - Open Google Authenticator on your phone
   - Tap '+' to add new account
   - Scan the displayed QR code

4. **Verify Setup**
   - Enter the 6-digit code from Google Authenticator
   - System validates and completes registration

5. **Test Login** (Optional)
   - Verify the complete authentication flow
   - Test with email, password, and current TOTP code

## 🔧 Configuration

### Database Configuration
Modify `DatabaseConfig.java` for your PostgreSQL setup:

```java
private static final String DB_URL = "jdbc:postgresql://hostname:port/database_name";
private static final String DB_USER = "your_username";
private static final String DB_PASSWORD = "your_password";
```

### TOTP Settings
Current configuration in `TOTPService.java`:
- **Algorithm**: HMAC-SHA1
- **Digits**: 6
- **Time Step**: 30 seconds
- **Window Tolerance**: ±1 time step (30 seconds)

### Customization Options
- **App Name**: Modify `APP_NAME` in `TOTPService.java`
- **Secret Length**: Adjust byte array size in `generateSecret()`
- **Password Requirements**: Update validation in `InputValidator.java`
- **Colors**: Customize terminal colors in `ConsoleColors.java`

## 🔒 Security Features

### Password Security
- **SHA-256 hashing** with unique salt per password
- **Secure random salt generation** using `SecureRandom`
- **Base64 encoding** for safe database storage

### TOTP Security
- **Cryptographically secure random secret generation**
- **Standard RFC 6238 implementation**
- **Time window tolerance** to handle clock skew
- **Base32 encoding** for authenticator app compatibility

### Database Security
- **Prepared statements** to prevent SQL injection
- **Password hashing** before database storage
- **Unique email constraints** to prevent duplicates
- **Connection management** with proper resource cleanup

### Input Validation
- **Email format validation** using regex patterns
- **Username and password length requirements**
- **TOTP code format validation** (6 digits only)
- **SQL injection prevention** through parameterized queries

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/new-feature`)
3. Commit your changes (`git commit -am 'Add new feature'`)
4. Push to the branch (`git push origin feature/new-feature`)
5. Create a Pull Request

### Development Guidelines
- Follow Java naming conventions
- Add comprehensive comments for complex logic
- Include input validation for all user inputs
- Maintain consistent error handling patterns
- Add appropriate logging for troubleshooting

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For questions, issues, or contributions:
- Create an issue in the repository
- Review the code documentation
- Check PostgreSQL connection settings
- Verify Google Authenticator setup

---

**Note**: This system provides production-ready 2FA implementation. Ensure proper testing in your environment before deployment in production systems.