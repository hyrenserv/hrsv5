package hrds.commons.codes;

import hrds.commons.exception.AppSystemException;
/**Created by automatic  */
/**代码类型名：流数据管理消费端目的地  */
public enum SdmConsumeDestination {
	/**数据库<ShuJuKu>  */
	ShuJuKu("1","数据库","129","流数据管理消费端目的地"),
	/**hbase<Hbase>  */
	Hbase("2","hbase","129","流数据管理消费端目的地"),
	/**rest服务<RestFuWu>  */
	RestFuWu("3","rest服务","129","流数据管理消费端目的地"),
	/**文件<LiuWenJian>  */
	LiuWenJian("4","文件","129","流数据管理消费端目的地"),
	/**二进制文件<ErJinZhiWenJian>  */
	ErJinZhiWenJian("5","二进制文件","129","流数据管理消费端目的地"),
	/**Kafka<Kafka>  */
	Kafka("6","Kafka","129","流数据管理消费端目的地"),
	/**自定义业务类<ZiDingYeWuLei>  */
	ZiDingYeWuLei("7","自定义业务类","129","流数据管理消费端目的地");

	private final String code;
	private final String value;
	private final String catCode;
	private final String catValue;

	SdmConsumeDestination(String code,String value,String catCode,String catValue){
		this.code = code;
		this.value = value;
		this.catCode = catCode;
		this.catValue = catValue;
	}
	public String getCode(){return code;}
	public String getValue(){return value;}
	public String getCatCode(){return catCode;}
	public String getCatValue(){return catValue;}
	public static final String CodeName = "SdmConsumeDestination";

	/**根据指定的代码值转换成中文名字
	* @param code   本代码的代码值
	* @return
	*/
	public static String ofValueByCode(String code) {
		for (SdmConsumeDestination typeCode : SdmConsumeDestination.values()) {
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
	public static SdmConsumeDestination ofEnumByCode(String code) {
		for (SdmConsumeDestination typeCode : SdmConsumeDestination.values()) {
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
		return SdmConsumeDestination.values()[0].getCatValue();
	}

	/**
	* 获取代码项的分类代码
	* @return
	*/
	public static String ofCatCode(){
		return SdmConsumeDestination.values()[0].getCatCode();
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
