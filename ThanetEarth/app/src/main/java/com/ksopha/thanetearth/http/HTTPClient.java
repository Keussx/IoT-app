package com.ksopha.thanetearth.http;

import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * This class can be used to send http requests.
 * It makes use of HttpURLConnection (https://developer.android.com/reference/java/net/HttpURLConnection.html)
 * to prepare and send requests
 * Created by Kelvin Sopha on 23/02/18.
 */

public class HTTPClient {

    private int connectTimeout;
    private int readTimeout;


    /**
     * constructor
     * @param connectTimeout a timeout to use while connecting
     * @param readTimeout a timeout to use while reading http responses
     */
    public HTTPClient(int connectTimeout, int readTimeout){
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    /**
     * performs a http request and returns the response body as String
     *
     * @param http_link the http url of web server to send request to
     * @return http response body string
     */
    public String getHttpResponseAsString(String http_link){

        StringBuffer responseBuffer = new StringBuffer();

        try {
            // construct URL object
            URL obj = new URL(http_link);

            // open a connection
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            if (con!=null) {

                // set http request to GET
                con.setRequestMethod("GET");

                //set timeout for connecting to web server
                con.setConnectTimeout(connectTimeout);

                //set timeout for reading response data from server
                con.setReadTimeout(readTimeout);

                // if http response is valid (by checking if http header status code is 200 (OK))
                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    // get input stream of connection
                    InputStream resStream = new BufferedInputStream(con.getInputStream());

                    // create reader to read from stream
                    BufferedReader reader = new BufferedReader(new InputStreamReader(resStream));

                    String inputLine;

                    // read and append every line of text from the connection stream to responseBuffer
                    while ((inputLine = reader.readLine()) != null) {
                        responseBuffer.append(inputLine);
                    }

                    // close the BufferedReader
                    reader.close();
                }

            }

        }
        catch (Exception e) {
            // log any error
            Log.i("HTTPClient", e.getMessage());
        }
        finally {
            // returns the saved response string or an empty string if nothing was appended
            return responseBuffer.toString();

        }
    }
}
