package services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.Assert;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import utils.Authenticator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static io.restassured.RestAssured.given;

public class RestfulBookerTest {
    private ExtentReports extent;
    private ExtentTest test;
    private String baseUrl;
    private JsonObject bookingBody;
    private String bookingEndpoint;
    private static String bookingId;
    private JsonObject updateBookingBody;
//    private static String firstName;

    @BeforeClass
    public void setup() {
        // Setup ExtentReports
        ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter("extent.html");
        htmlReporter.config().setTheme(Theme.STANDARD);
        htmlReporter.config().setDocumentTitle("Restful Booker API Test Report");
        htmlReporter.config().setReportName("Restful Booker API Test Report");
        extent = new ExtentReports();
        extent.attachReporter(htmlReporter);
    }

    @AfterClass
    public void teardown() {
        // Write everything to the log file
        extent.flush();
    }

    @BeforeTest
    public void init() throws IOException {
    String path = "src/main/resources/data/testData.json";
    JsonObject config = new Gson().fromJson(new String(Files.readAllBytes(Paths.get(path))), JsonObject.class);
        baseUrl = (String) config.get("url").getAsString();
        bookingBody = config.get("booking").getAsJsonObject();
        bookingEndpoint = (String) config.get("bookingEndpoint").getAsString();
        updateBookingBody = config.get("updateBooking").getAsJsonObject();
    }
    @Test
    public void testCreateBookingEndpoint() {

        test = extent.createTest("testCreateBookingEndpoint", "Testing POST /" + bookingEndpoint);
        RestAssured.baseURI = baseUrl;
        Response response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(bookingBody)
                .when()
                .post(bookingEndpoint)
                .then()
                .extract().response();

        test.info("Url: " + baseUrl);
        test.info("Response: " + response.asString());
        int statusCode = response.getStatusCode();
        bookingId = response.jsonPath().getString("bookingid");
        test.info("Status Code: " + statusCode);

        try {
            Assert.assertEquals(statusCode, 200);
            test.pass("Test Passed");
        } catch (AssertionError err) {
            test.fail("Test Failed: " + err.getMessage());
        }
    }

    @Test(dependsOnMethods = "testCreateBookingEndpoint")
    public void testGetBookingEndpoint(){
        test = extent.createTest("testGetBookingEndpoint", "Testing GET /" + bookingEndpoint + "/:id - Using the booking id from the previous test.");
        RestAssured.baseURI = baseUrl;
        Response response = given()
//                .header("Authorization", "Bearer " + Authenticator.getToken())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(bookingBody)
                .when()
                .get(bookingEndpoint +"/"+bookingId)
                .then()
                .extract().response();

        test.info("Url: " + baseUrl);
        test.info("Response: " + response.asString());
        int statusCode = response.getStatusCode();
        test.info("Status Code: " + statusCode);

        try {
            Assert.assertEquals(statusCode, 200);
            test.pass("Test Passed");
        } catch (AssertionError err) {
            test.fail("Test Failed: " + err.getMessage());
        }
    }

    @Test(dependsOnMethods = "testCreateBookingEndpoint")
    public void testUpdateBookingEndpoint(){
        test = extent.createTest("testUpdateBookingEndpoint", "Testing PUT /" + bookingEndpoint + "/:id - Using the booking id from the previous test.");
        RestAssured.baseURI = baseUrl;
        Response response = given()
                .header("Authorization", "Basic " + Authenticator.getToken())
                .header("Cookie", "token=" + Authenticator.getToken())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(updateBookingBody)
                .when()
                .put(bookingEndpoint +"/"+bookingId)
                .then()
                .extract().response();

        test.info("Url: " + baseUrl);
        test.info("Response: " + response.asString());
        int statusCode = response.getStatusCode();
        String firstName = response.jsonPath().getString("firstname");
        test.info("Status Code: " + statusCode);

        try {
            Assert.assertEquals(statusCode, 200);
            Assert.assertEquals(firstName, "Uma");
            test.pass("Test Passed");
        } catch (AssertionError err) {
            test.fail("Test Failed: " + err.getMessage());
        }
    }
}
