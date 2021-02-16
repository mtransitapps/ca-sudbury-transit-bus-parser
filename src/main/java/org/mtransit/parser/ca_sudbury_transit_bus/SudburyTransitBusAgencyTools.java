package org.mtransit.parser.ca_sudbury_transit_bus;

import org.jetbrains.annotations.NotNull;
import org.mtransit.commons.CharUtils;
import org.mtransit.commons.CleanUtils;
import org.mtransit.commons.StringUtils;
import org.mtransit.parser.ColorUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// http://opendata.greatersudbury.ca/
// http://opendata.greatersudbury.ca/datasets?q=Transportation&sort_by=relevance
// http://opendata.greatersudbury.ca/datasets/transit-schedule-data-gtfs
// http://www.greatersudbury.ca/image/opendata/gtfs.zip
// https://www.greatersudbury.ca/image/opendata/gtfs.zip
public class SudburyTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new SudburyTransitBusAgencyTools().start(args);
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "Sudbury Transit";
	}

	@Override
	public boolean isGoodEnoughAccepted() {
		return true; // TODO ?
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	private static final Pattern DIGITS = Pattern.compile("[\\d]+");

	@Override
	public long getRouteId(@NotNull GRoute gRoute) {
		if (!CharUtils.isDigitsOnly(gRoute.getRouteShortName())) {
			final Matcher matcher = DIGITS.matcher(gRoute.getRouteShortName());
			if (matcher.find()) {
				final int digits = Integer.parseInt(matcher.group());
				final String rsnLC = gRoute.getRouteShortName().toLowerCase(Locale.ENGLISH);
				if (rsnLC.endsWith("r")) {
					return digits + 180_000L;
				}
			}
			throw new MTLog.Fatal("Unexpected route ID for %s!", gRoute);
		}
		return Long.parseLong(gRoute.getRouteShortName()); // use route short name as route ID
	}

	@NotNull
	@Override
	public String cleanRouteLongName(@NotNull String routeLongName) {
		routeLongName = CleanUtils.cleanSlashes(routeLongName);
		routeLongName = CleanUtils.cleanStreetTypes(routeLongName);
		routeLongName = CleanUtils.cleanNumbers(routeLongName);
		return CleanUtils.cleanLabel(routeLongName);
	}

	@NotNull
	@Override
	public String getRouteColor(@NotNull GRoute gRoute) {
		if (ColorUtils.BLACK.equalsIgnoreCase(gRoute.getRouteColor())) {
			return null;
		}
		return super.getRouteColor(gRoute);
	}

	private static final String AGENCY_COLOR_GREEN = "005941"; // GREEN (from web site CSS)

	private static final String AGENCY_COLOR = AGENCY_COLOR_GREEN;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static final int OUTBOUND_ID = 1; // like MyBus API, do not change
	private static final int INBOUND_ID = 2; // like MyBus API, do not change
	// private static final int LOOP_ID = 3; // like MyBus API, do not change

	private static final String TRANSIT_TERMINAL = "Transit Terminal";
	private static final String NEW_SUDBURY_CENTRE = "New Sudbury Ctr";
	private static final String CONISTON = "Coniston";
	private static final String CAMBRIAN = "Cambrian";
	private static final String DOW = "Dow";
	private static final String CAPREOL = "Capreol";
	private static final String GRAVEL_DR = "Gravel Dr";
	private static final String SOUTH_END = "South End";

	@Override
	public void setTripHeadsign(@NotNull MRoute mRoute, @NotNull MTrip mTrip, @NotNull GTrip gTrip, @NotNull GSpec gtfs) {
		if (isGoodEnoughAccepted()) {
			final String tripHeadsign = gTrip.getTripHeadsign();
			if (mRoute.getId() == 1L) {
				if ("MAIN LINE TO SOUTH END".equalsIgnoreCase(tripHeadsign) //
						|| "MAIN LINE".equals(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), OUTBOUND_ID);
					return;
				} else if ("MAIN LINE TO NEW SUDBURY".equals(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), INBOUND_ID);
					return;
				}
				throw new MTLog.Fatal("%s: Unexpected trip %s!", mRoute.getId(), gTrip.toStringPlus());
			}
			if (mRoute.getId() == 2L) {
				if ("BARRY DOWNE / CAMBRIAN".equals(tripHeadsign) //
						|| "BARRY DOWNE / CAMBRIAN TO CAMBRIAN".equals(tripHeadsign) //
						|| "BARRY DOWNE / CAMBRIAN EXPRESS".equals(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), OUTBOUND_ID);
					return;
				} else if ("2 Barry Downe / Cambrian".equalsIgnoreCase(tripHeadsign) //
						|| "BARRY DOWNE / CAMBRIAN TO DOWNTOWN".equals(tripHeadsign) //
						|| "BARRY DOWNE / CAMBRIAN EXPRESS DOWNTOWN".equals(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), INBOUND_ID);
					return;
				}
				throw new MTLog.Fatal("%s: Unexpected trip %s!", mRoute.getId(), gTrip.toStringPlus());
			}
			if (mRoute.getId() == 11L) {
				if ("DONOVAN/COLLEGE BOREAL TO CAMBRIAN".equals(tripHeadsign) //
						|| "DONOVAN/COLLEGE BOREAL TO NEW SUDBURY CENTRE".equals(tripHeadsign) //
						|| "DONOVAN / COLLEGE BOREAL TO NEW SUDBURY CENTRE".equals(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), OUTBOUND_ID);
					return;
				} else if ("DONOVAN - COLLEGE BOREAL TO DOWNTOWN".equals(tripHeadsign)
						|| "DONOVAN/COLLEGE BOREAL TO DOWNTOWN".equals(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), INBOUND_ID);
					return;
				}
				throw new MTLog.Fatal("%s: Unexpected trip %s!", mRoute.getId(), gTrip.toStringPlus());
			}
			if (mRoute.getId() == 12L) {
				if (isGoodEnoughAccepted()) {
					if ("SECOND AVENUE TO NEW SUDBURY CENTRE".equals(tripHeadsign)) {
						mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), OUTBOUND_ID);
						return;
					} else if ("SECOND AVENUE TO DOWNTOWN".equals(tripHeadsign)) {
						mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), INBOUND_ID);
						return;
					}
				}
				throw new MTLog.Fatal("%s: Unexpected trip %s!", mRoute.getId(), gTrip.toStringPlus());
			}
			if (mRoute.getId() == 14L) {
				if (isGoodEnoughAccepted()) {
					if ("FOUR CORNERS TO SOUTH END".equals(tripHeadsign)) {
						mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), OUTBOUND_ID);
						return;
					} else if ("FOUR CORNERS TO DOWNTOWN".equals(tripHeadsign)) {
						mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), INBOUND_ID);
						return;
					}
				}
				throw new MTLog.Fatal("%s: Unexpected trip %s!", mRoute.getId(), gTrip.toStringPlus());
			}
			if (mRoute.getId() == 101L) {
				if ("LIVELY TO NAUGHTON".equalsIgnoreCase(tripHeadsign) //
						|| "LIVELY TO NAUGHTON VIA MR 24".equals(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), OUTBOUND_ID);
					return;
				} else if ("LIVELY TO DOW".equals(tripHeadsign) //
						|| "LIVELY TO DOWNTOWN".equals(tripHeadsign) //
						|| "Naughton to Downtown".equals(tripHeadsign) //
						|| "LIVELY NAUGHTON TO SOUTH END".equals(tripHeadsign) //
						|| "LIVELY NAUGHTON TO SOUTH END VIA MR 24".equals(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), INBOUND_ID);
					return;
				}
				throw new MTLog.Fatal("%s: Unexpected trip %s!", mRoute.getId(), gTrip.toStringPlus());
			}
			//noinspection IfStatementWithIdenticalBranches
			if (tripHeadsign != null
					&& (
					"Transit Terminal".equals(tripHeadsign) //
							|| tripHeadsign.endsWith(" TO DOWNTOWN") //
							|| tripHeadsign.endsWith(" TO NEW SUDBURY") //
							|| tripHeadsign.endsWith("TO NEW SUDBURY CENTRE") //
							|| tripHeadsign.endsWith(" TO SOUTH END"))
			) {
				mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), INBOUND_ID);
				return;
			} else {
				mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), OUTBOUND_ID);
				return;
			}
		}
		throw new MTLog.Fatal("%s: Unexpected trip %s!", mRoute.getId(), gTrip.toStringPlus());
	}

	private static final Pattern FIX_SUDBURY_SHOPPING_CENTER = Pattern.compile("(subdury shopping centre)", Pattern.CASE_INSENSITIVE);
	private static final String FIX_SUDBURY_SHOPPING_CENTER_REPLACEMENT = "Sudbury centre";

	private static final Pattern STARTS_WITH_TO = Pattern.compile("(^.*( to|to) )", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		// TODO FIXME in sync with Greater Sudbury Transit provider
		tripHeadsign = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, tripHeadsign);
		tripHeadsign = CleanUtils.keepToAndRemoveVia(tripHeadsign);
		tripHeadsign = STARTS_WITH_TO.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = FIX_SUDBURY_SHOPPING_CENTER.matcher(tripHeadsign).replaceAll(FIX_SUDBURY_SHOPPING_CENTER_REPLACEMENT);
		tripHeadsign = CleanUtils.CLEAN_AND.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@Override
	public boolean mergeHeadsign(@NotNull MTrip mTrip, @NotNull MTrip mTripToMerge) {
		List<String> headsignsValues = Arrays.asList(mTrip.getHeadsignValue(), mTripToMerge.getHeadsignValue());
		if (mTrip.getRouteId() == 1L) {
			if (Arrays.asList( //
					"Main Line", //
					SOUTH_END //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SOUTH_END, mTrip.getHeadsignId());
				return true;
			}
		}
		if (mTrip.getRouteId() == 11L) {
			if (Arrays.asList( //
					NEW_SUDBURY_CENTRE, //
					CAMBRIAN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CAMBRIAN, mTrip.getHeadsignId());
				return true;
			}
		}
		if (mTrip.getRouteId() == 14L) {
			if (Arrays.asList( //
					NEW_SUDBURY_CENTRE, //
					CAMBRIAN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CAMBRIAN, mTrip.getHeadsignId());
				return true;
			}
		}
		if (mTrip.getRouteId() == 29L) {
			if (Arrays.asList( //
					SOUTH_END, //
					"Martindale" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Martindale", mTrip.getHeadsignId());
				return true;
			}
		}
		if (mTrip.getRouteId() == 101L) {
			if (Arrays.asList( //
					DOW, //
					SOUTH_END //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SOUTH_END, mTrip.getHeadsignId());
				return true;
			}
		}
		if (mTrip.getRouteId() == 103L) {
			if (Arrays.asList( //
					CONISTON + " / " + TRANSIT_TERMINAL, //
					CONISTON //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CONISTON, mTrip.getHeadsignId());
				return true;
			}
		}
		if (mTrip.getRouteId() == 105L) {
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

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.cleanNumbers(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	@NotNull
	@Override
	public String getStopCode(@NotNull GStop gStop) {
		//noinspection deprecation
		return gStop.getStopId(); // use stop ID as stop code, used by real-time API, do not change
	}

	@Override
	public int getStopId(@NotNull GStop gStop) {
		return super.getStopId(gStop); // used by real-time API, do not change
	}
}
