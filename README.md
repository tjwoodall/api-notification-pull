# Pull Notifications API

The Pull Notifications API is used by external client applications to "pull" business event notifications that CDS generates in response to requests submitted using the CDS APIs.
For more information about the API, see the [API documentation](https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/api-notification-pull).

## Development Setup
- Run locally: `sbt run` which runs on port `9649` by default
- Run with test endpoints: `sbt 'run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes'`

##  Service Manager Profiles
The API Notification Pull service can be run locally from Service Manager, using the following profiles:


| Profile Details                       | Command                                                           | Description                                                    |
|---------------------------------------|:------------------------------------------------------------------|----------------------------------------------------------------|
| CUSTOMS_DECLARATION_ALL               | sm2 --start CUSTOMS_DECLARATION_ALL                               | To run all CDS applications.                                   |
| CUSTOMS_INVENTORY_LINKING_EXPORTS_ALL | sm2 --start CUSTOMS_INVENTORY_LINKING_EXPORTS_ALL                 | To run all CDS Inventory Linking Exports related applications. |
| CUSTOMS_INVENTORY_LINKING_IMPORTS_ALL | sm2 --start CUSTOMS_INVENTORY_LINKING_IMPORTS_ALL                 | To run all CDS Inventory Linking Imports related applications. |


## Run Tests
- Run Unit Tests: `sbt test`
- Run Integration Tests: `sbt IntegrationTest/test`
- Run Unit and Integration Tests: `sbt test IntegrationTest/test`
- Run Unit and Integration Tests with coverage report: `./run_all_tests.sh`<br/> which runs `clean scalastyle coverage test it:test coverageReport dependencyUpdates"`

### Acceptance Tests
To run the CDS acceptance tests, see [here](https://github.com/hmrc/customs-automation-test).

### Performance Tests
To run performance tests, see [here](https://github.com/hmrc/api-notification-pull-performance-test).


## API documentation
For Notification Pull API documentation, see [here](https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/api-notification-pull/1.0/oas/page).


### API Notification Pull specific routes
| Path - internal routes prefixed by `/notifications` | Supported Methods | Description                                                                           |
|-----------------------------------------------------|:-----------------:|---------------------------------------------------------------------------------------|
| `/:notificationId`                                  |      DELETE       | Retrieves and deletes a notification from `api-notification-queue`.                   |
| `/`                                                 |        GET        | Retrieves all notifications, for a specific client id, from `api-notification-queue`. |
| `/unpulled/:notificationId`                         |        GET        | Get an unpulled notification by notification ID.                                      |
| `/unpulled`                                         |        GET        | Get a list of unpulled notifications.                                                 |
| `/pulled/:notificationId`                           |        GET        | Get a pulled notification by notification ID.                                         |
| `/pulled`                                           |        GET        | Get a list of pulled notifications.                                                   |
| `/conversationId/:conversationId`                   |        GET        | Get a list of notifications by conversation ID.                                       |
| `/conversationId/:conversationId/unpulled`          |        GET        | Get a list of unpulled notifications by conversation ID.                              |
| `/conversationId/:conversationId/pulled`            |        GET        | Get a list of pulled notifications by conversation ID.                                |


### Test-only specific routes
This service does not have any specific test-only endpoints.


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
