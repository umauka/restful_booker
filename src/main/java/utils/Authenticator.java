package utils;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;

public class Authenticator {
    private static String token;

    public static String getToken() {
        if (token == null) {
            generateToken();
        }
        return token;
    }

    private static void generateToken() {
        RestAssured.baseURI = "https://restful-booker.herokuapp.com/";
        Response response = given()
                .header("Content-Type", "application/json")
                .body("{ \"username\": \"admin\", \"password\": \"password123\" }")
                .when().post("auth").then().extract().response();

        token = response.jsonPath().getString("token");
        System.out.println(token);
    }
}
