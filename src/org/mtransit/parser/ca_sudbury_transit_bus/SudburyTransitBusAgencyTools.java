package org.mtransit.parser.ca_sudbury_transit_bus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
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
		System.out.printf("\nGenerating Sudbury Transit bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this);
		super.start(args);
		System.out.printf("\nGenerating Sudbury Transit bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
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

	@Override
	public long getRouteId(GRoute gRoute) {
		return Long.parseLong(gRoute.getRouteShortName()); // use route short name as route ID
	}

	@Override
	public String getRouteShortName(GRoute gRoute) {
		if (Utils.isDigitsOnly(gRoute.getRouteShortName())) {
			return String.format("%03d", Integer.parseInt(gRoute.getRouteShortName())); // used by real-time API, do not change
		}
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
	private static final int LOOP_ID = 3; // like MyBus API, do not change

	private static final String _AND_ = " & ";
	private static final String TRANSIT_TERMINAL = "Transit Terminal";
	private static final String NEW_SUBDURY_SHOPPING_CENTRE = "New Subdury Ctr";
	private static final String SPRUCE_ST = "Spruce St";
	private static final String PIONEER_MANOR = "Pioneer Manor";
	private static final String MC_NEIL_BLVD = "McNeil Blvd";
	private static final String COLLEGE_BOREAL = "College Boreal";
	private static final String TAXATION_CENTRE = "Taxation Ctr";
	private static final String BURTON_AVE = "Burton Ave";
	private static final String BANCROFT_AND_MOONLIGHT = "Bancroft" + _AND_ + "Moonlight";
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

	private static HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;
	static {
		HashMap<Long, RouteTripSpec> map2 = new HashMap<Long, RouteTripSpec>();
		map2.put(2l, new RouteTripSpec(2l, //
				INBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, TRANSIT_TERMINAL, //
				OUTBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, NEW_SUBDURY_SHOPPING_CENTRE) //
				.addTripSort(INBOUND_ID, //
						Arrays.asList(new String[] { //
						"6940", "1000" //
						})) //
				.addTripSort(OUTBOUND_ID, //
						Arrays.asList(new String[] { //
						"1000", "6940" //
						})) //
				.compileBothTripSort());
		map2.put(6l, new RouteTripSpec(6l, //
				INBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, TRANSIT_TERMINAL, //
				OUTBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, SPRUCE_ST) //
				.addTripSort(INBOUND_ID, //
						Arrays.asList(new String[] { //
						"1330", "1000" //
						})) //
				.addTripSort(OUTBOUND_ID, //
						Arrays.asList(new String[] { //
						"1000", "1330" //
						})) //
				.compileBothTripSort());
		map2.put(7l, new RouteTripSpec(7l, //
				INBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, TRANSIT_TERMINAL, //
				OUTBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, PIONEER_MANOR) //
				.addTripSort(INBOUND_ID, //
						Arrays.asList(new String[] { //
						"1791", "1000" //
						})) //
				.addTripSort(OUTBOUND_ID, //
						Arrays.asList(new String[] { //
						"1000", "1791" //
						})) //
				.compileBothTripSort());
		map2.put(12l, new RouteTripSpec(12l, //
				INBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, TRANSIT_TERMINAL, //
				OUTBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, MC_NEIL_BLVD) //
				.addTripSort(INBOUND_ID, //
						Arrays.asList(new String[] { //
						"1395", "1000" //
						})) //
				.addTripSort(OUTBOUND_ID, //
						Arrays.asList(new String[] { //
						"1000", "1395" //
						})) //
				.compileBothTripSort());
		map2.put(14l, new RouteTripSpec(14l, //
				INBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, TRANSIT_TERMINAL, //
				OUTBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, COLLEGE_BOREAL) //
				.addTripSort(INBOUND_ID, //
						Arrays.asList(new String[] { //
						"1900", "1000" //
						})) //
				.addTripSort(OUTBOUND_ID, //
						Arrays.asList(new String[] { //
						"1000", "1900" //
						})) //
				.compileBothTripSort());
		map2.put(15l, new RouteTripSpec(15l, //
				INBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, TRANSIT_TERMINAL, //
				OUTBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, TAXATION_CENTRE) //
				.addTripSort(INBOUND_ID, //
						Arrays.asList(new String[] { //
						"1805", "1000" //
						})) //
				.addTripSort(OUTBOUND_ID, //
						Arrays.asList(new String[] { //
						"1000", "1805" //
						})) //
				.compileBothTripSort());
		map2.put(17l, new RouteTripSpec(17l, //
				INBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, TRANSIT_TERMINAL, //
				OUTBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, BURTON_AVE) //
				.addTripSort(INBOUND_ID, //
						Arrays.asList(new String[] { //
						"1530", "1000" //
						})) //
				.addTripSort(OUTBOUND_ID, //
						Arrays.asList(new String[] { //
						"1000", "1515", "1530" //
						})) //
				.compileBothTripSort());
		map2.put(101l, new RouteTripSpec(101l, //
				INBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, TRANSIT_TERMINAL, //
				OUTBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, BANCROFT_AND_MOONLIGHT) //
				.addTripSort(INBOUND_ID, //
						Arrays.asList(new String[] { //
						"4635", "1000" //
						})) //
				.addTripSort(OUTBOUND_ID, //
						Arrays.asList(new String[] { //
						"1000", "4640", "4635" //
						})) //
				.compileBothTripSort());
		map2.put(102l, new RouteTripSpec(102l, //
				INBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, TRANSIT_TERMINAL, //
				OUTBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, BANCROFT_AND_MOONLIGHT) //
				.addTripSort(INBOUND_ID, //
						Arrays.asList(new String[] { //
						"4635", "4310", "1000" //
						})) //
				.addTripSort(OUTBOUND_ID, //
						Arrays.asList(new String[] { //
						"1000", "4640", "4635" //
						})) //
				.compileBothTripSort());
		map2.put(103l, new RouteTripSpec(103l, //
				LOOP_ID, MTrip.HEADSIGN_TYPE_STRING, CONISTON_TRANSIT_TERMINAL, //
				0, MTrip.HEADSIGN_TYPE_STRING, "") //
				.addTripSort(LOOP_ID, //
						Arrays.asList(new String[] { //
						"1000", // Transit Terminal
								"4335", // != McDonalds
								"4356", // != Costco
								"6600", // == Westmount
								"6770", // == 650 Barrydowne
								"6785", // ==
								"6915", // ==
								"6925", // ==
								"6940", // == New Sudbury Centre
								"6935", // != Shoppers Drug Mart
								"6940", // New Sudbury Centre
								"4690", // 3147 Bancroft ------------------------
								"4735", // Amanda
								"4530", // == Third Ave.
								"4525", // != Third & Kingswa
								"6600", // == Westmount
								"6770", // == 650 Barrydowne
								"6785", // ==
								"6915", // ==
								"6925", // ==
								"6940", // == New Sudbury Centre
								"4330", "1000", // != McDonalds / Transit Terminal
								"4475", "4310" // != Third & Kenwood / 2nd & Bancroft
						})) //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						})) //
				.compileBothTripSort());
		map2.put(141L, new RouteTripSpec(141L, //
				INBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, TRANSIT_TERMINAL, //
				OUTBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, HAWTHORNE_BARRYDOWNE) //
				.addTripSort(INBOUND_ID, //
						Arrays.asList(new String[] { //
						"6925", // New Sudbury Centre
								"1000", // Transit Terminal
						})) //
				.addTripSort(OUTBOUND_ID, //
						Arrays.asList(new String[] { //
						"1000", // Transit Terminal
								"6760", // Barrydowne
								"6925", // New Sudbury Centre
								"6930", // Shoppers Drug Mart
						})) //
				.compileBothTripSort());
		map2.put(142l, new RouteTripSpec(142l, //
				INBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, TRANSIT_TERMINAL, //
				OUTBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, HAWTHORNE_BARRYDOWNE) //
				.addTripSort(INBOUND_ID, //
						Arrays.asList(new String[] { //
						"6590", "1000" //
						})) //
				.addTripSort(OUTBOUND_ID, //
						Arrays.asList(new String[] { //
						"1000", "6590" //
						})) //
				.compileBothTripSort());
		map2.put(147l, new RouteTripSpec(147l, //
				LOOP_ID, MTrip.HEADSIGN_TYPE_STRING, TRANSIT_TERMINAL, //
				0, MTrip.HEADSIGN_TYPE_STRING, "") //
				.addTripSort(LOOP_ID, //
						Arrays.asList(new String[] { //
						"1000", // Transit Terminal
								"1002", //
								"1530", // Burton
								"1055", //
								"1000", // Transit Terminal
								"1050", //
								"1695", // 276 Kathleen
								"1045", //
								"1000" // Transit Terminal
						})) //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						})) //
				.compileBothTripSort());
		map2.put(181l, new RouteTripSpec(181l, //
				INBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, TRANSIT_TERMINAL, //
				OUTBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, CAM_ST) //
				.addTripSort(INBOUND_ID, //
						Arrays.asList(new String[] { //
						"6195", "6200", "1000" //
						})) //
				.addTripSort(OUTBOUND_ID, //
						Arrays.asList(new String[] { //
						"1000", "6010", "6200" //
						})) //
				.compileBothTripSort());
		map2.put(182l, new RouteTripSpec(182l, //
				INBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, TRANSIT_TERMINAL, //
				OUTBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, CAM_ST) //
				.addTripSort(INBOUND_ID, //
						Arrays.asList(new String[] { //
						"6195", "6025", "1000" //
						})) //
				.addTripSort(OUTBOUND_ID, //
						Arrays.asList(new String[] { //
						"1000", "6005" //
						})) //
				.compileBothTripSort());
		map2.put(189l, new RouteTripSpec(189l, //
				INBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, TRANSIT_TERMINAL, //
				OUTBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, WALMART_SOUTH_END) //
				.addTripSort(INBOUND_ID, //
						Arrays.asList(new String[] { //
						"5830", "1000" //
						})) //
				.addTripSort(OUTBOUND_ID, //
						Arrays.asList(new String[] { //
						"1000", "5830" //
						})) //
				.compileBothTripSort());
		map2.put(241l, new RouteTripSpec(241l, //
				INBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, TRANSIT_TERMINAL, //
				OUTBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, BANCROFT_AND_MOONLIGHT) //
				.addTripSort(INBOUND_ID, //
						Arrays.asList(new String[] { //
						"4635", "1000" //
						})) //
				.addTripSort(OUTBOUND_ID, //
						Arrays.asList(new String[] { //
						"1000", "4635" //
						})) //
				.compileBothTripSort());
		map2.put(300l, new RouteTripSpec(300l, //
				INBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, TRANSIT_TERMINAL, //
				OUTBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, PEPERTREE_VILLAGE) //
				.addTripSort(INBOUND_ID, //
						Arrays.asList(new String[] { //
						"7300", // Pepertree Village
								"7180", // == Graywood
								"7320", "7520", // !=
								"7185", // !=
								"6585", // New Sudbury Centre
								"1000" // Transit Terminal
						})) //
				.addTripSort(OUTBOUND_ID, //
						Arrays.asList(new String[] { //
						"1000", "7300" //
						})) //
				.compileBothTripSort());
		map2.put(301l, new RouteTripSpec(301l, //
				INBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, TRANSIT_TERMINAL, //
				OUTBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, PEPERTREE_VILLAGE) //
				.addTripSort(INBOUND_ID, //
						Arrays.asList(new String[] { //
						"7300",// Pepertree Village
								"7015", // Auger & Lasalle
								"6585", // New Sudbury Centre
								"1000" // Transit Terminal
						})) //
				.addTripSort(OUTBOUND_ID, //
						Arrays.asList(new String[] { //
						"1000", "7300" //
						})) //
				.compileBothTripSort());
		map2.put(302L, new RouteTripSpec(302L, //
				INBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, TRANSIT_TERMINAL, //
				OUTBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, LANSING) //
				.addTripSort(INBOUND_ID, //
						Arrays.asList(new String[] { //
						"7180", // Graywood
								"7110", // == Cambrian College
								"7095", // !=
								"6505", // !=
								"6510", // !=
								"7085", // ==
								"6585", // New Sudbury Centre
								"1000", // Transit Terminal
						})) //
				.addTripSort(OUTBOUND_ID, //
						Arrays.asList(new String[] { //
						"1000", // Transit Terminal
								"7090", // ==
								"6552", // !=
								"6500", // !=
								"7100", // !=
								"7113", // ==
								"7180", // Graywood
						})) //
				.compileBothTripSort());
		map2.put(304l, new RouteTripSpec(304l, //
				LOOP_ID, MTrip.HEADSIGN_TYPE_STRING, TRANSIT_TERMINAL_NEW_SUDBURY, //
				0, MTrip.HEADSIGN_TYPE_STRING, "") //
				.addTripSort(LOOP_ID, //
						Arrays.asList(new String[] { //
						"1000", "6930", "1000" //
						})) //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						})) //
				.compileBothTripSort());
		map2.put(305l, new RouteTripSpec(305l, //
				INBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, TRANSIT_TERMINAL, //
				OUTBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, LASALLE_PEPERTREE) //
				.addTripSort(INBOUND_ID, //
						Arrays.asList(new String[] { //
						"7300", "1000" //
						})) //
				.addTripSort(OUTBOUND_ID, //
						Arrays.asList(new String[] { //
						"1000", "7300" //
						})) //
				.compileBothTripSort());
		map2.put(400l, new RouteTripSpec(400l, //
				INBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, TRANSIT_TERMINAL, //
				OUTBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, CAMBRIAN_COLLEGE) //
				.addTripSort(INBOUND_ID, //
						Arrays.asList(new String[] { //
						"7115", "1000" //
						})) //
				.addTripSort(OUTBOUND_ID, //
						Arrays.asList(new String[] { //
						"1000", "7115" //
						})) //
				.compileBothTripSort());
		map2.put(401l, new RouteTripSpec(401l, //
				INBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, TRANSIT_TERMINAL, //
				OUTBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, CAMBRIAN_COLLEGE) //
				.addTripSort(INBOUND_ID, //
						Arrays.asList(new String[] { //
						"7115", // Cambrian College
								"4371", // == !=
								"4350", // !=
								"4360", // != <>
								"4351", // == !=
								"1000", // Transit Terminal
						})) //
				.addTripSort(OUTBOUND_ID, //
						Arrays.asList(new String[] { //
						"1000", // Transit Terminal
								"4185", // ==
								"4325", // !=
								"4335", // ==
								"4346", // !=
								"4360", // <>
								"4366", // ==
								"4373", // !=
								"4356", // !=
								"6600", // ==
								"7115", // Cambrian College
						})) //
				.compileBothTripSort());
		map2.put(402l, new RouteTripSpec(402l, //
				INBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, TRANSIT_TERMINAL, //
				OUTBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, CAMBRIAN_COLLEGE) //
				.addTripSort(INBOUND_ID, //
						Arrays.asList(new String[] { //
						"7110", "1000" //
						})) //
				.addTripSort(OUTBOUND_ID, //
						Arrays.asList(new String[] { //
						"1000", "7110" //
						})) //
				.compileBothTripSort());
		map2.put(403l, new RouteTripSpec(403l, //
				INBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, TRANSIT_TERMINAL, //
				OUTBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, PEPERTREE_VILLAGE) //
				.addTripSort(INBOUND_ID, //
						Arrays.asList(new String[] { //
						"7300", "1000" //
						})) //
				.addTripSort(OUTBOUND_ID, //
						Arrays.asList(new String[] { //
						"1000", "7300" //
						})) //
				.compileBothTripSort());
		map2.put(502l, new RouteTripSpec(502l, //
				INBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, TRANSIT_TERMINAL, //
				OUTBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, ALGOMA_HOSPITAL) //
				.addTripSort(INBOUND_ID, //
						Arrays.asList(new String[] { //
						"5655", "1000" //
						})) //
				.addTripSort(OUTBOUND_ID, //
						Arrays.asList(new String[] { //
						"1000", "5655" //
						})) //
				.compileBothTripSort());
		map2.put(503l, new RouteTripSpec(503l, //
				INBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, UNIVERSITY, //
				OUTBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, WALMART_SOUTH_END) //
				.addTripSort(INBOUND_ID, //
						Arrays.asList(new String[] { //
						"5830", "5585", "1000" //
						})) //
				.addTripSort(OUTBOUND_ID, //
						Arrays.asList(new String[] { //
						"1000", "5590", "5830" //
						})) //
				.compileBothTripSort());
		map2.put(640l, new RouteTripSpec(640l, //
				INBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, TRANSIT_TERMINAL, //
				OUTBOUND_ID, MTrip.HEADSIGN_TYPE_STRING, COPPER_CLIFF) //
				.addTripSort(INBOUND_ID, //
						Arrays.asList(new String[] { //
						"2290", "1000" //
						})) //
				.addTripSort(OUTBOUND_ID, //
						Arrays.asList(new String[] { //
						"1000", "2175", "2290" //
						})) //
				.compileBothTripSort());
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
		if (mRoute.getId() == 303L) {
			if ("Falconbridge / Transit Terminal".equals(gTrip.getTripHeadsign())) {
				mTrip.setHeadsignString(FALCONBRIDGE_TRANSIT_TERMINAL, LOOP_ID);
				return;
			}
		} else if (mRoute.getId() == 500l) {
			if ("Transit Terminal".equals(gTrip.getTripHeadsign())) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if ("Laurentian University".equals(gTrip.getTripHeadsign())) {
				mTrip.setHeadsignString(LAURENTIAN_UNIVERSITY, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 501l) {
			if ("Transit Terminal".equals(gTrip.getTripHeadsign())) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if ("Algoma Hospital".equals(gTrip.getTripHeadsign())) {
				mTrip.setHeadsignString(ALGOMA_HOSPITAL, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 701l) {
			if ("Transit Terminal".equals(gTrip.getTripHeadsign())) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if ("Lively".equals(gTrip.getTripHeadsign())) {
				mTrip.setHeadsignString(LIVELY, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 702l) {
			if ("Transit Terminal".equals(gTrip.getTripHeadsign())) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if ("Chelmsford".equals(gTrip.getTripHeadsign())) {
				mTrip.setHeadsignString(CHELMSFORD, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 703l) {
			if ("Transit Terminal".equals(gTrip.getTripHeadsign())) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if ("Capreol".equals(gTrip.getTripHeadsign())) {
				mTrip.setHeadsignString(CAPREOL, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 704l) {
			if ("Transit Terminal".equals(gTrip.getTripHeadsign())) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if ("Gravelle Dr. Trailer Park".equals(gTrip.getTripHeadsign())) {
				mTrip.setHeadsignString(GRAVELLE_DR_TRAILER_PARK, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 819l) {
			if ("Transit Terminal".equals(gTrip.getTripHeadsign())) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if ("Walmart South End".equals(gTrip.getTripHeadsign())) {
				mTrip.setHeadsignString(WALMART_SOUTH_END, OUTBOUND_ID);
				return;
			}
		} else if (mRoute.getId() == 940l) {
			if ("Transit Terminal".equals(gTrip.getTripHeadsign())) {
				mTrip.setHeadsignString(TRANSIT_TERMINAL, INBOUND_ID);
				return;
			} else if ("Copper Cliff".equals(gTrip.getTripHeadsign())) {
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
		tripHeadsign = CleanUtils.CLEAN_AND.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
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
