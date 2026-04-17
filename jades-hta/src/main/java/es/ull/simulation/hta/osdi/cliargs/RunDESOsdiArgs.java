package es.ull.simulation.hta.osdi.cliargs;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

import es.ull.simulation.hta.HTAArguments;

public class RunDESOsdiArgs {
        @ParametersDelegate
        public HTAArguments htaArguments = new HTAArguments();
        @Parameter(names = { "--osdiPath", "-oP" }, description = "Path (URL or local file) to the file with OSDi individuals", required = true, order = 1)
        public String osdiPath = null;
        @Parameter(names = { "--osdiExperiment", "-oE" }, description = "The name of the OSDi experiment to test", required = true, order = 2)
        public String osdiExperiment = "";
}
