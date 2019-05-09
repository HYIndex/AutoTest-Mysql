package autotest;

import java.io.IOException;

import static autotest.service.DataAnalysis.extractTpccReport;

public class DataAnalysisTest {
    public static void main(String[] args) {
        try {
            String[] res = extractTpccReport("C:\\Users\\HYIndex\\Downloads\\AutoTest-Mysql\\src\\main\\resources\\autotest\\testreport\\tpcc_10_20190505053023.log");
            System.out.println(res.length);
            for (int i = 0; i < res.length; i++) {
                System.out.println(res[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
