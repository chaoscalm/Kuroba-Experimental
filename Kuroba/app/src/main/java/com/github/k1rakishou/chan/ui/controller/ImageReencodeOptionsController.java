package com.github.k1rakishou.chan.ui.controller;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.util.Pair;

import com.github.k1rakishou.chan.R;
import com.github.k1rakishou.chan.core.navigation.RequiresNoBottomNavBar;
import com.github.k1rakishou.chan.core.presenter.ImageReencodingPresenter;
import com.github.k1rakishou.chan.ui.helper.ImageOptionsHelper;
import com.github.k1rakishou.chan.ui.theme.ThemeHelper;

import javax.inject.Inject;

import static com.github.k1rakishou.chan.Chan.inject;
import static com.github.k1rakishou.chan.utils.AndroidUtils.getString;

public class ImageReencodeOptionsController
        extends BaseFloatingController
        implements View.OnClickListener,
        RadioGroup.OnCheckedChangeListener,
        RequiresNoBottomNavBar {
    private final static String TAG = "ImageReencodeOptionsController";

    @Inject
    ThemeHelper themeHelper;

    private ImageReencodeOptionsCallbacks callbacks;
    private ImageOptionsHelper imageReencodingHelper;
    private Bitmap.CompressFormat imageFormat;
    private Pair<Integer, Integer> dims;

    private ConstraintLayout viewHolder;
    private RadioGroup radioGroup;
    private AppCompatSeekBar quality;
    private AppCompatSeekBar reduce;
    private TextView currentImageQuality;
    private TextView currentImageReduce;
    private AppCompatButton cancel;
    private AppCompatButton ok;
    private AppCompatRadioButton reencodeImageAsIs;

    private ImageReencodingPresenter.ReencodeSettings lastSettings;
    private boolean ignoreSetup;

    private SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!ignoreSetup) { //this variable is to ignore any side effects of setting progress while loading last options
                if (seekBar == quality) {
                    if (progress < 1) {
                        //for API <26; the quality can't be lower than 1
                        seekBar.setProgress(1);
                        progress = 1;
                    }
                    currentImageQuality.setText(getString(R.string.image_quality, progress));
                } else if (seekBar == reduce) {
                    currentImageReduce.setText(getString(R.string.scale_reduce,
                            dims.first,
                            dims.second,
                            (int) (dims.first * ((100f - (float) progress) / 100f)),
                            (int) (dims.second * ((100f - (float) progress) / 100f)),
                            100 - progress
                    ));
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //do nothing
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //do nothing
        }
    };

    public ImageReencodeOptionsController(
            Context context,
            ImageOptionsHelper imageReencodingHelper,
            ImageReencodeOptionsCallbacks callbacks,
            Bitmap.CompressFormat imageFormat,
            Pair<Integer, Integer> dims,
            ImageReencodingPresenter.ReencodeSettings lastOptions
    ) {
        super(context);
        inject(this);

        this.imageReencodingHelper = imageReencodingHelper;
        this.callbacks = callbacks;
        this.imageFormat = imageFormat;
        this.dims = dims;
        lastSettings = lastOptions;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.layout_image_reencoding;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        viewHolder = view.findViewById(R.id.reencode_image_view_holder);
        radioGroup = view.findViewById(R.id.reencode_image_radio_group);
        quality = view.findViewById(R.id.reecode_image_quality);
        reduce = view.findViewById(R.id.reecode_image_reduce);
        currentImageQuality = view.findViewById(R.id.reecode_image_current_quality);
        currentImageReduce = view.findViewById(R.id.reecode_image_current_reduce);
        reencodeImageAsIs = view.findViewById(R.id.reencode_image_as_is);
        AppCompatRadioButton reencodeImageAsJpeg = view.findViewById(R.id.reencode_image_as_jpeg);
        AppCompatRadioButton reencodeImageAsPng = view.findViewById(R.id.reencode_image_as_png);
        cancel = view.findViewById(R.id.reencode_image_cancel);
        ok = view.findViewById(R.id.reencode_image_ok);

        viewHolder.setOnClickListener(this);
        cancel.setOnClickListener(this);
        ok.setOnClickListener(this);
        radioGroup.setOnCheckedChangeListener(this);

        quality.setOnSeekBarChangeListener(listener);
        reduce.setOnSeekBarChangeListener(listener);

        setReencodeImageAsIsText();

        if (imageFormat == Bitmap.CompressFormat.PNG) {
            quality.setEnabled(false);
            reencodeImageAsPng.setEnabled(false);
            reencodeImageAsPng.setButtonTintList(ColorStateList.valueOf(themeHelper.getTheme().textSecondary));
            reencodeImageAsPng.setTextColor(ColorStateList.valueOf(themeHelper.getTheme().textSecondary));
        } else if (imageFormat == Bitmap.CompressFormat.JPEG) {
            reencodeImageAsJpeg.setEnabled(false);
            reencodeImageAsJpeg.setButtonTintList(ColorStateList.valueOf(themeHelper.getTheme().textSecondary));
            reencodeImageAsJpeg.setTextColor(ColorStateList.valueOf(themeHelper.getTheme().textSecondary));
        }

        currentImageReduce.setText(getString(R.string.scale_reduce,
                dims.first,
                dims.second,
                dims.first,
                dims.second,
                100 - reduce.getProgress()
        ));

        if (lastSettings != null) {
            //this variable is to ignore any side effects of checking/setting progress on these views
            ignoreSetup = true;
            quality.setProgress(lastSettings.getReencodeQuality());
            reduce.setProgress(lastSettings.getReducePercent());
            switch (lastSettings.getReencodeType()) {
                case AS_JPEG:
                    reencodeImageAsJpeg.setChecked(true);
                    break;
                case AS_PNG:
                    reencodeImageAsPng.setChecked(true);
                    break;
                case AS_IS:
                    reencodeImageAsIs.setChecked(true);
                    break;
            }
            ignoreSetup = false;
        }
    }

    private void setReencodeImageAsIsText() {
        String format;

        if (imageFormat == Bitmap.CompressFormat.PNG) {
            format = "PNG";
        } else if (imageFormat == Bitmap.CompressFormat.JPEG) {
            format = "JPEG";
        } else {
            format = "Unknown";
        }

        reencodeImageAsIs.setText(getString(R.string.reencode_image_as_is, format));
    }

    @Override
    public boolean onBack() {
        imageReencodingHelper.pop();
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v == ok) {
            callbacks.onOk(getReencode());
        } else if (v == cancel || v == viewHolder) {
            callbacks.onCanceled();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (!ignoreSetup) {
            // this variable is to ignore any side effects of checking during last options load
            int index = group.indexOfChild(group.findViewById(group.getCheckedRadioButtonId()));

            // 0 - AS IS
            // 1 - AS JPEG
            // 2 - AS PNG

            // when re-encoding image as png it ignores the compress quality option so we can just
            // disable the quality seekbar
            if (index == 2 || (index == 0 && imageFormat == Bitmap.CompressFormat.PNG)) {
                quality.setProgress(100);
                quality.setEnabled(false);
            } else {
                quality.setEnabled(true);
            }
        }
    }

    private ImageReencodingPresenter.ReencodeSettings getReencode() {
        int index = radioGroup.indexOfChild(radioGroup.findViewById(radioGroup.getCheckedRadioButtonId()));
        ImageReencodingPresenter.ReencodeType reencodeType = ImageReencodingPresenter.ReencodeType.fromInt(index);

        return new ImageReencodingPresenter.ReencodeSettings(reencodeType, quality.getProgress(), reduce.getProgress());
    }

    public interface ImageReencodeOptionsCallbacks {
        void onCanceled();

        void onOk(ImageReencodingPresenter.ReencodeSettings reencodeSettings);
    }
}