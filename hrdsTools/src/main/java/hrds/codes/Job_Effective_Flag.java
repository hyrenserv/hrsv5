package hrds.codes;
/**Created by automatic  */
/**代码类型名：ETl作业有效标志  */
public enum Job_Effective_Flag {
	/**有效(Y)<YES>  */
	YES("Y","有效(Y)","110","ETl作业有效标志"),
	/**无效(N)<NO>  */
	NO("N","无效(N)","110","ETl作业有效标志"),
	/**空跑(V)<VIRTUAL>  */
	VIRTUAL("V","空跑(V)","110","ETl作业有效标志");

	private final String code;
	private final String value;
	private final String catCode;
	private final String catValue;

	Job_Effective_Flag(String code,String value,String catCode,String catValue){
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
		for (Job_Effective_Flag typeCode : Job_Effective_Flag.values()) {
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
	public static Job_Effective_Flag getCodeObj(String code) {
		for (Job_Effective_Flag typeCode : Job_Effective_Flag.values()) {
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
		return Job_Effective_Flag.values()[0].getCatValue();
	}

	/**
	* 获取代码项的分类代码
	* @return
	*/
	public static String getObjCatCode(){
		return Job_Effective_Flag.values()[0].getCatCode();
	}
}
