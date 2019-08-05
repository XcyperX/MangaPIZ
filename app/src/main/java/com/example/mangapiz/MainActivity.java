package com.example.mangapiz;

import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.PrecomputedText;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();

    private ProgressDialog pDialog;
    public Spinner spinner;
    public Spinner spinner2;
    public Spinner spinner3;
    public ImageView imageView;
    public Button plus;
    public Button minus;

    // URL to get contacts JSON
    private static String url = "https://api.myjson.com/bins/16l1x1";

    ArrayList<HashMap<String, String>> contactList;
    ArrayList<HashMap<String, String>> contactList_2;
    ArrayList<List<String>> LINK = new ArrayList();
    public final AllValuse mViewModel = ViewModelProviders.of(this).get(AllValuse.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinner = (Spinner) findViewById(R.id.spinner);
        imageView = (ImageView) findViewById(R.id.Photo);
        contactList = new ArrayList<>();
        contactList_2 = new ArrayList<>();

        final AllValuse mViewModel = ViewModelProviders.of(this).get(AllValuse.class);

        if (mViewModel.hight == 0) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imageView.getLayoutParams();
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            final int width = size.x;
            mViewModel.hight = size.y;
            params.height = mViewModel.hight;
            imageView.setLayoutParams(params);
        }


        new BookName().execute();
        spinner.setSelection(mViewModel.spinner1);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mViewModel.spinner1 = parent.getSelectedItemPosition();
                contactList_2.clear();
                new GetContacts().execute(parent.getSelectedItem().toString());
                System.out.println(parent.getSelectedItem().toString());
                System.out.println("Размер" + LINK.size());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * Async task class to get json by making HTTP call
     */
    private class GetContacts extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Пожалуйста подождите...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(String... params) {
            HttpHandler sh = new HttpHandler();
            List<String> list = new ArrayList<>();
            LINK.clear();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray contacts = jsonObj.getJSONArray(params[0]);

                    // looping through All Contacts
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);

                        GsonBuilder builder = new GsonBuilder();
                        Gson gson = builder.create();
                        Library book = gson.fromJson(c.toString(), Library.class);
                        Log.i("GSON", book.chapter_name + "\n" + book.url + "\n" + book.number + "\n" + book.src);
                        // tmp hash map for single contact
                        HashMap<String, String> contact = new HashMap<>();

                        // adding each child node to HashMap key => value
                        contact.put("url", book.url);
                        contact.put("chapter_name", book.chapter_name);
                        contact.put("number", book.number);
                        contact.put("src", Integer.toString(i));
                        // adding contact to contact list
                        contactList_2.add(contact);
                        LINK.add(book.src);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            spinner2 = (Spinner) findViewById(R.id.spinner2);
            spinner3 = (Spinner) findViewById(R.id.spinner3);
            imageView = (ImageView) findViewById(R.id.Photo);

            List<String> Chapter_name = new ArrayList<>();
            List<String> number = new ArrayList<>();

            for (int i = 0; i < contactList_2.size(); i++){
                String name = contactList_2.get(i).get("chapter_name");
                Chapter_name.add(name);
            }


            for (int i = 0; i < Integer.parseInt(contactList_2.get(0).get("number")); i++){
                number.add(Integer.toString(i + 1));
            }

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this,
                    android.R.layout.simple_spinner_dropdown_item, Chapter_name);

            ArrayAdapter<String> arrayAdapter_2 = new ArrayAdapter<String>(MainActivity.this,
                    android.R.layout.simple_spinner_dropdown_item, number);

            spinner2.setAdapter(arrayAdapter);
            spinner3.setAdapter(arrayAdapter_2);

            OpenImage(LINK.get(0));

        }

    }

    private class BookName extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Пожалуйста подождите...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray contacts = jsonObj.getJSONArray("Book_name");

                    // looping through All Contacts
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);

                        String name = c.getString("name");

                        // tmp hash map for single contact
                        HashMap<String, String> contact = new HashMap<>();

                        // adding each child node to HashMap key => value
                        contact.put("name", name);

                        // adding contact to contact list
                        contactList.add(contact);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            spinner = (Spinner) findViewById(R.id.spinner);
            spinner2 = (Spinner) findViewById(R.id.spinner2);
            spinner3 = (Spinner) findViewById(R.id.spinner3);

            List<String> Chapter = new ArrayList<>();
            for (int i = 0; i < contactList.size(); i++){
                String name = contactList.get(i).get("name");
                System.out.println(name);
                Chapter.add(name);
            }

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this,
                    android.R.layout.simple_spinner_dropdown_item, Chapter);
            spinner.setAdapter(arrayAdapter);
//            show_items();
        }

    }

    public void OpenImage(final List<String> link){
        spinner3 = (Spinner) findViewById(R.id.spinner3);
        imageView = (ImageView) findViewById(R.id.Photo);
//        final AllValuse mViewModel = ViewModelProviders.of(this).get(AllValuse.class);

        spinner3.setSelection(mViewModel.spinner2);
        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String url = link.get(parent.getSelectedItemPosition());
                mViewModel.spinner2 = parent.getSelectedItemPosition();
                Picasso.get().load(url).placeholder(R.drawable.loading).resize(0, mViewModel.hight).into(imageView);
                System.out.println(url);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void setPlus(View view){
        int x = spinner3.getSelectedItemPosition();
        spinner3.setSelection(x + 1);
    }

    public void setMinus(View view){
        int x = spinner3.getSelectedItemPosition();
        spinner3.setSelection(x - 1);
    }

//    private void show_items(){
//        spinner2 = (Spinner) findViewById(R.id.spinner2);
//    }

//    private List<String> openFile(String fileName) {
//        try {
//            InputStream inStream = openFileInput(fileName);
//
//            if (inStream != null) {
//                InputStreamReader tmp =
//                        new InputStreamReader(inStream);
//                BufferedReader reader = new BufferedReader(tmp);
//                String str;
//                List<String> Chapter_book = new ArrayList<>();
//
//
//                while ((str = reader.readLine()) != null) {
//                    Chapter_book.add(str + "\n");
//                }
//                inStream.close();
//                return Chapter_book;
//            }
//        }
//        catch (Throwable t) {
//            Toast.makeText(getApplicationContext(),
//                    "Exception: " + t.toString(), Toast.LENGTH_LONG)
//                    .show();
//            Log.e(TAG, t.getMessage());
//        }
//        return null;
//    }

}