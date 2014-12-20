package com.pwspray.trinitasrooster.Services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;


public class LoginService extends IntentService {
    private static final String LOG_TAG = "LoginService";

    public static final String ACTION_LOGIN = "com.pwspray.trinitasrooster.action.LOGIN";
    public static final String ACTION_RETURN_LOGIN = "com.pwspray.trinitasrooster.action.RETURN_LOGIN";

    public static final String PARAM_USERNAME = "com.pwspray.trinitasrooster.extra.USERNAME";
    public static final String PARAM_PASSWORD = "com.pwspray.trinitasrooster.extra.PASSWORD";
    public static final String PARAM_DEBUG = "com.pwspray.trinitasrooster.extra.DEBUG";
    public static final String PARAM_OUT_DEBUG = "com.pwspray.trinitasrooster.extra.NAME";
    public static final String PARAM_OUT_NAME = "com.pwspray.trinitasrooster.extra.OUT_DEBUG";

    private static final String URL_LOGIN = "https://leerlingen.trinitascollege.nl/login?passAction=login";

    public static void startActionLogin(Context context, String param1, String param2) {
        Intent intent = new Intent(context, LoginService.class);
        intent.setAction(ACTION_LOGIN);
        intent.putExtra(PARAM_USERNAME, param1);
        intent.putExtra(PARAM_PASSWORD, param2);
        context.startService(intent);
    }

    public static void startActionLogin(Context context, String param1, String param2, boolean doDebug) {
        Intent intent = new Intent(context, LoginService.class);
        intent.setAction(ACTION_LOGIN);
        intent.putExtra(PARAM_USERNAME, param1);
        intent.putExtra(PARAM_PASSWORD, param2);
        intent.putExtra(PARAM_DEBUG, doDebug);
        context.startService(intent);
    }

    public LoginService() {
        super("LoginService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action.equals(ACTION_LOGIN)) {

                final boolean doDebug = intent.getBooleanExtra(PARAM_DEBUG, false);

                ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if(networkInfo == null || !networkInfo.isConnected()){
                    finalizeService("", doDebug);
                    return;
                }

                final String username = intent.getStringExtra(PARAM_USERNAME);
                final String password = intent.getStringExtra(PARAM_PASSWORD);

                long beginTime = System.currentTimeMillis();

                String loginString = doLogin(username, password);
                if (loginString == null || loginString.isEmpty()) {
                    Log.d(LOG_TAG, "onHandleIntent() - doLogin error, returning.");
                    finalizeService("", doDebug);
                    return;
                }

                String name = parseLogin(loginString);
                if (name == null || name.isEmpty()) {
                    Log.d(LOG_TAG, "onHandleIntent() - parseLogin error, returning.");
                    finalizeService("", doDebug);
                    return;
                }

                long endTime = System.currentTimeMillis();
                long timeDifference = endTime - beginTime;
                Log.d(LOG_TAG, "onHandleIntent() returning, execution time: " + timeDifference);
                finalizeService(name, doDebug);

                //List<HttpCookie> list = cookieManager.getCookieStore().getCookies();
                //for(HttpCookie cookie : list){
                //    Log.d(LOG_TAG, cookie.getName() + " : " + cookie.getValue() + ". age: " + cookie.getMaxAge());
                //}
            }
        }
    }

    private String parseLogin(String loginPage) {
        Log.d(LOG_TAG, "parseLogin() starting.");

        if (loginPage.contains("Welkom")) {
            Log.d(LOG_TAG, "parseLogin() Welkom.");
            String name = "";
            Document homeDoc = Jsoup.parse(loginPage);
            if (homeDoc == null)
                return "";

            Elements elems = homeDoc.getElementsByClass("logintekst");
            for (Element elem : elems) {
                name = elem.getElementsByTag("b").text();
            }
            Log.d(LOG_TAG, "parseLogin() returning name: " + name);
            return name;
        } else {
            return "";
        }
    }

    private String doLogin(String username, String password) {
        Log.d(LOG_TAG, "doLogin() starting");

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String responseString = "";

        Map<String, Object> parameters = new LinkedHashMap<String, Object>();
        parameters.put("wu_loginname", username);
        parameters.put("wu_password", password);
        parameters.put("path", "/");

        StringBuilder postData = new StringBuilder();

        try {
            for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
                if (postData.length() != 0)
                    postData.append('&');
                postData.append(URLEncoder.encode(parameter.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(parameter.getValue()), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            URL url = new URL(URL_LOGIN);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setUseCaches(false);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "text/html;charset=UTF-8");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setReadTimeout(60 * 1000); //60 seconde
            urlConnection.setConnectTimeout(60 * 1000);

            urlConnection.getOutputStream().write(postDataBytes);
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            if (inputStream == null)
                return "";

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            if (buffer.length() == 0)
                return "";

            responseString = buffer.toString();
        } catch (SocketTimeoutException e) {
            Log.d(LOG_TAG, "doLogin() TIMEOUT ", e);
            return "";
        } catch (Exception e) { //TODO op een goede manier catchen..
            Log.e(LOG_TAG, "doLogin() caught exception: ", e);
            return "";
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    Log.e(LOG_TAG, "doLogin() caught exception while closing reader: " + e.toString());
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();

                return responseString;
            }
        }

        return "";
    }

    private void finalizeService(String name, boolean doDebug) { //name = empty -> gefaald
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ACTION_RETURN_LOGIN);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(PARAM_OUT_NAME, name);
        broadcastIntent.putExtra(PARAM_OUT_DEBUG, doDebug);
        sendBroadcast(broadcastIntent);
    }
}
