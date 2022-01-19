package ir.kimia.client.util;

/**
 * Utilities for manipulating strings.
 *
 * @author Amir
 */
public class StringUtil {

    public static String replaceArabicCharactersWithPersian(String string) {
        string = string.replaceAll( "ي" , "ی");
        string = string.replaceAll( "ة" , "ه");
        string = string.replaceAll( "ك" , "ک");
        string = string.replaceAll( "أ" , "ا");
        return string;
    }

}
