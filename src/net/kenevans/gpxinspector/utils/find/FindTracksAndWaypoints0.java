package net.kenevans.gpxinspector.utils.find;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.List;

import net.kenevans.gpxinspector.model.GpxFileModel;
import net.kenevans.gpxinspector.model.GpxTrackModel;
import net.kenevans.gpxinspector.model.GpxWaypointModel;
import net.kenevans.gpxinspector.preferences.IPreferenceConstants;
import net.kenevans.gpxinspector.utils.GpxException;
import net.kenevans.gpxinspector.utils.GpxUtils;
import net.kenevans.gpxinspector.utils.SWTUtils;
import net.kenevans.gpxinspector.utils.find.FindOptions0.Units;
import net.kenevans.gpxtrackpointextensionv2.TrksegType;
import net.kenevans.gpxtrackpointextensionv2.WptType;

/*
 * Created on Sep 6, 2010
 * By Kenneth Evans, Jr.
 */

public class FindTracksAndWaypoints0 implements IPreferenceConstants {
	private boolean ok = true;
	private boolean dirSpecified = false;
	private boolean latSpecified = false;
	private boolean lonSpecified = false;
	private boolean latMinSpecified = false;
	private boolean latMaxSpecified = false;
	private boolean lonMinSpecified = false;
	private boolean lonMaxSpecified = false;
	private String dirName = D_GPX_DIR;

	private FindOptions0 options;
	private PrintStream printStream = System.out;

	FindTracksAndWaypoints0(String args[]) {
		options = new FindOptions0();
		ok = parseCommand(args);
	}

	/**
	 * FindTracks constructor
	 * 
	 * @param latitude
	 * @param longitude
	 * @param deltaLat
	 * @param deltaLon
	 */
	FindTracksAndWaypoints0(double latitude, double longitude, double deltaLat, double deltaLon) {
		options = new FindOptions0();
		options.setLatitude(latitude);
		options.setLongitude(longitude);
		options.setDeltaLat(deltaLat);
		options.setDeltaLon(deltaLon);
	}

	/**
	 * FindTracks constructor
	 * 
	 * @param latitude
	 * @param longitude
	 * @param deltaLat
	 * @param deltaLon
	 */
	FindTracksAndWaypoints0(double latitude, double longitude, double radius, Units units) {
		options = new FindOptions0();
		options.setLatitude(latitude);
		options.setLongitude(longitude);
		options.setRadius(radius);
		options.setUnits(units);
	}

	/**
	 * FindTracks constructor
	 * 
	 * @param latitude
	 * @param longitude
	 */
	FindTracksAndWaypoints0(double latitude, double longitude) {
		options = new FindOptions0();
		options.setLatitude(latitude);
		options.setLongitude(longitude);
	}

	/**
	 * @return Returns the ok.
	 */
	public boolean isOk() {
		return ok;
	}

	/**
	 * Searches the default directory and subdirectories to find tracks with a
	 * trackpoint inside the limits.
	 */
	public void find() {
		File file = new File(dirName);
		find(file);
	}

	/**
	 * Searches the given directory and subdirectories to find tracks with a
	 * trackpoint inside the limits.
	 * 
	 * @param dir
	 */
	public void find(File dir) {
		if (dir == null) {
			SWTUtils.errMsg("Invalid directory");
			System.exit(1);
		}
		if (!dir.isDirectory()) {
			SWTUtils.errMsg("Not a directory");
			System.exit(1);
		}

		File dirList[] = dir.listFiles();
		int len = dirList.length;

		// int nFiles = 0;
		// int nDirs = 0;
		for (int i = 0; i < len; i++) {
			File file = dirList[i];
			if (file.isDirectory()) {
				// nDirs++;
				find(file);
			} else {
				// nFiles++;
				process(file);
			}
		}
	}

	/**
	 * Processes a single .gpsl file. Checks the extension first, then calls
	 * readAndProcess(File file, double[] limits).
	 * 
	 * @param file
	 */
	private void process(File file) {
		if (!file.exists())
			return;
		String ext = SWTUtils.getExtension(file);
		if (ext == null)
			return;
		if (ext.toLowerCase().equals("gpx")) {
			try {
				readAndProcessGpxFile(file, options.getUnits().radiusInMiles(options.getRadius()));
			} catch (Exception ex) {
				SWTUtils.excMsg("Eror parsing GPX file", ex);
			}
			// } else if(ext.toLowerCase().equals("gpsl")) {
			// try {
			// readAndProcessGpslFile(file, options.getLimits());
			// } catch(GpxException ex) {
			// SWTUtils.excMsg("Eror parsing GPSL file", ex);
			// }
		}
	}

	/**
	 * Reads and processes a single .gpx file.
	 * 
	 * @param file
	 */
	/**
	 * @param file   The file.
	 * @param radius The search radius in miles.
	 */
	public void readAndProcessGpxFile(File file, double radius) {
		boolean fileNamePrinted = false;
		// Create a new GpxFileModel with no parent
		GpxFileModel fileModel = null;
		try {
			fileModel = new GpxFileModel(null, file, false);
		} catch (Throwable t) {
			if (printStream == null) {
				SWTUtils.excMsg("Error parsing " + file.getPath(), t);
			} else {
				printStream.println("Error parsing " + file.getPath());
				printStream.println(t.getMessage());
			}
			return;
		}
		if (fileModel == null) {
			SWTUtils.errMsg("readAndProcessGpxFile: fileModel is null");
			return;
		}
		List<GpxTrackModel> trackModels;
		List<TrksegType> trackSegments;
		List<WptType> trackPoints;
		List<GpxWaypointModel> waypointModels;
		WptType waypoint;
		double lat0 = options.getLatitude();
		double lon0 = options.getLongitude();
		double lat, lon;
		// Loop over waypoints
		waypointModels = fileModel.getWaypointModels();
		for (GpxWaypointModel waypointModel : waypointModels) {
			waypoint = waypointModel.getWaypoint();
			lat = waypoint.getLat().doubleValue();
			lon = waypoint.getLon().doubleValue();
			if (GpxUtils.greatCircleDistance(lat0, lon0, lat, lon) <= radius) {
				if (!fileNamePrinted) {
					System.out.println(file.getAbsolutePath());
					fileNamePrinted = true;
				}
				System.out.println("  " + waypointModel.getWaypoint().getName());
			}
		}

		// Loop over tracks
		boolean found = false;
		trackModels = fileModel.getTrackModels();
		for (GpxTrackModel trackModel : trackModels) {
			found = false;
			trackSegments = trackModel.getTrack().getTrkseg();
			for (TrksegType trackSegment : trackSegments) {
				trackPoints = trackSegment.getTrkpt();
				for (WptType trackPoint : trackPoints) {
					lat = trackPoint.getLat().doubleValue();
					lon = trackPoint.getLon().doubleValue();
					if (GpxUtils.greatCircleDistance(lat0, lon0, lat, lon) <= radius) {
						found = true;
						if (!fileNamePrinted) {
							System.out.println(file.getAbsolutePath());
							fileNamePrinted = true;
						}
						System.out.println("  " + trackModel.getTrack().getName());
						// Done with this track, don't need more track points
						break;
					}
				}
				// Done with this track, don't need more segments
				if (found) {
					break;
				}
			}
		}
	}

	/**
	 * Reads and processes a single .gpsl file.
	 * 
	 * @param file
	 * @param limits
	 */
	public static void readAndProcessGpslFile(File file, double[] limits) {
		if (limits == null)
			return;
		String GPSLINK_ID = "!GPSLINK";
		String DELIMITER = "Delimiter";
		String GMTOFFSET = "GMTOffset";
		boolean fileNamePrinted = false;
		double lat0 = limits[0];
		double lat1 = limits[1];
		double lon0 = limits[2];
		double lon1 = limits[3];
		long lineNum = 0;
		boolean error = false;
		String line = null;
		String[] tokens = null;
		String delimiter = "\t";
		// String name = null;
		// double fileGMTOffsetHr = 0.0;
		String trackName = null;
		boolean trkDataInProgress = false;
		// boolean trkDataAborted = false;

		double latitude;
		double longitude;
		// double altitude;
		// String symbol = null;
		// String time = "";
		// boolean startTrack = false;
		boolean found = false;
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(file));

			// Read ID
			line = in.readLine();
			lineNum++;
			if (line == null) {
				SWTUtils.errMsg("Unexpected end of file at line " + lineNum + ":\n" + file.getName());
				in.close();
				return;
			}
			if (!line.equals(GPSLINK_ID)) {
				SWTUtils.errMsg("Invalid GPSLink file (Bad ID) at line " + lineNum + ":\n" + file.getName());
				in.close();
				return;
			}

			// Read timestamp
			line = in.readLine();
			lineNum++;
			if (line == null) {
				SWTUtils.errMsg("Unexpected end of file at line " + lineNum + ":\n" + file.getName());
				in.close();
				return;
			}

			// Delimiter
			line = in.readLine();
			lineNum++;
			if (line == null) {
				SWTUtils.errMsg("Unexpected end of file at line " + lineNum + ":\n" + file.getName());
				in.close();
				return;
			}
			tokens = line.split("=");
			if (tokens.length < 2 || !tokens[0].equals(DELIMITER)) {
				SWTUtils.warnMsg("No delimiter found at line + lineNum " + ", assuming TAB:\n" + file.getName());
				delimiter = "\t";
			}
			delimiter = tokens[1];
			if (!delimiter.equals(",") && !delimiter.equals("\t")) {
				SWTUtils.warnMsg("Invalid delimiter found at line + lineNum " + ", assuming TAB:\n" + file.getName());
				delimiter = "\t";
			}

			// GMTOffset
			line = in.readLine();
			lineNum++;
			if (line == null) {
				SWTUtils.errMsg("Unexpected end of file at line " + lineNum + ":\n" + file.getName());
				in.close();
				return;
			}
			tokens = line.split("=");
			if (tokens.length < 2 || !tokens[0].equals(GMTOFFSET)) {
				SWTUtils.warnMsg("No " + GMTOFFSET + " found at " + lineNum + ", assuming 0:\n" + file.getName());
				// fileGMTOffsetHr = 0.0;
			} else {
				// fileGMTOffsetHr = Double.valueOf(tokens[1]).doubleValue();
			}

			// Loop over the rest of the lines
			while ((line = in.readLine()) != null) {
				lineNum++;

				// Skip comments
				if (line.startsWith("#"))
					continue;
				// Insure there at least two characters
				if (line.length() < 2)
					continue;
				// Only handle lines that have a type identifier
				if (!line.subSequence(1, 2).equals(delimiter))
					continue;

				// Branch on type
				tokens = line.split(delimiter);
				String startChar = line.substring(0, 1);
				// Only look at tracks and trackpoints
				if (startChar.equals("H")) {
					// Track
					trackName = tokens[1];
					if (trackName == null) {
						SWTUtils.errMsg("Line " + lineNum + " Cannot create track:\n" + file.getName());
						error = true;
						break;
					}
					trkDataInProgress = true;
					// trkDataAborted = false;
					found = false;
				} else if (startChar.equals("T")) {
					// TrackPoint
					if (found)
						continue;
					if (tokens.length < 6) {
						SWTUtils.errMsg("Line " + lineNum + ": invalid trackpoint:\n" + file.getName());
						error = true;
						break;
					}
					// name = tokens[1];
					;
					latitude = Double.valueOf(tokens[2]).doubleValue();
					longitude = Double.valueOf(tokens[3]).doubleValue();
					// altitude = Double.valueOf(tokens[4]).doubleValue();
					// symbol = "";
					// time = tokens[5];

					if (!trkDataInProgress) {
						SWTUtils.errMsg("Line " + lineNum + " Found trackpoint without track:\n" + file.getName());
					}

					// Check
					if (latitude < lat0 || latitude > lat1 || longitude < lon0 || longitude > lon1)
						continue;
					found = true;
					if (!fileNamePrinted) {
						System.out.println(file.getAbsolutePath());
					}
					System.out.println(" " + trackName);
				}
			}
			if (error)
				return;
		} catch (Exception ex) {
			SWTUtils.errMsg(
					"Error reading " + file.getName() + "\nat line " + lineNum + "\n" + ex + "\n" + ex.getMessage());
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception ex1) {
				// Do nothing
			}
		}
	}

	/**
	 * @return A String with information abut the limits.
	 */
	public String info() {
		DecimalFormat format1 = new DecimalFormat(" 00.000000;-00.000000");
		DecimalFormat format2 = new DecimalFormat("0.00");
		DecimalFormat format3 = new DecimalFormat("0.0");
		DecimalFormat format4 = new DecimalFormat("0");
		String ls = SWTUtils.LS;
		double latitude = options.getLatitude();
		double longitude = options.getLongitude();
		double latMin = options.getLatMin();
		double latMax = options.getLatMax();
		double lonMin = options.getLonMin();
		double lonMax = options.getLonMax();
		double deltaLat = options.getDeltaLat();
		double deltaLon = options.getDeltaLon();
		double miLat = GpxUtils.greatCircleDistance(latitude - deltaLat / 2, longitude, latitude + deltaLat / 2,
				longitude);
		double miLon = GpxUtils.greatCircleDistance(latitude, longitude - deltaLon / 2, latitude,
				longitude + deltaLon / 2);
		double radius = options.getRadius();
		String latDist = "";
		String lonDist = "";
		String deltaUnits = options.getUnits().getName();
		if (deltaUnits.equalsIgnoreCase("mi")) {
			latDist = " (" + format2.format(miLat) + " mi)";
			lonDist = " (" + format2.format(miLon) + " mi)";
		} else if (deltaUnits.equalsIgnoreCase("ft")) {
			latDist = " (" + format4.format(miLat * 5280) + " ft)";
			lonDist = " (" + format4.format(miLon * 5280) + " ft)";
		} else {
			latDist = " (" + (miLat >= .9999 ? format3.format(miLat) + " mi)" : format4.format(miLat * 5280) + " ft)");
			lonDist = " (" + (miLon >= .9999 ? format3.format(miLon) + " mi)" : format4.format(miLon * 5280) + " ft)");
		}
		String info = "";
		info += "FindTracks" + ls;
		info += "  latitude=" + latitude + " longitude=" + longitude + ls;
		if (latMinSpecified || latMaxSpecified) {
			info += "  latMax=" + latMax + " latMin=" + latMin + ls;
		}
		if (lonMinSpecified || lonMaxSpecified) {
			info += "  lonMax=" + lonMax + " lonMin=" + lonMin + ls;
		}
		// Limits
		try {
			double[] limits = options.getLimits();
			info += "  radius=\"" + radius + "\"" + ls;
			info += "  deltaUnits=\"" + deltaUnits + "\"" + ls;
			info += "  deltaLat=" + format1.format(deltaLat) + latDist + "  deltaLon=" + format1.format(deltaLon)
					+ lonDist + ls;
			info += "  " + format1.format(limits[0]) + " < latitude  < " + format1.format(limits[1]) + lonDist + ls;
			info += "  " + format1.format(limits[2]) + " < longitude < " + format1.format(limits[3]) + ls;
		} catch (GpxException ex) {
			info += "Error determining latitude and longitude limits:" + ls;
			info += ex.getMessage() + ls;
		}
		return info;
	}

	/**
	 * @return A String with information abut the limits.
	 */
	public String scaleInfo() {
		// Cabin coordinated
		DecimalFormat format1 = new DecimalFormat("0.000000");
		DecimalFormat format2 = new DecimalFormat("0.0");
		String ls = SWTUtils.LS;
		double latitude = 0;
		double longitude = 0;
		String info = "Scale Information" + ls;
		double del = .000001;
		for (int i = 0; i < 7; i++) {
			double miLat = GpxUtils.greatCircleDistance(latitude - del / 2, longitude, latitude + del / 2, longitude);
			double miLon = GpxUtils.greatCircleDistance(latitude, longitude - del / 2, latitude, longitude + del / 2);
			info += "  delta=" + format1.format(del) + ": Latitide: " + format2.format(miLat) + " mi, "
					+ format2.format(miLat * 5280) + " ft" + " Longitude: " + format2.format(miLon) + " mi, "
					+ format2.format(miLon * 5280) + " ft" + ls;
			del *= 10;
		}
		return info;
	}

	/**
	 * Parses the command line
	 * 
	 * @param args
	 * @return success or failure
	 */
	private boolean parseCommand(String[] args) {
		int i;
		double latitude = 0;
		double longitude = 0;
		double latMin = 0;
		double latMax = 0;
		double lonMin = 0;
		double lonMax = 0;
		double deltaLat = 0;
		double deltaLon = 0;
		double radius = 0;
		String deltaUnits = null;
		for (i = 0; i < args.length; i++) {
			if (args[i].startsWith("-")) {
				if (args[i].equals("-lat")) {
					latitude = Double.parseDouble(args[++i]);
					options.setLatitude(latitude);
					latSpecified = true;
				} else if (args[i].equals("-lon")) {
					longitude = Double.parseDouble(args[++i]);
					options.setLongitude(longitude);
					lonSpecified = true;
				} else if (args[i].equals("-latMin")) {
					latMin = Double.parseDouble(args[++i]);
					options.setLatMin(latMin);
					latMinSpecified = true;
				} else if (args[i].equals("-latMax")) {
					latMax = Double.parseDouble(args[++i]);
					options.setLatMax(latMax);
					latMaxSpecified = true;
				} else if (args[i].equals("-lonMin")) {
					lonMin = Double.parseDouble(args[++i]);
					options.setLonMin(lonMin);
					lonMinSpecified = true;
				} else if (args[i].equals("-lonMax")) {
					lonMax = Double.parseDouble(args[++i]);
					options.setLonMax(lonMax);
					lonMaxSpecified = true;
				} else if (args[i].equals("-deltaLat")) {
					deltaLat = Double.parseDouble(args[++i]);
					options.setDeltaLat(deltaLat);
				} else if (args[i].equals("-deltaLon")) {
					deltaLon = Double.parseDouble(args[++i]);
					options.setDeltaLon(deltaLon);
				} else if (args[i].equals("-deltaUnits")) {
					deltaUnits = args[++i];
					options.setUnits(deltaUnits);
				} else if (args[i].equals("-radius")) {
					radius = Double.parseDouble(args[++i]);
					options.setRadius(radius);
				} else if (args[i].startsWith("-h")) {
					usage();
					System.exit(0);
				} else {
					System.err.println("\n\nInvalid option: " + args[i]);
					usage();
					return false;
				}
			} else {
				if (!dirSpecified) {
					dirName = args[i];
					dirSpecified = true;
				} else {
					System.err.println("\n\nInvalid option: " + args[i]);
					usage();
					return false;
				}
			}
		}
		if (!latSpecified && !(latMinSpecified && latMaxSpecified)) {
			SWTUtils.errMsg("lat or latMax and latMin must be specified");
			return false;
		}
		if (!lonSpecified && !(lonMinSpecified && lonMaxSpecified)) {
			SWTUtils.errMsg("lon or lonMax and lonMin must be specified");
			return false;
		}
		if (options.getUnits() == Units.UNSPECIFIED) {
			if (deltaUnits != null) {
				SWTUtils.errMsg("deltaUnits (" + deltaUnits + ") is not valid");
			} else {
				SWTUtils.errMsg("deltaUnits is not valid");
			}
			return false;
		}
		if (options.getDeltaLat() <= 0) {
			SWTUtils.errMsg("deltaLat must be greater than zero");
			return false;
		}
		if (options.getDeltaLon() <= 0) {
			SWTUtils.errMsg("deltaLon must be greater than zero");
			return false;
		}
		return true;
	}

	/**
	 * Prints usage
	 */
	private void usage() {
		System.out.println("\nUsage: java " + this.getClass().getName() + " [Options] directory\n"
				+ "  Find tracks: Find tracks in .gpsl files that have a trackpoint\n"
				+ "    that lies within the given latitude and longitude plus or minus\n"
				+ "    deltaLat and deltaLon, respectively, or between latMin and latMax,\n"
				+ "    etc., if these are specified.\n"
				+ "  Specifying latMin, etc. takes precedent over latitude, etc.\n"
				+ "    The latitude and longitude must be specified in some way.  (There\n"
				+ "    are no defaults for them.)\n" + "  The default directory is:\n    " + D_GPX_DIR + "\n"
				+ "  Options:\n" + "    -lat        center latitude, used with deltaLat\n"
				+ "    -lon        center longitude, used with deltaLon\n" + "    -latMin     min latitude\n"
				+ "    -latMax     max latitude\n" + "    -lonMin     min latitude\n" + "    -lonMax     max latitude\n"
				+ "    -deltaLat   deltalat  (Default=" + FindOptions0.DEFAULT_DELTA + ")\n"
				+ "    -deltaLon   deltaLon  (Default=" + FindOptions0.DEFAULT_DELTA + ")\n" + "    -radius (Default="
				+ FindOptions0.DEFAULT_DELTA + ")\n"
				+ "    -deltaUnits delta units (\"mi\", \"ft\", or \"\")  (Default=\"\")\n"
				+ "    -help       This message\n" + "");
	}

	/**
	 * @return The value of printStream. The caller is responsible for creating and
	 *         closing the PrintStream. The default is System.out.
	 */
	public PrintStream getPrintStream() {
		return printStream;
	}

	/**
	 * @param printStream The new value for printStream. The caller is responsible
	 *                    for creating and closing any existing PrintStream. The
	 *                    default is System.out.
	 */
	public void setPrintStream(PrintStream printStream) {
		this.printStream = printStream;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// FindNear app = new FindNear(45.986139,
		// -89.574683);
		FindTracksAndWaypoints0 app = new FindTracksAndWaypoints0(args);
		if (!app.isOk()) {
			System.err.println("Error parsing the command line");
			System.err.println("Aborted");
			System.exit(1);
		}
		System.out.println(app.scaleInfo());
		System.out.println(app.info());
		app.find();
		System.out.println();
		System.out.println("All done");
	}

}
