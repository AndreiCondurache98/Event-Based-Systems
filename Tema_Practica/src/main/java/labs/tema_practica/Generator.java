package labs.tema_practica;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import static labs.tema_practica.InitParams.*;

/**
 *
 * @author Andrei
 */
public class Generator {

    private final Random random = new Random();
    
    List<Subscription> subscriptions = new ArrayList<>();
    List<Publication> publications = new ArrayList<>();

    public List<Publication> generatePublications() {

        while (publications.size() < PUBLICATIONS_NUMBER) {

            String company = (Company.values()[random.nextInt(Company.values().length)]).toString();
            double currentValue = generateRandomNumber(MIN_VALUE, MAX_VALUE);
            double drop = generateRandomNumber(MIN_VALUE, currentValue);
            double variation = generateRandomNumber(MIN_VARIATION, MAX_VARIATION);
            LocalDate date = generateRandomDate(MIN_DATE, MAX_DATE);

            Publication publication = new Publication(company, currentValue, drop, variation, date);

            publications.add(publication);
        }

        return publications;
    }

    public List<Subscription> generateSubscriptions() {

        generateConstraints(Attribute.Company.toString(), COMPANY_FREQUENCY, 0, 0);
        generateConstraints(Attribute.Value.toString(), VALUE_FREQUENCY, MIN_VALUE, MAX_VALUE);
        generateConstraints(Attribute.Drop.toString(), DROP_FREQUENCY, MIN_VALUE, MAX_VALUE);
        generateConstraints(Attribute.Variation.toString(), VARIATION_FREQUENCY, MIN_VARIATION, MAX_VARIATION);
        generateConstraints(Attribute.Date.toString(), DATE_FREQUENCY, MIN_DATE, MAX_DATE);

        return subscriptions;
    }


    private <T> void generateConstraints(String attributeName, double frequency, T lowerLimit, T upperLimit) {
        int nrOfConstraints = 0;
        List<Subscription> availableSubscriptions = new ArrayList<>();

        while (nrOfConstraints < (int) (frequency * SUBSCRIPTIONS_NUMBER)) {
            Constraint constraint;

            if (Attribute.Company.toString().equals(attributeName)) {
                String attributeValue = (Company.values()[random.nextInt(Company.values().length)]).toString();

                if (nrOfConstraints < (int) Math.ceil(frequency * EQ_FREQUENCY * SUBSCRIPTIONS_NUMBER)) {
                    constraint = new Constraint(attributeName, "=", attributeValue);
                } else {
                    constraint = new Constraint(attributeName, "!=", attributeValue);
                }
            } else {
                String operator = (OPERATOR.values()[random.nextInt(OPERATOR.values().length)]).getOperator();
                if (Attribute.Date.toString().equals(attributeName)) {
                    LocalDate attributeValue = generateRandomDate((LocalDate) lowerLimit, (LocalDate) upperLimit);
                    constraint = new Constraint(attributeName, operator, attributeValue);
                } else {
                    double attributeValue = generateRandomNumber((double) lowerLimit, (double) upperLimit);
                    constraint = new Constraint(attributeName, operator, attributeValue);
                }
            }

            if (subscriptions.size() < SUBSCRIPTIONS_NUMBER) {
                Subscription subscription = new Subscription();
                subscription.getConstraints().add(constraint);
                subscriptions.add(subscription);
            } else {
                if (availableSubscriptions.isEmpty()) {
                    availableSubscriptions = subscriptions.stream()
                            .filter(s -> !s.getConstraints().stream()
                            .filter(c -> !c.getAttribute().equals(attributeName))
                            .collect(Collectors.toList()).isEmpty())
                            .collect(Collectors.toList());
                }
                int index = random.nextInt(availableSubscriptions.size());
                Subscription sub = availableSubscriptions.get(index);
                sub.getConstraints().add(constraint);
                availableSubscriptions.remove(index);
            }
            nrOfConstraints++;
        }
    }

    private double generateRandomNumber(double minLimit, double maxLimit) {
        double value = minLimit + random.nextDouble() * (maxLimit - minLimit);

        return Math.round(value * 100.0) / 100.0;
    }

    private LocalDate generateRandomDate(LocalDate startDate, LocalDate endDate) {
                
        long startEpochDay = startDate.toEpochDay();
        long endEpochDay = endDate.toEpochDay();
        long randomDay = ThreadLocalRandom
                .current()
                .nextLong(startEpochDay, endEpochDay);
        
        return LocalDate.ofEpochDay(randomDay);
    }
}
