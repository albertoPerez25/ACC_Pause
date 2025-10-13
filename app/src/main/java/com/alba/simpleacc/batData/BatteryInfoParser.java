package com.alba.simpleacc.batData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class BatteryInfoParser {
    private HashMap<String,String> batInfo = new HashMap<>();
    private final String cmdInfoBat;
    private final String cmdDaemonStatus;

    public BatteryInfoParser(){
        this.cmdInfoBat = "/dev/acca -i";
        this.cmdDaemonStatus = "/dev/accd,";
    }

    public BatteryInfoParser(String cmdInfoBat, String cmdDaemonStatus){
        this.cmdInfoBat = cmdInfoBat;
        this.cmdDaemonStatus = cmdDaemonStatus;
    }

    public HashMap<String,String> getBatteryInfo() {
        try {
            Process process = Runtime.getRuntime().exec("su -c "+ cmdInfoBat);

            int exitCode = process.waitFor(); // Wait for the process to finish

            // Read the output of the process
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            //Read the error stream.
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();

            // Read the output
            while ((line = reader.readLine()) != null) {
                if (!line.contains(" "))
                    continue;
                output.append(line).append("\n");
                String[] parts = {"",""};
                String[] split = line.split(" ");

                for(int j = 0; j < 2; j++){
                    if (j >= split.length)
                        parts[j] = "0";
                    else
                        parts[j] = split[j];
                }

                batInfo.put(parts[0],parts[1]);
            }

            // Read the error output
            while((line = errorReader.readLine()) != null){
                errorOutput.append(line).append("\n");
            }

            if(exitCode != 0){
                // Process exited with an error. Handle the error output
                android.util.Log.e("ExternalProcess", "Error exit code: " + exitCode);
                android.util.Log.e("ExternalProcess", "Error output:\n" + errorOutput);
                return batInfo; //TODO
            }

            // Process finished successfully. Return the output

            return batInfo;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return batInfo; // Other error //TODO
        }
    }
    public Boolean getDaemonStatus() {
        // ACC Daemon status is printed with "accd,"
        Process process = null;
        boolean daemonEnabled = false;

        try {
            process = Runtime.getRuntime().exec("su -c "+cmdDaemonStatus);

            int exitCode = process.waitFor();

            daemonEnabled = exitCode == 0;

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return daemonEnabled;
    }
}
