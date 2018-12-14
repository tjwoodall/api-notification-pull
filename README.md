# Pull Notifications API

# Introduction
This API allows third party developers to collect notifications.

---

## Endpoints

### GET `/notifications/unread/{notificationId}`

Read an unread notification

Required Headers:
  - `X-Client-ID`
  - `Accept`

```
curl -v -X GET "http://localhost:9649/notifications/unread/{notificationId}" \
  -H "X-Client-ID: 580e3940-fb35-4421-b7c7-949f64a97870" \
  -H "Accept: application/vnd.hmrc.1.0+xml"
```

#### Responses
##### Success
```
200 OK
 Notification
```

##### Bad Request
 ```
400 Bad Request
    <errorResponse>
        <code>BAD_REQUEST</code>
        <message>Notification has been read</message>
    </errorResponse>
```

##### Not Found
```
404 Not Found
    <errorResponse>
        <code>NOT_FOUND</code>
        <message>Resource was not found</message>
    </errorResponse>
```

### GET `/notifications/read/{notificationId}`

Re-reads a notification

Required Headers:
  - `X-Client-ID`
  - `Accept`

```
curl -v -X GET "http://localhost:9649/notifications/read/{notificationId}" \
  -H "X-Client-ID: 580e3940-fb35-4421-b7c7-949f64a97870" \
  -H "Accept: application/vnd.hmrc.1.0+xml"
```

#### Responses
##### Success
```
200 OK

Notification 
```

##### Bad Request

```
400 Bad Request

    <errorResponse>
        <code>BAD_REQUEST</code>
        <message>Notification is unread</message>
    </errorResponse>
```

##### Not Found

```
404 Not Found

    <errorResponse>
        <code>NOT_FOUND</code>
        <message>Resource was not found</message>
    </errorResponse>
```

### DELETE `/{notificationId}`

Retrieves and deletes a notification from `api-notification-queue`

Required Headers:
  - `X-Client-ID`
  - `Accept`

```
curl -v -X DELETE "http://localhost:9649/{notificationId}" \
  -H "X-Client-ID: 580e3940-fb35-4421-b7c7-949f64a97870" \
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
  -H "X-Client-ID: 580e3940-fb35-4421-b7c7-949f64a97870" \
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
There are unit and component tests along with code coverage reports.
In order to run them, execute this command line:
```
./precheck.sh
```

---

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
