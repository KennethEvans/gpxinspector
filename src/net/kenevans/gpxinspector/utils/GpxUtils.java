package net.kenevans.gpxinspector.utils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.datatype.XMLGregorianCalendar;

import net.kenevans.gpxtrackpointextensionv2.TrkType;
import net.kenevans.gpxtrackpointextensionv2.TrksegType;
import net.kenevans.gpxtrackpointextensionv2.WptType;

/*
 * Created on Sep 6, 2010
 * By Kenneth Evans, Jr.
 */

public class GpxUtils {
	/**
	 * Nominal radius of the earth in miles. The radius actually varies from 3937 to
	 * 3976 mi.
	 */
	public static final double REARTH = 3956;
	/** Multiplier to convert miles to nautical miles. */
	public static final double MI2NMI = 1.852; // Exact
	/** Multiplier to convert degrees to radians. */
	public static final double DEG2RAD = Math.PI / 180.;
	/** Multiplier to convert feet to miles. */
	public static final double FT2MI = 1. / 5280.;
	/** Multiplier to convert meters to miles. */
	public static final double M2MI = .00062137119224;
	/** Multiplier to convert kilometers to miles. */
	public static final double KM2MI = .001 * M2MI;
	/** Multiplier to convert meters to feet. */
	public static final double M2FT = 3.280839895;
	/** Multiplier to convert sec to hours. */
	public static final double SEC2HR = 1. / 3600.;
	/** Multiplier to convert millisec to hours. */
	public static final double MS2HR = .001 * SEC2HR;

	/** String used for non-available data */
	private static final String NOT_AVAILABLE = "NA";
	/**
	 * The speed in m/sec below which there is considered to be no movement for the
	 * purposes of calculating Moving Time. This is, of course, arbitrary. Note that
	 * 1 mi/hr is 0.44704 m/sec. This is expected to be set from preferences.
	 */
	public static double NO_MOVE_SPEED = .5;

	/**
	 * Returns great circle distance in meters. assuming a spherical earth. Uses
	 * Haversine formula.
	 * 
	 * @param lat1 Start latitude in deg.
	 * @param lon1 Start longitude in deg.
	 * @param lat2 End latitude in deg.
	 * @param lon2 End longitude in deg.
	 * @return
	 */
	public static double greatCircleDistance(double lat1, double lon1, double lat2, double lon2) {
		double slon, slat, a, c, d;

		// Convert to radians
		lat1 *= DEG2RAD;
		lon1 *= DEG2RAD;
		lat2 *= DEG2RAD;
		lon2 *= DEG2RAD;

		// Haversine formula
		slon = Math.sin((lon2 - lon1) / 2.);
		slat = Math.sin((lat2 - lat1) / 2.);
		a = slat * slat + Math.cos(lat1) * Math.cos(lat2) * slon * slon;
		c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		d = REARTH / M2MI * c;

		return (d);
	}

	/**
	 * Returns a TrackStat with statistics for the given segment.
	 * 
	 * @param seg
	 * @return
	 * @see net.kenevans.gpxinspector.utils.GpxUtils.TrackStat
	 */
	public static TrackStat trksegStatistics(TrksegType seg) {
		TrackStat stat = new TrackStat();
		if (seg == null) {
			return stat;
		}
		List<WptType> trackPoints = seg.getTrkpt();
		if (trackPoints == null || trackPoints.size() == 0) {
			return stat;
		}
		double length = 0, deltaLength, speed;
		double movingTime = 0, prevTime = 0, lastTime = 0, firstTime = 0;
		double deltaTime;
		double lat, lon, prevLat = 0, prevLon = 0;
		double maxSpeed = 0, avgSpeed = 0;
		double avgMovingSpeed = 0;
		double ele = 0, avgEle = 0;
		double maxEle = -Double.MAX_VALUE, minEle = Double.MAX_VALUE;
		BigDecimal elebd;
		int nPoints = 0;
		XMLGregorianCalendar xgcal = null;
		GregorianCalendar gcal = null;
		for (WptType wpt : seg.getTrkpt()) {
			;
			xgcal = wpt.getTime();
			if (xgcal != null) {
				gcal = xgcal.toGregorianCalendar(TimeZone.getTimeZone("GMT"), null, null);
				lastTime = gcal.getTimeInMillis();
			} else {
				lastTime = Double.NaN;
			}
			lat = wpt.getLat().doubleValue();
			lon = wpt.getLon().doubleValue();
			elebd = wpt.getEle();
			if (nPoints == 0) {
				firstTime = lastTime;
			} else {
				deltaLength = greatCircleDistance(prevLat, prevLon, lat, lon);
				deltaTime = lastTime - prevTime;
				length += deltaLength;
				speed = deltaTime > 0 ? 1000. * deltaLength / deltaTime : 0;
				// System.out.println("speed=" + String.format("%.4f", speed)
				// + " NO_MOVE_SPEED=" + String.format("%.4f", NO_MOVE_SPEED));
				if (speed > NO_MOVE_SPEED) {
					movingTime += deltaTime;
					// We keep track of the sum of dl and later divide by the
					// moving time.
					avgMovingSpeed += deltaLength;
				}
				if (speed > maxSpeed) {
					maxSpeed = speed;
				}
			}
			if (elebd != null) {
				ele = wpt.getEle().doubleValue();
				if (ele > maxEle) {
					maxEle = ele;
				}
				if (ele < minEle) {
					minEle = ele;
				}
				avgEle += ele;
			}
			nPoints++;
			prevTime = lastTime;
			prevLat = lat;
			prevLon = lon;
		}
		deltaTime = lastTime - firstTime;
		if (nPoints > 0) {
			avgEle /= nPoints;
		}
		// Average speed is based on distance and elapsed time
		if (deltaTime == 0) {
			avgSpeed = 0;
		} else {
			avgSpeed = 1000. * length / deltaTime;
		}
		if (movingTime == 0) {
			avgMovingSpeed = 0;
		} else {
			avgMovingSpeed /= movingTime;
		}
		if (maxEle == -Double.MAX_VALUE) {
			maxEle = Double.NaN;
		}
		if (minEle == Double.MAX_VALUE) {
			maxEle = Double.NaN;
		}
		// Fix some time-dependent variables to not be defined, if they could
		// not have been
		if (Double.isNaN(deltaTime)) {
			avgSpeed = Double.NaN;
			avgMovingSpeed = Double.NaN;
			maxSpeed = Double.NaN;
			movingTime = Double.NaN;
		}
		stat.setNPoints(nPoints);
		stat.setStartTime(firstTime);
		stat.setElapsedTime(deltaTime);
		stat.setMovingTime(movingTime);
		stat.setLength(length);
		stat.setMaxSpeed(maxSpeed);
		stat.setAvgSpeed(avgSpeed);
		stat.setAvgMovingSpeed(avgMovingSpeed);
		stat.setMaxEle(maxEle);
		stat.setMinEle(minEle);
		stat.setAvgEle(avgEle);
		return stat;
	}

	/**
	 * Returns a TrackStat with statistics for the given track.
	 * 
	 * @param seg
	 * @return
	 * @see net.kenevans.gpxinspector.utils.GpxUtils.TrackStat
	 */
	public static TrackStat trkStatistics(TrkType trk) {
		TrackStat stat = new TrackStat();
		if (trk == null) {
			return stat;
		}
		List<TrksegType> segments = trk.getTrkseg();
		if (segments == null || segments.size() == 0) {
			return stat;
		}
		int nPoints = 0;
		double length = 0;
		double movingTime = 0;
		double firstTime = 0;
		double elapsedTime = 0;
		double maxSpeed = 0;
		double avgSpeed = 0;
		double avgMovingSpeed = 0;
		double maxEle = -Double.MAX_VALUE, minEle = Double.MAX_VALUE;
		double avgEle = 0;
		TrackStat segStat;
		boolean first = true;
		for (TrksegType seg : segments) {
			segStat = trksegStatistics(seg);
			if (first) {
				first = false;
				firstTime = segStat.getStartTime();
			}
			nPoints += segStat.getNPoints();
			length += segStat.getLength();
			movingTime += segStat.getMovingTime();
			avgMovingSpeed = segStat.getAvgMovingSpeed() * segStat.getMovingTime();
			elapsedTime += segStat.getElapsedTime();
			avgEle += segStat.getAvgEle() * segStat.getNPoints();
			if (maxEle < segStat.getMaxEle()) {
				maxEle = segStat.getMaxEle();
			}
			if (minEle > segStat.getMinEle()) {
				minEle = segStat.getMinEle();
			}
			if (maxSpeed < segStat.getMaxSpeed()) {
				maxSpeed = segStat.getMaxSpeed();
			}
		}
		// Average speed is based on distance and elapsed time
		if (elapsedTime == 0) {
			avgSpeed = 0;
		} else {
			avgSpeed = 1000. * length / elapsedTime;
		}
		if (movingTime == 0) {
			avgMovingSpeed = 0;
		} else {
			avgMovingSpeed = 1000. * length / movingTime;
		}
		if (nPoints > 0) {
			avgEle /= nPoints;
		}
		if (maxEle == -Double.MAX_VALUE) {
			maxEle = Double.NaN;
		}
		if (minEle == Double.MAX_VALUE) {
			maxEle = Double.NaN;
		}
		// Fix some time-dependent variables to not be defined, if they could
		// not have been
		if (Double.isNaN(elapsedTime)) {
			avgSpeed = Double.NaN;
			avgMovingSpeed = Double.NaN;
			maxSpeed = Double.NaN;
			movingTime = Double.NaN;
		}
		stat.setNPoints(nPoints);
		stat.setStartTime(firstTime);
		stat.setElapsedTime(elapsedTime);
		stat.setMovingTime(movingTime);
		stat.setLength(length);
		stat.setMaxSpeed(maxSpeed);
		stat.setAvgSpeed(avgSpeed);
		stat.setAvgMovingSpeed(avgMovingSpeed);
		stat.setMaxEle(maxEle);
		stat.setMinEle(minEle);
		stat.setAvgEle(avgEle);
		return stat;
	}

	/**
	 * Returns a time string of the form HH:mm:ss from the specified date number. If
	 * the input is null, then the value of NOT_AVAILABLE is returned. The string
	 * represents GMT time.
	 * 
	 * @param startTime The long date number.
	 * @return
	 * @see #NOT_AVAILABLE
	 */
	public static String timeString(double time) {
		if (Double.isNaN(time)) {
			return NOT_AVAILABLE;
		}
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		long longTime = Math.round(time);
		Date date = new Date(longTime);
		return formatter.format(date);
	}

	/**
	 * Returns a time string of the form mm/dd/yyyy from the specified
	 * XMLGregorianCalendar. If the input is null, then the value of NOT_AVAILABLE
	 * is returned.
	 * 
	 * @param xgcal
	 * @return
	 * @see #NOT_AVAILABLE
	 */
	public static String getSpreadsheetTimeFromXMLGregorianCalendar(XMLGregorianCalendar xgcal) {
		if (xgcal == null) {
			return NOT_AVAILABLE;
		}
		GregorianCalendar gcal = xgcal.toGregorianCalendar(TimeZone.getTimeZone("GMT"), null, null);
		// Get the date
		Date date = gcal.getTime();
		// Make a new local GregorianCalendar with this date
		gcal = new GregorianCalendar();
		gcal.setTime(date);
		String time = String.format("%02d/%02d/%04d", gcal.get(GregorianCalendar.MONTH) + 1,
				gcal.get(GregorianCalendar.DAY_OF_MONTH), gcal.get(GregorianCalendar.YEAR));
		return time;
	}

	/**
	 * Returns a time string of the form yyyy-MM-dd'T'HH:mm:ss'Z' from the specified
	 * XMLGregorianCalendar. If the input is null, then the value of NOT_AVAILABLE
	 * is returned.
	 * 
	 * @param xgcal
	 * @return
	 * @see #NOT_AVAILABLE
	 */
	public static String getGmtTimeFromXMLGregorianCalendar(XMLGregorianCalendar xgcal) {
		if (xgcal == null) {
			return NOT_AVAILABLE;
		}
		GregorianCalendar gcal = xgcal.toGregorianCalendar(TimeZone.getTimeZone("GMT"), null, null);
		// Get the date
		Date date = gcal.getTime();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		// Make a new local GregorianCalendar with this date
		gcal = new GregorianCalendar();
		gcal.setTime(date);
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		return formatter.format(date);
	}

	/**
	 * Returns a time string of the form given by Date.toString() from the specified
	 * XMLGregorianCalendar. If the input is null, then the value of NOT_AVAILABLE
	 * is returned.
	 * 
	 * @param xgcal
	 * @return
	 * @see #NOT_AVAILABLE
	 */
	public static String getLocalTimeFromXMLGregorianCalendar(XMLGregorianCalendar xgcal) {
		if (xgcal == null) {
			return NOT_AVAILABLE;
		}
		GregorianCalendar gcal = xgcal.toGregorianCalendar(TimeZone.getTimeZone("GMT"), null, null);
		// Get the date
		Date date = gcal.getTime();
		return date.toString();
	}

	/**
	 * Returns a time string of the form yyyy-MM-dd'T'HH:mm:ss'Z' from the specified
	 * date number. If the input is null, then the value of NOT_AVAILABLE is
	 * returned. The string represents GMT time.
	 * 
	 * @param startTime The long date number.
	 * @return
	 * @see #NOT_AVAILABLE
	 */
	public static String getGmtTimeString(double startTime) {
		if (Double.isNaN(startTime)) {
			return NOT_AVAILABLE;
		}
		Date date = new Date(Math.round(startTime));
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		return formatter.format(date);
	}

	/**
	 * Returns a time string of the form given by Date.toString() from the specified
	 * date number. If the input is null, then the value of NOT_AVAILABLE is
	 * returned. The string represents local time.
	 * 
	 * @param startTime The long date number.
	 * @return
	 * @see #NOT_AVAILABLE
	 */
	public static String getLocalTimeString(double startTime) {
		if (Double.isNaN(startTime)) {
			return NOT_AVAILABLE;
		}
		Date date = new Date(Math.round(startTime));
		return date.toString();
	}

}
