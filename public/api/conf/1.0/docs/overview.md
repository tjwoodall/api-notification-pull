Use this API to "pull" business event notifications that CDS generates in response to requests submitted using the CDS APIs.

When a declaration is submitted to a CDS API a HTTP status code 202 is returned. As the declaration is processed notifications are generated.

If a callback URL was provided when subscribing to the CDS API the notifications are pushed to that URL.

If a callback URL was not provided when subscribing to the CDS API the notifications are sent to this pull queue.

Pull notifications remain queued for 14 days after which they are deleted from the queue automatically.

The Pull Notifications API works in 2 discrete modes. It is recommended that your application use the Pull Notifications API as described in the section below.

## Recommended usage pattern

1. Retrieve a list of notification identifiers by using the unpulled conversation identifier endpoint `GET /notifications/conversationId/{conversationId}/unpulled`

2. To retrieve each notification iterate over the list of returned notification identifiers by calling `GET /notifications/unpulled/{notificationId}`

3. In an exception case, to retrieve a notification again call `GET /notifications/pulled/{notificationId}`

## Retrieve notifications by conversation identifier

* `GET /notifications/conversationId/{conversationId}/unpulled` returns a list of unpulled notification identifiers for the specified conversation identifier
* `GET /notifications/conversationId/{conversationId}/pulled` returns a list of pulled notification identifiers for the specified conversation identifier
* `GET /notifications/conversationId/{conversationId}` returns a list of pulled and unpulled notification identifiers for the specified conversation identifier

## Retrieve individual notifications

* `GET /notifications/unpulled/{notificationId}` returns a notification that has not been pulled previously
* `GET /notifications/pulled/{notificationId}` returns a notification that has been pulled previously (this acts as a temporary backup, currently 14 days)

## Retrieve notifications by application

* `GET /notifications/unpulled` returns a list of unpulled notification identifiers for the subscribed application
* `GET /notifications/pulled` returns a list of pulled notification identifiers for the subscribed application

## Retrieve and delete notifications (deprecated)

To delete notifications as they are pulled use these 2 endpoints:
    
* `GET /notifications`  returns a list of available notification identifiers
* `DELETE /notifications/{Id}` pull and delete the requested notification

