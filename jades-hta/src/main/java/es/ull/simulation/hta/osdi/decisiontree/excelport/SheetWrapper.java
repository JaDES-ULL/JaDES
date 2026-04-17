package es.ull.simulation.hta.osdi.decisiontree.excelport;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;

public interface SheetWrapper {

    /**
     * Updates the sheet with the current data. 
     */
    void updateSheet() throws MalformedSimulationModelException, MalformedOSDiModelException, UnsupportedOSDiFeatureException;
}
