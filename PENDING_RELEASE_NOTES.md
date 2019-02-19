
## Version 20.5.0
_Date TBD_

### New features
* Update behavior of pre-loaded caches which will affect the /cache_status call.
  * All pre-loaded caches have an additional copy loaded in the background when their data changes. The background cache is then swapped with the live cache when necessary.
  * The /cache_status call was reporting OK after initial startup but would later report INITIALIZING if user actions changed any cached data. Now once it reports OK it should continue to do so.

---
