package ayaya.core.utils;

import ayaya.core.exceptions.http.HttpNullResponseException;
import ayaya.core.exceptions.http.HttpResponseFailedException;
import ayaya.core.exceptions.http.MissingHeaderInfoException;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * Class with static methods to retrieve objects from the internet.
 */
public class HTTP {

    /**
     * The encoding for the get requests.
     */
    private static final String ENCODING = "UTF-8";

    /**
     * The client to handle the get requests.
     */
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS).build();

    /**
     * Fetches a json object from a url.
     *
     * @param url         the url
     * @param headersData the titles and values of the headers
     * @return json object
     * @throws IOException                 in case something goes wrong
     * @throws HttpNullResponseException   when the response body is null
     * @throws HttpResponseFailedException when the response fails
     * @throws MissingHeaderInfoException  when there is a value missing for a header
     */
    public static JSONObject getJSON(String url, String... headersData)
            throws IOException, HttpNullResponseException, HttpResponseFailedException, MissingHeaderInfoException {
        return new JSONObject(get(url, headersData));
    }

    /**
     * Fetches content from an url.
     *
     * @param url         the url
     * @param headersData the titles and values of the headers
     * @return content string
     * @throws IOException                 in case something goes wrong
     * @throws HttpNullResponseException   when the response body is null
     * @throws HttpResponseFailedException when the response fails
     * @throws MissingHeaderInfoException  when there is a value missing for a header
     */
    public static String get(String url, String... headersData)
            throws IOException, HttpNullResponseException, HttpResponseFailedException, MissingHeaderInfoException {
        Request.Builder requestBuilder = new Request.Builder().url(url);
        int argAmount = headersData.length;
        if (argAmount == 0 || argAmount % 2 == 0) {
            for (int i = 0; i < argAmount; i += 2) {
                requestBuilder.addHeader(headersData[i], headersData[i + 1]);
            }
        } else throw new MissingHeaderInfoException("Missing value for a header");
        Response response = client.newCall(requestBuilder.build()).execute();
        if (response.body() == null) {
            if (response.isSuccessful()) {
                response.close();
                throw new HttpNullResponseException("Null response received");
            }
            throw new HttpResponseFailedException("Response failed");
        } else {
            InputStream stream = response.body().byteStream();
            String answer = new String(stream.readAllBytes(), ENCODING);
            response.close();
            return answer;
        }
    }

    /**
     * Posts json content to an url.
     *
     * @param url         the url
     * @param json        the json object
     * @param headersData the titles and values of the headers
     * @return true if the request was successful, false on the contrary
     * @throws IOException                in case something goes wrong
     * @throws MissingHeaderInfoException when there is a value missing for a header
     */
    public static boolean postJSON(String url, JSONObject json, String... headersData)
            throws IOException, MissingHeaderInfoException {
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"), json.toString()
        );
        Request.Builder requestBuilder = new Request.Builder().post(body).url(url);
        int argAmount = headersData.length;
        if (argAmount == 0 || argAmount % 2 == 0) {
            for (int i = 0; i < argAmount; i += 2) {
                requestBuilder.addHeader(headersData[i], headersData[i + 1]);
            }
        } else throw new MissingHeaderInfoException("Missing value for a header");
        Response response = client.newCall(requestBuilder.build()).execute();
        boolean result = response.isSuccessful();
        response.close();
        return result;

    }

}