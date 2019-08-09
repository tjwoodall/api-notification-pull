Use this API to "pull" business event notifications that CDS generates in response to requests submitted using the CDS APIs.

When a declaration is submitted to a CDS API a HTTP status code 202 is returned. As the declaration is processed notifications are generated.

If a callback URL was provided when subscribing to the CDS API the notifications are pushed to that URL.

If a callback URL was not provided when subscribing to the CDS API the notifications are sent to this pull queue.

Pull notifications remain queued for 14 days after which they are deleted from the queue automatically.

You can get all of the notifications for your application, or you can get them for a given conversation. We recommend getting them by conversation.

## Getting notifications for a given conversation (trade test only)

1. Get a list of unpulled notification identifiers for a given conversation by calling `GET /notifications/conversationId/{conversationId}/unpulled`

2. Iterate over this list of notification identifiers and get each unpulled notification from the unpulled queue by calling `GET /notifications/unpulled/{notificationId}`. When you get a notification from the unpulled queue, it will move to the pulled queue.

3. To get a notification again after you have retrieved it, get it from the pulled queue by calling `GET /notifications/pulled/{notificationId}`
