package es.ull.simulation.hta.osdi.decisiontree;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.decisiontree.excelport.HTAExcelModelFactory;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.EffectWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;

public class ModelVisualizer {
    private final Model model;
    private final StringWriter writer;
    private final PrintWriter out;

    public ModelVisualizer(Model model) throws MalformedOSDiModelException, MalformedSimulationModelException, UnsupportedOSDiFeatureException {
        if (model == null) {
            throw new IllegalArgumentException("The model cannot be null.");
        }
        this.model = model;
        this.writer = new StringWriter();
        this.out = new PrintWriter(writer);
        render();
    }

    public String print() {
        this.out.flush();
        return this.writer.toString();
    }
    
    private void render() throws MalformedOSDiModelException, MalformedSimulationModelException, UnsupportedOSDiFeatureException {
        // Implement visualization logic here
        // This could involve generating a graphical representation of the decision tree,
        // exporting it to a file, or displaying it in a user interface.
        // For now, we will just print the model structure to the console.
        out.println("Visualizing HTA Decision Tree Model: " + model.getExperimentWrapper().getWorkingModelIRI().getShortForm() + " (" + model.getExperimentWrapper().getShortName() + ")");
        final ChoiceNode rootNode = model.getTree();
        for (BranchDestinationNode node : rootNode.getSuccessors()) {
            Set<EffectWrapper> effects = node.getEffects();
            out.println("\tINTERVENTION: " + node.name());
            if (!node.isPayoff()) {
                printProbabilityNode(node, "\t\t", effects);
            } else {
                printPayoffNode(node, "\t\t", effects);
            } 
        }
    }

    private void printCostParameters(ArrayList<ParameterWrapper> costParameters, String indentationString) {
        if (costParameters.size() == 1) {
            out.println(indentationString + "COST: " + costParameters.get(0));
        }
        else if (costParameters.size() > 1) {
            out.print(indentationString + "COST: SUM OF ( " );
            for (ParameterWrapper costParam : costParameters) {
                out.print("(" + costParam + ") ");
            }
            out.println(")");
        }
    }

    private void printDisutilityParameters(Set<ParameterWrapper> disutilityParameters, String indentationString) {
        if (disutilityParameters.size() == 1) {
            out.println(indentationString + "DISUTILITY: " + disutilityParameters.iterator().next());
        }
        else if (disutilityParameters.size() > 1) {
            out.print(indentationString + "DISUTILITY: COMBINE ( " );
            for (ParameterWrapper disutilityParam : disutilityParameters) {
                out.print("(" + disutilityParam + ") ");
            }
            out.println(")");
        }
    }

    /**
     * Prints the structure of a probability node and its branches.
     *
     * @param node The probability node to print.
     * @param indentationString The string used for indentation in the output.
     * @return true if the default branch requires a fix, false otherwise. This happends when any of the branches is not deterministic.
     */
    private void printProbabilityNode(BranchDestinationNode node, String indentationString, Set<EffectWrapper> parentEffects) {
        final ArrayList<BranchDestinationNode> successors = node.getSuccessors();
        printCostParameters(node.getCostParameters(), indentationString);
        printDisutilityParameters(node.getDisutilityParameters(), indentationString);
        Set<String> nonDefaultParams = new TreeSet<>();
        for (BranchDestinationNode successor : successors) {
            Set<EffectWrapper> effects = successor.getEffects();
            effects.addAll(parentEffects);
            String txtFormula = HTAExcelModelFactory.getParameterTextFormula(successor.getProbability(), effects);
            out.println(indentationString + successor.name() + "(" + txtFormula + ")");
            nonDefaultParams.add(txtFormula);
            if (!successor.isPayoff()) {
                printProbabilityNode(successor, indentationString + "\t", effects);
            } else {
                printPayoffNode(successor, indentationString + "\t", effects);
            }
        }
        if (node.getDefaultSuccessor() != null) {
            out.println(indentationString + node.getDefaultSuccessor().name() + "(1 - (" + nonDefaultParams + "))");
            parentEffects.addAll(node.getDefaultSuccessor().getEffects());
            if (!node.getDefaultSuccessor().isPayoff()) {
                printProbabilityNode(node.getDefaultSuccessor(), indentationString + "\t", parentEffects);
            } else {
                printPayoffNode(node.getDefaultSuccessor(), indentationString + "\t", parentEffects);
            }
        }
    }

    private void printPayoffNode(BranchDestinationNode payoff, String indentationString, Set<EffectWrapper> parentEffects) {
        out.println(indentationString + "PAYOFF: " + payoff.name());
        printCostParameters(payoff.getCostParameters(), indentationString + "\t");
        printDisutilityParameters(payoff.getDisutilityParameters(), indentationString + "\t");
    }
}
