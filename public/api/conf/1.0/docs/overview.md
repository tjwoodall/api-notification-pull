Use this API to "pull" business event notifications that CDS generates in response to requests submitted using the CDS APIs.

When a declaration is submitted to a CDS API a HTTP status code 202 is returned. As the declaration is processed notifications are generated.

If a callback URL was provided when subscribing to the CDS API the notifications are pushed to that URL.

If a callback URL was not provided when subscribing to the CDS API the notifications are sent to this pull queue.

Pull notifications remain queued for 14 days after which they are deleted from the queue automatically.

## Recommended usage pattern

1. Retrieve a list of notification identifiers by using the unpulled conversation identifier endpoint `GET /notifications/conversationId/{conversationId}/unpulled`

2. To retrieve each notification iterate over the list of returned notification identifiers by calling `GET /notifications/unpulled/{notificationId}`

3. In an exception case, to retrieve a notification again call `GET /notifications/pulled/{notificationId}`

