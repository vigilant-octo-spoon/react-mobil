package com.octo_spoon.octo_spoon_mobile.Backend;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.octo_spoon.octo_spoon_mobile.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ESTEBANFML on 09-11-2017.
 */

public class PostResourceTask extends AsyncTask<String, Void, Boolean> {

    // TODO: 09-11-2017 BUG 500 EN ESTE REQUEST
    private DBHelper vosdb;
    private String resource_item, resource_info, resource_item_obt;
    private Exception exception;
    private Context context;
    private SessionManager sessionManager;
    private int follow_id;
    private boolean available = false;

    public PostResourceTask(DBHelper _vosdb, String resource_item, String resource_info, String resource_item_obt,
                             Context context, int follow_id) {
        this.vosdb = _vosdb;
        this.resource_item = resource_item;
        this.resource_info = resource_info;
        this.resource_item_obt = resource_item_obt;
        this.context = context;
        this.follow_id = follow_id;
    }

    protected void onPreExecute() {
        //stagePlanificationActivity.showProgress(true);
        sessionManager = new SessionManager(context);

    }

    protected Boolean doInBackground(String... strings) {
        try {
            URL url = new URL(context.getResources().getString(R.string.main_api_url) + context.getResources().getString(R.string.url_follows)
                    + Integer.toString(follow_id) + context.getResources().getString(R.string.user_methodology_resource_url));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("token", sessionManager.getToken());
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);
            urlConnection.setUseCaches(false);
            urlConnection.setAllowUserInteraction(false);
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("POST");

            JSONObject body = new JSONObject();
            body.put("item", resource_item);
            if (resource_info.equals("si")) {
                body.put("available", true);
                available = true;
            } else {
                body.put("available", false);
                available = false;
            }
            body.put("acquisition", resource_item_obt);

            OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
            wr.write(body.toString());
            wr.flush();

            StringBuilder sb = new StringBuilder();
            String result = urlConnection.getResponseMessage();
            int HttpResult = urlConnection.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK || HttpResult == HttpURLConnection.HTTP_CREATED) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                JSONObject jsonTemp = new JSONObject(sb.toString());
                String message = jsonTemp.getString("message");
                int idResource = jsonTemp.getInt("idResource");
                vosdb.insertResource(idResource, follow_id, resource_item, available, resource_item_obt);
                return Boolean.TRUE;
            } else {
                Log.i("HTTPE", "PostResourceTask" +  Integer.toString(HttpResult));
                System.out.println(urlConnection.getResponseMessage());
                return Boolean.FALSE;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return Boolean.FALSE;

        } catch (IOException e) {
            e.printStackTrace();
            return Boolean.FALSE;

        } catch (JSONException e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }
    }

    protected void onPostExecute(Boolean response) {
        /*
        stagePlanificationActivity.showProgress(false);
        if (response) {
            Intent intent = new Intent(context, MainActivity.class);
            context.startActivity(intent);

        } else {
            stagePlanificationActivity.mEmailView.setError(context.getString(R.string.failed_credentials));
            stagePlanificationActivity.mEmailView.requestFocus();
        }
        stagePlanificationActivity.mUserAuth = null;*/
    }


}
