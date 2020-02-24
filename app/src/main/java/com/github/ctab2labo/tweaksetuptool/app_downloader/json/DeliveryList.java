package com.github.ctab2labo.tweaksetuptool.app_downloader.json;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DeliveryList {
    public int list_version;
    public List<AppInfo> app_list;

    public int getListVersion() {
        return list_version;
    }

    public ArrayList<AppInfo> getAppList() {
        return (ArrayList<AppInfo>) app_list;
    }

    public static DeliveryList importFromFile(File path) throws FileNotFoundException {
        // ファイルを読み取る
        FileInputStream inputStream = new FileInputStream(path);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        StringWriter writer = new StringWriter();
        int data;
        try {
            while ((data = inputStreamReader.read()) != -1) {
                writer.write(data);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error Read DeliveryFile.");
        } finally {
            try {
                inputStreamReader.close();
            } catch (IOException e) {/* 無視 */}
        }

        // JSONを読み取る
        Gson gson = new Gson();
        DeliveryList deliveryList = gson.fromJson(writer.toString(), DeliveryList.class);
        return deliveryList;
    }
}
