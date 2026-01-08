/**
 * 
 */
package es.ull;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

import es.ull.simulation.info.IPieceOfInformation;
import es.ull.simulation.info.ResourceInfo;
import es.ull.simulation.info.ResourceUsageInfo;
import es.ull.simulation.info.SimulationStartStopInfo;
import es.ull.simulation.inforeceiver.BasicListener;
import es.ull.simulation.model.ResourceType;

/**
 * @author Iván Castilla Rodríguez
 *
 */
public class CheckResourcesListener extends BasicListener {
	private final static String ERROR_ROLON1 = "Wrong activation time of resource role";
	private final static String ERROR_ROLON2 = "Resource activating for unexpected role";
	private final static String ERROR_ROLON3 = "Resource never activated for role";
	private final static String ERROR_ROLOFF1 = "Wrong deactivation time of resource role";
	private final static String ERROR_ROLOFF2 = "Resource deactivating for unexpected role";
	private final static String ERROR_ROLOFF3 = "Resource never deactivated for role";
	private final static String ERROR_RESCREATED = "Not all the resources were created";
	private final static String ERROR_RESFINISHED = "Not all the resources were finished";
	private final static String ERROR_SEIZE = "Resource already seized";
	private final static String ERROR_RELEASE = "Resource not previously seized";
	private final int resources;
	private int resCreated;
	private int resFinished;
	private boolean []inUse;
	private final ArrayList<ResourceUsageTimeStamps> roleOns;
	private final ArrayList<ResourceUsageTimeStamps> roleOffs;

	public CheckResourcesListener(final ArrayList<ResourceUsageTimeStamps> roleOns, final ArrayList<ResourceUsageTimeStamps> roleOffs) {
		super("Resource checker");
		final TreeSet<Integer> resIds = new TreeSet<Integer>();
		for (ResourceUsageTimeStamps r : roleOns) {
			resIds.add(r.getResId());
		}
		for (ResourceUsageTimeStamps r : roleOffs) {
			resIds.add(r.getResId());
		}
		this.resources = resIds.size();
		this.resCreated = 0;
		this.resFinished = 0;
		this.inUse = new boolean[resources];
		Arrays.fill(inUse, false);
		this.roleOffs = roleOffs;
		this.roleOns = roleOns;
		addTargetInformation(ResourceInfo.class);
		addTargetInformation(ResourceUsageInfo.class);
	}

	private ResourceUsageTimeStamps find(ArrayList<ResourceUsageTimeStamps> usages, final int resId, final int roleId) {
		for (final ResourceUsageTimeStamps r : usages) {
			if (r.getResId() == resId && r.getRoleId() == roleId)
				return r;
		}
		return null;
	}

	@Override
	public void infoEmited(final IPieceOfInformation info) {
		if (info instanceof ResourceInfo) {
			final ResourceInfo rInfo = (ResourceInfo)info;
			final int resId = rInfo.getResource().getIdentifier();
			final ResourceType rt = rInfo.getResourceType();
			final int roleId = (rt != null) ? rt.getIdentifier() : -1;
			switch(rInfo.getType()) {
			case ROLON:
				final ResourceUsageTimeStamps rOn = find(roleOns, resId, roleId);
				assertTrue(rOn != null, rInfo.getResource().toString() + "\t" + ERROR_ROLON2 + "\t" + rInfo.getResourceType().toString());
				if (rOn != null)
					assertEquals(rInfo.getTs(),  rOn.getNextValidTimeStamp(), rInfo.getResource().toString() + "\t" + ERROR_ROLON1);
				rOn.check();
				resCreated++;
				break;
			case ROLOFF:
				final ResourceUsageTimeStamps rOff = find(roleOffs, resId, roleId);
				assertTrue(rOff != null, rInfo.getResource().toString() + "\t" + ERROR_ROLOFF2 + "\t" + rInfo.getResourceType().toString());
				if (rOff != null)
					assertEquals(rInfo.getTs(),  rOff.getNextValidTimeStamp(), rInfo.getResource().toString() + "\t" + ERROR_ROLOFF1);
				rOff.check();
				resFinished++;
				break;
			default:
				break;
			}
		}
		else if (info instanceof ResourceUsageInfo) {
			final ResourceUsageInfo rInfo = (ResourceUsageInfo)info;
			final int resId = rInfo.getResource().getIdentifier();
			switch(rInfo.getType()) {
				case CAUGHT:
					assertFalse(inUse[resId], rInfo.getResource().toString() + "\t" + ERROR_SEIZE);
					inUse[resId] = true;
					break;
				case RELEASED:
					assertTrue(inUse[resId], rInfo.getResource().toString() + "\t" + ERROR_RELEASE);
					inUse[resId] = false;
					break;
				default:
					break;			
			}
		}
		else if (info instanceof SimulationStartStopInfo) {
			final SimulationStartStopInfo tInfo = (SimulationStartStopInfo) info;
			if (SimulationStartStopInfo.Type.END.equals(tInfo.getType()))  {
				assertEquals(resCreated, resources, ERROR_RESCREATED);
				assertEquals(resFinished, resources, ERROR_RESFINISHED);
				for (ResourceUsageTimeStamps r : roleOns) {
					assertEquals(r.getNextValidTimeStamp(), -1, ERROR_ROLON3 + "\t" + r.getResId() + "\t" + r.getRoleId());
				}
				for (ResourceUsageTimeStamps r : roleOffs) {
					assertEquals(r.getNextValidTimeStamp(), -1, ERROR_ROLOFF3 + "\t" + r.getResId() + "\t" + r.getRoleId());
				}

			}
		}
	}

	public static class ResourceUsageTimeStamps {
		private final int resId;
		private final long[] timeStamps;
		private final int roleId;
		private int checked;

		public ResourceUsageTimeStamps(final int resId, final int roleId, final long[] timeStamps) {
			this.resId = resId;
			this.roleId = roleId;
			this.timeStamps = timeStamps;
			this.checked = -1;
		}
		public ResourceUsageTimeStamps(final int resId, final int roleId, long timeStamp) {
			this(resId, roleId, new long[] {timeStamp});
		}

		public int getResId() {
			return resId;
		}
		public long getNextValidTimeStamp() {
			if (checked == timeStamps.length - 1)
				return -1;
			return timeStamps[checked + 1];
		}
		public int getRoleId() {
			return roleId;
		}
		public void check() {
			this.checked++;
		}
	}
}
