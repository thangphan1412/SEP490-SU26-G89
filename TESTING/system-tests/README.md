# Report 5.3 automated system tests

This Playwright project maps the `ContractE2ELifecycle` sheet in
`Report5.3_System_Test_SEP490-SU26-G89_v5.xlsx` to executable browser and API
system tests.

## Run from the IDE or terminal

1. Start Docker Desktop.
2. Start the application: `docker compose -f deploy/docker-compose.yml up -d --build`.
3. In `system-tests`, run `npm install` and `npx playwright install chromium` once.
4. Copy `.env.example` to `.env` and enter credentials/IDs from the dedicated
   system-test database.
5. Run `npm run test:smoke` first, then `npm run test:report53`.
6. Open the HTML evidence with `npm run report`.

State-changing cases are skipped unless `E2E_ALLOW_DESTRUCTIVE=true`. Use that
flag only with a disposable test database. Playwright automatically records a
trace on retry and keeps screenshots/videos for failures.

## Traceability

| Report 5.3 case | Automated check |
| --- | --- |
| E2E-01 | Authenticated UI modules plus final project/contract/PDF/dashboard state |
| ALT-01 | Employee receives a 4xx denial and the project page shows an access error |
| ALT-02 | Duplicate email returns 400 and user creation stops |
| ALT-03 | Deleting an unfinished disposable project makes it inaccessible |
| ALT-04 | Referenced contract type deletion is rejected; contract remains |
| ALT-05 | Rejection cancels the workflow and removes the SIGN action |
| ALT-06 | Cancellation changes status and reduces pending-signature count |
| ALT-07 | Invalid signing key fails without changing contract status |
| ALT-08 | A successful OTP reset consumes the OTP; reuse returns 400 |
| ALT-09 | Dashboard derives Active, Cancelled and Pending from separate statuses |
| ALT-10 | A future-dated Active contract is not ended early |

The integration suite in
`TESTING/backend-integration/src/test/java/com/fpt/backend/it` provides its own
SQL Server and Redis through Testcontainers. It covers login/JWT, invalid
credentials, role authorization, duplicate email, and one-time OTP behavior.
