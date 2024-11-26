package jp.ac.meijou.s231205036.android.schedulestrengthcalendar;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HolidayApiFetcher {
    private static final String BASE_API_URL = "https://holidays-jp.github.io/api/v1/%d/date.json";
    private final Executor executor = Executors.newSingleThreadExecutor();

    public interface HolidayCallback {
        void onHolidayDataReceived(JSONObject holidayData);
        void onError(Exception e);
    }

    public void fetchHolidayData(int year, HolidayCallback callback) {
        executor.execute(() -> {
            try {
                // 指定した年をURLに埋め込む
                String apiUrl = String.format(BASE_API_URL, year);
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                // レスポンスコードの確認
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // JSONデータを作成
                    JSONObject holidayData = new JSONObject(response.toString());

                    // コールバックで結果を返す
                    callback.onHolidayDataReceived(holidayData);

                } else {
                    callback.onError(new Exception("Failed to fetch data: HTTP code " + responseCode));
                }
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
}
