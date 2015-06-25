package com.buzzsurf.sql;

import java.sql.SQLException;

public interface RowMapper<E>
{
	public E mapRow(Select select) throws SQLException;
}
