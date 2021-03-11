package hrds.c.biz.etlsys;

import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.utils.DateUtil;
import fd.ng.core.utils.JsonUtil;
import fd.ng.db.jdbc.DatabaseWrapper;
import fd.ng.db.jdbc.SqlOperator;
import fd.ng.netclient.http.HttpClient;
import fd.ng.web.action.ActionResult;
import hrds.commons.codes.IsFlag;
import hrds.commons.codes.Job_Status;
import hrds.commons.codes.Main_Server_Sync;
import hrds.commons.codes.Pro_Type;
import hrds.commons.entity.Etl_resource;
import hrds.commons.entity.Etl_sub_sys_list;
import hrds.commons.entity.Etl_sys;
import hrds.commons.exception.BusinessException;
import hrds.commons.utils.CommonVariables;
import hrds.commons.utils.Constant;
import hrds.commons.utils.ParallerTestUtil;
import hrds.testbase.WebBaseTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@DocClass(desc = "作业调度工程测试", author = "dhw", createdate = "2019/11/25 17:22")
public class EtlSysActionTest extends WebBaseTestCase {
	//请填写测试用户需要做登录验证的A项目的登录验证的接口
	private static final String LOGIN_URL = ParallerTestUtil.TESTINITCONFIG.getString("login_url");
	// 已经存在的用户ID,用于模拟登录
	private static final long USER_ID = ParallerTestUtil.TESTINITCONFIG.getLong("user_id");
	private static final String PASSWORD = ParallerTestUtil.TESTINITCONFIG.getString("password");
	private static final String ETL_SERV_IP = ParallerTestUtil.TESTINITCONFIG.getString("etl_serv_ip");
	private static final String USER_NAME = ParallerTestUtil.TESTINITCONFIG.getString("user_name");
	private static final String USER_PWD = ParallerTestUtil.TESTINITCONFIG.getString("user_pwd");
	private static final String serv_file_path = ParallerTestUtil.TESTINITCONFIG.getString("serv_file_path");
	//当前线程的id
	private String THREAD_ID = String.valueOf(Thread.currentThread().getId() * 1000000);
	// 初始化作业工程编号
	private final String ETL_SYS_CD = "dhwcs_" + THREAD_ID;
	// 初始化任务编号
	private final String SUB_SYS_CD = "dhwrwcs_" + THREAD_ID;
	// 初始化作业工程条数
	private final int ETLSYSNUM = 4;

	@Before
	public void before() {
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			// 构造etl_sys表测试数据
			Etl_sys etl_sys = new Etl_sys();
			for (int i = 0; i < ETLSYSNUM; i++) {
				etl_sys.setEtl_sys_cd(ETL_SYS_CD + i);
				etl_sys.setEtl_sys_name("dhwcs" + THREAD_ID + i);
				etl_sys.setComments("工程测试" + i);
				etl_sys.setCurr_bath_date(DateUtil.getSysDate());
				if (i == 2) {
					etl_sys.setSys_run_status(Job_Status.RUNNING.getCode());
				} else if (i == 3) {
					etl_sys.setEtl_serv_ip(ETL_SERV_IP);
					etl_sys.setEtl_serv_port(CommonVariables.SFTP_PORT);
					etl_sys.setSys_run_status(Job_Status.STOP.getCode());
					etl_sys.setUser_name(USER_NAME);
					etl_sys.setUser_pwd(USER_PWD);
					etl_sys.setServ_file_path(serv_file_path);
				} else {
					etl_sys.setSys_run_status(Job_Status.STOP.getCode());
				}
				etl_sys.setBath_shift_time(DateUtil.getSysDate());
				etl_sys.setComments("作业调度工程测试" + i);
				etl_sys.setMain_serv_sync(Main_Server_Sync.YES.getCode());
				etl_sys.setUser_id(USER_ID);
				assertThat("测试数据etl_sys初始化", etl_sys.add(db), is(1));
			}
			// 构造工程任务测试数据
			Etl_sub_sys_list etl_sub_sys_list = new Etl_sub_sys_list();
			etl_sub_sys_list.setEtl_sys_cd(ETL_SYS_CD + 1);
			etl_sub_sys_list.setSub_sys_cd(SUB_SYS_CD);
			etl_sub_sys_list.setSub_sys_desc("任务测试");
			assertThat("测试数据etl_sub_sys_list初始化", etl_sub_sys_list.add(db), is(1));
			// 初始化系统资源测试数据
			String[] paraType = {Pro_Type.Thrift.getCode(), Pro_Type.Yarn.getCode()};
			for (String para_type : paraType) {
				Etl_resource resource = new Etl_resource();
				resource.setEtl_sys_cd(ETL_SYS_CD + 3);
				resource.setResource_type(para_type);
				resource.setMain_serv_sync(Main_Server_Sync.YES.getCode());
				resource.setResource_max(10);
				resource.setResource_used(0);
				assertThat("测试数据Etl_resource初始化", resource.add(db), is(1));
			}
			// 12.提交事务
			SqlOperator.commitTransaction(db);
		}
		// 模拟用户登录
		String responseValue = new HttpClient()
				.buildSession()
				.addData("user_id", USER_ID)
				.addData("password", PASSWORD)
				.post(LOGIN_URL)
				.getBodyString();
		ActionResult ar = JsonUtil.toObjectSafety(responseValue, ActionResult.class)
				.orElseThrow(() -> new BusinessException("连接失败"));
		assertThat("用户登录成功", ar.isSuccess(), is(true));
	}

	@After
	public void after() {
		DatabaseWrapper db = new DatabaseWrapper();
		try {
			// 测试完成后删除etl_sys表测试数据
			for (int i = 0; i < 4; i++) {
				SqlOperator.execute(db, "delete from " + Etl_sys.TableName + " where etl_sys_cd=?",
						ETL_SYS_CD + i);
			}
			SqlOperator.execute(db, "delete from " + Etl_sys.TableName + " where etl_sys_cd=?",
					ETL_SYS_CD);
			// 删除任务数据
			SqlOperator.execute(db, "delete from " + Etl_sub_sys_list.TableName + " where sub_sys_cd=?",
					SUB_SYS_CD);
			// 删除资源定义数据
			SqlOperator.execute(db, "delete from " + Etl_resource.TableName + " where etl_sys_cd=?",
					ETL_SYS_CD);
			SqlOperator.execute(db, "delete from " + Etl_resource.TableName + " where resource_type in(?,?)",
					Pro_Type.Thrift.getCode(), Pro_Type.Yarn.getCode());
			// 测试完删除新增数据
			SqlOperator.execute(db, "delete from " + Etl_sys.TableName + " where etl_sys_cd=?",
					"addCs1");
			// 提交事务
			SqlOperator.commitTransaction(db);
		} catch (Exception e) {
			db.rollback();
		} finally {
			db.close();
		}
	}

	@Method(desc = "测试查询所有作业调度工程信息", logicStep = "1.正常的数据访问1，数据都正常，该方法只有一种情况")
	@Test
	public void searchEtlSys() {
		// 1.正常的数据访问1，数据都正常
		String bodyString = new HttpClient()
				.post(getActionUrl("searchEtlSys"))
				.getBodyString();
		ActionResult ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class)
				.orElseThrow(() -> new BusinessException("son对象转换成实体对象失败！！"));
		assertThat(ar.isSuccess(), is(true));
		int count = ar.getDataForResult().getRowCount();
		// 因为不确定原数据库是否已经有数据
		assertThat("数据量大于等于初始化数据", count >= ETLSYSNUM, is(true));
	}

	@Method(desc = "根据工程编号查询工程信息",
			logicStep = "1.正常的数据访问1，数据都正常" +
					"2.错误的数据访问1，etl_sys_cd不存在,该方法只有两种情况")
	@Test
	public void searchEtlSysById() {
		// 1.正常的数据访问1，数据都正常
		String bodyString = new HttpClient().addData("etl_sys_cd", ETL_SYS_CD + 0)
				.post(getActionUrl("searchEtlSysById"))
				.getBodyString();
		ActionResult ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class)
				.orElseThrow(() -> new BusinessException("son对象转换成实体对象失败！！"));
		assertThat(ar.isSuccess(), is(true));
		Map<Object, Object> map = ar.getDataForMap();
		assertThat(map.get("etl_sys_name"), is("dhwcs" + THREAD_ID + 0));
		assertThat(map.get("comments"), is("作业调度工程测试0"));
		// 2.错误的数据访问1，etl_sys_cd不存在
		bodyString = new HttpClient().addData("etl_sys_cd", "000")
				.post(getActionUrl("searchEtlSysById"))
				.getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class)
				.orElseThrow(() -> new BusinessException("连接失败！！"));
		assertThat(ar.isSuccess(), is(false));
	}

	@Method(desc = "测试新增作业调度工程",
			logicStep = "1.正常的数据访问1，数据都正常" +
					"2.错误的数据访问1，etl_sys_cd为空" +
					"3.错误的数据访问2，etl_sys_cd为空格" +
					"4.错误的数据访问3，etl_sys_cd为不合法（不为数字字母下划线）" +
					"5.错误的数据访问4，etl_sys_name为空" +
					"6.错误的数据访问5，etl_sys_name为空格")
	@Test
	public void addEtlSys() {
		// 1.正常的数据访问1，数据都正常
		String bodyString = new HttpClient()
				.addData("etl_sys_cd", "addCs1")
				.addData("etl_sys_name", "工程测试1")
				.addData("comments", "新增作业调度工程测试1")
				.post(getActionUrl("addEtlSys"))
				.getBodyString();
		ActionResult ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class)
				.orElseThrow(() -> new BusinessException("连接失败！！"));
		assertThat(ar.isSuccess(), is(true));
		// 验证数据正确性
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			Etl_sys etl_sys = SqlOperator.queryOneObject(db, Etl_sys.class, "select * from "
					+ Etl_sys.TableName + " where etl_sys_cd=?", "addCs1").orElseThrow(() ->
					new BusinessException("sql查询错误或者映射实体错误！"));
			assertThat(etl_sys.getSys_run_status(), is(Job_Status.STOP.getCode()));
			assertThat(etl_sys.getBath_shift_time(), is(DateUtil.getSysDate()));
			assertThat(etl_sys.getEtl_sys_name(), is("工程测试1"));
			assertThat(etl_sys.getComments(), is("新增作业调度工程测试1"));
			assertThat(etl_sys.getMain_serv_sync(), is(Main_Server_Sync.YES.getCode()));
			assertThat(etl_sys.getUser_id(), is(USER_ID));
		}
		// 2.错误的数据访问1，etl_sys_cd为空
		bodyString = new HttpClient()
				.addData("etl_sys_cd", "")
				.addData("etl_sys_name", "工程测试1")
				.addData("comments", "新增作业调度工程测试1")
				.post(getActionUrl("addEtlSys"))
				.getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class)
				.orElseThrow(() -> new BusinessException("连接失败！！"));
		assertThat(ar.isSuccess(), is(false));
		// 3.错误的数据访问2，etl_sys_cd为空格
		bodyString = new HttpClient()
				.addData("etl_sys_cd", " ")
				.addData("etl_sys_name", "工程测试1")
				.addData("comments", "新增作业调度工程测试1")
				.post(getActionUrl("addEtlSys"))
				.getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class)
				.orElseThrow(() -> new BusinessException("连接失败！！"));
		assertThat(ar.isSuccess(), is(false));
		// 4.错误的数据访问3，etl_sys_cd为不合法（不为数字字母下划线）
		bodyString = new HttpClient()
				.addData("etl_sys_cd", "etl-sys-cd")
				.addData("etl_sys_name", "工程测试1")
				.addData("comments", "新增作业调度工程测试1")
				.post(getActionUrl("addEtlSys"))
				.getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class)
				.orElseThrow(() -> new BusinessException("连接失败！！"));
		assertThat(ar.isSuccess(), is(false));
		// 5.错误的数据访问4，etl_sys_name为空
		bodyString = new HttpClient()
				.addData("etl_sys_cd", "addCs2")
				.addData("etl_sys_name", "")
				.addData("comments", "新增作业调度工程测试1")
				.post(getActionUrl("addEtlSys"))
				.getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class)
				.orElseThrow(() -> new BusinessException("连接失败！！"));
		assertThat(ar.isSuccess(), is(false));
		// 6.错误的数据访问5，etl_sys_name为空格
		bodyString = new HttpClient()
				.addData("etl_sys_cd", "addCs2")
				.addData("etl_sys_name", " ")
				.addData("comments", "新增作业调度工程测试1")
				.post(getActionUrl("addEtlSys"))
				.getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class)
				.orElseThrow(() -> new BusinessException("连接失败！！"));
		assertThat(ar.isSuccess(), is(false));
	}

	@Method(desc = "更新工程信息", logicStep = "1.正常的数据访问1，数据都正常" +
			"2.错误的数据访问1，etl_sys_cd为空" +
			"3.错误的数据访问2，etl_sys_cd为空格" +
			"4.错误的数据访问3，etl_sys_cd不存在" +
			"5.错误的数据访问4，etl_sys_name为空" +
			"6.错误的数据访问5，etl_sys_name为空格")
	@Test
	public void updateEtlSys() {
		// 1.正常的数据访问1，数据都正常
		String bodyString = new HttpClient()
				.addData("etl_sys_cd", ETL_SYS_CD + 0)
				.addData("etl_sys_name", "更新工程测试1")
				.addData("comments", "更新作业调度工程测试1")
				.post(getActionUrl("updateEtlSys"))
				.getBodyString();
		ActionResult ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class)
				.orElseThrow(() -> new BusinessException("连接失败！！"));
		assertThat(ar.isSuccess(), is(true));
		// 验证数据正确性
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			Etl_sys etl_sys = SqlOperator.queryOneObject(db, Etl_sys.class, "select * from "
					+ Etl_sys.TableName + " where etl_sys_cd=?", ETL_SYS_CD + 0).orElseThrow(() ->
					new BusinessException("sql查询错误或者映射实体错误！"));
			assertThat(etl_sys.getSys_run_status(), is(Job_Status.STOP.getCode()));
			assertThat(etl_sys.getBath_shift_time(), is(DateUtil.getSysDate()));
			assertThat(etl_sys.getEtl_sys_name(), is("更新工程测试1"));
			assertThat(etl_sys.getComments(), is("更新作业调度工程测试1"));
			assertThat(etl_sys.getMain_serv_sync(), is(Main_Server_Sync.YES.getCode()));
			assertThat(etl_sys.getUser_id(), is(USER_ID));
		}
		// 2.错误的数据访问1，etl_sys_cd为空
		bodyString = new HttpClient()
				.addData("etl_sys_cd", "")
				.addData("etl_sys_name", "更新测试2")
				.addData("comments", "更新作业调度工程测试2")
				.post(getActionUrl("updateEtlSys"))
				.getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class)
				.orElseThrow(() -> new BusinessException("连接失败！！"));
		assertThat(ar.isSuccess(), is(false));
		// 4.错误的数据访问3，etl_sys_cd不存在
		bodyString = new HttpClient()
				.addData("etl_sys_cd", "aaa")
				.addData("etl_sys_name", "更新工程测试4")
				.addData("comments", "更新作业调度工程测试4")
				.post(getActionUrl("updateEtlSys"))
				.getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class)
				.orElseThrow(() -> new BusinessException("连接失败！！"));
		assertThat(ar.isSuccess(), is(false));
		// 5.错误的数据访问4，etl_sys_name为空
		bodyString = new HttpClient()
				.addData("etl_sys_cd", "upCs4")
				.addData("etl_sys_name", "")
				.addData("comments", "更新作业调度工程测试1")
				.post(getActionUrl("updateEtlSys"))
				.getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class)
				.orElseThrow(() -> new BusinessException("连接失败！！"));
		assertThat(ar.isSuccess(), is(false));
		// 6.错误的数据访问5，etl_sys_name为空格
		bodyString = new HttpClient()
				.addData("etl_sys_cd", "upCs5")
				.addData("etl_sys_name", " ")
				.addData("comments", "更新作业调度工程测试5")
				.post(getActionUrl("updateEtlSys"))
				.getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class)
				.orElseThrow(() -> new BusinessException("连接失败！！"));
		assertThat(ar.isSuccess(), is(false));
	}

	//	@Method(desc = "读取trigger日志", logicStep = "1.正常的数据访问1，数据都正常")
//	@Test
//	public void downloadControlOrTriggerLog() {
//		// 1.正常的数据访问1，数据都正常
//		String bodyString = new HttpClient()
//				.addData("etl_sys_cd", ETL_SYS_CD + 0)
//				.addData("curr_bath_date", DateUtil.parseStr2DateWith8Char(DateUtil.getSysDate())
//						.toString())
//				.addData("isControl", IsFlag.Shi.getCode())
//				.post(getActionUrl("downloadControlOrTriggerLog"))
//				.getBodyString();
//		ActionResult ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class)
//				.orElseThrow(() -> new BusinessException("连接失败！！"));
//		assertThat(ar.isSuccess(), is(true));
//	}
//
	@Method(desc = "读取control日志", logicStep = "1.正常的数据访问1，数据都正常")
	@Test
	public void readControlOrTriggerLog() {
		// 1.正常的数据访问1，数据都正常
		String bodyString = new HttpClient()
				.addData("etl_sys_cd", ETL_SYS_CD + 3)
				.addData("readNum", 100)
				.addData("isControl", IsFlag.Shi.getCode())
				.post(getActionUrl("readControlOrTriggerLog"))
				.getBodyString();
		ActionResult ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class)
				.orElseThrow(() -> new BusinessException("连接失败！！"));
		assertThat(ar.isSuccess(), is(true));
	}

	@Method(desc = "停止工程", logicStep = "1.正常的数据访问1，数据都正常" +
			"2.正常的数据访问1，etl_sys_cd为空" +
			"3.正常的数据访问2，etl_sys_cd不存在")
	@Test
	public void stopEtlProject() {
		// 1.正常的数据访问1，数据都正常
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			// 停止工程前先检查工程是否运行状态
			Etl_sys etl_sys = SqlOperator.queryOneObject(db, Etl_sys.class,
					"select sys_run_status from " + Etl_sys.TableName + " where etl_sys_cd=?",
					ETL_SYS_CD + 2)
					.orElseThrow(() -> new BusinessException("sql查询错误"));
			assertThat("工程运行状态为运行", etl_sys.getSys_run_status(), is(Job_Status.RUNNING.getCode()));
			String bodyString = new HttpClient()
					.addData("etl_sys_cd", ETL_SYS_CD + 2)
					.post(getActionUrl("stopEtlProject"))
					.getBodyString();
			ActionResult ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class)
					.orElseThrow(() -> new BusinessException("连接失败！！"));
			assertThat(ar.isSuccess(), is(true));
			etl_sys = SqlOperator.queryOneObject(db, Etl_sys.class,
					"select sys_run_status from " + Etl_sys.TableName + " where etl_sys_cd=?",
					ETL_SYS_CD + 2)
					.orElseThrow(() -> new BusinessException("sql查询错误"));
			assertThat("工程运行状态为停止", etl_sys.getSys_run_status(), is(Job_Status.STOP.getCode()));
			// 2.正常的数据访问1，etl_sys_cd为空
			bodyString = new HttpClient()
					.addData("etl_sys_cd", "")
					.post(getActionUrl("stopEtlProject"))
					.getBodyString();
			ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class)
					.orElseThrow(() -> new BusinessException("连接失败！！"));
			assertThat(ar.isSuccess(), is(false));
			// 3.正常的数据访问2，etl_sys_cd不存在
			bodyString = new HttpClient()
					.addData("etl_sys_cd", "aaaaaaaaa")
					.post(getActionUrl("stopEtlProject"))
					.getBodyString();
			ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class)
					.orElseThrow(() -> new BusinessException("连接失败！！"));
			assertThat(ar.isSuccess(), is(false));
		}
	}

	@Method(desc = "停止工程", logicStep = "1.正常的数据访问1，数据都正常" +
			"2.正常的数据访问1，etl_sys_cd为空" +
			"3.正常的数据访问2，etl_sys_cd不存在")
	@Test
	public void deleteEtlProject() {
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			// 1.正常的数据访问1，数据都正常
			// 删除前查询数据库，确认预期删除的数据存在
			long num = SqlOperator.queryNumber(db,
					"select count(1) from " + Etl_sys.TableName + " where etl_sys_cd=?",
					ETL_SYS_CD + 3)
					.orElseThrow(() -> new BusinessException("sql查询错误"));
			assertThat("删除操作前，Etl_sys表中的确存在这样一条数据", num, is(1L));
			num = SqlOperator.queryNumber(db, "select count(1) from " +
					Etl_resource.TableName + " where etl_sys_cd=?", ETL_SYS_CD + 3)
					.orElseThrow(() -> new BusinessException("sql查询错误"));
			assertThat("删除操作前，Etl_resource表中的确存在这样一条数据", num, is(2L));
			String bodyString = new HttpClient().addData("etl_sys_cd", ETL_SYS_CD + 3)
					.post(getActionUrl("deleteEtlProject"))
					.getBodyString();
			ActionResult ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class)
					.orElseThrow(() -> new BusinessException("连接失败！！"));
			assertThat(ar.isSuccess(), is(true));
			// 删除后查询数据库，确认预期数据已删除
			num = SqlOperator.queryNumber(db, "select count(1) from " + Etl_sys.TableName
					+ " where etl_sys_cd=?", ETL_SYS_CD + 3)
					.orElseThrow(() -> new BusinessException("sql查询错误"));
			assertThat("删除操作后，确认这条数据已删除", num, is(0L));
			num = SqlOperator.queryNumber(db,
					"select count(1) from " + Etl_resource.TableName + " where etl_sys_cd=?",
					ETL_SYS_CD + 3)
					.orElseThrow(() -> new BusinessException("sql查询错误"));
			assertThat("删除操作后，确认这条数据已删除", num, is(0L));
			// 2.错误的数据访问1，etl_sys_cd为空
			bodyString = new HttpClient().addData("etl_sys_cd", "")
					.post(getActionUrl("deleteEtlProject"))
					.getBodyString();
			ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class)
					.orElseThrow(() -> new BusinessException("连接失败！"));
			assertThat(ar.isSuccess(), is(false));
			// 3.错误的数据访问2，etl_sys_cd不存在
			bodyString = new HttpClient().addData("etl_sys_cd", ETL_SYS_CD)
					.post(getActionUrl("deleteEtlProject"))
					.getBodyString();
			ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class)
					.orElseThrow(() -> new BusinessException("连接失败！"));
			assertThat(ar.isSuccess(), is(false));
			// 4.错误的数据访问3，工程下还有任务或者作业存在
			bodyString = new HttpClient().addData("etl_sys_cd", ETL_SYS_CD + 1)
					.post(getActionUrl("deleteEtlProject"))
					.getBodyString();
			ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class)
					.orElseThrow(() -> new BusinessException("连接失败！"));
			assertThat(ar.isSuccess(), is(false));
		}
	}
}
