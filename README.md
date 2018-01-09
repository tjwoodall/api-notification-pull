# API Notification Pull

[![Build Status](https://travis-ci.org/hmrc/api-notification-pull.svg)](https://travis-ci.org/hmrc/api-notification-pull) [ ![Download](https://api.bintray.com/packages/hmrc/releases/api-notification-pull/images/download.svg) ](https://bintray.com/hmrc/releases/api-notification-pull/_latestVersion)

# Introduction
This API allows third party developers to collect notifications.

---

## Endpoints

### DELETE `/{notificationId}`

Retrieves and deletes a notification from `api-notification-queue`

Required Headers:
  - `X-Client-Id`
  - `Accept`

```
curl -v -X GET "http://localhost:9649/notifications" \
  -H "X-Client-ID: pHnwo74C0y4SckQUbcoL2DbFAZ0b" \
  -H "Accept: application/vnd.hmrc.1.0+xml"
```

#### Response
200 OK on success otherwise 404 Not Found

---

### Tests
There are unit tests, integration tests and code coverage reports.
In order to run them, use this command line:
```
./run_all_tests.sh
```

---

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
