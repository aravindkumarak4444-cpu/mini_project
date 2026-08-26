
// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
import java.util.Scanner;

public class Main {
   public Main() {
   }

   public static void main(String[] var0) {
      Scanner var1 = new Scanner(System.in);
      TagValidator var2 = new TagValidator();
      System.out.println("========================================");
      System.out.println("       HTML/XML TAG VALIDATOR");
      System.out.println("========================================");
      System.out.println("Commands:");
      System.out.println("CHECK <document>");
      System.out.println("EXIT");
      System.out.println("========================================");

      while(true) {
         String var3 = var1.nextLine().trim();
         if (var3.equalsIgnoreCase("EXIT")) {
            System.out.println("Exiting HTML/XML Tag Validator...");
            var1.close();
            return;
         }

         if (var3.regionMatches(true, 0, "CHECK", 0, 5)) {
            String var4 = var3.substring(5).trim();
            System.out.println(var2.validate(var4));
         } else {
            System.out.println("Unknown command. Use CHECK <document> or EXIT.");
         }
      }
   }
}
