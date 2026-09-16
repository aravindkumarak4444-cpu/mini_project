
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

public class Main {
   private static final int MAX_LOGIN_ATTEMPTS = 3;
   private static final String USERS_FILE = "users.txt";
   private static final ArrayList<User> users = new ArrayList<>();
   private static final LinkedList<String> history = new LinkedList<>();
   private static int totalDocuments = 0;
   private static int validDocuments = 0;
   private static int invalidDocuments = 0;

   public static void main(String[] args) {
      loadUsersFromFile();
      Scanner scanner = new Scanner(System.in);
      TagValidator validator = new TagValidator();
      boolean running = true;

      while (running) {
         showAuthenticationMenu();
         String choice = readInput(scanner);

         if (choice.equals("1")) {
            if (login(scanner)) {
               running = runValidator(scanner, validator);
            }
         } else if (choice.equals("2")) {
            signUp(scanner);
         } else if (choice.equals("3")) {
            System.out.println("Exiting HTML/XML Tag Validator...");
            running = false;
         } else {
            System.out.println("Invalid choice. Please select 1, 2, or 3.");
         }
      }

      scanner.close();
   }

   private static void showAuthenticationMenu() {
      printHeader("       HTML/XML TAG VALIDATOR");
      System.out.println("1. LOGIN");
      System.out.println("2. SIGN UP");
      System.out.println("3. EXIT");
      System.out.println();
      System.out.println("Enter your choice:");
   }

   private static void signUp(Scanner scanner) {
      System.out.println();
      printHeader("                 SIGN UP");

      System.out.print("Username: ");
      String username = readInput(scanner);
      if (username.isEmpty()) {
         System.out.println("Username cannot be empty.");
         return;
      }

      if (findUser(username) != null) {
         System.out.println("Username already exists.");
         return;
      }

      System.out.print("Password: ");
      String password = readInput(scanner);
      if (password.isEmpty()) {
         System.out.println("Password cannot be empty.");
         return;
      }

      System.out.print("Confirm Password: ");
      String confirmPassword = readInput(scanner);
      if (!password.equals(confirmPassword)) {
         System.out.println("Passwords do not match.");
         return;
      }

      User newUser = new User(username, password);
      if (saveUserToFile(newUser)) {
         users.add(newUser);
         System.out.println("Account created successfully!");
      } else {
         System.out.println("Unable to create account. Please try again.");
      }
   }

   private static boolean login(Scanner scanner) {
      System.out.println();
      printHeader("                 LOGIN");

      int attempts = 0;
      while (attempts < MAX_LOGIN_ATTEMPTS) {
         System.out.print("Username: ");
         String username = readInput(scanner);
         System.out.print("Password: ");
         String password = readInput(scanner);

         User user = findUser(username);
         if (user != null && user.getPassword().equals(password)) {
            System.out.println();
            System.out.println("Login successful!");
            return true;
         }

         attempts++;
         System.out.println();
         System.out.println("Invalid username or password.");
      }

      System.out.println("Too many failed login attempts.");
      return false;
   }

   private static User findUser(String username) {
      for (User user : users) {
         if (user.getUsername().equals(username)) {
            return user;
         }
      }
      return null;
   }

   private static void loadUsersFromFile() {
      File file = new File(USERS_FILE);
      if (!file.exists()) {
         return;
      }

      try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
         String line;
         while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
               continue;
            }

            String[] parts = line.split("\\|", 2);
            if (parts.length != 2) {
               continue;
            }

            String username = parts[0].trim();
            String password = parts[1].trim();

            if (!username.isEmpty() && !password.isEmpty() && findUser(username) == null) {
               users.add(new User(username, password));
            }
         }
      } catch (IOException e) {
         System.out.println("ERROR: Unable to load users from file.");
      }
   }

   private static boolean saveUserToFile(User user) {
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, true))) {
         // Educational prototype: passwords are stored as plain text.
         // Production applications should use password hashing.
         writer.write(user.getUsername() + "|" + user.getPassword());
         writer.newLine();
         return true;
      } catch (IOException e) {
         return false;
      }
   }

   private static boolean runValidator(Scanner scanner, TagValidator validator) {
      while (true) {
         showValidatorMenu();
         String input = readInput(scanner);

         if (input.equals("1") || input.equalsIgnoreCase("CHECK")) {
            processCheck(scanner, validator);
         } else if (input.equals("2") || input.equalsIgnoreCase("FILE")) {
            processFileValidation(scanner, validator);
         } else if (input.equals("3") || input.equalsIgnoreCase("HISTORY")) {
            displayHistory();
         } else if (input.equals("4") || input.equalsIgnoreCase("STATISTICS")) {
            displayStatistics();
         } else if (input.equals("5") || input.equalsIgnoreCase("CLEAR")) {
            clearHistory();
         } else if (input.equals("6") || input.equalsIgnoreCase("LOGOUT")) {
            logout();
            return true;
         } else if (input.equals("7") || input.equalsIgnoreCase("EXIT")) {
            System.out.println("Exiting HTML/XML Tag Validator...");
            return false;
         } else if (input.regionMatches(true, 0, "CHECK", 0, 5)) {
            String document = input.substring(5).trim();
            processCheckDocument(document, validator);
         } else if (input.regionMatches(true, 0, "FILE", 0, 4)) {
            String filePath = input.substring(4).trim();
            validateFile(filePath, validator);
         } else {
            System.out.println("Invalid choice. Please select a valid option.");
         }
      }
   }

   private static void showValidatorMenu() {
      printHeader("       HTML/XML TAG VALIDATOR");
      System.out.println("1. CHECK DOCUMENT");
      System.out.println("2. VALIDATE FILE");
      System.out.println("3. VIEW HISTORY");
      System.out.println("4. VIEW STATISTICS");
      System.out.println("5. CLEAR HISTORY");
      System.out.println("6. LOGOUT");
      System.out.println("7. EXIT");
      System.out.println();
      System.out.print("Enter your choice: ");
   }

   private static void processCheck(Scanner scanner, TagValidator validator) {
      System.out.println();
      System.out.print("Enter HTML/XML document: ");
      String document = readInput(scanner);
      processCheckDocument(document, validator);
   }

   private static void processCheckDocument(String document, TagValidator validator) {
      if (document.trim().isEmpty()) {
         String result = "ERROR: Document is empty.";
         System.out.println(result);
         recordValidation(result);
         return;
      }

      String result = validator.validate(document);
      System.out.println(result);
      addHistoryRecord(document, result);
      recordValidation(result);
   }

   private static void processFileValidation(Scanner scanner, TagValidator validator) {
      System.out.println();
      System.out.print("Enter file path: ");
      String filePath = readInput(scanner);
      validateFile(filePath, validator);
   }

   private static void validateFile(String filePath, TagValidator validator) {
      if (filePath == null || filePath.trim().isEmpty()) {
         System.out.println("ERROR: Invalid file path.");
         return;
      }

      Path path;
      try {
         path = Paths.get(filePath.trim());
      } catch (InvalidPathException e) {
         System.out.println("ERROR: Invalid file path.");
         return;
      }

      if (!Files.exists(path)) {
         System.out.println("ERROR: File does not exist.");
         return;
      }

      if (!Files.isRegularFile(path)) {
         System.out.println("ERROR: Invalid file path.");
         return;
      }

      try {
         System.out.println();
         System.out.println("Reading file...");
         String content = Files.readString(path);
         System.out.println();
         printHeader("FILE VALIDATION");
         System.out.println();
         System.out.println("File: " + path.getFileName());
         System.out.println();

         if (content.trim().isEmpty()) {
            System.out.println("ERROR: Document is empty.");
            System.out.println();
            System.out.println("----------------------------------------");
            addHistoryRecord("FILE " + path.getFileName(), "ERROR: Document is empty.");
            recordValidation("ERROR: Document is empty.");
            return;
         }

         String result = validator.validate(content);
         System.out.println("Validation Result:");
         System.out.println(result);
         System.out.println();
         System.out.println("----------------------------------------");
         addHistoryRecord("FILE " + path.getFileName(), result);
         recordValidation(result);
      } catch (IOException e) {
         System.out.println("ERROR: Unable to read file.");
      }
   }

   private static void addHistoryRecord(String document, String result) {
      String status = result.startsWith("VALID") ? "VALID" : "INVALID";
      history.addLast(document + " | " + status);
   }

   private static void displayHistory() {
      System.out.println();
      printHeader("       VALIDATION HISTORY");

      if (history.isEmpty()) {
         System.out.println("No validation history available.");
         return;
      }

      int index = 1;
      for (String record : history) {
         String[] parts = record.split("\\|", 2);
         String document = parts[0].trim();
         String status = parts.length > 1 ? parts[1].trim() : "INVALID";
         System.out.println();
         System.out.println(index + ". " + document);
         System.out.println("   Result: " + status);
         System.out.println();
         System.out.println("----------------------------------------");
         index++;
      }
   }

   private static void clearHistory() {
      if (history.isEmpty()) {
         System.out.println("Validation history is already empty.");
         return;
      }

      history.clear();
      System.out.println("Validation history cleared successfully.");
   }

   private static void displayStatistics() {
      double successRate = 0.0;
      if (totalDocuments > 0) {
         successRate = (validDocuments * 100.0) / totalDocuments;
      }

      System.out.println();
   printHeader("       VALIDATION STATISTICS");
      System.out.println("Total Documents   : " + totalDocuments);
      System.out.println("Valid Documents   : " + validDocuments);
      System.out.println("Invalid Documents : " + invalidDocuments);
      System.out.println("Success Rate      : " + String.format("%.1f%%", successRate));
      System.out.println();
      printSeparator();
   }

   private static void recordValidation(String result) {
      totalDocuments++;
      if (result.startsWith("VALID")) {
         validDocuments++;
      } else {
         invalidDocuments++;
      }
   }

   private static String readInput(Scanner scanner) {
      if (!scanner.hasNextLine()) {
         return "";
      }
      return scanner.nextLine().trim();
   }

   private static void printHeader(String title) {
      printSeparator();
      System.out.println(title);
      printSeparator();
   }

   private static void printSeparator() {
      System.out.println("========================================");
   }

   private static void logout() {
      System.out.println("Logged out successfully.");
      System.out.println();
   }
}
