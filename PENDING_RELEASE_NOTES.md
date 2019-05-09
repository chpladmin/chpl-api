
## Version 20.11.0
_Date TBD_

### New Features
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
* Add activity for split actions to allow for better history display in listing 'eye'.
* Add temporary quartz job to insert missing split activity. Can only be run by ADMIN and should be removed in a following release.
* Add endpoints to provide ability for users to save filters for admin reports
* Respond with a 401 error when the user's token has expired
* Add FF4j framework (feature flags)

---
