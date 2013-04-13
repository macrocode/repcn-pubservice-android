package com.seventhmedia.android.nativeaudiovideoplayerplugin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class AudioOnBackgroundNoticeDialogFragment extends DialogFragment {
	
	public interface NoticeDialogListener {
        public void onDialogPositiveClick( DialogFragment dialog );
        public void onDialogNegativeClick( DialogFragment dialog );
    }
	
    @Override
    public Dialog onCreateDialog( Bundle savedInstanceState ) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
        builder.setTitle( R.string.AudioOnBackgroundNoticeDialogFragment_title )
        	   .setMessage( R.string.AudioOnBackgroundNoticeDialogFragment_message )
        	   .setIcon( R.drawable.ic_service_background_audio_info )
        	   .setPositiveButton( R.string.AudioOnBackgroundNoticeDialogFragment_ok, new DialogInterface.OnClickListener() {
        		   public void onClick( DialogInterface dialog, int id ) {
        			   // Send the positive button event back to the host activity
                       _listener.onDialogPositiveClick( AudioOnBackgroundNoticeDialogFragment.this );
                   }
               })
               .setNegativeButton( R.string.AudioOnBackgroundNoticeDialogFragment_cancel, new DialogInterface.OnClickListener() {
                   public void onClick( DialogInterface dialog, int id ) {
                	   // Send the negative button event back to the host activity
                       _listener.onDialogNegativeClick( AudioOnBackgroundNoticeDialogFragment.this );
                   }
               });
        
        // Create the AlertDialog object and return it
        return builder.create();
    }
    
	// Use this instance of the interface to deliver action events
	NoticeDialogListener _listener;
	
	// Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
	@Override
	public void onAttach( Activity activity ) {
	    super.onAttach( activity );
	    
	    // Verify that the host activity implements the callback interface
	    try {
	        // Instantiate the NoticeDialogListener so we can send events to the host
	        _listener = (NoticeDialogListener) activity;
	    }
	    catch ( ClassCastException e ) {
	        // The activity doesn't implement the interface, throw exception
	        throw new ClassCastException( activity.toString() + " must implement NoticeDialogListener" );
	    }
	}
    
}