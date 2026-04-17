package es.ull.simulation.hta;

public class EpidemiologicOutputFormat {
	/**
	 * Type of epidemiologic information
	 * @author Iván Castilla
	 */
	public enum Type {
		INCIDENCE,
		PREVALENCE,
		CUMUL_INCIDENCE
	}
	private final EpidemiologicOutputFormat.Type type;
	private final boolean absolute;
	private final boolean byAge;
	private final int interval;

	/**
	 * @param type
	 * @param absolute
	 * @param byAge
	 * @param interval
	 */
	private EpidemiologicOutputFormat(EpidemiologicOutputFormat.Type type, boolean absolute, boolean byAge, int interval) {
		this.type = type;
		this.absolute = absolute;
		this.byAge = byAge;
		this.interval = interval;
	}

	public static EpidemiologicOutputFormat build(String format) {
		EpidemiologicOutputFormat.Type type;
		switch (format.charAt(0)) {
		case 'i':
			type = Type.INCIDENCE;
			break;
		case 'p':
			type = Type.PREVALENCE;
			break;
		case 'c':
			type = Type.CUMUL_INCIDENCE;
			break;
		default:
			return null;
		}
		boolean absolute = false;
		if (format.length() > 1) {
			switch (format.charAt(1)) {
			case 'a':
				absolute = true;
				break;
			case 'r':
				absolute = false;
				break;
			default:
				return null;
			}
		}
		boolean byAge = false;
		if (format.length() > 2) {
			switch (format.charAt(2)) {
			case 'a':
				byAge = true;
				break;
			case 't':
				byAge = false;
				break;
			default:
				return null;
			}
		}
		int interval = 1;
		if (format.length() > 3) {
			try {
				interval = Integer.parseInt(format.substring(3));
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return new EpidemiologicOutputFormat(type, absolute, byAge, interval);
	}

	/**
	 * @return the type
	 */
	public EpidemiologicOutputFormat.Type getType() {
		return type;
	}

	/**
	 * @return the absolute
	 */
	public boolean isAbsolute() {
		return absolute;
	}

	/**
	 * @return the byAge
	 */
	public boolean isByAge() {
		return byAge;
	}

	/**
	 * @return the interval
	 */
	public int getInterval() {
		return interval;
	}
}