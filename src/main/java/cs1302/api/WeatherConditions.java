package cs1302.api;

/**
 * Represents a result in a response from the Weather Search API. This is
 * used by Gson to create an object from the JSON response body.
 */
public class WeatherConditions {
    String conditions;
    String icon;
    double temp;
} // WeatherConditions
