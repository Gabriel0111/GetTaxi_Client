package gabriel.gettaxi_client.model.datasource;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import gabriel.gettaxi_client.model.backend.ClientConst;
import gabriel.gettaxi_client.model.entities.ClientRequest;
import gabriel.gettaxi_client.model.entities.ClientRequestStatus;

/**
 * NOT USED IN THIS PROJECT
 */

public class Firebase_DBManager {
   // @Override
    public void addRequest(ClientRequest clientRequest, ContentValues contentValues) {

    }

    //region ***** INTERFACES *****

    public interface Action<T>
    {
        void onSuccess(T obj);
        void onFailure(Exception exception);
        void onProgress(String status, double percent);
    }

    public interface NotifyDataChange<T>
    {
        void onDataChange(T obj);
        void onFailure(Exception exception);
    }

    //endregion

    static DatabaseReference clientsRef;

    static
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        clientsRef = database.getReference("Clients");
    }

    static public ContentValues ClientToContentValue(ClientRequest clientRequest)
    {
        ContentValues contentValues = new ContentValues();

        contentValues.put(ClientConst.NAME, clientRequest.getClientName());
        contentValues.put(ClientConst.PHONE_NUMBER, clientRequest.getPhoneNumber());
        contentValues.put(ClientConst.EMAIL, clientRequest.getEmail());
        contentValues.put(ClientConst.STATUS, clientRequest.getClientRequestStatus().toString());
        contentValues.put(ClientConst.SOURCE_LONGITUDE,clientRequest.getSourceLongitude());
        contentValues.put(ClientConst.SOURCE_LATITUDE, clientRequest.getSourceLatitude());
        contentValues.put(ClientConst.DESTINATION_LONGITUDE, clientRequest.getDestinationLongitude());
        contentValues.put(ClientConst.DESTINATION_LATITUDE,  clientRequest.getDestinationLatitude());
        contentValues.put(ClientConst.TIME_DEPART, clientRequest.getDepartureTime().toString());
        contentValues.put(ClientConst.TIME_ARRIVAL, clientRequest.getArrivalTime().toString());

        return  contentValues;
    }

    static public ClientRequest ContentValueToClient(ContentValues contentValues) throws ParseException {
        ClientRequest c = new ClientRequest();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");

        c.setClientName(contentValues.getAsString(ClientConst.NAME));
        c.setPhoneNumber(contentValues.getAsString(ClientConst.PHONE_NUMBER));
        c.setEmail(contentValues.getAsString(ClientConst.EMAIL));
        c.setClientRequestStatus(ClientRequestStatus.valueOf(contentValues.getAsString(ClientConst.STATUS)));
        c.setSourceLongitude(contentValues.getAsDouble(ClientConst.SOURCE_LONGITUDE));
        c.setSourceLatitude(contentValues.getAsDouble(ClientConst.SOURCE_LATITUDE));
        c.setDestinationLongitude(contentValues.getAsDouble(ClientConst.DESTINATION_LONGITUDE));
        c.setDestinationLatitude(contentValues.getAsDouble(ClientConst.DESTINATION_LATITUDE));

        try
        {
            c.setDepartureTime(formatter.parse(contentValues.getAsString(ClientConst.TIME_DEPART)));
            c.setArrivalTime(formatter.parse(contentValues.getAsString(ClientConst.TIME_ARRIVAL)));
        }
        catch (ParseException e)
        {
            c.setDepartureTime(new Date(2018,11,1,16,0));
            c.setArrivalTime(new Date(2018,11,1,17,30));
        }

        return c;
    }

    private static void addClientToFirebase(final ContentValues client, final Action<String> action)
    {
        final String key = client.getAsString(ClientConst.EMAIL);
        clientsRef.child(key).setValue(client)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                action.onSuccess(key);
                action.onProgress("Upload successful", 100);
            }
        })  .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                action.onFailure(e);
                action.onProgress("Failure in uploading data", 100);
            }
        });
    }

//    private static void addClient(final ClientRequest client, final Action<String> action)
//    {
//        if(client.getImage() != null)
//        {
//            StorageReference imageRef = FirebaseStorage.getInstance().getReference();
//            imageRef = imageRef.child("Images").child(System.currentTimeMillis() + ".jpg");
//
//            imageRef.putFile(client.getImage())
//                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
//                        {
//                            action.onProgress("Uploading Client Data", 90);
//                            Uri downloadUri = taskSnapshot.getUploadSessionUri();
//                            client.setImage(downloadUri);
//
//                            addClientToFirebase(client, action);
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            action.onFailure(e);
//                            action.onProgress("Failure in uploading picture up to the server", 100);
//                        }
//                    })
//                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//                            double uploadBytes = taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
//                            double progress = (90.0 * uploadBytes);
//                            action.onProgress("Uploading file...", progress);
//                        }
//                    });
//        }
//        else
//            action.onFailure(new Exception("Any picture selected"));
//    }

}
