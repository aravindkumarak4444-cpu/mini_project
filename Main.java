
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

public class Main {
   public static void main(String[] args) {
      Scanner scanner = new Scanner(System.in);
      TagValidator validator = new TagValidator();
      ArrayList<User> users = new ArrayList<>();
      LinkedList<String> history = new LinkedList<>();
      boolean running = true;

      while (running) {
         showAuthenticationMenu();
         String choice = scanner.nextLine().trim();

         if (choice.equals("1")) {
            if (login(scanner, users)) {
               running = runValidator(scanner, validator, history);
            }
         } else if (choice.equals("2")) {
            signUp(scanner, users);
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
      System.out.println("========================================");
      System.out.println("       HTML/XML TAG VALIDATOR");
      System.out.println("========================================");
      System.out.println("1. LOGIN");
      System.out.println("2. SIGN UP");
      System.out.println("3. EXIT");
      System.out.println("\nEnter your choice:");
   }

   private static void signUp(Scanner scanner, ArrayList<User> users) {
      System.out.println("\n========================================");
      System.out.println("                 SIGN UP");
      System.out.println("========================================");

      System.out.print("Username: ");
      String username = scanner.nextLine().trim();
      if (username.isEmpty()) {
         System.out.println("Username cannot be empty.");
         return;
      }

      if (findUser(username, users) != null) {
         System.out.println("Username is already registered.");
         return;
      }

      System.out.print("Password: ");
      String password = scanner.nextLine();
      if (password.isEmpty()) {
         System.out.println("Password cannot be empty.");
         return;
      }

      System.out.print("Confirm Password: ");
      String confirmPassword = scanner.nextLine();
      if (!password.equals(confirmPassword)) {
         System.out.println("Passwords do not match.");
         return;
      }

      users.add(new User(username, password));
      System.out.println("Account created successfully!");
   }

   private static boolean login(Scanner scanner, ArrayList<User> users) {
      System.out.println("\n========================================");
      System.out.println("                 LOGIN");
      System.out.println("========================================");

      int attempts = 0;

      while (attempts < 3) {
         System.out.print("Username: ");
         String username = scanner.nextLine().trim();
         System.out.print("Password: ");
         String password = scanner.nextLine();

         User user = findUser(username, users);
         if (user != null && user.getPassword().equals(password)) {
            System.out.println("\nLogin successful!\n");
            return true;
         } else {
            attempts++;
            System.out.println("\nInvalid username or password.");
         }
      }

      System.out.println("Too many failed login attempts.");
      return false;
   }

   private static User findUser(String username, ArrayList<User> users) {
      for (User user : users) {
         if (user.getUsername().equals(username)) {
            return user;
         }
      }
      return null;
   }

   private static boolean runValidator(Scanner scanner, TagValidator validator,
         LinkedList<String> history) {
      System.out.println("========================================");
      System.out.println("       HTML/XML TAG VALIDATOR");
      System.out.println("========================================");
      System.out.println("Commands:");
      System.out.println("CHECK <document>");
      System.out.println("HISTORY");
      System.out.println("CLEAR");
      System.out.println("LOGOUT");
      System.out.println("EXIT");
      System.out.println("========================================");

      while (true) {
         System.out.print("Enter command: ");
         String command = scanner.nextLine().trim();

         if (command.equalsIgnoreCase("EXIT")) {
            System.out.println("Exiting HTML/XML Tag Validator...");
            return false;
         }

         if (command.equalsIgnoreCase("LOGOUT")) {
            logout();
            return true;
         }

         if (command.equalsIgnoreCase("HISTORY")) {
            if (history.isEmpty()) {
               System.out.println("No validation history.");
            } else {
               for (String entry : history) {
                  System.out.println(entry);
               }
            }
         } else if (command.equalsIgnoreCase("CLEAR")) {
            history.clear();
            System.out.println("History cleared.");
         } else if (command.regionMatches(true, 0, "CHECK", 0, 5)) {
            String document = command.substring(5).trim();
            String result = validator.validate(document);
            System.out.println(result);
            history.add("CHECK " + document + " -> " + result);
         } else {
            System.out.println("Unknown command. Use CHECK, HISTORY, CLEAR, LOGOUT, or EXIT.");
         }
      }
   }

   private static void logout() {
      System.out.println("Logged out successfully.\n");
   }
}
