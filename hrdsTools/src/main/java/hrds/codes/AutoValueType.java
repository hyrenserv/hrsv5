package hrds.codes;
/**Created by automatic  */
/**代码类型名：值类型  */
public enum AutoValueType {
	/**字符串<ZiFuChuan>  */
	ZiFuChuan("01","字符串","162","值类型"),
	/**数值<ShuZhi>  */
	ShuZhi("02","数值","162","值类型"),
	/**日期<RiQi>  */
	RiQi("03","日期","162","值类型"),
	/**枚举<MeiJu>  */
	MeiJu("04","枚举","162","值类型");

	private final String code;
	private final String value;
	private final String catCode;
	private final String catValue;

	AutoValueType(String code,String value,String catCode,String catValue){
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
		for (AutoValueType typeCode : AutoValueType.values()) {
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
	public static AutoValueType getCodeObj(String code) {
		for (AutoValueType typeCode : AutoValueType.values()) {
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
		return AutoValueType.values()[0].getCatValue();
	}

	/**
	* 获取代码项的分类代码
	* @return
	*/
	public static String getObjCatCode(){
		return AutoValueType.values()[0].getCatCode();
	}
}
