package com.iknow.android.features.trim;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.databinding.DataBindingUtil;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.iknow.android.R;
import com.iknow.android.databinding.ActivityTrimmerLayoutBinding;
import com.iknow.android.interfaces.CompressorListener;
import com.iknow.android.interfaces.TrimmerListener;
import com.iknow.android.features.compress.VideoCompressor;

import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoTrimActivity extends AppCompatActivity implements TrimmerListener {

  private static final String TAG = "jason";
  private static final String VIDEO_PATH_KEY = "video-file-path";
  public static final int VIDEO_TRIM_REQUEST_CODE = 0x001;
  private ActivityTrimmerLayoutBinding mBinding;
  private ProgressDialog mProgressDialog;

  public static void call(FragmentActivity from, String videoPath) {
    if (!TextUtils.isEmpty(videoPath)) {
      Bundle bundle = new Bundle();
      bundle.putString(VIDEO_PATH_KEY, videoPath);
      Intent intent = new Intent(from, VideoTrimActivity.class);
      intent.putExtras(bundle);
      from.startActivityForResult(intent, VIDEO_TRIM_REQUEST_CODE);
    }
  }

  @Override protected void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    mBinding = DataBindingUtil.setContentView(this, R.layout.activity_trimmer_layout);
    Bundle bd = getIntent().getExtras();
    String path = "";
    if (bd != null) path = bd.getString(VIDEO_PATH_KEY);
    if (mBinding.trimmerView != null) {
      mBinding.trimmerView.setOnTrimVideoListener(this);
      mBinding.trimmerView.initVideoByURI(Uri.parse(path));
    }
  }

  @Override public void onResume() {
    super.onResume();
  }

  @Override public void onPause() {
    super.onPause();
    mBinding.trimmerView.onVideoPause();
    mBinding.trimmerView.setRestoreState(true);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    mBinding.trimmerView.onDestroy();
  }

  @Override public void onStartTrim() {
    buildDialog(getResources().getString(R.string.trimming)).show();
  }

  @Override public void onFinishTrim(String in,boolean addmusic) {
    //TODO: please handle your trimmed video url here!!!
    String out = "/storage/emulated/0/Android/data/com.iknow.android/cache/music360.mp4";
    //String out= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "final40.mp4";
    buildDialog(getResources().getString(R.string.compressing)).show();
    VideoCompressor.compress(this, in, out, new CompressorListener() {
      @Override public void onSuccess(String message) {
      }

      @Override public void onFailure(String message) {
      }

      @Override public void onFinish() {
        if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
       // finish();
          if(addmusic) {
              muxing(out);
          }else
          {
              finish();
          }
      }
    });
  }

  @Override public void onCancel() {
    mBinding.trimmerView.onDestroy();
    finish();
  }

  private ProgressDialog buildDialog(String msg) {
    if (mProgressDialog == null) {
      mProgressDialog = ProgressDialog.show(this, "", msg);
    }
    mProgressDialog.setMessage(msg);
    return mProgressDialog;
  }
  private void muxing(String path) {

    String outputFile = "";

    try {



      outputFile = path;

      MediaExtractor videoExtractor = new MediaExtractor();

      videoExtractor.setDataSource(path);

      MediaExtractor audioExtractor = new MediaExtractor();
      AssetFileDescriptor aufdd=getResources().openRawResourceFd(R.raw.sample);
      audioExtractor.setDataSource(aufdd.getFileDescriptor(), aufdd.getStartOffset(), aufdd.getLength());

      Log.d(TAG, "Video Extractor Track Count " + videoExtractor.getTrackCount());
      Log.d(TAG, "Audio Extractor Track Count " + audioExtractor.getTrackCount());

      MediaMuxer muxer = new MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

      videoExtractor.selectTrack(0);
      MediaFormat videoFormat = videoExtractor.getTrackFormat(0);
      int videoTrack = muxer.addTrack(videoFormat);

      audioExtractor.selectTrack(0);
      MediaFormat audioFormat = audioExtractor.getTrackFormat(0);
      int audioTrack = muxer.addTrack(audioFormat);

      Log.d(TAG, "Video Format " + videoFormat.toString());
      Log.d(TAG, "Audio Format " + audioFormat.toString());

      boolean sawEOS = false;
      int frameCount = 0;
      int offset = 100;
      int sampleSize = 1920 * 960;
      ByteBuffer videoBuf = ByteBuffer.allocate(sampleSize);
      ByteBuffer audioBuf = ByteBuffer.allocate(sampleSize);
      MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
      MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();


      videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
      audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);

      muxer.start();

      while (!sawEOS) {
        videoBufferInfo.offset = offset;
        videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset);


        if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
          Log.d(TAG, "saw input EOS.");
          sawEOS = true;
          videoBufferInfo.size = 0;

        } else {
          videoBufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
          videoBufferInfo.flags = videoExtractor.getSampleFlags();
          muxer.writeSampleData(videoTrack, videoBuf, videoBufferInfo);
          videoExtractor.advance();


          frameCount++;
          Log.d(TAG, "Frame (" + frameCount + ") Video PresentationTimeUs:" + videoBufferInfo.presentationTimeUs + " Flags:" + videoBufferInfo.flags + " Size(KB) " + videoBufferInfo.size / 1024);
          Log.d(TAG, "Frame (" + frameCount + ") Audio PresentationTimeUs:" + audioBufferInfo.presentationTimeUs + " Flags:" + audioBufferInfo.flags + " Size(KB) " + audioBufferInfo.size / 1024);

        }
      }

      Toast.makeText(getApplicationContext(), "frame:" + frameCount, Toast.LENGTH_SHORT).show();


      boolean sawEOS2 = false;
      int frameCount2 = 0;
      while (!sawEOS2) {
        frameCount2++;

        audioBufferInfo.offset = offset;
        audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset);

        if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
          Log.d(TAG, "saw input EOS.");
          sawEOS2 = true;
          audioBufferInfo.size = 0;
        } else {
          audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
          audioBufferInfo.flags = audioExtractor.getSampleFlags();
          muxer.writeSampleData(audioTrack, audioBuf, audioBufferInfo);
          audioExtractor.advance();
          Log.d(TAG, "Frame (" + frameCount + ") Video PresentationTimeUs:" + videoBufferInfo.presentationTimeUs + " Flags:" + videoBufferInfo.flags + " Size(KB) " + videoBufferInfo.size / 1024);
          Log.d(TAG, "Frame (" + frameCount + ") Audio PresentationTimeUs:" + audioBufferInfo.presentationTimeUs + " Flags:" + audioBufferInfo.flags + " Size(KB) " + audioBufferInfo.size / 1024);

        }
      }

      Toast.makeText(getApplicationContext(), "frame:" + frameCount2, Toast.LENGTH_SHORT).show();
      Log.d(TAG,"outttt---"+outputFile);
      muxer.stop();
      muxer.release();
        Toast.makeText(getApplicationContext(), "mixing done", Toast.LENGTH_SHORT).show();

        finish();

    } catch (IOException e) {
      Log.d(TAG, "Mixer Error 1 " + e.getMessage());
    } catch (Exception e) {
      Log.d(TAG, "Mixer Error 2 " + e.getMessage());
    }
  }
}
