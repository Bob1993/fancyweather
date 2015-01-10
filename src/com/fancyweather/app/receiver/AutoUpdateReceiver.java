package com.fancyweather.app.receiver;

import com.fancyweather.app.service.AutoUpdateService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoUpdateReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Intent i= new Intent(context, AutoUpdateService.class);
		context.startService(i);
	}

}
