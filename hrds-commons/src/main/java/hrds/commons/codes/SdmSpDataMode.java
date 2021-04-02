package hrds.commons.codes;

import hrds.commons.exception.AppSystemException;
/**Created by automatic  */
/**代码类型名：StreamingPro输入的数据模式  */
public enum SdmSpDataMode {
	/**批量表<PILIANGBIAO>  */
	PILIANGBIAO("1","批量表","134","StreamingPro输入的数据模式"),
	/**流表<LIUBIAO>  */
	LIUBIAO("2","流表","134","StreamingPro输入的数据模式");

	private final String code;
	private final String value;
	private final String catCode;
	private final String catValue;

	SdmSpDataMode(String code,String value,String catCode,String catValue){
		this.code = code;
		this.value = value;
		this.catCode = catCode;
		this.catValue = catValue;
	}
	public String getCode(){return code;}
	public String getValue(){return value;}
	public String getCatCode(){return catCode;}
	public String getCatValue(){return catValue;}
	public static final String CodeName = "SdmSpDataMode";

	/**根据指定的代码值转换成中文名字
	* @param code   本代码的代码值
	* @return
	*/
	public static String ofValueByCode(String code) {
		for (SdmSpDataMode typeCode : SdmSpDataMode.values()) {
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
	public static SdmSpDataMode ofEnumByCode(String code) {
		for (SdmSpDataMode typeCode : SdmSpDataMode.values()) {
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
		return SdmSpDataMode.values()[0].getCatValue();
	}

	/**
	* 获取代码项的分类代码
	* @return
	*/
	public static String ofCatCode(){
		return SdmSpDataMode.values()[0].getCatCode();
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
