package com.iftalab.chuckjokes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements Response.Listener , Response.ErrorListener {
    private  Context context;
    private RequestQueue requestQueue;
    private static final String baseURL = "http://api.icndb.com/jokes";
    public static final String REQUEST_TAG = "IftaRequest";
    TextView tJoke;
    ImageView ivBackgroundImage;
    HashMap<Integer,Integer> imageNoToResourceId = new HashMap<Integer, Integer>();
    Random r = new Random();
    boolean fetchRandom = true;
    int currentJokesNumber = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getJoke();
            }
        });

        tJoke = (TextView) findViewById(R.id.tJoke);
        ivBackgroundImage = (ImageView) findViewById(R.id.ivBackgroundImage);
    }

    private void resolvePreference() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        fetchRandom = sp.getBoolean("fetchRandom" , true);
        currentJokesNumber = sp.getInt("currentJokesNumber",-1);
        if(currentJokesNumber == -1) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("currentJokesNumber" , 1);
            editor.commit();
            currentJokesNumber = 1;
        }
    }

    private void getJoke() {
        if(fetchRandom)
        {
            getRandomJoke();
        }else {
            getJokeByNumber(currentJokesNumber);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        resolvePreference();
        for(int i = 1 ; i <=9 ; i++)
        {
            imageNoToResourceId.put(i,getResources().getIdentifier("c"+i, "drawable", this.getPackageName()));
        }
        requestQueue = IftaVolleyQueue.getInstance(context).getRequestQueue();
        getJoke();
    }

    private void getJokeByNumber(int currentJokesNumber) {
        String url = makeURL(String.valueOf(currentJokesNumber));
        Log.i(REQUEST_TAG,"url " + url);
        IftaJSONobjectRequest iftaJSONobjectRequest = new IftaJSONobjectRequest(Request.Method.GET, url, new JSONObject(), this, this);
        iftaJSONobjectRequest.setTag(REQUEST_TAG);
        requestQueue.add(iftaJSONobjectRequest);
    }

    private void getRandomJoke()
    {
        String randomURL = makeURL("random");
        Log.i(REQUEST_TAG,"url " + randomURL);
        IftaJSONobjectRequest iftaJSONobjectRequest = new IftaJSONobjectRequest(Request.Method.GET, randomURL, new JSONObject(), this, this);
        iftaJSONobjectRequest.setTag(REQUEST_TAG);
        requestQueue.add(iftaJSONobjectRequest);
    }

    private void setRandomBackground()
    {
        int imageNo = r.nextInt(9)+1;
        Log.i(REQUEST_TAG,"image no = " + imageNo);

        ivBackgroundImage.setImageResource(imageNoToResourceId.get(imageNo));
    }
    @Override
    protected void onStop() {
        super.onStop();
        if(requestQueue!=null)
        {
            requestQueue.cancelAll(REQUEST_TAG);
        }
    }

    private String makeURL(String urlEnd) {
        return baseURL + "/" + urlEnd;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveCurrentState()
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("currentJokesNumber",currentJokesNumber-1);
        editor.commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveCurrentState();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }
    @Override
    public void onErrorResponse(VolleyError error) {
        Log.i(REQUEST_TAG,"error " + error.getLocalizedMessage());
        tJoke.setText("Error while fetching jokes no "+ currentJokesNumber+ ". Chuck Norris can make the error to tell a joke. But you can't.");
        currentJokesNumber++;
    }
    @Override
    public void onResponse(Object response) {
        try {
            JSONObject joke = (JSONObject) response;
            JSONObject jokeValue = joke.getJSONObject("value");
            String theJoke = jokeValue.getString("joke");
            theJoke = theJoke.replaceAll("&quot;","\"");
            if(!fetchRandom)
            {
                theJoke = currentJokesNumber + "\n" + theJoke;
                currentJokesNumber++;
            }
            tJoke.setText(theJoke);
            setRandomBackground();
            Log.i(REQUEST_TAG, theJoke);
        }catch (JSONException e)
        {
            Log.i(REQUEST_TAG,"Error = " + e.getLocalizedMessage());
        }
    }
}
