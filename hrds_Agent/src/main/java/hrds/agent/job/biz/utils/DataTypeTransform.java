package hrds.agent.job.biz.utils;

import fd.ng.core.conf.ConfFileLoader;
import fd.ng.core.utils.StringUtil;
import fd.ng.core.yaml.YamlArray;
import fd.ng.core.yaml.YamlFactory;
import fd.ng.core.yaml.YamlMap;
import hrds.commons.exception.AppSystemException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataTypeTransform {

	private static final Map<String, YamlMap> map = new HashMap<>();
	/* 这里是为了让某些转换的类型直接返回，不要根据原来的长度拼接新的长度，比如说NVARCHAR(16)类型，从数据库取出来的
	 * 类型长度只有16，但是转为普通数据库的varchar(16)长度不够用，所以要直接返回varchar(32),所以这里有一个list,
	 * list包含的值直接返回
	 */
	private static final String LKH = "(";
	private static final String RKH = ")";

	static {
		YamlMap rootConfig = YamlFactory.load(ConfFileLoader.getConfFile("contrast")).asMap();
		YamlArray arrays = rootConfig.getArray("typecontrast");
		for (int i = 0; i < arrays.size(); i++) {
			YamlMap trans = arrays.getMap(i);
			map.put(trans.getString("NAME"), trans);
		}
	}

	/**
	 * 转换为对应的存储数据库支持类型
	 */
	public static String tansform(String type, String dsl_name) {
		type = type.trim().toUpperCase();
		//获取要替换的值
		YamlMap yamlMap = map.get(dsl_name);
		if (yamlMap == null) {
			throw new AppSystemException("存储层" + dsl_name + "的配置信息在Agent的contrast.conf文件中没有，" +
				"请重新部署agent或者手动更新配置文件再重启Agent");
		}
		String val;
		if(type.contains(",")){
			val = yamlMap.getString("\""+type+"\"","");
		}else{
			val = yamlMap.getString(type,"");
		}
		if(StringUtil.isNotBlank(val)){
			return val;
		}
		//要转换的值带括号，则取出括号里面的值并拼接没有值得类型
		if (type.contains(LKH) && type.contains(RKH)) {
			String key_1 = type.substring(0, type.indexOf(LKH));
			String key_2 = key_1 + LKH + RKH;
			//默认类型是规范的即一对括号
			String length = type.substring(type.indexOf(LKH) + 1, type.length() - 1);
			val = yamlMap.getString(key_2, key_2);
			//如果要替换的值有括号则在括号中拼接长度信息
			if (val.contains(LKH) && val.contains(RKH)) {
				return val.substring(0, val.indexOf(LKH) + 1) + length + RKH;
			} else {
				return val;
			}
		} else {
			return map.get(dsl_name).getString(type, type);
		}
	}

	/**
	 * 源数据库的类型转换成对应的存储目的地支持的类型
	 */
	public static List<String> tansform(List<String> types, String dsl_name) {
		List<String> transformedTypeList = new ArrayList<>();
		for (String string : types) {
			transformedTypeList.add(tansform(string, dsl_name));
		}
		return transformedTypeList;
	}

}
