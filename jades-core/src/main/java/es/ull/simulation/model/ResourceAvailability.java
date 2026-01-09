package es.ull.simulation.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Manages availability aspects of a Resource, including timetables, cancellation periods,
 * resource types, and timeout status.
 * 
 * This class was extracted from Resource to improve adherence to the Single Responsibility Principle.
 * 
 * @author Iván Castilla Rodríguez
 */
public class ResourceAvailability {
    /** The resource this availability manager belongs to */
    private final Resource resource;
    
    /** Timetable which defines the availability structure of the resource. Define RollOn and RollOff events. */
    private final ArrayList<TimeTableEntry> timeTable = new ArrayList<>();
    
    /** Availability time table. Define CancelPeriodOn and CancelPeriodOff events */
    private final ArrayList<TimeTableEntry> cancelPeriodTable = new ArrayList<>();
    
    /** If true, indicates that this resource is being used after its availability time has expired */
    private boolean timeOut = false;
    
    /** The resource type which this resource is being booked for */
    private ResourceType currentResourceType = null;

    /**
     * Creates a new availability manager for the specified resource
     * @param resource The resource this availability manager belongs to
     */
    public ResourceAvailability(Resource resource) {
        this.resource = resource;
    }

    /**
     * Returns a collection with the timetable defined for this resource.
     * @return a collection with the timetable defined for this resource
     */
    public Collection<TimeTableEntry> getTimeTableEntries() {
        return timeTable;
    }

    /**
     * Returns a collection with the cancellation timetable defined for this resource.
     * @return a collection with the cancellation timetable defined for this resource
     */
    public Collection<TimeTableEntry> getCancellationPeriodEntries() {
        return cancelPeriodTable;
    }

    /**
     * Returns the {@link ResourceType resource type} currently assigned to this resource, in case it is in use.
     * Returns null otherwise.
     * @return Value of property currentResourceType.
     */
    public ResourceType getCurrentResourceType() {
        return currentResourceType;
    }

    /**
     * Assigns a {@link ResourceType resource type} for this resource,
     * whenever it is going to be used by an {@link Element}.
     * @param rt Value of property currentResourceType.
     */
    public void setCurrentResourceType(final ResourceType rt) {
        currentResourceType = rt;
    }

    /**
     * Returns <code>true</code> if this resource is being used in spite of having finished its availability.
     * @return <code>True</code> if this resource is being used in spite of having finished its availability;
     * <code>false</code> otherwise.
     */
    public boolean isTimeOut() {
        return timeOut;
    }

    /**
     * Sets the state of this resource as being used in spite of having finished its availability.
     * @param timeOut <code>True</code> if this resource is being used beyond its availability;
     * <code>false</code> otherwise.
     */
    public void setTimeOut(final boolean timeOut) {
        this.timeOut = timeOut;
    }

    /**
     * Returns a builder class for adding time table or cancellation entries
     * @param roleList The types of this resource during every activation /to be cancelled
     * @return a builder class for adding time table or cancellation entries
     */
    public TimeTableOrCancelEntriesAdder newTimeTableOrCancelEntriesAdder(final ArrayList<ResourceType> roleList) {
        return new TimeTableOrCancelEntriesAdder(roleList);
    }

    /**
     * Returns a builder class for adding time table or cancellation entries
     * @param role The type of this resource during every activation/to be cancelled
     * @return a builder class for adding time table or cancellation entries
     */
    public TimeTableOrCancelEntriesAdder newTimeTableOrCancelEntriesAdder(final ResourceType role) {
        return new TimeTableOrCancelEntriesAdder(role);
    }

    /**
     * A builder class to build time table or cancellation entries. With one builder you can create several entries
     * with the same cycle and duration for one or more resource types. If you don't use the
     * {@link #withDuration(ISimulationCycle, TimeStamp)} method, the entries are assumed to last for the whole
     * duration of the simulation. You can also use the same builder for time table or cancellation entries,
     * by invoking, respectively, {@link #addTimeTableEntry()} or {@link #addCancelEntry()}
     * @author Iván Castilla Rodríguez
     */
    public final class TimeTableOrCancelEntriesAdder {
        private final ArrayList<ResourceType> roleList = new ArrayList<>();
        private ISimulationCycle cycle = null;
        private TimeStamp dur = null;

        /**
         * Creates an entry adder with a single role
         * @param role The type of this resource during every activation/to be cancelled
         */
        public TimeTableOrCancelEntriesAdder(final ResourceType role) {
            roleList.add(role);
        }

        /**
         * Creates an entry adder with multiple concurrent roles
         * @param roleList The types of this resource during every activation /to be cancelled
         */
        public TimeTableOrCancelEntriesAdder(final ArrayList<ResourceType> roleList) {
            this.roleList.addAll(roleList);
        }

        /**
         * Adds a duration and activation/cancellation cycle
         * @param cycle Simulation cycle to define activation/deactivation time
         * @param dur How long the resource is active/will remain inactive
         * @return this builder for method chaining
         */
        public TimeTableOrCancelEntriesAdder withDuration(final ISimulationCycle cycle, final TimeStamp dur) {
            this.cycle = cycle;
            this.dur = dur;
            return this;
        }

        /**
         * Adds a duration and activation/cancellation cycle
         * @param cycle Simulation cycle to define activation/deactivation time
         * @param dur How long the resource is active/will remain inactive
         * @return this builder for method chaining
         */
        public TimeTableOrCancelEntriesAdder withDuration(final ISimulationCycle cycle, final long dur) {
            return this.withDuration(cycle, new TimeStamp(resource.getSimulation().getTimeUnit(), dur));
        }

        /**
         * Creates the time table entry/ies with the specified characteristics
         */
        public void addTimeTableEntry() {
            if (cycle == null) {
                for (final ResourceType role : roleList)
                    timeTable.add(new TimeTableEntry(role));
            }
            else {
                for (final ResourceType role : roleList)
                    timeTable.add(new TimeTableEntry(cycle, dur, role));
            }
        }

        /**
         * Creates the cancellation entry/ies with the specified characteristics
         */
        public void addCancelEntry() {
            if (cycle == null) {
                for (final ResourceType role : roleList)
                    cancelPeriodTable.add(new TimeTableEntry(role));
            }
            else {
                for (final ResourceType role : roleList)
                    cancelPeriodTable.add(new TimeTableEntry(cycle, dur, role));
            }
        }
    }
}
