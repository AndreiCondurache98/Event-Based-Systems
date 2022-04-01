package labs.tema_practica;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
/**
 *
 * @author Andrei
 */
public class Publication {
    
    private String company; 
    private double value;
    private double drop;
    private double variation;
    private LocalDate date;

    public Publication(String company, double value, double drop, double variation, LocalDate date) {
        this.company = company;
        this.value = value;
        this.drop = drop;
        this.variation = variation;
        this.date = date;
    }

    @Override
    public String toString() {
        return "Publicatie: {" + "(company, " + company + "); "
                + "(value, " + value + "); (drop, " + drop + "); "
                + "(variation, " + variation + "); (date, " + date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + ")} \n";
    }
    
    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getDrop() {
        return drop;
    }

    public void setDrop(double drop) {
        this.drop = drop;
    }

    public double getVariation() {
        return variation;
    }

    public void setVariation(double variation) {
        this.variation = variation;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

}
