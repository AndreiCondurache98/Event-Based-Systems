package labs.tema_practica;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Andrei
 */
public final class InitParams {

    public static final int PUBLICATIONS_NUMBER = 10;
    public static final int SUBSCRIPTIONS_NUMBER = 10;
    
    public static final double COMPANY_FREQUENCY = 0.5;
    public static final double VALUE_FREQUENCY = 0.0;
    public static final double DROP_FREQUENCY = 0.3;
    public static final double VARIATION_FREQUENCY = 0.3;
    public static final double DATE_FREQUENCY = 0.3;
    public static final double EQ_FREQUENCY = 0.6;

    public static final double MIN_VALUE = 0.0;
    public static final double MAX_VALUE = 100.0;

    public static final double MIN_VARIATION = 0.00;
    public static final double MAX_VARIATION = 1.00;
    
    public static final LocalDate MIN_DATE = LocalDate.parse("28.03.2022", DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    public static final LocalDate MAX_DATE = LocalDate.parse("01.04.2022", DateTimeFormatter.ofPattern("dd.MM.yyyy"));

    public enum Attribute {
        Company,
        Value,
        Drop,
        Variation,
        Date
    }
    
    
    public enum Company {
        GOOGLE,
        AMAZON,
        APPLE,
        MICROSOFT
    }

    public enum OPERATOR {
        EQ("="),
        DIF("!="),
        LT("<"),
        LQT("<="),
        GT(">"),
        GQT(">=");

        private final String operator;

        private OPERATOR(String operator) {
            this.operator = operator;
        }

        public String getOperator() {
            return operator;
        }
    }

}
