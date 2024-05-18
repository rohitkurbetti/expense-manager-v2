package com.example.myapplication;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InvoiceConstants {


    public static final String[] EMAIL_RECIPIENTS = {
            "rkurbetti30@gmail.com",
            "sumeetkurbetti04@gmail.com",
            "rohitbackup47@gmail.com",
            "rohitbackup0001@gmail.com",
            "nitiningole1996@gmail.com"
    };
    public static Map<String, Integer> ITEM_PRICE_MAP = new HashMap<>();




    static {

        Map<String, Integer> priceMap = new HashMap<>();

        priceMap.put("KOKAM", InvoiceConstants.KOKAM_RATE);
        priceMap.put("ORANGE", InvoiceConstants.ORANGE_RATE);
        priceMap.put("L. ORANGE", InvoiceConstants.LIMBU_ORANGE_RATE);
        priceMap.put("L. LEMON", InvoiceConstants.LIMBU_LEMON_RATE);
        priceMap.put("PACHAK", InvoiceConstants.PACHAK_RATE);
        priceMap.put("SARBAT", InvoiceConstants.SARBAT_RATE);
        priceMap.put("S SARBAT", InvoiceConstants.SODA_SARBAT_RATE);
        priceMap.put("WALA", InvoiceConstants.WALA_RATE);
        priceMap.put("J. SODA", InvoiceConstants.JEERA_SODA_RATE);
        priceMap.put("L. SODA", InvoiceConstants.LIMBU_SODA);
        priceMap.put("STWBRY SODA", InvoiceConstants.STRAWBERRY_SODA);
        priceMap.put("WATER_H", InvoiceConstants.WATER_500ML_RATE);
        priceMap.put("WATER_F", InvoiceConstants.WATER_1L_RATE);
        priceMap.put("LASSI_H", InvoiceConstants.LASSI_HALF_RATE);
        priceMap.put("LASSI_F", InvoiceConstants.LASSI_FULL_RATE);
        priceMap.put("MNG_LSSI_H", InvoiceConstants.MANGO_LASSI_HALF_RATE);
        priceMap.put("MNG_LSSI_F", InvoiceConstants.MANGO_LASSI_FULL_RATE);
        priceMap.put("TAAK", InvoiceConstants.TAAK_RATE);
        priceMap.put("KULFI", InvoiceConstants.KULFI_RATE);
        priceMap.put("BTRSCH", InvoiceConstants.BTRSCH_RATE);


        ITEM_PRICE_MAP = Collections.unmodifiableMap(priceMap);

    }

    private static final int KOKAM_RATE = 20;
    private static final int ORANGE_RATE = 20;
    private static final int LIMBU_ORANGE_RATE = 25;
    private static final int LIMBU_LEMON_RATE = 25;
    private static final int PACHAK_RATE = 20;
    private static final int SARBAT_RATE = 20;
    private static final int SODA_SARBAT_RATE = 20;
    private static final int WALA_RATE = 20;
    private static final int JEERA_SODA_RATE = 20;
    private static final int LIMBU_SODA = 20;
    private static final int STRAWBERRY_SODA = 20;
    private static final int WATER_500ML_RATE = 10;
    private static final int WATER_1L_RATE = 20;
    private static final int LASSI_HALF_RATE = 20;
    private static final int LASSI_FULL_RATE = 40;
    private static final int MANGO_LASSI_HALF_RATE = 25;
    private static final int MANGO_LASSI_FULL_RATE = 50;
    private static final int TAAK_RATE = 10;
    private static final int KULFI_RATE = 20;
    private static final int BTRSCH_RATE = 20;






}
