This API enables your application to “pull” notifications from some of our services, if they couldn’t “push” their notifications to it. The other service advises you if this service is available for your application.

For example, if you submit a form to an API, such as the Customs Declaration Service (CDS), it accepts it with an HTTP status code 202 but then waits for further human intervention. After this intervention happens, CDS has a new notification for your application.

CDS tries to push this notification to your application, but if this fails then they add this notification to the notification queue. Failure can happen if your system is down, or your firewall rules prevent it, or you choose to use this pull method instead.

In this case, your application can now pull the notification from the queue using this Pull Notifications API.

Once you pull your notification it is deleted automatically.
