package com.example.blinksticktest;

import com.agileinnovative.blinkstick.BlinkStick;
import com.agileinnovative.blinkstick.BlinkStickFinder;
import com.agileinnovative.blinkstick.BlinkStickUnauthorizedException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity {

	public final static String TAG = "USBController";  
	private PendingIntent mPermissionIntent;
	private static final String ACTION_USB_PERMISSION = "com.examples.blinksticktest.USB_PERMISSION";
	
    BlinkStick led;
    BlinkStickFinder finder;

	Button buttonConnect;
    Button buttonFrame;
    Button buttonOff;
    TextView textViewStatus;

	public class UsbReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action))
			{
				UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

				if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
				{
					if (device != null)
					{
					    try {
							if (openBlinkStick(led)) {
								textViewStatus.setText("Status: Connected to " + led.getSerial());
							}
						} catch (Exception e) {
							textViewStatus.setText(e.getMessage());
						}
					}
				}
				else
				{
					textViewStatus.setText("Status: Failed to get permission");
					led = null;
					updateUI();
				}
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			this.setTitle("BlinkStick Test " + pInfo.versionName);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		
		finder = new BlinkStickFinder();
		finder.setContext(this);
		finder.setPermissionIntent(mPermissionIntent);

		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		UsbReceiver usbReceiver = new UsbReceiver();
		registerReceiver(usbReceiver, filter);
		
		Button btn = (Button)findViewById(R.id.buttonConnect);
		btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				findBlinkStick();
				updateUI();
			}
		});

		textViewStatus = (TextView)findViewById(R.id.textViewStatus);

		buttonConnect = (Button)findViewById(R.id.buttonConnect);

		buttonFrame = (Button)findViewById(R.id.buttonFrame);
		buttonFrame.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//findDevice();
				if (led != null)
				{
					byte[] data = new byte[] {
							(byte)255, 0, 0,
							0, (byte)255, 0,
							0, 0, (byte)255,
							(byte)128, (byte)128, 0,
							0, (byte)128, (byte)128,
							(byte)128, 0, (byte)128,
							(byte)128, (byte)128, (byte)128,
							(byte)64, (byte)128, (byte)255,
					};
					led.setColors(data);
				}
			}
		});
		
		
		buttonOff = (Button)findViewById(R.id.buttonOff);
		buttonOff.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (led != null)
				{
					led.turnOff();
				}
			}
		});
		
		final SeekBar barR = (SeekBar)findViewById(R.id.seekBarColorR);
		final SeekBar barG = (SeekBar)findViewById(R.id.seekBarColorG);
		final SeekBar barB = (SeekBar)findViewById(R.id.seekBarColorB);
		final SeekBar barIndex = (SeekBar)findViewById(R.id.seekBarLedIndex);
		
		SeekBar.OnSeekBarChangeListener onColorChange = new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				
				int index = barIndex.getProgress();


				if (index == 0)
				{
					byte[] data = new byte[3 * 32];
					for (int i = 0; i < data.length / 3; i++) {
						data[i*3] = (byte)barG.getProgress();
						data[i*3 + 1] = (byte)barR.getProgress();
						data[i*3 + 2] = (byte)barB.getProgress();
					}
					led.setColors(data);
				}
				else if (index == 1)
				{
					led.setColor(barR.getProgress(), barG.getProgress(), barB.getProgress());
				}
				else
				{
                    led.setIndexedColor((byte)0, (byte)(index - 1), barR.getProgress(), barG.getProgress(), barB.getProgress());
				}
			}
		};
        barR.setOnSeekBarChangeListener(onColorChange);
        barG.setOnSeekBarChangeListener(onColorChange);
        barB.setOnSeekBarChangeListener(onColorChange);

		final SeekBar barLimit = (SeekBar)findViewById(R.id.seekBarLedBrightness);
		barLimit.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				led.setBrightnessLimit(progress);
			}
		});

		barIndex.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
										  boolean fromUser) {
				updateLedIndexText(progress);
			}
		});


		updateLedIndexText(0);
		updateUI();
	}

	private void updateLedIndexText(int index) {
		TextView ledIndexView = (TextView)findViewById(R.id.textViewLedIndex);
		if (index == 0) {
		    ledIndexView.setText("Led Index (all LEDs)");
		} else {
			ledIndexView.setText("Led Index ("+ String.valueOf(index - 1) + ")");
		}
	}

	private void updateUI() {
		LinearLayout layout = (LinearLayout) findViewById(R.id.mainLinearLayout);
		for (int i = 0; i < layout.getChildCount(); i++) {
			View child = layout.getChildAt(i);
			if (child.getId() != R.id.textViewStatus) {
				child.setEnabled(led != null);
			}
		}

		buttonOff.setEnabled(led != null);
		buttonFrame.setEnabled(led != null);

		if (led == null) {
			buttonConnect.setText("Connect");
		} else {
			buttonConnect.setText("Disconnect");
		}
	}

	private void findBlinkStick()
	{
		if (led != null) {
		    led = null;
			textViewStatus.setText("Status: Disconnected");
		    updateUI();
			return;
		}

		led = finder.findFirst();

		if (led == null)
		{
			textViewStatus.setText("Status: Could not find BlinkStick...");
		}
		else
		{
			l("Found BlinkStick device");

			try {
				if (openBlinkStick(led))
				{
					textViewStatus.setText("Status: Connected to " + led.getSerial());
				}
			}
			catch (BlinkStickUnauthorizedException e) {
                finder.requestPermission(led);
			}
		}
	}

	private Boolean openBlinkStick(BlinkStick device) throws BlinkStickUnauthorizedException {
	    try {
			if (finder.openDevice(device)) {
				l("Manufacturer: " + led.getManufacturer());
				l("Product: " + led.getProduct());
				l("Serial: " + led.getSerial());
				l("Color: " + led.getColorString());
				l("Mode: " + led.getMode());
				return true;
			}
		} catch (BlinkStickUnauthorizedException e) {
	    	throw e;
		}

		return false;
	}

	private void l(Object msg) {
        Log.d(TAG, ">==< " + msg.toString() + " >==<");  
    }  

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
}


