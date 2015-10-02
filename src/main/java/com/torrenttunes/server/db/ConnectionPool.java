package com.torrenttunes.server.db;

import com.torrenttunes.server.DataSources;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public enum ConnectionPool {
	pool;
	
	public HikariDataSource dataSource;
	
	private ConnectionPool() {
		HikariConfig config = new HikariConfig();
		
		String dbUrl = DataSources.DB_PROP.getProperty("dburl");
		String dbUser = DataSources.DB_PROP.getProperty("dbuser");
		String dbPass = DataSources.DB_PROP.getProperty("dbpassword");
				
		System.out.println(dbUrl);
		
		config.setJdbcUrl(dbUrl);
		config.setUsername(dbUser);
		config.setPassword(dbPass);
//		config.addDataSourceProperty("cachePrepStmts", "true");
//		config.addDataSourceProperty("prepStmtCacheSize", "250");
//		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
//		config.setDataSourceClassName("com.mysql.jdbc.Driver");
		
		dataSource = new HikariDataSource(config);
		

		
//		dataSourceClassName=com.mysql.jdbc.jdbc2.optional.MysqlDataSource
//				dataSource.url=jdbc:mysql://localhost/database
//				dataSource.user=test
//				dataSource.password=test
//				dataSource.cachePrepStmts=true
//				dataSource.prepStmtCacheSize=250
//				dataSource.prepStmtCacheSqlLimit=2048
	}
	
}
