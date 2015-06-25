package com.buzzsurf.sql;

import java.sql.*;

/**
 A StoredProcedure object executes a stored program in a relational database. A StoredProcedure is similar to a Select
 object, as it can have a result set. It is also similar to an Update object, as it can have an update count. It is a
 combination of the underlying JDBC classes;
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
 After execution, you may call <code>hasResultSet()</code> to find out of the StoredProcedure produced a
 result set. If it has a result set, you may iterate through the <code>StoredProcedure</code> object using
 <code>next()</code>, and retrieve your results in any format using one of the variety of
 <code>get</code> methods. You must call <code>next()</code> at least once before any
 <code>get</code> call to set the index in the result set to the first result. You may also get the number
 of rows updated by calling <code>getUpdateCount()</code> or <code>getRowCount()</code>.
 <br>
 <br>
 Some databases allow a stored procedure to return results in the form of OUT or INOUT parameters. BuzzSQL supports this
 through the use of the <code>com.buzzsurf.sql.OutParameter</code> and
 <code>com.buzzsurf.sql.InOutParameter</code> objects. For more detailed information see the sections on
 <a href="#OutParameter">OutParameter</a> and <a href="#InOutParameter">InOutParameter</a> objects.
 <br>
 <br>
 As an example, the following code fragment creates a <code>StoredProcedure</code> object using the
 automatic default connection to the database. The SQL statement is passed via constructor, and the arguments are passed
 using the <code>setArgs(Object...)</code> method. This example makes use of an
 OutParameter and an InOutParameter to demonstrate the usage of
 these classes. OUT and INOUT parameters are used only in conjunction with stored procedures.
 <code>execute()</code> is called to execute the database call, and then results are iterated through using
 <code>next()</code>. The object is then closed to cleanup any resources and return the connection to the pool.
 <br>
 <pre>
 OutParameter outParam = new OutParameter(java.sql.Types.VARCHAR);
 InOutParameter inOutParam = new InOutParameter(java.sql.Types.INTEGER, 10);
 StoredProcedure storedProc = new StoredProcedure("call test.storedproc_test(?, ?, ?)");
 storedProc.setArgs("asdf", outParam, inOutParam);
 storedProc.execute();
 if (storedProc.hasResultSet())
 {
 while (storedProc.next())
 System.out.println(storedProc.getLine());
 }
 System.out.println(outParam.getString());
 System.out.println(inOutParam.getInt());
 storedProc.close();
 </pre>
 Note: This code does not handle exceptions. You normally should wrap your calls in try/catch/finally blocks to handle
 any problems. It is advisable to put your <code>close()</code> call in a finally block to insure the
 connection is always released.
 * @see BuzzSQL
 * @see Select
 * @see Update
 * @see OutParameter
 * @see InOutParameter
 * @author Paul Cowan (<a href="http://www.buzzsurf.com/sql">www.buzzsurf.com/sql</a>)
 */
public class StoredProcedure extends Select
{
	protected boolean	hasResultSet	= false;
	protected int		updateCount		= -1;

	/**
	 A zero argument constructor is provided for simplified operation with JavBeans, SOAP, and reflection scenarios where
	 having such a constructor is necessary or convenient. At a minimum, you must call
	 <code>setSQL(String)</code> before execution when using this constructor.
	 */
	public StoredProcedure()
	{
		super();
	}

	/**
	 A single argument constructor that accepts your SQL statement and uses the default DataSource.
	 * @param sql The SQL statement
	 */
	public StoredProcedure(String sql)
	{
		super();
		super.setSQL(sql);
	}

	/**
	 A dual argument constructor that accepts your SQL statement and the explicit name of a DataSource to use.
	 * @param sql The SQL statement
	 * @param dataSourceName The explicit name of the dataSource to obtain a connecton from in <code>DataSourceManager</code>. 
	 */
	public StoredProcedure(String sql, String dataSourceName)
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
	public StoredProcedure(String sql, Connection con)
	{
		super();
		super.setConnection(con);
		super.setSQL(sql);
	}

	/**
	 * Returns a <code>boolean</code> to
	 * indicate the form of the first result.  You must call either the method
	 * <code>getResultSet</code> or <code>getUpdateCount</code>
	 * to retrieve the result; you must call <code>getMoreResults</code> to
	 * move to any subsequent result(s).
	 *
	 * @return <code>true</code> if the first result is a <code>ResultSet</code>
	 *         object; <code>false</code> if the first result is an update
	 *         count or there is no result
	 */
	public boolean hasResultSet()
	{
		return hasResultSet;
	}

	/**
	 * Get the number of rows updated.
	 * @return Number of rows updated
	 */
	public int getUpdateCount()
	{
		return updateCount;
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
	public StoredProcedure execute() throws SQLException
	{
		prepare();
		stmt = merge(con.prepareCall(sql));
		hasResultSet = stmt.execute();
		updateCount = ((CallableStatement) stmt).getUpdateCount();

		if (hasResultSet)
		{
			rs = stmt.getResultSet();
			if (rs != null)
				rsmd = rs.getMetaData();
		}

		if (args != null && !args.isEmpty())
		{
			for (int i = 0; i < args.size(); i++)
			{
				Object obj = args.get(i);

				if (obj instanceof OutParameter)
				{
					((OutParameter) obj).setOutValue(((CallableStatement) stmt).getObject(i + 1));
				}
			}
		}

		return this;
	}
}
