Use this API to "pull" business event notifications that CDS generates in response to requests submitted using the CDS APIs.

When a declaration is submitted to a CDS API a HTTP status code 202 is returned. As the declaration is processed notifications are generated.

If a callback URL was provided when subscribing to the CDS API the notifications are pushed to that URL.

If a callback URL was not provided when subscribing to the CDS API the notifications are sent to this pull queue.

Pull notifications remain queued for 14 days after which they are deleted from the queue automatically.

The Pull Notifications API works in 2 discrete modes. It is recommended that your application use the Pull Notifications API as described under Retrieve pull notifications.

## Retrieve pull notifications

Use these 2 endpoints to pull notifications have not been pulled yet:

* `GET /notifications/unpulled` returns a list of identifiers for notifications that have not been pulled previously 
* `GET /notifications/unpulled/{notificationId}` returns a notification that has not pulled previously

To retrieve previously pulled notifications use these 2 endpoints. This effectively acts as a backup:

* `GET /notifications/pulled` returns a list of identifiers for notifications that have been pulled previously 
* `GET /notifications/pulled/{notificationId}` returns a notification that has been pulled previously


## Retrieve and delete pull notifications (deprecated)

To delete notifications as they are pulled use these 2 endpoints:
    
* `GET /notifications`  returns a list of available notification identifiers
* `DELETE /notifications/{Id}` pull and delete the requested notification

