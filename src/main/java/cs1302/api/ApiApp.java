package cs1302.api;

import java.io.IOException;
import java.lang.ArrayIndexOutOfBoundsException;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import javafx.event.EventHandler;
import javafx.event.ActionEvent;

/**
 * REPLACE WITH NON-SHOUTING DESCRIPTION OF YOUR APP.
 */
public class ApiApp extends Application {
    /** HTTP client. */
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)           // uses HTTP protocol version 2 where possible
        .followRedirects(HttpClient.Redirect.NORMAL)  // always redirects, except from HTTPS to HTTP
        .build();                                     // builds and returns a HttpClient object
    /** Google {@code Gson} object for parsing JSON-formatted strings. */
    public static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()                          // enable nice output when printing
        .create();                                    // builds and returns a Gson object
    private static final String WEATHER_API = "https://weather.visualcrossing.com" +
        "/VisualCrossingWebServices/rest/services/timeline/";
    private static final String WEATHER_API_KEY = "LWMY8BLH3HD9C7U96UDWU6HEV";
    private static final String SUN_TIMES_API = "https://api.sunrise-sunset.org/json?";
    private Stage stage;
    private Scene scene;
    private VBox root;
    // search hbox
    private HBox searchBox;
    private Text searchText;
    private TextField cityField;
    private Button loadButton;
    // instructions hbox
    private HBox instructionsBox;
    private Text instructionsText;
    // weather and update hbox
    private VBox textBox;
    private Text weatherText;
    // sun times hbox
    private HBox placeBox;
    private HBox p1;
    private HBox p2;
    private Image defaultImage = new Image("file:resources/placeImg.png");
    private Image weatherImage = new Image("file:resources/weatherBanner.jpg");
    private ImageView weatherView = new ImageView();
    private ImageView iv1 = new ImageView();
    private ImageView iv2 = new ImageView();
    private Text s1 = new Text("Sunrise Time:    ");
    private TextFlow tF1 = new TextFlow(s1);
    private Text s2 = new Text("Sunset Time:     ");
    private TextFlow tF2 = new TextFlow(s2);
    private static double temp = 0;
    private static float lati = 0;
    private static float longi = 0;
    private static String sunRiseTime;
    private static String sunSetTime;
    // footer hbox
    private HBox footerBox;
    private Text footerText;

    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        this.stage = null;
        this.scene = null;
        this.root = new VBox();
        this.textBox = new VBox();
        this.instructionsBox = new HBox();
        this.placeBox = new HBox();
        this.searchBox = new HBox();
        this.p1 = new HBox();
        this.p2 = new HBox();
        this.stage = stage;
        this.iv1.setImage(defaultImage);
        this.iv1.setFitHeight(100);
        this.iv1.setFitWidth(100);
        this.iv2.setImage(defaultImage);
        this.iv2.setFitHeight(100);
        this.iv2.setFitWidth(100);
        this.p1.getChildren().addAll(iv1, tF1);
        this.p2.getChildren().addAll(iv2, tF2);
        this.searchText = new Text(" Search:   ");
        this.instructionsText = new Text("Enter the city and state/country, and click enter.\n" +
            "Loads the latitude, longitude, temperature, and current weather conditions.\n" +
            "Based on The info, lat, long, the sunrise and sunset time is displayed.");
        this.cityField = new TextField("Athens GA");
        this.cityField.setMinWidth(388);
        this.loadButton = new Button("Enter");
        this.weatherView.setImage(weatherImage);
        this.weatherView.setFitHeight(110);
        this.weatherView.setFitWidth(500);
        this.weatherText = new Text("location =\n" +
            "conditions =\n" + "temperature =\n" + "latitude =\n" + "longitude =\n");
        this.footerBox = new HBox();
        this.footerText = new Text("                                      " +
            "Data Provided By:  weatherapi & sunrise-sunset-api ");
    } // ApiApp

    /** {@inheritDoc} */
    @Override
    public void init() {
        System.out.println("- init");
        this.searchBox.getChildren().addAll(this.searchText, this.cityField, this.loadButton);
        this.instructionsBox.getChildren().addAll(this.instructionsText);
        this.textBox.getChildren().addAll(this.weatherView, this.weatherText);
        this.placeBox.getChildren().addAll(this.p1, this.p2);
        this.footerBox.getChildren().addAll(this.footerText);
        this.root.getChildren().addAll(this.searchBox, this.instructionsBox,
            this.textBox, this.placeBox, this.footerBox);
        this.loadButton.setOnAction((e) -> {
            this.loadWeatherAndPlace();
        });
    } // init

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {
        System.out.println("- start");
        this.stage = stage;
        this.scene = new Scene(root);
        this.stage.setTitle("ApiApp!");
        this.stage.setScene(scene);
        this.stage.setOnCloseRequest(event -> Platform.exit());
        this.stage.sizeToScene();
        this.stage.show();
    } // start

    /** {@inheritDoc} */
    @Override
    public void stop() {
        System.out.println("- stop");
    } // stop

    /** method to load the weather and sunset times. */
    public void loadWeatherAndPlace() {
        this.loadWeather();
        this.loadSunTime();
    } // loadWeatherAndSong

    /** method to load the weather. */
    public void loadWeather() {
        try {
            String location = URLEncoder.encode(cityField.getText(), StandardCharsets.UTF_8);
            //String query = String.format("loaction%20", location);
            String endingQuery = "?unitGroup=metric&include=current&key=" +
                WEATHER_API_KEY + "&contentType=json";
            String uri = WEATHER_API + location + endingQuery;
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .build();
            HttpResponse<String> response = HTTP_CLIENT
                .send(request, BodyHandlers.ofString());
            if (response == null) {
                throw new IOException("Unable to retreive.");
            } // if
            String jsonString = response.body();
            WeatherResponse weatherResponse = GSON
                .fromJson(jsonString, cs1302.api.WeatherResponse.class);
            this.temp = weatherResponse.currentConditions.temp;
            this.lati = weatherResponse.latitude;
            this.longi = weatherResponse.longitude;
            this.weatherText.setText("location = " + weatherResponse.resolvedAddress +
                "\nconditions = " + weatherResponse.currentConditions.conditions +
                "\ntemperature = " + temp +
                "\nlatitude = " + lati +
                "\nlongitude = " + longi + "\n");
        } catch (IOException | InterruptedException e) {
            System.err.println(e);
            e.printStackTrace();
        } // try
    } // load Weather

    /** Method to load the sunrise/sunset times. */
    public void loadSunTime() {
        try {
            String query = String.format("lat=%s&long=%s", lati, longi);
            String uri = SUN_TIMES_API + query;
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .build();
            HttpResponse<String> response = HTTP_CLIENT
                .send(request, BodyHandlers.ofString());
            String jsonString = response.body();
            SunTimeResponse sunTimeResponse = GSON
                .fromJson(jsonString, cs1302.api.SunTimeResponse.class);
            if (sunTimeResponse.status.equals("OK")) {
                this.sunRiseTime = sunTimeResponse.results.sunrise;
                this.sunSetTime = sunTimeResponse.results.sunset;
                this.s1.setText("Sunrise:       \n" + sunRiseTime);
                this.s2.setText("Sunset:        \n" + sunSetTime);
            } else {
                throw new IOException("Sunset/Sunrise time was not fetched");
            }
        } catch (IOException | InterruptedException e) {
            System.err.println(e);
            e.printStackTrace();
        } // try
    } // loadSunTime

} // ApiApp
