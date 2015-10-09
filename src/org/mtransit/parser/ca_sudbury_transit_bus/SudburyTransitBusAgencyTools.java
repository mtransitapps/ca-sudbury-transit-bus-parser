package org.mtransit.parser.ca_sudbury_transit_bus;

import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Pattern;

import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;

// http://opendata.greatersudbury.ca/
// http://opendata.greatersudbury.ca/datasets?q=Transportation&sort_by=relevance
// http://www.greatersudbury.ca/image/opendata/gtfs.zip
public class SudburyTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-sudbury-transit-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new SudburyTransitBusAgencyTools().start(args);
	}

	private HashSet<String> serviceIds;

	@Override
	public void start(String[] args) {
		System.out.printf("\nGenerating Sudbury Transit bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this);
		super.start(args);
		System.out.printf("\nGenerating Sudbury Transit bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludeCalendar(GCalendar gCalendar) {
		if (this.serviceIds != null) {
			return excludeUselessCalendar(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(GCalendarDate gCalendarDates) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarDate(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (this.serviceIds != null) {
			return excludeUselessTrip(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public long getRouteId(GRoute gRoute) {
		return Long.parseLong(gRoute.getRouteShortName()); // use route short name as route ID
	}

	@Override
	public String getRouteShortName(GRoute gRoute) {
		return super.getRouteShortName(gRoute); // used by real-time API, do not change
	}

	@Override
	public String getRouteLongName(GRoute gRoute) {
		String routeLongName = gRoute.getRouteLongName();
		routeLongName = CleanUtils.cleanSlashes(routeLongName);
		routeLongName = CleanUtils.cleanStreetTypes(routeLongName);
		routeLongName = CleanUtils.cleanNumbers(routeLongName);
		return routeLongName;
	}

	private static final String AGENCY_COLOR_GREEN = "005941"; // GREEN (from web site CSS)

	private static final String AGENCY_COLOR = AGENCY_COLOR_GREEN;

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static final int OUTBOUND_ID = 1; // like MyBus API, do not change
	private static final int INBOUND_ID = 2; // like MyBus API, do not change
	private static final int LOOP_ID = 3; // like MyBus API, do not change

	private static final String INBOUND_LC = "inbound";
	private static final String OUTBOUND_LC = "outbound";
	private static final String LOOP_LC = "loop";

	private static final String TRANSIT_TERMINAL = "Transit Terminal";
	private static final String NEW_SUBDURY_SHOPPING_CENTRE = "New Subdury Ctr";
	private static final String SPRUCE_ST = "Spruce St";
	private static final String PIONEER_MANOR = "Pioneer Manor";
	private static final String MC_NEIL_BLVD = "McNeil Blvd";
	private static final String COLLEGE_BOREAL = "College Boreal";
	private static final String TAXATION_CENTRE = "Taxation Ctr";
	private static final String BURTON_AVE = "Burton Ave";
	private static final String BANCROFT_AND_MOONLIGHT = "Bancroft & Moonlight";
	private static final String CONISTON_TRANSIT_TERMINAL = "Coniston / Transit Terminal";
	private static final String HAWTHORNE_BARRYDOWNE = "Hawthorne @ Barrydowne";
	private static final String CAM_ST = "Cam St";
	private static final String LANSING = "Lansing";
	private static final String TRANSIT_TERMINAL_NEW_SUDBURY = "Transit Terminal / New Sudbury";
	private static final String CAMBRIAN_COLLEGE = "Cambrian College";
	private static final String PEPERTREE_VILLAGE = "Pepertree Vlg";
	private static final String LAURENTIAN_UNIVERSITY = "Laurentian U";
	private static final String LAURENTIAN_UNIVERSITY_FR = "U Laurentienne";
	private static final String ALGOMA_HOSPITAL = "Algoma Hosp";
	private static final String LIVELY = "Lively";
	private static final String CHELMSFORD = "Chelmsford";
	private static final String CAPREOL = "Capreol";
	private static final String GRAVELLE_DR_TRAILER_PARK = "Gravelle Dr Trailer Pk";
	private static final String WALMART_SOUTH_END = "Walmart South End";
	private static final String COPPER_CLIFF = "Copper Clf";
	private static final String FALCONBRIDGE_TRANSIT_TERMINAL = "Falconbridge / Transit Terminal";
	private static final String LASALLE_PEPERTREE = "Lasalle / Pepertree";
	private static final String UNIVERSITY = "University";

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		String tripHeadsignLC = gTrip.getTripHeadsign().toLowerCase(Locale.ENGLISH);
		if (mRoute.getId() == 2l) { // 002
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(NEW_SUBDURY_SHOPPING_CENTRE, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 6l) { // 006
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(SPRUCE_ST, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 7l) { // 007
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(PIONEER_MANOR, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 12l) { // 012
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(MC_NEIL_BLVD, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 14l) { // 014
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(COLLEGE_BOREAL, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 15l) { // 015
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TAXATION_CENTRE, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 17l) { // 017
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(BURTON_AVE, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 101l) {
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(BANCROFT_AND_MOONLIGHT, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 102l) {
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(BANCROFT_AND_MOONLIGHT, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 103l) {
			if (LOOP_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(CONISTON_TRANSIT_TERMINAL, LOOP_ID);
				return;
			}
		} else if (mRoute.getId() == 141l) {
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(HAWTHORNE_BARRYDOWNE, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 142l) {
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(HAWTHORNE_BARRYDOWNE, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 147l) {
			if (LOOP_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, LOOP_ID);
				return;
			}
		} else if (mRoute.getId() == 181l) {
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(CAM_ST, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 182l) {
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(CAM_ST, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 189l) {
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(WALMART_SOUTH_END, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 241l) {
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(BANCROFT_AND_MOONLIGHT, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 300l) {
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(PEPERTREE_VILLAGE, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 301l) {
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(PEPERTREE_VILLAGE, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 302l) {
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(LANSING, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 303l) {
			if (LOOP_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(FALCONBRIDGE_TRANSIT_TERMINAL, LOOP_ID);
				return;
			}
		} else if (mRoute.getId() == 304l) {
			// split 304 ? NO! would not match real-time MyBus API
			if (LOOP_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL_NEW_SUDBURY, LOOP_ID);
				return;
			}
		} else if (mRoute.getId() == 305l) {
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(LASALLE_PEPERTREE, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 400l) {
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(CAMBRIAN_COLLEGE, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 401l) {
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(CAMBRIAN_COLLEGE, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 402l) {
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(CAMBRIAN_COLLEGE, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 403l) {
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(PEPERTREE_VILLAGE, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 500l) {
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(LAURENTIAN_UNIVERSITY, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 501l) {
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(ALGOMA_HOSPITAL, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 502l) {
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(ALGOMA_HOSPITAL, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 503l) {
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(UNIVERSITY, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(WALMART_SOUTH_END, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 640l) {
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(COPPER_CLIFF, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 701l) {
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(LIVELY, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 702l) {
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(CHELMSFORD, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 703l) {
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(CAPREOL, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 704l) {
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(GRAVELLE_DR_TRAILER_PARK, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 819l) {
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(WALMART_SOUTH_END, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 940l) {
			if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
				mTrip.setHeadsignString(COPPER_CLIFF, OUTBOUND_ID);
				return;
			}
		}
		System.out.printf("\n%s: Unexpected trip %s!\n", mRoute.getId(), gTrip);
		System.exit(-1);
	}

	private static final Pattern CLEAN_UNIVERISITY = Pattern.compile("((^|\\W){1}(laurentian university)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String CLEAN_UNIVERISITY_REPLACEMENT = "$2" + LAURENTIAN_UNIVERSITY + "$4";

	private static final Pattern CLEAN_UNIVERISITY_FR = Pattern.compile("((^|\\W){1}(universit[e|é] laurentienne)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String CLEAN_UNIVERISITY_FR_REPLACEMENT = "$2" + LAURENTIAN_UNIVERSITY_FR + "$4";

	private static final Pattern SUDBURY_SHOPPING_CENTER = Pattern.compile("(subdury shopping centre)", Pattern.CASE_INSENSITIVE);
	private static final String SUDBURY_SHOPPING_CENTER_REPLACEMENT = "Subdury centre";

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = SUDBURY_SHOPPING_CENTER.matcher(tripHeadsign).replaceAll(SUDBURY_SHOPPING_CENTER_REPLACEMENT);
		tripHeadsign = CLEAN_UNIVERISITY.matcher(tripHeadsign).replaceAll(CLEAN_UNIVERISITY_REPLACEMENT);
		tripHeadsign = CLEAN_UNIVERISITY_FR.matcher(tripHeadsign).replaceAll(CLEAN_UNIVERISITY_FR_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		tripHeadsign = CleanUtils.removePoints(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = CLEAN_UNIVERISITY.matcher(gStopName).replaceAll(CLEAN_UNIVERISITY_REPLACEMENT);
		gStopName = CLEAN_UNIVERISITY_FR.matcher(gStopName).replaceAll(CLEAN_UNIVERISITY_FR_REPLACEMENT);
		gStopName = CleanUtils.removePoints(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	@Override
	public String getStopCode(GStop gStop) {
		return gStop.getStopId(); // use stop ID as stop code, used by real-time API, do not change
	}

	@Override
	public int getStopId(GStop gStop) {
		return super.getStopId(gStop); // used by real-time API, do not change
	}
}
