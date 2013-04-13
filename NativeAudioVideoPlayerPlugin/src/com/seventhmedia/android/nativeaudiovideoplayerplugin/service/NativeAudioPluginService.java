package com.seventhmedia.android.nativeaudiovideoplayerplugin.service;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.seventhmedia.android.nativeaudiovideoplayerplugin.NativeAudioPluginActivity;
import com.seventhmedia.android.nativeaudiovideoplayerplugin.R;

public class NativeAudioPluginService extends Service implements
	android.media.MediaPlayer.OnPreparedListener,
	android.media.MediaPlayer.OnErrorListener,
	io.vov.vitamio.MediaPlayer.OnPreparedListener,
	io.vov.vitamio.MediaPlayer.OnErrorListener {
	
	private NotificationManager _notificationManager;
	private Object _mediaPlayer = null;
	private android.media.MediaPlayer _androidMediaPlayer = null;
	private io.vov.vitamio.MediaPlayer _vitamioMediaPlayer = null;
	
	//logic
	private String _dataSource;
	private Boolean _isMediaPlayerAutoPlay = false;
	private String _activityNameToLaunch = "";
	private ResultReceiver _resultReceiver = null;
	
	private Boolean _isMediaPlayerStarted = false;
	private Boolean _isStreamReady = false;
	
	private int NOTIFICATION = R.string.NativeAudioPluginService_started;
	
	/**
     * Class used for the client Binder.
     * Because we know this service always runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class NativeAudioPluginServiceBinder extends Binder {
    	public NativeAudioPluginService getService() {
            //return this instance of NativeAudioPluginService so clients can call public methods declared in this Service
            return NativeAudioPluginService.this;
        }
    }
	
	/**
     * Checks whether the Service is currently running or not.
     * 
     * @param context	A reference to the Context object that needs to know if a service is running.
     * @return	Returns true if the Service is running, and false if it is not running.
     */
    public static boolean checkIfServiceIsRunning( Context context ) {
    	ActivityManager manager = (ActivityManager) context.getSystemService( Context.ACTIVITY_SERVICE );
	    for ( RunningServiceInfo service : manager.getRunningServices( Integer.MAX_VALUE ) ) {
	        if ( NativeAudioPluginService.class.getName().equals( service.service.getClassName() ) ) {
	        	return true;
	        }
	    }
	    return false;
	}
    
    public void startAudio( String dataSource, Boolean isMediaPlayerAutoPlay ) {
    	if ( !_isMediaPlayerStarted ) {
    		_dataSource = dataSource;
    		_isMediaPlayerAutoPlay = isMediaPlayerAutoPlay;
    		
    		//create the appropriate MediaPlayer based on the current OS version
    		/**
    		if ( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH ) {
    			_androidMediaPlayer = new android.media.MediaPlayer();
	    		_androidMediaPlayer.setVolume( 100,100 );
    			_androidMediaPlayer.setAudioStreamType( AudioManager.STREAM_MUSIC );
    			
    			_mediaPlayer = _androidMediaPlayer;
    		}
    		else {
    			_vitamioMediaPlayer = new io.vov.vitamio.MediaPlayer( NativeAudioPluginService.this );
    			_vitamioMediaPlayer.setVolume( 100,100 );
    			//_vitamioMediaPlayer.setAudioStreamType( AudioManager.STREAM_MUSIC );
    			_vitamioMediaPlayer.setBufferSize( 32 );
    			
    			_mediaPlayer = _vitamioMediaPlayer;
    		}
    		**/
    		_vitamioMediaPlayer = new io.vov.vitamio.MediaPlayer( NativeAudioPluginService.this );
			_vitamioMediaPlayer.setVolume( 100,100 );
			//_vitamioMediaPlayer.setAudioStreamType( AudioManager.STREAM_MUSIC );
			_vitamioMediaPlayer.setBufferSize( 32 );
			
			_mediaPlayer = _vitamioMediaPlayer;
    		
    		try {
	    		if ( _androidMediaPlayer != null ) {
	    			_androidMediaPlayer.setDataSource( _dataSource );
	    			_androidMediaPlayer.setOnPreparedListener( this );
	    			_androidMediaPlayer.setOnErrorListener( this );
	    			_androidMediaPlayer.prepareAsync();
	    			//_androidMediaPlayer.prepare();
	    		}
	    		else if ( _vitamioMediaPlayer != null ) {
	    			_vitamioMediaPlayer.setDataSource( _dataSource );
	    			_vitamioMediaPlayer.setOnPreparedListener( this );
	    			_vitamioMediaPlayer.setOnErrorListener( this );
	    			_vitamioMediaPlayer.prepareAsync();
	    			//_vitamioMediaPlayer.prepare();
	    		}
				
	    		_isStreamReady = false;
				_isMediaPlayerStarted = true;
			}
	    	catch ( Exception e ) {
	    		_isStreamReady = false;
	    		_isMediaPlayerStarted = false;
	    		
	    		clearMediaPlayer();
	    		e.printStackTrace();
	    	}
    	}
    }
    
    public void startAudio() {
    	startAudio( _dataSource, _isMediaPlayerAutoPlay );
    }
    
    public void stopAudio() {
    	if ( _isMediaPlayerStarted ) {
    		clearMediaPlayer();
    		_isStreamReady = false;
    		_isMediaPlayerStarted = false;
    	}
    }
    
    public void playAudio() {
    	if ( ( _isMediaPlayerStarted ) && ( _isStreamReady ) ) {
    		Notification notification = getNotification( getText( R.string.NativeAudioPluginService_label_play ).toString() );
    		if ( notification != null ) {
    			_notificationManager.notify( NOTIFICATION, notification );
    		}
    		
    		if ( _androidMediaPlayer != null ) {
    			_androidMediaPlayer.start();
    		}
    		else if ( _vitamioMediaPlayer != null ) {
    			_vitamioMediaPlayer.start();
    		}
    	}
    }
    
    public void pauseAudio() {
    	if ( ( _isMediaPlayerStarted ) && ( _isStreamReady ) ) {
    		Notification notification = getNotification( getText( R.string.NativeAudioPluginService_label_pause ).toString() );
    		if ( notification != null ) {
    			_notificationManager.notify( NOTIFICATION, notification );
    		}
    		
    		if ( _androidMediaPlayer != null ) {
    			_androidMediaPlayer.pause();
    		}
    		else if ( _vitamioMediaPlayer != null ) {
    			_vitamioMediaPlayer.pause();
    		}
    	}
    }
    
	@Override
	public void onCreate() {
		super.onCreate();
		
		_notificationManager = (NotificationManager)getSystemService( NOTIFICATION_SERVICE );
	}
	
	@Override
    public boolean onUnbind( Intent intent ) {
    	//super.onUnbind( intent );
    	
    	//hide the Notification
    	this.stopForeground( true );
    	
    	//tell the user we stopped the service
        Toast.makeText( this, R.string.NativeAudioPluginService_stopped, Toast.LENGTH_SHORT ).show();
    	
    	clearMediaPlayer();
    	stopSelf();
    	
    	return false;
    }
	
	@Override
	public IBinder onBind( Intent intent ) {
		//check if there were extras passed to this Service
    	//	and retrieve them
    	if ( ( intent != null ) && ( intent.getExtras() != null ) ) {
        	Bundle bundle = intent.getExtras();
        	
        	_dataSource = bundle.getString( NativeAudioPluginActivity.class.getName() + NativeAudioPluginActivity.EXTRA_AUDIO_PATH );
        	_isMediaPlayerAutoPlay = bundle.getBoolean( ( NativeAudioPluginActivity.class.getName() + NativeAudioPluginActivity.EXTRA_MEDIA_PLAYER_AUTO_PLAY ), false );
        	_activityNameToLaunch = bundle.getString( NativeAudioPluginActivity.class.getName() + NativeAudioPluginActivity.EXTRA_ACTIVITY_NAME_TO_LAUNCH );
        	_resultReceiver = bundle.getParcelable( NativeAudioPluginActivity.class.getName() + NativeAudioPluginActivity.EXTRA_RESULT_RECEIVER );
        }
    	
    	//tell the user we stopped the service
        Toast.makeText( this, R.string.NativeAudioPluginService_started, Toast.LENGTH_SHORT ).show();
    	
        //Display a notification about us starting.  We put an icon in the status bar.
        Notification notification = getNotification( getText( R.string.NativeAudioPluginService_label_loading ).toString() );
        
        if ( notification != null ) {
        	this.startForeground( NOTIFICATION, notification );
        }
        
        startAudio();
        
		return _binder;
	}
	
	//Binder given to clients
    private final IBinder _binder = new NativeAudioPluginServiceBinder();
	
    @Override
	public void onPrepared( android.media.MediaPlayer mediaPlayer ) {
    	initMediaPlayer();
	}
    
    @Override
    public void onPrepared( io.vov.vitamio.MediaPlayer mediaPlayer ) {
    	initMediaPlayer();
    }
    	
	@Override
	public boolean onError( android.media.MediaPlayer mp, int what, int extra ) {
		closeMediaPlayerDueToError();
		return false;
	}

	@Override
	public boolean onError( io.vov.vitamio.MediaPlayer mp, int what, int extra ) {
		closeMediaPlayerDueToError();
    	return false;
	}
    
    private Notification getNotification( String label ) {
    	String title = label;
    	String message = getText( R.string.NativeAudioPluginService_message ).toString();
    	String ticker = getText( R.string.NativeAudioPluginService_started ).toString();
    	
    	Class<?> cls;
    	try {
    		cls = Class.forName( _activityNameToLaunch );
    	}
    	catch ( ClassNotFoundException e ) {
    		cls = null;
    	}
    	
    	//make sure that our class definition is not null
    	if ( cls != null ) {
			// The PendingIntent to launch our activity if the user selects this notification
			Intent intent = new Intent( this, cls ); 
			PendingIntent contentIntent = PendingIntent.getActivity( this, 0, intent, 0 );
			
			Resources res = getResources();
			NotificationCompat.Builder builder = new NotificationCompat.Builder( this );
			builder.setContentIntent( contentIntent )
						.setSmallIcon( R.drawable.ic_service_livestreammusicplayer_headphones_small )		//required
						.setContentTitle( title )															//required
						.setContentText( message )															//required
						
						.setLargeIcon( BitmapFactory.decodeResource( res, R.drawable.ic_service_livestreammusicplayer_headphones_large ) )
						.setTicker( ticker )
						.setWhen( System.currentTimeMillis() )
						.setAutoCancel( false );
			Notification notification = builder.build();
			notification.flags |= Notification.FLAG_NO_CLEAR;
			
			return notification;
    	}
    	
    	return null;
    }
    
    private void clearMediaPlayer() {
    	//remove listeners
		if ( _androidMediaPlayer != null ) {
			_androidMediaPlayer.setOnPreparedListener( null );
			_androidMediaPlayer.setOnErrorListener( null );
			//_androidMediaPlayer.stop();
			_androidMediaPlayer.pause();
			
			try {
				_androidMediaPlayer.release();
			}
			catch ( Exception e ) {}
		}
		else if ( _vitamioMediaPlayer != null ) {
			_vitamioMediaPlayer.setOnPreparedListener( null );
			_vitamioMediaPlayer.setOnErrorListener( null );
			//_vitamioMediaPlayer.stop();
			_vitamioMediaPlayer.pause();
			
			try {
				_vitamioMediaPlayer.release();
			}
			catch ( Exception e ) {}
		}
		
		_mediaPlayer = null;
		_androidMediaPlayer = null;
		_vitamioMediaPlayer = null;
    }
    
    private void initMediaPlayer() {
    	//set flag that will let us know that the stream is ready to be used
    	_isStreamReady = true;
    	
    	//inform the user ( via Notification ) that the audio stream is ready
    	Notification notification = getNotification( getText( R.string.NativeAudioPluginService_label_ready ).toString() );
    	if ( notification != null ) {
    		_notificationManager.notify( NOTIFICATION, notification );
    	}
    	
    	//if the Service is set to auto-play, then play it
    	if ( _isMediaPlayerAutoPlay ) {
    		playAudio();
		}
    	
    	//remove listeners
    	if ( _androidMediaPlayer != null ) {
			_androidMediaPlayer.setOnPreparedListener( null );
		}
		else if ( _vitamioMediaPlayer != null ) {
			_vitamioMediaPlayer.setOnPreparedListener( null );
			_vitamioMediaPlayer.setBufferSize( 32 );
		}
    }
	
    private void closeMediaPlayerDueToError() {
    	if ( _resultReceiver != null ) {
    		_resultReceiver.send( NativeAudioPluginActivity.RESULT_CODE_SERVICE_ERROR, null );
    	}
    }
}