package cs1302.api;

 /**
  * Represents a response from the Weather API. This is used by Gson to
  * create an object from the JSON response body. This class is provided with
  * project's starter code, and the instance variables are intentionally set
  * to package private visibility.
  */
public class WeatherResponse {
    int queryCost;
    float latitude;
    float longitude;
    String resolvedAddress;
    String address;
    String timezone;

    WeatherConditions currentConditions;
}
