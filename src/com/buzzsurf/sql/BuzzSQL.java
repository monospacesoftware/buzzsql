package com.buzzsurf.sql;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.logging.*;

/**
 * Every SQL object in BuzzSQL extends from the base class com.buzzsurf.sql.BuzzSQL, and therefore all operate
 * in a very similar way. <br>
 * <br>
 * Four different constructors are provided for each object to handle the different cases of how a connection
 * to the database is obtained. See <a href="#Connecting%20to%20the%20Database">Connecting to the Database</a>
 * for more information on obtaining a connection to the database explicitly or automatically. <br>
 * <ul>
 * <li> A zero argument constructor is provided for simplified operation with JavBeans, SOAP, and reflection
 * scenarios where having such a constructor is necessary or convenient. At a minimum, you must call
 * <code>setSQL(String)</code> before execution when using this constructor. </li>
 * <li> A single argument constructor that accepts your SQL statement and uses the default DataSource. </li>
 * <li> A dual argument constructor that accepts your SQL statement and the explicit name of a DataSource to
 * use. </li>
 * <li> A dual argument constructor that accepts your SQL statement and a <code>java.sql.Connection</code>
 * object. This constructor provides a great deal of flexibility by allowing the use of "explicit" connections
 * that are supplied by the user rather than being obtained automatically by BuzzSQL. Using a explicit
 * connection also allows BuzzSQL to support database transactions. </li>
 * </ul>
 * <br>
 * All BuzzSQL objects use an internal <code>java.sql.PreparedStatement</code> object that expects argument
 * placeholders in the SQL as question marks (?). The SQL statement can be passed in via constructor or using
 * the <code>setSQL(String)</code> method. <br>
 * <br>
 * Arguments are set using the <code>setArgs(Object...)</code> or <code>addArgs(Object...)</code> methods,
 * which are Java 5 variable arguments methods. Therefore you can call <code>setArgs(Object...)</code> and
 * pass any type of object or primitive in any combination. The order of your arguments must only match the
 * order of your question marks in the SQL statement. The difference between setArgs and addArgs is that
 * setArgs will first clear an previously set values. <br>
 * <br>
 * BuzzSQL does not handle quoting or escaping of any arguments; the decision to quote or not to quote is left
 * up the JDBC driver. Therefore it is important to pass your arguments in <code>setArgs(Object...)</code>
 * as the appropriate native Java type. <br>
 * <br>
 * For example; <br>
 * <ul>
 * <li> <code>byte, short, int, long</code> if the database type is numeric </li>
 * <li> <code>float, double</code> if the database type is numeric with precision </li>
 * <li> <code>String, char</code> if the database type is varchar based </li>
 * <li> <code>java.util.Date, java.util.Calendar</code> if the database type is a date/time based </li>
 * <li> <code>com.buzzsurf.sql.OutParameter, com.buzzsurf.sql.InOutParameter</code> if the database type is
 * a stored procedure OUT or INOUT parameter. See <a href="#Stored%20Procedure">Stored Procedure</a> for more
 * information. </li>
 * </ul>
 * <br>
 * During execution a database connection is obtained (if needed), SQL and arguments are merged, and the
 * PreparedStatement is executed against the database. <code>execute()</code> throws an exception if any of
 * these steps fails for any reason. <br>
 * <br>
 * Typical post-execution steps are slightly different depending on the object subtype. Select based objects
 * will obtain a <code>ResultSet</code> and <code>ResultSetMetaData</code>, while Update based objects
 * will query and save the updated row count to a local variable. <br>
 * <br>
 * <code>execute()</code> returns a reference to the current object to support method chaining. See <a
 * href="#Method%20Chaining">Method Chaining</a> for more information. You can assume execution succeeded if
 * no exception is thrown. <br>
 * <br>
 * It is import to call <code>close()</code> after you have finished using any BuzzSQL object.
 * <code>close()</code> will release any resources including the database connection if appropriate. It will
 * never throw an exception, so it is always safe to call <code>close()</code>. <br>
 * <br>
 * You may reuse a SQL object after calling <code>close()</code>.
 * 
 * @see DataSourceManager
 * @see Select
 * @see Update
 * @see Insert
 * @see Delete
 * @see StoredProcedure
 * @author Paul Cowan (<a href="http://www.buzzsurf.com/sql">www.buzzsurf.com/sql</a>)
 */
public abstract class BuzzSQL
{
	protected static ThreadLocal<SimpleDateFormat>	DATABASE_FORMATTER		= new ThreadLocal<SimpleDateFormat>() {
																				protected synchronized SimpleDateFormat initialValue()
																				{
																					return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
																				}
																			};

	private static Log								log						= LogFactory.getLog(BuzzSQL.class);

	private static final ReleaseInfo				releaseInfo				= new ReleaseInfo();

	protected String								dataSourceName;
	protected String								sql;
	protected List<Object>							args					= new ArrayList<Object>();

	protected Connection							con						= null;
	protected PreparedStatement						stmt					= null;

	protected boolean								usingExplicitConnection	= false;

	protected BuzzSQL()
	{

	}

	/**
	 * Get the current dataSource name. The name is used during <code>execute()</code> to obtain a
	 * connection from <code>DataSourceManager</code>.
	 * 
	 * @return current dataSource name
	 * @see DataSourceManager
	 */
	public String getDataSourceName()
	{
		return dataSourceName;
	}

	/**
	 * Set the dataSource name. Used during <code>execute()</code> to obtain a connection from
	 * DataSourceManager. <code>dataSourceName</code> will usually be passed via constructor, but access is
	 * provided here to support beans, soap, etc. Will have no effect if the object is currently executing or
	 * using an explicit connection.
	 * 
	 * @param dataSourceName dataSource name
	 * @return Reference to this object which can be used for method call chaining
	 * @see #usingExplicitConnection()
	 * @see DataSourceManager
	 */
	public <E extends BuzzSQL> E setDataSourceName(String dataSourceName)
	{
		if (con == null && !usingExplicitConnection)
			this.dataSourceName = dataSourceName;
		return (E) this;
	}

	/**
	 * Get the current database connection. You normally do not need to access the internal connection,
	 * especially if you are not using an external connection. However access is provided here if needed.
	 * 
	 * @return current database connection if statement is executing, or the external connection, or null
	 *         otherwise
	 */
	public Connection getConnection()
	{
		return con;
	}

	/**
	 * Set the current connection to an explicit connection. If con is non-null, this will automatically set
	 * <code>usingExplicitConnection</code> true. When using an explicit connection you are responsible for
	 * opening and closing the connection. <code>setConnection</code> has no effect if the object is
	 * currently executing.
	 * 
	 * @param con The external connection or null to make the connection null and not use an external
	 *        connection
	 * @return Reference to this object which can be used for method call chaining
	 * @see #usingExplicitConnection()
	 */
	public <E extends BuzzSQL> E setConnection(Connection con)
	{
		if (this.con == null || (usingExplicitConnection && stmt == null))
		{
			this.con = con;
			if (con == null)
				usingExplicitConnection = false;
			else
				usingExplicitConnection = true;
		}
		return (E) this;
	}

	/**
	 * Get SQL statement string. Statements should have ? as placeholders for arguments.
	 * 
	 * @return SQL statement string
	 */
	public String getSQL()
	{
		return sql;
	}

	/**
	 * Set the SQL statement. Statements should have ? as placeholders for args. The SQL statement is usually
	 * passed via constructor, but access is provided here to support beans, soap, etc. <code>setSQL</code>
	 * has no effect if the object is currently executing.
	 * 
	 * @param sql The SQL statement
	 * @return Reference to this object which can be used for method call chaining
	 */
	public <E extends BuzzSQL> E setSQL(String sql)
	{
		if (this.con == null || (usingExplicitConnection && stmt == null))
			this.sql = sql;
		return (E) this;
	}

	/**
	 * Get array of the currently set args. Args will be merged with SQL statement ? placeholders during
	 * execution.
	 * 
	 * @return array of object args or null if there are no args to the SQL statment.
	 */
	public List<Object> getArgs()
	{
		return Collections.unmodifiableList(args);
	}

	/**
	 * Set the SQL statement arguments. Objects will be merged with SQL statement ? placeholders during
	 * execution. <code>setArgs</code> has no effect if the object is currently executing.
	 * 
	 * @param args Object args or null if there are no args to the SQL statement.
	 * @return Reference to this object which can be used for method call chaining
	 */
	public <E extends BuzzSQL> E setArgs(Object... args)
	{
		if (this.con == null || (usingExplicitConnection && stmt == null))
		{
			this.args.clear();
			addArgs(args);
		}
		return (E) this;
	}

	/**
	 * Add objects to the current SQL statement arguments. Objects will be merged with SQL statement ?
	 * placeholders during execution. <code>setArgs</code> has no effect if the object is currently
	 * executing.
	 * 
	 * @param args Object args or null if there are no args to the SQL statement.
	 * @return Reference to this object which can be used for method call chaining
	 */
	public <E extends BuzzSQL> E addArgs(Object... args)
	{
		if (this.con == null || (usingExplicitConnection && stmt == null))
		{
			for (Object o : args)
				this.args.add(o);
		}
		return (E) this;
	}

	/**
	 * <code>usingExplicitConnection()</code> will return true if you pass a
	 * <code>java.sql.Connection</code> in via the constructor, or call
	 * <code>setConnection(Connection con)</code> with a non-null <code>java.sql.Connection</code> object.
	 * You can reset the BuzzSQL object's state to use an automatic connections by passing null to
	 * <code>setConnection(Connection con)</code>. <br>
	 * <br>
	 * The explicit <code>java.sql.Connection</code> can come from any source such as an external connection
	 * pool, a JNDI lookup, or BuzzSQL's DataSource Manager. It is common for an application to use BuzzSQL's
	 * automatic connection handling for 99% of the database access, but need to wrap a few specific calls in
	 * a transaction. In this case you can call <code>DataSourceManager.getConnection()</code> to obtain an
	 * explicit connection for the calls that need to be executed in a transaction, and allow BuzzSQL to use
	 * automatic connection handling for the rest.
	 * 
	 * @return true if the SQL object is using an explicit connection
	 */
	public boolean usingExplicitConnection()
	{
		return usingExplicitConnection;
	}

	/**
	 * Get the internal <code>java.sql.PreparedStatement</code>.
	 * 
	 * @return the <code>PreparedStatement</code> or null if the object is not executing.
	 */
	public PreparedStatement getStatement()
	{
		return stmt;
	}

	/**
	 * During execution a database connection is obtained (if needed), SQL and arguments are merged, and the
	 * PreparedStatement is executed against the database. <code>execute()</code> throws an exception if any
	 * of these steps fails for any reason. <br>
	 * <br>
	 * Typical post-execution steps are slightly different depending on the object subtype. Select based
	 * objects will obtain a <code>ResultSet</code> and <code>ResultSetMetaData</code>, while Update
	 * based objects will query and save the updated row count to a local variable. <br>
	 * <br>
	 * <code>execute()</code> returns a reference to the current object to support method chaining. See <a
	 * href="#Method%20Chaining">Method Chaining</a> for more information. You can assume execution succeeded
	 * if no exception is thrown.
	 * 
	 * @throws SQLException if any of the executing JDBC operations failed
	 */
	public abstract <E extends BuzzSQL> E execute() throws SQLException;

	/**
	 * Close and commit the SQL object. <br>
	 * <br>
	 * It is import to call <code>close()</code> after you have finished using any BuzzSQL object.
	 * <code>close()</code> will release any resources including the database connection if appropriate. It
	 * will never throw an exception, so it is always safe to call <code>close()</code>. <br>
	 * <br>
	 * You may reuse a SQL object after calling <code>close()</code>. <br>
	 * <br>
	 * The best practice to insure all BuzzSQL objects are closed is to put your call to <code>close()</code>
	 * in a finally block.
	 * 
	 * @see #usingExplicitConnection()
	 * @return Reference to this object which can be used for method call chaining
	 */
	public <E extends BuzzSQL> E close()
	{
		close(true);
		return (E) this;
	}

	/**
	 * Close the SQL object with a choice to commit. <br>
	 * <br>
	 * It is import to call <code>close()</code> after you have finished using any BuzzSQL object.
	 * <code>close()</code> will release any resources including the database connection if appropriate. It
	 * will never throw an exception, so it is always safe to call <code>close()</code>. <br>
	 * <br>
	 * You may reuse a SQL object after calling <code>close()</code>. <br>
	 * <br>
	 * The best practice to insure all BuzzSQL objects are closed is to put your call to <code>close()</code>
	 * in a finally block.
	 * 
	 * @see #usingExplicitConnection()
	 * @param commit If true BuzzSQL will commit the transaction before closing the connection.
	 * @return Reference to this object which can be used for method call chaining
	 */
	public <E extends BuzzSQL> E close(boolean commit)
	{
		if (stmt != null)
		{
			try
			{
				stmt.close();
			}
			catch (Exception e)
			{

			}
			
			stmt = null;
		}

		if(con != null)
		{
			try
			{
				if (!con.getAutoCommit() && commit)
				{
					try
					{
						con.commit();
					}
					catch (Exception e)
					{
	
					}
				}
			}
			catch (Exception e)
			{
	
			}
	
			if (!usingExplicitConnection)
			{
				try
				{
					if (con != null)
						DataSourceManager.releaseConnection(dataSourceName, con);
				}
				catch (Exception e)
				{
	
				}
	
				con = null;
			}
		}

		return (E) this;
	}

	protected PreparedStatement merge(PreparedStatement stmt) throws SQLException
	{
		if (args != null && !args.isEmpty())
		{
			for (int i = 0; i < args.size(); i++)
			{
				Object obj = args.get(i);

				if (obj == null)
					stmt.setNull(i + 1, java.sql.Types.VARCHAR);
				else if (obj instanceof Byte)
					stmt.setInt(i + 1, ((Byte) obj).byteValue());
				else if (obj instanceof Short)
					stmt.setInt(i + 1, ((Short) obj).shortValue());
				else if (obj instanceof Integer)
					stmt.setInt(i + 1, ((Integer) obj).intValue());
				else if (obj instanceof Long)
					stmt.setLong(i + 1, ((Long) obj).longValue());
				else if (obj instanceof Float)
					stmt.setFloat(i + 1, ((Float) obj).floatValue());
				else if (obj instanceof Double)
					stmt.setDouble(i + 1, ((Double) obj).doubleValue());
				else if (obj instanceof Character)
					stmt.setString(i + 1, ((Character) obj).toString());
				else if (obj instanceof String)
					stmt.setString(i + 1, (String) obj);
				else if (obj instanceof java.util.Date)
				{
					// stmt.setTimestamp(i+1, new Timestamp(((java.util.Date)obj).getTime()));
					Timestamp timestamp = new Timestamp(((java.util.Date) obj).getTime());
					stmt.setTimestamp(i + 1, timestamp);
				}
				else if (obj instanceof java.util.Calendar)
				{
					// stmt.setTimestamp(i+1, new Timestamp(((java.util.Date)obj).getTime()));
					Timestamp timestamp = new Timestamp(((java.util.Calendar) obj).getTimeInMillis());
					stmt.setTimestamp(i + 1, timestamp);
				}
				else if ((obj instanceof OutParameter) && (stmt instanceof CallableStatement))
				{
					((CallableStatement) stmt).registerOutParameter(i + 1, ((OutParameter) obj).getOutSqlType());
				}
				else if ((obj instanceof InOutParameter) && (stmt instanceof CallableStatement))
				{
					((CallableStatement) stmt).setObject(i + 1, ((InOutParameter) obj).getInValue(), ((InOutParameter) obj).getOutSqlType());
					((CallableStatement) stmt).registerOutParameter(i + 1, ((InOutParameter) obj).getOutSqlType());
				}
				else
				{
					stmt.setObject(i + 1, obj);
				}
			}
		}

		if (log.isDebugEnabled())
		{
			try
			{
				log.debug(queryToString(sql, (args == null ? null : args)));
			}
			catch (Throwable t)
			{
				log.debug("Failed to convert statement to string: " + t.getMessage(), t);
			}
		}

		return stmt;
	}

	protected void prepare() throws SQLException
	{
		if (stmt != null)
			throw new SQLException("Failed to prepare SQL for execution: not closed since prior execution");

		if (sql == null)
			throw new SQLException("Failed to prepare SQL for execution: sql is null");

		if (con != null)
			return;

		con = DataSourceManager.getConnection(dataSourceName);
	}

	/**
	 * Get information on this BuzzSQL release.
	 * 
	 * @return ReleaseInfo object with product name, version, build, etc
	 */
	public static ReleaseInfo getReleaseInfo()
	{
		return releaseInfo;
	}

	/**
	 * Utility method to create a nice readable representation of the results of merging a statment with ?
	 * placeholders with object args.
	 * 
	 * @param sql Statement
	 * @param args Args
	 * @return Nice string representation of merged SQL.
	 */
	public static String queryToString(String sql, List<Object> args)
	{
		if (sql == null)
			throw new NullPointerException();

		if (args == null || args.isEmpty())
			return sql;

		StringTokenizer st = new StringTokenizer(sql, "?", true);
		StringBuilder sb = new StringBuilder();
		String temp = null;
		String value = null;
		Iterator iter = args.iterator();
		while (st.hasMoreTokens())
		{
			temp = st.nextToken();
			if (temp.equals("?"))
			{
				Object obj = iter.next();
				if (obj instanceof String)
				{
					value = "'" + (String) obj + "'";
				}
				else if (obj instanceof java.util.Date)
				{
					value = "'" + BuzzSQL.DATABASE_FORMATTER.get().format((java.util.Date) obj) + "'";
				}
				else if (obj != null)
					value = obj.toString();
				else
					value = "null";

				sb.append(value);
			}
			else
			{
				sb.append(temp);
			}
		}
		return sb.toString();
	}
}
