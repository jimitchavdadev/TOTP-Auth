// Main Application Class
package com.totp;

import com.totp.service.UserService;
import com.totp.service.TOTPService;
import com.totp.service.QRCodeService;
import com.totp.model.User;
import com.totp.util.ConsoleColors;
import com.totp.util.InputValidator;

import java.util.Scanner;

public class TOTPRegistrationApp {

    private final UserService userService;
    private final TOTPService totpService;
    private final QRCodeService qrCodeService;

    public TOTPRegistrationApp() {
        this.userService = new UserService();
        this.totpService = new TOTPService();
        this.qrCodeService = new QRCodeService();
    }

    public static void main(String[] args) {
        TOTPRegistrationApp app = new TOTPRegistrationApp();
        app.run();
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);

        printWelcomeHeader();

        try {
            // Step 1: Collect user information
            User newUser = collectUserInfo(scanner);

            // Step 2: Generate TOTP secret
            String secret = totpService.generateSecret();
            newUser.setTotpSecret(secret);

            // Step 3: Generate and display QR code
            String qrCodeUrl = totpService.generateQRCodeURL(newUser.getEmail(), secret);
            displaySetupInstructions();
            qrCodeService.displayQRCode(qrCodeUrl, secret);

            // Step 4: Verify TOTP code
            if (verifyTOTP(scanner, secret)) {
                // Step 5: Complete registration - Save to database
                if (userService.createUser(newUser)) {
                    printRegistrationSuccess(newUser);

                    // Demo login
                    if (askForLoginTest(scanner)) {
                        testLogin(scanner);
                    }
                } else {
                    System.out.println(ConsoleColors.RED + "❌ Failed to save user to database!" + ConsoleColors.RESET);
                }
            } else {
                System.out.println(ConsoleColors.RED + "❌ REGISTRATION FAILED!" + ConsoleColors.RESET);
                System.out.println("2FA verification unsuccessful. Please try again.");
            }

        } catch (Exception e) {
            System.err.println(ConsoleColors.RED + "❌ Error during registration: " + e.getMessage() + ConsoleColors.RESET);
            e.printStackTrace();
        } finally {
            scanner.close();
            userService.close();
        }
    }

    private void printWelcomeHeader() {
        System.out.println(ConsoleColors.CYAN + "╔═══════════════════════════════════════╗" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN + "║        🔐 2FA Registration System      ║" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN + "║      Google Authenticator Required     ║" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN + "║         PostgreSQL Database            ║" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN + "╚═══════════════════════════════════════╝" + ConsoleColors.RESET);
    }

    private User collectUserInfo(Scanner scanner) {
        System.out.println("\n" + ConsoleColors.BLUE + "📝 User Registration:" + ConsoleColors.RESET);

        String email = getValidEmail(scanner);
        String username = getValidUsername(scanner);
        String password = getValidPassword(scanner);

        return new User(email, username, password);
    }

    private String getValidEmail(Scanner scanner) {
        String email;
        do {
            System.out.print("Enter email: ");
            email = scanner.nextLine().trim().toLowerCase();

            if (!InputValidator.isValidEmail(email)) {
                System.out.println(ConsoleColors.RED + "❌ Invalid email format!" + ConsoleColors.RESET);
                continue;
            }

            if (userService.emailExists(email)) {
                System.out.println(ConsoleColors.RED + "❌ Email already registered!" + ConsoleColors.RESET);
                email = "";
            }
        } while (email.isEmpty());

        return email;
    }

    private String getValidUsername(Scanner scanner) {
        String username;
        do {
            System.out.print("Enter username: ");
            username = scanner.nextLine().trim();
            if (!InputValidator.isValidUsername(username)) {
                System.out.println(ConsoleColors.RED + "❌ Username must be at least 3 characters!" + ConsoleColors.RESET);
            }
        } while (!InputValidator.isValidUsername(username));

        return username;
    }

    private String getValidPassword(Scanner scanner) {
        String password;
        do {
            System.out.print("Enter password (min 6 chars): ");
            password = scanner.nextLine();
            if (!InputValidator.isValidPassword(password)) {
                System.out.println(ConsoleColors.RED + "❌ Password must be at least 6 characters!" + ConsoleColors.RESET);
            }
        } while (!InputValidator.isValidPassword(password));

        return password;
    }

    private void displaySetupInstructions() {
        System.out.println("\n" + ConsoleColors.BLUE + "📱 Setup Google Authenticator:" + ConsoleColors.RESET);
        System.out.println("1. Open Google Authenticator app on your phone");
        System.out.println("2. Tap '+' to add a new account");
        System.out.println("3. Choose 'Scan QR code'");
        System.out.println("4. Scan the QR code below:");
    }

    private boolean verifyTOTP(Scanner scanner, String secret) {
        System.out.println("\n" + ConsoleColors.BLUE + "🔐 2FA Verification:" + ConsoleColors.RESET);
        System.out.println("After scanning the QR code, Google Authenticator will show a 6-digit code.");

        int attempts = 3;
        while (attempts > 0) {
            System.out.print("Enter the 6-digit code from Google Authenticator: ");
            String userCode = scanner.nextLine().trim();

            if (!InputValidator.isValidTOTPCode(userCode)) {
                System.out.println(ConsoleColors.RED + "❌ Please enter exactly 6 digits." + ConsoleColors.RESET);
                attempts--;
                continue;
            }

            if (totpService.verifyCode(secret, userCode)) {
                System.out.println(ConsoleColors.GREEN + "✅ 2FA code verified successfully!" + ConsoleColors.RESET);
                return true;
            } else {
                attempts--;
                if (attempts > 0) {
                    System.out.println(ConsoleColors.RED + "❌ Invalid code. " + attempts + " attempts remaining." + ConsoleColors.RESET);
                    System.out.println("💡 Make sure your phone's time is synchronized.");
                } else {
                    System.out.println(ConsoleColors.RED + "❌ Too many failed attempts." + ConsoleColors.RESET);
                }
            }
        }

        return false;
    }

    private void printRegistrationSuccess(User user) {
        System.out.println("\n" + ConsoleColors.GREEN + "🎉 REGISTRATION SUCCESSFUL!" + ConsoleColors.RESET);
        System.out.println("✅ User: " + user.getUsername());
        System.out.println("✅ Email: " + user.getEmail());
        System.out.println("✅ 2FA: Enabled");
        System.out.println("✅ Database: User saved successfully");
        System.out.println("\n🔒 Your account is now protected with 2FA!");
    }

    private boolean askForLoginTest(Scanner scanner) {
        System.out.println("\n" + ConsoleColors.YELLOW + "Would you like to test login? (y/n): " + ConsoleColors.RESET);
        return scanner.nextLine().toLowerCase().startsWith("y");
    }

    private void testLogin(Scanner scanner) {
        System.out.println("\n" + ConsoleColors.CYAN + "🔐 Login Test:" + ConsoleColors.RESET);

        System.out.print("Email: ");
        String email = scanner.nextLine().trim().toLowerCase();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        User user = userService.authenticateUser(email, password);
        if (user == null) {
            System.out.println(ConsoleColors.RED + "❌ Invalid credentials!" + ConsoleColors.RESET);
            return;
        }

        System.out.print("Enter 2FA code: ");
        String code = scanner.nextLine().trim();

        if (totpService.verifyCode(user.getTotpSecret(), code)) {
            System.out.println(ConsoleColors.GREEN + "✅ Login successful!" + ConsoleColors.RESET);
            System.out.println("🎉 Welcome, " + user.getUsername() + "!");
        } else {
            System.out.println(ConsoleColors.RED + "❌ Invalid 2FA code!" + ConsoleColors.RESET);
        }
    }
}




/*
Maven Dependencies (pom.xml):
<dependencies>
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
</dependencies>

PostgreSQL Setup:
1. Install PostgreSQL
2. Create database: CREATE DATABASE totp_auth;
3. Update connection details in DatabaseConfig.java
4. Run the application (it will create the table automatically)
*/