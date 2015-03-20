package th.ac.tu.siit.its333.lab7exercise1;

        import android.app.ProgressDialog;
        import android.os.AsyncTask;
        import android.support.v7.app.ActionBarActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.widget.TextView;
        import android.widget.Toast;

        import org.json.JSONException;
        import org.json.JSONObject;
        import org.json.JSONArray;


        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStreamReader;
        import java.net.HttpURLConnection;
        import java.net.MalformedURLException;
        import java.net.URL;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        WeatherTask w = new WeatherTask();
        w.execute("http://ict.siit.tu.ac.th/~cholwich/bangkok.json", "Bangkok Weather");
    }

    long lastClickedBKK = 0, lastClickedNon = 0, lastClickedPat = 0 ;int idprev ;

    public void buttonClicked(View v) {
        long current = System.currentTimeMillis();

        int id = v.getId();
        WeatherTask w = new WeatherTask();
        switch (id) {

            case R.id.btBangkok:
                if(current - lastClickedBKK > 60000 || id != idprev) {
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/bangkok.json", "Bangkok Weather");
                    lastClickedBKK = current;
                    lastClickedNon = 0;
                    lastClickedPat = 0;
                    idprev = id;

                }
                else {
                    Toast.makeText(getApplicationContext(),
                            "Please wait for 60 seconds before reloading.", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btNon:
                if(current - lastClickedNon > 60000 || id != idprev) {
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/nonthaburi.json", "Nonthaburi Weather");
                    lastClickedNon = current;
                    lastClickedBKK = 0;
                    lastClickedPat = 0;
                    idprev = id;


                }
                else {
                    Toast.makeText(getApplicationContext(),
                            "Please wait for 60 seconds before reloading.", Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.btPathum:
                if(current - lastClickedPat > 60000||id != idprev) {
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/pathumthani.json", "Pathumthani Weather");
                    lastClickedPat = current;
                    lastClickedBKK = 0;
                    lastClickedNon = 0;
                    idprev = id;

                }
                else {
                    Toast.makeText(getApplicationContext(),
                            "Please wait for 60 seconds before reloading.", Toast.LENGTH_LONG).show();
                }
                break;


        }
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class WeatherTask extends AsyncTask<String, Void, Boolean> {
        String errorMsg = "";
        ProgressDialog pDialog;
        String title;

        double windSpeed;

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading weather data ...");
            pDialog.show();
        }

        Double temp;
        Double tempmax;
        Double tempmin;
        Double humidity;
        String weather;

        @Override
        protected Boolean doInBackground(String... params) {
            BufferedReader reader;
            StringBuilder buffer = new StringBuilder();
            String line;
            try {
                title = params[1];
                URL u = new URL(params[0]);
                HttpURLConnection h = (HttpURLConnection)u.openConnection();
                h.setRequestMethod("GET");
                h.setDoInput(true);
                h.connect();

                int response = h.getResponseCode();
                if (response == 200) {
                    reader = new BufferedReader(new InputStreamReader(h.getInputStream()));
                    while((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    //Start parsing JSON
                    JSONObject jWeather = new JSONObject(buffer.toString());
                    JSONObject jWind = jWeather.getJSONObject("wind");
                    JSONObject jTemp = jWeather.getJSONObject("main");
                    JSONArray whether1 = jWeather.getJSONArray("weather");
                    weather = whether1.getJSONObject(0).getString("main");

                    windSpeed = jWind.getDouble("speed");
                    temp = jTemp.getDouble("temp");
                    temp = temp - 273.15;
                    tempmax = jTemp.getDouble("temp_max");
                    tempmax = tempmax - 273.15;
                    tempmin = jTemp.getDouble("temp_min");
                    tempmin = tempmin -273.15;
                    humidity = jTemp.getDouble("humidity");
                    errorMsg = "";
                    return true;
                }
                else {
                    errorMsg = "HTTP Error";
                }
            } catch (MalformedURLException e) {
                Log.e("WeatherTask", "URL Error");
                errorMsg = "URL Error";
            } catch (IOException e) {
                Log.e("WeatherTask", "I/O Error");
                errorMsg = "I/O Error";
            } catch (JSONException e) {
                Log.e("WeatherTask", "JSON Error");
                errorMsg = "JSON Error";
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            TextView tvTitle, tvWeather, tvWind,tvTemp,tvHumid;
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            tvTitle = (TextView)findViewById(R.id.tvTitle);
            tvWeather = (TextView)findViewById(R.id.tvWeather);
            tvWind = (TextView)findViewById(R.id.tvWind);
            tvTemp = (TextView)findViewById(R.id.tvTemp);
            tvHumid = (TextView)findViewById(R.id.tvHumid);

            if (result) {
                tvTitle.setText(title);
                tvWind.setText(String.format("%.1f", windSpeed));
                tvTemp.setText(String.format("%.1f(max = %.1f,min = %.1f)", temp,tempmax,tempmin));
                tvHumid.setText(String.format("%.1f", humidity)+"%");
                tvWeather.setText(weather);

            }
            else {
                tvTitle.setText(errorMsg);
                tvWeather.setText("");
                tvWind.setText("");
            }
        }
    }
}