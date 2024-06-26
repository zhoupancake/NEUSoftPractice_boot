package com.system.util;

import java.util.Map;

public class AQIUtil {
    private static final Map<String, int[]> AQI_range = Map.of(
            "pm25", new int[]{0, 35, 75, 115, 150, 250, 350, 500},
            "so2", new int[]{0, 50, 150, 475, 800, 1600, 2100, 2620},
            "co", new int[]{0, 2, 4, 14, 24, 36, 48, 60},
            "IAQI", new int[]{0, 50, 100, 150, 200, 300, 400, 500}
    );
    public static int getAQILevel(double pm25, double so2, double co) {
        int maxAQI = 0;
        double[] concentration = {so2, co, pm25};
        int[] concentrationArray = null;
        for(int i = 0; i < concentration.length;i++) {
            int j;
            switch (i){
                case 0:
                    concentrationArray = AQI_range.get("so2");
                    break;
                case 1:
                    concentrationArray = AQI_range.get("co");
                    break;
                case 2:
                    concentrationArray = AQI_range.get("pm25");
                    break;
                default:
                    break;
            }
            for (j = 0; j < concentrationArray.length; j++)
                if (concentration[i] < concentrationArray[j])
                    break;
            if (j > maxAQI)
                maxAQI = j;
        }
        return maxAQI;
    }

    public static int getAQI(double pm25, double so2, double co){
        int maxAQI = 0;
        double[] concentration = {so2, co, pm25};
        int[] concentrationArray = null;
        for(int i = 0; i < concentration.length;i++) {
            int IAQI = 0;
            switch (i){
                case 0:
                    concentrationArray = AQI_range.get("so2");
                    break;
                case 1:
                    concentrationArray = AQI_range.get("co");
                    break;
                case 2:
                    concentrationArray = AQI_range.get("pm25");
                    break;
                default:
                    break;
            }
            for (int j = 0; j < concentrationArray.length; j++)
                if (concentration[i] < concentrationArray[j]) {
                    IAQI = (int)Math.ceil((double) (AQI_range.get("IAQI")[j] - AQI_range.get("IAQI")[j - 1]) /
                            (concentrationArray[j] - concentrationArray[j - 1]) *
                            (concentration[i] - concentrationArray[j - 1]) +
                            AQI_range.get("IAQI")[j-1]);
                }
            if (IAQI > maxAQI)
                maxAQI = IAQI;
        }
        return maxAQI;
    }

    public static int AQILevel2value_pm25(int AQILevel){
        return AQI_range.get("pm25")[AQILevel-1];
    }

    public static int AQILevel2value_so2(int AQILevel){
        return AQI_range.get("so2")[AQILevel-1];
    }

    public static int AQILevel2value_co(int AQILevel){
        return AQI_range.get("co")[AQILevel-1];
    }
}
