
## Version 19.0.0
_Date TBD_

### Backwards compatibility breaking API changes
* Changed PUT /products call to not accept a productID

### New Features
* Add developer and product contact information to 2014/2015 download file

### Bugs Fixed
* Properly handle invalid test tools entered into upload files by removing them and informing the user
* Make sure test tools are optional for 2014 ambulatory listings on g1, g2, and f3
* Remove required productID in /products PUT call that isn't used by the back end
* Insert listing update activity during meaningful use user uploads

---
