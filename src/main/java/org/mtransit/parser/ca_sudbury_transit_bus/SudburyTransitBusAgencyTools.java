package org.mtransit.parser.ca_sudbury_transit_bus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.Pair;
import org.mtransit.parser.SplitUtils;
import org.mtransit.parser.SplitUtils.RouteTripSpec;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.gtfs.data.GTripStop;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;
import org.mtransit.parser.mt.data.MTripStop;

// http://opendata.greatersudbury.ca/
// http://opendata.greatersudbury.ca/datasets?q=Transportation&sort_by=relevance
// http://opendata.greatersudbury.ca/datasets/transit-schedule-data-gtfs
// http://www.greatersudbury.ca/image/opendata/gtfs.zip
// https://www.greatersudbury.ca/image/opendata/gtfs.zip
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
		MTLog.log("Generating Sudbury Transit bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this, true);
		super.start(args);
		MTLog.log("Generating Sudbury Transit bus data... DONE in %s.", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean isGoodEnoughAccepted() {
		return true; // TODO ?
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIds != null && this.serviceIds.isEmpty();
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

	private static final Pattern DIGITS = Pattern.compile("[\\d]+");

	@Override
	public long getRouteId(GRoute gRoute) {
		if (!Utils.isDigitsOnly(gRoute.getRouteShortName())) {
			Matcher matcher = DIGITS.matcher(gRoute.getRouteShortName());
			if (matcher.find()) {
				int digits = Integer.parseInt(matcher.group());
				String rsnLC = gRoute.getRouteShortName().toLowerCase(Locale.ENGLISH);
				if (rsnLC.endsWith("r")) {
					return digits + 180_000L;
				}
			}
			MTLog.logFatal("Unexpected route ID for %s!", gRoute);
		}
		return Long.parseLong(gRoute.getRouteShortName()); // use route short name as route ID
	}

	@Override
	public String getRouteLongName(GRoute gRoute) {
		String routeLongName = gRoute.getRouteLongName();
		routeLongName = CleanUtils.cleanSlashes(routeLongName);
		routeLongName = CleanUtils.cleanStreetTypes(routeLongName);
		routeLongName = CleanUtils.cleanNumbers(routeLongName);
		return routeLongName;
	}

	@Override
	public String getRouteColor(GRoute gRoute) {
		if ("000000".equals(gRoute.getRouteColor())) {
			return null;
		}
		return super.getRouteColor(gRoute);
	}

	private static final String AGENCY_COLOR_GREEN = "005941"; // GREEN (from web site CSS)

	private static final String AGENCY_COLOR = AGENCY_COLOR_GREEN;

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static final int OUTBOUND_ID = 1; // like MyBus API, do not change
	private static final int INBOUND_ID = 2; // like MyBus API, do not change
	@SuppressWarnings("unused")
	private static final int LOOP_ID = 3; // like MyBus API, do not change

	private static final String TRANSIT_TERMINAL = "Transit Terminal";
	private static final String NEW_SUDBURY_CENTRE = "New Sudbury Ctr";
	private static final String CONISTON = "Coniston";
	private static final String CAMBRIAN = "Cambrian";
	private static final String DOWNTOWN = "Downtown";
	private static final String DOW = "Dow";
	private static final String LAURENTIAN_UNIVERSITY = "Laurentian U";
	private static final String LAURENTIAN_UNIVERSITY_FR = "U Laurentienne";
	private static final String CAPREOL = "Capreol";
	private static final String GRAVEL_DR = "Gravel Dr";
	private static final String SOUTH_END = "South End";

	private static HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;
	static {
		//noinspection UnnecessaryLocalVariable
		HashMap<Long, RouteTripSpec> map2 = new HashMap<>();
		ALL_ROUTE_TRIPS2 = map2;
	}

	@Override
	public int compareEarly(long routeId, List<MTripStop> list1, List<MTripStop> list2, MTripStop ts1, MTripStop ts2, GStop ts1GStop, GStop ts2GStop) {
		if (ALL_ROUTE_TRIPS2.containsKey(routeId)) {
			return ALL_ROUTE_TRIPS2.get(routeId).compare(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop, this);
		}
		return super.compareEarly(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
	}

	@Override
	public ArrayList<MTrip> splitTrip(MRoute mRoute, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return ALL_ROUTE_TRIPS2.get(mRoute.getId()).getAllTrips();
		}
		return super.splitTrip(mRoute, gTrip, gtfs);
	}

	@Override
	public Pair<Long[], Integer[]> splitTripStop(MRoute mRoute, GTrip gTrip, GTripStop gTripStop, ArrayList<MTrip> splitTrips, GSpec routeGTFS) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS2.get(mRoute.getId()), this);
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, routeGTFS);
	}

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return; // split
		}
		if (isGoodEnoughAccepted()) {
			if (mRoute.getId() == 2L) {
				if ("BARRY DOWNE / CAMBRIAN".equals(gTrip.getTripHeadsign()) //
						|| "BARRY DOWNE / CAMBRIAN TO CAMBRIAN".equals(gTrip.getTripHeadsign()) //
						|| "BARRY DOWNE / CAMBRIAN EXPRESS".equals(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(CAMBRIAN, OUTBOUND_ID);
					return;
				} else if ("2 Barry Downe / Cambrian".equalsIgnoreCase(gTrip.getTripHeadsign()) //
						|| "BARRY DOWNE / CAMBRIAN TO DOWNTOWN".equals(gTrip.getTripHeadsign()) //
						|| "BARRY DOWNE / CAMBRIAN EXPRESS DOWNTOWN".equals(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(DOWNTOWN, INBOUND_ID);
					return;
				}
				MTLog.logFatal("%s: Unexpected trip %s!", mRoute.getId(), gTrip.toStringPlus());
				return;
			}
			if (mRoute.getId() == 11L) {
				if ("DONOVAN/COLLEGE BOREAL TO CAMBRIAN".equals(gTrip.getTripHeadsign()) //
						|| "DONOVAN / COLLEGE BOREAL TO NEW SUDBURY CENTRE".equals(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), OUTBOUND_ID);
					return;
				} else if ("DONOVAN - COLLEGE BOREAL TO DOWNTOWN".equals(gTrip.getTripHeadsign())
						|| "DONOVAN/COLLEGE BOREAL TO DOWNTOWN".equals(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), INBOUND_ID);
					return;
				}
				MTLog.logFatal("%s: Unexpected trip %s!", mRoute.getId(), gTrip.toStringPlus());
				return;
			}
			if (mRoute.getId() == 12L) {
				if (isGoodEnoughAccepted()) {
					if ("SECOND AVENUE TO NEW SUDBURY CENTRE".equals(gTrip.getTripHeadsign())) {
						mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), OUTBOUND_ID);
						return;
					} else if ("SECOND AVENUE TO DOWNTOWN".equals(gTrip.getTripHeadsign())) {
						mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), INBOUND_ID);
						return;
					}
				}
				MTLog.logFatal("%s: Unexpected trip %s!", mRoute.getId(), gTrip.toStringPlus());
				return;
			}
			if (mRoute.getId() == 101L) {
				if ("LIVELY TO NAUGHTON".equalsIgnoreCase(gTrip.getTripHeadsign()) //
						|| "LIVELY TO NAUGHTON VIA MR 24".equals(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), OUTBOUND_ID);
					return;
				} else if ("LIVELY TO DOW".equals(gTrip.getTripHeadsign()) //
						|| "LIVELY TO DOWNTOWN".equals(gTrip.getTripHeadsign()) //
						|| "Naughton to Downtown".equals(gTrip.getTripHeadsign()) //
						|| "LIVELY NAUGHTON TO SOUTH END".equals(gTrip.getTripHeadsign()) //
						|| "LIVELY NAUGHTON TO SOUTH END VIA MR 24".equals(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), INBOUND_ID);
					return;
				}
				MTLog.logFatal("%s: Unexpected trip %s!", mRoute.getId(), gTrip.toStringPlus());
				return;
			}
			if (gTrip.getTripHeadsign().equals("Transit Terminal") //
					|| gTrip.getTripHeadsign().endsWith(" TO DOWNTOWN") //
					|| gTrip.getTripHeadsign().endsWith(" TO NEW SUDBURY") //
					|| gTrip.getTripHeadsign().endsWith("TO NEW SUDBURY CENTRE") //
					|| gTrip.getTripHeadsign().endsWith(" LOCAL TO SOUTH END")) {
				mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), INBOUND_ID);
				return;
			} else {
				mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), OUTBOUND_ID);
				return;
			}
		}
		throw new MTLog.Fatal("%s: Unexpected trip %s!", mRoute.getId(), gTrip.toStringPlus());
	}

	private static final Pattern CLEAN_UNIVERISITY = Pattern.compile("((^|\\W)(laurentian university|LU)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String CLEAN_UNIVERISITY_REPLACEMENT = "$2" + LAURENTIAN_UNIVERSITY + "$4";

	private static final Pattern CLEAN_UNIVERISITY_FR = Pattern.compile("((^|\\W)(universit[e|é] laurentienne|UL)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String CLEAN_UNIVERISITY_FR_REPLACEMENT = "$2" + LAURENTIAN_UNIVERSITY_FR + "$4";

	private static final Pattern SUDBURY_SHOPPING_CENTER = Pattern.compile("(subdury shopping centre)", Pattern.CASE_INSENSITIVE);
	private static final String SUDBURY_SHOPPING_CENTER_REPLACEMENT = "Subdury centre";

	private static final Pattern STARTS_WITH_TO = Pattern.compile("(^.*( to|to) )", Pattern.CASE_INSENSITIVE);

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		// TODO FIXME in sync with Greater Sudbury Transit provider
		if (Utils.isUppercaseOnly(tripHeadsign, true, true)) {
			tripHeadsign = tripHeadsign.toLowerCase(Locale.ENGLISH);
		}
		tripHeadsign = CleanUtils.keepToAndRemoveVia(tripHeadsign);
		tripHeadsign = STARTS_WITH_TO.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY); // New Sudbury Localto New Sudbury Ctr
		tripHeadsign = SUDBURY_SHOPPING_CENTER.matcher(tripHeadsign).replaceAll(SUDBURY_SHOPPING_CENTER_REPLACEMENT);
		tripHeadsign = CLEAN_UNIVERISITY.matcher(tripHeadsign).replaceAll(CLEAN_UNIVERISITY_REPLACEMENT);
		tripHeadsign = CLEAN_UNIVERISITY_FR.matcher(tripHeadsign).replaceAll(CLEAN_UNIVERISITY_FR_REPLACEMENT);
		tripHeadsign = CleanUtils.CLEAN_AND.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		tripHeadsign = CleanUtils.removePoints(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@Override
	public boolean mergeHeadsign(MTrip mTrip, MTrip mTripToMerge) {
		List<String> headsignsValues = Arrays.asList(mTrip.getHeadsignValue(), mTripToMerge.getHeadsignValue());
		if (mTrip.getRouteId() == 11L) {
			if (Arrays.asList( //
					NEW_SUDBURY_CENTRE, //
					CAMBRIAN //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CAMBRIAN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 29L) {
			if (Arrays.asList( //
					"South End", //
					"Martindale" //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Martindale", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 101L) {
			if (Arrays.asList( //
					DOW, //
					SOUTH_END //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SOUTH_END, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 103L) {
			if (Arrays.asList( //
					CONISTON + " / " + TRANSIT_TERMINAL, //
					CONISTON //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CONISTON, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 105L) {
			if (Arrays.asList( //
					"Vly", //
					"Blezard", //
					GRAVEL_DR, //
					CAPREOL //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CAPREOL, mTrip.getHeadsignId());
				return true;
			}
		}
		throw new MTLog.Fatal("%s: Couldn't merge %s and %s!", mTrip.getRouteId(), mTrip, mTripToMerge);
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

	@NotNull
	@Override
	public String getStopCode(GStop gStop) {
		return gStop.getStopId(); // use stop ID as stop code, used by real-time API, do not change
	}

	@Override
	public int getStopId(GStop gStop) {
		return super.getStopId(gStop); // used by real-time API, do not change
	}
}
