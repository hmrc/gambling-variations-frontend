
# gambling-variations-frontend

This is the new gambling-variations-frontend repository

## Running the service

Service Manager: `sm2 --start DASS_GAMBLING_ALL`

To run all tests and coverage: `./run_all_tests.sh`

To start the server locally: `sbt run`

To check coverage: `sbt clean coverage test it/test coverageReport`

To enable test-only routes when running locally, start the server with: `sbt 'run -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes 10401'`

### Formatting

Scala sources and the `conf/messages.[cc]` files are kept in a canonical format, which is checked every
time the project compiles. Compiling fails when something is out of format.

To reformat everything, run:

```
sbt format
```

This applies scalafmt to `app`, `test`, `it` and the build files, and runs
[msgman](https://github.com/dboresjo/msgman) over the `conf/messages` files, sorting them into
canonical order and adding a placeholder for any translation that is missing.

msgman is not part of the standard toolchain, so the build installs it into `~/.local/bin` from its
GitHub releases the first time it is needed. An msgman that is already installed is used as it is. On
a platform with no published release, the messages check is skipped with a warning instead of failing
the build.

To avoid disappointment in your development workflow, use format in your usual edit-build-test cycle, eg:
```
sbt format compile test
```

## Adding New Pages

### Folder Structure
The project uses domain-based organisation. Each new page should be placed in the appropriate domain folder:

```
app/
├── controllers/[domain]/          # e.g., monthlyreturns/
├── models/[domain]/               # e.g., monthlyreturns/
├── views/[domain]/                # e.g., monthlyreturns/
├── forms/[domain]/                # e.g., monthlyreturns/
├── pages/[domain]/                # e.g., monthlyreturns/
└── viewmodels/checkAnswers/[domain]/
```

```
test/
├── controllers/[domain]/
├── models/[domain]/
├── forms/[domain]/
└── views/[domain]/
```

### Example: routes and messages

```routes
GET        /there-is-a-problem-with-the-service                       controllers.SystemErrorController.onPageLoad()
```

Message key (messages.en):

### Internationalization (i18n)

```properties
systemError.title = Sorry, there is a problem with the service
```
Before adding new translation keys, please check the top of the following files [messages.en](conf/messages.en) | [messages.cy](conf/messages.cy) and review existing entries to avoid duplicates.
Refer to the `DASS Welsh Translation Repository` in confluence.

A key added to [messages.en](conf/messages.en) alone leaves the Welsh file incomplete, which fails the
check on the next compile. Running `sbt format` inserts the missing key with a placeholder value
prefixed by its language code, for example `cy: Some English text`. These placeholders keep the build
green, so remember to replace them with a real translation.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").