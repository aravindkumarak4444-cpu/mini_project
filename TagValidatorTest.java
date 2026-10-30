public class TagValidatorTest {
    public static void main(String[] args) {
        TagValidator validator = new TagValidator();
        String[][] tests = {
                {"<html><body><p>Hello</p></body></html>", "VALID"},
                {"<html><body></html></body>", "INVALID"},
                {"<html><body></body>", "INVALID"},
                {"</p>", "INVALID"},
                {"<div><p>Hello</p></div>", "VALID"},
                {"<p class=\"intro\">Hello</p>", "VALID"},
                {"<div><br/></div>", "VALID"},
                {"<div><p>Hello</div></p>", "INVALID"},
                {"", "INVALID"},
                {"<html><body>", "INVALID"},
                {"<div><br></div>", "VALID"},
                {"<?xml version=\"1.0\"?><!-- note --><Root><Item value=\"a > b\"/></Root>", "VALID"},
                {"<Root></root>", "INVALID"},
                {"<!-- no close", "INVALID"},
                {"<p class=\"intro>Hello</p>", "INVALID"},
                {"<!DOCTYPE html [ <!ELEMENT html ANY> ]><html></html>", "VALID"},
                {"<!UNKNOWN><root></root>", "INVALID"},
                {"<!DOCTYPEhtml><html></html>", "INVALID"},
                {"<!-- bad -- comment --><root></root>", "INVALID"},
                {"<p value=\"a < b\">text</p>", "INVALID"}
        };

        for (int index = 0; index < tests.length; index++) {
            String expected = tests[index][1];
            String actual = validator.validate(tests[index][0]).getStatus();
            if (!expected.equals(actual)) {
                throw new AssertionError("Test " + (index + 1) + " expected "
                        + expected + " but got " + actual + ".");
            }
        }

        TagValidator.ValidationResult namedResult = validator.validate("<item/>", "sample.xml");
        if (!"sample.xml - VALID".equals(namedResult.toString())) {
            throw new AssertionError("Named validation result was not formatted as expected.");
        }

        System.out.println("TagValidator tests passed: " + tests.length + "/" + tests.length);
    }
}
