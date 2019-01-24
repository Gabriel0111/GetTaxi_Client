package gabriel.gettaxi_client.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.util.Map;

import gabriel.gettaxi_client.R;
import gabriel.gettaxi_client.model.backend.ClientConst;
import gabriel.gettaxi_client.model.datasource.Firebase_DBManager;
import gabriel.gettaxi_client.model.entities.ClientRequest;

public class AddClientAsync extends AsyncTask<Object, Void, Void> {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference clientRef;
    ClientRequest clientRequest;
    Context context;


    /**
     * Calculate the distance between the distance between the departure and the arrival address and put the
     *      result into the ClientRequest object
     * @param args : Contains the ClientRequest and the context (used to show an AlertDialog)
     */
    @Override
    protected Void doInBackground(Object... args) {

        final ClientRequest clientCV = (ClientRequest) args[0];
        context = (Context) args[1];
        String key = clientCV.getPhoneNumber();

        Location clientSourceLocation = new Location("Client Source Location");
        Location clientDestinationLocation = new Location("Client Destination Location");

        clientSourceLocation.setLatitude(clientCV.getSourceLatitude());
        clientSourceLocation.setLongitude(clientCV.getSourceLongitude());

        clientDestinationLocation.setLatitude(clientCV.getDestinationLatitude());
        clientDestinationLocation.setLongitude(clientCV.getDestinationLongitude());

        double distance = clientSourceLocation.distanceTo(clientDestinationLocation);

      //  DecimalFormat df = new DecimalFormat("#.##");
        clientCV.setTravelDistance(Double.valueOf(distance));

        clientRef.child(key).setValue(clientCV)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                     @Override
                     public void onSuccess(Void aVoid) {
                         AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                         dialog.setTitle(R.string.app_name);
                         dialog.setMessage(context.getString(R.string.uploadSuccess) + '\n' + context.getString(R.string.from) + '\n' + clientCV.getSourceAddress() + '\n' + context.getString(R.string.to) + '\n' + clientCV.getDestinationAddress());
                         dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(context, MainActivity.class);
                                    context.startActivity(intent);
                                 ((Activity)context).finish();
                             }
                         });
                         dialog.show();
                     }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Uploading Failure. Try again", Toast.LENGTH_LONG).show();
                    }
                });

        return null;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        clientRef = database.getReference(ClientConst.CLIENTS);
    }

    @Override
    protected void onCancelled(Void aVoid) {
        super.onCancelled(aVoid);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

}
