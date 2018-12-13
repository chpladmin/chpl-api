
## Version 20.0.0
_Date TBD_

### Backwards compatibility breaking changes
* Removed all previously deprecated API endpoints
* Removed the following endpoints:
  * /atls/{id}/delete
  * /atls/{id}/undelete
  * /acbs/{id}/delete
  * /acbs/{id}/undelete
* Remove showDeleted parameter from the following endpoints:
  * /activity/acbs
  * /activity/acbs/{id}
  * /activity/atls
  * /activity/atls/{id}
  * /data/search_options
  * /atls
  * /acbs
* Removed space following the colon in the /cache_status response. {"status": "OK"} becomes {"status":"OK"}
* Removed space following the colon in the /status response. {"status": "OK"} becomes {"status":"OK"}
* Removed /notifications endpoints.

### New Features
* Add retired flag to ACBs and ATLs to replace the functionality that previously used the deleted flag.
* Add Quartz job to require all users to change password on next login
* Update email notification about potential Developer ban to include:
  * Reason for status change
  * Reason for listing change
* Prevent users from using the following macra measures which are under review: RT13 EH/CAH Stage 3, RT14 EH/CAH Stage 3, RT15 EH/CAH Stage 3 
* Update invalid error and warning messages for upload file

### Bugs Fixed
* Do not show error for 2014 listings on upload or edit if they attest to g3, have ICS = true, and do not have any criteria marked as SED.
* Fix issue with determining whether there is additional software associated with the certified product

---
