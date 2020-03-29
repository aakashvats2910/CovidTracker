package com.worldhelper.covidtracker.capture;

import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.worldhelper.covidtracker.LocationCollector;
import com.worldhelper.covidtracker.util.LocalVariables;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NewLocationCapture{

    private static Context context;

    private static String previousReference = "";

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static double latitude;
    private static double longitude;

    private static FusedLocationProviderClient fusedLocationProviderClient;
    private static LocationRequest locationRequest;
    private static LocationCallback locationCallback;

    NewLocationCapture() {}

    public static void setLatitude(double latitude) {
        NewLocationCapture.latitude = latitude;
    }

    public static void setLongitude(double longitude) {
        NewLocationCapture.longitude = longitude;
    }

    public static double getLatitude() {
        return latitude;
    }

    public static double getLongitude() {
        return longitude;
    }

    // Building location request.
    private static void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(0);
        locationRequest.setSmallestDisplacement(0);
    }

    // Building the LocationCallback.
    private static void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location: locationResult.getLocations()) {

                    String trimmedLatitude = "" + location.getLatitude();
                    String trimmedLongitude = "" + location.getLongitude();

                    try {
                        trimmedLatitude = trimmedLatitude.substring(0, 6);
                    } catch (Exception e) {
                        System.out.println("()()()() ERROR : " + e.getMessage());
                    }

                    try {
                        trimmedLongitude = trimmedLongitude.substring(0, 6);
                    } catch (Exception e) {
                        System.out.println("()()()() ERROR : " + e.getMessage());
                    }

                    LocalVariables.setTrimmedLat(trimmedLatitude);
                    LocalVariables.setTrimmedLng(trimmedLongitude);

                    if (!(trimmedLatitude + ":" + trimmedLongitude).equals(previousReference)) {
                        System.out.println("()()()() NOT-EQUAL");
                        System.out.println("()()()() " + previousReference + " PREV");
                        if (!previousReference.equals("")) deletePreviousPosition();
                        initiatePositionInDatabase(trimmedLatitude, trimmedLongitude);
                    }

                    System.out.println("()()()() " + trimmedLatitude + ":" + trimmedLongitude);

                }
            }
        };
    }

    // If we have to get location then this method is must to be called.
    public static void mandatoryForGettingLocation(Context context) {
        NewLocationCapture.context = context;
        buildLocationRequest();
        buildLocationCallback();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        startLocationService();
    }

    public static void startLocationService() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        try {
            NearbyCollector.startCapturing();
        } catch (Exception e) {
            System.out.println("()()()() ERROR : " + e.getMessage());
        }
    }

    public static void stopLocationService() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private static void initiatePositionInDatabase(String trimmedLatitude, String trimmedLongitude) {
        previousReference = trimmedLatitude + ":" + trimmedLongitude;
        LocalVariables.setDefaults("previousReference", previousReference, NewLocationCapture.context);
        Map<String, Object> docData = new HashMap<>();
        docData.put(FirebaseAuth.getInstance().getUid(),0);
        db.collection("userdata").document(
                trimmedLatitude + ":" + trimmedLongitude)
                .set(docData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        System.out.println("()()()() SUCCESS :: ");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("()()()() FAILURE :: " + e.getMessage());
                    }
                });
    }

    public static void deletePreviousPosition() {
        Map<String, Object> updates = new HashMap<>();
        updates.put(FirebaseAuth.getInstance().getUid(), FieldValue.delete());
        db.collection("userdata").document(previousReference).update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        System.out.println("()()()() UPDATE");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("()()()() UPDATEFAILED");
                    }
                });
    }

    public static void deleteByQuery() {
        final Map<String, Object> updates = new HashMap<>();
        updates.put(FirebaseAuth.getInstance().getUid(), FieldValue.delete());

        db.collection("userdata").whereEqualTo(FirebaseAuth.getInstance().getUid(), 0)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if (document.exists()) {
                            document.getReference().update(updates);
                        }
                    }
                }
            }
        });
    }

}