package ir.kimia.client.ui;

import javafx.application.Platform;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextField;

import static ir.kimia.client.common.Constants.THOUSAND_SEPARATOR;

public class FormattedDoubleTextField extends TextField {

    public FormattedDoubleTextField() {
        super();
    }

    public FormattedDoubleTextField(String text) {
        super(text);
    }

    @Override
    public void replaceText(int start, int end, String text) {
        // Really crude attempt at parsing. Probably better ways to do this.

        // Mock up the result of inserting the text "as is"
        StringBuilder mockupText = new StringBuilder(getText());
        mockupText.replace(start, end, text);

        // Strip the commas out, they will need to move anyway
        int commasRemovedBeforeInsert = 0;
        for (int commaIndex = mockupText.lastIndexOf(THOUSAND_SEPARATOR); commaIndex >= 0; commaIndex = mockupText
                .lastIndexOf(THOUSAND_SEPARATOR)) {
            mockupText.replace(commaIndex, commaIndex + 1, "");
            if (commaIndex < start) {
                commasRemovedBeforeInsert++;
            }
        }

        // Check if the inserted text is ok (still forms a number)
        boolean ok = true;
        int decimalPointCount = 0;
        for (int i = 0; i < mockupText.length() && ok; i++) {
            char c = mockupText.charAt(i);
            if (c == '-') {
                ok = i == 0;
            } else if (c == '.') {
                ok = decimalPointCount == 0;
            } else {
                ok = Character.isDigit(c);
            }
        }

        // if it's ok, insert the commas in the correct place, update the text,
        // and position the carat:
        if (ok) {
            int commasInsertedBeforeInsert = 0;
            int startNonFractional = 0;
            if (mockupText.length() > 0 && mockupText.charAt(0) == '-') {
                startNonFractional = 1;
            }
            int endNonFractional = mockupText.indexOf(".");
            if (endNonFractional == -1) {
                endNonFractional = mockupText.length();
            }
            for (int commaInsertIndex = endNonFractional - 3; commaInsertIndex > startNonFractional; commaInsertIndex -= 3) {
                mockupText.insert(commaInsertIndex, THOUSAND_SEPARATOR);
                if (commaInsertIndex < start - commasRemovedBeforeInsert
                        + text.length()) {
                    commasInsertedBeforeInsert++;
                }
            }

            final int caratPos = start - commasRemovedBeforeInsert
                    + commasInsertedBeforeInsert + text.length();

            // System.out.printf("Original text: %s. Replaced text: %s. start: %d. end: %d. commasInsertedBeforeInsert: %d. commasRemovedBeforeInsert: %d. caratPos: %d.%n",
            // getText(), mockupText, start, end, commasInsertedBeforeInsert,
            // commasRemovedBeforeInsert, caratPos);

            // update the text:
            this.setText(mockupText.toString());

            // move the carat:
            // Needs to be scheduled to the fx application thread after the current
            // event has finished processing to override
            // default behavior
            // This seems like a bit of a hack...
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    positionCaret(caratPos);
                }
            });
        }
    }

    @Override
    public void replaceText(IndexRange range, String text) {
        this.replaceText(range.getStart(), range.getEnd(), text);
    }

    @Override
    public void insertText(int index, String text) {
        this.replaceText(index, index, text);
    }

    @Override
    public void deleteText(int start, int end) {

        // special case where user deletes a comma:
        if (start >= 1 && end - start == 1 && getText().charAt(start) == ',') {
            // move cursor back
            this.selectRange(getAnchor() - 1, getAnchor() - 1);
        } else {
            this.replaceText(start, end, "");
        }
    }

    @Override
    public void deleteText(IndexRange range) {
        this.deleteText(range.getStart(), range.getEnd());
    }

    @Override
    public void replaceSelection(String replacement) {
        this.replaceText(getSelection(), replacement);
    }

    public String getValue() {
        final String text = getText();
        if(text != null) {
            return text.replaceAll(THOUSAND_SEPARATOR, "");
        } else {
            return null;
        }
    }
}
