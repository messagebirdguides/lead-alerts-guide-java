import com.messagebird.MessageBirdClient;
import com.messagebird.MessageBirdService;
import com.messagebird.MessageBirdServiceImpl;
import com.messagebird.exceptions.GeneralException;
import com.messagebird.exceptions.UnauthorizedException;
import com.messagebird.objects.MessageResponse;
import io.github.cdimascio.dotenv.Dotenv;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static spark.Spark.get;
import static spark.Spark.post;

public class LeadAlerts {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();

        // Create a MessageBirdService
        final MessageBirdService messageBirdService = new MessageBirdServiceImpl(dotenv.get("MESSAGEBIRD_API_KEY"));
        // Add the service to the client
        final MessageBirdClient messageBirdClient = new MessageBirdClient(messageBirdService);

        get("/",
                (req, res) ->
                {
                    return new ModelAndView(null, "landing.mustache");
                },

                new MustacheTemplateEngine()
        );

        // Handle request
        post("/callme",
                (req, res) ->
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

                    // Choose one of the sales agent numbers randomly
                    // a) Convert comma-separated values to array
                    List<String> numbers = Arrays.asList(dotenv.get("SALES_AGENT_NUMBERS").split("\\s*,\\s*"));
                    // b) Random number between 0 and (number count - 1)
                    int randomNum = ThreadLocalRandom.current().nextInt(0, numbers.size());
                    // c) Pick number
                    String recipient = numbers.get(randomNum);

                    // convert String number into acceptable format
                    BigInteger phoneNumber = new BigInteger(recipient);
                    final List<BigInteger> phones = new ArrayList<BigInteger>();
                    phones.add(phoneNumber);

                    String body = String.format("You have a new lead: %s. Call them at %s", name, number);

                    try {
                        // Send lead message with MessageBird API
                        final MessageResponse response = messageBirdClient.sendMessage(dotenv.get("MESSAGEBIRD_ORIGINATOR"), body, phones);

                        return new ModelAndView(model, "sent.mustache");
                    } catch (UnauthorizedException | GeneralException ex) {
                        model.put("errors", ex.toString());
                        return new ModelAndView(model, "sent.mustache");
                    }
                },
                new MustacheTemplateEngine()
        );
    }
}