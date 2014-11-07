package gr.ammar.gpstransmitter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.content.Context;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;


public class MainActivity extends Activity implements LocationListener
{
   int locationsRegistered=0;
   float latitude=0;
   float longitude=0;
   float speed =0;
   float bearing = 0;
   float altitude=0;
   String provider="http://ammar.gr:8081/";

    public String getMyPhoneIMEI()
    {
        return ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
    }


    public String getMyPhoneNumber()
    {
        return ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getLine1Number();
    }


    public void OpenGeoPosLocationMapBrowser()
    {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(provider+"geolocation.html?mlat="+latitude+"&mlon="+longitude));
        startActivity(browserIntent);
    }

    public void OpenOpenStreetMapBrowser()
    {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.openstreetmap.org/?mlat="+latitude+"&mlon="+longitude));
        startActivity(browserIntent);
    }

    public static String getHttpResponse(URI uri) {
        Log.d("Geo_Location", "Going to make a get request");
        StringBuilder response = new StringBuilder();
        try {
            HttpGet get = new HttpGet();
            Log.d("demo", "Setting URI");
            get.setURI(uri);
            Log.d("demo", "Creating httpClient ");
            DefaultHttpClient httpClient = new DefaultHttpClient();
            Log.d("demo", "Executing request");

            HttpResponse httpResponse = httpClient.execute(get);
            Log.d("demo", "Got Status Code "+httpResponse.getStatusLine().getStatusCode());
            if (httpResponse.getStatusLine().getStatusCode() == 200)
            {
                Log.d("demo", "HTTP Get succeeded");

                HttpEntity messageEntity = httpResponse.getEntity();
                InputStream is = messageEntity.getContent();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = br.readLine()) != null)
                {
                    response.append(line);
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();

            Log.e("demo",  ("Got Exception Msg : "+e.getMessage() ) );
        }
        Log.d("demo", "Done with HTTP getting");
        return response.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    public boolean transmitLocation()
    {
        TextView t = new TextView(this);
        t = (TextView) findViewById(R.id.GPSStatus);

        if (locationsRegistered > 0)
        {
            String locationID = " Latitude: " + latitude + "\n Longitude: " + longitude + "\n Speed: " + speed + "\n Bearing: " + bearing + "\n Altitude: " + altitude + "\n\n ";
            String phoneID = getMyPhoneNumber() + "_" + getMyPhoneIMEI();

            t.setText((" Transmitting...\n" + locationID));
            URI uri = URI.create((provider + "gps.html?lat=" + latitude + "&lon=" + longitude + "&from=" + phoneID + "&speed=" + speed + "&bearing=" + bearing + "&msg=android"));
            getHttpResponse(uri);
            t.setText((" Transmitted...\n" + locationID));
            return true;
        } else
        {
            t.setText(("No GPS Location to transmit!\n"));
        }
       return false;
    }


    public void buttonViewClicked(View view)
    {
        OpenOpenStreetMapBrowser();
    }

    public void buttonShareClicked(View view)
    {
        if ( transmitLocation() )
        {
            OpenGeoPosLocationMapBrowser();
        }
    }


    @Override
    public void onLocationChanged(Location location)
    {
         ++locationsRegistered;
         speed = location.getSpeed();
         latitude = (float) (location.getLatitude());
         longitude = (float) (location.getLongitude());
         bearing= location.getBearing();
         altitude= (float) location.getAltitude();

        String locationID=" Latitude: " + latitude + "\n Longitude: " + longitude+"\n Speed: "+speed+"\n Bearing: "+bearing+"\n Altitude: "+altitude+"\n\n ";

        CheckBox autoRefresh = (CheckBox)findViewById(R.id.checkBoxAutoShare);
        if ( autoRefresh.isChecked() )
        {
            transmitLocation();
        } else
        {
            TextView t=new TextView(this);
            t=(TextView)findViewById(R.id.GPSStatus);
            t.setText((" Offline...\n"+locationID));
        }

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




    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }
}

