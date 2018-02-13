This API provides your application with the ability to “pull” notifications from some of our other services, if those services have been unable to “push” their notifications to your application.

The other service will advise you if this service is available for you to retrieve your notifications

For example, if you submit forms or documentation to another API service, such as the Customs Declaration Service (CDS), it might accept them with a code 202 but then need to wait for further human intervention. Following that intervention, CDS now has a notification available for you to retrieve.

CDS could have tried to push this notification to your application, but if your system could not accept it then the notification is added to the notification queue. This might happen if your system is down, or your firewall rules prevent it, or you chose to use this pull method instead.

In this case, the notification is now available for you to pull from the queue using this Pull Notification API.

Once you pull your notification it is deleted automatically .
