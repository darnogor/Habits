package com.blakit.petrenko.habits.view;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

/**
 * Created by user_And on 20.04.2016.
 */
public class NotZeroPercentFormatter implements ValueFormatter, YAxisValueFormatter {

    protected DecimalFormat mFormat;

    public NotZeroPercentFormatter() {
        mFormat = new DecimalFormat("###,###,##0.0");
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        if (value == 0) {
            return "";
        }
        return mFormat.format(value) + " %";
    }


    @Override
    public String getFormattedValue(float value, YAxis yAxis) {
        if (value == 0) {
            return "";
        }
        return mFormat.format(value) + " %";
    }
}
