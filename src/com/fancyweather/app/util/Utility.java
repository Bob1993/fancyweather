package com.fancyweather.app.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.fancyweather.app.db.City;
import com.fancyweather.app.db.County;
import com.fancyweather.app.db.FancyWeatherDB;
import com.fancyweather.app.db.Province;

public class Utility {
	/*
	 * 将从HttpUtil请求来的response进行 解析+封装存库处理
	 */
	public synchronized static boolean handleProvincesResponse(
			FancyWeatherDB fancyWeatherDB, String response) {
		if (!TextUtils.isEmpty(response)) {
			String[] allProvinces = response.split(",");
			if (allProvinces != null && allProvinces.length > 0) {
				for (String string : allProvinces) {//开始解析请求省信息的返回response
					String[] array = string.split("\\|");
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					fancyWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}

	public synchronized static boolean handleCitiesResponse(
			FancyWeatherDB fancyWeatherDB, String response, int provinceId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCities = response.split(",");
			if (allCities != null && allCities.length > 0) {
				for (String string : allCities) {
					String[] array = string.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					fancyWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}

	public synchronized static boolean handleCountiesResponse(
			FancyWeatherDB fancyWeatherDB, String response, int cityId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCounties = response.split(",");
			if (allCounties != null && allCounties.length > 0) {
				for (String string : allCounties) {
					String[] array = string.split("\\|");
					County county = new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
					fancyWeatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}

	public static void handleWeatherResponse(Context context, String response) {//对请求天气得到的json格式数据的SharedPreference解析和存储
		LogUtil.d("run", "handle weather is called");
		LogUtil.d("run", response);
		try {
			JSONObject jsonObject = new JSONObject(response);
			JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");//用GSON来解析得到的json数据
			String cityName = weatherInfo.getString("city");
			String weatherCode = weatherInfo.getString("cityid");
			String temp1 = weatherInfo.getString("temp1");
			String temp2 = weatherInfo.getString("temp2");
			String weatherDesp = weatherInfo.getString("weather");
			String publishTime = weatherInfo.getString("ptime");
			saveWeatherInfo(context, cityName, weatherCode, temp1, temp2,
					weatherDesp, publishTime);
			LogUtil.d("run", cityName+" "+weatherCode);
		} catch (Exception e) {
			// TODO: handle exception
			LogUtil.d("run", "jsonObject 数据处理失败");
		}
	}

	public static void saveWeatherInfo(Context context, String cityName,//将获取到的天气信息存储到持久化存储SharedPreference中
			String weatherCode, String temp1, String temp2, String weatherDesp,
			String publishTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);//即就是格式化日期的格式
		SharedPreferences.Editor editor = PreferenceManager
				.getDefaultSharedPreferences(context).edit();
		
		LogUtil.d("run", "save is called");
		editor.putBoolean("city_selected", true);// 标记sharedPreference中是否有城市被选中
		editor.putString("city_name", cityName);
		editor.putString("weather_code", weatherCode);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_desp", weatherDesp);
		editor.putString("publish_time", publishTime);
		editor.putString("current_date", sdf.format(new Date()));// 获取本地时间来初始化显示信息里的内容
		editor.commit();
	}
}
