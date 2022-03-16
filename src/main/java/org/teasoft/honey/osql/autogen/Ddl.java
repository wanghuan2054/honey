/*
 * Copyright 2016-2021 the original author.All rights reserved.
 * Kingstar(honeysoft@126.com)
 * The license,see the LICENSE file.
 */

package org.teasoft.honey.osql.autogen;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.teasoft.bee.osql.DatabaseConst;
import org.teasoft.bee.osql.PreparedSql;
import org.teasoft.bee.osql.annotation.PrimaryKey;
import org.teasoft.honey.osql.core.BeeFactoryHelper;
import org.teasoft.honey.osql.core.HoneyConfig;
import org.teasoft.honey.osql.core.HoneyContext;
import org.teasoft.honey.osql.core.HoneyUtil;
import org.teasoft.honey.osql.core.Logger;
import org.teasoft.honey.osql.core.NameTranslateHandle;
import org.teasoft.honey.osql.name.NameUtil;
import org.teasoft.honey.util.EntityUtil;
import org.teasoft.honey.util.SqlKeyCheck;

/**
 * 根据Javabean创建表.Create table according to Javabean
 * @author Kingstar
 * @since  1.9
 */
public class Ddl {

	private static final String CREATE_TABLE = "CREATE TABLE ";
	//	private static Map<String, String> java2DbType = Java2DbType.getJava2DbType(HoneyContext.getDbDialect());
	private static String LINE_SEPARATOR = System.getProperty("line.separator"); // 换行符
	private static PreparedSql preparedSql = BeeFactoryHelper.getPreparedSql();
	private static Map<String,String> pkStatement=new HashMap<>();
	private static String java_lang_String="java.lang.String";
	
	static {
		initPkStatement();
	}

	private Ddl() {}
	
	private static Map<String, String> getJava2DbType() {
		return Java2DbType.getJava2DbType(HoneyContext.getDbDialect());
	}

	public static <T> boolean createTable(T entity, boolean isDropExistTable) {
		if (isDropExistTable) {
			String tableName = _toTableName(entity);
			boolean second = false;
			try {
				String sql0 = "";

				if (HoneyUtil.isOracle() || HoneyUtil.isSqlServer()) {
					sql0 = "DROP TABLE " + tableName;
				} else {
					sql0 = " DROP TABLE IF EXISTS " + tableName;
					second = true;
				}
				preparedSql.modify(sql0);
			} catch (Exception e) {
				if (second) {
					try {
						preparedSql.modify("DROP TABLE " + tableName);
					} catch (Exception e2) {
						Logger.warn(e2.getMessage());
					}
				}
			}
			return createTable(entity, tableName);

		} else {
			return createTable(entity);
		}

	}

	/**
	 * 根据Javabean生成数据库表,Javabean无需配置过多的字段信息.此方法只考虑通用情况,若有详细需求,不建议采用
	 * <br>According to the database table generated by JavaBean, JavaBean does not need to configure 
	 * <br>too much field information. This method only considers the general situation, and is not 
	 * <br>recommended if there are detailed requirements.
	 * @param entity Javabean entity.
	 * @return
	 */
	public static <T> boolean createTable(T entity) {
		return createTable(entity, null);
	}

	private static <T> boolean createTable(T entity, String tableName) {
		boolean result = false;
		try {
			//V1.11 创建语句的可执行语句与占位的是一样的,无需要重复输出.
			boolean old=HoneyConfig.getHoneyConfig().showSql_showExecutableSql;
			if(old) HoneyConfig.getHoneyConfig().showSql_showExecutableSql=false;
			preparedSql.modify(toCreateTableSQL(entity, tableName));
			if(old) HoneyConfig.getHoneyConfig().showSql_showExecutableSql=old;
			result = true;
		} catch (Exception e) {
			Logger.error(e.getMessage(),e);
			result = false;
		}

		return result;
	}

	/**
	 * 根据Javabean生成数据库表建表语句,Javabean无需配置过多的字段信息.此方法只考虑通用情况,若有详细需求,不建议采用
	 * <br>According to the statement of creating database table generated by JavaBean, JavaBean does 
	 * <br> not need to configure too much field information. This method only considers the general 
	 * <br>situation, and is not recommended if there are detailed requirements
	 * @param entity Javabean entity.
	 * @return 建表语句.create table string.
	 */
	public static <T> String toCreateTableSQL(T entity) {
		return toCreateTableSQL(entity, null);
	}

	private static <T> String toCreateTableSQL(T entity, String tableName) {

		if (HoneyUtil.isSQLite()) {
			return toCreateTableSQLForSQLite(entity, tableName);
		} else if (HoneyUtil.isMysql()) {
			return toCreateTableSQLForMySQL(entity, tableName);
		} else if (DatabaseConst.H2.equalsIgnoreCase(HoneyContext.getDbDialect())) {
			return toCreateTableSQLForH2(entity, tableName);
		} else if (DatabaseConst.PostgreSQL.equalsIgnoreCase(HoneyContext.getDbDialect())) {
			return toCreateTableSQLForPostgreSQL(entity, tableName);
		} else if (HoneyUtil.isSqlServer()) {
			return toCreateTableSQLForSQLSERVER(entity, tableName);
		} else{
			//ORACLE,Cassandra ...
			return _toCreateTableSQL(entity, tableName);
		}
	}

	//ORACLE, Cassandra
	private static <T> String _toCreateTableSQL(T entity, String tableName) {

		if (tableName == null) tableName = _toTableName(entity);
		StringBuilder sqlBuffer = new StringBuilder();
		sqlBuffer.append(CREATE_TABLE + tableName + " (").append(LINE_SEPARATOR);
		Field fields[] = entity.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			if (isSkipField(fields[i])) {
				if (i == fields.length - 1) sqlBuffer.delete(sqlBuffer.length() - 5, sqlBuffer.length() - 2);
				continue;
			}
			sqlBuffer.append(_toColumnName(fields[i].getName(),entity.getClass())).append("  ");
			
			String type =getType(fields[i]);
			
			if(HoneyUtil.isCassandra()) {
				if(EntityUtil.isList(fields[i]) || EntityUtil.isSet(fields[i])) {
					Class<?> clazz=EntityUtil.getGenericType(fields[i]);
					String type0=getType(clazz);
					type=type+"<"+type0+">";
				}else if(EntityUtil.isMap(fields[i])) {
					Class<?>[] classes=EntityUtil.getGenericTypeArray(fields[i]);
					String type1=getType(classes[0]);
					String type2=getType(classes[1]);
					type=type+"<"+type1+","+type2+">";
				}
				
			}
			
			sqlBuffer.append(type);
			
			if (isPrimaryKey(fields[i])) sqlBuffer.append(" PRIMARY KEY");
			if (i != fields.length - 1)
				sqlBuffer.append(",  ");
			else
				sqlBuffer.append("  ");
			sqlBuffer.append(LINE_SEPARATOR);
		}
		sqlBuffer.append(" )");

		return sqlBuffer.toString();

	}
	
	private static String getType(Field field) {
		String type = getJava2DbType().get(field.getType().getName());
		if(type==null) {
			Logger.warn("The java type:"+type+" can not the relative database column type!");
			type=getJava2DbType().get(java_lang_String);
			Logger.warn("It will be replace with type: "+type);
		}
		
		return type;
	}
	
	private static String getType(Class<?> c) {
		String name="";
		if(c==null) {
			Logger.warn("The Class is null,it will be replace with "+java_lang_String);
			name=java_lang_String;
		}else {
			name=c.getName();
		}
		String type = getJava2DbType().get(name);
		if (type == null) {
			if (EntityUtil.isCustomBean(name)) {
//				type=c.getSimpleName();
				type=NameUtil.firstLetterToLowerCase(c.getSimpleName());
			} else {
				Logger.warn("The java type:" + name + " can not the relative database column type!");
				type = getJava2DbType().get(java_lang_String);
				Logger.warn("It will be replace with type: " + type);
			}
		}
		
		return type;
	}
	
	//SQLite
	private static <T> String toCreateTableSQLForSQLite(T entity, String tableName) {
		return toCreateTableSQLComm(entity, tableName, DatabaseConst.SQLite);
	}
	
/*	//SQLite
	private static <T> String toCreateTableSQLForSQLite(T entity, String tableName) {
		if (tableName == null) tableName = _toTableName(entity);
		StringBuilder sqlBuffer = new StringBuilder();
		sqlBuffer.append(CREATE_TABLE + tableName + " (").append(LINE_SEPARATOR);
		Field fields[] = entity.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			if (isSkipField(fields[i])) {
				if (i == fields.length - 1) sqlBuffer.delete(sqlBuffer.length() - 5, sqlBuffer.length() - 2);
				continue;
			}
			sqlBuffer.append(_toColumnName(fields[i].getName(),entity.getClass())).append("  ");

			if (isPrimaryKey(fields[i]))
				sqlBuffer.append(" INTEGER PRIMARY KEY NOT NULL");
			else {
				sqlBuffer.append(getJava2DbType().get(fields[i].getType().getName()));

				String type = getJava2DbType().get(fields[i].getType().getName());
				if ("timestamp".equalsIgnoreCase(type) || "datetime".equalsIgnoreCase(type)) {
					sqlBuffer.append(" DEFAULT CURRENT_TIMESTAMP");
				} else {
					sqlBuffer.append(" DEFAULT NULL");
				}
			}
			if (i != fields.length - 1)
				sqlBuffer.append(",  ");
			else
				sqlBuffer.append("  ");
			sqlBuffer.append(LINE_SEPARATOR);
		}
		sqlBuffer.append(" )");

		return sqlBuffer.toString();

	}
	*/

	//MySQL
	private static <T> String toCreateTableSQLForMySQL(T entity, String tableName) {
		if (tableName == null) tableName = _toTableName(entity);
		StringBuilder sqlBuffer = new StringBuilder();
		sqlBuffer.append(CREATE_TABLE + tableName + " (").append(LINE_SEPARATOR);
		Field fields[] = entity.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			if (isSkipField(fields[i])) {
				if (i == fields.length - 1) sqlBuffer.delete(sqlBuffer.length() - 5, sqlBuffer.length() - 2);
				continue;
			}
			sqlBuffer.append(_toColumnName(fields[i].getName(),entity.getClass())).append("  ");

			if (isPrimaryKey(fields[i]))
				sqlBuffer.append("bigint(20) PRIMARY KEY NOT NULL AUTO_INCREMENT");
			else {
				String type = getJava2DbType().get(fields[i].getType().getName());
				if(type==null) {
					Logger.warn("The java type:"+type+" can not the relative database column type!");
					type=getJava2DbType().get(java_lang_String);
					Logger.warn("It will be replace with type: "+type);
				}
				sqlBuffer.append(type);
				
				if ("timestamp".equalsIgnoreCase(type) || "datetime".equalsIgnoreCase(type)) {
					sqlBuffer.append(" DEFAULT CURRENT_TIMESTAMP");
				} else {
					sqlBuffer.append(" DEFAULT NULL");
				}
			}

			if (i != fields.length - 1)
				sqlBuffer.append(",  ");
			else
				sqlBuffer.append("  ");
			sqlBuffer.append(LINE_SEPARATOR);
		}
		sqlBuffer.append(" )");

		return sqlBuffer.toString();

	}
	
//	H2
	private static <T> String toCreateTableSQLForH2(T entity, String tableName) {
		return toCreateTableSQLComm(entity, tableName, DatabaseConst.H2);
	}
	

/*	//H2
	private static <T> String toCreateTableSQLForH2(T entity, String tableName) {
		if (tableName == null) tableName = _toTableName(entity);
		StringBuilder sqlBuffer = new StringBuilder();
		sqlBuffer.append(CREATE_TABLE + tableName + " (").append(LINE_SEPARATOR);
		Field fields[] = entity.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			if (isSkipField(fields[i])) {
				if (i == fields.length - 1) sqlBuffer.delete(sqlBuffer.length() - 5, sqlBuffer.length() - 2);
				continue;
			}
			sqlBuffer.append(_toColumnName(fields[i].getName(),entity.getClass())).append("  ");

			if (isPrimaryKey(fields[i]))
				sqlBuffer.append("bigint PRIMARY KEY NOT NULL");
			else {
				sqlBuffer.append(getJava2DbType().get(fields[i].getType().getName()));

				String type = getJava2DbType().get(fields[i].getType().getName());
				if ("timestamp".equalsIgnoreCase(type) || "datetime".equalsIgnoreCase(type)) {
					sqlBuffer.append(" DEFAULT CURRENT_TIMESTAMP");
				} else {
					sqlBuffer.append(" DEFAULT NULL");
				}
			}

			if (i != fields.length - 1)
				sqlBuffer.append(",  ");
			else
				sqlBuffer.append("  ");
			sqlBuffer.append(LINE_SEPARATOR);
		}
		sqlBuffer.append(" )");

		return sqlBuffer.toString();
	}
*/
	
	private static void initPkStatement() {
		pkStatement.put(DatabaseConst.H2.toLowerCase(), "bigint PRIMARY KEY NOT NULL");
		pkStatement.put(DatabaseConst.SQLite.toLowerCase(), " INTEGER PRIMARY KEY NOT NULL");
		pkStatement.put(DatabaseConst.PostgreSQL.toLowerCase(), "bigserial NOT NULL");
		pkStatement.put("", "");
		pkStatement.put(null, "");
	}
	
	private static String getPrimaryKeyStatement(String databaseName){
		return pkStatement.get(databaseName.toLowerCase());
	}
	
	//Comm: H2,SQLite,PostgreSQL
	private static <T> String toCreateTableSQLComm(T entity, String tableName, String databaseName) {
		if (tableName == null) tableName = _toTableName(entity);
		StringBuilder sqlBuffer = new StringBuilder();
		sqlBuffer.append(CREATE_TABLE + tableName + " (").append(LINE_SEPARATOR);
		Field fields[] = entity.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			if (isSkipField(fields[i])) {
				if (i == fields.length - 1)
					sqlBuffer.delete(sqlBuffer.length() - 5, sqlBuffer.length() - 2);
				continue;
			}
			sqlBuffer.append(_toColumnName(fields[i].getName(), entity.getClass())).append("  ");

			if (isPrimaryKey(fields[i]))
				sqlBuffer.append(getPrimaryKeyStatement(databaseName));//different
			else {
				
				String type = getJava2DbType().get(fields[i].getType().getName());
				if(type==null) {
					Logger.warn("The java type:"+type+" can not the relative database column type!");
					type=getJava2DbType().get(java_lang_String);
					Logger.warn("It will be replace with type: "+type);
				}
				sqlBuffer.append(type);
				if ("timestamp".equalsIgnoreCase(type) || "datetime".equalsIgnoreCase(type)) {
					sqlBuffer.append(" DEFAULT CURRENT_TIMESTAMP");
				} else {
					sqlBuffer.append(" DEFAULT NULL");
				}
			}

			if (i != fields.length - 1)
				sqlBuffer.append(",  ");
			else
				sqlBuffer.append("  ");
			sqlBuffer.append(LINE_SEPARATOR);
		}
		sqlBuffer.append(" )");

		return sqlBuffer.toString();
	}
	
	
//	PostgreSQL
	private static <T> String toCreateTableSQLForPostgreSQL(T entity, String tableName) {
		return toCreateTableSQLComm(entity, tableName, DatabaseConst.PostgreSQL);
	}
	
/*	//PostgreSQL
	private static <T> String toCreateTableSQLForPostgreSQL(T entity, String tableName) {
		if (tableName == null) tableName = _toTableName(entity);
		StringBuilder sqlBuffer = new StringBuilder();
		sqlBuffer.append(CREATE_TABLE + tableName + " (").append(LINE_SEPARATOR);
		Field fields[] = entity.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			if (isSkipField(fields[i])) {
				if (i == fields.length - 1) sqlBuffer.delete(sqlBuffer.length() - 5, sqlBuffer.length() - 2);
				continue;
			}
			sqlBuffer.append(_toColumnName(fields[i].getName(),entity.getClass())).append("  ");

			if (isPrimaryKey(fields[i]))
				sqlBuffer.append("bigserial NOT NULL");
			else {
				sqlBuffer.append(getJava2DbType().get(fields[i].getType().getName()));

				String type = getJava2DbType().get(fields[i].getType().getName());
				if ("timestamp".equalsIgnoreCase(type) || "datetime".equalsIgnoreCase(type)) {
					sqlBuffer.append(" DEFAULT CURRENT_TIMESTAMP");
				} else {
					sqlBuffer.append(" DEFAULT NULL");
				}
			}

			if (i != fields.length - 1)
				sqlBuffer.append(",  ");
			else
				sqlBuffer.append("  ");
			sqlBuffer.append(LINE_SEPARATOR);
		}
		sqlBuffer.append(" )");

		return sqlBuffer.toString();

	}
	*/

	//SQLSERVER
	private static <T> String toCreateTableSQLForSQLSERVER(T entity, String tableName) {
		if (tableName == null) tableName = _toTableName(entity);
		StringBuilder sqlBuffer = new StringBuilder();
		sqlBuffer.append(CREATE_TABLE + tableName + " (").append(LINE_SEPARATOR);
		Field fields[] = entity.getClass().getDeclaredFields();
		boolean hasCurrentTime = false;
		for (int i = 0; i < fields.length; i++) {
			if (isSkipField(fields[i])) {
				if (i == fields.length - 1) sqlBuffer.delete(sqlBuffer.length() - 5, sqlBuffer.length() - 2);
				continue;
			}
			sqlBuffer.append(_toColumnName(fields[i].getName(),entity.getClass())).append("  ");

			if (isPrimaryKey(fields[i]))
				sqlBuffer.append("bigint PRIMARY KEY NOT NULL");
			else {
				String type = getJava2DbType().get(fields[i].getType().getName());
				if(type==null) {
					Logger.warn("The java type:"+type+" can not the relative database column type!");
					type=getJava2DbType().get(java_lang_String);
					Logger.warn("It will be replace with type: "+type);
				}
//				sqlBuffer.append(type);
				
				if (("timestamp".equalsIgnoreCase(type))) {
					if (!hasCurrentTime) {
						sqlBuffer.append(type);
						sqlBuffer.append(" ");
						hasCurrentTime = true;
					} else {
						sqlBuffer.append("datetime DEFAULT NULL");
					}
				} else {
					sqlBuffer.append(type);
					sqlBuffer.append(" DEFAULT NULL");
				}
			}

			if (i != fields.length - 1)
				sqlBuffer.append(",  ");
			else
				sqlBuffer.append("  ");
			sqlBuffer.append(LINE_SEPARATOR);
		}
		sqlBuffer.append(" )");

		return sqlBuffer.toString();

	}

	public static void setDynamicParameter(String para, String value) {
		BeeFactoryHelper.getSuid().setDynamicParameter(para, value);
	}

	private static String _toTableName(Object entity) {
		return NameTranslateHandle.toTableName(NameUtil.getClassFullName(entity));
	}

	@SuppressWarnings("rawtypes")
	private static String _toColumnName(String fieldName,Class entityClass) {
		String name= NameTranslateHandle.toColumnName(fieldName,entityClass);
		if(SqlKeyCheck.isKeyWord(name)) {
			Logger.warn("The '"+name+"' is Sql Keyword. Do not recommend!");
		}
		return name;
	}

	private static boolean isSkipField(Field field) {
//		if (field != null) {
//			if ("serialVersionUID".equals(field.getName())) return true;
//			if (field.isSynthetic()) return true;
//			if (field.isAnnotationPresent(JoinTable.class)) return true;
//		}
//		return false;
		return HoneyUtil.isSkipField(field);
	}
	
	private static boolean isPrimaryKey(Field field) {
		if ("id".equalsIgnoreCase(field.getName())) return true;
		if (field.isAnnotationPresent(PrimaryKey.class)) return true;//V1.11
		return false;
	}

}
