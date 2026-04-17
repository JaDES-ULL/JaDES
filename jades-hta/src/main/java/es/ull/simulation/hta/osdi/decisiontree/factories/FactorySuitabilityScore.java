package es.ull.simulation.hta.osdi.decisiontree.factories;

import java.util.ArrayList;
import java.util.List;

public class FactorySuitabilityScore {
    final static int MAX_SCORE = 100;
    final static int MIN_SCORE = 0;
    final static int MILD_PENALTY = 10;
    final static int MODERATE_PENALTY = 25;
    final static int SEVERE_PENALTY = 100;
    private int score;
    private final List<String> reasons;

    public FactorySuitabilityScore() {
        this.score = MAX_SCORE;
        this.reasons = new ArrayList<>();
    }

    public void addPenalty(String reason, int penalty) {
        this.score -= penalty;
        this.score = Math.max(this.score, MIN_SCORE);
        this.reasons.add(reason + " (PENALTY: " + penalty + ")");
    }

    public void addMildPenalty(String reason) {
        addPenalty(reason, MILD_PENALTY);
    }

    public void addModeratePenalty(String reason) {
        addPenalty(reason, MODERATE_PENALTY);
    }

    public void addSeverePenalty(String reason) {
        addPenalty(reason, SEVERE_PENALTY);
    }

    public int getScore() {
        return score;
    }

    public List<String> getReasons() {
        return reasons;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SuitabilityScore [score=").append(score).append(", reasons=").append(reasons).append("]");
        return sb.toString();
    }
}