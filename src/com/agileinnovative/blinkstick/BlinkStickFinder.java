package com.agileinnovative.blinkstick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.PendingIntent;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

public class BlinkStickFinder {
	/**
	 * BlinkStick vendor ID 
	 */
	public final static int VENDOR_ID = 0x20a0;
	
	/**
	 * BlinkStick product ID 
	 */
	public final static int PRODUCT_ID = 0x41e5;


    private Context context;
    private UsbManager usbManager;

	private PendingIntent permissionIntent;
    
	/** 
	 * Set context which will be used to request access to UsbManager class.
	 * 
	 * @param intent	permission intent object
	 */
    public void setContext(Context c)
    {
    	context = c;
    }
    
	/** 
	 * Set permission intent object to be notified when user allows application to user BlinkStick device.
	 * 
	 * @param intent	permission intent object
	 */
    public void setPermissionIntent(PendingIntent intent)
    {
    	permissionIntent = intent;
    }

	/** 
	 * Find first BlinkStick connected to the computer
	 * 
	 * @return BlinkStick object or null if no BlinkSticks are connected
	 */
	public BlinkStick findFirst() {
		UsbDevice[] infos = findAllDescriptors();

		if (infos.length > 0) {
			BlinkStick result = new BlinkStick();
            result.setDevice(infos[0]);
            return result;
		}

		return null;
	}

	/** 
	 * Find all BlinkStick UsbDevice objects connected to the computer
	 * 
	 * @return an array of UsbDevice objects with VID and PID matching BlinkStick
	 */
	private UsbDevice[] findAllDescriptors() {
		if (usbManager == null)
		{
            usbManager = (UsbManager)context.getSystemService(Context.USB_SERVICE);  
		}

        HashMap<String, UsbDevice> devlist = usbManager.getDeviceList();
        Iterator<UsbDevice> deviter = devlist.values().iterator(); 
        List<UsbDevice> devices = new ArrayList<UsbDevice>();
        
        while (deviter.hasNext())   
        {  
            UsbDevice d = deviter.next();  
            if (d.getVendorId() == VENDOR_ID && d.getProductId() == PRODUCT_ID)
            {
            	devices.add(d);
            }
        }
		return devices.toArray(new UsbDevice[0]);
	}
	
	/** 
	 * Open BlinkStick device. The function checks for permission to open BlinkStick device.
	 * If permission hasn't been granted, it requests the permission from UsbManager class.
	 * 
	 * @param blinkStick	BlinkStick device to open
	 * 
	 * @return true if device was opened successfully
	 */
	public Boolean openDevice(BlinkStick blinkStick)
	{
        if (usbManager.hasPermission(blinkStick.getDevice()))
        {
            blinkStick.setConnection(usbManager.openDevice(blinkStick.getDevice()));
            return true;
        }
        else
        {
            usbManager.requestPermission(blinkStick.getDevice(), permissionIntent);
            return false;
        }
	}
}
