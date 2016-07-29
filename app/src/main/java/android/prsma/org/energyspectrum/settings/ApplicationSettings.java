package android.prsma.org.energyspectrum.settings;


import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.text.method.DigitsKeyListener;
import android.widget.EditText;

public class ApplicationSettings extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		try	{
			super.onCreate(savedInstanceState);
			//addPreferencesFromResource(R.xml.main_preferences);
			EditTextPreference myEditTextPreference = (EditTextPreference) findPreference("installation_ID");
			EditText myEditText = (EditText)myEditTextPreference.getEditText(); 
			myEditText.setKeyListener(DigitsKeyListener.getInstance(false,true));

		} catch (Exception e) {
			e.printStackTrace();
		}
}
}

