package com.example.stockwatch;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

public class FinancialDataDownloader extends AsyncTask<String, Double, String> {


    @SuppressLint("StaticFieldLeak")
    private MainActivity mainActivity;
    private String rawURL = "https://cloud.iexapis.com/stable/stock/";
    private String requestMethod = "GET";

    //Symbol and name for stocks, pos for keeping track of list position
    private String symbol;
    private String name;
    private int position;

    private static final String TAG = "FinancialDataDownloader";

    public FinancialDataDownloader(MainActivity ma, int pos){
        this.mainActivity = ma;
        this.position = pos;
    }

    @Override
    protected String doInBackground(String... strArgs){

        this.symbol = strArgs[0];
        this.name = strArgs[1];

        rawURL = rawURL + this.symbol + "/quote?token=sk_b700e68ef2d2422d983c42dcd438bbe3";
        Uri rawURI = Uri.parse(rawURL);
        String parsedURI = rawURI.toString();
        StringBuilder stringBuilder = new StringBuilder();



        try{
            //set up a connection to URL
            URL url = new URL(parsedURI);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(requestMethod);

            InputStream inputStream = con.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            //put newly parsed JSON into a string
            String currentLine;
            while((currentLine =  bufferedReader.readLine()) != null){
                stringBuilder.append(currentLine).append("\n");
            }

            return stringBuilder.toString();
        } catch(IOException e){
            e.printStackTrace();
            return null;
        }

    }

    @Override
    protected void onPostExecute(String str){
        super.onPostExecute(str);

            double latestPrice = 0.0;
            double change = 0.0;
            double changePercent = 0.0;

        try{
            JSONObject jo = new JSONObject(str);
            latestPrice = Double.valueOf(jo.getDouble("latestPrice"));
            change = Double.valueOf(jo.getDouble("change"));
            changePercent = Double.valueOf(jo.getDouble("changePercent"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Make that new stock object
            Stock s = new Stock(this.symbol, this.name, latestPrice, change, changePercent);

            //If stock exists UPDATE STOCK s
            if(this.position > -1){
                this.mainActivity.updateStock(s, this.position);

                Log.d(TAG, "onPostExecute: update");
            }
            //If stock doesn't exist ADD STOCK s
            else{
                this.mainActivity.addStock(s);
            }
    }
}
