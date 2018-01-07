package com.at.cancerbero.utils.server;

import android.util.Log;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.http.ApacheHttpClient;
import com.amazonaws.http.HttpRequest;
import com.amazonaws.http.HttpResponse;
import com.at.cancerbero.utils.ExceptionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ServerConnector {

    private static final int TIMEOUT = 5000;

    protected final String TAG = getClass().getSimpleName();

    private final String baseUrl;

    private Map<String, String> cookies = new HashMap<>();

    private Map<String, String> commonHeaders = new HashMap<>();

    private final ClientConfiguration clientConfiguration;
    private final ApacheHttpClient client;


    public ServerConnector(String baseUrl, boolean ignoreHostVerification) {
        this.baseUrl = baseUrl;

        commonHeaders.put("Content-Type", "application/json");

        clientConfiguration = new ClientConfiguration();
        clientConfiguration.setConnectionTimeout(TIMEOUT);
        client = new ApacheHttpClient(clientConfiguration);

        if (ignoreHostVerification) {
            CertificatesUtils.disableSSLCertificateChecking();
        }
    }

    public Map<String, String> getCommonHeaders() {
        return commonHeaders;
    }

    public <T> T get(String relativeUrl, Class<T> outputClass) throws UnexpectedCodeException {
        return execute(relativeUrl, "GET", null, outputClass);
    }

    public <T> T post(String relativeUrl, Object input, Class<T> outputClass) throws UnexpectedCodeException {
        return execute(relativeUrl, "POST", input, outputClass);
    }

    public <T> T delete(String relativeUrl, Class<T> outputClass) throws UnexpectedCodeException {
        return execute(relativeUrl, "DELETE", null, outputClass);
    }

    public <T> T execute(String relativeUrl, String method, Object input, Class<T> outputClass) throws UnexpectedCodeException {
        T result = null;

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            long t1 = System.currentTimeMillis();

            InputStream in = null;
            if (input != null) {
                String content = objectMapper.writeValueAsString(input);
                in = new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8")));
            }

            Map<String, String> headers = getHeaders();
            Log.d(TAG, "Header: " + headers);
            URI url = new URI(baseUrl + relativeUrl);
            HttpRequest request = new HttpRequest(method, url, headers, in);


            HttpResponse response = client.execute(request);
            scanCookies(response);
            Log.d(TAG, "Send data " + method + " in " + (System.currentTimeMillis() - t1) + "ms");

            Log.d(TAG, "Response code: " + response.getStatusCode());

            if (isSuccessAnswer(response.getStatusCode())) {
                if (outputClass != null) {
                    result = objectMapper.readValue(response.getContent(), outputClass);
                }
            } else {
                Scanner scanner = new Scanner(response.getContent());
                StringBuilder builder = new StringBuilder();

                while (scanner.hasNextLine()) {
                    builder.append(scanner.nextLine()).append("\n");
                }
                Log.d(TAG, "Server response: " + builder);

                throw new UnexpectedCodeException(response.getStatusCode(), builder.toString());
            }
            Log.d(TAG, method + " " + url + " " + (System.currentTimeMillis() - t1) + "ms");
        } catch (Exception e) {
            ExceptionUtils.throwRuntimeException(e);
        }
        return result;
    }

    private boolean isSuccessAnswer(int statusCode) {
        return (statusCode >= 200) && (statusCode <= 300);
    }

    public void invalidateCookies() {
        cookies.clear();
    }

    private void scanCookies(HttpResponse response) {
        Map<String, String> headerFields = response.getHeaders();

        String cookiesHeaders = headerFields.get("Set-Cookie");

        if (cookiesHeaders != null) {
            String[] cookieParts = cookiesHeaders.split(";");
            String cookie = cookieParts[0];

            String[] cookieMainParts = cookie.split("=");
            String key = cookieMainParts[0];
            String value = cookieMainParts[1];
            cookies.put(key, value);
            Log.d(TAG, "Cookie: " + key + " - " + value);
        }
    }


    private Map<String, String> getHeaders() {
        Map<String, String> result = new HashMap<>(commonHeaders);
        String cookiesString = "";

        for (String cookieKey : cookies.keySet()) {
            if (!cookiesString.isEmpty()) {
                cookiesString += "; ";
            }
            cookiesString += cookieKey + "=" + cookies.get(cookieKey);
        }
        result.put("Cookie", cookiesString);

        return result;
    }
}
