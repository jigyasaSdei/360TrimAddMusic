package com.iknow.android.widget;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.VideoView;

public class ZVideoView extends VideoView {
  private int mVideoWidth = 480;
  private int mVideoHeight = 480;
  private int videoRealW = 1;
  private int videoRealH = 1;

  public ZVideoView(Context context) {
    super(context);
  }

  public ZVideoView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ZVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public void setVideoURI(Uri uri) {
    super.setVideoURI(uri);
    MediaMetadataRetriever retr = new MediaMetadataRetriever();
    retr.setDataSource(uri.getPath());
    String height = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
    String width = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
    try {
      videoRealH = Integer.parseInt(height);
      videoRealW = Integer.parseInt(width);
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    //int width = getDefaultSize(0, widthMeasureSpec);
    //int height = getDefaultSize(0, heightMeasureSpec);
    //if (height > width) {
    //  if (videoRealH > videoRealW) {
    //    mVideoHeight = height;
    //    mVideoWidth = width;
    //  } else {
    //    mVideoWidth = width;
    //    float r = videoRealH / (float) videoRealW;
    //    mVideoHeight = (int) (mVideoWidth * r);
    //  }
    //} else {
    //  if (videoRealH > videoRealW) {
    //    mVideoHeight = height;
    //    float r = videoRealW / (float) videoRealH;
    //    mVideoWidth = (int) (mVideoHeight * r);
    //  } else {
    //    mVideoHeight = height;
    //    mVideoWidth = width;
    //  }
    //}
    //if (videoRealH == videoRealW && videoRealH == 1) {
    //  super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    //} else {
    //  setMeasuredDimension(mVideoWidth, mVideoHeight);
    //}

  }
}
