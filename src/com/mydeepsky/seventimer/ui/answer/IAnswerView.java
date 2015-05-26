package com.mydeepsky.seventimer.ui.answer;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

public interface IAnswerView {

    View getView(Context context);

    void setData(Object data);

    void addChildView(Context context, LinearLayout parentLayout);
}
