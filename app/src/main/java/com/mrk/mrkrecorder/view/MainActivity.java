package com.mrk.mrkrecorder.view;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mrk.mrkrecorder.DeviceUtils;
import com.mrk.mrkrecorder.JianXiCamera;
import com.mrk.mrkrecorder.LocalMediaCompress;
import com.mrk.mrkrecorder.MediaRecorderActivity;
import com.mrk.mrkrecorder.R;
import com.mrk.mrkrecorder.StringUtils;
import com.mrk.mrkrecorder.model.AutoVBRMode;
import com.mrk.mrkrecorder.model.BaseMediaBitrateConfig;
import com.mrk.mrkrecorder.model.CBRMode;
import com.mrk.mrkrecorder.model.LocalMediaConfig;
import com.mrk.mrkrecorder.model.MediaRecorderConfig;
import com.mrk.mrkrecorder.model.OnlyCompressOverBean;
import com.mrk.mrkrecorder.model.VBRMode;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final int PERMISSION_REQUEST_CODE = 0x001;
    private final int CHOOSE_CODE = 0x000520;
    private static final String[] permissionManifest = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private ScrollView sv;
    private ProgressDialog mProgressDialog;

    // 包含：录制视频、本地选择压缩
    private RadioGroup rg_aspiration;

    /******************录制视频********************/
    // 是否全屏录制
    private Spinner spinner_need_full;
    // 经过检查你的摄像头
    private TextView tv_size;
    // 摄像头预览宽度
    private EditText et_width;
    // 摄像头预览高度
    private EditText et_height;
    // 视频最大帧率
    private EditText et_maxframerate;
    // 视频比特率
    private EditText et_bitrate;
    // 最大录制时间
    private EditText et_maxtime;
    // 最小录制时间
    private EditText et_mintime;
    // 开始录制按钮
    private Button bt_start;
    /******************************************/

    /*******************本地选择压缩*******************/
    private LinearLayout ll_only_compress;
    private View i_only_compress;
    // 包含：AutoVBRMode、VBRMode、CBRMode
    private RadioGroup rg_only_compress_mode;
    // 视频质量
    private LinearLayout ll_only_compress_crf;

    // 最大码率、额定码率
    private LinearLayout ll_only_compress_bitrate;
    // 最大码率
    private TextView tv_only_compress_maxbitrate;
    private EditText et_only_compress_maxbitrate;
    private EditText et_only_compress_crfSize;
    private EditText et_only_compress_bitrate;
    private EditText et_only_framerate;
    // 缩放视频比例
    private EditText et_only_scale;
    private Spinner spinner_only_compress;
    // 压缩转码速度
    private Spinner spinner_record;
    // 选择视频并压缩按钮
    private Button bt_choose;

    /******************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        permissionCheck();
        initEvent();
        initSmallVideo();
    }

    public void initSmallVideo() {
        // 设置拍摄视频缓存路径
        File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if (DeviceUtils.isZte()) {
            if (dcim.exists()) {
                JianXiCamera.setVideoCachePath(dcim + "/mrkrecorder/");
            } else {
                JianXiCamera.setVideoCachePath(dcim.getPath().replace("/sdcard/", "/sdcard-ext/") + "/mrkrecorder/");
            }
        } else {
            JianXiCamera.setVideoCachePath(dcim + "/mrkrecorder/");
        }

        // 初始化拍摄
        JianXiCamera.initialize(false, null);
    }

    private void initView() {
        sv = (ScrollView) findViewById(R.id.sv);

        rg_aspiration = (RadioGroup) findViewById(R.id.rg_aspiration);

        // 录制视频
        tv_size = (TextView) findViewById(R.id.tv_size);
        spinner_need_full = (Spinner) findViewById(R.id.spinner_need_full);
        et_width = (EditText) findViewById(R.id.et_width);
        et_height = (EditText) findViewById(R.id.et_height);
        et_maxframerate = (EditText) findViewById(R.id.et_maxframerate);
        et_bitrate = (EditText) findViewById(R.id.et_record_bitrate);
        et_maxtime = (EditText) findViewById(R.id.et_maxtime);
        et_mintime = (EditText) findViewById(R.id.et_mintime);
        bt_start = (Button) findViewById(R.id.bt_start);

        // 本地选择压缩
        i_only_compress = findViewById(R.id.i_only_compress);
        tv_only_compress_maxbitrate = (TextView) i_only_compress.findViewById(R.id.tv_maxbitrate);
        rg_only_compress_mode = (RadioGroup) i_only_compress.findViewById(R.id.rg_mode);
        ll_only_compress = (LinearLayout) findViewById(R.id.ll_only_compress);
        ll_only_compress_crf = (LinearLayout) i_only_compress.findViewById(R.id.ll_crf);
        ll_only_compress_bitrate = (LinearLayout) i_only_compress.findViewById(R.id.ll_bitrate);
        et_only_framerate = (EditText) findViewById(R.id.et_only_framerate);
        et_only_compress_crfSize = (EditText) i_only_compress.findViewById(R.id.et_crfSize);
        et_only_compress_maxbitrate = (EditText) i_only_compress.findViewById(R.id.et_maxbitrate);
        et_only_compress_bitrate = (EditText) i_only_compress.findViewById(R.id.et_bitrate);
        et_only_scale = (EditText) findViewById(R.id.et_only_scale);
        spinner_only_compress = (Spinner) findViewById(R.id.spinner_only_compress);
        spinner_record = (Spinner) findViewById(R.id.spinner_record);
        bt_choose = (Button) findViewById(R.id.bt_choose);
    }

    private void initEvent() {
        // AutoVBRMode、VBRMode、CBRMode选择
        rg_only_compress_mode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    // AutoVBRMode
                    case R.id.rb_auto:
                        ll_only_compress_crf.setVisibility(View.VISIBLE);
                        ll_only_compress_bitrate.setVisibility(View.GONE);
                        break;
                    // VBRMode
                    case R.id.rb_vbr:
                        ll_only_compress_crf.setVisibility(View.GONE);
                        ll_only_compress_bitrate.setVisibility(View.VISIBLE);
                        tv_only_compress_maxbitrate.setVisibility(View.VISIBLE);
                        et_only_compress_maxbitrate.setVisibility(View.VISIBLE);
                        break;
                    // CBRMode
                    case R.id.rb_cbr:
                        ll_only_compress_crf.setVisibility(View.GONE);
                        ll_only_compress_bitrate.setVisibility(View.VISIBLE);
                        tv_only_compress_maxbitrate.setVisibility(View.GONE);
                        et_only_compress_maxbitrate.setVisibility(View.GONE);
                        break;
                }
            }
        });

        // 录制视频、本地选择压缩选择
        rg_aspiration.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    // 录制视频
                    case R.id.rb_recorder:
                        sv.setVisibility(View.VISIBLE);
                        ll_only_compress.setVisibility(View.GONE);
                        break;
                    // 本地选择压缩选择
                    case R.id.rb_local:
                        sv.setVisibility(View.GONE);
                        ll_only_compress.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        spinner_need_full.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (((TextView) view).getText().toString().equals("false")) {
                    et_width.setVisibility(View.VISIBLE);
                } else {
                    et_width.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void permissionCheck() {
        if (Build.VERSION.SDK_INT >= 23) {
            boolean permissionState = true;

            for (String permission : permissionManifest) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionState = false;
                }
            }

            if (!permissionState) {
                ActivityCompat.requestPermissions(this, permissionManifest, PERMISSION_REQUEST_CODE);
            } else {
                setSupportCameraSize();
            }
        } else {
            setSupportCameraSize();
        }
    }

    private void setSupportCameraSize() {
        Camera back = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        List<Camera.Size> backSizeList = back.getParameters().getSupportedPreviewSizes();
        StringBuilder str = new StringBuilder();
        str.append("经过检查您的摄像头，如使用后置摄像头您可以输入的高度有：\n");
        for (Camera.Size bSize : backSizeList) {
            str.append(bSize.height + "、");
        }
        str.append("\n");
        back.release();

        Camera front = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        List<Camera.Size> frontSizeList = front.getParameters().getSupportedPreviewSizes();
        str.append("如使用前置摄像头您可以输入的高度有：\n");
        for (Camera.Size fSize : frontSizeList) {
            str.append(fSize.height + "、");
        }
        front.release();

        tv_size.setText(str);
    }

    public void startRecord(View c) {
        String s = spinner_need_full.getSelectedItem().toString().trim();
        boolean needFull = Boolean.parseBoolean(s);
        String width = et_width.getText().toString().trim();
        String height = et_height.getText().toString().trim();
        String maxFramerate = et_maxframerate.getText().toString().trim();
        String bitrate = et_bitrate.getText().toString().trim();
        String maxTime = et_maxtime.getText().toString().trim();
        String minTime = et_mintime.getText().toString().trim();

        if (!needFull && checkStrEmpty(width, "请输入预览宽度")) {
            return;
        }
        if (checkStrEmpty(height, "请输入预览高度")
                || checkStrEmpty(maxFramerate, "请输入视频最大帧率")
                || checkStrEmpty(bitrate, "请输入视频比特率")
                || checkStrEmpty(maxTime, "请输入最大录制时间")
                || checkStrEmpty(minTime, "请输入最小录制时间")
                ) {
            return;
        }

        BaseMediaBitrateConfig recordMode = new AutoVBRMode();
        if (!spinner_record.getSelectedItem().toString().trim().equals("none")) {
            recordMode.setVelocity(spinner_record.getSelectedItem().toString().trim());
        }

        MediaRecorderConfig config = new MediaRecorderConfig.Buidler()
                .fullScreen(needFull)
                .smallVideoWidth(needFull ? 0 : Integer.valueOf(width))
                .smallVideoHeight(Integer.valueOf(height))
                .recordTimeMax(Integer.valueOf(maxTime))
                .recordTimeMin(Integer.valueOf(minTime))
                .maxFrameRate(Integer.valueOf(maxFramerate))
                .videoBitrate(Integer.valueOf(bitrate))
                .captureThumbnailsTime(1)
                .build();
        MediaRecorderActivity.goSmallVideoRecorder(this, SendSmallVideoActivity.class.getName(), config);
    }

    /**
     * 选择视频并压缩，为了方便我采取了系统的API，所以也许在一些定制机上会取不到视频地址，
     * 所以选择手机里视频的代码根据自己业务写为妙。
     *
     * @param v
     */
    public void choose(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video/*");
        startActivityForResult(intent, CHOOSE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 选择视频并压缩
        if (requestCode == CHOOSE_CODE) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                Uri uri = data.getData();
                String[] proj = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.MIME_TYPE};

                Cursor cursor = getContentResolver().query(uri, proj, null,
                        null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int _data_num = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    int mime_type_num = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE);

                    String _data = cursor.getString(_data_num);
                    String mime_type = cursor.getString(mime_type_num);
                    if (!TextUtils.isEmpty(mime_type) && mime_type.contains("video") && !TextUtils.isEmpty(_data)) {
                        BaseMediaBitrateConfig compressMode = null;

                        int compressModeCheckedId = rg_only_compress_mode.getCheckedRadioButtonId();

                        if (compressModeCheckedId == R.id.rb_cbr) {
                            String bitrate = et_only_compress_bitrate.getText().toString().trim();
                            if (checkStrEmpty(bitrate, "请输入压缩额定码率")) {
                                return;
                            }
                            compressMode = new CBRMode(166, Integer.valueOf(bitrate));
                        } else if (compressModeCheckedId == R.id.rb_auto) {
                            String crfSize = et_only_compress_crfSize.getText().toString().trim();
                            if (TextUtils.isEmpty(crfSize)) {
                                compressMode = new AutoVBRMode();
                            } else {
                                compressMode = new AutoVBRMode(Integer.valueOf(crfSize));
                            }
                        } else if (compressModeCheckedId == R.id.rb_vbr) {
                            String maxBitrate = et_only_compress_maxbitrate.getText().toString().trim();
                            String bitrate = et_only_compress_bitrate.getText().toString().trim();

                            if (checkStrEmpty(maxBitrate, "请输入压缩最大码率") || checkStrEmpty(bitrate, "请输入压缩额定码率")) {
                                return;
                            }
                            compressMode = new VBRMode(Integer.valueOf(maxBitrate), Integer.valueOf(bitrate));
                        } else {
                            compressMode = new AutoVBRMode();
                        }

                        if (!spinner_only_compress.getSelectedItem().toString().trim().equals("none")) {
                            compressMode.setVelocity(spinner_only_compress.getSelectedItem().toString().trim());
                        }

                        String sRate = et_only_framerate.getText().toString().trim();
                        String scale = et_only_scale.getText().toString().trim();
                        int iRate = 0;
                        float fScale = 0;
                        if (!TextUtils.isEmpty(sRate)) {
                            iRate = Integer.valueOf(sRate);
                        }
                        if (!TextUtils.isEmpty(scale)) {
                            fScale = Float.valueOf(scale);
                        }

                        LocalMediaConfig.Buidler buidler = new LocalMediaConfig.Buidler();
                        final LocalMediaConfig config = buidler
                                .setVideoPath(_data)
                                .captureThumbnailsTime(1)
                                .doH264Compress(compressMode)
                                .setFramerate(iRate)
                                .setScale(fScale)
                                .build();

                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        showProgress("", "压缩中...", -1);
                                    }
                                });

                                OnlyCompressOverBean onlyCompressOverBean = new LocalMediaCompress(config).startCompress();

                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        hideProgress();
                                    }
                                });

                                Intent intent = new Intent(MainActivity.this, SendSmallVideoActivity.class);
                                intent.putExtra(MediaRecorderActivity.VIDEO_URI, onlyCompressOverBean.getVideoPath());
                                intent.putExtra(MediaRecorderActivity.VIDEO_SCREENSHOT, onlyCompressOverBean.getPicPath());
                                startActivity(intent);
                            }
                        }).start();
                    } else {
                        Toast.makeText(this, "选择的不是视频或者地址错误,也可能是这种方式定制神机取不到！", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    if (Manifest.permission.CAMERA.equals(permissions[i])) {
                        setSupportCameraSize();
                    } else if (Manifest.permission.RECORD_AUDIO.equals(permissions[i])) {

                    }
                }
            }
        }
    }

    private boolean checkStrEmpty(String str, String display) {
        if (TextUtils.isEmpty(str)) {
            Toast.makeText(this, display, Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    private void showProgress(String title, String message, int theme) {
        if (mProgressDialog == null) {
            if (theme > 0) {
                mProgressDialog = new ProgressDialog(this, theme);
            } else {
                mProgressDialog = new ProgressDialog(this);
            }

            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mProgressDialog.setCanceledOnTouchOutside(false);// 不能取消
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);// 设置进度条是否不明确
        }

        if (!StringUtils.isEmpty(title)) {
            mProgressDialog.setTitle(title);
        }
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }

    private void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

}
