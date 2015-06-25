package com.buzzsurf.sql;

import java.util.Calendar;

/**
 * <p>
 * An object that models a stored procedure OUT parameter.
 * <p>
 * See <code>com.buzzsurf.sql.StoredProcedure</code> for usage information.
 * <p>
 * @see StoredProcedure
 * @see java.sql.Types
 * @author Paul Cowan (<a href="http://www.buzzsurf.com/sql">www.buzzsurf.com/sql</a>)
 */
public class OutParameter
{
	protected int		outSqlType;
	protected Object	outValue;

	/**
	 * Empty constructor provided for convenience
	 */
	public OutParameter()
	{

	}

	/**
	 * Construct a stored procedure OUT parameter of the specified type
	 * @param outSqlType A <code>java.sql.Type</code> constant int
	 */
	public OutParameter(int outSqlType)
	{
		setOutSqlType(outSqlType);
	}

	/**
	 * Set the out parameter SQL type from <code>java.sql.Type</code>
	 * @param outSqlType A <code>java.sql.Type</code> constant int
	 */
	public void setOutSqlType(int outSqlType)
	{
		this.outSqlType = outSqlType;
	}

	/**
	 * 
	 * @return The <code>java.sql.Type</code> of the out parameter.  
	 */
	public int getOutSqlType()
	{
		return outSqlType;
	}

	void setOutValue(Object outValue)
	{
		this.outValue = outValue;
	}

	/**
	 * 
	 * @return The out parameter value from the stored procedure in it's native format.
	 */
	public Object getObject()
	{
		return outValue;
	}

	/**
	 * 
	 * @return The out parameter value from the stored procedure in String format, or null.
	 */
	public String getString()
	{
		if (outValue == null)
			return null;

		return outValue.toString();
	}

	/**
	 * 
	 * @return The out parameter value from the stored procedure as a boolean, or false if it can not be converted to a boolean.
	 */
	public boolean getBoolean()
	{
		if (outValue == null)
			return false;

		try
		{
			return Boolean.valueOf(outValue.toString());
		}
		catch (Exception e)
		{
			return false;
		}
	}

	/**
	 * 
	 * @return The out parameter value from the stored procedure as a byte, or 0 if it can not be converted to a byte.
	 */
	public byte getByte()
	{
		if (outValue == null)
			return 0;

		try
		{
			return Byte.parseByte(outValue.toString());
		}
		catch (Exception e)
		{
			return 0;
		}
	}

	/**
	 * 
	 * @return The out parameter value from the stored procedure as a short, or 0 if it can not be converted to a short.
	 */
	public short getShort()
	{
		if (outValue == null)
			return 0;

		try
		{
			return Short.parseShort(outValue.toString());
		}
		catch (Exception e)
		{
			return 0;
		}
	}

	/**
	 * 
	 * @return The out parameter value from the stored procedure as an int, or 0 if it can not be converted to an int.
	 */
	public int getInt()
	{
		if (outValue == null)
			return 0;

		try
		{
			return Integer.parseInt(outValue.toString());
		}
		catch (Exception e)
		{
			return 0;
		}
	}

	/**
	 * 
	 * @return The out parameter value from the stored procedure as a long, or 0 if it can not be converted to a long.
	 */
	public long getLong()
	{
		if (outValue == null)
			return 0;

		try
		{
			return Long.parseLong(outValue.toString());
		}
		catch (Exception e)
		{
			return 0;
		}
	}

	/**
	 * 
	 * @return The out parameter value from the stored procedure as a float, or 0 if it can not be converted to a float.
	 */
	public float getFloat()
	{
		if (outValue == null)
			return 0;

		try
		{
			return Float.parseFloat(outValue.toString());
		}
		catch (Exception e)
		{
			return 0;
		}
	}

	/**
	 * 
	 * @return The out parameter value from the stored procedure as a double, or 0 if it can not be converted to a double.
	 */
	public double getDouble()
	{
		if (outValue == null)
			return 0;

		try
		{
			return Double.parseDouble(outValue.toString());
		}
		catch (Exception e)
		{
			return 0;
		}
	}

	/**
	 * 
	 * @return The out parameter value from the stored procedure as a <code>java.util.Date</code>, or null if it is not an instanceof <code>java.util.Date</code>
	 */
	public java.util.Date getDate()
	{
		if (outValue == null)
			return null;

		try
		{
			return (java.util.Date) outValue;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * 
	 * @return The out parameter value from the stored procedure as a <code>java.util.Calendar</code>, or null if it is not an instanceof <code>java.sql.Timestamp</code>
	 */
	public java.util.Calendar getCalendar()
	{
		if (outValue == null)
			return null;

		try
		{
			Calendar c = Calendar.getInstance();
			c.setTime((java.sql.Timestamp) outValue);
			return c;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * 
	 * Set the <code>java.util.Calendar</code> to the value of the out parameter value from the stored procedure if it is an instanceof <code>java.sql.Timestamp</code>
	 */
	public void setCalendar(int columnIndex, java.util.Calendar c)
	{
		if (outValue == null)
			return;

		try
		{
			c.setTime((java.sql.Timestamp) outValue);
		}
		catch (Exception e)
		{
			return;
		}
	}

	/**
	 * 
	 * @return The out parameter value from the stored procedure as a byte array, or null if it can not an instanceof byte array.
	 */
	public byte[] getBytes()
	{
		if (outValue == null)
			return null;

		try
		{
			return (byte[]) outValue;
		}
		catch (Exception e)
		{
			return null;
		}
	}
}
