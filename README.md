# Pull Notifications API

[![Build Status](https://travis-ci.org/hmrc/api-notification-pull.svg)](https://travis-ci.org/hmrc/api-notification-pull) [ ![Download](https://api.bintray.com/packages/hmrc/releases/api-notification-pull/images/download.svg) ](https://bintray.com/hmrc/releases/api-notification-pull/_latestVersion)

# Introduction
This API allows third party developers to collect notifications.

---

## Endpoints

### DELETE `/{notificationId}`

Retrieves and deletes a notification from `api-notification-queue`

Required Headers:
  - `X-Client-ID`
  - `Accept`

```
curl -v -X DELETE "http://localhost:9649/{notificationId}" \
  -H "X-Client-ID: pHnwo74C0y4SckQUbcoL2DbFAZ0b" \
  -H "Accept: application/vnd.hmrc.1.0+xml"
```

#### Responses

##### Success
```
200 OK

Notification
```

##### Not Found

`404 Not Found`

### GET `/`

Retrieves all notifications, for a specific client id, from `api-notification-queue`

Required Headers:
  - `X-Client-ID`
  - `Accept`

```
curl -v -X GET "http://localhost:9649/" \
  -H "X-Client-ID: pHnwo74C0y4SckQUbcoL2DbFAZ0b" \
  -H "Accept: application/vnd.hmrc.1.0+xml"
```

#### Response
```
200 OK

<resource href="/notifications/">
   <link rel="self" href="/notifications/"/>
   <link rel="notification" href="/notifications/7ab99957-b138-4f09-888e-ab4e8107bbe0"/>
</resource>
```

---

### Tests
There are unit, integration component tests and code coverage reports.
In order to run them, use this command line:
```
./precheck.sh
```

---

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
