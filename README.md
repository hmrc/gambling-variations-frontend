
# gambling-variations-frontend

This is the new gambling-variations-frontend repository

## Git Hooks

This project includes a pre-push hook that checks code formatting with scalafmt before pushing.

To activate it, run once after cloning:

If the check fails, format your code with `sbt scalafmtAll` and try again.

## Running the service

Service Manager: `sm2 --start DASS_GAMBLING_ALL`

To run all tests and coverage: `./run_all_tests.sh`

To start the server locally: `sbt run`

To check coverage: `sbt clean coverage test it/test coverageReport`

To enable test-only routes when running locally, start the server with: `sbt 'run -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes 10401'`

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

```properties
systemError.title = Sorry, there is a problem with the service
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").