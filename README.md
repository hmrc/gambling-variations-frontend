
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

To enable test-only routes when running locally, start the server with: `sbt 'run -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes 10402'`

## Testing agent access locally

Agents are checked against their client list (`hasClient`) on every page.

Stubbed: by default the gambling backend uses `gambling-external-stub` (port 10405), so agent checks pass for any client. Sign in through auth-login-stub as an `Agent` with an `HMRC-MGD-AGNT` enrolment, redirecting to the landing URL `http://localhost:10402/gambling-variations/{regNumber}/change-registration-details`.

Real (local Oracle) agent checks:

1. Point the gambling backend at the local `rds-datacache-proxy` on port **6992**
2. Pick a client MGD reg number that exists in `MGD_DATA.MGD_OPERATOR_DETAILS`
3. Seed the delegated `HMRC-MGD-ORG` enrolment for the agent's credential in `enrolment-store-stub`
4. Sign in through auth-login-stub as an `Agent` with the same CredID and an `HMRC-MGD-AGNT` enrolment, redirecting to the landing URL above

For the full steps, sample requests and troubleshooting, see the `Agent Auth — Local Testing (Gambling / MGD & GTR)` page in Confluence.

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

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").