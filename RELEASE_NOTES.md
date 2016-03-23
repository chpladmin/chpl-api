# Release Notes

## Version 0.4.0
_14 March 2016_

### Features added
* Add validation to 2014 products
* Update Download file generation
* Made Transparency Attestation an ENUM, URL per product
* Added "targeted users" to Certified Product
* Restrict uploads to CSV only
* Added CQM mappings to Certified Product
* Generate ATL/ACB codes if ATL/ACB is created

### Bugs fixed
* Fix null value in announcement activity

## Version 0.3.2
_29 February 2016_

### Features added
* Added ability to do new 2014 upload files

## Version 0.3.1
_22 February 2016_

### Bugs fixed
* Added missing product number to CP manange select box

## Version 0.3.0
_18 February 2016_

### Features added or Updated
* Changed Corrective Action Plan/Surveillance API parameters
* Added descriptions to API methods.
* Changed API for certified products and certification results to match 2014 data requirements.
* Allow multiple values for the 'hasCap' field in search.

## Version 0.2.0
_3 February 2016_

### Features added or Updated
* Set up swagger API and endpoint annotations
* Added code to create the XML download file
* Added code to allow a currently logged in user to get additional permissions if invited (/users/authorize)
* Adjust what happens when an ACB gets deleted. The ACB is marked as deleted and any users who were ONLY associated with that ACB (no other ACBs, no ATLs, no ADMIN role) will have their account disabled.
* Add service to undelete ACBs and ATLs accessed by /acbs/3/undelete or /atls/3/undelete
* Added service for announcements

### Bugs Fixed
* Allow certified products with errors to be edited and remove those errors
* Fix email content typos

## Version 0.1.1
_12 January 2016_

### New features
* Integrated Swagger API generation

### Bugs Fixed
* Search on certifications / cqms
* Allow editing of products with errors
* Fixed various email content issues

## Version 0.1.0
_5 January 2016_

Features added or Updated
* Added terms of use and api documentation to the certified product apis.
* Added ability to update transparencyAttestation field per vendor and ACB combination. ADMINs can update the transparencyAttestation for all vendor/ACB combinations and anyone else can only update that field for the ACBs to which they have access.
* Added transparencyAttestation to the fields that come back with certified product details and search results. It is inferred from the vendor/ACB mapping.
* Added APIs for testing labs, found under /atls urls. 
* Complete CHPL Product Number for 2015 products with testing lab code.

Bugs Fixed
* Correct CHPL number is inserted in corrective action plan documentation activity.
* Authorizing existing users for new roles or ACBs/ATLs is fixed 

## Version 0.0.2
_7 December 2015_

Features added or Updated
* Updated the format activity streams for users are returned.
* Updated search options to remove references to 2011 edition data in UI
* Added Api key requirement and Api key activity logging.
* Updated search to allow search by "hasCAP" (search by Corrective Action Plan)

Bugs fixed
* ETL inserts to correct 170.314 (b)(5)(A) or (b)(5)(B) depending on Ambulatory/Inpatient
* ETL correctly inserts CMS9 and CMS26 CQMs

## Version 0.0.1
_13 November 2015_

First release
