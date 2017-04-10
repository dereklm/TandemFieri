package com.gmail.dleemcewen.tandemfieri.Utility;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.gmail.dleemcewen.tandemfieri.Entities.DisplayItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Created by Derek on 4/9/2017.
 */

public class CsvUtil {

    public CsvUtil () {}

    public static File generateCsvFile(ArrayList<DisplayItem> items, Context context) {

        File csvFile = null;
        int count = 0;

        try {
            csvFile = new File(context.getFilesDir(), "tempCsv.csv");

            if (!csvFile.exists()) {
                boolean x = csvFile.createNewFile();
                boolean y = csvFile.mkdirs();
                Log.i("WRITER", "File didn't exist " + x + " " + y);
            }

            csvFile.deleteOnExit();

            FileOutputStream fos = new FileOutputStream(csvFile);
            OutputStreamWriter outWriter = new OutputStreamWriter(fos);
            outWriter.append("Name,Base Price,Quantity,Total\n");
            for (DisplayItem item: items) {
                outWriter.append(item.getName());
                outWriter.append(',');
                outWriter.append(item.getBasePrice() + "");
                outWriter.append(',');
                outWriter.append(item.getQuantity() + "");
                outWriter.append(',');
                outWriter.append(item.getTotal() + "");
                outWriter.append("\r\n");
                Log.i("WRITER", "wrote a line: " + ++count + " " + csvFile.getAbsolutePath().toString());
            }
            outWriter.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return csvFile;
    }

    public static void emailCsv(ArrayList<DisplayItem> items, String emailAddr, Context context) {
        if (items.isEmpty()) {
            Toast.makeText(context, "Please select some records.", Toast.LENGTH_SHORT).show();
            return;
        }
        File file = generateCsvFile(items, context);
        Uri path = FileProvider.getUriForFile(context, "com.gmail.dleemcewen.tandemfieri", file);
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("vnd.android.cursor.dir/email");
        String to[] = {emailAddr};
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        emailIntent.putExtra(Intent.EXTRA_STREAM, path);
        //emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+path));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Sales Report");
        context.startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }
}
