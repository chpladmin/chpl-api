
## Version 19.2.0
_Date TBD_

### Backwards compatibility breaking changes
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

### New Features
* 2015 Functionality testing is restricted by criteria.
* Add retired flag to ACBs and ATLs to replace the functionality that previously used the deleted flag.
* Save 'reason' for developer's status change in questionable activivty.
* Output 'reason' for developer's status change on questionable activity report.

---

