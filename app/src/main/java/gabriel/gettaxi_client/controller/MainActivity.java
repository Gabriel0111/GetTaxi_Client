package gabriel.gettaxi_client.controller;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.*;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.File;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import gabriel.gettaxi_client.R;
import gabriel.gettaxi_client.model.backend.ClientConst;
import gabriel.gettaxi_client.model.entities.ClientRequest;
import gabriel.gettaxi_client.model.entities.ClientRequestStatus;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends Activity implements View.OnClickListener {

    // ***** Properties *****

    final String ACTIVITY_LIFE_TAG = "GetTaxi_Client";

    private Button registerButton;
    private RelativeLayout layout;
    private MaterialEditText edtName;
    private MaterialEditText edtEmail;
    private MaterialEditText edtPhoneNumber;
    private MaterialEditText edtDepartTime;
    private MaterialEditText edtArrivalTime;

    private Bundle saveDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializesFont();
        setContentView(R.layout.activity_splash);
        Log.i(ACTIVITY_LIFE_TAG, "Hello World");

        initialize();
    }

    void initializesFont()
    {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("font/arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
    }

    void initialize()
    {
        askPermissions();
        findViewMain();
    }

    /**
     * Ask permissions for the LOCATION access, and vibrate feature
     */
    void askPermissions()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED);
        {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.VIBRATE},
                    1);
        }
    }

    /**
     * Initializes the views
     */
    void findViewMain()
    {
        layout = (RelativeLayout) findViewById(R.id.splashView);
        registerButton = (Button) findViewById(R.id.btnRegister);
        registerButton.setOnClickListener(this);
        saveDialog = new Bundle();
    }

    @Override
    public void onClick(View v) {
        if (v == registerButton)
            showRegisterDialog();
    }

    // If the Register button was clicked...

    void findViewRegister(View registerLayout)
    {
        edtName = registerLayout.findViewById(R.id.name);
        edtEmail = registerLayout.findViewById(R.id.email);
        edtPhoneNumber = registerLayout.findViewById(R.id.phoneNumber);
        edtDepartTime = registerLayout.findViewById(R.id.departure);
        edtArrivalTime = registerLayout.findViewById(R.id.arrival);
    }

    /**
     * Show the register dialog
     */
    private void showRegisterDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.register);
        dialog.setMessage(R.string.informationAboutTravel);

        LayoutInflater inflater = LayoutInflater.from(this);
        View registerLayout = inflater.inflate(R.layout.activity_register, null);

        findViewRegister(registerLayout);
        dialog.setView(registerLayout);

        //Set button
        dialog.setPositiveButton(R.string.next, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                saveData();
                dialog.dismiss();

                if (checkFields() == -1) return;
                else sendClientToMap();
            }
        });

        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveData();
                dialog.dismiss();
            }
        });

        showData();
        dialog.show();
    }

    /**
     * This function is used to to pass to the the next screen.
     * Makes checking, modifies fields in case of need.
     * If the checks are not conclusive, cancel the registration and print a message thanks to the Snapbar
     */
    void sendClientToMap()
    {
        ClientRequest clientRequest = new ClientRequest();

        clientRequest.setClientName(edtName.getText().toString());
        clientRequest.setPhoneNumber(edtPhoneNumber.getText().toString());
        clientRequest.setEmail(edtEmail.getText().toString());
        clientRequest.setClientRequestStatus(ClientRequestStatus.AWAITING);

        Date departureDate = new Date();
        Date arrivalDate = new Date();

        Calendar cal = Calendar.getInstance(Locale.getDefault());
        int hours = cal.get(Calendar.HOUR_OF_DAY);
        int minuts = cal.get(Calendar.MINUTE);

        String departureStringTab[] = {String.valueOf(hours), String.valueOf(minuts)};
        String arrivalStringTab[] = {String.valueOf(hours + 1), String.valueOf(minuts)};

        departureStringTab = checkTimeAndModify(edtDepartTime.getText().toString());
        arrivalStringTab = checkTimeAndModify(edtArrivalTime.getText().toString());


        //region ***** CheckTime *****
        if (Integer.parseInt(departureStringTab[0]) > 23 ||
                Integer.parseInt(departureStringTab[0]) < 0 ||
                Integer.parseInt(departureStringTab[1]) > 59 ||
                Integer.parseInt(departureStringTab[1]) < 0) {
            Snackbar.make(layout, R.string.correctDepartureTime, Snackbar.LENGTH_LONG).show();
            return;
        }
        else if(Integer.parseInt(arrivalStringTab[0]) > 23 ||
                Integer.parseInt(arrivalStringTab[0]) < 0 ||
                Integer.parseInt(arrivalStringTab[1]) > 59 ||
                Integer.parseInt(arrivalStringTab[1]) < 0)
        {
            Snackbar.make(layout, R.string.correctArrivalTime, Snackbar.LENGTH_LONG).show();
            return;
        }

        else if (Integer.parseInt(departureStringTab[0]) >= Integer.parseInt(arrivalStringTab[0]))
        {
            if (Integer.parseInt(departureStringTab[0]) == Integer.parseInt(arrivalStringTab[0]))
            {
                if (Integer.parseInt(departureStringTab[1]) > Integer.parseInt(arrivalStringTab[1])) {
                    Snackbar.make(layout, R.string.arrivalLessDeparture, Snackbar.LENGTH_LONG).show();
                    return;
                } else if (Integer.parseInt(departureStringTab[1]) == Integer.parseInt(arrivalStringTab[1])) {
                    Snackbar.make(layout, R.string.arrivalEqualsDeparture, Snackbar.LENGTH_LONG).show();
                    return;
                }
            }
            else
            {
                Snackbar.make(layout, R.string.arrivalLessDeparture, Snackbar.LENGTH_LONG).show();
                return;
            }
        }
        //endregion

        departureDate.setHours(Integer.parseInt(departureStringTab[0]));
        departureDate.setMinutes(Integer.parseInt(departureStringTab[1]));

        arrivalDate.setHours(Integer.parseInt(arrivalStringTab[0]));
        arrivalDate.setMinutes(Integer.parseInt(arrivalStringTab[1]));

        clientRequest.setDepartureTime(departureDate);
        clientRequest.setArrivalTime(arrivalDate);

        Intent intent = new Intent(MainActivity.this, Map.class);
        intent.putExtra(ClientConst.CLIENT, clientRequest);
        startActivity(intent);
    }

    //region ***** CHECK DATA *****

    /**
     * Modifies time.
     * For example, if it written 21, transforms it to 21:00
     * @param timeString
     * @return
     */
    String[] checkTimeAndModify (String timeString)
    {
        String[] timeStringTab = new String[2];

        if (timeString.indexOf(':') > 0 )
        {
            if (timeString.indexOf(':') + 1 != timeString.length())
            {
                timeStringTab = timeString.split("[:]");
            }
            else
            {
                timeStringTab[0] = timeString.substring(0, timeString.indexOf(':'));
                timeStringTab[1] = "00";
            }
        }
        else
        {
            if (Character.getNumericValue(timeString.charAt(0)) >= 1
                    && Character.getNumericValue(timeString.charAt(0)) <= 2
                    && timeString.length() > 3)
            {
                timeStringTab[0] = timeString.charAt(0) + "" + timeString.charAt(1);
                timeStringTab[1] = timeString.charAt(2) + "" + timeString.charAt(3);
            }
            else if (timeString.length() == 2)
            {
                timeStringTab[0] = timeString.charAt(0) + "" + timeString.charAt(1);
                timeStringTab[1] = "00";
            }
            else if (timeString.length() == 1)
            {
                timeStringTab[0] = timeString.charAt(0) + "";
                timeStringTab[1] = "00";
            }

            else if (timeString.length() <= 3)
            {
                timeStringTab[0] = timeString.charAt(0) + "";
                timeStringTab[1] = timeString.charAt(1) + "" + timeString.charAt(2);
            }
        }

        return timeStringTab;
    }

    /**
     * Checks if the fields are not empty and correctly filled
     * @return : -1 -> ERROR -> CANCEL THE REGISTRATION
     */
    int checkFields()
    {
        if (TextUtils.isEmpty(edtName.getText().toString()))
        {
            Snackbar.make(layout, R.string.enterAName, Snackbar.LENGTH_LONG).show();
            return -1;
        }
        else
        {
            String name = edtName.getText().toString().toLowerCase();
            String capNames[] = name.split(" ");

            String result = "";

            for (String n: capNames) {
                result += n.substring(0,1).toUpperCase() + n.substring(1) + " ";
            }

            edtName.setText(result);
        }

        if (TextUtils.isEmpty(edtEmail.getText().toString()))
        {
            Snackbar.make(layout, R.string.enterEmail, Snackbar.LENGTH_LONG).show();
            return -1;
        }

        if (TextUtils.isEmpty(edtPhoneNumber.getText().toString()))
        {
            Snackbar.make(layout, R.string.enterPN, Snackbar.LENGTH_LONG).show();
            return -1;
        }

        if (TextUtils.isEmpty(edtDepartTime.getText().toString()))
        {
            Snackbar.make(layout, R.string.enterDT, Snackbar.LENGTH_LONG).show();
            return -1;
        }

        if (TextUtils.isEmpty(edtArrivalTime.getText().toString()))
        {
            Snackbar.make(layout, R.string.enterAT, Snackbar.LENGTH_LONG).show();
            return -1;
        }

        if (!edtEmail.getText().toString().matches(
                "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"))
        {
            Snackbar.make(layout, R.string.correctMail, Snackbar.LENGTH_LONG).show();
            return -1;
        }

        if (!edtPhoneNumber.getText().toString().matches("(?:(?:\\+|00)972|0)\\s*[1-9](?:[\\s.-]*\\d{2}){4}"))
        {
            Snackbar.make(layout, R.string.correctPN, Snackbar.LENGTH_LONG).show();
            return -1;
        }

        return 0;
    }

    //endregion

    //region ***** ADMINISTRATION *****

    /**
     * Use to modify the font
     * @param newBase
     */
    @Override
    protected void attachBaseContext(Context newBase)
    {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    /**
     * When the user canceled his registration and wants to re-register,
     * the former data is shown for his.
     */
    void showData()
    {
        if (saveDialog != null)
        {
            edtName.setText(saveDialog.getString(ClientConst.NAME));
            edtEmail.setText(saveDialog.getString(ClientConst.EMAIL));
            edtPhoneNumber.setText(saveDialog.getString(ClientConst.PHONE_NUMBER));
            edtDepartTime.setText(saveDialog.getString(ClientConst.TIME_DEPART));
            edtArrivalTime.setText(saveDialog.getString(ClientConst.TIME_ARRIVAL));
        }
    }

    /**
     * Save data when he canceled
     */
    void saveData()
    {
        saveDialog.putString(ClientConst.NAME, edtName.getText().toString());
        saveDialog.putString(ClientConst.EMAIL, edtEmail.getText().toString());
        saveDialog.putString(ClientConst.PHONE_NUMBER, edtPhoneNumber.getText().toString());
        saveDialog.putString(ClientConst.TIME_DEPART, edtDepartTime.getText().toString());
        saveDialog.putString(ClientConst.TIME_ARRIVAL, edtArrivalTime.getText().toString());
    }

    //endregion

}