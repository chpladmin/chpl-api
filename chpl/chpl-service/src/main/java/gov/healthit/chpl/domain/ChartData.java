package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.ChartDataDTO;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ChartData implements Serializable {
	
	private static final long serialVersionUID = 3313308808845667852L;
	
	private Long id;
	private Long date;
	private String data;
	private ChartDataStatType statisticType;
	
	public ChartData(ChartDataDTO dto) throws ParseException{
		this.id = dto.getId();
		SimpleDateFormat f = new SimpleDateFormat("yyyy-mm-dd");
		this.date = f.parse(dto.getDate().toString()).getTime();
		this.data = dto.getData();
		this.statisticType = new ChartDataStatType(dto.getStatisticType());
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public Long getDate() {
		return date;
	}

	public void setDate(Long date) {
		this.date = date;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public ChartDataStatType getStatisticType() {
		return statisticType;
	}

	public void setStatisticType(ChartDataStatType statisticType) {
		this.statisticType = statisticType;
	}
}
