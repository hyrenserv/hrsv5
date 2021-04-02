package hrds.commons.codes;

import hrds.commons.exception.AppSystemException;
/**Created by automatic  */
/**代码类型名：采集编码  */
public enum DataBaseCode {
	/**UTF-8<UTF_8>  */
	UTF_8("1","UTF-8","42","采集编码"),
	/**GBK<GBK>  */
	GBK("2","GBK","42","采集编码"),
	/**UTF-16<UTF_16>  */
	UTF_16("3","UTF-16","42","采集编码"),
	/**GB2312<GB2312>  */
	GB2312("4","GB2312","42","采集编码"),
	/**ISO-8859-1<ISO_8859_1>  */
	ISO_8859_1("5","ISO-8859-1","42","采集编码");

	private final String code;
	private final String value;
	private final String catCode;
	private final String catValue;

	DataBaseCode(String code,String value,String catCode,String catValue){
		this.code = code;
		this.value = value;
		this.catCode = catCode;
		this.catValue = catValue;
	}
	public String getCode(){return code;}
	public String getValue(){return value;}
	public String getCatCode(){return catCode;}
	public String getCatValue(){return catValue;}
	public static final String CodeName = "DataBaseCode";

	/**根据指定的代码值转换成中文名字
	* @param code   本代码的代码值
	* @return
	*/
	public static String ofValueByCode(String code) {
		for (DataBaseCode typeCode : DataBaseCode.values()) {
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
	public static DataBaseCode ofEnumByCode(String code) {
		for (DataBaseCode typeCode : DataBaseCode.values()) {
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
		return DataBaseCode.values()[0].getCatValue();
	}

	/**
	* 获取代码项的分类代码
	* @return
	*/
	public static String ofCatCode(){
		return DataBaseCode.values()[0].getCatCode();
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
