package labs.tema_practica;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrei
 */
public class Subscription {
    
    private List<Constraint> constraints = new ArrayList<>();

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<Constraint> constraints) {
        this.constraints = constraints;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Constraint constraint: constraints) {
            builder.append(constraint.toString()).append("; ");
        }
        return "{ " + builder.toString() + " }\n";
    }
    
    
}
