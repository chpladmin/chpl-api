
## Version 21.0.0
_Date TBD_

### Backwards compatibility breaking features
* Moved permissions from many-to-many relationship with users to a one-to-many so that each user may only have one role. Invitations are sent slightly differently and logic enforces a user having a single role within the system while ACB and ATL users may still have access to multiple ACBs and ATLs. Returned user data is also slightly different as it has only a single role per user rather than a set of granted permissions. Affected endpoints include:
  * /users/invite
  * /users
  * /acbs/{id}/users
  * /atls/{id}/users

### New Features
* Added endpoint /collections/decertified-developers to improve response time getting that data. This endpoint will eventually replace /decertifications/developers which has been deprecated.
* Updated security for edit/split/merge of developers, products, and versions

---
