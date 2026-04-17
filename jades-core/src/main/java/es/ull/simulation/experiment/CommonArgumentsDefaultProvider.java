package es.ull.simulation.experiment;

import com.beust.jcommander.IDefaultProvider;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides default values for common experiment arguments handled with JCommander.
 */
public final class CommonArgumentsDefaultProvider implements IDefaultProvider {
    /**
     * Mapping of argument names to their default values.
     */
    private final Map<String, String> defaults = new HashMap<>();

    public CommonArgumentsDefaultProvider() {
        defaults.put("--runs", "" + IExperiment.DEFAULT_RUNS);
        defaults.put("--seed", "" + IExperiment.getSeed());
        defaults.put("--horizon", "" + IExperiment.DEFAULT_TIME_HORIZON);
        defaults.put("--nthreads", "" + IExperiment.DEFAULT_N_THREADS);
        defaults.put("--parallel", "false");
    }

    @Override
    public String getDefaultValueFor(String optionName) {
        return defaults.get(optionName);
    }
}
