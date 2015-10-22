package com.torrenttunes.server.scheduled;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.torrenttunes.server.DataSources;


public class BuildFastTables implements Job {

	static final Logger log = LoggerFactory.getLogger(BuildFastTables.class);


	private static void create() {


		try {

			Properties prop = DataSources.DB_PROP;

			ArrayList<String> cmd = new ArrayList<String>();
			cmd.add("mysql");
			cmd.add("-u" + prop.getProperty("dbuser"));
			cmd.add("-p" + prop.getProperty("dbpassword"));
			cmd.add(prop.getProperty("dburl"));
			cmd.add("<");
			cmd.add(DataSources.SQL_FAST_TABLES_FILE());


			ProcessBuilder b = new ProcessBuilder(cmd);
			b.inheritIO();
			Process p;

			p = b.start();


			p.waitFor();

			log.info("Fast Tables created succesfully.");
			
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		


	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		create();
	}
}
