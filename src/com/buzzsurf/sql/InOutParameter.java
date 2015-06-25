package com.buzzsurf.sql;

/**
 * <p>
 * An object that models a stored procedure INOUT parameter.
 * <p>
 * See <code>com.buzzsurf.sql.StoredProcedure</code> for usage information.
 * <p>
 * @see StoredProcedure
 * @see java.sql.Types
 * @author Paul Cowan (<a href="http://www.buzzsurf.com/sql">www.buzzsurf.com/sql</a>)
 */
public class InOutParameter extends OutParameter
{
	protected Object	inValue;

	/**
	 * Empty constructor provided for convenience
	 */
	public InOutParameter()
	{
		super();
	}

	/**
	 * Construct a stored procedure INOUT parameter of the specified type
	 * @param outSqlType A <code>java.sql.Type</code> constant int
	 * @param inValue The input parameter value
	 */
	public InOutParameter(int outSqlType, Object inValue)
	{
		super(outSqlType);
		setInValue(inValue);
	}

	/**
	 * 
	 * @param inValue The input parameter value
	 */
	public void setInValue(Object inValue)
	{
		this.inValue = inValue;
	}

	/**
	 * 
	 * @return The input parameter value
	 */
	public Object getInValue()
	{
		return inValue;
	}
}
