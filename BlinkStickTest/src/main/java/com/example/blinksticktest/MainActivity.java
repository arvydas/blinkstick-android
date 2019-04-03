package com.example.blinksticktest;

import com.agileinnovative.blinkstick.BlinkStick;
import com.agileinnovative.blinkstick.BlinkStickFinder;
import com.agileinnovative.blinkstick.BlinkStickUnauthorizedException;

import android.hardware.usb.UsbDeviceConnection;
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
import android.widget.Toast;

public class MainActivity extends Activity {

	public final static String TAG = "USBController";  
	private PendingIntent mPermissionIntent;
	private static final String ACTION_USB_PERMISSION = "com.examples.accessory.controller.action.USB_PERMISSION";
	
    UsbDeviceConnection connection;
    BlinkStick led;
    BlinkStickFinder finder;

    Button buttonFrame;
    Button buttonOff;
    TextView textViewStatus;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		
		finder = new BlinkStickFinder();
		finder.setContext(this);
		finder.setPermissionIntent(mPermissionIntent);
		
		Button btn = (Button)findViewById(R.id.buttonConnect);
		btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				findAndRequestPermission(true);
				updateUI();
			}
		});

		textViewStatus = (TextView)findViewById(R.id.textViewStatus);

		buttonFrame = (Button)findViewById(R.id.buttonFrame);
		buttonFrame.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//findDevice();
				if (led != null)
				{
					byte[] data = new byte[] {(byte)255, 0, 0, 0, (byte)255, 0, 0, 0, (byte)255};
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
                    led.setColor(barR.getProgress(), barG.getProgress(), barB.getProgress());
				}
				else
				{
                    led.setIndexedColor((byte)0, (byte)index, barR.getProgress(), barG.getProgress(), barB.getProgress());
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

		updateUI();
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
	}

	private void findAndRequestPermission(Boolean requestPermission)
	{
		if (led != null) {
			Toast.makeText(this, "Already connected to BlinkStick...", Toast.LENGTH_SHORT).show();
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
				if (finder.openDevice(led))
				{
					l("Manufacturer: " + led.getManufacturer());
					l("Product: " + led.getProduct());
					l("Serial: " + led.getSerial());
					l("Color: " + led.getColorString());
					l("Mode: " + led.getMode());
				}

				textViewStatus.setText("Status: Connected to " + led.getSerial());
			}
			catch (BlinkStickUnauthorizedException e) {
				if (requestPermission) {
					finder.requestPermission(led);
					findAndRequestPermission(false);
				} else {
					textViewStatus.setText("Status: Failed to get permission");
				}
			}
		}
	}
	
	/*
	private void findDevice()
	{
        UsbManager mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);  
        HashMap<String, UsbDevice> devlist = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviter = devlist.values().iterator(); 
        
        while (deviter.hasNext())   
        {  
            UsbDevice d = deviter.next();  
            if (d.getVendorId() == 0x20A0 && d.getProductId() == 0x41e5)
            {
                l("Found BlinkStick device: "+ String.format("%04X:%04X", d.getVendorId(),d.getProductId()));  
                
                if (mUsbManager.hasPermission(d))
                {
                    connection = mUsbManager.openDevice(d);
                }
                else
                {
                	mUsbManager.requestPermission(d, mPermissionIntent);
                }
            }
        }  
	}
	
	private void SetColor(int r, int g, int b)
	{
        if (connection != null)
        {
            byte[] buffer = new byte[] {0, (byte)r, (byte)g, (byte)b};
            connection.controlTransfer(0x20, 0x9, 1, 0, buffer, buffer.length, 2000);
        }
	}
	*/
	
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
