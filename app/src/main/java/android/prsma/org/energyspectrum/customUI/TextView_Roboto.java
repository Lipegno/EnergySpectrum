package android.prsma.org.energyspectrum.customUI;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextView_Roboto extends TextView {

    private Context mContext;

    public TextView_Roboto(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            mContext = context;
            createFont();
    }

    public TextView_Roboto(Context context, AttributeSet attrs) {
            super(context, attrs);
            mContext = context;
            createFont();
    }

    public TextView_Roboto(Context context) {
            super(context);
            mContext = context;
            createFont();
    }

    public void createFont() {
            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Thin.ttf");
            setTypeface(font);
    }

    @Override
    public void setTypeface(Typeface tf) {
            super.setTypeface(tf);
    }

}
