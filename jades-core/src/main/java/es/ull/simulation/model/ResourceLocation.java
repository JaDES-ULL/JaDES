package es.ull.simulation.model;

import es.ull.simulation.info.EntityLocationInfo;
import es.ull.simulation.model.location.ILocation;
import es.ull.simulation.model.location.Location;
import es.ull.simulation.model.location.IMovable;

/**
 * Manages the location and movement logic for a Resource.
 * This class encapsulates all location-related responsibilities that were previously
 * in the Resource class, following the Single Responsibility Principle.
 *
 * @author Refactoring - 2026
 */
public class ResourceLocation implements IMovable {
    /** The resource this location manager belongs to */
    private final Resource resource;
    /** The current location of the resource */
    private Location currentLocation;
    /** The initial location of the resource */
    private final Location initLocation;
    /** The size of the resource */
    private final int size;
    /** The current element instance that drives the movement of the resource */
    private ElementInstance movingInstance = null;

    /**
     * Creates a location manager for a resource
     * @param resource The resource this manager belongs to
     * @param initLocation The initial location of the resource
     * @param size The size of the resource
     */
    public ResourceLocation(final Resource resource, final Location initLocation, final int size) {
        this.resource = resource;
        this.initLocation = initLocation;
        this.size = size;
        this.currentLocation = null;
    }

    @Override
    public int getCapacity() {
        return size;
    }

    @Override
    public ILocation getLocation() {
        return currentLocation;
    }

    @Override
    public void setLocation(final ILocation location) {
        final Location resolvedLocation = (Location) location;
        final Simulation simul = resource.getSimulation();
        final long ts = resource.getTs();

        if (currentLocation == null) {
            simul.notifyInfo(new EntityLocationInfo(simul, resource, resolvedLocation,
                    EntityLocationInfo.Type.START, ts));
            currentLocation = resolvedLocation;
        } else {
            simul.notifyInfo(new EntityLocationInfo(simul, resource, currentLocation,
                    EntityLocationInfo.Type.LEAVE, ts));
            currentLocation = resolvedLocation;
            simul.notifyInfo(new EntityLocationInfo(simul, resource, currentLocation,
                    EntityLocationInfo.Type.ARRIVE, ts));
        }
    }

    /**
     * Returns the initial location of the resource
     * @return the initial location
     */
    public Location getInitLocation() {
        return initLocation;
    }

    /**
     * Returns the current element instance driving the movement
     * @return the moving instance or null
     */
    public ElementInstance getMovingInstance() {
        return movingInstance;
    }

    /**
     * Sets the element instance that is moving this resource
     * @param movingInstance the element instance
     */
    public void setMovingInstance(final ElementInstance movingInstance) {
        this.movingInstance = movingInstance;
    }

    /**
     * Initializes the location by entering the init location if available
     * @return true if initialization succeeded, false otherwise
     */
    public boolean initialize() {
        if (initLocation != null) {
            if (initLocation.fitsIn(resource)) {
                initLocation.enter(resource);
                return true;
            } else {
                resource.error("Unable to initialize resource. Not enough space in location " +
                        initLocation + " (available: " + initLocation.getAvailableCapacity() +
                        " - required: " + size + ")");
                return false;
            }
        }
        return true;
    }

    @Override
    public void notifyLocationAvailable(final ILocation location) {
        final Location resolvedLocation = (Location) location;
        resolvedLocation.enter(resource);

        if (movingInstance != null) {
            // Delegate to Resource for flow-specific logic
            resource.handleLocationAvailable(resolvedLocation, movingInstance);
        }
    }
}
