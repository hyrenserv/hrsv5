package hrds.commons.codes;

import hrds.commons.exception.AppSystemException;
/**Created by automatic  */
/**代码类型名：StreamingPro输入输出的类型  */
public enum SdmSpInputOutputType {
	/**文本文件<WENBENWENJIAN>  */
	WENBENWENJIAN("1","文本文件","133","StreamingPro输入输出的类型"),
	/**数据库表<SHUJUKUBIAO>  */
	SHUJUKUBIAO("2","数据库表","133","StreamingPro输入输出的类型"),
	/**消费主题<XIAOFEIZHUTI>  */
	XIAOFEIZHUTI("3","消费主题","133","StreamingPro输入输出的类型"),
	/**内部表<NEIBUBIAO>  */
	NEIBUBIAO("4","内部表","133","StreamingPro输入输出的类型");

	private final String code;
	private final String value;
	private final String catCode;
	private final String catValue;

	SdmSpInputOutputType(String code,String value,String catCode,String catValue){
		this.code = code;
		this.value = value;
		this.catCode = catCode;
		this.catValue = catValue;
	}
	public String getCode(){return code;}
	public String getValue(){return value;}
	public String getCatCode(){return catCode;}
	public String getCatValue(){return catValue;}
	public static final String CodeName = "SdmSpInputOutputType";

	/**根据指定的代码值转换成中文名字
	* @param code   本代码的代码值
	* @return
	*/
	public static String ofValueByCode(String code) {
		for (SdmSpInputOutputType typeCode : SdmSpInputOutputType.values()) {
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
	public static SdmSpInputOutputType ofEnumByCode(String code) {
		for (SdmSpInputOutputType typeCode : SdmSpInputOutputType.values()) {
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
		return SdmSpInputOutputType.values()[0].getCatValue();
	}

	/**
	* 获取代码项的分类代码
	* @return
	*/
	public static String ofCatCode(){
		return SdmSpInputOutputType.values()[0].getCatCode();
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
