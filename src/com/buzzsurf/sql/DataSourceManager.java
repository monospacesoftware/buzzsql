package com.buzzsurf.sql;

import java.io.InputStream;
import java.sql.*;
import java.util.*;

import javax.naming.*;
import javax.sql.DataSource;

import org.apache.commons.logging.*;

/**
 DataSource Manager handles loading <code>javax.sql.DataSource</code> objects from a JNDI naming context.
 This is done intelligently by examining any existing naming context. All database connections obtained automatically by
 BuzzSQL come from DataSource bound in a JNDI naming context
 <br>
 <br>
 Note: DataSource Manager does not need to be used exclusively with BuzzSQL objects. It can be
 a valuable stand-alone tool for obtaining database connections. This is particularly true in a J2SE environment where
 no JNDI naming context is available. DataSource Manager will create a naming context for you and load and bind your
 dataSources conveniently.
 
 @see #initialize()
 @see #isInitialized()
 @see #initializationFailed()
 @author Paul Cowan (<a href="http://www.buzzsurf.com/sql">www.buzzsurf.com/sql</a>)
 */
public final class DataSourceManager
{
	public static final String					DEFAULT_DS_NAME			= "DEFAULT";
	public static final String					CONFIG_RESOURCE_NAME	= "/buzzsql.properties";

	private static DataSourceManager			instance				= null;
	private static boolean						initializationFailed	= false;
	private static String						defaultDataSourceName	= null;

	private static Log							log						= LogFactory.getLog(DataSourceManager.class);

	private LinkedHashMap<String, DataSource>	dataSources				= new LinkedHashMap<String, DataSource>();

	/**
	 DataSource Manager uses "lazy initialization" to create/lookup/load dataSource from the
	 JNDI naming context. This can caused a problem in some cases, as the first request may be delayed as resources are
	 loaded. To prevent this, a single simple call to the static method
	 <code>DataSourceManager.initialize()</code> will result in pre-loading of all dataSources. You can query
	 DataSource Manager on the status of initialization using the
	 <code>DataSourceManager.isInitialized()</code> and <code>DataSourceManager.initializationFailed()</code>
	 methods. These methods allow your application is determine if DataSource Manager is correctly initialized. All errors
	 during initialization will be written to the common logging system. See <a href="#BuzzSQL%20Logging">BuzzSQL Logging</a> for more
	 information on DataSource Manager logging.
	 */
	public static synchronized boolean initialize()
	{
		if (instance != null)
			return true;

		try
		{
			instance = new DataSourceManager();
			return true;
		}
		catch (Throwable e)
		{
			initializationFailed = true;
			log.fatal("DataSourceManager failed to initialize: " + e.getMessage(), e);
			return false;
		}
	}

	/**
	 * Check the status if initialization.
	 * @return true if <code>DataSourceManager</code> is initialized correctly.
	 */
	public synchronized static boolean isInitialized()
	{
		return (instance != null);
	}

	/**
	 * Check the status if initialization.
	 * You can query the initialization state of DataSource Manager by calling
	 * <code>DataSouceManager.initializationFailed()</code>. initializationFailed will return
	 * <code>true</code> if a FATAL error was detected.
	 * @return true if there was an error detected during initialization.
	 */
	public synchronized static boolean initializationFailed()
	{
		return initializationFailed;
	}

	private DataSourceManager() throws Exception
	{
		Properties config = new Properties();

		try
		{
			config.load(DataSourceManager.class.getResourceAsStream(CONFIG_RESOURCE_NAME));
			log.debug("Loading properties from \"" + CONFIG_RESOURCE_NAME + "\"");
		}
		catch (Exception e)
		{
			log.debug(CONFIG_RESOURCE_NAME + " not found in the classpath: using defaults");
			config = new Properties();
		}

		String rootContextName = config.getProperty("rootNamespace", "java:comp/env/jdbc");
		log.debug("Datasource root context is \"" + rootContextName + "\"; any dataSources must be bound under this name");

		Context ctx = null;

		Properties env = System.getProperties();
		if (!env.containsKey(Context.INITIAL_CONTEXT_FACTORY))
		{
			log.debug("No J2EE context exists, attempting to create a standalone context");
			env.put(Context.INITIAL_CONTEXT_FACTORY, "org.shiftone.ooc.InitialContextFactoryImpl");
			if (System.getProperty("org.shiftone.ooc.config") == null)
				System.setProperty("org.shiftone.ooc.config", "buzzsql-datasources.xml");
		}
		else
		{
			log.debug("Attempting to lookup dataSources from an existing J2EE context");
		}

		try
		{
			Context ic = new InitialContext(env);
			ctx = (Context) ic.lookup(rootContextName);
		}
		catch (javax.naming.NameNotFoundException e)
		{
			log.error("Could not load initial context at root namespace \"" + rootContextName + "\": " + e.getMessage(), e);
			throw e;
		}
		catch (NoInitialContextException e)
		{
			log.error("No inital context was found: " + e.getMessage());
			throw e;
		}

		DataSource firstDS = null;
		String firstDSName = null;

		String dateSourceNamesStr = config.getProperty("dataSourceNames");
		if (dateSourceNamesStr != null && dateSourceNamesStr.trim().length() > 0)
		{
			StringTokenizer st = new StringTokenizer(dateSourceNamesStr, ",");
			while (st.hasMoreTokens())
			{
				String boundName = st.nextToken().trim();
				DataSource ds = (DataSource) ctx.lookup(boundName);
				if (ds != null)
				{
					dataSources.put(boundName, ds);
					log.debug("Adding available dataSource \"" + boundName + "\"");

					if (firstDS == null)
					{
						firstDS = ds;
						firstDSName = boundName;
					}
				}
				else
				{
					log.warn("Property dataSourceNames value \"" + boundName + "\"  does not refer to a known dataSource, or the dataSource failed initialization!");
				}
			}
		}
		else
		{
			searchSubContext(ctx, "");
		}

		if (!dataSources.isEmpty())
		{
			DataSource defaultDataSource = null;
			defaultDataSourceName = config.getProperty("defaultDataSourceName");

			if (defaultDataSourceName != null && defaultDataSourceName.trim().length() > 0)
			{
				DataSource ds = dataSources.get(defaultDataSourceName);
				if (ds != null)
				{
					defaultDataSource = ds;
				}
				else
				{
					log.warn("Property defaultDataSourceName \"" + defaultDataSourceName + "\" does not refer to a known dataSource, or the dataSource failed initialization!");
					defaultDataSourceName = null;
				}
			}

			if (defaultDataSource == null && firstDS != null)
			{
				defaultDataSource = firstDS;
				defaultDataSourceName = firstDSName;
			}

			if (defaultDataSource == null)
			{
				Map.Entry<String, DataSource> entry = dataSources.entrySet().iterator().next();
				defaultDataSourceName = entry.getKey();
				defaultDataSource = entry.getValue();

				if (dataSources.size() > 1)
					log.warn("Multiple dataSources were found without a clear default; set the propety \"defaultDataSourceName\" to prevent ambiguity");
			}

			dataSources.put(DEFAULT_DS_NAME, defaultDataSource);
			log.debug("Using dataSource named \"" + defaultDataSourceName + "\" as the default");
		}
		else
		{
			log.warn("DataSourceManager initialization failed: No available dataSources were found!");
		}
	}

	private void searchSubContext(Context ctx, String lastName) throws Exception
	{
		NamingEnumeration<Binding> namingEnum = ctx.listBindings("");
		while (namingEnum.hasMoreElements())
		{
			Binding binding = namingEnum.nextElement();
			String boundName = binding.getName();
			Object boundObject = binding.getObject();

			log.debug("Found bound object \"" + boundName + "\" of type " + boundObject.getClass().getName());

			if (boundObject instanceof javax.naming.Context)
			{
				log.debug("Serching subcontext " + lastName + boundName + "/ for dataSources");
				searchSubContext((Context) boundObject, lastName + boundName + "/");
			}
			else if (boundObject instanceof javax.sql.DataSource)
			{
				String fullName = lastName + boundName;
				DataSource ds = (DataSource) ctx.lookup(boundName);
				dataSources.put(fullName, ds);
				log.debug("Adding available dataSource \"" + fullName + "\" of type " + ds.getClass().getName());
			}
			else if (boundObject instanceof javax.naming.Reference)
			{
				String fullName = lastName + boundName;
				Object o = ctx.lookup(boundName);

				if (o == null)
				{
					log.warn("Bound object \"" + fullName + "\" refers to a null dataSource; driver initialization may have failed");
				}
				else if (o instanceof javax.sql.DataSource)
				{
					DataSource ds = (DataSource) o;
					dataSources.put(fullName, ds);
					log.debug("Adding available dataSource \"" + fullName + "\" of type " + ds.getClass().getName());
				}
				else
				{
					log.debug("Found reference name \"" + boundName + "\" to object type " + ((Reference) boundObject).getClassName());
				}
			}
		}
	}

	/**
	 * Get the name of the default dataSource
	 * @return The default dataSource name
	 */
	public static String getDefaultDataSourceName()
	{
		return defaultDataSourceName;
	}

	/**
	 * Get a <code>java.sql.Connection</code> object from the default dataSource.
	 * @return The JDBC connection
	 * @throws SQLException If <code>DataSourceManager</code> failed to initialize correctly or the <code>DataSource</code>
	 * was unable to connect to the database.
	 */
	public static Connection getConnection() throws SQLException
	{
		return getConnection(null);
	}

	/**
	 DataSources are made available to BuzzSQL using a name that is based on the JNDI path. For example, a dataSource bound
	 in your JNDI naming context at <code>java:comp/env/jdbc/testDB</code> would be named
	 <code>testDB</code>, since <code>java:comp/env/jdbc/</code> is the default root namespace search path.
	 <br>
	 <br>
	 All dataSources found under the root namespace are made available in DataSource Manager. This can create a problem if
	 you have multiple dataSource bound in JNDI; the default dataSource is non-deterministic when there are multiple
	 dataSources available. To fix this problem, you must either always specify a <code>dataSourceName</code>
	 in the constructor of your BuzzSQL objects, call <code>setDataSourceName(String)</code> on your BuzzSQL
	 objects, or include the <code>buzzsql.properties</code> file with the
	 <code>defaultDataSourceName</code> property.
	 
	 * @param dataSourceName The name of the dataSource as loaded by <code>DataSourceManager</code> based on the bound JNDI name.
	 * @return The JDBC connection
	 * @throws SQLException If <code>DataSourceManager</code> failed to initialize correctly or the <code>DataSource</code>
	 * was unable to connect to the database.
	 */
	public synchronized static Connection getConnection(String dataSourceName) throws SQLException
	{
		if (instance == null && !initializationFailed)
			initialize();

		if (instance == null && initializationFailed)
			throw new SQLException("DataSourceManger initialization failed: no dataSources available!");

		if (instance.dataSources.isEmpty())
			throw new SQLException("No available dataSources were found during DataSourceManger initialization!");

		String name = (dataSourceName == null ? DEFAULT_DS_NAME : dataSourceName);

		DataSource ds = instance.dataSources.get(name);
		if (ds != null)
			return ds.getConnection();
		else
			throw new SQLException("DataSource \"" + name + "\" is unrecognized or is not initialized!");
	}

	/**
	 * Release the JDBC connection
	 * @param con
	 */
	public static void releaseConnection(Connection con)
	{
		releaseConnection(null, con);
	}

	/**
	 * Release the JDBC connection to the specified dataSource
	 * @param dataSourceName
	 * @param con
	 */
	public static void releaseConnection(String dataSourceName, Connection con)
	{
		//String name = (dsName == null ? DEFAULT_DS_NAME : dsName);

		try
		{
			// since the connection is comming from a DataSource all we need to do here is close it to return it to the pool (i think...)
			con.close();
		}
		catch (Exception e)
		{
		}
	}

	/**
	 * Get a collection of all available dataSource names
	 * @return An unmodifiable collection of Strings
	 */
	public synchronized static Collection<String> listDataSourceNames()
	{
		return Collections.unmodifiableCollection(instance.dataSources.keySet());
	}
}
