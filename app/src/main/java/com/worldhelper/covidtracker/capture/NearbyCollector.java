package com.worldhelper.covidtracker.capture;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.worldhelper.covidtracker.util.LocalVariables;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NearbyCollector {

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static boolean collect = false;

    private static File file;
    private static BufferedReader bufferedReader;
    private static BufferedWriter bufferedWriter;

    public static void initializeFile(Context context) {
        file = new File(context.getFilesDir() + "/nearby.txt");
        try {
            if (!file.exists()) {
                boolean result = file.createNewFile();
                System.out.println("()()()() RESULT : " + result);
            }
            System.out.println("()()()() PATH : " + file.getAbsolutePath());
            bufferedWriter = new BufferedWriter(new FileWriter(file, true));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static void runAlpha(FirebaseFirestore db, String trimmedLatitude, String trimmedLongitude) {
        db.collection("userdata").document(trimmedLatitude + ":" + trimmedLongitude)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        Map<String, Object> map = documentSnapshot.getData();
                        if (map != null) {
                            try {
                                BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
                                Date currentTime = Calendar.getInstance().getTime();
                                writer.write("" + currentTime.toString() + "::");
                                for (String key : map.keySet()) {
                                    writer.write(key + ",");
                                    System.out.println("()()()() ADDED == : " + key);
                                }
                                writer.write("\n");
                                writer.close();
                            } catch (Exception e) {
                                System.out.println("()()()() ERROR : " + e.getMessage());
                            }
                        }
                    }
                }
            }
        });

    }

    public static void startCapturing() {
        new Thread() {
            @Override
            public void run() {
                while (collect) {
                    try {
                        TimeUnit.SECONDS.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    NearbyCollector.runAlpha(db, LocalVariables.getTrimmedLat(), LocalVariables.getTrimmedLng());
                }
            }
        }.start();
    }

    public static void startCollector() {
        collect = true;
    }

    public static void stopCollector() {
        collect = false;
    }

}
