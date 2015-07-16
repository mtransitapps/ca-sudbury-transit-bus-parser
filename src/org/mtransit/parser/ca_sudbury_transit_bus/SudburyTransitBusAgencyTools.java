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
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MInboundType;
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
		return Long.parseLong(gRoute.route_short_name);
	}

	@Override
	public String getRouteShortName(GRoute gRoute) {
		return super.getRouteShortName(gRoute); // used by real-time API, do not change
	}

	@Override
	public String getRouteLongName(GRoute gRoute) {
		String routeLongName = gRoute.route_long_name;
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

	private static final String INBOUND_LC = "inbound";
	private static final String OUTBOUND_LC = "outbound";
	private static final String LOOP_LC = "loop";

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		String tripHeadsignLC = gTrip.trip_headsign.toLowerCase(Locale.ENGLISH);
		if (INBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
			mTrip.setHeadsignInbound(MInboundType.INBOUND);
			return;
		} else if (OUTBOUND_LC.equalsIgnoreCase(tripHeadsignLC)) {
			mTrip.setHeadsignInbound(MInboundType.OUTBOUND);
			return;
		}
		if (LOOP_LC.equalsIgnoreCase(tripHeadsignLC)) {
			mTrip.setHeadsignString(gTrip.trip_headsign, 0);
			return;
		}
		System.out.printf("\n%s: Unexpected trip %s!\n", mRoute.id, gTrip);
		System.exit(-1);
	}

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final Pattern CLEAN_UNIVERISITY = Pattern.compile("((^|\\W){1}(laurentian university)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String CLEAN_UNIVERISITY_REPLACEMENT = "$2Laurentian U$4";

	private static final Pattern CLEAN_UNIVERISITY_FR = Pattern.compile("((^|\\W){1}(universit[e|é] laurentienne)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String CLEAN_UNIVERISITY_FR_REPLACEMENT = "$2U Laurentienne$4";

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = CLEAN_UNIVERISITY.matcher(gStopName).replaceAll(CLEAN_UNIVERISITY_REPLACEMENT);
		gStopName = CLEAN_UNIVERISITY_FR.matcher(gStopName).replaceAll(CLEAN_UNIVERISITY_FR_REPLACEMENT);
		gStopName = CleanUtils.removePoints(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}
}
