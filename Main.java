
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class Main {

    static Scanner sc = new Scanner(System.in);
    static ArrayList<User> users = new ArrayList<>();
    static LinkedList<String> history = new LinkedList<>();
    static TagValidator validator = new TagValidator();

    static String ask(String prompt) {
        System.out.print(prompt);
        return sc.nextLine();
    }

    public static void main(String[] args) {

        while (true) {

            System.out.println("\n===== HTML/XML TAG VALIDATOR =====");
            System.out.println("1. LOGIN");
            System.out.println("2. SIGN UP");
            System.out.println("3. EXIT");

            switch (ask("Enter choice: ")) {

                case "1":
                    if (login()) userMenu();
                    break;

                case "2":
                    signUp();
                    break;

                case "3":
                    System.out.println("Program ended.");
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    // Sign Up
    static void signUp() {

        String username = ask("Enter username: ");
        String password = ask("Enter password: ");

        users.add(new User(username, password));

        System.out.println("Account created successfully!");
    }

    // Login
    static boolean login() {

        String username = ask("Enter username: ");
        String password = ask("Enter password: ");

        for (User u : users) {

            if (u.username.equals(username) &&
                u.password.equals(password)) {

                System.out.println("Login successful!");
                return true;
            }
        }

        System.out.println("Invalid username or password!");
        return false;
    }

    // User Menu
    static void userMenu() {

        while (true) {

            System.out.println("\n===== USER MENU =====");
            System.out.println("1. CHECK DOCUMENT");
            System.out.println("2. VALIDATE FILE");
            System.out.println("3. VIEW HISTORY");
            System.out.println("4. CLEAR HISTORY");
            System.out.println("5. LOGOUT");

            switch (ask("Enter choice: ")) {

                case "1":
                    checkDocument();
                    break;

                case "2":
                    validateFile();
                    break;

                case "3":
                    showHistory();
                    break;

                case "4":
                    history.clear();
                    System.out.println("History cleared!");
                    break;

                case "5":
                    System.out.println("Logged out!");
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    // Check Document
    static void checkDocument() {

        System.out.println("Enter HTML/XML document:");

        String document = sc.nextLine();
        String result = validator.validate(document);

        System.out.println(result);

        history.add("Manual Document: " + result);
    }

    // Validate File
    static void validateFile() {

        String path = ask("Enter file path: ");

        try {

            String document = Files.readString(Path.of(path));
            String result = validator.validate(document);

            System.out.println(result);

            history.add("File: " + path + " - " + result);

        } catch (IOException | InvalidPathException e) {

            System.out.println("Unable to read file!");
        }
    }

    // View History
    static void showHistory() {

        if (history.isEmpty()) {

            System.out.println("No history available!");
            return;
        }

        System.out.println("\n===== VALIDATION HISTORY =====");

        for (String item : history) {
            System.out.println(item);
        }
    }

    // User Class
    static class User {

        String username;
        String password;

        User(String username, String password) {

            this.username = username;
            this.password = password;
        }
    }

    static class TagValidator {

        // HTML tags without closing tags
        static Set<String> voidTags = new HashSet<>(
            Arrays.asList(
                "area", "base", "br", "col", "embed",
                "hr", "img", "input", "link", "meta",
                "param", "source", "track", "wbr"
            )
        );

        String validate(String document) {

            Stack<String> stack = new Stack<>();

            Matcher m = Pattern.compile(
                "<(/?)([a-zA-Z][a-zA-Z0-9:_-]*)([^>]*)>"
            ).matcher(document);

            while (m.find()) {

                boolean closing = m.group(1).equals("/");
                String tag = m.group(2).toLowerCase();
                boolean selfClosing = m.group(3).trim().endsWith("/");

                // Closing tag
                if (closing) {

                    if (stack.isEmpty()) {

                        return "INVALID DOCUMENT\nUnexpected closing tag: </"
                            + tag + ">";
                    }

                    String opening = stack.pop();

                    if (!opening.equals(tag)) {

                        return "INVALID DOCUMENT\nTag <" + opening
                            + "> is not closed properly.\n"
                            + "Expected closing tag: </" + opening
                            + ">\nFound: </" + tag + ">";
                    }
                }

                // Opening tag
                else if (!selfClosing && !voidTags.contains(tag)) {

                    stack.push(tag);
                }
            }

            // Check unclosed tags
            if (!stack.isEmpty()) {

                StringBuilder msg = new StringBuilder();

                msg.append("INVALID DOCUMENT\n");
                msg.append("The following tags are not closed:\n");

                while (!stack.isEmpty()) {

                    String tag = stack.pop();

                    msg.append("<").append(tag)
                       .append("> is not closed.\n");

                    msg.append("Expected closing tag: </")
                       .append(tag).append(">\n");
                }

                return msg.toString();
            }

            return "VALID DOCUMENT\nAll tags are properly closed!";
        }
    }
}
