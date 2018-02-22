package pl.maxmati.tobiasz.mmos.bread.api;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import pl.maxmati.tobiasz.mmos.bread.R;
import pl.maxmati.tobiasz.mmos.bread.api.session.Session;
import pl.maxmati.tobiasz.mmos.bread.api.session.SessionException;
import pl.maxmati.tobiasz.mmos.bread.api.session.SessionExpiredException;
import pl.maxmati.tobiasz.mmos.bread.api.session.SessionManager;

/**
 * Created by mmos on 11.02.16.
 *
 * @author mmos
 */
public class APIConnector {
    private static final String TAG = "APIConnector";

    private final String apiUri;
    private final Session session;

    public APIConnector(Context context, Session session) throws SessionException {
        this.session = session;
        this.apiUri = getAPIUri(context);
    }

    public <T> ResponseEntity<T> sendRequest(APIRequest request, Class<T> responseType) throws
            SessionException {
        final String fullUri;
        final HttpHeaders requestHeaders;
        final RestTemplate requestRestTemplate;
        final Gson gson;
        final GsonHttpMessageConverter gsonHttpMessageConverter;

        try {
            if(!SessionManager.check(apiUri, session))
                throw new SessionExpiredException("Session expired");
        } catch (RestClientException e) {
            throw new SessionException("Failed to check session expire", e);
        }

        fullUri = apiUri + request.getRequestPath();
        requestHeaders = new HttpHeaders();
        SessionManager.addSessionCookieToHeader(requestHeaders, session);

        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'").create();
        gsonHttpMessageConverter = new GsonHttpMessageConverter();
        gsonHttpMessageConverter.setGson(gson);

        requestRestTemplate = new RestTemplate();
        requestRestTemplate.setMessageConverters(new ArrayList<>(Arrays.asList(
                new HttpMessageConverter<?>[]{gsonHttpMessageConverter})));
        if(request.getCustomResponseErrorHandler() != null)
            requestRestTemplate.setErrorHandler(request.getCustomResponseErrorHandler());

        addLoggingInterceptor(requestRestTemplate);

        Log.d(TAG, "REST exchange: " + fullUri);
        try {
            return requestRestTemplate.exchange(fullUri, request.getMethod(),
                    buildHttpEntity(request.getData(), requestHeaders), responseType);
        } catch (RestClientException e) {
            throw new SessionException("REST exchange failed: " + e.getMessage());
        }
    }

    private void addLoggingInterceptor(RestTemplate restTemplate) {
        restTemplate.getInterceptors().add(new ClientHttpRequestInterceptor() {
            @Override
            public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                                ClientHttpRequestExecution execution)
                    throws IOException {
                if(body.length > 0)
                    Log.v(TAG, "Request data: " + new String(body, "UTF-8"));
                return execution.execute(request, body);
            }
        });
    }

    public static HttpEntity<Object> buildHttpEntity(Object data, HttpHeaders customHeaders) {
        final HttpHeaders requestHeaders;

        if(customHeaders != null) {
            requestHeaders = customHeaders;
        } else {
            requestHeaders = new HttpHeaders();
            requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        }

        if(data == null)
            return new HttpEntity<>(requestHeaders);
        else
            return new HttpEntity<>(data, requestHeaders);
    }

    public Session getSession() {
        return session;
    }

    public static String getAPIUri(Context context) {
        return context.getString(R.string.api_uri);
    }
}
