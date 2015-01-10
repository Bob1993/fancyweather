package com.fancyweather.app.activity;

import com.fancyweather.app.R;
import com.fancyweather.app.util.HttpCallbackListener;
import com.fancyweather.app.util.HttpUtil;
import com.fancyweather.app.util.LogUtil;
import com.fancyweather.app.util.Utility;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity implements OnClickListener{
	private LinearLayout weatherInfoLayout;//整个天气布局
	private TextView cityNameText;
	private TextView publishText;
	private TextView weatherDespText;
	
	private TextView currentDateText;
	private TextView temp1text;
	private TextView temp2text;
	
	private Button switchCity;
	private Button refreshWeather;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//不要题目
		setContentView(R.layout.weather_layout);
		weatherInfoLayout= (LinearLayout) findViewById(R.id.weather_info_layout);//加载显示天气的控件布局
		cityNameText= (TextView) findViewById(R.id.city_name);
		publishText= (TextView) findViewById(R.id.publish_text);
		weatherDespText= (TextView) findViewById(R.id.weather_desp);
		temp1text= (TextView) findViewById(R.id.temp1);
		temp2text= (TextView) findViewById(R.id.temp2);
		currentDateText= (TextView) findViewById(R.id.current_date);
		switchCity= (Button) findViewById(R.id.switch_city);
		refreshWeather= (Button) findViewById(R.id.refresh_city);
		String countyCode= getIntent().getStringExtra("county_code");
		
		if (!TextUtils.isEmpty(countyCode)) {
			publishText.setText("同步中...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);//将显示天气部分设置为不可见
			queryWeatherCode(countyCode);
		}else {
			showWeather();
		}
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.switch_city:
			Intent intent= new Intent(this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_city:
			publishText.setText("同步中...");
			SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode= prefs.getString("weather_code", "");
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);//在刷新的时候，sharedPreference里边肯定有数据，我们只需要根据天气代号再查一下就ok，
			}
			break;
		default:
			break;
		}
	}
	
	private void queryWeatherCode(String countyCode)//根据县代号查询对应的天气代号
	{
		String address= "http://www.weather.com.cn/data/list3/city"+
	countyCode+".xml";
		queryFromServer(address, "countyCode");
	}
	
	private void queryWeatherInfo(String weatherCode)//根据天气代号查询天气信息，这个和上边那个是递进的关系，并非并列，不过在刷新的时候是可以进行直接调用的
	{
		String address= "http://www.weather.com.cn/data/cityinfo/"+
	weatherCode+".html";//注意，这里是html超文本文件，而不是xml文件
		queryFromServer(address, "weatherCode");
	}
	
	private void queryFromServer(final String address, final String type)//从服务器获取信息
	{
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				if ("countyCode".equals(type)) {//如果是查询天气代号的
					if (!TextUtils.isEmpty(response)) {
						String[] array= response.split("\\|");
						if (array!= null && array.length==2) {
							String weatherCode= array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				}else if ("weatherCode".equals(type)) {
					LogUtil.d("run", "handle weatherCode is called");
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							showWeather();
						}
					});
				}
			} 
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					public void run() {
						publishText.setText("同步失败");
					}
				});
			} 
		});
	}
	
	private void showWeather()//从sharedPreference中获取存储好的数据
	{
		Log.d("run", "show successfully!!!");
		SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);//获取当前应用的sharedPreference存储体（文件）
		cityNameText.setText(prefs.getString("city_name", ""));
		temp1text.setText(prefs.getString("temp1", ""));
		temp2text.setText(prefs.getString("temp2", ""));
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		publishText.setText("今天"+prefs.getString("publish_time", "")+ "发布");
		currentDateText.setText(prefs.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
	}
}
