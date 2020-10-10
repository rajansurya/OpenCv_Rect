package com.sqs.carddetect;

import android.widget.ImageView;

public interface ICropView {
    ImageView getPaper();
    DrawRectangle getPaperRect();
    ImageView getCroppedPaper();
}
