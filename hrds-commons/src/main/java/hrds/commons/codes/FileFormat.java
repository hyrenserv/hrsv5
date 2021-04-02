package hrds.commons.codes;

import hrds.commons.exception.AppSystemException;
/**Created by automatic  */
/**代码类型名：DB文件格式  */
public enum FileFormat {
	/**定长<DingChang>  */
	DingChang("0","定长","44","DB文件格式"),
	/**非定长<FeiDingChang>  */
	FeiDingChang("1","非定长","44","DB文件格式"),
	/**CSV<CSV>  */
	CSV("2","CSV","44","DB文件格式"),
	/**SEQUENCEFILE<SEQUENCEFILE>  */
	SEQUENCEFILE("3","SEQUENCEFILE","44","DB文件格式"),
	/**PARQUET<PARQUET>  */
	PARQUET("4","PARQUET","44","DB文件格式"),
	/**ORC<ORC>  */
	ORC("5","ORC","44","DB文件格式");

	private final String code;
	private final String value;
	private final String catCode;
	private final String catValue;

	FileFormat(String code,String value,String catCode,String catValue){
		this.code = code;
		this.value = value;
		this.catCode = catCode;
		this.catValue = catValue;
	}
	public String getCode(){return code;}
	public String getValue(){return value;}
	public String getCatCode(){return catCode;}
	public String getCatValue(){return catValue;}
	public static final String CodeName = "FileFormat";

	/**根据指定的代码值转换成中文名字
	* @param code   本代码的代码值
	* @return
	*/
	public static String ofValueByCode(String code) {
		for (FileFormat typeCode : FileFormat.values()) {
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
	public static FileFormat ofEnumByCode(String code) {
		for (FileFormat typeCode : FileFormat.values()) {
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
		return FileFormat.values()[0].getCatValue();
	}

	/**
	* 获取代码项的分类代码
	* @return
	*/
	public static String ofCatCode(){
		return FileFormat.values()[0].getCatCode();
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
