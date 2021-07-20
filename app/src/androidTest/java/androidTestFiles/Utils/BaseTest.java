package androidTestFiles.Utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class BaseTest {

    protected char getLocaleDecimalSeparator() {
        DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance();
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        char decimalSeparator = symbols.getDecimalSeparator();
        return decimalSeparator;
    }
}
