import java.util.*;
import java.sql.SQLException;
import java.sql.Connection;

import com.buzzsurf.sql.*;

public class Example1
{
	public static void main(String args[]) throws SQLException
	{
		new Example1();
	}

	public Example1() throws SQLException
	{
		// Release info usage
		ReleaseInfo releaseInfo = BuzzSQL.getReleaseInfo();
		System.out.println(releaseInfo.getWelcome());
		
		System.out.println();

		// simple delete
		Delete delete1 = new Delete("delete from example1.table_example1");
		delete1.execute();
		delete1.close();

		// insert with retrieval of auto-increment primary key
		Insert insert1 = new Insert("insert into example1.table_example1(col_string, col_int) values (?,?)");
		insert1.setArgs("asdf", 123);
		insert1.execute();
		int pk1 = insert1.getGeneratedKey();
		System.out.println(pk1);
		insert1.close();

		// simple select with result set iteration
		Select query1 = new Select().setSQL("select col_pk, col_string, col_int from example1.table_example1");
		query1.execute();
		while (query1.next())
			System.out.println(query1.getLine());
		query1.close();

		// update with setArgs method chaining
		Update update1 = new Update("update example1.table_example1 set col_int = ? where col_string = ?").setArgs(456, "asdf");
		update1.execute();
		update1.close();

		// simple insert followed by object reuse
		Insert insert2 = new Insert("insert into example1.table_example1(col_string, col_int) values (?,?)");
		insert2.setArgs("jkl;", 000);
		insert2.execute();
		insert2.close();

		insert2.setArgs("qwerty", 9999);

		insert2.execute();
		insert2.close();

		// delete with setArgs chaining
		Delete delete2 = new Delete().setSQL("delete from example1.table_example1 where col_int = ?").setArgs(456);
		delete2.execute();
		delete2.close();

		// reuse of prior Select object
		query1.execute();
		while (query1.next())
			System.out.println(query1.getLine());
		query1.close();
		
		Calendar c = Calendar.getInstance();
		c.roll(Calendar.MONTH, -1);
		
		// insert with usage of a calendar object to set a datetime column value
		Insert insert3 = new Insert("insert into example1.table_example1(col_int, col_date) values (?,?)");
		insert3.setArgs(234, c);
		insert3.execute();
		
		// select with datetime retrieval as a Date object
		Select query2 = new Select("select col_pk, col_date from example1.table_example1 where col_int = ?").setArgs(234);
		query2.execute();
		while(query2.next())
		{
			Date date2 = query2.getDate("col_date");
			System.out.println("date from db = " + date2);
		}
		
		// select with datetime retrieval as a Calendar object
		Select query3 = new Select("select col_pk, col_int, col_date from example1.table_example1 where col_date =?").setArgs(c);
		query3.execute();
		while(query3.next())
		{
			int i = query3.getInt("col_int");
			c = query3.getCalendar("col_date");
		}
		
		c.roll(Calendar.MONTH, -1);
		
		// simple update using modified Calendar object from prior query result
		Update update2 = new Update("update example1.table_example1 set col_date = ? where col_int = ?");
		update2.setArgs(c, 234);
		update2.execute();
		update2.close();
		
		// update with full method chaining
		new Update("update example1.table_example1 set col_date = ? where col_int = ?").setArgs(c, 234).execute().close();
		
		// stored procedure with IN and INOUT parameter usage
		InOutParameter inOutParam = new InOutParameter(java.sql.Types.INTEGER, 5);
		OutParameter outParam = new OutParameter(java.sql.Types.VARCHAR);
		StoredProcedure storedProc = new StoredProcedure("call example1.storedproc_example1(?, ?, ?)");
		storedProc.setArgs("asdf", inOutParam, outParam);
		storedProc.execute();
		if (storedProc.hasResultSet())
		{
			while (storedProc.next())
				System.out.println(storedProc.getLine());
		}
		System.out.println(inOutParam.getInt());
		System.out.println(outParam.getString());
		storedProc.close();	
		
		// 2 simple updates executed inside a transaction via an explicit connection
		
		// get a connection from DataSourceManager
		Connection con = DataSourceManager.getConnection();
		
		// make sure auto-commit is not enabled (it is by default)
		con.setAutoCommit(false);
		
		Update update3 = null;
		Update update4 = null;
		boolean tranSuccess = true;
		
		try
		{
			// simple update 1
			update3 = new Update("update example1.table_example1 set col_string = ? where col_int = ?", con);
			update3.setArgs("tran", 234);
			update3.execute();

			// simple update 2
			update4 = new Update("update example1.table_example1 set col_string = ? where col_int = ?", con);
			update4.setArgs("tran", 567);
			update4.execute();
		}
		catch(Exception e)
		{
			// if exception was thrown then transaction was not successfull
			tranSuccess = false;
			e.printStackTrace();
		}
		finally
		{
			// always close the SQL objects regardless of execution success
			if(update3 != null)
				update3.close();
			
			// always close the SQL objects regardless of execution success
			if(update4 != null)
				update4.close();
			
			try
			{
				if(tranSuccess)
					// if no exception was thrown then commit the transaction
					con.commit();
				else
					// otherwise an exception was thrown so rollback the transaction
					con.rollback();
			}
			catch(Exception e2)
			{
				// there was a problem encountered commiting the transaction
				e2.printStackTrace();
			}
			
			// reset the autocommit status
			con.setAutoCommit(true);
			
			// release the connection back to DataSourceManager
			DataSourceManager.releaseConnection(con);
		}
	}
}

