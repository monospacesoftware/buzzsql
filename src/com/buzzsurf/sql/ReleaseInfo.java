package com.buzzsurf.sql;

/**
 * <p>
 * Immutable product release information.
 * <p>
 * Call <code>BuzzSQL.getReleaseInfo()</code> to get a instance of this object.
 * <p>
 * @see BuzzSQL#getReleaseInfo()
 * @author Paul Cowan (<a href="http://www.buzzsurf.com/sql">www.buzzsurf.com/sql</a>)
 */
public final class ReleaseInfo
{
	public static final String	LB				= System.getProperty("line.separator");

	private final String		productName		= "BuzzSQL";

	private final int			majorVersion	= 1;
	private final int			minorVersion	= 3;
	private final int			buildNumber		= 8;

	private final String		releaseDate		= "11/04/2007";
	private final String		licenseInfo		= "Usage of this software is subject to the terms found in the included LGPL license.";
	private final String		website			= "http://www.buzzsurf.com/sql";

	ReleaseInfo()
	{

	}

	/**
	 * Get the product name
	 * @return Product name "BuzzSQL"
	 */
	public final String getProductName()
	{
		return productName;
	}

	/**
	 * Get the major version number
	 * @return Product major version
	 */
	public final int getMajorVersion()
	{
		return majorVersion;
	}

	/**
	 * Get the minor version number
	 * @return Product minor version
	 */
	public final int getMinorVersion()
	{
		return minorVersion;
	}

	/**
	 * Get the release date string
	 * @return Product release date in form MM/DD/YYYY
	 */
	public final String getReleaseDate()
	{
		return releaseDate;
	}

	/**
	 * Get the license info
	 * @return Brief product license information string
	 */
	public final String getLicenseInfo()
	{
		return licenseInfo;
	}

	/**
	 * Get the website URL
	 * @return Product website URL
	 */
	public final String getWebsiteString()
	{
		return website;
	}

	/**
	 * Get the build number
	 * @return Product build number
	 */
	public final int getBuildNumber()
	{
		// TODO: modify this to pull from an Ant build version file
		return buildNumber;
	}

	/**
	 * Get the version in formation major.minor.build
	 * @return Product version in format major.minor.build
	 */
	public final String getVersionString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getMajorVersion());
		sb.append(".");
		sb.append(getMinorVersion());
		sb.append(".");
		sb.append(getBuildNumber());
		return sb.toString();
	}

	/**
	 * Get the welcome message
	 * @return Multi-line formated welcome message describing the product
	 */
	public String getWelcome()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getProductName() + " version " + getVersionString() + " (" + getReleaseDate() + ") ");
		sb.append(LB);
		sb.append(getLicenseInfo());
		sb.append(LB);
		sb.append("For the most up-to-date information please visit: " + getWebsiteString());
		return sb.toString();
	}
}
