package cn.cf.task.schedule.operation;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.regex.Pattern;

import cn.cf.common.ExportDoJsonParams;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.alibaba.fastjson.JSON;

import cn.cf.common.ColAuthConstants;
import cn.cf.common.Constants;
import cn.cf.common.ExportUtil;
import cn.cf.common.utils.BeanUtils;
import cn.cf.common.utils.CommonUtil;
import cn.cf.common.utils.OSSUtils;
import cn.cf.common.utils.ZipUtils;
import cn.cf.dao.SysExcelStoreExtDao;
import cn.cf.dto.SysExcelStoreDto;
import cn.cf.dto.SysExcelStoreExtDto;
import cn.cf.entity.BussStoreDataReport;
import cn.cf.entity.ReportFormsDataTypeParams;
import cn.cf.model.SysExcelStore;
import cn.cf.task.schedule.ScheduledFutureMap;
import cn.cf.util.DateUtil;

public class BussStoreDataRunnable implements Runnable {
	private String name;

	private String fileName;

	private String accountPk;
	private SysExcelStoreExtDto storeDtoTemp;
	private String uuid;

	private SysExcelStoreExtDao storeDao;
	public BussStoreDataRunnable() {


	}
	public BussStoreDataRunnable(String name,String uuid) {

		this.name = name;
		this.uuid =uuid;
	}

	@Override
	public void run() {
        //上传数据
        ScheduledFuture future = null;
        if (CommonUtil.isNotEmpty(this.name)) {
        	future = ScheduledFutureMap.map.get(this.name);
		}
        try {
            upLoadFile();
        } catch (Exception e) {
			ExportDoJsonParams.updateErrorExcelStoreStatus(this.storeDao,this.storeDtoTemp,e.getMessage());
        } finally {
            ScheduledFutureMap.stopFuture(future,this.name);
        }
    }

	private void upLoadFile() {

		MongoTemplate mongoTemplate = (MongoTemplate) BeanUtils.getBean("mongoTemplate");
		this.storeDao = (SysExcelStoreExtDao) BeanUtils.getBean("sysExcelStoreExtDao");
		if (storeDao != null) {
			Map<String, Object> map = new HashMap<>();
			map.put("isDeal", Constants.TWO);
			map.put("methodName", "exportBussStoreData_"+StringUtils.defaultIfEmpty(this.uuid, ""));
			// 查询存在要导出的任务
			List<SysExcelStoreExtDto> list = storeDao.getFirstTimeExcelStore(map);
			if (list != null && list.size() > Constants.ZERO) {
				for (SysExcelStoreExtDto storeDto : list) {
					this.storeDtoTemp = storeDto;
					this.fileName = "运营中心-数据报表-交易数据-店铺日交易数据-" + storeDto.getAccountName() + "-"
							+ DateUtil.formatYearMonthDayHMS(new Date());
					if (CommonUtil.isNotEmpty(storeDto.getParams())) {
						ReportFormsDataTypeParams params = JSON.parseObject(storeDto.getParams(),
								ReportFormsDataTypeParams.class);
						this.accountPk = storeDto.getAccountPk();
						if (mongoTemplate != null) {
								// 设置查询参数
								Query query = reportParams(params, storeDto);
								// 设置权限
								String ossPath = "";
								int counts = (int) mongoTemplate.count(query, BussStoreDataReport.class);
								if (counts > Constants.EXCEL_NUMBER_TENHOUSAND) {
									// 大于10000，分页查询数据
									ossPath = setLimitPages(query, counts, mongoTemplate, storeDto);
								} else {
									// 如果小于或等于10000条直接上传
									ossPath = setNotLimitPages(query, mongoTemplate, storeDto);
								}
								// 更新订单导出状态
							ExportDoJsonParams.updateExcelStoreStatus(storeDto,storeDao, ossPath);
							}
							

					}
				}
			}
		}
	}

	private String setLimitPages(Query query, int counts, MongoTemplate mongoTemplate, SysExcelStoreExtDto storeDto) {
		double numbers = Math.floor(counts / Constants.EXCEL_NUMBER_TENHOUSAND);// 获取分页查询次数

		List<File> fileList = new ArrayList<>();
		for (int i = Constants.ZERO; i < numbers + Constants.ONE; i++) { //
			int start = Constants.ZERO;
			if (i != Constants.ZERO) {
				start = ExportDoJsonParams.indexPageNumbers(i);
			}
			query.skip(start).limit(Constants.EXCEL_NUMBER_TENHOUSAND);
			List<BussStoreDataReport> list = mongoTemplate.find(query, BussStoreDataReport.class);
			if (list != null && list.size() > Constants.ZERO) {
				getCol(list);
				ExportUtil<BussStoreDataReport> exportUtil = new ExportUtil<BussStoreDataReport>();
				String excelName = "运营中心-数据报表-交易数据-店铺日交易数据-" + storeDto.getAccountName() + "-"
						+ DateUtil.formatYearMonthDayHMS(new Date())+"-"+(i+1);
				String path = exportUtil.exportDynamicUtil("bussStoreDataReport", excelName, list);
				File file = new File(path);
				fileList.add(file);
			}
		}
		// 上传Zip到OSS
		return OSSUtils.ossUploadXls(new File(ZipUtils.fileToZip(fileList, this.fileName)), Constants.FILE);
	}

	private void getCol(List<BussStoreDataReport> list) {
		String regex = "*****";
		if (list.size() > 0) {
			for (BussStoreDataReport report : list) {
				if (report != null && report.getAccumWeight() != null) {
					report.setAccumWeight(report.getAccumWeight().setScale(4, BigDecimal.ROUND_DOWN));
				}
				if (report != null && report.getWeight() != null) {
					report.setWeight(report.getWeight().setScale(4, BigDecimal.ROUND_DOWN));
				}
				if (!CommonUtil.isExistAuthName(accountPk,
						ColAuthConstants.OPER_REPORTFORMS_TRANSSTORENUM_COL_STORENAME)) {
					report.setStoreName(CommonUtil.setColRegexParams(regex, report.getStoreName()));
				}
			}
		}
	}
	private String setNotLimitPages(Query query, MongoTemplate mongoTemplate, SysExcelStoreExtDto storeDto) {
		String ossPath = "";
		List<BussStoreDataReport> list = mongoTemplate.find(query, BussStoreDataReport.class);
		if (list != null && list.size() > Constants.ZERO) {
			getCol(list);
			ExportUtil<BussStoreDataReport> exportUtil = new ExportUtil<BussStoreDataReport>();
			String path = exportUtil.exportDynamicUtil("bussStoreDataReport", this.fileName, list);
			File file = new File(path);
			// 上传到OSS
			ossPath = OSSUtils.ossUploadXls(file, Constants.FIVE);
		}
		return ossPath;
	}

	private Query reportParams(ReportFormsDataTypeParams params, SysExcelStoreExtDto storeDto) {
		Query query = new Query();
		List<Order> orderList = new ArrayList<Order>();
		if (params.getStoreName() != null && !params.getStoreName().equals("")) {
			Pattern pattern = Pattern.compile("^.*" + params.getStoreName() + ".*$", Pattern.CASE_INSENSITIVE);
			query.addCriteria(Criteria.where("storeName").regex(pattern));
		}
		if (params.getStartTime() != null && !params.getStartTime().equals("")) {
			if (params.getEndTime() != null && !params.getEndTime().equals("")) {
				query.addCriteria(Criteria.where("date").gte(params.getStartTime()).lte(params.getEndTime()));
			} else {
				query.addCriteria(Criteria.where("date").gte(params.getStartTime()));
			}
		} else if (params.getEndTime() != null && !params.getEndTime().equals("")) {
			query.addCriteria(Criteria.where("date").lte(params.getEndTime()));
		}
		if (null != params.getIds() && !"".equals(params.getIds())) {
			List<String> idsList = getIds(params);
			query.addCriteria(Criteria.where("_id").in(idsList));
		}
		orderList.add(new Order(Direction.DESC, "date"));
		orderList.add(new Order(Direction.DESC, "weight"));
		orderList.add(new Order(Direction.DESC, "amount"));
		query.with(new Sort(orderList));
		return query;
	}

	private static List<String> getIds(ReportFormsDataTypeParams params) {
		List<String> idsList = new ArrayList<>();
		if (params.getIds().contains(",")) {
			String[] idarr = params.getIds().split(",");
			for (int i = 0; i < idarr.length; i++) {
				idsList.add(idarr[i]);
			}
		} else {
			idsList.add(params.getIds());
		}
		return idsList;
	}


	public static String bussStoreDataParam(ReportFormsDataTypeParams params, SysExcelStore store) {
		String paramStr = "";
		if (CommonUtil.isNotEmpty(params.getStoreName())) {
			paramStr = "店铺名称：" + params.getStoreName()+";";
		}
		paramStr = CommonUtil.getDateShow(paramStr,  params.getStartTime(), params.getEndTime(),  "日期：");

		if (null != params.getIds() && !"".equals( params.getIds())) {
			List<String> idsList = getIds(params);
			paramStr = paramStr + "勾选" + idsList.size()+"条数据;";
		}
		return paramStr;
	}
}
