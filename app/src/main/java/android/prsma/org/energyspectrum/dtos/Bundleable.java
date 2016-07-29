package android.prsma.org.energyspectrum.dtos;

import android.os.Bundle;

public interface Bundleable {

	public Bundle toBundle();
	
	public void fromBundle(Bundle b);
	
}
