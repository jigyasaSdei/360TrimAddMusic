package com.iknow.android.features.select;

import android.Manifest;
import android.annotation.SuppressLint;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import com.iknow.android.R;
import com.iknow.android.databinding.VideoSelectLayoutBinding;
import com.iknow.android.features.trim.VideoTrimActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;
import iknow.android.utils.callback.SimpleCallback;
import iknow.android.utils.callback.SingleCallback;

public class SelectVideoActivity extends AppCompatActivity implements View.OnClickListener {

  private VideoSelectLayoutBinding mBinding;
  private SelectVideoAdapter mVideoSelectAdapter;
  private String mVideoPath;
  private LoaderVideoManager mVideoLoadManager;

  @SuppressLint("CheckResult")
  @Override protected void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    mVideoLoadManager = new LoaderVideoManager();
    mVideoLoadManager.setLoader(new LoaderVideoCursor());
    mBinding = DataBindingUtil.setContentView(this, R.layout.video_select_layout);
    mBinding.videoShoot.setOnClickListener(this);
    mBinding.mBtnBack.setOnClickListener(this);
    mBinding.nextStep.setOnClickListener(this);

    mBinding.nextStep.setTextAppearance(this, R.style.gray_text_18_style);
    mBinding.nextStep.setEnabled(false);

    RxPermissions rxPermissions = new RxPermissions(this);
    rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE).subscribe(granted -> {
          if (granted) { // Always true pre-M
            mVideoLoadManager.load(this, new SimpleCallback() {
              @SuppressWarnings("unchecked")
              @Override public void success(Object obj) {
                if (mVideoSelectAdapter == null) {
                  mVideoSelectAdapter = new SelectVideoAdapter(SelectVideoActivity.this, (Cursor)obj);
                  mVideoSelectAdapter.setItemClickCallback(new SingleCallback<Boolean, String>() {
                    @Override public void onSingleCallback(Boolean isSelected, String videoPath) {
                      if (!TextUtils.isEmpty(videoPath)) mVideoPath = videoPath;
                      mBinding.nextStep.setEnabled(isSelected);
                      mBinding.nextStep.setTextAppearance(SelectVideoActivity.this, isSelected ? R.style.blue_text_18_style : R.style.gray_text_18_style);
                    }
                  });
                } else {
                  mVideoSelectAdapter.swapCursor((Cursor)obj);
                }
                if (mBinding.videoGridview.getAdapter() == null) {
                  mBinding.videoGridview.setAdapter(mVideoSelectAdapter);
                }
                mVideoSelectAdapter.notifyDataSetChanged();
              }
            });
          } else {
            finish();
          }
        });
  }

  @Override protected void onDestroy() {
    super.onDestroy();
  }

  @Override public void onClick(View v) {
    if (v.getId() == mBinding.mBtnBack.getId()) {
      finish();
    } else if (v.getId() == mBinding.nextStep.getId()) {
      VideoTrimActivity.call(SelectVideoActivity.this, mVideoPath);
    }
  }
}
