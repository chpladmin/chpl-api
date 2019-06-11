
_surveillance-reporting_
* Added endpoint /data/quarters to get back a list of options for quarterly reporting.
* Added endpoints to get, create, update, and delete quarterly reports for authorized users.
  * GET /surveillance-report/quarterly
  * GET /surveillance-report/quarterly/{id}
  * POST /surveillance-report/quarterly
  * PUT /surveillance-report/quarterly
  * DELETE /surveillance-report/quarterly/{id}
* Added capabiltiy to export some quarterly report data as an XLSX spreadsheet.
* Added endpoints to get, create, update, and delete annual reports for authorized users.
  * GET /surveillance-report/annual
  * GET /surveillance-report/annual/{id}
  * POST /surveillance-report/annual
  * PUT /surveillance-report/annual
  * DELETE /surveillance-report/annual/{id}
* Added capability to export some annual report data as an XLSX spreadsheet.

