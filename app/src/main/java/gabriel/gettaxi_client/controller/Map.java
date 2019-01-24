package gabriel.gettaxi_client.controller;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Locale;

import gabriel.gettaxi_client.R;
import gabriel.gettaxi_client.model.backend.ClientConst;
import gabriel.gettaxi_client.model.entities.ClientRequest;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Map extends FragmentActivity implements LocationListener, View.OnClickListener {

    //region ***** ATTRIBUTES *****

    private static final int PERMS_CALL_ID = 1234;

    private LocationManager lm;
    private static MapFragment mapFragment;
    private GoogleMap googleMap;

    private TextView lbl_departureAddress;
    private Button sendPosition;
    private PlaceAutocompleteFragment destinationAddress;
    private Location departureLocation;
    private Location arrivalLocation;

    ClientRequest clientRequestIntent = null;

    //endregion

    void initializer()
    {
        lbl_departureAddress = (TextView) findViewById(R.id.lbl_depart);
        sendPosition = (Button) findViewById(R.id.sendPosition);
        sendPosition.setOnClickListener(this);
        sendPosition.setEnabled(false);

        Bundle bundle = getIntent().getExtras();
        if (bundle == null)
            Toast.makeText(this,"Impossible to receive the client's data", Toast.LENGTH_LONG).show();
        else
            clientRequestIntent = (ClientRequest) bundle.get(ClientConst.CLIENT);

        destinationAddress = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.destinationAddress);

        departureLocation = new Location("Departure Location");
        arrivalLocation = new Location("Destination Location");

        destinationAddress.setHint("Destination");
        destinationAddress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                sendPosition.setEnabled(true);
                googleMap.clear();
                arrivalLocation.setLatitude(place.getLatLng().latitude);
                arrivalLocation.setLongitude(place.getLatLng().longitude);

                String arrivalAddress = LocationToString(arrivalLocation);
                clientRequestIntent.setDestinationAddress(arrivalAddress);

                new ItineraireTask(Map.this, googleMap, lbl_departureAddress.getText().toString(), arrivalAddress).execute();
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(Map.this, status.getStatusCode() + "\n" + status.getStatusMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    //region ***** GEOLOCATION *****

    private void checkPermissions()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMS_CALL_ID);
        }

        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        }
        if (lm.isProviderEnabled(LocationManager.PASSIVE_PROVIDER))
        {
            lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 1000, 0, this);
        }
        if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
        }

        FragmentManager fragmentManager = getFragmentManager();
        mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map);
        loadMap();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMS_CALL_ID)
            checkPermissions();
    }

    @SuppressWarnings("MissingPermission")
    private void loadMap()
    {
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    Map.this.googleMap = googleMap;
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(departureLocation.getLatitude(), departureLocation.getLongitude()),3));
                    googleMap.setMyLocationEnabled(true);
                }
            });
        }
        else
            Toast.makeText(Map.this, "Impossible to find mapFragment", Toast.LENGTH_LONG).show();
    }

    /**
     * Change a location to a String
     */
    public String LocationToString(Location location)
    {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;

        try
        {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses.size() > 0)
                return addresses.get(0).getAddressLine(0);

            else return "NULL";
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return "IOException ...";
    }

    public Location StringToLocation(String locationString)
    {
        Geocoder gc = new Geocoder(this);
        Location location = new Location("location provider");

        if(gc.isPresent())
        {
            List<Address> list = null;
            try
            {
                list = gc.getFromLocationName(locationString, 1);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            Address address = list.get(0);

            location.setLatitude(address.getLatitude());
            location.setLongitude(address.getLongitude());
        }

        return location;
    }

    //region ***** FUNCTIONS LOCATION-LISTENER *****

    @Override
    public void onLocationChanged(Location location) {
        departureLocation = location;

        String departureAddress = LocationToString(location);
        clientRequestIntent.setSourceAddress(departureAddress);
        lbl_departureAddress.setText(departureAddress);

        //Toast.makeText(this, getPlace(location), Toast.LENGTH_LONG).show();
//        if (googleMap != null)
//        {
//            LatLng googleLocation = new LatLng(location.getLatitude(), location.getLongitude());
//            googleMap.moveCamera(CameraUpdateFactory.newLatLng(googleLocation));
//        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //endregion

    //endregion

    //region ***** ADMINISTRATION *****

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializesFont();

        Fragment_Map fragment_map = new Fragment_Map();
        getSupportFragmentManager().beginTransaction().replace(R.id.frameMap, fragment_map).commit();

        setContentView(R.layout.fragment_map);

        initializer();
    }

    @Override
    public void onClick(View v) {
        if (v == sendPosition)
        {
            if (arrivalLocation.getLongitude() != 0 && arrivalLocation.getLongitude() != 0)
            {
                clientRequestIntent.setSourceLatitude(departureLocation.getLatitude());
                clientRequestIntent.setSourceLongitude(departureLocation.getLongitude());
                clientRequestIntent.setDestinationLatitude(arrivalLocation.getLatitude());
                clientRequestIntent.setDestinationLongitude(departureLocation.getLongitude());

                AddClientAsync task = new AddClientAsync();
                task.execute(clientRequestIntent, Map.this);
            }
            else
                Toast.makeText(Map.this, "You must first enquire the destination field before continuing", Toast.LENGTH_LONG).show();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (lm != null)
            lm.removeUpdates(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    void initializesFont()
    {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("font/arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
    }

    //endregion
}

