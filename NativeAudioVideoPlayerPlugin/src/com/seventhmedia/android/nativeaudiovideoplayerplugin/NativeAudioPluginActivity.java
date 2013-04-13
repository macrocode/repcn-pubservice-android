package com.seventhmedia.android.nativeaudiovideoplayerplugin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.seventhmedia.android.nativeaudiovideoplayerplugin.service.NativeAudioPluginService;
import com.seventhmedia.android.nativeaudiovideoplayerplugin.service.NativeAudioPluginService.NativeAudioPluginServiceBinder;

public class NativeAudioPluginActivity extends FragmentActivity implements OnClickListener, AudioOnBackgroundNoticeDialogFragment.NoticeDialogListener {
	
	private Button _buttonPlayAudio;
	private Button _buttonPauseAudio;
	private CheckBox _checkboxPersistAudio;
	
	private String _audioPath = 					"";
	private Boolean _isMediaPlayerAutoPlay = 		false;
	private Boolean _isAudioPersistent = 			false;
	
	private Boolean _isServiceBounded = false;
	
	private NativeAudioPluginService _service = null;
	private ServiceResultReceiver _serviceResultReceiver = null;
	
	public final static String EXTRA_AUDIO_PATH 					= "_audioPath";
	public final static String EXTRA_MEDIA_PLAYER_AUTO_PLAY 		= "_isMediaPlayerAutoPlay";
	public final static String EXTRA_ACTIVITY_NAME_TO_LAUNCH		= "_activityNameToLaunch";
	public final static String EXTRA_RESULT_RECEIVER				= "_resultReceiver";
	
	public final static int RESULT_CODE_SERVICE_ERROR				= 100;
	
	/** Defines callbacks for service binding, passed to bindService() **/
    private ServiceConnection _serviceConnection = new ServiceConnection() {
    	
    	@Override
    	public void onServiceConnected( ComponentName className, IBinder service ) {
    		//We've bound to NativeAudioPluginService, cast the IBinder and get NativeAudioPluginService instance
    		NativeAudioPluginServiceBinder binder = (NativeAudioPluginServiceBinder)service;
    		_service = binder.getService();
    		_isServiceBounded = true;
    	}
    	
    	@Override
    	public void onServiceDisconnected( ComponentName arg0 ) {
    		//only gets called in extreme situations, so don't rely on this callback
    		_isServiceBounded = false;
    	}
    	
    };
	
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        
        setContentView( R.layout.activity_nativeaudioplugin );
        
        //retrieve extras
        Bundle bundle = getIntent().getExtras();
        
        //this Activity can be launched through a Notification
        //if it's launched this way, we're not passing any extras to this Activity
        if ( bundle != null ) {
        	_audioPath = bundle.getString( NativeAudioPluginActivity.class.getName() + EXTRA_AUDIO_PATH );
        	_isMediaPlayerAutoPlay = bundle.getBoolean( ( NativeAudioPluginActivity.class.getName() + EXTRA_MEDIA_PLAYER_AUTO_PLAY ), false );
        }
        
        //get the references to our view objects
        _buttonPlayAudio = (Button)findViewById( R.id.NativeAudioPluginActivity_Button_playAudio );
        _buttonPauseAudio = (Button)findViewById( R.id.NativeAudioPluginActivity_Button_pauseAudio );
        _checkboxPersistAudio = (CheckBox)findViewById( R.id.NativeAudioPluginActivity_CheckBox_persistAudio );
        
        //set the properties of our View
        _checkboxPersistAudio.setChecked( _isAudioPersistent );
        
        //add onClick listeners
        _buttonPlayAudio.setOnClickListener( this );
        _buttonPauseAudio.setOnClickListener( this );
        _checkboxPersistAudio.setOnClickListener( this );
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	
    	if ( !NativeAudioPluginService.checkIfServiceIsRunning( this ) ) {
			startPluginService();
		}
		else {
		}
    }
    
    @Override
	public void onResume() {
		super.onResume();
		
		if ( _isMediaPlayerAutoPlay ) {
			_buttonPlayAudio.setEnabled( false );
        	_buttonPauseAudio.setEnabled( true );
		}
		else {
			_buttonPlayAudio.setEnabled( true );
        	_buttonPauseAudio.setEnabled( false );
		}
	}
    
    @Override
   	public void onPause() {
   		super.onPause();
   		
   		stopPluginService();
   	}
    
	@Override
	public void onClick( View v ) {
		if ( v == _buttonPlayAudio ) {
			playMedia();
		}
		else if ( v == _buttonPauseAudio ) {
			pauseMedia();
		}
		else if ( v == _checkboxPersistAudio ) {
			_isAudioPersistent = _checkboxPersistAudio.isChecked();
			
			DialogFragment newFragment = new AudioOnBackgroundNoticeDialogFragment();
		    newFragment.show( getSupportFragmentManager(), "audioOnBackgroundConfirmation" );
		}
	}
	
	@Override
	public void onDialogPositiveClick( DialogFragment dialog ) {
		_isAudioPersistent = true;
		_checkboxPersistAudio.setChecked( _isAudioPersistent );
		
		Intent returnIntent = new Intent();
		setResult( RESULT_OK, returnIntent ); 
		finish();
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		_isAudioPersistent = false;
		_checkboxPersistAudio.setChecked( _isAudioPersistent );
	}
	
	@Override
	public void onConfigurationChanged( Configuration newConfig ) {
	    super.onConfigurationChanged( newConfig );
	    
	    getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
	}
	
	private void startPluginService() {
		//bind to NativeAudioPluginService ( it is okay to start an already running Service )
        Intent intent = new Intent( this, NativeAudioPluginService.class );
        
        if ( _serviceResultReceiver == null ) {
        	//create the ServiceResultReceiver if we've established a connection to the service
    		_serviceResultReceiver = new ServiceResultReceiver( null );
        }
        
        //add in extra data
        intent.putExtra( NativeAudioPluginActivity.class.getName() + EXTRA_AUDIO_PATH, _audioPath );
        intent.putExtra( NativeAudioPluginActivity.class.getName() + EXTRA_MEDIA_PLAYER_AUTO_PLAY, _isMediaPlayerAutoPlay );
        intent.putExtra( NativeAudioPluginActivity.class.getName() + EXTRA_ACTIVITY_NAME_TO_LAUNCH, NativeAudioPluginActivity.class.getName() );
        intent.putExtra( NativeAudioPluginActivity.class.getName() + EXTRA_RESULT_RECEIVER , _serviceResultReceiver );
        
        bindService( intent, _serviceConnection, Context.BIND_AUTO_CREATE );
	}
	
	private void stopPluginService() {
		if ( _isServiceBounded ) {
			unbindService( _serviceConnection );
			_isServiceBounded = false;
		}
	}
	
	private void playMedia() {
		if ( _isServiceBounded ) {
			_service.playAudio();
			_buttonPlayAudio.setEnabled( false );
			_buttonPauseAudio.setEnabled( true );
		}
	}
	
	private void pauseMedia() {
		if ( _isServiceBounded ) {
			_service.pauseAudio();
			_buttonPlayAudio.setEnabled( true );
			_buttonPauseAudio.setEnabled( false );
		}
	}
	
	class ServiceResultReceiver extends ResultReceiver {
		public ServiceResultReceiver( Handler handler ) {
			super( handler );
		}

		@Override
		protected void onReceiveResult( int resultCode, Bundle resultData ) {
			if ( NativeAudioPluginActivity.RESULT_CODE_SERVICE_ERROR == resultCode ) {
				Toast.makeText( NativeAudioPluginActivity.this, R.string.NativeAudioPluginActivity_message_error, Toast.LENGTH_SHORT ).show();
				stopPluginService();
				finish();
			}
		}
	}

}