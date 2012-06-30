package org.ofbiz.smartphone;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class ProfileManagementActivity extends Activity {
	//The Views
	private Spinner spinner=null;
	private EditText etProfileName=null;
	private EditText etServerAddress=null;
	private TextView tvPort=null;
	private EditText etPort=null;
	private EditText etUser=null;
	private EditText etPwd=null;
	private CheckBox chkIsDefault=null;
	private Button btnSaveProfile=null;
	private Button btnCancelProfile=null;
	private TextView tvProfileName=null;
	private TextView tvServerAddress=null;
	
	private final int PORT_NULL=-1;
	private boolean isNewProfile=false;
	private ArrayAdapter<String> spinnerAdapter=null; 
	private DatabaseHelper dbHelper=null;
	private Cursor cursor=null;
	private ContentValues profileValues=null;
	private final String TAG="ProfileManagementActivity";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Intent intent=this.getIntent();
        isNewProfile=intent.getBooleanExtra("isNewProfile", false);
        spinner=(Spinner)findViewById(R.id.spinnerProfiles);
        spinnerAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
        tvProfileName=(TextView)findViewById(R.id.tvProfileName);
        tvServerAddress=(TextView)findViewById(R.id.tvServerAddress);
        tvPort=(TextView)findViewById(R.id.tvPort);
        etProfileName=(EditText)findViewById(R.id.etProfileName);
        etServerAddress=(EditText)findViewById(R.id.etServerAddress);
        etPort=(EditText)findViewById(R.id.etPort);
        etUser=(EditText)findViewById(R.id.etUser);
        etPwd=(EditText)findViewById(R.id.etPwd);
        chkIsDefault=(CheckBox)findViewById(R.id.chkIsDefaultProfile);
        btnSaveProfile=(Button)findViewById(R.id.btnSaveProfile);
        btnCancelProfile=(Button)findViewById(R.id.btnCancelProfile);
         
        //TODO set the value of isNewProfile
        dbHelper=new DatabaseHelper(this);
        profileValues=new ContentValues();
        //
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        if(cursor!=null)
			cursor.close();
        cursor = dbHelper.queryAll();
        int rowcount=cursor.getCount();
        if(rowcount==0)
        {
        	isNewProfile=true;
        	spinner.setVisibility(Spinner.GONE);
        	tvProfileName.setVisibility(Spinner.VISIBLE);
        	etProfileName.setVisibility(Spinner.VISIBLE);
        }
        else
        {
        	isNewProfile=false;
        	spinner.setVisibility(Spinner.VISIBLE);
        	tvProfileName.setVisibility(Spinner.GONE);
        	etProfileName.setVisibility(Spinner.GONE);
        	reloadSpinner();     	
        }
        
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				loadProfile(spinner.getSelectedItemPosition());
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});

        btnSaveProfile.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if(checkUserInput()==false)
				{
					return;
				}
				
				profileValues.put("serveraddress", etServerAddress.getText().toString());
				profileValues.put("username", etUser.getText().toString());
				profileValues.put("password", etPwd.getText().toString());
				if(chkIsDefault.isChecked())
				{
					profileValues.put("isdefault",1 );
				}
				else
				{
					profileValues.put("isdefault", 0);
				}
				String port = etPort.getText().toString();
				if (port.length()!=0)
				{
					try{
						int portInt=Integer.parseInt(port);
						if( portInt>0 )
						{
							profileValues.put("port", portInt);
						}
					}catch(NumberFormatException e){
						Log.i(TAG, "NumberFormatException");
					}
				}
				else
				{
					profileValues.put("port", PORT_NULL);
				}

				//If this is a new profile, insert it; else update it
				if(isNewProfile==true)
				{
					profileValues.put("profilename", etProfileName.getText().toString());
					dbHelper.insertProfile(profileValues);
					if(cursor!=null)
						cursor.close();
					cursor=dbHelper.queryAll();
				}
				else
				{
					cursor.moveToPosition(spinner.getSelectedItemPosition());
					dbHelper.updateProfile(cursor.getInt(cursor.getColumnIndex("id")), profileValues);
					if(cursor!=null)
						cursor.close();
					cursor=dbHelper.queryAll();
				}
				
				setResult(RESULT_OK);
				finish();
			}
		});
        btnCancelProfile.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) 
			{
				setResult(RESULT_CANCELED);
				finish();
			}	
		});
	}
	
	private void reloadSpinner()
    {
		if(cursor!=null)
			cursor.close();
        cursor = dbHelper.queryAll();
    	int rowcount=cursor.getCount();
        System.out.println(rowcount);
        if(rowcount<=0)
    	{
        	startActivity(getIntent()); 
        	finish();
        	return;
    	}
    	cursor.moveToFirst();
    	spinnerAdapter.clear();
    	
        for (int index = 0; index < rowcount; index++) {
    		//System.out.println("index=" + index + ";" + cursor.getString(1));
    		spinnerAdapter.add(cursor.getString(cursor.getColumnIndex("profilename")));
    		spinner.setSelection(index);
    		if (cursor.getInt(cursor.getColumnIndex("isdefault"))==1)
    		{
				spinner.setSelection(index);
    		}
    		cursor.moveToNext();
    	}
        loadProfile(spinner.getSelectedItemPosition()); 
    }
	private void loadProfile(int index)
	{
		cursor.moveToPosition(index);
		if(etProfileName.isEnabled())
			etProfileName.setText(cursor.getString(cursor.getColumnIndex("profilename")));
		etServerAddress.setText(cursor.getString(cursor.getColumnIndex("serveraddress")));
		String port=cursor.getString(cursor.getColumnIndex("port"));
		if(port.equals(String.valueOf(PORT_NULL)))
		{
			etPort.setText("");
		}
		else
		{
			etPort.setText(port);
		}
		etUser.setText(cursor.getString(cursor.getColumnIndex("username")));
		etPwd.setText(cursor.getString(cursor.getColumnIndex("password"))); 
		chkIsDefault.setChecked(cursor.getInt(cursor.getColumnIndex("isdefault"))>0);
	}
	
	private boolean checkUserInput()
	{
		AlertDialog ad=new AlertDialog.Builder(ProfileManagementActivity.this).create();
		String port = etPort.getText().toString().trim();
		if ( etProfileName.getText().toString().trim().equals("") ||
			 etServerAddress.getText().toString().trim().equals(""))
		{
			ad.setMessage("The profile name and server address cannot be empty !");
			ad.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					return;
				}
			});
			ad.show();
			return false;
		}
		else if (port.length()>0)
		{
			if(port.contains("-") || port.contains("e"))
			{
				ad.setMessage("Invalide port number !");
				ad.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						return;
					}
				});
				ad.show();
				return false;
			}
			try{
				Integer.parseInt(port);
			}catch(NumberFormatException e){
				Log.i(TAG, "NumberFormatException");
				ad.setMessage("Invalide port number !");
				ad.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						return;
					}
				});
				ad.show();
				return false;
			}				
		}
		return true;
	}

	
    public boolean onCreateOptionsMenu(Menu menu) 
    {
   	    MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.menu_profile, menu);
        return true;
     }
 
      public boolean onOptionsItemSelected(MenuItem item) 
      {
         switch (item.getItemId()) {
            case R.id.menuAddProfile:
            	isNewProfile=true;
            	tvProfileName.setVisibility(Spinner.VISIBLE);
            	etProfileName.setVisibility(Spinner.VISIBLE);
            	spinner.setVisibility(Spinner.GONE);
            	etServerAddress.setText("");
            	etUser.setText("");
            	etPwd.setText("");
                return true;
            case R.id.menuDelProfile:
            	cursor.moveToPosition(spinner.getSelectedItemPosition());
                dbHelper.deleteProfile(cursor.getInt(cursor.getColumnIndex("id")));
                if(cursor!=null)
        			cursor.close();
                cursor=dbHelper.queryAll();
                reloadSpinner();
                return true;
           case R.id.quitter:
               finish();
               return true;
         }
         return false;
      }


      @Override
      public void onDestroy()
      {
      	cursor.close();
      	dbHelper.close();
      	super.onDestroy();
      }
      
}