var NativeAudioVideoPlayerPlugin = {
	
	initVideoPluginThroughActivity: function ( success, fail, path, autoPlay, videoPersist ) {
		path = 				typeof path !== 'undefined' ? path : "";
		autoPlay = 			typeof autoPlay != 'undefined' ? autoPlay : false;
		videoPersist = 		typeof videoPersist != 'undefined' ? videoPersist : false;
		
		return cordova.exec(
			success,
			fail,
			"com.seventhmedia.android.nativeaudiovideoplayerplugin.plugins.NativeVideoPlugin",
			"initVideoPluginThroughActivity",
			[ path, autoPlay, videoPersist ]
		);
	},
	
	initVideoPlugin: function ( success, fail, path, autoPlay, videoPersist ) {
		path = 				typeof path !== 'undefined' ? path : "";
		autoPlay = 			typeof autoPlay != 'undefined' ? autoPlay : false;
		videoPersist = 		typeof videoPersist != 'undefined' ? videoPersist : false;
		
		return cordova.exec(
			success,
			fail,
			"com.seventhmedia.android.nativeaudiovideoplayerplugin.plugins.NativeVideoPlugin",
			"initVideoPlugin",
			[ path, autoPlay, videoPersist ]
		);
	},
	
	stopVideoPlugin: function ( success, fail ) {
		return cordova.exec(
			success,
			fail,
			"com.seventhmedia.android.nativeaudiovideoplayerplugin.plugins.NativeVideoPlugin",
			"stopVideoPlugin",
			[]
		);
	},
	
	playVideo: function ( success, fail ) {
		return cordova.exec(
			success,
			fail,
			"com.seventhmedia.android.nativeaudiovideoplayerplugin.plugins.NativeVideoPlugin",
			"playVideo",
			[]
		);
	},
	
	pauseVideo: function ( success, fail ) {
		return cordova.exec(
			success,
			fail,
			"com.seventhmedia.android.nativeaudiovideoplayerplugin.plugins.NativeVideoPlugin",
			"pauseVideo",
			[]
		);
	},
	
	initAudioPluginThroughActivity: function ( success, fail, path, autoPlay, audioPersist ) {
		path = 				typeof path !== 'undefined' ? path : "";
		autoPlay = 			typeof autoPlay != 'undefined' ? autoPlay : false;
		audioPersist = 		typeof audioPersist != 'undefined' ? audioPersist : false;
		
		return cordova.exec(
			success,
			fail,
			"com.seventhmedia.android.nativeaudiovideoplayerplugin.plugins.NativeAudioPlugin",
			"initAudioPluginThroughActivity",
			[ path, autoPlay, audioPersist ]
		);
	},
	
	initAudioPlugin: function ( success, fail, path, autoPlay, audioPersist ) {
		path = 				typeof path !== 'undefined' ? path : "";
		autoPlay = 			typeof autoPlay != 'undefined' ? autoPlay : false;
		audioPersist = 		typeof audioPersist != 'undefined' ? audioPersist : false;
		
		return cordova.exec(
			success,
			fail,
			"com.seventhmedia.android.nativeaudiovideoplayerplugin.plugins.NativeAudioPlugin",
			"initAudioPlugin",
			[ path, autoPlay, audioPersist ]
		);
	},
	
	stopAudioPlugin: function ( success, fail ) {
		return cordova.exec(
			success,
			fail,
			"com.seventhmedia.android.nativeaudiovideoplayerplugin.plugins.NativeAudioPlugin",
			"stopAudioPlugin",
			[]
		);
	},
	
	playAudio: function ( success, fail ) {
		return cordova.exec(
			success,
			fail,
			"com.seventhmedia.android.nativeaudiovideoplayerplugin.plugins.NativeAudioPlugin",
			"playAudio",
			[]
		);
	},
	
	pauseAudio: function ( success, fail ) {
		return cordova.exec(
			success,
			fail,
			"com.seventhmedia.android.nativeaudiovideoplayerplugin.plugins.NativeAudioPlugin",
			"pauseAudio",
			[]
		);
	}
	
};