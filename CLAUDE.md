# CLAUDE.md

Guidance for Claude Code when working in this repository, with a focus on **writing unit tests that match the existing conventions**.

## Project overview

CHPL (Certified Health IT Product List) API — a Java / Spring web application packaged as a WAR and deployed to Tomcat.

- **Java 25**, **Maven 3.9.x** (enforced by `maven-enforcer-plugin`)
- **Spring Framework 7 / Spring Security 7** (not Spring Boot test infrastructure — tests do not load a Spring context)
- **Lombok** — domain objects are built with `@Builder` (`Foo.builder()...build()`)
- **FF4j** feature flags (`gov.healthit.chpl.FeatureList` constants, checked with `ff4j.check(...)`)
- **Quartz** scheduler for background jobs
- **Log4j2** logging
- **Checkstyle** rules in `chpl/chpl-checkstyle-checks.xml` (no tab characters; 4-space indentation)

### Test stack
- **JUnit Jupiter (JUnit 5/6 API)** — `org.junit.jupiter.api.*`
- **Mockito 5** (`mockito-core`) — including `Mockito.mockStatic` for static utilities
- `spring-test` only for helpers like `MockMultipartFile`
- **No** AssertJ, Hamcrest, `@ParameterizedTest`, `@Nested`, `@DisplayName`, `@ExtendWith(MockitoExtension.class)`, or Spring context tests. Do not introduce them.

### Repository layout

```
chpl-api/                       (git root)
├── chpl/                       (Maven parent: gov.healthit:chpl)
│   ├── pom.xml
│   ├── chpl-checkstyle-checks.xml
│   ├── chpl-api/               Web layer: controllers (web/), filters, security config, rate limiting
│   │   └── src/test/java/...   (only 1 legacy test — avoid adding tests here unless testing web-layer code)
│   ├── chpl-service/           Business logic: managers, DAOs, validation/reviewers, upload handlers,
│   │   │                       normalizers, permissions, scheduler jobs, domain objects
│   │   └── src/test/java/gov/healthit/chpl/   <-- almost all unit tests live here
│   └── chpl-resources/         Shared properties (errors.properties, environment.properties), email templates, Quartz XML
├── newman/                     Postman/newman E2E API tests (run with `yarn e2e:<env>`)
└── docker/, tomcat-config/     Deployment config
```

## Running tests

Run Maven from the `chpl/` directory:

```bash
cd chpl
mvn test -pl chpl-service -am                                                   # all service tests
mvn test -pl chpl-service -am -Dtest=UrlReviewerTest -Dsurefire.failIfNoSpecifiedTests=false   # one class
mvn test -pl chpl-service -am -Dtest=UrlReviewerTest#review_nullSvapNoticeUrl_noError -Dsurefire.failIfNoSpecifiedTests=false   # one method
```

`chpl-service` depends on the `chpl-resources` module, so `-am` builds it too. Without `-am`, `chpl-resources` has to be installed in your local Maven repository first (`mvn install -pl chpl-resources`).

Always run the new/modified test class after writing it and confirm it passes.

---

## How to write a new unit test

### 1. Location and package
- Put the test in `chpl/chpl-service/src/test/java/` under **the exact same package as the class under test**.
  - `gov.healthit.chpl.validation.listing.reviewer.UrlReviewer` → `.../test/java/gov/healthit/chpl/validation/listing/reviewer/UrlReviewerTest.java`
- Because the package matches, the class under test is usually **not imported**.
- Exception: permission tests live in `gov.healthit.chpl.permissions.domain.<area>` while the classes under test live in `gov.healthit.chpl.permissions.domains.<area>` (note `domain` vs `domains`) and are imported explicitly.

### 2. Class naming and declaration
- Class name = `<ClassUnderTest>Test` (e.g. `SvapReviewerTest`, `UcdProcessNormalizerTest`, `RealWorldTestingManagerTest`).
- Declare it `public class ...Test` (every existing test class is `public`). No class-level annotations.

### 3. Test method naming — `methodUnderTest_condition_expectedResult`
This is the dominant convention (~3000 of ~3000 test methods). Use **lowerCamelCase segments separated by underscores**:

```
<methodUnderTest>_<scenario/inputState>_<expectedOutcome>
```

Real examples from the codebase:
- `review_nullSvapNoticeUrl_noError`
- `review_duplicateDateExists_errorFound`
- `review_associatesInvalidCriterion_hasWarningAndRemovesCriterion`
- `normalize_emptyUcdProcesses_noChanges`
- `normalize_ucdProcessNameFound_fillsInId`
- `normalize_decertificationDateStartWhitespaceAround_trimsCorrectly`
- `search_statusClosedProvidedAndHasMatches_findsMatches`
- `validate_pageSizeLargerThanMax_addsError`
- `isValid_DeveloperExists_ReturnsTrue`
- `uploadRealWorldTestingCsv_EmptyFile_ValidationException`
- `parseUcdProcess_NoUcdProcessColumns_ReturnsEmptyList`

Rules:
- The first segment is the name of the public method being exercised (`review`, `normalize`, `handle`/`parseX`, `isValid`, `search`, `validate`, etc.).
- **Match the casing style already used in the file you are editing.** Reviewer/normalizer/search tests use lowercase-starting segments (`_noError`, `_hasWarning`); some older manager/handler/change-request tests capitalize the later segments (`_EmptyFile_ValidationException`, `_ReturnsTrue`). In a new file, prefer lowercase-starting segments.
- Common outcome phrases — reuse them: `noError`, `noErrors`, `noWarning`, `hasError`, `hasErrors`, `errorFound`, `hasWarning`, `noChanges`, `noChange`, `fillsInId`, `setsFieldNull`, `returnsX`, `findsMatches`, `findsNoMatches`, `addsError`, `throwsValidationException` / `ValidationException`.
- Do **not** use a `test` prefix (`testFoo`) — that style exists only in a handful of legacy tests.

Permission tests are the one exception — they override abstract methods and are named exactly `hasAccess_Admin`, `hasAccess_Onc`, `hasAccess_Acb`, `hasAccess_Cms`, `hasAccess_Anon` (and optionally `hasAccess_Developer`). See section 7.

### 4. Class structure (order matters)

```java
package gov.healthit.chpl.<same.package.as.class.under.test>;

// 1. static JUnit assertion imports (only the ones used)
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// 2. java.* imports
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// 3. third-party imports (org.*) — alphabetical
import org.ff4j.FF4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

// 4. gov.healthit.chpl.* imports — alphabetical
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class FooReviewerTest {
    // a) private static final String constants mirroring messages in errors.properties
    private static final String SOME_ERROR = "The value for %s is not valid.";

    // b) private fields for mocks and the object under test
    private ErrorMessageUtil msgUtil;
    private FooDAO fooDao;
    private FooReviewer reviewer;

    // c) @BeforeEach setup
    @BeforeEach
    public void setup() {
        ...
    }

    // d) @Test methods
    @Test
    public void review_nullFoo_noError() {
        ...
    }

    // e) private helper methods at the bottom of the class
    private CertifiedProductSearchDetails getListingWithFoo(...) { ... }
}
```

- Imports: use explicit imports, never wildcards. Use **static imports only for `org.junit.jupiter.api.Assertions.*`**. Do **not** statically import Mockito — always write `Mockito.mock(...)`, `Mockito.when(...)`, `ArgumentMatchers.eq(...)`, `ArgumentMatchers.any()`.
- Setup method: `@BeforeEach public void setup()` (most common; `before()` also appears). It may declare `throws` for checked exceptions thrown by stubbed methods, e.g. `public void setup() throws EntityRetrievalException`.
- Test methods are `public void` and declare checked exceptions with `throws` (`throws ValidationException`, `throws EntityRetrievalException`, `throws Exception`) rather than catching them.
- Indent with 4 spaces; no tabs.

### 5. Creating the object under test and its mocks (preferred style)
Production classes use constructor injection (`@Autowired` constructors). In tests, **create mocks manually with `Mockito.mock(...)` and pass them to the constructor with `new`**. This is the standard approach in every non-permission test.

```java
@BeforeEach
public void setup() {
    msgUtil = Mockito.mock(ErrorMessageUtil.class);
    resourcePermissions = Mockito.mock(ResourcePermissions.class);
    ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
    Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);
    FF4j ff4j = Mockito.mock(FF4j.class);

    reviewer = new UrlReviewer(new ValidationUtils(), msgUtil, resourcePermissionsFactory);
}
```

- Keep a mock as a field only if a test needs to re-stub or verify it; otherwise create it as a local or inline (`Mockito.mock(ErrorMessageUtil.class)` directly in the constructor call).
- Use real instances for lightweight utilities where existing tests do (e.g. `new ValidationUtils(Mockito.mock(CertificationCriterionService.class))`, `new ListingUploadHeadingUtil(criteriaService, ff4j)`, `new ListingUploadHandlerUtil(uploadHeadingUtil, msgUtil)`).
- Do not use `@ExtendWith(MockitoExtension.class)`. `@Mock`/`@InjectMocks` are only used in the permissions tests (initialized by `ActionPermissionsBaseTest`).

### 6. Common stubbing patterns

**ErrorMessageUtil** — stub each message key the code uses so the returned text is checkable. Copy the message text from `chpl/chpl-resources/src/main/resources/errors.properties` into a `private static final String` constant using `%s` placeholders, then format it with the invocation arguments:

```java
private static final String SVAP_NOT_FOUND_AND_REMOVED =
        "Standards Version Advancement Process %s is not valid for criteria %s. It has been removed.";

Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.svap.invalidCriteriaAndRemoved"),
        ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
    .thenAnswer(i -> String.format(SVAP_NOT_FOUND_AND_REMOVED, i.getArgument(1), i.getArgument(2)));
```

For a one-argument message the codebase pads the format call: `String.format(MSG, i.getArgument(1), "")`. When the exact text doesn't matter: `Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.any(Object[].class))).thenReturn("This is an error message.");`

**Feature flags:**
```java
FF4j ff4j = Mockito.mock(FF4j.class);
Mockito.when(ff4j.check(ArgumentMatchers.eq(FeatureList.HTI_5_ERD))).thenReturn(false);
```

**DAOs / services:**
```java
Mockito.when(ucdProcessDao.getByName(ArgumentMatchers.anyString()))
    .thenReturn(UcdProcess.builder().id(1L).name("ucd 1").build());
Mockito.when(developerDAO.getById(ArgumentMatchers.anyLong())).thenThrow(new EntityRetrievalException());
```

**Static methods** (e.g. current user) — use try-with-resources inside the test:
```java
try (MockedStatic<AuthUtil> mockedAuthUtil = Mockito.mockStatic(AuthUtil.class)) {
    mockedAuthUtil.when(AuthUtil::getCurrentUser).thenReturn(getUser());
    RealWorldTestingUploadResponse response = realWorldTestingManager.uploadRealWorldTestingCsv(file);
    assertEquals("user@abc.com", response.getEmail());
}
```

**Verifying interactions** (use sparingly; most tests assert on state):
```java
Mockito.verify(svapDao, Mockito.times(1)).update(ArgumentMatchers.any(Svap.class));
```

### 7. Building test data
- Use Lombok builders, nested inline, with each chained call on its own line indented 8 spaces:

```java
CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
        .certificationResult(CertificationResult.builder()
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (a)(1)")
                        .startDay(LocalDate.parse("2023-01-01"))
                        .certificationEdition("2015")
                        .build())
                .success(true)
                .build())
        .build();
```
- Build lists with `Stream.of(...).collect(Collectors.toList())` (or `Collectors.toCollection(LinkedHashSet::new)` for sets); `new ArrayList<Foo>()` for empty lists.
- To test null-collection handling, build the object and then set the field to null explicitly (`listing.getSed().setUcdProcesses(null);`), since builders default collections to empty.
- Use `LocalDate.parse("yyyy-MM-dd")` for dates; avoid `LocalDate.now()` unless the logic is relative to today.
- Reusable test helpers already exist — use them instead of re-implementing:
  - `gov.healthit.chpl.upload.listing.ListingUploadTestUtil.getRecordsFromString(String csv)` — CSV text → `List<CSVRecord>` for upload handler tests.
  - `gov.healthit.chpl.domain.CertificationStatusProvider` — canned `CertificationStatus` objects (`ACTIVE`, `RETIRED`, `SUSPENDED_BY_ONC`, ...).
  - `gov.healthit.chpl.TestingUsers` — `setupForAdminUser/OncUser/AcbUser/CmsUser/StartupUser(resourcePermissions)`.
  - `gov.healthit.chpl.activity.history.explorer.JsonMapperUtil`.
  - Test resources in `chpl-service/src/test/resources/` (`listingDetails.json`, `upload_v19_listing*.csv`).

### 8. Assertions
- Use JUnit Jupiter static assertions: `assertEquals`, `assertTrue`, `assertFalse`, `assertNull`, `assertNotNull`, `assertThrows`, `fail`.
- Put the **expected value first**: `assertEquals(0, listing.getErrorMessages().size());`
- For listing reviewers, assert both the count and the specific message:
  ```java
  reviewer.review(listing);

  assertEquals(0, listing.getWarningMessages().size());
  assertEquals(1, listing.getErrorMessages().size());
  assertTrue(listing.getErrorMessages().contains(String.format(BAD_URL, "SVAP Notice URL '" + url + "'")));
  ```
- For `Long` ids compare with `.longValue()`: `assertEquals(1, result.get(0).getId().longValue());`
- Exceptions — use `assertThrows` with a block lambda:
  ```java
  Exception exception = assertThrows(ValidationException.class, () -> {
      realWorldTestingManager.uploadRealWorldTestingCsv(file);
  });
  assertNotNull(exception);
  ```
- Each test follows arrange → act → assert, typically separated by blank lines. Some manager tests label these with `// Setup`, `// Run`, `// Check` comments; follow that if the file already does.

### 9. Test type templates

**Listing reviewer** (`validation.listing.reviewer`, `upload.listing.validation.reviewer`): build a `CertifiedProductSearchDetails`, call `reviewer.review(listing)`, assert on `getErrorMessages()` / `getWarningMessages()` and on any data the reviewer removes. Cover: null collection, empty collection, valid value (no error), and each invalid case (error/warning with exact message).

**Upload normalizer** (`upload.listing.normalizer`): call `normalizer.normalize(listing)`, assert on fields being filled in / trimmed / nulled. Cover null input, empty input, found-by-name (fills in id), not-found paths (fuzzy match etc.).

**Upload handler** (`upload.listing.handler`): define a `HEADER_ROW_...` constant, build `CSVRecord`s with `ListingUploadTestUtil.getRecordsFromString(...)`, call `handler.handle(headingRecord, records)`, assert on the parsed result. Construct real `ListingUploadHeadingUtil` / `ListingUploadHandlerUtil` with a mocked `CertificationCriterionService` (stub `getAllowedCriterionHeadingsForNewListing()`) and mocked `FF4j`.

**Change request validation** (`changerequest.validation`): build a `ChangeRequestValidationContext` with `.newChangeRequest(...)` / `.validationDAOs(new ValidationDAOs(...))`, call `validator.isValid(context)`, assert `true`/`false`. Method names like `isValid_DeveloperExists_ReturnsTrue`.

**Manager / service** (`*.manager`, `*.service`): construct with mocked DAOs and collaborators; assert on returned objects, thrown `ValidationException`s, or `Mockito.verify` interactions. Set up a security context when the code reads the current user (see `RealWorldTestingManagerTest.setSecurityContext()`), or `Mockito.mockStatic(AuthUtil.class)`.

**Action permissions** (`permissions.domain.<area>`): this is the one place that uses `@Mock`/`@InjectMocks`. Extend `ActionPermissionsBaseTest` (which calls `MockitoAnnotations.openMocks(this)` and extends `TestingUsers`), and implement every abstract role method with both `@Override` and `@Test`:

```java
package gov.healthit.chpl.permissions.domain.accessibilityStandard;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.accessibilityStandard.CreateActionPermissions;

public class CreateActionPermissionsTest extends ActionPermissionsBaseTest {

    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private CertifiedProductDAO certifiedProductDao;

    @Mock
    private DeveloperCertificationBodyMapDAO developerCertificationBodyMapDao;

    @Mock
    private ResourcePermissionsFactory resourcePermissionsFactory;

    @InjectMocks
    private CreateActionPermissions permissions;

    @BeforeEach
    public void setup() {
        permissions = new CreateActionPermissions(resourcePermissionsFactory, certifiedProductDao, developerCertificationBodyMapDao);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        assertTrue(permissions.hasAccess());

        // Should always be false -- not used
        assertFalse(permissions.hasAccess(new Object()));
    }

    // ... hasAccess_Onc, hasAccess_Acb, hasAccess_Cms (setupForXUser), hasAccess_Anon (setupForAnonUser)
}
```

For the ACB role, use `getAllAcbForUser(...)`, `getAllDeveloperForUser(...)`, and `getCertifiedProduct(id, acbId)` from the base class to stub which ACBs/developers the user can access, and test both an allowed and a disallowed object.

### 10. Checklist before finishing
1. File is in `chpl-service/src/test/java` under the same package as the class under test, named `<ClassUnderTest>Test`.
2. `public class`, `@BeforeEach public void setup()`, `@Test public void method_condition_result()`.
3. Mocks made with `Mockito.mock(...)` and injected through the constructor (except permission tests).
4. No static Mockito imports; only JUnit `Assertions` are statically imported; no wildcard imports.
5. Error message keys stubbed with text copied from `errors.properties`, and assertions check the exact message.
6. Cover null, empty, valid, and each invalid/edge branch of the method under test.
7. 4-space indentation, no tabs, no unused imports.
8. `mvn test -pl chpl-service -am -Dtest=<NewTestClass> -Dsurefire.failIfNoSpecifiedTests=false` passes (run from `chpl/`).
