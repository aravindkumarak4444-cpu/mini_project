import java.util.*;

public class Main {
    private static final ArrayList<User> users = new ArrayList<>();
    private static final LinkedList<String> history = new LinkedList<>();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n1. LOGIN  2. SIGN UP  3. EXIT");
            System.out.print("Enter choice: ");
            String choice = sc.nextLine();

            if (choice.equals("1")) {
                if (login(sc)) running = menu(sc);
            } else if (choice.equals("2")) {
                signUp(sc);
            } else if (choice.equals("3")) {
                running = false;
            } else {
                System.out.println("Invalid choice.");
            }
        }
        sc.close();
        System.out.println("Program ended.");
    }

    private static void signUp(Scanner sc) {
        System.out.print("Username: ");
        String user = sc.nextLine().trim();
        if (user.isEmpty() || findUser(user) != null) {
            System.out.println("Username is empty or already exists.");
            return;
        }

        System.out.print("Password: ");
        String pass = sc.nextLine().trim();
        System.out.print("Confirm Password: ");
        String confirm = sc.nextLine().trim();
        if (pass.isEmpty() || !pass.equals(confirm)) {
            System.out.println("Password is empty or does not match.");
            return;
        }

        users.add(new User(user, pass));
        System.out.println("Account created successfully!");
    }

    private static boolean login(Scanner sc) {
        for (int i = 0; i < 3; i++) {
            System.out.print("Username: ");
            String user = sc.nextLine().trim();
            System.out.print("Password: ");
            String pass = sc.nextLine().trim();

            User found = findUser(user);
            if (found != null && found.getPassword().equals(pass)) {
                System.out.println("Login successful!");
                return true;
            }
            System.out.println("Invalid username or password.");
        }
        return false;
    }

    private static User findUser(String username) {
        for (User u : users) {
            if (u.getUsername().equals(username)) return u;
        }
        return null;
    }

    private static boolean menu(Scanner sc) {
        while (true) {
            System.out.println("\n1. CHECK DOCUMENT  2. VIEW HISTORY  3. CLEAR HISTORY");
            System.out.println("4. LOGOUT  5. EXIT");
            System.out.print("Enter choice: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Enter HTML/XML document: ");
                    history.add(validate(sc.nextLine(), true));
                    break;
                case "2":
                    showHistory();
                    break;
                case "3":
                    history.clear();
                    System.out.println("History cleared.");
                    break;
                case "4":
                    System.out.println("Logged out successfully.");
                    return true;
                case "5":
                    return false;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    public static String validate(String document) {
        return validate(document, false);
    }

    public static String validate(String document, boolean show) {
        String result = validateDocument(document);
        if (show) System.out.println(result);
        return result;
    }

    private static String validateDocument(String doc) {
        if (doc == null || doc.trim().isEmpty()) return "INVALID";

        Stack<String> stack = new Stack<>();
        int pos = 0;
        boolean foundTag = false;

        while (pos < doc.length()) {
            int start = doc.indexOf("<", pos);
            if (start == -1) break;

            int end = doc.indexOf(">", start);
            if (end == -1) return "INVALID";

            String tag = doc.substring(start + 1, end).trim();
            if (tag.isEmpty()) return "INVALID";
            foundTag = true;

            if (tag.startsWith("/")) {
                if (stack.empty() || !stack.peek().equals(tag.substring(1).trim())) return "INVALID";
                stack.pop();
            } else if (!tag.endsWith("/")) {
                stack.push(tag);
            }
            pos = end + 1;
        }
        return (foundTag && stack.empty()) ? "VALID" : "INVALID";
    }

    private static void showHistory() {
        if (history.isEmpty()) {
            System.out.println("No validation history.");
            return;
        }
        for (int i = 0; i < history.size(); i++) {
            System.out.println((i + 1) + ". " + history.get(i));
        }
    }
}