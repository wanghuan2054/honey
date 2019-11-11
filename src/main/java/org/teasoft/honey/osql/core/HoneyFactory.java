package org.teasoft.honey.osql.core;

import org.teasoft.bee.osql.BeeSql;
import org.teasoft.bee.osql.Cache;
import org.teasoft.bee.osql.CallableSql;
import org.teasoft.bee.osql.NameTranslate;
import org.teasoft.bee.osql.ObjToSQL;
import org.teasoft.bee.osql.ObjToSQLRich;
import org.teasoft.bee.osql.PreparedSql;
import org.teasoft.bee.osql.Suid;
import org.teasoft.bee.osql.SuidRich;
import org.teasoft.bee.osql.dialect.DbFeature;
import org.teasoft.bee.osql.exception.NoConfigException;
import org.teasoft.honey.osql.constant.DatabaseConst;
import org.teasoft.honey.osql.dialect.mysql.MySqlFeature;
import org.teasoft.honey.osql.dialect.oracle.OracleFeature;
import org.teasoft.honey.osql.dialect.sqlserver.SqlServerFeature;
import org.teasoft.honey.osql.name.UnderScoreAndCamelName;

/**
 * @author Kingstar
 * @since  1.0
 */
public class HoneyFactory {
	
	private Suid suid;
	private SuidRich suidRich;
	private BeeSql beeSql;
	private ObjToSQL objToSQL;
	private ObjToSQLRich objToSQLRich;
	private PreparedSql preparedSql;
	private CallableSql callableSql;
	
	private DbFeature dbFeature;
	private NameTranslate nameTranslate;
	private Cache cache;

	public Suid getSuid() {
		if(suid==null) return new ObjSQL();
		else return suid; //可以通过配置spring bean的方式注入
	}

	public void setSuid(Suid suid) {
		this.suid = suid;
	}
	
	public SuidRich getSuidRich() {
		if(suidRich==null) return new ObjSQLRich();
		else return suidRich;
	}

	public void setSuidRich(SuidRich suidRich) {
		this.suidRich = suidRich;
	}

	public BeeSql getBeeSql() {
		if(this.beeSql==null) return new SqlLib();
		return beeSql;
	}

	public void setBeeSql(BeeSql beeSql) {
		this.beeSql = beeSql;
	}

	public ObjToSQL getObjToSQL() {
		if(objToSQL==null) return new ObjectToSQL();
		else return objToSQL;
	}

	public void setObjToSQL(ObjToSQL objToSQL) {
		this.objToSQL = objToSQL;
	}

	public ObjToSQLRich getObjToSQLRich() {
		if(objToSQLRich==null) return new ObjectToSQLRich();
		else return objToSQLRich;
	}

	public void setObjToSQLRich(ObjToSQLRich objToSQLRich) {
		this.objToSQLRich = objToSQLRich;
	}

	public PreparedSql getPreparedSql() {
		if(preparedSql==null) return new PreparedSqlLib();
		else return preparedSql;
	}

	public void setPreparedSql(PreparedSql preparedSql) {
		this.preparedSql = preparedSql;
	}

	public CallableSql getCallableSql() {
		if(callableSql==null) return new CallableSqlLib();
		else return callableSql;
	}

	public void setCallableSql(CallableSql callableSql) {
		this.callableSql = callableSql;
	}

	private DbFeature getDbDialect() {
		if (DatabaseConst.MYSQL.equalsIgnoreCase((HoneyContext.getDbDialect()))
		 || DatabaseConst.MariaDB.equalsIgnoreCase((HoneyContext.getDbDialect()))
		   )return new MySqlFeature();
		else if (DatabaseConst.ORACLE.equalsIgnoreCase((HoneyContext.getDbDialect())))
			return new OracleFeature();
		else if (DatabaseConst.SQLSERVER.equalsIgnoreCase((HoneyContext.getDbDialect())))
			return new SqlServerFeature();
		else {
			throw new NoConfigException("Error: Do not set the database name. ");
		}
	}

	public DbFeature getDbFeature() {
		if(dbFeature!=null) return dbFeature;
		else return getDbDialect();
	}

	public void setDbFeature(DbFeature dbFeature) {
		this.dbFeature = dbFeature;
	}
	
	public NameTranslate getNameTranslate() {
		if(nameTranslate==null) return new UnderScoreAndCamelName();
		else return nameTranslate;
	}

	public void setNameTranslate(NameTranslate nameTranslate) {
		this.nameTranslate = nameTranslate;
	}

	public Cache getCache() {
		
		if(cache==null){
			return new DefaultCache();
		}
		else return cache;
	}

//	public void setCache(Cache cache) {
//		this.cache = cache;
//	}
	
}
