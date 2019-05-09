package autotest.service;

import common.SystemInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class DataAnalysis {

    public static String[] extractSysbenchReport(String report) throws IOException {
        try {
            BufferedReader in = new BufferedReader(new FileReader(report));//open file ande create datastream
            String str;
            String[] result = new String[0];

            /*select data from resultlog_backup.txt*/
            while ((str = in.readLine()) != null) {
                if (str.startsWith("    transactions:")) {
                    if (str.length() > 0) {
                        result = Arrays.copyOf(result, result.length + 1);
                        int start = str.indexOf('(');
                        int end = str.indexOf('p');
                        result[result.length - 1] = str.substring(start + 1, end - 1);
                    }
                } else if (str.startsWith("FATAL:")) {
                    result = Arrays.copyOf(result, result.length + 1);
                    result[result.length - 1] = "0.00";
                    while ((str = in.readLine()) != null) {
                        if (str.startsWith("sysbench")) {
                            break;
                        }
                    }
                }//record crash result*/
            }
            in.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String[] extractTpccReport(String report) throws IOException {
        try {
            BufferedReader in = new BufferedReader(new FileReader(report));//open file ande create datastream
            String str;
            String[] result = new String[0];

            /*select data from resultlog_backup.txt*/
            while ((str = in.readLine()) != null) {
                if (str.endsWith("TpmC")) {
                    if (str.length() > 0) {
                        result = Arrays.copyOf(result, result.length + 1);
                        int start = 17;
                        int end = str.indexOf('T');
                        result[result.length - 1] = str.substring(start, end - 1);
                    }
                }
            }
            in.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static SystemInfo extractSystemInfo(ArrayList<String> info) {
        SystemInfo ret = new SystemInfo();
        String[] cpuInfoStr = info.get(1).split("[ ]+");
        Float[] cpuInfo = new Float[6];
        Float sum = new Float(0);
        for (int i = 0; i < 6; i++) {
            cpuInfo[i] = Float.parseFloat(cpuInfoStr[i+2]);
            sum += cpuInfo[i];
        }
        String cpuUtil = String.format("%.2f", (sum - cpuInfo[5]) / sum);
        ret.setCpuUtil(cpuUtil);
        String[] memInfoStr = info.get(3).split("[ ]+");
        ret.setMemUtil(memInfoStr[3]);
        int netIndex = 0;
        for (int i = 0; i < info.size(); i++) {
            if (info.get(i).indexOf("IFACE") != -1) {
                netIndex = i + 1;
                break;
            }
        }
        Float[] diskUtil = new Float[netIndex - 6];
        for (int i = 5; i < netIndex - 1; i++) {
            String[] diskInfoStr = info.get(i).split("[ ]+");
            diskUtil[i - 5] = Float.parseFloat(diskInfoStr[9]);
        }
        Float[] netUtil = new Float[info.size() - netIndex];
        for (int i = netIndex; i < info.size(); i++) {
            String[] netInfoStr = info.get(i).split("[ ]+");
            netUtil[i - netIndex] = Float.parseFloat(netInfoStr[9]);
        }
        Float maxDiskUtil = diskUtil[0];
        for (int i = 1; i < diskUtil.length; i++) {
            if (diskUtil[i] > maxDiskUtil) {
                maxDiskUtil = diskUtil[i];
            }
        }
        ret.setDiskUtil(String.format("%.2f", maxDiskUtil));
        Float maxNetUtil = diskUtil[0];
        for (int i = 1; i < netUtil.length; i++) {
            if (netUtil[i] > maxNetUtil) {
                maxNetUtil = netUtil[i];
            }
        }
        ret.setNetUtil(String.format("%.2f", maxNetUtil));
        return ret;
    }
}
