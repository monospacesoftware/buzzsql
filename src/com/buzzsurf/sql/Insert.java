package com.buzzsurf.sql;

import java.sql.*;

/**
 An Insert object inserts new rows to a table and queries the number of rows inserted. It is a combination of the
 underlying JDBC classes;
 <ul>
 <li>
 <code>java.sql.Connection</code>
 </li>
 <li>
 <code>java.sql.PreparedStatement</code>
 </li>
 </ul>
 After execution, you may get the number of rows inserted by calling <code>getInsertCount()</code> or
 <code>getRowCount()</code>.
 <br>
 <br>
 If the database table contains an auto increment column, the value can be retrieved using
 <code>getGeneratedKey()</code>. This method allows the retrieval of only the first auto increment column
 in the table, and only as an <code>int</code>. BuzzSQL always allows access to the underlying base JDBC
 object if you need more control, such as to retrieve other auto increment column values.
 <br>
 <br>
 <code>com.buzzsurf.sql.Insert extends com.buzzsurf.sql.Update</code>, but add only minor functionality
 beyond that which is contained in the parent class; <code>getGeneratedKey()</code> being the primary
 addition. Therefore you may choose to use <code>com.buzzsurf.sql.Update</code> to perform inserts if it
 helps to streamline your object structure.
 <br>
 <br>
 As an example, the following code fragment creates an <code>Insert</code> object using the automatic
 default connection to the database. The SQL statement is passed via constructor, and the arguments are passed using the
 <code>setArgs(Object...)</code> method. <code>execute()</code> is called to execute the
 database call, and then the primary key column value is retrieved using <code>getGeneratedKey()</code>.
 The object is then closed to cleanup any resources and return the connection to the pool.
 <br>
 <pre>
 Insert insert = new Insert("insert into example.table_example1(col_str, col_int) values (?,?)")
 insert.setArgs("asdf", 123);
 insert.execute();
 int pk = insert.getGeneratedKey();
 System.out.println(pk);
 insert.close();
 </pre>
 Note: This code does not handle exceptions. You normally should wrap your calls in try/catch/finally blocks to handle
 any problems. It is advisable to put your <code>close()</code> call in a finally block to insure the
 connection is always released.
 * @see BuzzSQL
 * @see Update
 * @author Paul Cowan (<a href="http://www.buzzsurf.com/sql">www.buzzsurf.com/sql</a>)
 */
public class Insert extends Update
{
	/**
	 A zero argument constructor is provided for simplified operation with JavBeans, SOAP, and reflection scenarios where
	 having such a constructor is necessary or convenient. At a minimum, you must call
	 <code>setSQL(String)</code> before execution when using this constructor.
	 */
	public Insert()
	{
		super();
	}

	/**
	 A single argument constructor that accepts your SQL statement and uses the default DataSource.
	 * @param sql The SQL statement
	 */
	public Insert(String sql)
	{
		super();
		super.setSQL(sql);
	}

	/**
	 A dual argument constructor that accepts your SQL statement and the explicit name of a DataSource to use.
	 * @param sql The SQL statement
	 * @param dataSourceName The explicit name of the dataSource to obtain a connecton from in <code>DataSourceManager</code>. 
	 */
	public Insert(String sql, String dataSourceName)
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
	public Insert(String sql, Connection con)
	{
		super();
		super.setConnection(con);
		super.setSQL(sql);
	}

	/**
	 * Get the number of rows inserted.
	 * @return Number of rows inserted
	 */
	public int getInsertCount()
	{
		return getRowCount();
	}

	/**
	 If the database table contains an auto increment column, the value can be retrieved using
	 <code>getGeneratedKey()</code>. This method allows the retrieval of only the first auto increment column
	 in the table, and only as an <code>int</code>. BuzzSQL always allows access to the underlying base JDBC
	 object if you need more control, such as to retrieve other auto increment column values.
	 * @return The value or -1 if the Object is not executing or the table had no auto-increment columns.
	 * @throws SQLException if a database access error occurs
	 */
	public int getGeneratedKey() throws SQLException
	{
		if (stmt == null)
			return -1;

		ResultSet rs = stmt.getGeneratedKeys();
		if (!rs.next())
			return -1;

		return rs.getInt(1);
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
	public Insert execute() throws SQLException
	{
		super.execute();
		return this;
	}
}
