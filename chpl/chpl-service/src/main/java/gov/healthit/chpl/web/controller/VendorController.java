package gov.healthit.chpl.web.controller;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.UpdateVendorsRequest;
import gov.healthit.chpl.domain.Vendor;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.VendorDTO;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.manager.VendorManager;
import gov.healthit.chpl.web.controller.results.VendorResults;

@RestController
@RequestMapping("/vendors")
public class VendorController {
	
	@Autowired VendorManager vendorManager;
	@Autowired ProductManager productManager;
	
	@RequestMapping(value="/", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody VendorResults getVendors(){
		List<VendorDTO> vendorList = vendorManager.getAll();		
		
		List<Vendor> vendors = new ArrayList<Vendor>();
		if(vendorList != null && vendorList.size() > 0) {
			for(VendorDTO dto : vendorList) {
				Vendor result = new Vendor(dto);
				vendors.add(result);
			}
		}
		
		VendorResults results = new VendorResults();
		results.setVendors(vendors);
		return results;
	}
	
	@RequestMapping(value="/{vendorId}", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Vendor getVendorById(@PathVariable("vendorId") Long vendorId) throws EntityRetrievalException {
		VendorDTO vendor = vendorManager.getById(vendorId);
		
		Vendor result = null;
		if(vendor != null) {
			result = new Vendor(vendor);
		}
		return result;
	}
	
	@RequestMapping(value="/update", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public Vendor updateVendor(@RequestBody(required=true) UpdateVendorsRequest vendorInfo) throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
		VendorDTO result = null;
		
		if(vendorInfo.getVendorIds().size() > 1) {
			//merge these vendors into one 
			// - create a new vendor with the rest of the passed in information
			VendorDTO toCreate = new VendorDTO();
			toCreate.setVendorCode(vendorInfo.getVendor().getVendorCode());
			toCreate.setName(vendorInfo.getVendor().getName());
			toCreate.setWebsite(vendorInfo.getVendor().getWebsite());
			Address vendorAddress = vendorInfo.getVendor().getAddress();
			if(vendorAddress != null) {
				AddressDTO toCreateAddress = new AddressDTO();
				toCreateAddress.setStreetLineOne(vendorAddress.getLine1());
				toCreateAddress.setStreetLineTwo(vendorAddress.getLine2());
				toCreateAddress.setCity(vendorAddress.getCity());
				toCreateAddress.setState(vendorAddress.getState());
				toCreateAddress.setZipcode(vendorAddress.getZipcode());
				toCreateAddress.setCountry(vendorAddress.getCountry());
				toCreate.setAddress(toCreateAddress);
			}
			result = vendorManager.merge(vendorInfo.getVendorIds(), toCreate);
			//re-query because the vendor code isn't filled in otherwise
			result = vendorManager.getById(result.getId());
		} else if(vendorInfo.getVendorIds().size() == 1) {
			//update the information for the vendor id supplied in the database
			VendorDTO toUpdate = new VendorDTO();
			toUpdate.setVendorCode(vendorInfo.getVendor().getVendorCode());
			toUpdate.setId(vendorInfo.getVendorIds().get(0));
			toUpdate.setName(vendorInfo.getVendor().getName());
			toUpdate.setWebsite(vendorInfo.getVendor().getWebsite());
			if(vendorInfo.getVendor().getAddress() != null) {
				AddressDTO address = new AddressDTO();
				address.setId(vendorInfo.getVendor().getAddress().getAddressId());
				address.setStreetLineOne(vendorInfo.getVendor().getAddress().getLine1());
				address.setStreetLineTwo(vendorInfo.getVendor().getAddress().getLine2());
				address.setCity(vendorInfo.getVendor().getAddress().getCity());
				address.setState(vendorInfo.getVendor().getAddress().getState());
				address.setZipcode(vendorInfo.getVendor().getAddress().getZipcode());
				address.setCountry(vendorInfo.getVendor().getAddress().getCountry());
				toUpdate.setAddress(address);
			}
			result = vendorManager.update(toUpdate);
		}
		
		if(result == null) {
			throw new EntityCreationException("There was an error inserting or updating the vendor information.");
		}
		Vendor restResult = new Vendor(result);
		return restResult;
	}
}
