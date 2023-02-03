package uk.ac.ed.inf;
import static uk.ac.ed.inf.OrderOutcome.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import java.net.URL;

/**
 * A class for managing orders
 */
public class Order{

    @JsonProperty("orderNo")
    public String orderNo;
    @JsonProperty("orderDate")
    public String orderDate;
    @JsonProperty("customer")
    public String customer;
    @JsonProperty("creditCardNumber")
    public String creditCardNumber;
    @JsonProperty("creditCardExpiry")
    public String creditCardExpiry;
    @JsonProperty("cvv")
    public String cvv;
    @JsonProperty("priceTotalInPence")
    public int priceTotalInPence;
    @JsonProperty("orderItems")
    public String[] orderItems;

    public Order(
            @JsonProperty("orderNo")
            String orderNo,
            @JsonProperty("orderDate")
            String orderDate,
            @JsonProperty("customer")
            String customer,
            @JsonProperty("creditCardNumber")
            String creditCardNumber,
            @JsonProperty("creditCardExpiry")
            String creditCardExpiry,
            @JsonProperty("cvv")
            String cvv,
            @JsonProperty("priceTotalInPence")
            int priceTotalInPence,
            @JsonProperty("orderItems")
            String[] orderItems)

    {
        this.orderNo = orderNo;
        this.orderDate = orderDate;
        this.customer = customer;
        this.creditCardNumber = creditCardNumber;
        this.creditCardExpiry = creditCardExpiry;
        this.cvv = cvv;
        this.priceTotalInPence = priceTotalInPence;
        this.orderItems = orderItems;
    }

    /** A method for pulling orders from the REST server
     *
     * @param serverAddress Base address of server where order data is located
     * @param date Date for which orders we want to get
     * @return Null or orders
     */
    public static Order[] getOrdersFromServer(String serverAddress, String date) {
        try {
            if (!serverAddress.endsWith("/")) {
                serverAddress = serverAddress + "/";
            }
            String url = serverAddress + "orders";
            if (date != null) {
                url += "/" + date;
            }
            return new ObjectMapper().readValue(new URL(url), Order[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * A method for finding the delivery cost of a pizza order
     * @param restaurantArray An array of participating restaurants
     * @param pizzaStrings Strings of pizza names which for the delivery cost is calculated
     * @return Sum of pizza price and delivery cost
     * @throws InvalidPizzaCombinationException if an invalid pizza combination is detected
     */
    public int getDeliveryCost(Restaurant[] restaurantArray, String... pizzaStrings)throws InvalidPizzaCombinationException{
        int price = 0;
        boolean pizzaExists;
        for (Restaurant restaurant : restaurantArray) {
            pizzaExists = false;
            for (String pizza : pizzaStrings) {
                pizzaExists = false;
                for (Menu item : restaurant.getMenu()) {
                    if (Objects.equals(item.name, pizza)) {
                        price = price + item.priceInPence;
                        pizzaExists = true;
                        break;
                    }
                }
                if (!pizzaExists) {
                    break;
                }
            }
            if (pizzaExists) {
                return price + 100;
            }
        }
        throw new InvalidPizzaCombinationException("Incorrect pizza combination");
    }

    /**
     * A method for verifying the validity of an order by conducting checks on order contents, credit
     * card information and if it's possible to deliver the order
     *
     * @param restaurants array of order restaurants
     * @return outcome of the order as a OrderOutcome object.
     */
    public OrderOutcome isOrderValid(Restaurant[] restaurants) {
        try {
            if (!this.isCreditCardNumberValid()) {
                return InvalidCardNumber;
            }
            else if (!this.checkIfPizzaNotDefined(restaurants)) {
                return InvalidPizzaNotDefined;
            }
            else if (!this.isCreditCardExpired()) {
                return InvalidExpiryDate;
            }
            else if (!this.isCVVNumberValid()) {
                return InvalidCvv;
            }
            else if (this.getDeliveryCost(restaurants, this.orderItems) != this.priceTotalInPence) {
                return InvalidTotal;
            }
            else if (!this.PizzaCount()) {
                return InvalidPizzaCount;
            }
        } catch (InvalidPizzaCombinationException e) {
            return Invalid;
        }
        return ValidButNotDelivered;
    }

    /**
     * Checks if the credit card is expired
     *
     * @return boolean indicating if the card has expired
     */
    public boolean isCreditCardExpired() {
        try {
            String expiryDate = new StringBuilder(this.creditCardExpiry)
                    .insert(0, "01/").insert(6, "20").toString();
            LocalDate convertedDate = LocalDate.parse(expiryDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            LocalDate orderDate = LocalDate.parse(this.orderDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate creditCardExpiryDate = convertedDate
                    .withDayOfMonth(convertedDate.getMonth().length(convertedDate.isLeapYear()));
            return orderDate.isBefore(creditCardExpiryDate) || orderDate.isEqual(creditCardExpiryDate);
        } catch (DateTimeException dateTimeException) {
            return false;
        }
    }

    /**
     * A method for building a JsonObject
     *
     * @param cost  cost of order in pence
     * @param orderNumber order number
     * @param orderOutcome order outcome
     * @return JsonObject with information from the parameters
     */
    public static JsonObject getOrderJson(int cost, String orderNumber, OrderOutcome orderOutcome) {
        JsonObject json = new JsonObject();
        json.addProperty("orderNo", orderNumber);
        json.addProperty("outcome", orderOutcome.toString());
        json.addProperty("costInPence", cost);
        return json;
    }

    /**
     * A method for checking if the credit card number is valid
     *
     * @return boolean showing if the credit card number is valid
     */
    public boolean isCreditCardNumberValid() {
        boolean flip = false;
        int sum = 0;
        int n;
        if (Objects.equals(this.creditCardNumber, "0000000000000000")){
            return false;
        }
        for (int i = this.creditCardNumber.length() - 1; i >= 0; i--) {
            n = Character.getNumericValue(this.creditCardNumber.charAt(i));
            if (n < 0 || n > 9){
                return false;
            }
            if (flip) {
                n = n * 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum = sum + n;
            flip = !flip;
        }
        return sum % 10 == 0;
    }

    /**
     * A method for checking if the order doesn't have too many pizzas
     *
     * @return boolean showing if there are too many pizzas
     */
    private boolean PizzaCount() {
        return this.orderItems.length <= 4;
    }

    /**
     * A method for checking if any of the restaurants offer the pizza
     *
     * @param restaurants array of restaurants
     * @return boolean showing if the pizza exists
     */
    private boolean checkIfPizzaNotDefined(Restaurant[] restaurants) {
        Set<String> pizzas = new TreeSet<>();
        for (Restaurant restaurant : restaurants) {
            for (Menu menuItem : restaurant.getMenu()) {
                pizzas.add(menuItem.name);
            }
        }
        for (String pizzaName : this.orderItems) {
            if (!pizzas.contains(pizzaName)) {
                return false;
            }
        }
        return true;
    }

    /** A method for locating a restaurant that can fulfill an order
     *
     * @param restaurants array of restaurants
     * @param pizzaList list of pizzas to be ordered
     * @return restaurant which can fulfill the order
     * @throws InvalidPizzaCombinationException exception for if the pizza combination is not valid
     */
    public Restaurant findRestaurant(Restaurant[] restaurants, String[] pizzaList)
            throws InvalidPizzaCombinationException {
        //pick a restaurant
        for (Restaurant restaurant : restaurants) {
            //pick a pizza from ordered pizzas
            for (int i = 0; i < pizzaList.length; i++) {
                boolean pizzaFound = false;
                //look for pizza in menu
                for (Menu menuItem : restaurant.getMenu()) {
                    if (Objects.equals(pizzaList[i], menuItem.name)) {
                        pizzaFound = true;
                        break;
                    }
                }
                if (i == pizzaList.length - 1) {
                    return restaurant;
                }
                else if (!pizzaFound) {
                    break;
                }
            }
        }
        throw new InvalidPizzaCombinationException("Invalid pizza combination");
    }

    /**
     * A method for writing order outcomes to a json file
     *
     * @param orders array of order outcomes as jsons
     * @param date order date
     */
    public static void writeOrderOutcomes(List<JsonObject> orders, String date) {
        try (PrintWriter out = new PrintWriter(new FileWriter("deliveries-" + date + ".json"))) {
            out.write(orders.toString());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * A method for checking if CVV number is valid
     *
     * @return boolean showing if the CVV number is valid
     */
    public boolean isCVVNumberValid() {
        if (this.cvv == null || this.cvv.length() != 3) {
            return false;
        }
        for(int i = 0; i < 3; i++){
            int number = Character.getNumericValue(this.cvv.charAt(i));
            if (number > 9 || number < 0){
                return false;
            }
        }
        return true;
    }
}
