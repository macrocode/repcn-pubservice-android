package com.seventhmedia.android.nativeaudiovideoplayerplugin;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

public class NativeVideoPluginActivity extends FragmentActivity implements 
	AudioOnBackgroundNoticeDialogFragment.NoticeDialogListener,
	OnClickListener,
	OnPreparedListener,
	OnErrorListener {
	
	private VideoView _vitamioVideo = 			null;
	private MediaPlayer _vitamioMediaPlayer = 			null;
	private CheckBox _checkboxAudioOnly;
	private ProgressBar _progressbarLoad;
	
	private String _videoPath = 					"";
	private Boolean _isMediaPlayerAutoPlay = 		false;
	private Boolean _isAudioOnly = 					false;
	
	//logic
	private Boolean _isMediaPlayerStarted = 		false;
	private Boolean _isStreamReady = 				false;
	
	private MediaController _vitamioMediaController = 	null;
	
	public final static String EXTRA_VIDEO_PATH 					= "_videoPath";
	public final static String EXTRA_MEDIA_PLAYER_AUTO_PLAY 		= "_isMediaPlayerAutoPlay";
	public final static String EXTRA_ACTIVITY_NAME_TO_LAUNCH		= "_activityNameToLaunch";
	public final static String EXTRA_RESULT_RECEIVER				= "_resultReceiver";
	
	public final static int RESULT_CODE_SERVICE_ERROR				= 100;
	
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        
        if ( !io.vov.vitamio.LibsChecker.checkVitamioLibs( this ) )
			return;
        
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        
        setContentView( R.layout.activity_nativevideoplugin );
        
        //retrieve extras
        Bundle bundle = getIntent().getExtras();
        
        //this Activity can be launched through a Notification
        //if it's launched this way, we're not passing any extras to this Activity
        if ( bundle != null ) {
        	_videoPath = bundle.getString( NativeVideoPluginActivity.class.getName() + EXTRA_VIDEO_PATH );
        	_isMediaPlayerAutoPlay = bundle.getBoolean( ( NativeVideoPluginActivity.class.getName() + EXTRA_MEDIA_PLAYER_AUTO_PLAY ), false );
        }
        
        //get the references to our view objects
        _vitamioVideo = (VideoView)findViewById( R.id.NativeVideoPluginActivity_VideoView_vitamioVideo );
        _checkboxAudioOnly = (CheckBox)findViewById( R.id.NativeVideoPluginActivity_CheckBox_audioOnly );
        _progressbarLoad = (ProgressBar)findViewById( R.id.NativeVideoPluginActivity_ProgressBar_load );
        
		_vitamioMediaController = new MediaController( this );
		_vitamioMediaController.setAnchorView( _vitamioVideo );
		_vitamioMediaController.setMediaPlayer( _vitamioVideo );
    	
		_vitamioVideo.setMediaController( _vitamioMediaController );
		
    	//set the properties of our View
        _checkboxAudioOnly.setChecked( _isAudioOnly );
        
        //add onClick listeners
        _checkboxAudioOnly.setOnClickListener( this );
        
        //
        _checkboxAudioOnly.setVisibility( View.GONE );
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    }
    
    @Override
	public void onResume() {
		super.onResume();
		
		_progressbarLoad.setVisibility( View.VISIBLE );
		startMedia();
	}
    
    @Override
   	public void onPause() {
   		super.onPause();
   		
   		stopMedia();
   	}
    
	@Override
	public void onClick( View v ) {
		if ( v == _checkboxAudioOnly ) {
			_isAudioOnly = _checkboxAudioOnly.isChecked();
			
			DialogFragment newFragment = new AudioOnBackgroundNoticeDialogFragment();
		    newFragment.show( getSupportFragmentManager(), "audioOnBackgroundConfirmation" );
		}
	}
	
	@Override
	public void onDialogPositiveClick( DialogFragment dialog ) {
		_isAudioOnly = true;
		_checkboxAudioOnly.setChecked( _isAudioOnly );
		
		Intent returnIntent = new Intent();
		setResult( RESULT_OK, returnIntent ); 
		finish();
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		_isAudioOnly = false;
		_checkboxAudioOnly.setChecked( _isAudioOnly );
	}
	
	@Override
	public void onConfigurationChanged( Configuration newConfig ) {
	    super.onConfigurationChanged( newConfig );
	    
	    getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
	}
	
	private void startMedia() {
		if ( !_isMediaPlayerStarted ) {
			_vitamioVideo.setOnErrorListener( this );
			_vitamioVideo.setOnPreparedListener( this );
			
			//_vitamioVideo.setVideoURI( Uri.parse( _videoPath ) );
			_vitamioVideo.setVideoPath( _videoPath );
			_vitamioVideo.setVideoQuality( io.vov.vitamio.MediaPlayer.VIDEOQUALITY_LOW );
			_vitamioVideo.setBufferSize( 32 );
			
			_isMediaPlayerStarted = true;
		}
	}
	
	private void stopMedia() {
		if ( _isMediaPlayerStarted ) {
			_vitamioVideo.setOnPreparedListener( null );
			_vitamioVideo.setOnErrorListener( null );
			
			if ( _isStreamReady ) {
				_vitamioMediaPlayer.stop();
			}
			_isMediaPlayerStarted = false;
			_isStreamReady = false;
		}
	}
	
	private void playMedia() {
		if ( ( _isMediaPlayerStarted ) && ( _isStreamReady) ) {
			_vitamioVideo.start();
		}
	}
	
	@Override
	public boolean onError( MediaPlayer mediaPlayer, int arg1, int arg2 ) {
		Toast.makeText( NativeVideoPluginActivity.this, R.string.NativeVideoPluginActivity_message_error, Toast.LENGTH_SHORT ).show();
		stopMedia();
		finish();
		return false;
	}

	@Override
	public void onPrepared( MediaPlayer mediaPlayer ) {
		_vitamioMediaPlayer = mediaPlayer;
		_isStreamReady = true;
		
		_vitamioVideo.setBufferSize( 32 );
		_vitamioMediaPlayer.setBufferSize( 32 );
		
		if ( _isMediaPlayerAutoPlay ) {
			playMedia();
		}
		
		_progressbarLoad.setVisibility( View.INVISIBLE );
		_vitamioVideo.setOnPreparedListener( null );
	}
}