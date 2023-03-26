# Know your friends bot

Telegram bot which helps you with storing information about what your friends like, what are their hobbies and interests in one place.

It also allows you to set reminders about their birthdays and other important dates.

>
> Note that at this moment the application is in development stage,
> thus code is not well documented and some features are not implemented yet.
>

## Usage

You can try this bot by yourself - add it to Telegram: [@KnowYourFriendsTgBot](https://t.me/KnowYourFriendsTgBot)

> Currently bot is **disabled**. It will be enabled with first stable release.


## Security

Before implementing security measures, it was important to take into account the following: 

* Interaction with bot should be simple and deprive the need to create login & password combination.
* No sensitive data is expected to be stored.
* It is very unlikely that someone will try to attack this bot.

Considering all of the above, token-based authentication mechanism was chosen for securing this application.

It is less secure than some other alternatives, but in this case it is acceptable.


## Testing

Controller and service tests can be found in [this directory](server/src/test/kotlin/com/backend/kyf).

> Tests will be automated with CI before first stable release. 