package hrds.agent.trans.biz.systemfile;

import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.core.utils.StringUtil;
import fd.ng.core.utils.SystemUtil;
import hrds.commons.base.AgentBaseAction;
import hrds.commons.exception.AppSystemException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DocClass(desc = "获取系统文件信息", author = "dhw", createdate = "2021/4/13 9:37")
public class SystemFileInfo extends AgentBaseAction {

	@Method(desc = "获取服务器指定文件夹下的目录及文件"
		, logicStep = "1.如果需要显示文件夹的路径为空，则默认取根目录下的文件和文件夹" +
		"2.获取操作系统的名称" +
		"3.根据操作系统获取系统目录" +
		"4.需要显示文件且是文件则放到list")
	@Param(name = "pathVal", desc = "页面选择的文件夹路径，为空则表示根目录", nullable = true, range = "可为空")
	@Param(name = "isFile", desc = "是否显示当前目录下的文件，默认false", valueIfNull = "false", range = "可为空")
	@Return(desc = "当前文件夹下所有的目录(当isFile为true时返回当前文件夹下所有的目录和文件)", range = "可能为空")
	public List<Map<String, String>> getSystemFileInfo(String pathVal, String isFile) {
		File[] file_array;
		//1.如果需要显示文件夹的路径为空，则默认取根目录下的文件和文件夹
		if (StringUtil.isBlank(pathVal)) {
			file_array = File.listRoots();
		} else {
			file_array = new File(pathVal).listFiles();
		}
		List<Map<String, String>> list = new ArrayList<>();
		// 2.获取操作系统的名称
		String osName = SystemUtil.OS_NAME;
		if (file_array != null && file_array.length > 0) {
			for (File file : file_array) {
				//是文件夹直接放到list
				if (file.isDirectory()) {
					// 3.根据操作系统获取系统目录
					if (osName.toLowerCase().contains("windows")) {
						getDirectoryMap(list, osName, file);
					} else if (osName.toLowerCase().contains("linux")) {
						getDirectoryMap(list, osName, file);
					} else {
						throw new AppSystemException("不支持的操作系统类型");
					}
				}
			}
			//4.需要显示文件且是文件则放到list
			if ("true".equals(isFile)) {
				for (File file : file_array) {
					if (!file.isDirectory() && !file.getName().startsWith(".") && file.canRead()) {
						Map<String, String> map = new HashMap<>();
						map.put("name", file.getName());
						map.put("path", file.getPath());
						map.put("isFolder", "false");
						map.put("osName", osName);
						list.add(map);
					}
				}
			}
		}
		return list;
	}

	/**
	 * 获取系统目录
	 *
	 * @param list   存放系统目录集合
	 * @param osName 操作系统名称
	 * @param file   文件
	 */
	private void getDirectoryMap(List<Map<String, String>> list, String osName, File file) {
		Map<String, String> map = new HashMap<>();
		map.put("isFolder", "true");
		map.put("name", file.getName());
		map.put("path", file.getPath());
		map.put("osName", osName);
		map.put("canExecute", String.valueOf(file.canExecute()));
		map.put("canWrite", String.valueOf(file.canWrite()));
		map.put("canRead", String.valueOf(file.canRead()));
		list.add(map);
	}
}
