package hrds.b.biz.agentinfo;

import fd.ng.core.utils.DateUtil;
import fd.ng.core.utils.StringUtil;
import fd.ng.netclient.http.HttpClient;
import fd.ng.web.annotation.RequestBean;
import fd.ng.web.util.Dbo;
import hrds.commons.base.BaseAction;
import hrds.commons.codes.AgentStatus;
import hrds.commons.codes.AgentType;
import hrds.commons.entity.Agent_info;
import hrds.commons.exception.BusinessException;
import hrds.commons.utils.key.PrimayKeyGener;

import java.util.List;
import java.util.Map;

/**
 * agent增删改类
 *
 * @author dhw
 * @date 2019-09-04 17:30:27
 */
public class AgentInfoAction extends BaseAction {
	/**
	 * 保存agent信息
	 * <p>
	 * 1.数据可访问权限处理方式，新增时会设置创建用户ID，会获取当前用户ID，所以不需要权限验证
	 * 2.字段合法性验证
	 * 3.判断端口是否被占用，被占用抛异常，否则正常保存
	 * 4.初始化AgentInfo的一些非页面传值
	 * 5.检查数据源是否还存在以及判断数据源下相同的IP地址中是否包含相同的端口
	 * 6.保存agent信息
	 *
	 * @param agentInfo agent_info表对象
	 *                  含义：agent_info表实体对象
	 *                  取值范围：与数据库agent_info表字段定义规则一致
	 */
	public void saveAgent(@RequestBean Agent_info agentInfo) {
		// 1.数据可访问权限处理方式，新增时会设置创建用户ID，会获取当前用户ID，所以不需要权限验证
		// 2.字段合法性验证
		fieldLegalityValidation(agentInfo.getAgent_name(), agentInfo.getAgent_type(),
				agentInfo.getAgent_ip(), agentInfo.getAgent_port(), agentInfo.getSource_id()
				, agentInfo.getUser_id());
		// 3.判断端口是否被占用
		boolean flag = isPortOccupied(agentInfo.getAgent_ip(),
				Integer.parseInt(agentInfo.getAgent_port()));
		if (flag) {
			// 端口别占用，不可使用
			throw new BusinessException("端口被占用，agent_port=" + agentInfo.getAgent_port() + "," +
					"agent_ip =" + agentInfo.getAgent_ip());
		}
		// 4.初始化AgentInfo的一些非页面传值
		agentInfo.setSource_id(PrimayKeyGener.getNextId());
		agentInfo.setAgent_status(AgentStatus.WeiLianJie.getCode());
		agentInfo.setCreate_time(DateUtil.getSysTime());
		agentInfo.setCreate_date(DateUtil.getSysDate());
		// 5.检查数据源是否还存在以及判断数据源下相同的IP地址中是否包含相同的端口
		check(agentInfo.getSource_id(), agentInfo.getAgent_type(), agentInfo.getAgent_ip(),
				agentInfo.getAgent_port());
		// 6.保存agent信息
		if (agentInfo.add(Dbo.db()) != 1) {
			throw new BusinessException("新增agent_info表信息失败," + "agent_port=" +
					agentInfo.getAgent_port() + ",agent_ip =" + agentInfo.getAgent_ip() +
					",agent_name=" + agentInfo.getAgent_name());
		}
	}

	/**
	 * 检查数据源是否还存在以及数据源下相同的IP地址中是否包含相同的端口
	 * <p>
	 * 1.验证数据源是否还存在
	 * 2.判断数据源下相同的IP地址中是否包含相同的端口
	 *
	 * @param source_id  long
	 *                   含义：data_source表主键ID
	 *                   取值范围：十位数字，新增时自动生成
	 * @param agent_type String
	 *                   含义：agent类型
	 *                   取值范围：1:数据库Agent,2:文件系统Agent,3:FtpAgent,4:数据文件Agent,5:对象Agent
	 * @param agent_ip   String
	 *                   含义：agent所在服务器ip
	 *                   取值范围：合法IP地址
	 * @param agent_port String
	 *                   含义：agent连接端口
	 *                   取值范围：1024-65535
	 */
	private void check(long source_id, String agent_type, String agent_ip, String agent_port) {
		// 1.验证数据源是否还存在
		long count = Dbo.queryNumber("select * from data_source where source_id = ? and "
				+ " create_user_id=?", source_id, getUserId()).orElse(-1);
		if (count == -1) {
			throw new BusinessException("查询到的数据是非法数据，不是预期结果");
		}
		if (count == 0) {
			throw new BusinessException("该agent对应的数据源已不存在不可新增，source_id=" + source_id);
		}
		// 2.判断数据源下相同的IP地址中是否包含相同的端口
		long aiCount = Dbo.queryNumber("SELECT count(1) count FROM agent_info WHERE " +
						"source_id = ? AND agent_type = ? AND agent_ip = ? AND" +
						" agent_port = ?  and user_id", source_id, agent_type, agent_ip,
				agent_port).orElse(-1);
		if (aiCount == -1) {
			throw new BusinessException("查询到的数据是非法数据，不是预期结果");
		}
		if (aiCount == 0) {
			throw new BusinessException("该agent对应的数据源下相同的IP地址中是否包含相同的端口，" +
					"source_id=" + source_id);
		}
	}

	/**
	 * 字段合法性验证
	 * <p>
	 * 1.数据可访问权限处理方式，这是个私有方法，不会单独被调用，所以不需要权限验证
	 * 2.2.验证agent_type是否为空或空格以及是否为规定类型
	 * 3. 验证agent_name是否为空或空格
	 * 4. 验证agent_ip是否为空或空格
	 * 5.判断agent_ip是否是一个为空或空格的ip
	 * 6.验证agent_port是否为空或空格
	 * 7.判断agent_port是否是一个有效的端口
	 * 8.验证user_id是否为空或空格
	 * 9.验证source_id是否为空或空格,以及长度是否为10
	 *
	 * @param agent_name String
	 *                   含义：agent名称
	 *                   取值范围：不为空以及空格
	 * @param agent_type String
	 *                   含义：agent类型
	 *                   取值范围：1:数据库Agent,2:文件系统Agent,3:FtpAgent,4:数据文件Agent,5:对象Agent
	 * @param agent_ip   String
	 *                   含义：agent所在服务器ip
	 *                   取值范围：合法IP地址
	 * @param agent_port String
	 *                   含义：agent连接端口
	 *                   取值范围：1024-65535
	 * @param source_id  long
	 *                   含义：agent_info表外键ID，data_source表主键ID
	 *                   取值范围：十位数字，新增数据源时自动生成
	 * @param user_id    long
	 *                   含义：数据采集用户ID
	 *                   取值范围：四位数字，新增用户时自动生成
	 */
	private void fieldLegalityValidation(String agent_name, String agent_type, String agent_ip
			, String agent_port, long source_id, long user_id) {
		// 1.数据可访问权限处理方式，这是个私有方法，不会单独被调用，所以不需要权限验证
		// 2.验证agent_type是否为空或空格以及是否为规定类型
		AgentType agentType = AgentType.ofEnumByCode(agent_type);
		if (StringUtil.isBlank(agent_type) || agentType != AgentType.ShuJuKu
				|| agentType != AgentType.DBWenJian || agentType != AgentType.FTP
				|| agentType != AgentType.DuiXiang || agentType != AgentType.WenJianXiTong) {
			throw new BusinessException("agent_type不能为空且不能为空格或agent_type不是规定类型，" +
					"agent_type=" + agent_type);
		}
		// 3.验证agent_name是否为空或空格
		if (StringUtil.isBlank(agent_name)) {
			throw new BusinessException("agent_name不为空且不为空格，agent_name=" + agent_name);
		}
		// 4.验证agent_ip是否为空或空格
		if (StringUtil.isBlank(agent_ip)) {
			throw new BusinessException("agent_ip不为空且不为空格，agent_ip=" + agent_ip);
		}
		// 5.判断agent_ip是否是一个为空或空格的ip
		String[] split = agent_ip.split("\\.");
		for (int i = 0; i < split.length; i++) {
			int temp = Integer.parseInt(split[i]);
			if (temp < 0 || temp > 255) {
				throw new BusinessException("agent_ip不是一个为空或空格的ip地址," +
						"agent_ip=" + agent_ip);
			}
		}
		// 6.验证agent_port是否为空或空格
		if (StringUtil.isBlank(agent_port)) {
			throw new BusinessException("agent_port不为空且不为空格，agent_port=" + agent_port);
		}
		// 端口范围最小值
		int min = 1024;
		// 端口范围最大值
		int max = 65535;
		// 7.判断agent_port是否是一个有效的端口
		if (Integer.parseInt(agent_port) < min && Integer.parseInt(agent_port) > max) {
			throw new BusinessException("agent_port端口不是有效的端口，不在取值范围内，" +
					"agent_port=" + agent_port);
		}
		// 8.验证user_id是否为空或空格
		if (StringUtil.isBlank(String.valueOf(user_id))) {
			throw new BusinessException("user_id不为空且不为空格，user_id=" + user_id);
		}
		// 9.验证source_id是否为空或空格,以及长度是否为10
		int len = 10;
		if (StringUtil.isBlank(String.valueOf(source_id)) ||
				String.valueOf(source_id).length() > len) {
			throw new BusinessException("source_id不为空且不为空格，长度也不能超过10，source_id="
					+ source_id);
		}
	}

	/**
	 * 更新agent信息
	 * <p>
	 * 1.数据可访问权限处理方式，通过关联agent_id与user_id检查
	 * 2.验证agent_id是否合法
	 * 3.字段合法性验证
	 * 4.创建agent_info实体对象，同时封装值
	 * 5.创建agent_info实体对象，同时封装值
	 * 6.更新agent信息
	 *
	 * @param agent_id   long
	 *                   含义：agent_info主键ID
	 *                   取值范围：十位数字，新增时自动生成
	 * @param agent_name String
	 *                   含义：agent名称
	 *                   取值范围：不为空以及空格
	 * @param agent_type String
	 *                   含义：agent类型
	 *                   取值范围：1:数据库Agent,2:文件系统Agent,3:FtpAgent,4:数据文件Agent,5:对象Agent
	 * @param agent_ip   String
	 *                   含义：agent所在服务器ip
	 *                   取值范围：合法IP地址
	 * @param agent_port String
	 *                   含义：agent连接端口
	 *                   取值范围：1024-65535
	 * @param source_id  long
	 *                   含义：agent_info表外键ID，data_source表主键ID
	 *                   取值范围：十位数字，新增数据源时自动生成
	 * @param user_id    long
	 *                   含义：数据采集用户ID
	 *                   取值范围：四位数字，新增用户时自动生成
	 */
	public void updateAgent(long agent_id, String agent_name, String agent_type, String agent_ip
			, String agent_port, long source_id, long user_id) {
		// 1.数据可访问权限处理方式，通过关联agent_id与user_id检查
		if (Dbo.queryNumber("select count(*) from " + Agent_info.TableName +
				"  where agent_id=? and  create_user_id=?", agent_id, getUserId())
				.orElse(0) > 0) {
			throw new BusinessException("数据权限校验失败，数据不可访问！");
		}
		//agent_id长度
		int len = 10;
		// 2.验证agent_id是否合法
		if (String.valueOf(agent_id).length() != len) {
			throw new BusinessException("agent_id应为一个10位数字，新增时自动生成，agent_id="
					+ agent_id);
		}
		// 3.字段合法性验证
		fieldLegalityValidation(agent_name, agent_type, agent_ip, agent_port, source_id, user_id);
		// 4.创建agent_info实体对象，同时封装值
		Agent_info agent_info = new Agent_info();
		agent_info.setAgent_id(agent_id);
		agent_info.setUser_id(user_id);
		agent_info.setSource_id(source_id);
		agent_info.setAgent_ip(agent_ip);
		agent_info.setAgent_port(agent_port);
		agent_info.setAgent_type(agent_type);
		agent_info.setAgent_name(agent_name);
		// 5.检查数据源是否还存在以及判断数据源下相同的IP地址中是否包含相同的端口
		check(source_id, agent_type, agent_ip, agent_port);
		// 6.更新agent信息
		if (agent_info.update(Dbo.db()) != 1) {
			throw new BusinessException("更新agent_info表信息失败," + "agent_port="
					+ agent_info.getAgent_port() + ",agent_ip =" + agent_info.getAgent_ip() +
					",agent_name=" + agent_info.getAgent_name());
		}
	}

	/**
	 * 监控agent端口是否被占用（后期移动到hrds-commons下）
	 * <p>
	 * 1.通过http方式去测试端口连通情况，测通则被占用，不通则可以使用
	 *
	 * @param agent_ip   String
	 *                   含义： agent地址
	 *                   取值范围：不为空，服务器地址
	 * @param agent_port int
	 *                   含义：agent端口
	 *                   取值范围：1024-65535
	 * @return 返回端口是否被占用信号
	 */
	private boolean isPortOccupied(String agent_ip, int agent_port) {

		// 1.通过http方式去测试端口连通情况，测通则被占用，不通则可以使用
		HttpClient httpClient = new HttpClient();
		String url = "http://".concat(agent_ip).concat(":").concat(agent_port + "");
		HttpClient.ResponseValue post = httpClient.post(url);

		if (post.getCode() != 200) {
			// 未连通，端口可用
			return false;
		} else {
			// 连通，端口被使用中
			return true;
		}
	}

	/**
	 * 查询Agent信息
	 * <p>
	 * 1.根据agent_id与agent_type查询该agent信息
	 *
	 * @param agent_id   long
	 *                   含义：agent_info表主键
	 *                   取值范围：不为空且不为空格，长度不超过10
	 * @param agent_type String
	 *                   含义：agent类型
	 *                   取值范围：1:数据库Agent,2:文件系统Agent,3:FtpAgent,4:数据文件Agent,5:对象Agent
	 * @return 返回根据agent_id与agent_type查询该agent信息结果
	 */
	public List<Map<String, Object>> searchAgent(long agent_id, String agent_type) {
		// 1.根据agent_id与agent_type查询该agent信息
		return Dbo.queryList(" SELECT * FROM agent_info WHERE agent_id = ? AND agent_type = ?",
				agent_id, agent_type);
	}

	/**
	 * 删除agent
	 * <p>
	 * 1.删除前查询此agent是否已部署，已部署不能删除
	 * 2.判断此数据源与agent下是否有任务，有任务不能删除
	 * 3.删除agent
	 *
	 * @param agent_id   long
	 *                   含义：agent_info表主键
	 *                   取值范围：不为空且不为空格，长度不超过10
	 * @param agent_type String
	 *                   含义：agent类型
	 *                   取值范围：1:数据库Agent,2:文件系统Agent,3:FtpAgent,4:数据文件Agent,5:对象Agent
	 */
	public void deleteAgent(Long agent_id, String agent_type) {

		//FIXME 这个结果是 -1 ，不用处理了吗？

		// 1.删除前查询此agent是否已部署
		if (Dbo.queryNumber("select * from agent_down_info where agent_id=?",
				agent_id).orElse(-1) > 0) {
			// 此agent已部署不能删除
			throw new BusinessException("此agent已部署不能删除");
		}
		// 2.判断此数据源与agent下是否有任务
		if (Dbo.queryNumber(" SELECT task_name FROM agent_info t1 join database_set t2 on " +
						"t1.agent_id=t2.agent_id WHERE  t1.agent_id=? and  t1.agent_type=?",
				agent_id, agent_type).orElse(-1) > 0) {
			// 此数据源与agent下有任务，不能删除
			throw new BusinessException("此数据源与agent下有任务，不能删除");
		}

		// 3.删除agent
		int num = Dbo.execute("delete  from agent_info where agent_id=?", agent_id);
		if (num != 1) {
			// 3.判断库里是否没有这条数据
			if (num == 0) {
				throw new BusinessException("删除agent_info表信息失败，数据库里没有此条数据，" +
						"agent_id=" + agent_id + ",agent_type=" + agent_type);
			}
			throw new BusinessException("删除agent_info表信息失败，agent_id=" + agent_id
					+ ",agent_type=" + agent_type);
		}
	}

}

