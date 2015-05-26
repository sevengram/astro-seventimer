package com.mydeepsky.seventimer.ui.answer;

import android.content.Context;
import android.view.View;

public interface IAnswerItemView {

    View getView(Context context);

    void setData(Object data);
}
