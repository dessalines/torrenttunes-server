package com.torrenttunes.server.scheduled;


import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.torrenttunes.server.DataSources;
import com.torrenttunes.server.tools.Tools;


public class BuildFastTables implements Job {

	static final Logger log = LoggerFactory.getLogger(BuildFastTables.class);


	private static void create() {
		Connection c = null;

		try {
			Class.forName("com.mysql.jdbc.Driver");


			Properties prop = DataSources.DB_PROP;

			c = DriverManager.getConnection(prop.getProperty("dburl") + "?useUnicode=true&characterEncoding=UTF-8", 
					prop.getProperty("dbuser"), 
					prop.getProperty("dbpassword"));

			Tools.runSQLFile(c, new File(DataSources.SQL_FAST_TABLES_FILE()));
		
			c.close();

			log.info("Fast Tables created succesfully.");

		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		create();
	}
}
