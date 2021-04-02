package hrds.commons.codes;

import hrds.commons.exception.AppSystemException;
/**Created by automatic  */
/**代码类型名：StreamingPro流式数据的版本  */
public enum SdmSpStreamVer {
	/**kafka8<KAFKA8>  */
	KAFKA8("1","kafka8","140","StreamingPro流式数据的版本"),
	/**kafka9<KAFKA9>  */
	KAFKA9("2","kafka9","140","StreamingPro流式数据的版本"),
	/**kafka<KAFKA>  */
	KAFKA("3","kafka","140","StreamingPro流式数据的版本");

	private final String code;
	private final String value;
	private final String catCode;
	private final String catValue;

	SdmSpStreamVer(String code,String value,String catCode,String catValue){
		this.code = code;
		this.value = value;
		this.catCode = catCode;
		this.catValue = catValue;
	}
	public String getCode(){return code;}
	public String getValue(){return value;}
	public String getCatCode(){return catCode;}
	public String getCatValue(){return catValue;}
	public static final String CodeName = "SdmSpStreamVer";

	/**根据指定的代码值转换成中文名字
	* @param code   本代码的代码值
	* @return
	*/
	public static String ofValueByCode(String code) {
		for (SdmSpStreamVer typeCode : SdmSpStreamVer.values()) {
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
	public static SdmSpStreamVer ofEnumByCode(String code) {
		for (SdmSpStreamVer typeCode : SdmSpStreamVer.values()) {
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
		return SdmSpStreamVer.values()[0].getCatValue();
	}

	/**
	* 获取代码项的分类代码
	* @return
	*/
	public static String ofCatCode(){
		return SdmSpStreamVer.values()[0].getCatCode();
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
