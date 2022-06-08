package labs.tema_practica;

import java.util.Locale;

/**
 *
 * @author Andrei
 */
public class Constraint {
    
    private String attribute;
    
    private String operator;
    
    private Object value;

    public Constraint(String attribute, String operator, Object value) {
        this.attribute = attribute;
        this.operator = operator;
        this.value = value;
    }

    @Override
    public String toString() {
        return "("+ attribute.toLowerCase() + ", " + operator + ", " + value + ')';
    }
    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
    
    
}
