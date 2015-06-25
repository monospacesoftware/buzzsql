package com.buzzsurf.sql;

import java.sql.*;
import java.util.*;
import java.lang.ref.WeakReference;
import javax.sql.rowset.*;

/**
 A Select object queries a database and can be iterated through to read results. It is a combination of the underlying
 JDBC classes;
 <ul>
 <li>
 <code>java.sql.Connection</code>
 </li>
 <li>
 <code>java.sql.PreparedStatement</code>
 </li>
 <li>
 <code>java.sql.ResultSet</code>
 </li>
 <li>
 <code>java.sql.ResultSetMetaData</code>
 </li>
 </ul>
 After execution, you may iterate through the <code>Select</code> object using
 <code>next()</code>, and retrieve your results in any format using one of the variety of
 <code>get</code> methods. You must call <code>next()</code> at least once before any
 <code>get</code> call to set the index in the result set to the first result.
 <br>
 <br>
 As an example, the following code fragment creates a <code>Select</code> object using the automatic
 default connection to the database. The SQL statement is passed via constructor, and the arguments are passed using the
 <code>setArgs(Object...)</code> method. <code>execute()</code> is called to execute the
 database call, and then results are iterated through using <code>next()</code>. The object is then closed
 to cleanup any resources and return the connection to the pool.
 <br>
 <pre>
 Select select = new Select();
 select.setSQL("select col_pk, col_str, col_int from example.table_example1");
 select.execute();
 while (select.next())
 System.out.println(select.getLine());
 select.close();
 </pre>
 Note: This code does not handle exceptions. You normally should wrap your calls in try/catch/finally blocks to handle
 any problems. It is advisable to put your <code>close()</code> call in a finally block to insure the
 connection is always released.
 * @see BuzzSQL
 * @author Paul Cowan (<a href="http://www.buzzsurf.com/sql">www.buzzsurf.com/sql</a>)
 */
public class Select extends BuzzSQL
{
	protected ResultSet			rs				= null;
	protected ResultSetMetaData	rsmd			= null;
	protected RowMapper<Object>	rm				= null;
	
	/**
	 A zero argument constructor is provided for simplified operation with JavBeans, SOAP, and reflection scenarios where
	 having such a constructor is necessary or convenient. At a minimum, you must call
	 <code>setSQL(String)</code> before execution when using this constructor.
	 */
	public Select()
	{
		super();
	}

	/**
	 A single argument constructor that accepts your SQL statement and uses the default DataSource.
	 * @param sql The SQL statement
	 */
	public Select(String sql)
	{
		super();
		super.setSQL(sql);
	}

	/**
	 A dual argument constructor that accepts your SQL statement and the explicit name of a DataSource to use.
	 * @param sql The SQL statement
	 * @param dataSourceName The explicit name of the dataSource to obtain a connecton from in <code>DataSourceManager</code>. 
	 */
	public Select(String sql, String dataSourceName)
	{
		super();
		super.setDataSourceName(dataSourceName);
		super.setSQL(sql);
	}

	/**
	 A dual argument constructor that accepts your SQL statement and a <code>java.sql.Connection</code> object.
	 This constructor provides a great deal of flexibility by allowing the use of "explicit" connections that are supplied
	 by the user rather than being obtained automatically by BuzzSQL. Using a explicit connection also allows BuzzSQL to
	 support database transactions.
	 * @param sql The SQL statement
	 * @param con The explicit database Connection 
	 */
	public Select(String sql, Connection con)
	{
		super();
		super.setConnection(con);
		super.setSQL(sql);
	}
	
	public <E extends Select> E setRowMapper(RowMapper rowMapper)
	{
		this.rm = rowMapper;
		return (E) this;
	}
	
	/**
	 * Get the internal <code>java.sql.ResultSet</code>.  You normally do not need to access the internal ResultSet
	 * directly, however access is provided here if needed.
	 * @return The <code>java.sql.ResultSet</code> or null if the object is not executing.
	 */
	public ResultSet getResultSet()
	{
		return rs;
	}

	/**
	 * Get the internal <code>java.sql.ResultSetMetaData</code>.  You normally do not need to access the internal 
	 * ResultSetMetaData directly, however access is provided here if needed.
	 * @return The <code>java.sql.ResultSetMetaData</code> or null if the object is not executing.
	 */
	public ResultSetMetaData getMetaData()
	{
		return rsmd;
	}
	
	public Select execute() throws SQLException
	{
		return execute(false);
	}

	/**
	 During execution a database connection is obtained (if needed), SQL and arguments are merged, and the PreparedStatement
	 is executed against the database. <code>execute()</code> throws an exception if any of these steps fails
	 for any reason.
	 <br>
	 <br>
	 Typical post-execution steps are slightly different depending on the object subtype. Select based objects will obtain a
	 <code>ResultSet</code> and <code>ResultSetMetaData</code>, while Update based objects will
	 query and save the updated row count to a local variable.
	 <br>
	 <br>
	 <code>execute()</code> returns a reference to the current object to support method chaining. See
	 <a href="#Method%20Chaining">Method Chaining</a> for more information. You can assume execution succeeded if no exception is thrown.
	 * @throws SQLException if any of the executing JDBC operations failed
	 */
	public Select execute(boolean precache) throws SQLException
	{
		prepare();
		
		stmt = merge(con.prepareStatement(sql));
		rs = stmt.executeQuery();
		
		if(precache)
		{
			CachedRowSet crs = new com.sun.rowset.CachedRowSetImpl();
			crs.populate(rs);
			closeForPreCache();
			rsmd = crs.getMetaData();
			rs = crs;
		}
		else
		{
			rsmd = rs.getMetaData();
		}

		return this;
	}

	/**
	 * Close and commit the SQL object.
	 * <br>
	 * <br>
	 It is import to call <code>close()</code> after you have finished using any BuzzSQL object.
	 <code>close()</code> will release any resources including the database connection if appropriate. It will
	 never throw an exception, so it is always safe to call <code>close()</code>.
	 <br>
	 <br>
	 You may reuse a SQL object after calling <code>close()</code>.
	 <br>
	 <br>
	 The best practice to insure all BuzzSQL objects are closed is to put your call to <code>close()</code> in
	 a finally block.
	 @see #usingExplicitConnection()
	 @return Reference to this object which can be used for method call chaining
	 */
	public Select close()
	{
		if (rs != null)
		{
			try
			{
				rs.close();
			}
			catch (Exception e)
			{
			}
		}

		rsmd = null;
		rs = null;

		return super.close();
	}
	
	protected Select closeForPreCache()
	{
		if (rs != null)
		{
			try
			{
				rs.close();
			}
			catch (Exception e)
			{
			}
		}

		return super.close();
	}

	/**
	 * Moves the ResultSet cursor down one row from its current position.
	 * A <code>ResultSet</code> cursor is initially positioned
	 * before the first row; the first call to the method
	 * <code>next</code> makes the first row the current row; the
	 * second call makes the second row the current row, and so on. 
	 *
	 * <P>If an input stream is open for the current row, a call
	 * to the method <code>next</code> will
	 * implicitly close it. A <code>ResultSet</code> object's
	 * warning chain is cleared when a new row is read.
	 *
	 * @return <code>true</code> if the new current row is valid; 
	 * <code>false</code> if there are no more rows 
	 * @exception SQLException if a database access error occurs
	 */
	public boolean next() throws SQLException
	{
		if (rs == null)
			return false;
		
		return rs.next();
	}

	public <E extends Object> E getMappedObject() throws SQLException
	{
		if (rm == null)
			throw new SQLException("No RowMapper is assigned");
		
		return (E) rm.mapRow(this);
	}

	/**
	 * @return All columns in the current ResultSet concatenated togeather and delimited with a COMMA. 
	 */
	public String getLine() throws SQLException
	{
		return getLine(",");
	}

	/**
	 * @param delim The delimiter
	 * @return All columns in the current ResultSet concatenated togeather and delimited with the delimiter. 
	 */
	public String getLine(String delim) throws SQLException
	{
		StringBuilder sb = new StringBuilder();
		int numCols = getColumnCount();
		for (int i = 1; i <= getColumnCount(); i++)
		{
			sb.append(getString(i));
			if (i < numCols)
				sb.append(delim);
		}

		return sb.toString();
	}

	/**
	 * @return All columns in the current ResultSet concatenated togeather, enclosed in \"quotes\" and delimited with a comma.
	 */
	public String getLineCSV() throws SQLException
	{
		StringBuilder sb = new StringBuilder();
		int numCols = getColumnCount();
		for (int i = 1; i <= getColumnCount(); i++)
		{
			sb.append("\"");
			sb.append(getString(i));
			sb.append("\"");
			if (i < numCols)
				sb.append(",");
		}

		return sb.toString();
	}

	/**
	 * Returns the number of columns in this <code>ResultSet</code> object.
	 *
	 * @return the number of columns or -1
	 * @exception SQLException if a database access error occurs
	 */
	public int getColumnCount() throws SQLException
	{
		if (rsmd != null)
			return rsmd.getColumnCount();

		return -1;
	}

	/**
	 * Get the designated column's name.
	 *
	 * @param column the first column is 1, the second is 2, ...
	 * @return column name
	 * @exception SQLException if a database access error occurs
	 */
	public String getColumnName(int column) throws SQLException
	{
		if (rsmd != null)
			return rsmd.getColumnName(column);

		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>String</code> in the Java programming language.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 * @exception SQLException if a database access error occurs
	 */
	public String getString(int columnIndex) throws SQLException
	{
		if (rs != null)
			return rs.getString(columnIndex);

		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>boolean</code> in the Java programming language.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>false</code>
	 * @exception SQLException if a database access error occurs
	 */
	public boolean getBoolean(int columnIndex) throws SQLException
	{
		if (rs != null)
			return rs.getBoolean(columnIndex);

		return false;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>byte</code> in the Java programming language.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 * @exception SQLException if a database access error occurs
	 */
	byte getByte(int columnIndex) throws SQLException
	{
		if (rs != null)
			return rs.getByte(columnIndex);

		return 0;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * an <code>short</code> in the Java programming language.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 * @exception SQLException if a database access error occurs
	 */
	public short getShort(int columnIndex) throws SQLException
	{
		if (rs != null)
			return rs.getShort(columnIndex);

		return 0;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * an <code>int</code> in the Java programming language.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 * @exception SQLException if a database access error occurs
	 */
	public int getInt(int columnIndex) throws SQLException
	{
		if (rs != null)
			return rs.getInt(columnIndex);

		return 0;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>long</code> in the Java programming language.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 * @exception SQLException if a database access error occurs
	 */
	public long getLong(int columnIndex) throws SQLException
	{
		if (rs != null)
			return rs.getLong(columnIndex);

		return 0;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>float</code> in the Java programming language.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 * @exception SQLException if a database access error occurs
	 */
	public double getFloat(int columnIndex) throws SQLException
	{
		if (rs != null)
			return rs.getFloat(columnIndex);

		return 0;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>double</code> in the Java programming language.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 * @exception SQLException if a database access error occurs
	 */
	public double getDouble(int columnIndex) throws SQLException
	{
		if (rs != null)
			return rs.getDouble(columnIndex);

		return 0;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>java.util.Date</code> object in the Java programming language.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 * @exception SQLException if a database access error occurs
	 */
	public java.util.Date getDate(int columnIndex) throws SQLException
	{
		if (rs != null)
			return rs.getTimestamp(columnIndex);

		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>java.util.Calendar</code> object in the Java programming language.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 * @exception SQLException if a database access error occurs
	 */
	public java.util.Calendar getCalendar(int columnIndex) throws SQLException
	{
		if (rs != null)
		{
			Calendar c = Calendar.getInstance();
			c.setTime(rs.getTimestamp(columnIndex));
			return c;
		}

		return null;
	}

	/**
	 * Set the current time of the provided <code>java.util.Calendar</code> object to the 
	 * value of the designated column in the current row of this <code>ResultSet</code>.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param c the Calendar to set
	 * @exception SQLException if a database access error occurs
	 */
	public void setCalendar(int columnIndex, java.util.Calendar c) throws SQLException
	{
		if (rs != null)
			c.setTime(rs.getTimestamp(columnIndex));
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>byte</code> array in the Java programming language.
	 * The bytes represent the raw values returned by the driver.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 * @exception SQLException if a database access error occurs
	 */
	public byte[] getBytes(int columnIndex) throws SQLException
	{
		if (rs != null)
			return rs.getBytes(columnIndex);

		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>String</code> in the Java programming language.
	 *
	 * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 * @exception SQLException if a database access error occurs
	 */
	public String getString(String columnName) throws SQLException
	{
		if (rs != null)
			return rs.getString(columnName);

		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>boolean</code> in the Java programming language.
	 *
	 * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>false</code>
	 * @exception SQLException if a database access error occurs
	 */
	public boolean getBoolean(String columnName) throws SQLException
	{
		if (rs != null)
			return rs.getBoolean(columnName);

		return false;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>byte</code> in the Java programming language.
	 *
	 * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 * @exception SQLException if a database access error occurs
	 */
	public double getByte(String columnName) throws SQLException
	{
		if (rs != null)
			return rs.getByte(columnName);

		return 0;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>short</code> in the Java programming language.
	 *
	 * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 * @exception SQLException if a database access error occurs
	 */
	public double getShort(String columnName) throws SQLException
	{
		if (rs != null)
			return rs.getShort(columnName);

		return 0;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * an <code>int</code> in the Java programming language.
	 *
	 * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 * @exception SQLException if a database access error occurs
	 */
	public int getInt(String columnName) throws SQLException
	{
		if (rs != null)
			return rs.getInt(columnName);

		return 0;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>long</code> in the Java programming language.
	 *
	 * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 * @exception SQLException if a database access error occurs
	 */
	public long getLong(String columnName) throws SQLException
	{
		if (rs != null)
			return rs.getLong(columnName);

		return 0;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>float</code> in the Java programming language.
	 *
	 * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 * @exception SQLException if a database access error occurs
	 */
	public double getFloat(String columnName) throws SQLException
	{
		if (rs != null)
			return rs.getFloat(columnName);

		return 0;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>double</code> in the Java programming language.
	 *
	 * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 * @exception SQLException if a database access error occurs
	 */
	public double getDouble(String columnName) throws SQLException
	{
		if (rs != null)
			return rs.getDouble(columnName);

		return 0;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>java.util.Date</code> object in the Java programming language.
	 *
	 * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 * @exception SQLException if a database access error occurs
	 */
	public java.util.Date getDate(String columnName) throws SQLException
	{
		if (rs != null)
			return rs.getTimestamp(columnName);

		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>java.util.Calendar</code> object in the Java programming language.
	 *
	 * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 * @exception SQLException if a database access error occurs
	 */
	public java.util.Calendar getCalendar(String columnName) throws SQLException
	{
		Calendar c = Calendar.getInstance();

		if (rs != null)
		{
			c.setTime(rs.getTimestamp(columnName));
			return c;
		}

		return null;
	}

	/**
	 * Set the current time of the provided <code>java.util.Calendar</code> object to the 
	 * value of the designated column in the current row of this <code>ResultSet</code>.
	 *
	 * @param columnName the SQL name of the column
	 * @param c the Calendar to set
	 * @exception SQLException if a database access error occurs
	 */
	public void setCalendar(String columnName, java.util.Calendar c) throws SQLException
	{
		if (rs != null)
			c.setTime(rs.getTimestamp(columnName));
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>byte</code> array in the Java programming language.
	 * The bytes represent the raw values returned by the driver.
	 *
	 * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 * @exception SQLException if a database access error occurs
	 */
	public byte[] getBytes(String columnName) throws SQLException
	{
		if (rs != null)
			return rs.getBytes(columnName);

		return null;
	}
}
