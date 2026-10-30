import java.util.Stack;

public class TagValidator {
    public ValidationResult validate(String document) {
        if (document == null || document.trim().isEmpty()) {
            return ValidationResult.invalid("The document is empty.");
        }

        Stack<String> tagStack = new Stack<>();
        int position = 0;
        int tagsFound = 0;

        while (position < document.length()) {
            int tagStart = document.indexOf('<', position);
            if (tagStart < 0) {
                break;
            }

            if (document.startsWith("<!--", tagStart)) {
                int commentEnd = document.indexOf("-->", tagStart + 4);
                if (commentEnd < 0) {
                    return ValidationResult.invalid("An HTML comment is not closed.");
                }
                if (document.substring(tagStart + 4, commentEnd).contains("--")) {
                    return ValidationResult.invalid("A comment cannot contain '--'.");
                }
                position = commentEnd + 3;
                continue;
            }

            if (document.startsWith("<![CDATA[", tagStart)) {
                int cdataEnd = document.indexOf("]]>", tagStart + 9);
                if (cdataEnd < 0) {
                    return ValidationResult.invalid("A CDATA section is not closed.");
                }
                position = cdataEnd + 3;
                continue;
            }

            if (document.startsWith("<?", tagStart)) {
                int instructionEnd = document.indexOf("?>", tagStart + 2);
                if (instructionEnd < 0) {
                    return ValidationResult.invalid("A processing instruction is not closed.");
                }
                position = instructionEnd + 2;
                continue;
            }

            if (document.startsWith("<!", tagStart)) {
                if (!document.regionMatches(true, tagStart, "<!DOCTYPE", 0, 9)) {
                    return ValidationResult.invalid("An unsupported or malformed declaration was found.");
                }
                int declarationStart = tagStart + 9;
                if (declarationStart >= document.length()
                        || !Character.isWhitespace(document.charAt(declarationStart))) {
                    return ValidationResult.invalid("A DOCTYPE declaration is malformed.");
                }
                int declarationEnd = findDeclarationEnd(document, declarationStart);
                if (declarationEnd < 0) {
                    return ValidationResult.invalid("A declaration is not closed.");
                }
                position = declarationEnd + 1;
                continue;
            }

            int tagEnd = findTagEnd(document, tagStart + 1);
            if (tagEnd < 0) {
                return ValidationResult.invalid("A tag is not closed with '>'.");
            }

            String tagContent = document.substring(tagStart + 1, tagEnd).trim();
            if (tagContent.isEmpty()) {
                return ValidationResult.invalid("An empty tag was found.");
            }

            boolean closingTag = tagContent.charAt(0) == '/';
            if (closingTag) {
                String name = parseClosingTagName(tagContent);
                if (name == null) {
                    return ValidationResult.invalid("A closing tag is malformed.");
                }
                if (tagStack.empty()) {
                    return ValidationResult.invalid("Unexpected closing tag </" + name + ">.");
                }
                String openName = tagStack.peek();
                if (!openName.equals(name)) {
                    return ValidationResult.invalid(
                            "Closing tag </" + name + "> does not match <" + openName + ">.");
                }
                tagStack.pop();
            } else {
                ParsedOpeningTag openingTag = parseOpeningTag(tagContent);
                if (openingTag == null) {
                    return ValidationResult.invalid("An opening tag or its attributes are malformed.");
                }
                if (!openingTag.selfClosing && !isHtmlVoidElement(openingTag.name)) {
                    tagStack.push(openingTag.name);
                }
            }

            tagsFound++;
            position = tagEnd + 1;
        }

        if (tagsFound == 0) {
            return ValidationResult.invalid("No element tags were found.");
        }
        if (!tagStack.empty()) {
            return ValidationResult.invalid("Opening tag <" + tagStack.peek() + "> has no closing tag.");
        }
        return ValidationResult.valid();
    }

    public ValidationResult validate(String document, String documentName) {
        ValidationResult result = validate(document);
        if (documentName == null || documentName.trim().isEmpty()) {
            return result;
        }
        return result.withDocumentName(documentName.trim());
    }

    private static int findTagEnd(String document, int position) {
        char quote = 0;
        for (int index = position; index < document.length(); index++) {
            char current = document.charAt(index);
            if (quote != 0) {
                if (current == quote) {
                    quote = 0;
                }
            } else if (current == '"' || current == '\'') {
                quote = current;
            } else if (current == '>') {
                return index;
            }
        }
        return -1;
    }

    private static int findDeclarationEnd(String document, int position) {
        char quote = 0;
        int subsetDepth = 0;
        for (int index = position; index < document.length(); index++) {
            char current = document.charAt(index);
            if (quote != 0) {
                if (current == quote) {
                    quote = 0;
                }
            } else if (current == '"' || current == '\'') {
                quote = current;
            } else if (current == '[') {
                subsetDepth++;
            } else if (current == ']' && subsetDepth > 0) {
                subsetDepth--;
            } else if (current == '>' && subsetDepth == 0) {
                return index;
            }
        }
        return -1;
    }

    private static String parseClosingTagName(String content) {
        int position = 1;
        while (position < content.length() && Character.isWhitespace(content.charAt(position))) {
            position++;
        }
        int nameEnd = readNameEnd(content, position);
        if (nameEnd == position) {
            return null;
        }
        String name = content.substring(position, nameEnd);
        position = nameEnd;
        while (position < content.length() && Character.isWhitespace(content.charAt(position))) {
            position++;
        }
        return position == content.length() ? name : null;
    }

    private static ParsedOpeningTag parseOpeningTag(String content) {
        int end = content.length();
        while (end > 0 && Character.isWhitespace(content.charAt(end - 1))) {
            end--;
        }
        boolean selfClosing = end > 0 && content.charAt(end - 1) == '/';
        if (selfClosing) {
            end--;
            while (end > 0 && Character.isWhitespace(content.charAt(end - 1))) {
                end--;
            }
        }

        int nameEnd = readNameEnd(content, 0);
        if (nameEnd == 0 || nameEnd > end) {
            return null;
        }
        String name = content.substring(0, nameEnd);
        int position = nameEnd;

        while (position < end) {
            if (!Character.isWhitespace(content.charAt(position))) {
                return null;
            }
            while (position < end && Character.isWhitespace(content.charAt(position))) {
                position++;
            }
            if (position == end) {
                break;
            }

            int attributeNameEnd = readNameEnd(content, position);
            if (attributeNameEnd == position || attributeNameEnd > end) {
                return null;
            }
            position = attributeNameEnd;
            while (position < end && Character.isWhitespace(content.charAt(position))) {
                position++;
            }

            if (position < end && content.charAt(position) == '=') {
                position++;
                while (position < end && Character.isWhitespace(content.charAt(position))) {
                    position++;
                }
                if (position == end) {
                    return null;
                }

                char firstValueCharacter = content.charAt(position);
                if (firstValueCharacter == '"' || firstValueCharacter == '\'') {
                    position++;
                    while (position < end && content.charAt(position) != firstValueCharacter) {
                        if (content.charAt(position) == '<') {
                            return null;
                        }
                        position++;
                    }
                    if (position == end) {
                        return null;
                    }
                    position++;
                    if (position < end && !Character.isWhitespace(content.charAt(position))) {
                        return null;
                    }
                } else {
                    int valueStart = position;
                    while (position < end && !Character.isWhitespace(content.charAt(position))) {
                        char valueCharacter = content.charAt(position);
                        if (valueCharacter == '<' || valueCharacter == '"' || valueCharacter == '\'') {
                            return null;
                        }
                        position++;
                    }
                    if (position == valueStart) {
                        return null;
                    }
                }
            }
        }

        return new ParsedOpeningTag(name, selfClosing);
    }

    private static int readNameEnd(String value, int start) {
        if (start >= value.length() || !isNameStart(value.charAt(start))) {
            return start;
        }
        int position = start + 1;
        while (position < value.length() && isNameCharacter(value.charAt(position))) {
            position++;
        }
        return position;
    }

    private static boolean isNameStart(char character) {
        return Character.isLetter(character) || character == '_' || character == ':';
    }

    private static boolean isNameCharacter(char character) {
        return isNameStart(character)
                || Character.isDigit(character)
                || character == '-'
                || character == '.';
    }

    private static boolean isHtmlVoidElement(String name) {
        return name.equals("area") || name.equals("base") || name.equals("br")
                || name.equals("col") || name.equals("embed") || name.equals("hr")
                || name.equals("img") || name.equals("input") || name.equals("link")
                || name.equals("meta") || name.equals("param") || name.equals("source")
                || name.equals("track") || name.equals("wbr");
    }

    private static final class ParsedOpeningTag {
        private final String name;
        private final boolean selfClosing;

        private ParsedOpeningTag(String name, boolean selfClosing) {
            this.name = name;
            this.selfClosing = selfClosing;
        }
    }

    public static final class ValidationResult {
        private final boolean valid;
        private final String message;
        private final String documentName;

        private ValidationResult(boolean valid, String message, String documentName) {
            this.valid = valid;
            this.message = message;
            this.documentName = documentName;
        }

        private static ValidationResult valid() {
            return new ValidationResult(true, "VALID", null);
        }

        private static ValidationResult invalid(String message) {
            return new ValidationResult(false, message, null);
        }

        private ValidationResult withDocumentName(String name) {
            return new ValidationResult(valid, message, name);
        }

        public boolean isValid() {
            return valid;
        }

        public String getStatus() {
            return valid ? "VALID" : "INVALID";
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            String result = valid ? "VALID" : "INVALID: " + message;
            return documentName == null ? result : documentName + " - " + result;
        }
    }
}
