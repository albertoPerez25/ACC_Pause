package com.alba.accpause.database;
import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;

import com.alba.accpause.ACCPause;

public class ProcessParser {
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
                output.append(line).append("\n");
                String[] parts = line.split("=");

                if(parts.length != 2)
                    continue; // Wrong file format

                data[i] = new Data();
                //data[i].id = i;
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

            // ACC Daemon status is printed with "accd,"
            process = Runtime.getRuntime().exec("su -c /dev/accd,");
            exitCode = process.waitFor();

            data[i] = new Data();
            data[i].key = "daemon_enabled";
            switch (exitCode){
                case 8:
                    data[i].value = "true";
                    break;
                default:
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
