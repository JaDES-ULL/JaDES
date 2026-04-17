package es.ull.simulation.hta.osdi.cliargs;

import com.beust.jcommander.Parameter;

public class RootArgs {
        @Parameter(names = {"--help","-h"}, help = true, description = "Show help")
        public boolean help;
}
