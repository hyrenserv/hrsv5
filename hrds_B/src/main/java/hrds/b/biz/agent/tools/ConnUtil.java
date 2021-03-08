
package hrds.b.biz.agent.tools;

import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.core.utils.StringUtil;
import hrds.b.biz.agent.bean.DBConnectionProp;
import hrds.commons.codes.DatabaseType;
import hrds.commons.exception.AppSystemException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@DocClass(desc = "数据库直连采集获取数据库连接相关信息的工具类", author = "WangZhengcheng")
public class ConnUtil {

	private static final Logger logger = LogManager.getLogger(ConnUtil.class);


	@Method(desc = "根据数据库类型获取数据库连接信息填写模板", logicStep = "" +
			"1、构建返回使用Map集合" +
			"2、判断数据库类型，根据数据库类型构建数据库连接信息填写模板并放入Map" +
			"3、返回Map集合")
	@Param(name = "dbType", desc = "数据库类型", range = "不为空，DatabaseType代码项code值")
	@Return(desc = "保存着数据库连接URL相关信息的实体类对象", range = "DBConnectionProp实体类对象")
	public static DBConnectionProp getConnURLProp(String dbType, String port) {
		//1、构建返回使用Map集合
		DBConnectionProp template = new DBConnectionProp();
		//2、判断数据库类型，根据数据库类型构建数据库连接信息填写模板并放入Map
		DatabaseType databaseType = DatabaseType.ofEnumByCode(dbType);
		if (DatabaseType.MYSQL == databaseType) {
			template.setUrlPrefix("jdbc:mysql://");
			template.setIpPlaceholder(":");
			template.setPortPlaceholder("/");
			template.setUrlSuffix("?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull");
		} else if (DatabaseType.Oracle9i == databaseType || DatabaseType.Oracle10g == databaseType) {
			template.setUrlPrefix("jdbc:oracle:thin:@");
			template.setIpPlaceholder(":");
			template.setPortPlaceholder(":");
			template.setUrlSuffix("");
		} else if (DatabaseType.DB2 == databaseType) {
			template.setUrlPrefix("jdbc:db2://");
			template.setIpPlaceholder(":");
			template.setPortPlaceholder("/");
			template.setUrlSuffix("");
		} else if (DatabaseType.SqlServer2000 == databaseType || DatabaseType.SqlServer2005 == databaseType) {
			template.setUrlPrefix("jdbc:sqlserver://");
			template.setIpPlaceholder(":");
			template.setPortPlaceholder(";DatabaseName=");
			template.setUrlSuffix("");
		} else if (DatabaseType.Postgresql == databaseType) {
			template.setUrlPrefix("jdbc:postgresql://");
			template.setIpPlaceholder(":");
			template.setPortPlaceholder("/");
			template.setUrlSuffix("");
		} else if (DatabaseType.SybaseASE125 == databaseType) {
			template.setUrlPrefix("jdbc:sybase:Tds:");
			template.setIpPlaceholder(":");
			template.setPortPlaceholder("/");
			template.setUrlSuffix("");
		} else if (DatabaseType.ApacheDerby == databaseType) {
			template.setUrlPrefix("jdbc:derby://");
			template.setIpPlaceholder(":");
			template.setPortPlaceholder("/");
			template.setUrlSuffix(";create=true");
		} else if (DatabaseType.GBase == databaseType) {
			template.setUrlPrefix("jdbc:gbase://");
			template.setIpPlaceholder(":");
			template.setPortPlaceholder("/");
			template.setUrlSuffix("");
		} else if (DatabaseType.TeraData == databaseType) {
			if (StringUtil.isBlank(port)) {
				template.setUrlPrefix("jdbc:teradata://");
				template.setIpPlaceholder("/TMODE=TERA,CHARSET=ASCII,CLIENT_CHARSET=cp936,DATABASE=");
				template.setPortPlaceholder("");
				template.setUrlSuffix(",lob_support=off");
			} else {
				template.setUrlPrefix("jdbc:teradata://");
				template.setIpPlaceholder("/TMODE=TERA,CHARSET=ASCII,CLIENT_CHARSET=cp936,DATABASE=");
				template.setPortPlaceholder("");
				template.setUrlSuffix(",lob_support=off,DBS_PORT=");
			}
		} else if (DatabaseType.Informatic == databaseType) {
			template.setUrlPrefix("jdbc:informix-sqli://");
			template.setIpPlaceholder(":");
			template.setPortPlaceholder("/");
			template.setUrlSuffix(":INFORMIXSERVER=myserver");
		} else if (DatabaseType.H2 == databaseType) {
			template.setUrlPrefix("jdbc:h2:tcp://");
			template.setIpPlaceholder(":");
			template.setPortPlaceholder("/");
			template.setUrlSuffix("");
		} else if (DatabaseType.Hive == databaseType) {
			template.setUrlPrefix("jdbc:hive2://");
			template.setIpPlaceholder(":");
			template.setPortPlaceholder("/");
			template.setUrlSuffix("");
		}else if(DatabaseType.Odps == databaseType){
			template.setUrlPrefix("jdbc:odps:");
			template.setIpPlaceholder("");
			template.setPortPlaceholder("");
			template.setUrlSuffix("");
		}else if( DatabaseType.KingBase == databaseType) {
			template.setUrlPrefix("jdbc:kingbase8://");
			template.setIpPlaceholder(":");
			template.setPortPlaceholder("/");
			template.setUrlSuffix("");
		}
		else {
			logger.error("目前不支持对该数据库类型进行采集，请联系管理员");
			throw new AppSystemException("目前不支持对该数据库类型进行采集，请联系管理员");
		}
		//3、返回Map集合
		return template;
	}

	@Method(desc = "根据数据库类型获取数据库连接驱动", logicStep = "" +
			"1、判断数据库类型，根据数据库类型返回数据库连接驱动")
	@Param(name = "dbType", desc = "数据库类型", range = "不为空，DatabaseType代码项code值")
	@Return(desc = "数据库连接驱动", range = "不为空")
	public static String getJDBCDriver(String dbType) {
		DatabaseType databaseType = DatabaseType.ofEnumByCode(dbType);
		if (DatabaseType.MYSQL == databaseType) {
			return "com.mysql.jdbc.Driver";
		} else if (DatabaseType.Oracle9i == databaseType) {
			return "oracle.jdbc.driver.OracleDriver";
		} else if (DatabaseType.Oracle10g == databaseType) {
			return "oracle.jdbc.OracleDriver";
		} else if (DatabaseType.DB2 == databaseType) {
			return "com.ibm.db2.jcc.DB2Driver";
		} else if (DatabaseType.SqlServer2000 == databaseType || DatabaseType.SqlServer2005 == databaseType) {
			return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		} else if (DatabaseType.Postgresql == databaseType) {
			return "org.postgresql.Driver";
		} else if (DatabaseType.SybaseASE125 == databaseType) {
			return "com.sybase.jdbc2.jdbc.SybDriver";
		} else if (DatabaseType.ApacheDerby == databaseType) {
			return "org.apache.derby.jdbc.EmbeddedDriver";
		} else if (DatabaseType.GBase == databaseType) {
			return "com.gbase.jdbc.Driver";
		} else if (DatabaseType.TeraData == databaseType) {
			return "com.ncr.teradata.TeraDriver";
		} else if (DatabaseType.Informatic == databaseType) {
			return "com.informix.jdbc.IfxDriver";
		} else if (DatabaseType.H2 == databaseType) {
			return "org.h2.Driver";
		} else if (DatabaseType.Hive == databaseType) {
			return "org.apache.hive.jdbc.HiveDriver";
		} else if (DatabaseType.Odps == databaseType) {
			return "com.aliyun.odps.jdbc.OdpsDriver";
		} else if(DatabaseType.KingBase == databaseType){
			return "com.kingbase8.Driver";
		}else {
			logger.error("目前不支持对该数据库类型进行采集，请联系管理员");
			throw new AppSystemException("目前不支持对该数据库类型进行采集，请联系管理员");
		}
	}

}
