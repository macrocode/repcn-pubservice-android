package com.seventhmedia.android.nativeaudiovideoplayerplugin.plugins;

import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;

import com.seventhmedia.android.nativeaudiovideoplayerplugin.NativeVideoPluginActivity;
import com.seventhmedia.android.nativeaudiovideoplayerplugin.service.NativeVideoPluginService;
import com.seventhmedia.android.nativeaudiovideoplayerplugin.service.NativeVideoPluginService.NativeVideoPluginServiceBinder;

public class NativeVideoPlugin extends Plugin {
	
	//passable as extras in an Intent
	private String _videoPath = 										"";
	private Boolean _isMediaPlayerAutoPlay = 							false;
	private Boolean _isVideoPersistent = 								false;
	
	//logic
	private Boolean _isServiceBoundToExistingActivity = 				false;
	private Boolean _isServiceResumedFromNotification = 				false;
	private Boolean _isServiceResumedFromActivityResult = 				false;
	private Boolean _isServiceBounded = 								false;
	private Boolean _isVitamioLibraryInitialized = 						false;
	
	//usable PhoneGap plugin related actions
	public static final String INIT_VIDEO_PLUGIN_THROUGH_ACTIVITY_ACTION 		= "initVideoPluginThroughActivity";
	public static final String INIT_VIDEO_PLUGIN_ACTION 						= "initVideoPlugin";
	public static final String STOP_VIDEO_PLUGIN_ACTION							= "stopVideoPlugin";
	
	public static final String PLAY_VIDEO_ACTION 								= "playVideo";
	public static final String PAUSE_VIDEO_ACTION 								= "pauseVideo";
	
	private ServiceResultReceiver _serviceResultReceiver = null;
	private NativeVideoPluginService _service = null;
	private ServiceConnection _serviceConnection = new ServiceConnection() {
    	
    	@Override
    	public void onServiceConnected( ComponentName className, IBinder service ) {
    		//We've bound to NativeVideoPluginService, cast the IBinder and get NativeVideoPluginService instance
    		NativeVideoPluginServiceBinder binder = (NativeVideoPluginServiceBinder)service;
    		_service = binder.getService();
    		_isServiceBounded = true;
    	}
    	
    	@Override
    	public void onServiceDisconnected( ComponentName arg0 ) {
    		//only gets called in extreme situations, so don't rely on this callback
    		_isServiceBounded = false;
    	}
    	
    };
	
    /**
     * Executes the request and returns PluginResult.
     *
     * @param action        The action to execute.
     * @param args          JSONArray of arguments for the plugin.
     * @param callbackId    The callback id used when calling back into JavaScript.
     * @return              A PluginResult object with a status and message.
     */
	@Override
    public PluginResult execute( String action, JSONArray args, String callbackId ) {
		//we check if the plugin action we're supposed to do is an initialization type of action
		//	an initialization type of action requires a valid path
		Boolean isValidInitialization = false;
		
		//retrieve the arguments 
		try {
			//retrieve the audio path from the JSONArray
			_videoPath = args.getString( 0 ); 
			_videoPath = _videoPath.trim();
			
			//retrieve the auto-play flag from the JSONArray
			_isMediaPlayerAutoPlay = args.getBoolean( 1 );
			
			//retrieve the audio-persistence flag from the JSONArray
			_isVideoPersistent = args.getBoolean( 2 );
			
			//perform additional checking here if necessary
			if ( !_videoPath.equals( "" ) ) {
				isValidInitialization = true;
			}
		} 
		catch ( Exception ex ) {}
		
		//get a reference to the current Activity
		Activity activityReference = this.cordova.getActivity();
		
		//do a check for the Vitamio library and initialize it for devices running versions of the Android OS lower than ICS
		//we only need to do it once
		//if ( android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH ) {
			if ( ( !_isVitamioLibraryInitialized ) && ( !io.vov.vitamio.LibsChecker.checkVitamioLibs( activityReference ) ) )
				return new PluginResult( PluginResult.Status.ERROR, "Error initializing video stream" );
			_isVitamioLibraryInitialized = true;
		//}
		
		if ( ( INIT_VIDEO_PLUGIN_THROUGH_ACTIVITY_ACTION.equals( action ) ) && ( !_isVideoPersistent ) ) {
			if ( isValidInitialization ) {
				//this flag is to let us quickly know if the Plugin ( and subsequently the Service ) is bound to the existing Activity,
				//	or if it's going to launch a new Activity that will then launch the Service
				_isServiceBoundToExistingActivity = false;
				
				Intent intent = new Intent( activityReference, NativeVideoPluginActivity.class );
				
				//add the extras
				intent.putExtra( ( NativeVideoPluginActivity.class.getName() + NativeVideoPluginActivity.EXTRA_VIDEO_PATH ), 					_videoPath );
				intent.putExtra( ( NativeVideoPluginActivity.class.getName() + NativeVideoPluginActivity.EXTRA_MEDIA_PLAYER_AUTO_PLAY ), 		_isMediaPlayerAutoPlay );
				
				//( (Activity)this.cordova.getActivity() ).startActivity( intent );
				this.cordova.setActivityResultCallback( this );
				( (Activity)this.cordova.getActivity() ).startActivityForResult( intent, 1 );
				
				return new PluginResult( PluginResult.Status.OK, "Video plugin started" ); 
			}
			else {
				return new PluginResult( PluginResult.Status.ERROR, "Error initializing video stream" ); 
			}
		}
		else if ( ( INIT_VIDEO_PLUGIN_ACTION.equals( action ) ) || ( ( INIT_VIDEO_PLUGIN_THROUGH_ACTIVITY_ACTION.equals( action ) ) && ( _isVideoPersistent ) ) ) {
			if ( isValidInitialization ) {
				//this flag is to let us quickly know if the Service is bound to the existing Activity,
				//	or if it's going to launch a new Activity that will then launch the Service
				_isServiceBoundToExistingActivity = true;
				
				if ( !NativeVideoPluginService.checkIfServiceIsRunning( activityReference ) ) {
					startPluginService();
				}
				else {
				}
				return new PluginResult( PluginResult.Status.OK, "Video plugin started" );
			}
			else {
				return new PluginResult( PluginResult.Status.ERROR, "Invalid path received" ); 
			}
		}
		else if ( STOP_VIDEO_PLUGIN_ACTION.equals( action ) ) {
			if ( _isServiceBoundToExistingActivity ) {
				//this flag is to let us quickly know if the Service is bound to the existing Activity,
				//	or if it's going to launch a new Activity that will then launch the Service
				_isServiceBoundToExistingActivity = false;
				
				if ( NativeVideoPluginService.checkIfServiceIsRunning( activityReference ) ) {
					stopPluginService();
				}
				return new PluginResult( PluginResult.Status.OK, "Video plugin stopped" ); 
			}
			else {
				return new PluginResult( PluginResult.Status.ERROR, "Video is not yet initialized" );
			}
		}
		else if ( PLAY_VIDEO_ACTION.equals( action ) ) {
			if ( _isServiceBounded ) {
				_service.playVideo();
				return new PluginResult( PluginResult.Status.OK, "Playing video stream" ); 
			}
			else {
				return new PluginResult( PluginResult.Status.ERROR, "Video is not yet initialized" );
			}
		}
		else if ( PAUSE_VIDEO_ACTION.equals( action ) ) {
			if ( _isServiceBounded ) {
				_service.pauseVideo();
				return new PluginResult( PluginResult.Status.OK, "Pausing video stream" ); 
			}
			else {
				return new PluginResult( PluginResult.Status.ERROR, "Video is not yet initialized" );
			}
		}
		
		return null;
    }
	
	public void onResume( boolean multitasking ) {
		super.onResume( multitasking );
		
		if ( _isServiceResumedFromNotification ) {
			//get a reference to the current Activity
			Activity activityReference = this.cordova.getActivity();
			
			//this flag is to let us quickly know if the Service is bound to the existing Activity,
			//	or if it's going to launch a new Activity that will then launch the Service
			_isServiceBoundToExistingActivity = true;
			
			if ( !NativeVideoPluginService.checkIfServiceIsRunning( activityReference ) ) {
				//startPluginService();
			}
		}
		if ( _isServiceResumedFromActivityResult ) {
			//get a reference to the current Activity
			Activity activityReference = this.cordova.getActivity();
			
			//this flag is to let us quickly know if the Service is bound to the existing Activity,
			//	or if it's going to launch a new Activity that will then launch the Service
			_isServiceBoundToExistingActivity = true;
			
			if ( !NativeVideoPluginService.checkIfServiceIsRunning( activityReference ) ) {
				startPluginService();
			}
		}
		
		_isServiceResumedFromActivityResult = false;
		_isServiceResumedFromNotification = false;
	}
	
	@Override
	public void onPause( boolean multitasking ) {
		super.onPause( multitasking );
		
		if ( _isServiceBoundToExistingActivity ) {
			if ( !_isVideoPersistent ) {
	   			stopPluginService();
	   		}
		}
	}
		
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		//back-up logic to stop the Service when the Activity gets destroyed
		stopPluginService();
	}
	
	@Override
	public void onNewIntent( Intent intent ) {
		super.onNewIntent( intent );
		
		if ( intent.getComponent().getClassName().equals( this.cordova.getActivity().getClass().getName() ) ) {
			_isServiceResumedFromNotification = true;
		}
	}
	
	@Override
	public void onActivityResult( int requestCode, int resultCode, Intent intent ) {
		super.onActivityResult( requestCode, resultCode, intent );
		
		if ( requestCode == 1 ) {
			if ( resultCode == Activity.RESULT_OK ) {
				_isServiceResumedFromActivityResult = true;
			}
			else if ( resultCode == Activity.RESULT_CANCELED ) {
				_isServiceResumedFromActivityResult = false;
			}
		}
	}
	
	private void startPluginService() {
		Activity activityReference = this.cordova.getActivity();
		
		//bind to NativeVideoPluginService ( it is okay to start an already running Service )
        Intent intent = new Intent( activityReference, NativeVideoPluginService.class );
        
        if ( _serviceResultReceiver == null ) {
        	//create the ServiceResultReceiver if we've established a connection to the service
    		_serviceResultReceiver = new ServiceResultReceiver( null );
        }
        
        //add in extra data
        intent.putExtra( NativeVideoPluginActivity.class.getName() + NativeVideoPluginActivity.EXTRA_VIDEO_PATH, _videoPath );
        intent.putExtra( NativeVideoPluginActivity.class.getName() + NativeVideoPluginActivity.EXTRA_MEDIA_PLAYER_AUTO_PLAY, _isMediaPlayerAutoPlay );
        intent.putExtra( NativeVideoPluginActivity.class.getName() + NativeVideoPluginActivity.EXTRA_ACTIVITY_NAME_TO_LAUNCH, this.cordova.getActivity().getClass().getName() );
        intent.putExtra( NativeVideoPluginActivity.class.getName() + NativeVideoPluginActivity.EXTRA_RESULT_RECEIVER , _serviceResultReceiver );
        
        activityReference.bindService( intent, _serviceConnection, Context.BIND_AUTO_CREATE );
	}
	
	private void stopPluginService() {
		Activity activityReference = this.cordova.getActivity();
		
		if ( _isServiceBounded ) {
			activityReference.unbindService( _serviceConnection );
			
			_isServiceBoundToExistingActivity = false;
			_isServiceResumedFromNotification = false;
			_isServiceResumedFromActivityResult = false;
			_isServiceBounded = false;
			_service = null;
		}
	}
	
	class ServiceResultReceiver extends ResultReceiver {
		public ServiceResultReceiver( Handler handler ) {
			super( handler );
		}

		@Override
		protected void onReceiveResult( int resultCode, Bundle resultData ) {
			if ( NativeVideoPluginActivity.RESULT_CODE_SERVICE_ERROR == resultCode ) {
				stopPluginService();
			}
		}
	}
}