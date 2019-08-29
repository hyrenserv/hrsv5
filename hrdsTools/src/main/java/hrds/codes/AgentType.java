package hrds.codes;
/**Created by automatic  */
/**代码类型名：Agent类别  */
public enum AgentType {
	/**数据库Agent<ShuJuKu>  */
	ShuJuKu("1","数据库Agent","5","Agent类别"),
	/**文件系统Agent<WenJianXiTong>  */
	WenJianXiTong("2","文件系统Agent","5","Agent类别"),
	/**FtpAgent<FTP>  */
	FTP("3","FtpAgent","5","Agent类别"),
	/**数据文件Agent<DBWenJian>  */
	DBWenJian("4","数据文件Agent","5","Agent类别"),
	/**对象Agent<DuiXiang>  */
	DuiXiang("5","对象Agent","5","Agent类别");

	private final String code;
	private final String value;
	private final String catCode;
	private final String catValue;

	AgentType(String code,String value,String catCode,String catValue){
		this.code = code;
		this.value = value;
		this.catCode = catCode;
		this.catValue = catValue;
	}
	public String getCode(){return code;}
	public String getValue(){return value;}
	public String getCatCode(){return catCode;}
	public String getCatValue(){return catValue;}

	/**根据指定的代码值转换成中文名字
	* @param code   本代码的代码值
	* @return
	*/
	public static String getValue(String code) {
		for (AgentType typeCode : AgentType.values()) {
			if (typeCode.getCode().equals(code)) {
				return typeCode.value;
			}
		}
		return null;
	}

	/**根据指定的代码值转换成对象
	* @param code   本代码的代码值
	* @return
	*/
	public static AgentType getCodeObj(String code) {
		for (AgentType typeCode : AgentType.values()) {
			if (typeCode.getCode().equals(code)) {
				return typeCode;
			}
		}
		return null;
	}

	/**
	* 获取代码项的中文类名名称
	* @return
	*/
	public static String getObjCatValue(){
		return AgentType.values()[0].getCatValue();
	}

	/**
	* 获取代码项的分类代码
	* @return
	*/
	public static String getObjCatCode(){
		return AgentType.values()[0].getCatCode();
	}
}
