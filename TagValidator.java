
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagValidator {
   private static final Pattern TAG_PATTERN = Pattern.compile("<\\s*(/)?\\s*([A-Za-z][A-Za-z0-9:_-]*)\\b[^>]*>");

   public TagValidator() {
   }

   public String validate(String document) {
      if (document == null || document.trim().isEmpty()) {
         return "ERROR: Document is empty.";
      }

      String text = document.trim();
      Stack<TagInfo> stack = new Stack<>();
      Matcher matcher = TAG_PATTERN.matcher(text);

      while (matcher.find()) {
         String closingMarker = matcher.group(1);
         String tagName = matcher.group(2);
         String rawTag = matcher.group(0);
         int position = matcher.start();
         boolean selfClosing = rawTag.endsWith("/>");

         if (selfClosing) {
            continue;
         }

         if (closingMarker == null || closingMarker.isEmpty()) {
            stack.push(new TagInfo(tagName, position));
         } else {
            if (stack.isEmpty()) {
               return buildError("Unmatched Closing Tag", "</" + tagName + ">", position);
            }

            TagInfo top = stack.peek();
            if (!top.name.equals(tagName)) {
               return buildMismatchError(top.name, tagName);
            }

            stack.pop();
         }
      }

      if (stack.isEmpty()) {
         return "VALID: Tags are properly nested.";
      }

      TagInfo top = stack.peek();
      return buildError("Unclosed Tag", "<" + top.name + ">", top.position);
   }

   private String buildMismatchError(String expectedName, String foundName) {
      String expectedTag = "</" + expectedName + ">";
      String foundTag = "</" + foundName + ">";
      return "INVALID: Expected " + expectedTag + " but found " + foundTag + ".";
   }

   private String buildError(String errorType, String tag, int position) {
      return "INVALID: " + errorType + " " + tag + " at position " + position + ".";
   }

   private static class TagInfo {
      private final String name;
      private final int position;

      private TagInfo(String name, int position) {
         this.name = name;
         this.position = position;
      }
   }
}
