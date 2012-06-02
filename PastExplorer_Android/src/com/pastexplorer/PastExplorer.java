package com.pastexplorer;

import com.pastexplorer.api.APIException;
import com.pastexplorer.api.Client;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class PastExplorer extends Activity implements OnClickListener {
	
	private Button _signInButton;
	private EditText _loginEditBox;
	private EditText _passwordEditBox;
	
	private static final String DEBUG_TAG = "PE Home";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        _signInButton = (Button)findViewById(R.id.signin);
        _signInButton.setOnClickListener(this);
        
        _loginEditBox = (EditText)findViewById(R.id.login);
        _passwordEditBox = (EditText)findViewById(R.id.password);
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.signin:
			try {
				Client client = new Client(_loginEditBox.getText().toString(), _passwordEditBox.getText().toString());
				boolean validCredentials = client.verifyCredentials();
				Log.d(DEBUG_TAG, "verification result: " + validCredentials);
			} catch (APIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}
	}
    
    
}