package com.buzzsurf.sql;

import java.sql.*;

/**
 An Update object modifies rows in a database and queries the number of rows updated. It is a combination of the
 underlying JDBC classes;
 <ul>
 <li>
 <code>java.sql.Connection</code>
 </li>
 <li>
 <code>java.sql.PreparedStatement</code>
 </li>
 </ul>
 After execution, you may get the number of rows updated by calling <code>getUpdateCount()</code> or
 <code>getRowCount()</code>.
 <br>
 <br>
 As an example, the following code fragment creates an <code>Update</code> object using the automatic
 default connection to the database. The SQL statement is passed via constructor, and the arguments are passed using the
 <code>setArgs(Object...)</code> method. <code>execute()</code> is called to execute the
 database call, and then the update count is queries by calling <code>getUpdateCount()</code>. The object
 is then closed to cleanup any resources and return the connection to the pool.
 <br>
 <pre>
 Update update = new Update();
 update.setSQL("update example.table_example1 set col_str = ? where col_pk = ?");
 update.setArgs("asdf",1);
 update.execute();
 int updateCount = update.getUpdateCount();
 update.close();
 </pre>
 Note: This code does not handle exceptions. You normally should wrap your calls in try/catch/finally blocks to handle
 any problems. It is advisable to put your <code>close()</code> call in a finally block to insure the
 connection is always released.
 * @see BuzzSQL
 * @author Paul Cowan (<a href="http://www.buzzsurf.com/sql">www.buzzsurf.com/sql</a>)
 */
public class Update extends BuzzSQL
{
	protected int	rowCount	= -1;

	/**
	 A zero argument constructor is provided for simplified operation with JavBeans, SOAP, and reflection scenarios where
	 having such a constructor is necessary or convenient. At a minimum, you must call
	 <code>setSQL(String)</code> before execution when using this constructor.
	 */
	public Update()
	{
		super();
	}

	/**
	 A single argument constructor that accepts your SQL statement and uses the default DataSource.
	 * @param sql The SQL statement
	 */
	public Update(String sql)
	{
		super();
		super.setSQL(sql);
	}

	/**
	 A dual argument constructor that accepts your SQL statement and the explicit name of a DataSource to use.
	 * @param sql The SQL statement
	 * @param dataSourceName The explicit name of the dataSource to obtain a connecton from in <code>DataSourceManager</code>. 
	 */
	public Update(String sql, String dataSourceName)
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
	public Update(String sql, Connection con)
	{
		super();
		super.setConnection(con);
		super.setSQL(sql);
	}

	/**
	 * Get the number of rows updated.
	 * @return Number of rows updated
	 */
	public int getRowCount()
	{
		return rowCount;
	}

	/**
	 * Get the number of rows updated.
	 * @return Number of rows updated
	 */
	public int getUpdateCount()
	{
		return getRowCount();
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
	public Update execute() throws SQLException
	{
		prepare();
		stmt = merge(con.prepareStatement(sql));
		rowCount = stmt.executeUpdate();

		return this;
	}
}
