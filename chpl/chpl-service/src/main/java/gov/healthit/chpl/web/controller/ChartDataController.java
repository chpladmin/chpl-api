package gov.healthit.chpl.web.controller;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.Announcement;
import gov.healthit.chpl.domain.ChartData;
import gov.healthit.chpl.domain.ChartDataStatType;
import gov.healthit.chpl.dto.AnnouncementDTO;
import gov.healthit.chpl.dto.ChartDataDTO;
import gov.healthit.chpl.dto.ChartDataStatTypeDTO;
import gov.healthit.chpl.manager.ChartDataManager;
import gov.healthit.chpl.web.controller.results.AnnouncementResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Api(value="data")
@RestController
@RequestMapping(value="/data")
public class ChartDataController {
	
private static final Logger logger = LogManager.getLogger(AnnouncementController.class);
	
	@Autowired private ChartDataManager chartDataManager;
	
	@ApiOperation(value="Get all types of statistics")
	@RequestMapping(value="/statistic_types", method=RequestMethod.GET,produces="application/json; charset=utf-8")
	public @ResponseBody List<ChartDataStatType> getStatisticTypes() {
		List<ChartDataStatTypeDTO> results = chartDataManager.getAllTypes();
		
		List<ChartDataStatType> ret = new ArrayList<ChartDataStatType>();
		for(ChartDataStatTypeDTO stat : results){
			ret.add(new ChartDataStatType(stat));
		}
		return ret;
	}
	
	@ApiOperation(value="Get all types of statistics")
	@RequestMapping(value="/statistics", method=RequestMethod.GET,produces="application/json; charset=utf-8")
	public @ResponseBody List<ChartData> getStatistics() throws ParseException {
		List<ChartDataDTO> results = chartDataManager.getAllData();
		
		List<ChartData> ret = new ArrayList<ChartData>();
		for(ChartDataDTO stat : results){
			ret.add(new ChartData(stat));
		}
		return ret;
	}

}
