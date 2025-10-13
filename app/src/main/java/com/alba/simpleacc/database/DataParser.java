package com.alba.simpleacc.database;
import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.alba.simpleacc.ACCPause;
import com.alba.simpleacc.batData.BatteryInfoParser;

public class DataParser {
    public static int updateConfigsDatabase(String command, Context context) {
        try {
            Process process = Runtime.getRuntime().exec("su -c "+command);

            int exitCode = process.waitFor(); // Wait for the process to finish

            // Read the output of the process
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            //Read the error stream.
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();
            Data [] data = new Data[99];
            int i = 0;

            // Read the output
            while ((line = reader.readLine()) != null) {
                if (!line.contains("="))
                    continue;
                output.append(line).append("\n");
                String[] parts = {"",""};
                String[] split = line.split("=");

                for(int j = 0; j < 2; j++){
                    if (j >= split.length)
                        parts[j] = "0";
                    else
                        parts[j] = split[j];
                }

                data[i] = new Data();
                data[i].id = i;
                data[i].key = parts[0];
                data[i].value = parts[1];
                i++;
            }

            // Read the error output
            while((line = errorReader.readLine()) != null){
                errorOutput.append(line).append("\n");
            }

            if(exitCode != 0){
                // Process exited with an error. Handle the error output
                android.util.Log.e("ExternalProcess", "Error exit code: " + exitCode);
                android.util.Log.e("ExternalProcess", "Error output:\n" + errorOutput);
                return exitCode;
            }

            // Process finished successfully. Parse the output
            ACCPause application = (ACCPause) context;
            DataDao dataDao = application.getDataDao();
            i = 0;

            while (data[i] != null){
                dataDao.insert(data[i]);
                i++;
            }

            // ACC Daemon status
            BatteryInfoParser batInfo = new BatteryInfoParser();

            data[i] = new Data();
            data[i].id = i;
            data[i].key = "daemon_enabled";
            if (batInfo.getDaemonStatus()) {
                data[i].value = "true";
            } else {
                data[i].value = "false";
            }

            dataDao.insert(data[i]);

            return 0;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return 100; // Other error
        }
    }
}
