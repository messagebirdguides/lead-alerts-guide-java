# Instant Lead Alerts

### ⏱ 15 min build time

## Why build instant lead alerts for Sales?

Even though a lot of business transactions happen on the web, from both a business and user perspective, it's still often preferred to switch the channel and talk on the phone. Especially when it comes to high-value transactions in industries such as real estate or mobility, personal contact is essential.

One way to streamline this workflow is by building callback forms onto your website. Through these forms, customers can enter their contact details and receive a call to their phone, thus skipping queues where prospective leads need to stay on hold.

Callback requests reflect a high level of purchase intent and should be dealt with as soon as possible to increase the chance of converting a lead. Therefore it's paramount to get them pushed to a sales agent as quickly as possible. SMS messaging has proven to be one of the most instant and effective channels for this use case.

In this MessageBird Developer Guide, we'll show you how to implement a callback form on a Java-based website with SMS integration powered by MessageBird for our fictitious car dealership, M.B. Cars.

## Getting Started

To run the application, you will need [Java 1.8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) and [Maven](https://maven.apache.org/).

The source code is available in the [MessageBird Developer Guides GitHub repository](https://github.com/messagebirdguides/lead-alerts-guide-java) from which it can be cloned or downloaded into your development environment.

After saving the code, import the project into your IDE. Maven should automatically download all the dependencies necessary.

## Configuring the MessageBird SDK

The MessageBird SDK is used to send messages. It's added as a dependency and loaded with the following lines in `LeadAlerts.java`:

``` java
// Create a MessageBirdService
final MessageBirdService messageBirdService = new MessageBirdServiceImpl(dotenv.get("MESSAGEBIRD_API_KEY"));
// Add the service to the client
final MessageBirdClient messageBirdClient = new MessageBirdClient(messageBirdService);
```

You need an API key, which you can retrieve from [the MessageBird dashboard](https://dashboard.messagebird.com/en/developers/access). As you can see in the code example above, the key is set as a parameter when including the SDK, and it's loaded from an environment variable called `MESSAGEBIRD_API_KEY`. With [dotenv](https://mvnrepository.com/artifact/io.github.cdimascio/java-dotenv) you can define these variables in a `.env` file.

The repository contains an `.env.example` file which you can copy to `.env` and then enter your information.

Apart from the API key, we also specify the originator, which is what is displayed as the sender of the messages. Please note that alphanumeric sender IDs like the one in our example file don't work in all countries, most importantly, they don't work in the United States. If you can't use alphanumeric IDs, use a real phone number instead.

Additionally, we specify the sales agent's telephone numbers. These are the recipients that will receive the SMS alerts when a potential customer submits the callback form. You can separate multiple numbers with commas.

Here's an example of a valid `.env` file for our sample application:

```
MESSAGEBIRD_API_KEY=YOUR-API-KEY
MESSAGEBIRD_ORIGINATOR=Mbcars
SALES_AGENT_NUMBERS=+31970XXXXXXX,+31970YYYYYYY
```

## Showing a Landing Page

The landing page is a simple HTML page with information about our company, a call to action and a form with two input fields, name and number, and a submit button. We use Handlebars templates so we can compose the view with a layout and have the ability to show dynamic content. You can see the landing page in the file `resources/templates/landing.mustache`, which extends the layouts stored in `resources/templates/header.mustache` and `resources/templates/footer.mustache`. The `get '/'` route in `LeadAlerts.java` is responsible for rendering it.

## Handling Requests

When the user submits the form, the `post '/callme'` route receives their name and number. First, we do some input validation:

``` java
// Handle request
post("/callme", (req, res) ->
 {
     // Read request
     String name = req.queryParams("name");
     String number = req.queryParams("number");
 
     Map<String, Object> model = new HashMap<>();
 
     if (name.isBlank() || number.isBlank()) {
         model.put("errors", "Please fill all required fields!");
         model.put("name", name);
         model.put("number", number);
 
         return new ModelAndView(model, "landing.mustache");
     }
```

Then, we define where to send the message. As you've seen above, we specified multiple recipient numbers in the `SALES_AGENT_NUMBERS` environment variable. M.B. Cars have decided to randomly distribute incoming calls to their staff so that every salesperson receives roughly the same amount of leads. Here's the code for the random distribution:

``` java
// Choose one of the sales agent numbers randomly
// a) Convert comma-separated values to array
List<String> numbers = Arrays.asList(dotenv.get("SALES_AGENT_NUMBERS").split("\\s*,\\s*"));
// b) Random number between 0 and (number count - 1)
int randomNum = ThreadLocalRandom.current().nextInt(0, numbers.size());
// c) Pick number
String recipient = numbers.get(randomNum);
```

Now we can formulate a message for the agent and send it through the MessageBird SDK using the `messageBirdClient.sendMessage` method:

``` java
// Send lead message with MessageBird API
final MessageResponse response = messageBirdClient.sendMessage(dotenv.get("MESSAGEBIRD_ORIGINATOR"), body, phones);
```

The arguments are as follows:

* `originator`: This is the first parameter. It represents a sender ID for the SMS. The sender ID comes from the environment variable defined earlier.
* `body`: This is the second parameter. The text of the message that includes the input from the form.
* `recipients`: This is the third parameter. It's an array of one or more phone numbers to send the message to.

Inside this function, we handle the error case by showing the previous form again and informing the user that something went wrong. In the success case, we show a basic confirmation page which you can see in `templates/sent.mustache`. This is how the response is handled:

``` java
try {
    // Send lead message with MessageBird API
    final MessageResponse response = messageBirdClient.sendMessage(dotenv.get("MESSAGEBIRD_ORIGINATOR"), body, phones);

    return new ModelAndView(model, "sent.mustache");
} catch (UnauthorizedException | GeneralException ex) {
    model.put("errors", ex.toString());
    return new ModelAndView(model, "sent.mustache");
}
```

## Testing the Application

Have you created your `.env` file with a working key and added one more phone number to the existing phone numbers, as explained above in _Configuring the MessageBird SDK_, to receive the lead alert? Awesome!

Now, build and run the application through your IDE.

Go to http://localhost:4567/ to see the form and request a lead!

## Nice work!

You've just built your own instant lead alerts application with MessageBird!

You can now use the flow, code snippets and UI examples from this tutorial as an inspiration to build your own SMS-based lead alerts application. Don't forget to download the code from the [MessageBird Developer Guides GitHub repository](https://github.com/messagebirdguides/lead-alerts-guide-java).

## Next steps

Want to build something similar but not quite sure how to get started? Please feel free to let us know at support@messagebird.com, we'd love to help!
