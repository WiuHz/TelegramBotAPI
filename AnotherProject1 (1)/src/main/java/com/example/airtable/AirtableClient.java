package com.example.airtable;

import com.example.util.Constant;
import okhttp3.*;
import java.io.IOException;

public class AirtableClient {
    private final OkHttpClient client = new OkHttpClient();

    public String postToTable(String tableName, String jsonData) {
        String url = Constant.AIRTABLE_API_URL + tableName;

        RequestBody body = RequestBody.create(jsonData, MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer " + Constant.AIRTABLE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error details";
                System.out.println("Failed to record: " + response.code() + ", Error: " + errorBody);
                return null;
            }
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getFromTable(String tableName) {
        String url = Constant.AIRTABLE_API_URL + tableName;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + Constant.AIRTABLE_API_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error details";
                System.out.println("Request Failed: " + response.code() + ", Error: " + errorBody);
                return null;
            }
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String patchToTable(String tableName, String jsonData) {
        String url = Constant.AIRTABLE_API_URL + tableName;

        RequestBody body = RequestBody.create(jsonData, MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .method("PATCH", body)
                .addHeader("Authorization", "Bearer " + Constant.AIRTABLE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error details";
                System.out.println("Failed to update: " + response.code() + ", Error: " + errorBody);
                return null;
            }
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteFromTable(String tableName, String recordId) {
        String url = Constant.AIRTABLE_API_URL + tableName + "/" + recordId;

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .addHeader("Authorization", "Bearer " + Constant.AIRTABLE_API_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error details";
                System.out.println("Failed to delete record: " + response.code() + ", Error: " + errorBody);
                return false;
            }
            System.out.println("Successfully deleted record: " + recordId);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}