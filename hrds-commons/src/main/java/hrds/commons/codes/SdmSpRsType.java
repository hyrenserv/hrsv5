package hrds.commons.codes;

import hrds.commons.exception.AppSystemException;
/**Created by automatic  */
/**代码类型名：StreamingProREST返回处理方式  */
public enum SdmSpRsType {
	/**忽略<HuLue>  */
	HuLue("0","忽略","142","StreamingProREST返回处理方式"),
	/**数据库<ShuJuKu>  */
	ShuJuKu("1","数据库","142","StreamingProREST返回处理方式"),
	/**kafka<KafKa>  */
	KafKa("2","kafka","142","StreamingProREST返回处理方式");

	private final String code;
	private final String value;
	private final String catCode;
	private final String catValue;

	SdmSpRsType(String code,String value,String catCode,String catValue){
		this.code = code;
		this.value = value;
		this.catCode = catCode;
		this.catValue = catValue;
	}
	public String getCode(){return code;}
	public String getValue(){return value;}
	public String getCatCode(){return catCode;}
	public String getCatValue(){return catValue;}
	public static final String CodeName = "SdmSpRsType";

	/**根据指定的代码值转换成中文名字
	* @param code   本代码的代码值
	* @return
	*/
	public static String ofValueByCode(String code) {
		for (SdmSpRsType typeCode : SdmSpRsType.values()) {
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
	public static SdmSpRsType ofEnumByCode(String code) {
		for (SdmSpRsType typeCode : SdmSpRsType.values()) {
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
		return SdmSpRsType.values()[0].getCatValue();
	}

	/**
	* 获取代码项的分类代码
	* @return
	*/
	public static String ofCatCode(){
		return SdmSpRsType.values()[0].getCatCode();
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
