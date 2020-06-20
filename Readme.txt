
Shaharna13

313586877

"I pledge the highest level of ethical principles in support of academic excellence.
I ensure that all of my work reflects my own abilities and not those of someone else."

Currently, every time we send an SMS we also show notification "sending sms: .....".
What should we add in our code-base so that when the SMS will get delivered, this notification's text will be changed to "sms sent: ......"?

[hint: in android's SmsManager you can supply a PendingIntent for delivery. this pending intent could start a broadcast or a service that will have some logic. what will you need to add for the intent's extras so the new service/broadcast-receiver can update the notification's message?]

As we seen in TA 7 each notification is sent with an ID (the first value sent to notify function) wich allows us to later call again to the notify function with the same id and different notification object (the second attribute sent to notify) and it will edit the first notification rather then popping a new one.