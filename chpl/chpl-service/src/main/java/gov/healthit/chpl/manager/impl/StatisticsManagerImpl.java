package gov.healthit.chpl.manager.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.dao.StatisticsDAO;
import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.manager.StatisticsManager;

@Service
public class StatisticsManagerImpl implements StatisticsManager {
	
	@Autowired StatisticsDAO statisticsDao;

	@Override
	public Long getTotalDevelopers(DateRange dateRange) {
		if(dateRange != null){
			// Set startDate to beginning of time if null
			if(dateRange.getStartDate() == null){
				dateRange.setStartDate(new Date(0));
			}
			// Set endDate to today if null
			if(dateRange.getEndDate() == null){
				dateRange.setEndDate(new Date());
			}
			return statisticsDao.getTotalDevelopers(dateRange);
		}
		else {
			DateRange allDates = new DateRange(new Date(0), new Date());
			return statisticsDao.getTotalDevelopers(allDates);
		}
	}

	@Override
	public Long getTotalDevelopersWith2014Listings(DateRange dateRange) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getTotalDevelopersWith2015Listings(DateRange dateRange) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getTotalCertifiedProducts(DateRange dateRange) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getTotalCPsActive2014Listings(DateRange dateRange) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getTotalCPsActive2015Listings(DateRange dateRange) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getTotalCPsActiveListings(DateRange dateRange) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getTotalListings(DateRange dateRange) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getTotalActive2014Listings(DateRange dateRange) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getTotalActive2015Listings(DateRange dateRange) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getTotal2014Listings(DateRange dateRange) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getTotal2015Listings(DateRange dateRange) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getTotal2011Listings(DateRange dateRange) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getTotalSurveillanceActivities(DateRange dateRange) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getTotalOpenSurveillanceActivities(DateRange dateRange) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getTotalClosedSurveillanceActivities(DateRange dateRange) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getTotalNonConformities(DateRange dateRange) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getTotalOpenNonconformities(DateRange dateRange) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getTotalClosedNonconformities(DateRange dateRange) {
		// TODO Auto-generated method stub
		return null;
	}

}
