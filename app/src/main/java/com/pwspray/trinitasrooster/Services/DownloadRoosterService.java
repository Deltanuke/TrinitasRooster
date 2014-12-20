package com.pwspray.trinitasrooster.Services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.pwspray.trinitasrooster.Constants;
import com.pwspray.trinitasrooster.Data.PeriodObject;
import com.pwspray.trinitasrooster.Data.RoosterDbHelper;
import com.pwspray.trinitasrooster.R;
import com.pwspray.trinitasrooster.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DownloadRoosterService extends IntentService {
    private static final String LOG_TAG = "DownloadRoosterService";

    public static final String ACTION_DOWNLOAD_ROOSTER = "com.pwspray.trinitasrooster.action.DOWNLOAD_ROOSTER";
    public static final String ACTION_RETURN_DOWNLOAD_ROOSTER = "com.pwspray.trinitasrooster.action.RETURN_DOWNLOAD_ROOSTER";

    public static final String PARAM_DATE = "com.pwspray.trinitasrooster.extra.DATE";
    public static final String PARAM_DEBUG = "com.pwspray.trinitasrooster.extra.DEBUG";

    public static final String PARAM_OUT_SUCCES = "com.pwspray.trinitasrooster.extra.OUT_SUCCES";

    private static final String URL_BASE_ROOSTER = "https://leerlingen.trinitascollege.nl/fs/SOMTools/Comps/Agenda.cfc?format=json&method=getLeerlingRooster&so_id=7227";

    private final int NOTIFICATION_ID = 3005;

    private boolean doDebug = false;

    public static void startActionDownloadRooster(Context context, String date, boolean doDebug) {
        Log.d(LOG_TAG, "startActionDownloadRooster()");
        Intent intent = new Intent(context, DownloadRoosterService.class);
        intent.setAction(ACTION_DOWNLOAD_ROOSTER);
        intent.putExtra(PARAM_DATE, date);
        intent.putExtra(PARAM_DEBUG, doDebug);
        context.startService(intent);
    }

    public static void startActionDownloadRooster(Context context, String date) {
        Log.d(LOG_TAG, "startActionDownloadRooster()");
        Intent intent = new Intent(context, DownloadRoosterService.class);
        intent.setAction(ACTION_DOWNLOAD_ROOSTER);
        intent.putExtra(PARAM_DATE, date);
        intent.putExtra(PARAM_DEBUG, false);
        context.startService(intent);
    }

    public DownloadRoosterService() {
        super("DownloadRoosterService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.d(LOG_TAG, "onHandleIntent() - starting");
            final String action = intent.getAction();
            if (action.equals(ACTION_DOWNLOAD_ROOSTER)) {
                Log.d(LOG_TAG, "onHandleIntent() - equals");

                ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if(networkInfo == null || !networkInfo.isConnected()){
                    finalizeService(false);
                    return;
                }

                final String date = intent.getStringExtra(PARAM_DATE);
                doDebug = intent.getBooleanExtra(PARAM_DEBUG, false);

                Log.d(LOG_TAG, "onHandleIntent() - debug?" + doDebug);

                long beginTime = System.currentTimeMillis();

                String roosterString = doDownloadRooster(date);
                if (roosterString == null || roosterString.isEmpty()) {
                    Log.d(LOG_TAG, "onHandleIntent() - doDownloadRooster error, returning.");
                    finalizeService(false);
                    return;
                }
                else if(roosterString.contains("Er is een fout opgetreden")) {
                    Log.d(LOG_TAG, "onHandleIntent() - fout opgetreden, (wss niet ingelogd)");
                    finalizeService(false);
                    return;
                }

                doParseRooster(roosterString, date);

                long endTime = System.currentTimeMillis();
                long timeDifference = endTime - beginTime;
                Log.d(LOG_TAG, "onHandleIntent() returning, execution time: " + timeDifference);
                finalizeService(true);
            }
        }
    }


    private void doParseRooster(String rooster, String sDate){
        Log.d(LOG_TAG, "doParseRooster() starting ");
        try{
            JSONObject object = new JSONObject(rooster);
            JSONArray events = object.getJSONArray("events");

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            Calendar monday = Calendar.getInstance();
            monday.setTime(sdf.parse(sDate));

            Calendar lastMonday = Calendar.getInstance();
            lastMonday.setTime(monday.getTime());
            lastMonday.add(Calendar.DAY_OF_YEAR, -7);

            Boolean hasWeekInDatabase = RoosterDbHelper.hasWeek(this, monday);
            Boolean hasLastWeekInDatabase = RoosterDbHelper.hasWeek(this, lastMonday);

            RoosterDbHelper.setHasWeek(this, monday);

            int eventId =  0;

            for(int i = 1; i <= 5; i++){ //5 dagen
                Calendar loopDate = Calendar.getInstance();
                loopDate.setTime(monday.getTime());
                int addDays = i - 1;
                loopDate.add(Calendar.DAY_OF_YEAR, addDays);

                for(int j = 1; j <= Constants.HOURS_IN_DAY; j++){
                    if(eventId >= events.length())
                        continue;

                    JSONObject event = events.getJSONObject(eventId);
                    JSONObject afspraak = event.getJSONObject("afspraakObject");
                    int lesuur = afspraak.getInt("lesuur");
                    long unixStart = event.getLong("start");
                    long unixEnd = event.getLong("end");

                    Boolean dateHasPassed = System.currentTimeMillis() > (unixStart + 1000 * 60 * 10);
                    dateHasPassed = false; //debug

                    Calendar jsonDate = Calendar.getInstance();
                    jsonDate.setTimeInMillis(unixStart);
                    String stringDate = sdf.format(jsonDate.getTime());

                    RoosterDbHelper dbHelper = new RoosterDbHelper(this);

                    PeriodObject periodDataThisWeek = dbHelper.getPeriodByDateAndHourObject(stringDate, j);
                    PeriodObject periodDataLastWeek = null;

                    Calendar lastWeekDate = Calendar.getInstance();
                    lastWeekDate.setTime(jsonDate.getTime());
                    lastWeekDate.add(Calendar.DAY_OF_YEAR, -7);
                    String lastWeekDateString = sdf.format(lastWeekDate.getTime());

                    if(hasLastWeekInDatabase){
                        periodDataLastWeek = dbHelper.getPeriodByDateAndHourObject(lastWeekDateString, lesuur);
                    }


                    if(jsonDate.get(Calendar.DAY_OF_YEAR) != loopDate.get(Calendar.DAY_OF_YEAR)){
                        Log.d(LOG_TAG, "doParseRooster() dag: "+ jsonDate.get(Calendar.DAY_OF_YEAR) + " != " + loopDate.get(Calendar.DAY_OF_YEAR));
                        //hele dag vrij
                        break;
                    }

                    if(lesuur != j){//j vrij
                        Log.d(LOG_TAG, "doParseRooster() lesuur != j ");
                        if(hasWeekInDatabase) {
                            dbHelper.deletePeriod(stringDate, j);
                            if (periodDataThisWeek != null) {
                                if(periodDataThisWeek.state != 1) { //state 1 = al notified
                                    Log.d(LOG_TAG, "doParseRooster() - uitval t.o.v. eerdere info van deze week");

                                    if(!dateHasPassed)
                                        doNotification(j, jsonDate, periodDataThisWeek.className); //(int lesuur, Calendar datum, String oldClassname){ //voor uitval

                                    dbHelper.writePeriod(j, null, null, null, null, null, 1, stringDate, null, null);
                                }
                            }
                        }
                        else if(hasLastWeekInDatabase){
                            if(periodDataLastWeek != null){ //nu geen les, vorige week wel
                                if(periodDataThisWeek != null && periodDataThisWeek.state != 1) {//state 1 = al notified
                                    Log.d(LOG_TAG, "doParseRooster() -uitval t.o.v. vorige week");

                                    if(!dateHasPassed)
                                        doNotification(j, jsonDate, periodDataLastWeek.className); //(int lesuur, Calendar datum, String oldClassname){ //voor uitval

                                    dbHelper.writePeriod(j, null, null, null, null, null, 1, stringDate, null, null);
                                }
                                else if(periodDataThisWeek == null){
                                    Log.d(LOG_TAG, "doParseRooster() -uitval t.o.v. vorige week");

                                    if(!dateHasPassed)
                                        doNotification(j, jsonDate, periodDataLastWeek.className); //(int lesuur, Calendar datum, String oldClassname){ //voor uitval

                                    dbHelper.writePeriod(j, null, null, null, null, null, 1, stringDate, null, null);
                                }
                            }
                        }
                        continue;
                    }

                    eventId++;

                    //if(dateHasPassed)
                    //    continue;

                    String lesgroep = afspraak.getString("lesgroep");
                    if (lesgroep == null || lesgroep.isEmpty()) { //Studiehuis gebeuren
                        continue;
                    }

                    JSONArray docentArray = afspraak.getJSONArray("docent");
                    JSONObject docent = docentArray.getJSONObject(0);
                    JSONObject huiswerk;
                    String huiswerkOmschrijving = "";
                    boolean huiswerkToets;

                    Boolean heeftHuiswerk = afspraak.has("huiswerk");
                    if(heeftHuiswerk){
                        huiswerk = afspraak.getJSONObject("huiswerk"); //huiswerk.getBoolean("gemaakt");
                        huiswerkOmschrijving = huiswerk.getString("omschrijving");
                        //huiswerkToets = huiswerk.getBoolean("toets");
                    }

                    String lokaal = afspraak.getString("lokaal");
                    String docentAfkorting = docent.getString("afkorting");
                    String docentAchternaam = docent.getString("achternaam");
                    String docentTitel = docent.getString("title");

                    docentAchternaam = docentTitel + " " + docentAchternaam;

                    if(hasWeekInDatabase) { //als deze week al een keer gedownload is checken op wijzigingen
                        if(periodDataThisWeek != null){

                            Boolean changeTeacher = false;
                            Boolean changeClassroom = false;
                            Boolean changeClassname = false;
                            Boolean changeHomework = false;

                            if (!periodDataThisWeek.teacherShort.equals(docentAfkorting)) { //Leraar veranderd tov laaste update
                                changeTeacher = true;
                            }
                            if (!periodDataThisWeek.classroom.equals(lokaal)) { //Lokaal wijzigingtov laaste update
                                changeClassroom = true;
                            }
                            if (!periodDataThisWeek.className.equals(lesgroep)) { //lesgroep verandering (vak) tov laaste update
                                changeClassname = true;
                            }
                            if (!periodDataThisWeek.homework.equals(huiswerkOmschrijving)) { //huiswerk verandering tov laaste update
                                changeHomework = true;
                            }

                            if(hasLastWeekInDatabase && !changeTeacher && !changeClassroom && !changeClassname && !changeHomework){
                                if(periodDataLastWeek == null){ //vorige week uitval, nu niet

                                } else {
                                    if (!periodDataLastWeek.teacherShort.equals(docentAfkorting)) { //Leraar veranderd tov laaste update
                                        changeTeacher = true;
                                    }
                                    if (!periodDataLastWeek.classroom.equals(lokaal)) { //Lokaal wijzigingtov laaste update
                                        changeClassroom = true;
                                    }
                                    if (!periodDataLastWeek.className.equals(lesgroep)) { //lesgroep verandering (vak) tov laaste update
                                        changeClassname = true;
                                    }
                                }
                            }

                            if(changeTeacher || changeClassroom || changeClassname){ //changeHomework
                                Log.d(LOG_TAG, "doParseRooster() CHANGE!");

                                if(changeClassroom){
                                    Log.d(LOG_TAG, "doParseRooster() CHANGE! STATE != 0 changeClassroom");
                                    if(!dateHasPassed && periodDataThisWeek.state != 1)
                                        doNotification(lesuur, jsonDate, periodDataThisWeek.classroom, lokaal);
                                }

                                Boolean update = dbHelper.updatePeriod(stringDate, lesuur, docentAfkorting, docentAchternaam, lokaal, lesgroep, huiswerkOmschrijving, 1);
                                if (!update)
                                    Log.d(LOG_TAG, "doParseRooster() - Iets fout gegaan met updaten");
                            }
                        } else { //debug inval
                            boolean update = dbHelper.updatePeriod(stringDate, lesuur, lesgroep, lokaal, docentAfkorting, docentAchternaam, huiswerkOmschrijving, 0, unixStart, unixEnd);
                            if(!update){
                                boolean write = dbHelper.writePeriod(lesuur, lesgroep, lokaal, docentAfkorting, docentAchternaam, huiswerkOmschrijving, 0, stringDate ,unixStart, unixEnd);
                                if(!write){
                                    Log.d(LOG_TAG, "doParseRooster() - Iets fout gegaan met writen");
                                }
                            }

                        }

                    }
                    else if(hasLastWeekInDatabase) {
                        Log.d(LOG_TAG, "doParseRooster() - hasLastWeekInDatabase");

                        if(periodDataLastWeek == null){ // vorige week vrij, nu niet

                        } else {
                            Boolean changeTeacher = false;
                            Boolean changeClassroom = false;
                            Boolean changeClassname = false;

                            if (!periodDataLastWeek.teacherShort.equals(docentAfkorting)) { //Leraar veranderd tov laaste update
                                changeTeacher = true;
                            }
                            if (!periodDataLastWeek.classroom.equals(lokaal)) { //Lokaal wijzigingtov laaste update
                                changeClassroom = true;
                            }
                            if (!periodDataLastWeek.className.equals(lesgroep)) { //lesgroep verandering (vak) tov laaste update
                                changeClassname = true;
                            }

                            if(changeClassroom && !dateHasPassed)
                                doNotification(lesuur, jsonDate, periodDataLastWeek.classroom, lokaal);

                        }

                        boolean write = dbHelper.writePeriod(lesuur, lesgroep, lokaal, docentAfkorting, docentAchternaam, huiswerkOmschrijving, 0, stringDate, unixStart, unixEnd);
                        if(!write)
                            Log.d(LOG_TAG, "doParseRooster() - Iets fout gegaan met writen (vorige week in db)");
                    } else { //helemaal nog niet in db
                        Log.d(LOG_TAG, "doParseRooster() - niks in database");
                        boolean write = dbHelper.writePeriod(lesuur, lesgroep, lokaal, docentAfkorting, docentAchternaam, huiswerkOmschrijving, 0, stringDate ,unixStart, unixEnd);
                        if(!write)
                            Log.d(LOG_TAG, "doParseRooster() - Iets fout gegaan met writen");
                    }

                }
            }
        } catch (Exception e){
            Log.e(LOG_TAG, "doParseRooster() caught exception: ", e);
        }
    }

    private String doDownloadRooster(String date) {
        Log.d(LOG_TAG, "doDownloadRooster() starting");

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String responseString = "";

        String roosterUrl = URL_BASE_ROOSTER + "&startDate=" + date;
        if(doDebug)
            roosterUrl = "https://dl.dropboxusercontent.com/u/73617382/test.json";

        try {
            URL url = new URL(roosterUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setUseCaches(false);
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "text/html;charset=UTF-8");
            urlConnection.setDoInput(true);
            urlConnection.setReadTimeout(60 * 1000); //60 seconde
            urlConnection.setConnectTimeout(60 * 1000);

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
            Log.d(LOG_TAG, "doDownloadRooster() TIMEOUT ", e);
            return "";
        } catch (Exception e) { //TODO op een goede manier catchen..
            Log.e(LOG_TAG, "doDownloadRooster() caught exception: ", e);
            return "";
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    Log.e(LOG_TAG, "doDownloadRooster() caught exception while closing reader: " + e.toString());
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();

                return responseString;
            }
        }

        return "";
    }

    public void doNotification(int lesuur, Calendar datum, String oldClassname){ //voor uitval
        Log.d(LOG_TAG, "doNotification uitval");
        Resources resources = this.getResources();

        int iDag = datum.get(Calendar.DAY_OF_WEEK); //zondag=1, maandag=2, etc
        String dag = resources.getStringArray(R.array.week_dagen)[iDag - 2];//maandag = 0, dinsdag = 1, etc
        String lesuur_rangteel = resources.getStringArray(R.array.uren_rangteel)[lesuur - 1];
        String vak = Util.getFullSubject(oldClassname.substring(3));

        String content = String.format(resources.getString(R.string.uur_uitval), dag, lesuur_rangteel, vak); //<!-- Dag, uur_rangteel, vak -->

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_notificatie)
                        .setContentTitle(resources.getString(R.string.uur_uitval_titel))
                        .setContentText(content)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(content));
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public void doNotification(int lesuur, Calendar datum, String oldClassroom, String newClassroom){ //voor lokaal wijziging
        Log.d(LOG_TAG, "doNotification lokaal wijzigign");
        Resources resources = this.getResources();

        int iDag = datum.get(Calendar.DAY_OF_WEEK);
        String dag = resources.getStringArray(R.array.week_dagen)[iDag - 2];
        String lesuur_rangteel = resources.getStringArray(R.array.uren_rangteel)[lesuur - 1];

        String content = String.format(resources.getString(R.string.lokaal_wijziging), dag, lesuur_rangteel, newClassroom); //<!-- Dag, uur_rangteel, nieuw Lokaal, -->

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_notificatie)
                        .setContentTitle(resources.getString(R.string.lokaal_wijziging_titel))
                        .setContentText(content)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(content));
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        int mId = 0;
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private void finalizeService(Boolean succes) {
        Log.d(LOG_TAG, "finalizeService()");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ACTION_RETURN_DOWNLOAD_ROOSTER);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(PARAM_OUT_SUCCES, succes);
        sendBroadcast(broadcastIntent);
    }
}
