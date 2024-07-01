package com.dzakysalman.cuaca;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity
{
    private RecyclerView _recyclerView1;
    private SwipeRefreshLayout _swipeRefreshLayout1;
    private TextView _textViewCityInfo;
    private TextView _textViewTotalRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _recyclerView1 = (RecyclerView)findViewById(R.id.recyclerView1);
        _swipeRefreshLayout1 = findViewById(R.id.swipeRefreshLayout1);
        _textViewCityInfo = findViewById(R.id.textView_cityInfo);
        _textViewTotalRecord = findViewById(R.id.textView_totalRecords);

        initRecyclerView1();
        initSwipeRefreshLayout();
    }

    private void initSwipeRefreshLayout() {
        _swipeRefreshLayout1.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                initRecyclerView1();
                _swipeRefreshLayout1.setRefreshing(false);
            }
        });
    }

    private void initRecyclerView1() {
        String url = "https://api.openweathermap.org/data/2.5/forecast?id=1630789&appid=91ad02eb393ead013a48f7615e09936d";
        AsyncHttpClient ahc = new AsyncHttpClient();

        ahc.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Gson gson = new Gson();
                RootModel rm = gson.fromJson(new String(responseBody), RootModel.class);

                // Setel data kota, matahari terbit, dan terbenam di TextView
                try {
                    JSONObject responseJson = new JSONObject(new String(responseBody));
                    JSONObject city = responseJson.getJSONObject("city");
                    String cityName = city.getString("name");
                    long sunrise = city.getLong("sunrise");
                    long sunset = city.getLong("sunset");

                    // Konversi waktu matahari terbit dan terbenam ke format yang mudah dibaca
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                    String sunriseTime = sdf.format(new Date(sunrise * 1000));
                    String sunsetTime = sdf.format(new Date(sunset * 1000));

                    // Setel informasi kota di TextView
                    String cityInfo = "Kota: " + cityName + "\n" +
                            "Matahari Terbit: " + sunriseTime + " WIB\n" +
                            "Matahari Terbenam: " + sunsetTime + " WIB";
                    _textViewCityInfo.setText(cityInfo);

                    // Setel total record di TextView
                    String totalRecordsInfo = "Total Record: " + rm.getListModelList().size();
                    _textViewTotalRecord.setText(totalRecordsInfo);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RecyclerView.LayoutManager lm = new LinearLayoutManager(MainActivity.this);
                CuacaAdapter ca = new CuacaAdapter(rm);

                _recyclerView1.setLayoutManager(lm);
                _recyclerView1.setAdapter(ca);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}