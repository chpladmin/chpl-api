
## Version 21.0.0
_Date TBD_

### Backwards compatibility breaking features
* Moved permissions from many-to-many relationship with users to a one-to-many so that each user may only have one role. Invitations are sent slightly differently and logic enforces a user having a single role within the system while ACB and ATL users may still have access to multiple ACBs and ATLs. Returned user data is also slightly different as it has only a single role per user rather than a set of granted permissions. Affected endpoints include:
  * /users/invite
  * /users
  * /acbs/{id}/users
  * /atls/{id}/users

### New features
* Quartz jobs that gather data delete old data and insert new data within a single transaction

---
