package com.fancyweather.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class FancyWeatherOpenHelper extends SQLiteOpenHelper{

	public static final String CREATE_PROVINCE= "create table Province(id integer " +//省份表的创建
			"primary key autoincrement, province_name text, province_code text)";
	public static final String CREATE_CITY= "create table City(id integer " +//市表的创建，最后加了一个
			"primary key autoincrement, city_name text, city_code text, province_id integer)";
	public static final String CREATE_COUNTY= "create table County(id integer " +//县表的创建，其中，市和县都加上了对上一级的外键
			"primary key autoincrement, county_name text, county_code text, city_id integer)";
	
	public FancyWeatherOpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(CREATE_PROVINCE);
		db.execSQL(CREATE_CITY);
		db.execSQL(CREATE_COUNTY);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {//更新方法
		// TODO Auto-generated method stub
		
	}

}
