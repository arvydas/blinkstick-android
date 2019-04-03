package com.agileinnovative.blinkstick;

import java.util.Hashtable;
import java.util.Random;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

/**
 * Class designed to communicate with BlinkStick devices.
 */
public class BlinkStick {

	protected static final int STD_USB_REQUEST_GET_DESCRIPTOR = 0x06;
	protected static final int LIBUSB_DT_STRING = 0x03;

	/** 
	 * USB device object to communicate directly with BlinkStick
	 */
	private UsbDevice device = null;

	/** 
	 * USB connection for communicating with BlinkStick
	 */
	private UsbDeviceConnection connection;

	/** 
	 * Cached manufacturer name
	 */
	private String manufacturer = null;

	/** 
	 * Cached product name
	 */
	private String productName = null;

	/** 
	 * Assign UsbDevice
	 * 
	 * @param device object to communicate directly with BlinkStick
	 */
	public void setDevice(UsbDevice device) {
		this.device = device;
	}

	/** 
	 * Get UsbDevice
	 * 
	 * @return		USB device reference
	 */
	public UsbDevice getDevice()
	{
		return this.device;
	}

	/** 
	 * Assign USB device connection
	 * 
	 * @param con Connection object to communicate with BlinkStick device
	 */
	public void setConnection(UsbDeviceConnection con)
	{
		connection = con;
	}
	
	private int _VersionMajor = -1;

	/**
	 * Get major version number from serial
	 * 
	 * @return Major version number as int
	 */
	public int getVersionMajor() 
	{
		if (_VersionMajor == -1)
		{
			String serial = getSerial();
			if (serial != null) {
				_VersionMajor = Integer.parseInt(serial.substring(serial.length() - 3, serial.length() - 2));
			}
		}
		return _VersionMajor;
	}

	private int _VersionMinor = -1;

	/**
	 * Get minor version number from serial
	 * 
	 * @return Minor version number as int
	 */
	public int getVersionMinor() {
		if (_VersionMinor == -1)
		{
			String serial = getSerial();
			if (serial != null) {
				_VersionMinor = Integer.parseInt(serial.substring(serial.length() - 1, serial.length()));
			}
		}
		return _VersionMinor;
	}

	
	/**
	 * Get BlinkStick device type
	 * 
	 * @return BlinkStick device type
	 */
	public BlinkStickDeviceEnum getBlinkStickDevice()
	{
		if (getVersionMajor() == 1)
		{
			return BlinkStickDeviceEnum.BlinkStick;
		}
		else if (getVersionMajor() == 2)
		{
			return BlinkStickDeviceEnum.BlinkStickPro;
		}
		else if (getVersionMajor() == 3)
		{
			return BlinkStickDeviceEnum.BlinkStickStripOrSquare;
		}
		return BlinkStickDeviceEnum.Unknown;
	}

	private int brightnessLimit = 255;
	
	/**
	 * Set the brightness limit
	 * 
	 * @param value the maximum amount of brightness for LEDs in the range of [0..255]
	 */
	public void setBrightnessLimit(int value)
	{
		if (value < 0)
		{
			value = 0;
		}
		else if (value > 255)
		{
			value = 255;
		}
		
		brightnessLimit = value;
	}
	
	
	/**
	 * Get the current brightness limit
	 * 
	 * @return the maximum amount of brightness for LEDs in the range of [0..255]
	 */
	public int getBrightnessLimit() 
	{
		return brightnessLimit;
	}

	/**
	 * Check if BlinkStick is connected
	 * 
	 * @return Returns true if BlinkStick is connected
	 */
	public boolean isConnected()
	{
		return connection != null;
	}

	/** 
	 * Set the color of the device with separate r, g and b int values.
	 * The values are automatically converted to byte values
	 * 
	 * @param r	red int color value 0..255
	 * @param g gree int color value 0..255
	 * @param b blue int color value 0..255
	 */
	public void setColor(int r, int g, int b) {
		this.setColor((byte) r, (byte) g, (byte) b);
	}

	/** 
	 * Set the color of the device with separate r, g and b byte values
	 * 
	 * @param r	red byte color value 0..255
	 * @param g gree byte color value 0..255
	 * @param b blue byte color value 0..255
	 */
	public void setColor(byte r, byte g, byte b) {
		if (brightnessLimit < 255)
		{
			r = remapColor(r, brightnessLimit);
			g = remapColor(g, brightnessLimit);
			b = remapColor(b, brightnessLimit);
		}
		
		try {
			sendFeatureReport(new byte[] {1, r, g, b});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Sends feature report to BlinkStick
	 * 
	 * @param buffer An array of bytes to send to the device. First byte has to be report id.
	 */
	private void sendFeatureReport(byte[] buffer)
	{
		if (connection != null)
		{
			connection.controlTransfer(0x20, 0x9, buffer[0], 0, buffer, buffer.length, 2000);
		}
	}

	/**
	 * Get feature report from BlinkStick
	 * 
	 * @param buffer An array of bytes to receive from the device. First byte has to be report id. The array must be initialized with correct size.
	 * 
	 * @return		Number of bytes read from the device
	 */
	private int getFeatureReport(byte[] buffer)
	{
		if (connection != null)
		{
			return connection.controlTransfer(0x80 | 0x20, 0x1, buffer[0], 0, buffer, buffer.length, 2000);
		}

		return 0;
	}

	/** 
	 * Set indexed color of the device with separate r, g and b byte values for channel and LED index
	 * 
	 * @param channel	Channel (0 - R, 1 - G, 2 - B)
	 * @param index	Index of the LED
	 * @param r	red int color value 0..255
	 * @param g gree int color value 0..255
	 * @param b blue int color value 0..255
	 */
	public void setIndexedColor(int channel, int index, int r, int g, int b) {
		this.setIndexedColor((byte)channel, (byte)index, (byte)r, (byte)g, (byte)b);
	}

	/** 
	 * Set indexed color of the device with separate r, g and b byte values for channel and LED index
	 * 
	 * @param channel	Channel (0 - R, 1 - G, 2 - B)
	 * @param index	Index of the LED
	 * @param r	red byte color value 0..255
	 * @param g gree byte color value 0..255
	 * @param b blue byte color value 0..255
	 */
	public void setIndexedColor(byte channel, byte index, byte r, byte g, byte b) {
		if (brightnessLimit < 255)
		{
			r = remapColor(r, brightnessLimit);
			g = remapColor(g, brightnessLimit);
			b = remapColor(b, brightnessLimit);
		}

		try {
			sendFeatureReport(new byte[] {5, channel, index, r, g, b});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 
	 * Set the indexed color of BlinkStick Pro with Processing color value
	 * 
	 * @param channel	Channel (0 - R, 1 - G, 2 - B)
	 * @param index	Index of the LED
	 * @param value	color as int
	 */
	public void setIndexedColor(int channel, int index, int value) {
		int r = (value >> 16) & 0xFF;
		int g = (value >> 8)  & 0xFF;
		int b =  value        & 0xFF;

		this.setIndexedColor(channel, index, r, g, b);
	}

	/** 
	 * Set the indexed color of BlinkStick Pro with Processing color value for channel 0
	 * 
	 * @param index	Index of the LED
	 * @param value	color as int
	 */
	public void setIndexedColor(int index, int value) {
		int r = (value >> 16) & 0xFF;
		int g = (value >> 8)  & 0xFF;
		int b =  value        & 0xFF;

		this.setIndexedColor(0, index, r, g, b);
	}

	/** 
	 * Set the color of the device with Processing color value
	 * 
	 * @param value	color as int
	 */
	public void setColor(int value) {
		int r = (value >> 16) & 0xFF;
		int g = (value >> 8)  & 0xFF;
		int b =  value        & 0xFF;

		this.setColor(r, g, b);
	}

	/** 
	 * Set the color of the device with string value
	 * 
	 * @param value	this can either be a named color "red", "green", "blue" and etc.
	 * 			or a hex color in #rrggbb format
	 */
	public void setColor(String value) {
		if (COLORS.containsKey(value)) {
			this.setColor(hex2Rgb(COLORS.get(value)));
		} else {
			this.setColor(hex2Rgb(value));
		}
	}

	/** 
	 * Set random color
	 */
	public void setRandomColor() {
		Random random = new Random();
		this.setColor(
				random.nextInt(256), 
				random.nextInt(256),
				random.nextInt(256));
	}

	/** 
	 * Turn BlinkStick off
	 */
	public void turnOff() {
		if (this.getBlinkStickDevice() == BlinkStickDeviceEnum.BlinkStickStripOrSquare)
		{
			this.setColors(0, new byte[24]);
		}
		else
		{
			this.setColor(0, 0, 0);
		}
	}


	/** 
	 * Convert hex string to color object
	 * 
	 * @param colorStr	Color value as hex string #rrggbb
	 * 
	 * @return			color object
	 */
	private int hex2Rgb(String colorStr) {
		int red   = Integer.valueOf(colorStr.substring(1, 3), 16)+ 0;
		int green = Integer.valueOf(colorStr.substring(3, 5), 16) + 0;
		int blue  = Integer.valueOf(colorStr.substring(5, 7), 16) + 0;

		return (255 << 24) | (red << 16) | (green << 8) | blue;
	}

	/** 
	 * Get the current color of the device as int
	 * 
	 * @return The current color of the device as int
	 */
	public int getColor() {
		byte[] data = new byte[33];
		data[0] = 1;// First byte is ReportID

		try {
			int read = getFeatureReport(data);
			if (read > 0) {
				return (255 << 24) | (data[1] << 16) | (data[2] << 8) | data[3];
			}
		} catch (Exception e) {
		}

		return 0;
	}

	/** 
	 * Get the current color of the device in #rrggbb format 
	 * 
	 * @return Returns the current color of the device as #rrggbb formated string
	 */
	public String getColorString() {
		int c = getColor();

		int red   = (c >> 16) & 0xFF;
		int green = (c >> 8)  & 0xFF;
		int blue  =  c        & 0xFF;

		return "#" + String.format("%02X", red)
				+ String.format("%02X", green)
				+ String.format("%02X", blue);
	}

	/** 
	 * Get value of InfoBlocks
	 * 
	 * @param id	InfoBlock id, should be 1 or 2 as only supported info blocks
	 */
	private String getInfoBlock(int id) {
		byte[] data = new byte[33];
		data[0] = (byte) (id + 1);

		String result = "";
		try {
			int read = getFeatureReport(data);
			if (read > 0) {
				for (int i = 1; i < data.length; i++) {
					if (i == 0) {
						break;
					}

					result += (char) data[i];
				}
			}
		} catch (Exception e) {
		}

		return result;
	}

	/** 
	 * Get value of InfoBlock1
	 * 
	 * @return The value of info block 1
	 */
	public String getInfoBlock1() {
		return getInfoBlock(1);
	}

	/** 
	 * Get value of InfoBlock2
	 * 
	 * @return The value of info block 2
	 */
	public String getInfoBlock2() {
		return getInfoBlock(2);
	}


	/** 
	 * Set value for InfoBlocks
	 * 
	 * @param id	InfoBlock id, should be 1 or 2 as only supported info blocks
	 * @param value	The value to be written to the info block
	 */
	private void setInfoBlock(int id, String value) {
		char[] charArray = value.toCharArray();
		byte[] data = new byte[33];
		data[0] = (byte) (id + 1);

		for (int i = 0; i < charArray.length; i++) {
			if (i > 32) {
				break;
			}

			data[i + 1] = (byte) charArray[i];
		}

		try {
			sendFeatureReport(data);
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	/** 
	 * Set value for InfoBlock1
	 * 
	 * @param value	The value to be written to the info block 1
	 */
	public void setInfoBlock1(String value) {
		setInfoBlock(1, value);
	}

	/** 
	 * Set value for InfoBlock2
	 * 
	 * @param value	The value to be written to the info block 2
	 */
	public void setInfoBlock2(String value) {
		setInfoBlock(2, value);
	}

	/** 
	 * Get the manufacturer of the device
	 * 
	 * @return Returns the manufacturer name of the device
	 */
	public String getManufacturer() {
		if (manufacturer == null)
		{
			manufacturer = "";

			byte[] rawDescs = connection.getRawDescriptors();
			byte[] buffer = new byte[255];
			int idxMan = rawDescs[14];

			try
			{
				int rdo = connection.controlTransfer(UsbConstants.USB_DIR_IN
						| UsbConstants.USB_TYPE_STANDARD, STD_USB_REQUEST_GET_DESCRIPTOR,
						(LIBUSB_DT_STRING << 8) | idxMan, 0, buffer, 0xFF, 0);


				manufacturer = new String(buffer, 2, rdo - 2, "UTF-16LE");
			}
			catch (Exception e)
			{
			}
		}

		return manufacturer;
	}

	/** 
	 * Get the product description of the device
	 * 
	 * @return Returns the product name of the device.
	 */
	public String getProduct() {
		if (productName == null)
		{
			productName = "";

			byte[] rawDescs = connection.getRawDescriptors();
			byte[] buffer = new byte[255];
			int idxPrd = rawDescs[15];

			try
			{
				int rdo = connection.controlTransfer(UsbConstants.USB_DIR_IN
						| UsbConstants.USB_TYPE_STANDARD, STD_USB_REQUEST_GET_DESCRIPTOR,
						(LIBUSB_DT_STRING << 8) | idxPrd, 0, buffer, 0xFF, 0);
				productName = new String(buffer, 2, rdo - 2, "UTF-16LE");
			}
			catch (Exception e)
			{
			}
		}

		return productName;
	}


	/** 
	 * Get the serial number of the device
	 * 
	 * @return Returns the serial number of device.
	 */
	public String getSerial() {
		return connection.getSerial();
	}


	/** 
	 * Determine report id for the amount of data to be sent
	 * 
	 * @return Returns the report id
	 */
	private byte determineReportId(int length) {
		byte reportId = 9;
		//Automatically determine the correct report id to send the data to
		if (length <= 8 * 3)
		{
			reportId = 6;
		}
		else if (length <= 16 * 3)
		{
			reportId = 7;
		}
		else if (length <= 32 * 3)
		{
			reportId = 8;
		}
		else if (length <= 64 * 3)
		{
			reportId = 9;
		}
		else if (length <= 128 * 3)
		{
			reportId = 10;
		}

		return reportId;
	}

	/** 
	 * Determine the adjusted maximum amount of LED for the report
	 * 
	 * @return Returns the adjusted amount of LED data
	 */
	private byte determineMaxLeds(int length) {
		byte maxLeds = 64;
		//Automatically determine the correct report id to send the data to
		if (length <= 8 * 3)
		{
			maxLeds = 8;
		}
		else if (length <= 16 * 3)
		{
			maxLeds = 16;
		}
		else if (length <= 32 * 3)
		{
			maxLeds = 32;
		}
		else if (length <= 64 * 3)
		{
			maxLeds = 64;
		}
		else if (length <= 128 * 3)
		{
			maxLeds = 64;
		}

		return maxLeds;
	}

	/** 
	 * Send a packet of data to LEDs on channel 0 (R)
	 * 
	 * @param colorData	Report data must be a byte array in the following format: [g0, r0, b0, g1, r1, b1, g2, r2, b2 ...]
	 */
	public void setColors(byte[] colorData)
	{
		this.setColors((byte)0, colorData);
	}

	/** 
	 * Send a packet of data to LEDs
	 * 
	 * @param channel	Channel (0 - R, 1 - G, 2 - B)
	 * @param colorData	Report data must be a byte array in the following format: [g0, r0, b0, g1, r1, b1, g2, r2, b2 ...]
	 */
	public void setColors(int channel, byte[] colorData)
	{
		this.setColors((byte)channel, colorData);
	}

	/** 
	 * Send a packet of data to LEDs
	 * 
	 * @param channel	Channel (0 - R, 1 - G, 2 - B)
	 * @param colorData	Report data must be a byte array in the following format: [g0, r0, b0, g1, r1, b1, g2, r2, b2 ...]
	 */
	public void setColors(byte channel, byte[] colorData)
	{
		byte leds = this.determineMaxLeds(colorData.length);
		byte[] data = new byte[leds * 3 + 2];

		data[0] = this.determineReportId(colorData.length);
		data[1] = channel;


		for (int i = 0; i < Math.min(colorData.length, data.length - 2); i++)
		{
            if (brightnessLimit < 255)
            {
                data[i + 2] = remapColor(colorData[i], brightnessLimit);
            }
            else
            {
                data[i + 2] = colorData[i];
            }
		}

		for (int i = colorData.length + 2; i < data.length; i++)
		{
			data[i] = 0;
		}

		try {
			sendFeatureReport(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 
	 * Set the mode of BlinkStick Pro as int
	 * 
	 * @param mode	0 - Normal, 1 - Inverse, 2 - WS2812, 3 - WS2812 mirror
	 */
	public void setMode(int mode)
	{
		this.setMode((byte)mode);
	}

	/** 
	 * Set the mode of BlinkStick Pro as byte
	 * 
	 * @param mode	0 - Normal, 1 - Inverse, 2 - WS2812, 3 - WS2812 mirror
	 */
	public void setMode(byte mode)
	{
		try {
			sendFeatureReport(new byte[] {4, mode});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 
	 * Get the mode of BlinkStick Pro
	 * 
	 * @return 0 - Normal, 1 - Inverse, 2 - WS2812, 3 - WS2812 mirror
	 */
	public byte getMode()
	{
		byte[] data = new byte[2];
		data[0] = 4;// First byte is ReportID

		try {
			int read = getFeatureReport(data);
			if (read > 0) {
				return data[1];
			}
		} catch (Exception e) {
		}

		return -1;
	} 

	private byte remap(byte value, float leftMin, float leftMax, float rightMin, float rightMax)
	{
		//Figure out how 'wide' each range is
		float leftSpan = leftMax - leftMin;
		float rightSpan = rightMax - rightMin;

		//Java does not have unsigned bytes, so we have to do some byte to int conversion
		int valueInt = value;
		if (valueInt < 0)
		{
			valueInt = valueInt + 0xff;
		}
		
		//Convert the left range into a 0-1 range (float)
		float valueScaled = (valueInt - leftMin) / leftSpan;
		
		//Convert the 0-1 range into a value in the right range.
		valueInt = (int)(rightMin + (valueScaled * rightSpan));
		
		//Convert back to correct signed value before conversion to byte
		if (valueInt > 127)
		{
			valueInt = valueInt - 0xff;
		}
		
		return (byte)valueInt;
	}
	
	private byte remapColor(byte value, float max_value)
	{
		return remap(value, 0, 255, 0, max_value);
	}
	
	private int remapColorReverse(byte value, byte max_value)
	{
		return remap(value, 0, max_value, 0, 255);
	}
	
	/**
	 * Variable holds the list of valid CSS colors as a hashtable
	 */
	private static final Hashtable<String, String> COLORS = new Hashtable<String, String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			put("aqua", "#00ffff");
			put("aliceblue", "#f0f8ff");
			put("antiquewhite", "#faebd7");
			put("black", "#000000");
			put("blue", "#0000ff");
			put("cyan", "#00ffff");
			put("darkblue", "#00008b");
			put("darkcyan", "#008b8b");
			put("darkgreen", "#006400");
			put("darkturquoise", "#00ced1");
			put("deepskyblue", "#00bfff");
			put("green", "#008000");
			put("lime", "#00ff00");
			put("mediumblue", "#0000cd");
			put("mediumspringgreen", "#00fa9a");
			put("navy", "#000080");
			put("springgreen", "#00ff7f");
			put("teal", "#008080");
			put("midnightblue", "#191970");
			put("dodgerblue", "#1e90ff");
			put("lightseagreen", "#20b2aa");
			put("forestgreen", "#228b22");
			put("seagreen", "#2e8b57");
			put("darkslategray", "#2f4f4f");
			put("darkslategrey", "#2f4f4f");
			put("limegreen", "#32cd32");
			put("mediumseagreen", "#3cb371");
			put("turquoise", "#40e0d0");
			put("royalblue", "#4169e1");
			put("steelblue", "#4682b4");
			put("darkslateblue", "#483d8b");
			put("mediumturquoise", "#48d1cc");
			put("indigo", "#4b0082");
			put("darkolivegreen", "#556b2f");
			put("cadetblue", "#5f9ea0");
			put("cornflowerblue", "#6495ed");
			put("mediumaquamarine", "#66cdaa");
			put("dimgray", "#696969");
			put("dimgrey", "#696969");
			put("slateblue", "#6a5acd");
			put("olivedrab", "#6b8e23");
			put("slategray", "#708090");
			put("slategrey", "#708090");
			put("lightslategray", "#778899");
			put("lightslategrey", "#778899");
			put("mediumslateblue", "#7b68ee");
			put("lawngreen", "#7cfc00");
			put("aquamarine", "#7fffd4");
			put("chartreuse", "#7fff00");
			put("gray", "#808080");
			put("grey", "#808080");
			put("maroon", "#800000");
			put("olive", "#808000");
			put("purple", "#800080");
			put("lightskyblue", "#87cefa");
			put("skyblue", "#87ceeb");
			put("blueviolet", "#8a2be2");
			put("darkmagenta", "#8b008b");
			put("darkred", "#8b0000");
			put("saddlebrown", "#8b4513");
			put("darkseagreen", "#8fbc8f");
			put("lightgreen", "#90ee90");
			put("mediumpurple", "#9370db");
			put("darkviolet", "#9400d3");
			put("palegreen", "#98fb98");
			put("darkorchid", "#9932cc");
			put("yellowgreen", "#9acd32");
			put("sienna", "#a0522d");
			put("brown", "#a52a2a");
			put("darkgray", "#a9a9a9");
			put("darkgrey", "#a9a9a9");
			put("greenyellow", "#adff2f");
			put("lightblue", "#add8e6");
			put("paleturquoise", "#afeeee");
			put("lightsteelblue", "#b0c4de");
			put("powderblue", "#b0e0e6");
			put("firebrick", "#b22222");
			put("darkgoldenrod", "#b8860b");
			put("mediumorchid", "#ba55d3");
			put("rosybrown", "#bc8f8f");
			put("darkkhaki", "#bdb76b");
			put("silver", "#c0c0c0");
			put("mediumvioletred", "#c71585");
			put("indianred", "#cd5c5c");
			put("peru", "#cd853f");
			put("chocolate", "#d2691e");
			put("tan", "#d2b48c");
			put("lightgray", "#d3d3d3");
			put("lightgrey", "#d3d3d3");
			put("thistle", "#d8bfd8");
			put("goldenrod", "#daa520");
			put("orchid", "#da70d6");
			put("palevioletred", "#db7093");
			put("crimson", "#dc143c");
			put("gainsboro", "#dcdcdc");
			put("plum", "#dda0dd");
			put("burlywood", "#deb887");
			put("lightcyan", "#e0ffff");
			put("lavender", "#e6e6fa");
			put("darksalmon", "#e9967a");
			put("palegoldenrod", "#eee8aa");
			put("violet", "#ee82ee");
			put("azure", "#f0ffff");
			put("honeydew", "#f0fff0");
			put("khaki", "#f0e68c");
			put("lightcoral", "#f08080");
			put("sandybrown", "#f4a460");
			put("beige", "#f5f5dc");
			put("mintcream", "#f5fffa");
			put("wheat", "#f5deb3");
			put("whitesmoke", "#f5f5f5");
			put("ghostwhite", "#f8f8ff");
			put("lightgoldenrodyellow", "#fafad2");
			put("linen", "#faf0e6");
			put("salmon", "#fa8072");
			put("oldlace", "#fdf5e6");
			put("bisque", "#ffe4c4");
			put("blanchedalmond", "#ffebcd");
			put("coral", "#ff7f50");
			put("cornsilk", "#fff8dc");
			put("darkorange", "#ff8c00");
			put("deeppink", "#ff1493");
			put("floralwhite", "#fffaf0");
			put("fuchsia", "#ff00ff");
			put("gold", "#ffd700");
			put("hotpink", "#ff69b4");
			put("ivory", "#fffff0");
			put("lavenderblush", "#fff0f5");
			put("lemonchiffon", "#fffacd");
			put("lightpink", "#ffb6c1");
			put("lightsalmon", "#ffa07a");
			put("lightyellow", "#ffffe0");
			put("magenta", "#ff00ff");
			put("mistyrose", "#ffe4e1");
			put("moccasin", "#ffe4b5");
			put("navajowhite", "#ffdead");
			put("orange", "#ffa500");
			put("orangered", "#ff4500");
			put("papayawhip", "#ffefd5");
			put("peachpuff", "#ffdab9");
			put("pink", "#ffc0cb");
			put("red", "#ff0000");
			put("seashell", "#fff5ee");
			put("snow", "#fffafa");
			put("tomato", "#ff6347");
			put("white", "#ffffff");
			put("yellow", "#ffff00");
		}
	};

}
