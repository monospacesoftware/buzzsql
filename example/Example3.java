import java.util.*;
import java.sql.SQLException;

import com.buzzsurf.sql.*;

public class Example3
{
	public static void main(String args[]) throws SQLException
	{
		new Example3();
	}

	public Example3() throws SQLException
	{
		// simple select with result set iteration
		Select query1 = new Select().setSQL("select col_pk, col_string, col_int from example1.table_example1");
		query1.execute(true);
		while (query1.next())
		{
			System.out.println(query1.getLine());
		}
		query1.close();
	}
}
