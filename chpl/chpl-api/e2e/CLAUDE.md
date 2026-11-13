# CLAUDE.md — E2E API tests

Guidance for writing and running the automated end-to-end API tests in this directory. These are **Postman collections run with Newman** against a deployed CHPL API (dev/qa/stg). 

## Layout

```
chpl/chpl-api/e2e/
├── collections/   One Postman collection (v2.1 JSON) per controller: <name>-controller.postman_collection.json
├── data/          Iteration data (JSON arrays) for data-driven folders, plus upload fixtures (CSV)
├── env/           <env>.postman_environment.json — only sample.postman_environment.json is committed
└── script/newman-run.js   Runs every collection in series; one entry per collection/folder
```

JUnit reports are written to `newman/` at the repo root (gitignored).

## Running

Run from the **repo root** (paths in `newman-run.js` are relative to it):

```bash
yarn install
yarn e2e:dev        # or e2e:local / e2e:qa / e2e:stg — uses env/<env>.postman_environment.json
yarn e2e:lint
```

Run a single collection or folder while developing:

```bash
npx newman run chpl/chpl-api/e2e/collections/ucd-processes-controller.postman_collection.json \
  -e chpl/chpl-api/e2e/env/dev.postman_environment.json --insecure
# add --folder "ROLE_ONC" and/or -d chpl/chpl-api/e2e/data/<file>.json for data-driven folders
```

Always run the new/modified collection and confirm it passes before finishing.

## Environment variables

Env files hold real credentials — they are gitignored. **Never commit them or print their values.** Only `env/sample.postman_environment.json` is tracked; if you add a new variable, add a placeholder for it there.

Variables used by the collections: `url`, `apiKey`, `authurl`, `authTokenField`, `token` (set at runtime), `emailADMIN`/`passwordADMIN`, `emailONC`/`passwordONC`, `emailACB`/`passwordACB`, `emailDeveloper`/`passwordDeveloper`, and for performance tests `baselineSeconds_getDetails`, `baselineSeconds_putUpdate`, `tolerancePercent`.

## Writing tests

### Requests
- URL: `{{url}}/rest/<path>`.
- Every request sends headers `API-Key: {{apiKey}}` and `Content-Type: application/json`.
- Authenticated requests use request-level auth `bearer` with `{{token}}`. Anonymous requests use auth `noauth`.
- No collection-level auth, scripts, or variables are used — everything is set per request.
- Request name format: `<METHOD> /<path> - <short description> - <role>`, e.g. `PUT /ucd-processes - Provides functionality to update the name of a UCD Process - ONC user`.
- Group requests by role in folders named `ROLE_ADMIN`, `ROLE_ONC`, `ROLE_ACB`, `AnonymousUsers` when a collection covers several roles.

### Login (pre-request script)
Authenticated requests obtain a token in their pre-request script — copy this pattern, changing only the role:

```js
var ReqBody = {
    userName: pm.environment.get("emailONC"), password: pm.environment.get("passwordONC")
};
pm.sendRequest({
    url: pm.environment.get("authurl"),
    method: "POST",
    header: {
        "Content-Type": "application/json",
        "API-KEY": pm.environment.get("apiKey")
    },
    body: JSON.stringify(ReqBody)
}, function (err, response) {
    var jsonData = response.json();
    pm.environment.set("token", jsonData[pm.environment.get("authTokenField")]);
});
```

### Test scripts
- Use `pm.test("<readable sentence of what is expected>", function () { ... })` with `pm.response.to.have.status(...)` and `pm.expect(...)` (Chai BDD).
- Typical checks: status code, response not null, expected properties present (`to.have.property('id')`), and specific values (`to.eql(...)`).
- Cover the positive case for each allowed role and a negative case for disallowed access (anonymous → `401`), and invalid input (→ `400` with the expected error message) where relevant.

```js
var jsonData = pm.response.json();
pm.test("expect GET ucd-process endpoint to return 200 status and response body to not be empty", function () {
    pm.response.to.have.status(200);
    pm.expect(jsonData.response).not.eq(null);
});
```

### Data-driven tests
- Put iteration data in `data/<name>-test-data.json` as a JSON array of objects.
- Read values with `pm.iterationData.get("<field>")`; put such requests in a dedicated folder (e.g. `GET : Developers: data driven tests`).

### Test data safety
Tests run against shared, deployed environments. Use existing records for reads; for PUT/POST, use idempotent changes (e.g. update a record to a known value) and avoid deleting data.

## Adding a new collection or folder to the run

Every collection (or folder run separately) needs an entry in `script/newman-run.js`:

1. Add a config object:
   ```js
   const fooControllerTests = {
     ...commonOptions,
     collection: collection_path + '/foo-controller.postman_collection.json',
     // folder: '<folder name>',                           // only when running one folder
     // iterationData: data_path + '/foo-test-data.json',  // only for data-driven folders
     reporter: {
       junit: {
         export: reports_path + '/foo-controller-tests.xml',
       },
     },
   };
   ```
2. Add `cb => newman.run(fooControllerTests,cb),` to the `jobs` array.

When adding requests to an existing collection that is already in `jobs`, no script change is needed.

## Checklist
1. Collection file is `collections/<name>-controller.postman_collection.json` (Postman v2.1 export).
2. Each request has `API-Key` / `Content-Type` headers, correct auth (`bearer {{token}}` or `noauth`), and a login pre-request script if authenticated.
3. Request names follow `<METHOD> /<path> - <description> - <role>`; tests have descriptive `pm.test` names.
4. Positive, unauthorized, and invalid-input cases covered.
5. New collections/folders registered in `script/newman-run.js`.
6. No credentials committed; new env variables added to `env/sample.postman_environment.json`.
7. Collection passes with `npx newman run ...` against the target environment.
