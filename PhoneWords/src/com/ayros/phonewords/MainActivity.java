package com.ayros.phonewords;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ayros.phonetowordsjava.PhoneToWords;
import com.ayros.phonetowordsjava.PhoneToWordsDB;

public class MainActivity extends Activity
{
	private class DatabaseLoader extends AsyncTask<Void, Void, Void>
	{
		private ProgressDialog pd;
		
		@Override
		protected void onPreExecute()
		{
			pd = new ProgressDialog(MainActivity.this);
			pd.setTitle("Building Database");
			pd.setMessage("Building database, this could take a few seconds...");
			pd.setCancelable(false);
			pd.setIndeterminate(true);
			
			pd.show();
		}
		
		@Override
		protected Void doInBackground(Void... arg0)
		{
			loadDB();
			if (ptw == null)
			{
				showToast("Error: Could not build database");
				
				MainActivity.this.finish();
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result)
		{
			pd.dismiss();
		}
	}
	
	private static final int MAX_NUMBER_LENGTH = 12;
	
	PhoneToWords ptw = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		AsyncTask<Void, Void, Void> databaseLoader = new DatabaseLoader();
		databaseLoader.execute((Void[])null);
		
		TextView txtNum = (TextView)findViewById(R.id.main_txtNum);
		txtNum.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void afterTextChanged(Editable arg0)
			{
				MainActivity.this.updateNumbers();
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
			{
				// Not needed
			}
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
			{
				// Not needed
			}
		});
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
			
			PhoneToWordsDB ptwDB = PhoneToWordsDB.fromProcessedWordList(dict, 50000);
			ptw = new PhoneToWords(ptwDB, 0);
		}
		catch (IOException e)
		{
			ptw = null;
		}
	}
	
	public void updateNumbers()
	{
		TextView txtNum = (TextView)findViewById(R.id.main_txtNum);
		String num = txtNum.getText().toString();
		if (num.matches(".*[^0-9].*"))
		{
			setList("Error: Only digits allowed");
			return;
		}
		if (num.length() == 0)
		{
			setList("");
			return;
		}
		if (num.length() > MAX_NUMBER_LENGTH)
		{
			setList("Error: Number too long");
			return;
		}
		
		List<String> words = getWords(num, 0);
		
		setList(words);
	}
	
	private void setList(String row)
	{
		List<String> rows = new ArrayList<String>();
		rows.add(row);
		
		setList(rows);
	}
	
	private void setList(List<String> rows)
	{
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, rows);
		
		ListView lstNums = (ListView)findViewById(R.id.main_lstNumbers);
		lstNums.setAdapter(adapter);
	}
	
	private List<String> getWords(String phoneNum, int maxDigits)
	{
		if (maxDigits > phoneNum.length())
		{
			return new ArrayList<String>();
		}
		
		ptw.setMaxDigits(maxDigits);
		List<String> words = ptw.getWords(phoneNum);
		
		return words.size() > 0 ? words : getWords(phoneNum, maxDigits + 1);
	}
	
	public void showToast(String msg)
	{
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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
