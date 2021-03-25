package hrds.commons.codes;

import hrds.commons.exception.AppSystemException;
/**Created by automatic  */
/**代码类型名：流数据管理变量类型  */
public enum SdmVariableType {
	/**整数<ZhengShu>  */
	ZhengShu("1","整数","128","流数据管理变量类型"),
	/**字符串<ZiFuChuan>  */
	ZiFuChuan("2","字符串","128","流数据管理变量类型"),
	/**浮点数<FuDianShu>  */
	FuDianShu("3","浮点数","128","流数据管理变量类型"),
	/**字节数组<ZiJieShuZu>  */
	ZiJieShuZu("4","字节数组","128","流数据管理变量类型");

	private final String code;
	private final String value;
	private final String catCode;
	private final String catValue;

	SdmVariableType(String code,String value,String catCode,String catValue){
		this.code = code;
		this.value = value;
		this.catCode = catCode;
		this.catValue = catValue;
	}
	public String getCode(){return code;}
	public String getValue(){return value;}
	public String getCatCode(){return catCode;}
	public String getCatValue(){return catValue;}
	public static final String CodeName = "SdmVariableType";

	/**根据指定的代码值转换成中文名字
	* @param code   本代码的代码值
	* @return
	*/
	public static String ofValueByCode(String code) {
		for (SdmVariableType typeCode : SdmVariableType.values()) {
			if (typeCode.getCode().equals(code)) {
				return typeCode.value;
			}
		}
		throw new AppSystemException("根据"+code+"没有找到对应的代码项");
	}

	/**根据指定的代码值转换成对象
	* @param code   本代码的代码值
	* @return
	*/
	public static SdmVariableType ofEnumByCode(String code) {
		for (SdmVariableType typeCode : SdmVariableType.values()) {
			if (typeCode.getCode().equals(code)) {
				return typeCode;
			}
		}
		throw new AppSystemException("根据"+code+"没有找到对应的代码项");
	}

	/**
	* 获取代码项的中文类名名称
	* @return
	*/
	public static String ofCatValue(){
		return SdmVariableType.values()[0].getCatValue();
	}

	/**
	* 获取代码项的分类代码
	* @return
	*/
	public static String ofCatCode(){
		return SdmVariableType.values()[0].getCatCode();
	}

	/**
	* 禁止使用类的tostring()方法
	* @return
	*/
	@Override
	public String toString() {
		throw new AppSystemException("There's no need for you to !");
	}
}
