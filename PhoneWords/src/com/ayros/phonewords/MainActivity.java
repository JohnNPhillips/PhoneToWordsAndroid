package com.ayros.phonewords;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ayros.phonetowordsjava.PhoneToWords;
import com.ayros.phonetowordsjava.PhoneToWordsDB;

public class MainActivity extends Activity implements OnClickListener
{
	private static final int MAX_DIGITS = 12;
	
	PhoneToWordsDB ptwDB = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button btnSearch = (Button)findViewById(R.id.main_btnSearch);
		btnSearch.setOnClickListener(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void loadDB()
	{
		try
		{
			InputStream is = getAssets().open("dictionary.txt");
			
			String dict = IOUtils.toString(is);
			
			ptwDB = PhoneToWordsDB.fromProcessedWordList(dict, 50000);
		}
		catch (IOException e)
		{
			ptwDB = null;
		}
	}
	
	@Override
	public void onClick(View v)
	{
		TextView txtNum = (TextView)findViewById(R.id.main_txtNum);
		String num = txtNum.getText().toString();
		if (num.matches("[^0-9]"))
		{
			Toast.makeText(this, "Error: Phone number can only contain digits", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		if (num.length() == 0)
		{
			Toast.makeText(this, "Error: Phone number must be at least 1 digit", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		if (num.length() > MAX_DIGITS)
		{
			Toast.makeText(this, "Error: Phone number can only be at most " + MAX_DIGITS
					+ " digits", Toast.LENGTH_SHORT);
			return;
		}
		
		if (ptwDB == null)
		{
			loadDB();
			if (ptwDB == null)
			{
				Toast.makeText(this, "Unknown error when trying to lookup phone number",
						Toast.LENGTH_SHORT).show();
				return;
			}
		}
		
		PhoneToWords ptw = new PhoneToWords(ptwDB, 1);
		
		List<String> words = ptw.getWords(num);
		
		Toast.makeText(this, "Found " + words.size() + " matches", Toast.LENGTH_SHORT).show();
		
		ArrayAdapter<String> numAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, words);
		
		ListView lstNums = (ListView)findViewById(R.id.main_lstNumbers);
		lstNums.setAdapter(numAdapter);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.main_menu_about)
		{
			AlertDialog.Builder infoDialog = new AlertDialog.Builder(this);
			
			infoDialog.setMessage(R.string.app_about);
			infoDialog.setTitle("About");
			infoDialog.setCancelable(true);
			infoDialog.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
				}
			});
			infoDialog.create().show();
			
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
}
