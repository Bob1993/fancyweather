package com.fancyweather.app.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class FancyWeatherDB {
	/*
	 * 数据库名
	 */
	public static final String DB_NAME= "fancy_weather";
	
	/*
	 * 数据库版本
	 */
	public static final int VERSION = 1;
	private static FancyWeatherDB fancyWeatherDB;//实现单例模式
	SQLiteDatabase db;//操作时获取到的数据库实例
	
	private FancyWeatherDB(Context context)
	{
		FancyWeatherOpenHelper dbHelper= new FancyWeatherOpenHelper(context, 
				DB_NAME, null, VERSION);
		db= dbHelper.getWritableDatabase();
	}
	
	public synchronized static FancyWeatherDB getInstance(Context context)//获取单例模式
	{
		if(fancyWeatherDB== null)
			fancyWeatherDB= new FancyWeatherDB(context);
		return fancyWeatherDB;
	}	
	
	public void saveProvince(Province province)
	{
		if(province!= null)
		{
			ContentValues values= new ContentValues();
			values.put("province_name", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());
			db.insert("Province", null, values);
		}
	}
	
	public List<Province> loadProvinces()
	{
		List<Province> list= new ArrayList<Province>();
		Cursor cursor= db.query("Province", null, null, null, null, null, null);
		
		//if (cursor.moveToFirst()) {//如果第一行不为空，则开始遍历
		while(cursor.moveToNext()){
			Province province= new Province();
			province.setId(cursor.getInt(cursor.getColumnIndex("id")));
			province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
			province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
			list.add(province);
			}
		return list;
	}
	
	public void saveCity(City city)
	{
		if(city!= null)
		{
			ContentValues values= new ContentValues();
			values.put("city_name", city.getCityName());
			values.put("city_code", city.getCityCode());
			values.put("province_id", city.getProvinceId());
			db.insert("City", null, values);
		}
	}
	
	public List<City> loadCities(int provinceId)
	{
		List<City> list= new ArrayList<City>();
		Cursor cursor= db.query("City", null, "province_id= ?", new String[]{String.valueOf(provinceId)}, null, null, null);
		
		//if (cursor.moveToFirst()) {//如果第一行不为空，则开始遍历
		while(cursor.moveToNext()){
			City city= new City();
			city.setId(cursor.getInt(cursor.getColumnIndex("id")));
			city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
			city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
			city.setProvinceId(provinceId);
			list.add(city);
			}
		return list;
	}
	
	
	public void saveCounty(County county)
	{
		if(county!= null)
		{
			ContentValues values= new ContentValues();
			values.put("county_name", county.getCountyName());
			values.put("county_code", county.getCountyCode());
			values.put("city_id", county.getCityId());
			db.insert("County", null, values);
		}
	}
	
	public List<County> loadCounties(int cityId)
	{
		List<County> list= new ArrayList<County>();
		Cursor cursor= db.query("County", null, "city_id= ?", new String[]{String.valueOf(cityId)}, null, null, null);
		
		//if (cursor.moveToFirst()) {//如果第一行不为空，则开始遍历
		while(cursor.moveToNext()){
			County county= new County();
			county.setId(cursor.getInt(cursor.getColumnIndex("id")));
			county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
			county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
			county.setCityId(cityId);
			list.add(county);
			}
		return list;
	}
}
