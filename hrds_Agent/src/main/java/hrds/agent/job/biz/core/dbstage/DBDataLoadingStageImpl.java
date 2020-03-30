package hrds.agent.job.biz.core.dbstage;

import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Return;
import hrds.agent.job.biz.bean.CollectTableBean;
import hrds.agent.job.biz.bean.StageParamInfo;
import hrds.agent.job.biz.bean.StageStatusInfo;
import hrds.agent.job.biz.constant.RunStatusConstant;
import hrds.agent.job.biz.constant.StageConstant;
import hrds.agent.job.biz.core.AbstractJobStage;
import hrds.agent.job.biz.utils.JobStatusInfoUtil;
import hrds.commons.codes.CollectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DocClass(desc = "数据库直连采集数据加载阶段", author = "WangZhengcheng")
public class DBDataLoadingStageImpl extends AbstractJobStage {
	private final static Logger LOGGER = LoggerFactory.getLogger(DBUploadStageImpl.class);
	//数据采集表对应的存储的所有信息
	private CollectTableBean collectTableBean;

	public DBDataLoadingStageImpl(CollectTableBean collectTableBean) {
		this.collectTableBean = collectTableBean;
	}

	@Method(desc = "数据库直连采集数据加载阶段处理逻辑，处理完成后，无论成功还是失败，" +
			"将相关状态信息封装到StageStatusInfo对象中返回", logicStep = "")
	@Return(desc = "StageStatusInfo是保存每个阶段状态信息的实体类", range = "不会为null，StageStatusInfo实体类对象")
	@Override
	public StageParamInfo handleStage(StageParamInfo stageParamInfo) {
		LOGGER.info("------------------数据库直连采集数据加载阶段开始------------------");
		//1、创建卸数阶段状态信息，更新作业ID,阶段名，阶段开始时间
		StageStatusInfo statusInfo = new StageStatusInfo();
		JobStatusInfoUtil.startStageStatusInfo(statusInfo, collectTableBean.getTable_id(),
				StageConstant.DATALOADING.getCode());
		JobStatusInfoUtil.endStageStatusInfo(statusInfo, RunStatusConstant.SUCCEED.getCode(), "执行成功");
		LOGGER.info("------------------数据库直连采集数据加载阶段成功------------------");
		//结束给stageParamInfo塞值
		JobStatusInfoUtil.endStageParamInfo(stageParamInfo, statusInfo, collectTableBean
				, CollectType.ShuJuKuCaiJi.getCode());
		return stageParamInfo;
	}

	@Override
	public int getStageCode() {
		return StageConstant.DATALOADING.getCode();
	}
}
