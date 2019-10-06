package org.teasoft.honey.osql.core;

import org.teasoft.honey.osql.constant.DbConfigConst;

/**
 * @author Kingstar
 * @since  1.0
 */
public final class HoneyConfig {

	private static HoneyConfig honeyConfig = null;
	static {
		honeyConfig = new HoneyConfig();
		honeyConfig.init(); // just run one time
	}

	private HoneyConfig() {}

	public static HoneyConfig getHoneyConfig() {

		return honeyConfig;
	}

	private void init() {
		setDbName(BeeProp.getBeeProp("bee.databaseName"));
		setShowSQL(Boolean.parseBoolean(BeeProp.getBeeProp("bee.osql.showSQL")));
		String t_batchSize = BeeProp.getBeeProp("bee.osql.select.batchSize");
		if (t_batchSize != null) setBatchSize(Integer.parseInt(t_batchSize));
		String t_maxResultSize = BeeProp.getBeeProp("bee.osql.select.maxResultSize");
		if (t_maxResultSize != null) setMaxResultSize(Integer.parseInt(t_maxResultSize));

		setUnderScoreAndCamelTransform(Boolean.parseBoolean(BeeProp.getBeeProp("bee.osql.underScoreAndCamelTransform")));
		setDbNamingToLowerCaseBefore(Boolean.parseBoolean(BeeProp.getBeeProp("bee.osql.dbNaming.toLowerCaseBefore")));
		//		BeeProp.getBeeProp("bee.osql.delete.isAllowDeleteAllDataInOneTable");
		setIgnoreNullInSelectJson(Boolean.parseBoolean(BeeProp.getBeeProp("bee.osql.selectJson.ignoreNull"))); //2019-08-17
		setTimestampWithMillisecondInSelectJson(Boolean.parseBoolean(BeeProp.getBeeProp("bee.osql.selectJson.timestamp.withMillisecond")));
		setDateWithMillisecondInSelectJson(Boolean.parseBoolean(BeeProp.getBeeProp("bee.osql.selectJson.date.withMillisecond")));
		setTimeWithMillisecondInSelectJson(Boolean.parseBoolean(BeeProp.getBeeProp("bee.osql.selectJson.time.withMillisecond")));
		setNullToEmptyStringInReturnStringList(Boolean.parseBoolean(BeeProp.getBeeProp("bee.osql.select.returnStringList.nullToEmptyString"))); 
		
		setDriverName(BeeProp.getBeeProp(DbConfigConst.DB_DRIVERNAME));
		setUrl(BeeProp.getBeeProp(DbConfigConst.DB_URL));
		setUsername(BeeProp.getBeeProp(DbConfigConst.DB_USERNAM));
		setPassword(BeeProp.getBeeProp(DbConfigConst.DB_PASSWORD));
		
//		setCacheType(BeeProp.getBeeProp("bee.osql.cache.type"));  //暂时只有FIFO
		
		String t1 = BeeProp.getBeeProp("bee.osql.cache.map.size"); //缓存集数据量大小
		if (t1 != null) setCacheMapSize(Integer.parseInt(t1));
		
		String t2 = BeeProp.getBeeProp("bee.osql.cache.timeout");//缓存保存时间(毫秒 ms)
		if (t2 != null) setCacheTimeout(Integer.parseInt(t2));
		
		String t3 = BeeProp.getBeeProp("bee.osql.cache.work.resultSet.size"); //resultset超过一定的值将不会放缓存
		if (t3 != null) setCacheWorkResultSetSize(Integer.parseInt(t3));
		
		String t4 = BeeProp.getBeeProp("bee.osql.cache.startDeleteCache.rate"); 
		if (t4 != null) setStartDeleteCacheRate(Double.parseDouble(t4));
		
		String t5 = BeeProp.getBeeProp("bee.osql.cache.fullUsed.rate"); 
		if (t5 != null) setCachefullUsedRate(Double.parseDouble(t5));
		
		String t6 = BeeProp.getBeeProp("bee.osql.cache.fullClearCache.rate"); 
		if (t6 != null) setFullClearCacheRate(Double.parseDouble(t6));
	}

	// 启动时动态获取
	private boolean showSQL;
	private int batchSize = 100; //不设置,默认100
	private String dbName;
	private boolean underScoreAndCamelTransform;
	private boolean dbNamingToLowerCaseBefore;
	
	private boolean ignoreNullInSelectJson;//2019-08-17
	private boolean timestampWithMillisecondInSelectJson;
	private boolean dateWithMillisecondInSelectJson;
	private boolean timeWithMillisecondInSelectJson;
	private boolean nullToEmptyStringInReturnStringList;

	private String driverName;
	private String url;
	private String username;
	private String password;
	private int maxResultSize;
	
	private int cacheTimeout;
	private int cacheWorkResultSetSize;
	private int cacheMapSize;
	private String cacheType="FIFO";
	
	private static double startDeleteCacheRate;  //when timeout use
	private static double cachefullUsedRate;      //when add element in cache use
	private static double fullClearCacheRate;  //when add element in cache use

	private void setShowSQL(boolean showSQL) {
		this.showSQL = showSQL;
	}

	private void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	private void setDbName(String dbName) {
		this.dbName = dbName;
	}

	private void setUnderScoreAndCamelTransform(boolean underScoreAndCamelTransform) {
		this.underScoreAndCamelTransform = underScoreAndCamelTransform;
	}

	private void setDbNamingToLowerCaseBefore(boolean dbNamingToLowerCaseBefore) {
		this.dbNamingToLowerCaseBefore = dbNamingToLowerCaseBefore;
	}

	private void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	private void setUrl(String url) {
		this.url = url;
	}

	private void setUsername(String username) {
		this.username = username;
	}

	private void setPassword(String password) {
		this.password = password;
	}

	private void setMaxResultSize(int maxResultSize) {
		this.maxResultSize = maxResultSize;
	}

	public boolean isShowSQL() {
		return showSQL;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public String getDbName() {
		return dbName;
	}

	public boolean isUnderScoreAndCamelTransform() {
		return underScoreAndCamelTransform;
	}

	public boolean isDbNamingToLowerCaseBefore() {
		return dbNamingToLowerCaseBefore;
	}

	public String getDriverName() {
		return driverName;
	}

	public String getUrl() {
		return url;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public int getMaxResultSize() {
		return maxResultSize;
	}

	public boolean isIgnoreNullInSelectJson() {
		return ignoreNullInSelectJson;
	}

	private void setIgnoreNullInSelectJson(boolean ignoreNullInSelectJson) {
		this.ignoreNullInSelectJson = ignoreNullInSelectJson;
	}
	
	public boolean isTimestampWithMillisecondInSelectJson() {
		return timestampWithMillisecondInSelectJson;
	}

	private void setTimestampWithMillisecondInSelectJson(boolean timestampWithMillisecondInSelectJson) {
		this.timestampWithMillisecondInSelectJson = timestampWithMillisecondInSelectJson;
	}

	public boolean isDateWithMillisecondInSelectJson() {
		return dateWithMillisecondInSelectJson;
	}

	private void setDateWithMillisecondInSelectJson(boolean dateWithMillisecondInSelectJson) {
		this.dateWithMillisecondInSelectJson = dateWithMillisecondInSelectJson;
	}

	public boolean isTimeWithMillisecondInSelectJson() {
		return timeWithMillisecondInSelectJson;
	}

	private void setTimeWithMillisecondInSelectJson(boolean timeWithMillisecondInSelectJson) {
		this.timeWithMillisecondInSelectJson = timeWithMillisecondInSelectJson;
	}

	public boolean isNullToEmptyStringInReturnStringList() {
		return nullToEmptyStringInReturnStringList;
	}

	private void setNullToEmptyStringInReturnStringList(boolean nullToEmptyStringInReturnStringList) {
		this.nullToEmptyStringInReturnStringList = nullToEmptyStringInReturnStringList;
	}

	public int getCacheTimeout() {
		return cacheTimeout;
	}

	public int getCacheMapSize() {
		return cacheMapSize;
	}

	private void setCacheTimeout(int cacheTimeout) {
		this.cacheTimeout = cacheTimeout;
	}

	private void setCacheMapSize(int cacheMapSize) {
		this.cacheMapSize = cacheMapSize;
	}

	public int getCacheWorkResultSetSize() {
		return cacheWorkResultSetSize;
	}

	private void setCacheWorkResultSetSize(int cacheWorkResultSetSize) {
		this.cacheWorkResultSetSize = cacheWorkResultSetSize;
	}

	public String getCacheType() {
		return cacheType;
	}

	private void setCacheType(String cacheType) {
		this.cacheType = cacheType;
	}

	private static void setStartDeleteCacheRate(double startDeleteCacheRate) {
		HoneyConfig.startDeleteCacheRate = startDeleteCacheRate;
	}

	private static void setCachefullUsedRate(double cachefullUsedRate) {
		HoneyConfig.cachefullUsedRate = cachefullUsedRate;
	}

	private static void setFullClearCacheRate(double fullClearCacheRate) {
		HoneyConfig.fullClearCacheRate = fullClearCacheRate;
	}

	public static double getStartDeleteCacheRate() {
		return startDeleteCacheRate;
	}

	public static double getCachefullUsedRate() {
		return cachefullUsedRate;
	}

	public static double getFullClearCacheRate() {
		return fullClearCacheRate;
	}
	
}