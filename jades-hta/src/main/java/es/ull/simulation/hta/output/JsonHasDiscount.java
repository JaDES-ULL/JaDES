package es.ull.simulation.hta.output;

public interface JsonHasDiscount {
    /**
     * Returns the discount rate for this output specification, null if none is specified.
     * @return The discount rate, or null if none is specified
     */
    Double getDiscount();
}