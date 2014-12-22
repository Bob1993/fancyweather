package com.fancyweather.app.util;

import android.text.TextUtils;

import com.fancyweather.app.db.City;
import com.fancyweather.app.db.County;
import com.fancyweather.app.db.FancyWeatherDB;
import com.fancyweather.app.db.Province;

public class Utility {
	/*
	 * 解析和处理服务器返回的省级数据，解析+存库
	 */
	public synchronized static boolean handleProvincesResponse(
			FancyWeatherDB fancyWeatherDB, String response) {
		if (!TextUtils.isEmpty(response)) {
			String[] allProvinces = response.split(",");
			if(allProvinces!= null&& allProvinces.length>0){
			for (String string : allProvinces) {// 解析并存储每一个省份
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
			if(allCities!= null&& allCities.length>0){//真正确定获取到数据了，才开始进行解析
			for (String string : allCities) {// 解析并存储每一个省份
				String[] array = string.split("\\|");
				City city= new City();
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
			if(allCounties!= null&& allCounties.length>0){
			for (String string : allCounties) {
				String[] array = string.split("\\|");
				County county= new County();
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
}
