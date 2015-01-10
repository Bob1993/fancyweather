package com.fancyweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import com.fancyweather.app.R;
import com.fancyweather.app.db.City;
import com.fancyweather.app.db.County;
import com.fancyweather.app.db.FancyWeatherDB;
import com.fancyweather.app.db.Province;
import com.fancyweather.app.util.HttpCallbackListener;
import com.fancyweather.app.util.HttpUtil;
import com.fancyweather.app.util.LogUtil;
import com.fancyweather.app.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;

	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private FancyWeatherDB fancyWeatherDB;
	private List<String> dataList = new ArrayList<String>();

	private List<Province> provinceList;// 省列表
	private List<City> cityList;// 市列表
	private List<County> countyList;// 县列表

	private Province selectedProvince;
	private City selectedCity;
	private int currentLevel;// 当前所属等级
	private boolean isFromWeatherActivity;//判断跳转是否来自天气界面

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		isFromWeatherActivity= getIntent().getBooleanExtra("from_weather_activity", false);//默认不是来自天气界面
		SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
		if (prefs.getBoolean("city_selected", false)&& !isFromWeatherActivity) {//默认为未选定，因为第一次进来的时候可能prefs为空，这样就不需要跳入天气界面了，而是继续选择天气.还有一种可能就是已经选定城市了，现在时重新选城市，这时候如果是来自天气的跳转，就不应该转向天气。
			Intent intent= new Intent(this, WeatherActivity.class);//在sharedPreferenced里有选定的天气，那么进入程序后直接进入天气界面
			startActivity(intent);
			finish();
			return;
		}
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		fancyWeatherDB = FancyWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(position);// 将选中的省赋给selectedProvince
					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(position);
					queryCounties();
				} else if (currentLevel== LEVEL_COUNTY) {
					String countyCode= countyList.get(position).getCountyCode();
					Intent intent= new Intent(ChooseAreaActivity.this, WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}
		});

		queryProvinces();// 首先得要加载省级数据
	}

	private void queryProvinces()// 加载中国所有省份信息，优先从数据库中加载
	{
		provinceList = fancyWeatherDB.loadProvinces();
		if (provinceList.size() > 0) {
			dataList.clear();
			for (Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();// 通知adapter去更新 ListView界面
			listView.setSelection(0);// 默认选中第一项
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;// 当前所处省级列表
		} else {
			queryFromServer(null, "province");// 第一次加载，肯定要去服务器端下载所有省得数据的
		}
	}

	private void queryCities()// 查询省内所有市
	{
		cityList = fancyWeatherDB.loadCities(selectedProvince.getId());
		if (cityList.size() > 0) {
			dataList.clear();
			for (City city : cityList) {
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();// 通知adapter去更新 ListView界面
			listView.setSelection(0);// 默认选中第一项
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;// 当前所处省级列表
		} else {
			queryFromServer(selectedProvince.getProvinceCode(), "city");// 数据库中没有该城市信息，则根据省号去查其下的所有市
		}
	}

	private void queryCounties()// 查询市内所有县
	{
		countyList = fancyWeatherDB.loadCounties(selectedCity.getId());// 利用被选中的市的id来加载所有县，注意：id在county表里是外键
		if (countyList.size() > 0) {
			dataList.clear();
			for (County county : countyList) {
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();// 通知adapter去更新 ListView界面
			listView.setSelection(0);// 默认选中第一项
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;// 当前所处县级列表
		} else {
			LogUtil.d("query", countyList.size()+"");
			//Log.d("query", "query form server in County");
			queryFromServer(selectedCity.getCityCode(), "county");// 数据库中没有该城市信息，则根据市号去查其下的所有县
			LogUtil.d("query", "finish");
		}
	}

	private void queryFromServer(final String code, final String type) {
		String address;
		if (!TextUtils.isEmpty(code))// 查询的不是省级信息
		{
			address = "http://www.weather.com.cn/data/list3/city" + code
					+ ".xml";
		} else
			address = "http://www.weather.com.cn/data/list3/city.xml";
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onFinish(String response) {// 访问成功，处理回执信息
				// TODO Auto-generated method stub
				boolean result = false;
				if ("province".equals(type))
					result = Utility.handleProvincesResponse(fancyWeatherDB,
							response);// 将response中转到handle中去处理,即就是解析+存储
				else if ("city".equals(type))
					result = Utility.handleCitiesResponse(fancyWeatherDB,
							response, selectedProvince.getId());
				else if ("county".equals(type))
					result = Utility.handleCountiesResponse(fancyWeatherDB,
							response, selectedCity.getId());//以后还是不要复制代码的好，要不容易漏改，这种错误很难查出来的。

				if (result) {
					// 如果结果处理成功，需要跳回主线程
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							closeProgressDialog();
							if ("province".equals(type))
								queryProvinces();
							else if ("city".equals(type)) {
								queryCities();
							} else if ("county".equals(type)) {
								queryCounties();
							}
						}
					});
				}
			}

			@Override
			public void onError(Exception e) {// 访问失败
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", 0)
								.show();
					}
				});
			}
		});

	}

	private void showProgressDialog()// 显示进度对话框
	{
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("加载中···");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}

	private void closeProgressDialog() {
		if (progressDialog != null)
			progressDialog.dismiss();
	}

	@Override
	public void onBackPressed() {// 按返回键，根据当前情况返回不同的地方
		// TODO Auto-generated method stub
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
		} else
		{
			if(isFromWeatherActivity)
			{
				Intent intent= new Intent(this, WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}
}
