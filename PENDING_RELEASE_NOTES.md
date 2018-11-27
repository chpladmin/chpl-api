
## Version 20.0.0
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
* Implement user ability to reset their password using a unique link
* Change /cache_status endpoint to report "OK" status only when all pre-loaded caches have completed.
* Add "user must reset password on next login" workflow
  * Gives error when user tries to log in and needs to change password
  * Adds end point to change_expired_password
  * Update User edit to allow admins to require password change

---
