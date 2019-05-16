
## Version 21.0.0
_Date TBD_

### Backwards compatibility breaking features
* Moved permissions from many-to-many relationship with users to a one-to-many so that each user may only have one role. Invitations are sent slightly differently and logic enforces a user having a single role within the system while ACB and ATL users may still have access to multiple ACBs and ATLs. Returned user data is also slightly different as it has only a single role per user rather than a set of granted permissions. Affected endpoints include:
  * /users/invite
  * /users
  * /acbs/{id}/users
  * /atls/{id}/users

### New features
* Add FF4j framework (feature flags)
* Add endpoints to provide ability for users to save filters for admin report
* Updated product and version activity metadata to parse activity for merges and splits; better parsing of product and developer names if they have been deleted.
* Added endpoints for ACB and ATL activity metadata.
  * /activity/metadata/acbs
  * /activity/metadata/acb/{acbId}
  * /activity/metadata/atls
  * /activity/metadata/atl/{atlId}
* Deprecated existing endpoints for ACB and ATL activity
  * /activity/acbs
  * /activity/acb/{acbId}
  * /activity/atls
  * /activity/atl/{atlId}

---
