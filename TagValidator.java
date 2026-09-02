
// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagValidator {
   private static final Pattern TAG_PATTERN = Pattern.compile("<(/?)([A-Za-z][A-Za-z0-9]*)>");

   public TagValidator() {
   }

   public String validate(String var1) {
      Stack var2 = new Stack();
      Matcher var3 = TAG_PATTERN.matcher(var1);

      while(var3.find()) {
         boolean var4 = !var3.group(1).isEmpty();
         String var5 = var3.group(2);
         if (!var4) {
            var2.push(var5);
         } else {
            if (var2.empty()) {
               return "INVALID: Unmatched closing tag </" + var5 + ">";
            }

            String var6 = (String)var2.peek();
            if (!var6.equals(var5)) {
               return "INVALID: Expected </" + var6 + "> but found </" + var5 + ">";
            }

            var2.pop();
         }
      }

      if (var2.empty()) {
         return "VALID: Tags are properly nested.";
      } else {
         return "INVALID: Unclosed tag <" + (String)var2.peek() + ">";
      }
   }
}
