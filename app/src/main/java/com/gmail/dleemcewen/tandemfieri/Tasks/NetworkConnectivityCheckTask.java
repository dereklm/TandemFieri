package com.gmail.dleemcewen.tandemfieri.Tasks;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.AbstractMap;
import java.util.Map;

/**
 * NetworkConnectivityCheckTask defines a task that can be used to check for network connectivity
 */

public class NetworkConnectivityCheckTask implements Continuation<Void, Task<Map.Entry<Boolean, DatabaseError>>> {
    private Context context;

    /**
     * Default constructor
     * @param context indicates the current application context
     */
    public NetworkConnectivityCheckTask(Context context) {
        this.context = context;
    }

    @Override
    public Task<Map.Entry<Boolean, DatabaseError>> then(@NonNull Task<Void> task) throws Exception {
        final TaskCompletionSource<Map.Entry<Boolean, DatabaseError>> taskCompletionSource = new TaskCompletionSource<>();

        ConnectivityManager connectivityManager =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean connected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (connected) {
            Map.Entry<Boolean, DatabaseError> result =
                    new AbstractMap.SimpleEntry<>(true, null);
            taskCompletionSource.setResult(result);
        }
        else {
            DatabaseError databaseError = DatabaseError.fromException(new Exception("Error accessing network"));
            Map.Entry<Boolean, DatabaseError> errorResult =
                    new AbstractMap.SimpleEntry<>(false, databaseError);
            taskCompletionSource.setResult(errorResult);
        }

        return taskCompletionSource.getTask();
    }
}

