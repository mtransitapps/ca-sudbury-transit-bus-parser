package org.mtransit.parser.ca_sudbury_transit_bus;

import org.jetbrains.annotations.NotNull;
import org.mtransit.commons.CharUtils;
import org.mtransit.commons.CleanUtils;
import org.mtransit.commons.provider.GreaterSudburyProviderCommons;
import org.mtransit.parser.ColorUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.mt.data.MAgency;

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

	@Override
	public boolean directionSplitterEnabled() {
		return true;
	}

	@Override
	public boolean directionSplitterEnabled(long routeId) {
		if (routeId == 1L) {
			return false;
		}
		return super.directionSplitterEnabled(routeId);
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		return GreaterSudburyProviderCommons.cleanTripHeadSign(tripHeadsign);
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.cleanNumbers(gStopName);
		gStopName = CleanUtils.fixMcXCase(gStopName);
		// gStopName = CleanUtils.cleanBounds(gStopName); // TODO maybe later w/ conditional text shortening relative to target length
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
