import java.util.*;
import java.sql.SQLException;

import com.buzzsurf.sql.*;

public class Example2
{
	public static void main(String args[]) throws SQLException
	{
		new Example2();
	}

	public Example2() throws SQLException
	{
		// simple select with result set iteration
		Select query1 = new Select().setSQL("select col_pk, col_string, col_int from example1.table_example1");
		query1.setRowMapper(new TestRowMapper());
		query1.execute();
		while (query1.next())
		{
			TestObj obj = query1.getMappedObject();
			System.out.println(obj);
		}
		query1.close();
	}
	
	protected class TestRowMapper implements com.buzzsurf.sql.RowMapper<TestObj>
	{
		public TestObj mapRow(Select select) throws SQLException
		{
			return new TestObj(select.getInt(1), select.getString(2));
		}
	}
	
	protected class TestObj
	{
		protected int pk;
		protected String str;
		
		protected TestObj(int pk, String str)
		{
			this.pk = pk;
			this.str = str;
		}

		public int getPk()
		{
			return pk;
		}

		public void setPk(int pk)
		{
			this.pk = pk;
		}

		public String getStr()
		{
			return str;
		}

		public void setStr(String str)
		{
			this.str = str;
		}

	}
}

