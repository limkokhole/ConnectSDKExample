/*
 * Copyright 2014 Dustin D. Brand - google.com/+DustinBrand
 * 
 * 
The content of this project itself is licensed under the 
Creative Commons Attribution 3.0 license, and the underlying 
source code used to format and display that content is 
licensed under the MIT license.

http://creativecommons.org/licenses/by/3.0/us/deed.en_US
http://opensource.org/licenses/mit-license.php
 */

package amo.connectsdkexample;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.device.ConnectableDeviceListener;
import com.connectsdk.device.DevicePicker;
import com.connectsdk.discovery.CapabilityFilter;
import com.connectsdk.discovery.DiscoveryManager;
import com.connectsdk.discovery.DiscoveryManager.PairingLevel;
import com.connectsdk.service.DeviceService;
import com.connectsdk.service.DeviceService.PairingType;
import com.connectsdk.service.capability.MediaControl;
import com.connectsdk.service.capability.MediaPlayer;
import com.connectsdk.service.capability.MediaPlayer.MediaLaunchObject;
import com.connectsdk.service.capability.VolumeControl;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.sessions.LaunchSession;


public class ConnectActivity extends Activity implements ConnectableDeviceListener {

	public DiscoveryManager mDiscoveryManager;
	public ConnectableDevice mDevice;	
	public ConnectableDeviceListener deviceListener;
	private Button btnVideo;
	private Button btnImage;
	private Button btnDevices;
	private Button btnPause;
	private Button btnPlay;
	private Button btnClose;
	
	
	private TextView txtStatus;
	
	public LaunchSession mLaunchSession;
	public MediaControl mMediaControl;
	//MediaPlayer.LaunchListener mLaunchListener;
	
	
	CapabilityFilter videoFilter = new CapabilityFilter(
			MediaPlayer.Display_Video, 
			MediaControl.Any, 
			VolumeControl.Volume_Up_Down
	);

	CapabilityFilter imageCapabilities = new CapabilityFilter(
			MediaPlayer.Display_Image
	);

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_main);
		DiscoveryManager.init(getApplicationContext());
		deviceListener = this;
		btnDevices = (Button) findViewById(R.id.btnDevices);
		btnImage = (Button) findViewById(R.id.btnImage);
		btnVideo = (Button) findViewById(R.id.btnVideo);
		btnPause = (Button) findViewById(R.id.btnPause);
		btnPlay = (Button) findViewById(R.id.btnPlay);
		btnClose = (Button) findViewById(R.id.btnStop);
		txtStatus = (TextView) findViewById(R.id.txtStatus);
		
		btnDevices.setOnClickListener(new OnClickListener() 
    	{
    		 @Override          
    		 public void onClick(View v) 
    		 {              
    			 showDevicePicker();
    		 }         
    	});
    	
		btnImage.setOnClickListener(new OnClickListener() 
    	{
    		 @Override          
    		 public void onClick(View v) 
    		 {              
    			 beamImage();
    		 }         
    	});
		
		btnVideo.setOnClickListener(new OnClickListener() 
    	{
    		 @Override          
    		 public void onClick(View v) 
    		 {              
    			 beamVideo();
    		 }         
    	});
		
		btnPause.setOnClickListener(new OnClickListener() 
    	{
    		 @Override          
    		 public void onClick(View v) 
    		 {              
    			 doPause();
    		 }         
    	});
    	
		btnPlay.setOnClickListener(new OnClickListener() 
    	{
    		 @Override          
    		 public void onClick(View v) 
    		 {              
    			 doPlay();
    		 }         
    	});
		
		btnClose.setOnClickListener(new OnClickListener() 
    	{
    		 @Override          
    		 public void onClick(View v) 
    		 {              
    			 doClose();
    		 }         
    	});
		
		
	    // Better to place this in the application lifecycle and retrieve it here
	    mDiscoveryManager = DiscoveryManager.getInstance();
		// with capability limits
		//mDiscoveryManager.setCapabilityFilters(videoFilter, imageCapabilities);
		// limit devices
		/*
		 * 
		mDiscoveryManager.registerDeviceService(CastService.class, CastDiscoveryProvider.class);
		mDiscoveryManager.registerDeviceService(DIALService.class, SSDPDiscoveryProvider.class);
		mDiscoveryManager.registerDeviceService(RokuService.class, SSDPDiscoveryProvider.class);
		mDiscoveryManager.registerDeviceService(DLNAService.class, SSDPDiscoveryProvider.class); // LG TV devices only, includes NetcastTVService
		mDiscoveryManager.registerDeviceService(WebOSTVService.class, SSDPDiscoveryProvider.class);
		 */
		// pairing is off by default, turn it on
		mDiscoveryManager.setPairingLevel(PairingLevel.ON);
		
	    mDiscoveryManager.start();
	    
	    
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		showDevicePicker();
	}
	
	MediaPlayer.LaunchListener mLaunchListener = new MediaPlayer.LaunchListener() {
	    
	    @Override
	    public void onError(ServiceCommandError error) {
	    	// 
	        System.out.println("Display failure: " + error);
	    }

		@Override
		public void onSuccess(MediaLaunchObject object) {
	        // save these object references to control media playback
	        mLaunchSession = object.launchSession;
	        mMediaControl = object.mediaControl;
	        runOnUiThread(new Runnable() {
	            public void run() {
	            	txtStatus.setText("Successful device session");
	            }
	        });
	        //
	        // you will want to enable your media control UI elements here
			//showPlaybackControls(true);
		}
	};


    AdapterView.OnItemClickListener selectDevice = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
				System.out.println("Setting mDevice onClick");
		    mDevice = (ConnectableDevice) parent.getItemAtPosition(position);
            mDevice.addListener(deviceListener);
            mDevice.connect();
			
		}
    };
	
	
	private void showDevicePicker() {
	    DevicePicker devicePicker = new DevicePicker(this);
	    AlertDialog dialog = devicePicker.getPickerDialog("Devices", selectDevice);
	    dialog.show();
	}
	
	@Override
	public void onDeviceReady(ConnectableDevice device) {
		// TODO Auto-generated method stub
		System.out.println("Device ready: " + device.getFriendlyName());
		txtStatus.setText("Device ready: " + device.getFriendlyName());
	}

	@Override
	public void onDeviceDisconnected(ConnectableDevice device) {
		// TODO Auto-generated method stub
		System.out.println("Device disconnected: " + device.getFriendlyName());
		txtStatus.setText("Device disconnected: " + device.getFriendlyName());
	}

	@Override
	public void onPairingRequired(ConnectableDevice device,
			DeviceService service, PairingType pairingType) {
		// TODO Auto-generated method stub
		System.out.println("Pair required: " + device.getFriendlyName());
		txtStatus.setText("Pair required: " + device.getFriendlyName());
	}

	@Override
	public void onCapabilityUpdated(ConnectableDevice device,
			List<String> added, List<String> removed) {
		// TODO Auto-generated method stub
		System.out.println("Compatibility updated: " + device.getFriendlyName());
		txtStatus.setText("Compatability updated: " + device.getFriendlyName());
	}

	@Override
	public void onConnectionFailed(ConnectableDevice device,
			ServiceCommandError error) {
		// TODO Auto-generated method stub
		System.out.println("Connect failed: " + device.getFriendlyName());
		txtStatus.setText("Connect failed: " + device.getFriendlyName());
	}

	
		
	
	private void beamVideo() {
		String mediaURL = "http://www.connectsdk.com/files/8913/9657/0225/test_video.mp4"; // credit: Blender Foundation/CC By 3.0
		String iconURL = "http://www.connectsdk.com/files/7313/9657/0225/test_video_icon.jpg"; // credit: sintel-durian.deviantart.com
		String title = "Sintel Trailer";
		String description = "Blender Open Movie Project";
		String mimeType = "video/mp4"; // valid mimeType (http format)
		try {
			if(mDevice != null) {
				System.out.println("we have mDevice");
				//mDevice.connect();
				//System.out.println(mDevice.getMediaPlayer().getMediaPlayerCapabilityLevel().name());
				//mDevice.getMediaControl();
				MediaPlayer mPlayer = mDevice.getMediaPlayer();
				mDevice.getMediaPlayer().playMedia(mediaURL, mimeType, title, description, iconURL, false, mLaunchListener);
				this.txtStatus.setText("Sent video");
			} else {
				showDevicePicker();
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	private void beamImage() {
		String mediaURL = "http://www.connectsdk.com/files/9613/9656/8539/test_image.jpg"; // credit: Blender Foundation/CC By 3.0
		String iconURL = "http://www.connectsdk.com/files/2013/9656/8845/test_image_icon.jpg"; // credit: sintel-durian.deviantart.com
		String title = "Sintel Character Design";
		String description = "Blender Open Movie Project";
		String mimeType = "image/jpeg";
		try {
			if(mDevice != null) {
				mDevice.getMediaPlayer().displayImage(mediaURL, mimeType, title, description, iconURL, mLaunchListener);
				this.txtStatus.setText("Sent image");
			} else {
				showDevicePicker();
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
			
		}
		
	}
	
	private void doPause() {
		try {
			mMediaControl.pause(null);
			this.txtStatus.setText("Paused");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void doPlay() {
		try {
			mMediaControl.play(null);
			this.txtStatus.setText("Playing");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}


	private void doClose() {
		try {
			if(mLaunchSession != null) {
				mDevice.getMediaPlayer().closeMedia(mLaunchSession, null);
				this.txtStatus.setText("Closed");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

}
