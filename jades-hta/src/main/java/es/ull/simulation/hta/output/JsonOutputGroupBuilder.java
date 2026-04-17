package es.ull.simulation.hta.output;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.inforeceiver.BreakdownCostCollector;
import es.ull.simulation.hta.inforeceiver.BudgetImpactCollector;
import es.ull.simulation.hta.inforeceiver.CostCollector;
import es.ull.simulation.hta.inforeceiver.LYCollector;
import es.ull.simulation.hta.inforeceiver.ListenerCollector;
import es.ull.simulation.hta.inforeceiver.QALYCollector;
import es.ull.simulation.hta.output.JsonOutputGroup.Phase;
import es.ull.simulation.hta.params.Discount;

public class JsonOutputGroupBuilder {
    private final JsonOutputGroup group;    

    public JsonOutputGroupBuilder(JsonOutputGroup group) {
        this.group = group;
    }

    public JsonOutputGroup getGroup() {
        return group;
    }

    public List<ListenerCollector> buildListenerCollectors(HTAExperiment exp, boolean baseCase) {
        ArrayList<ListenerCollector> listenerCollectors = new ArrayList<>();

        Phase phaseType = group.getPhase();
        if (baseCase && Phase.PSA_RUNS.equals(phaseType)) return listenerCollectors;
        if (!baseCase && Phase.BASE_CASE.equals(phaseType)) return listenerCollectors;

        for (JsonOutputSpec spec : group.getList()) {
            final String name = spec.getName();
            final String description = spec.getDescription();

            if (spec instanceof JsonBudgetImpactOutput) {
                JsonBudgetImpactOutput bi = (JsonBudgetImpactOutput) spec;
                int nYears = bi.getHorizon();
                listenerCollectors.add(new BudgetImpactCollector(exp, baseCase, nYears));
            }
            else if (spec instanceof JsonEpiOutput) {
                switch (name) {
                        case "incidence":
                            // TODO: listenerCollectors.add(new IncidenceCollector(...));
                            break;
                        case "prevalence":
                            // TODO: listenerCollectors.add(new PrevalenceCollector(...));
                            break;
                        case "cumulative_incidence":
                            // TODO: listenerCollectors.add(new CumulativeIncidenceCollector(...));
                            break;
                        default:
                            throw new IllegalArgumentException("Unsupported JSONEpiOutput name: " + name);
                    }                
            }
            else if (spec instanceof JsonCoreMetricOutput) {
                switch (name) {
                    case "breakdown_costs":
                        listenerCollectors.add(new BreakdownCostCollector(exp, baseCase, new Discount(exp.resolveDiscountForCosts(spec))));
                        break;

                    case "cost":
                        listenerCollectors.add(new CostCollector(exp, baseCase, description, new Discount(exp.resolveDiscountForCosts(spec))));
                        break;

                    case "life_expectancy":
                        listenerCollectors.add(new LYCollector(exp, baseCase, description, new Discount(exp.resolveDiscountForEffects(spec))));
                        break;

                    case "quality_adjusted_life_expectancy":
                        listenerCollectors.add(new QALYCollector(exp, baseCase, description, new Discount(exp.resolveDiscountForEffects(spec))));
                        break;

                    case "time_to_event":
                        // TODO: listenerCollectors.add(new TimeToEventCollector(exp, baseCase, description, discount));
                        break;

                    case "manifestation_prevalence":
                        // TODO: listenerCollectors.add(new ManifestationPrevalenceCollector(...));
                        break;

                    case "manifestation_incidence":
                        // TODO: listenerCollectors.add(new ManifestationIncidenceCollector(...));
                        break;

                    case "individual_outcomes":
                        // TODO: listenerCollectors.add(new IndividualOutcomesCollector(...));
                        break;

                    default:
                        throw new IllegalArgumentException("Unsupported JSONCoreMetricOutput name: " + name);
                }                
            }
        }
        return listenerCollectors;
    }    
    
    public OutputItem<?> buildOutputItem(HTAExperiment exp, boolean baseCase) {
        final PrintWriter out = createWriter();
        final OutputItem<?> outputItem;

        switch(group.getProcessing()) {
            case CSV:
                outputItem = new CSVOutputItem(out);
                break;
            case JSON:
                outputItem = new JsonOutputItem(out);
                break;
            case TXT:
                outputItem = new TxtOutputItem(out);
                break;
            default:
                throw new IllegalStateException("Unsupported processing: " + group.getProcessing());
        }
        for (ListenerCollector listenerBuilder : buildListenerCollectors(exp, baseCase)) {
            outputItem.addListenerCollector(listenerBuilder);
        }
        return outputItem;
    }

    private PrintWriter createWriter() {
        final String outputFileName = group.getFile();
        // "file" is optional in the schema. If not provided or blank, use stdout.
        if (outputFileName == null || outputFileName.isBlank()) {
            return new PrintWriter(System.out);
        }
        try {
            return new PrintWriter(new BufferedWriter(new FileWriter(outputFileName)));
        } catch (IOException e) {
            // If it fails, degrade to stdout
            e.printStackTrace();
            return new PrintWriter(System.out);
        }
    }    
}