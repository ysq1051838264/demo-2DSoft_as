package com.example.administrator.barcode2ds;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zebra.adc.decoder.Barcode2DWithSoft;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/*
new demo 20171212
 */
public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";
    EditText data1;
    Button btn;
    Button share;
    Button cancel;
    RecyclerView dataRv;
    Barcode2DWithSoft barcode2DWithSoft = null;
    List<String> mDatas;

    MyRecyclerAdapter adapter;

    String fileName = "/sdcard/code.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT > 21) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //先判断有没有权限 ，没有就在这里进行权限的申请
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.CAMERA}, 1);
            } else {
                //说明已经获取到摄像头权限了 想干嘛干嘛
            }
            //读写内存权限
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // 请求权限
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        1);
            }

            int checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,}, 1);
                return;
            } else {
                // 上面已经写好的拨号方法
            }
        } else {
            //这个说明系统版本在6.0之下，不需要动态获取权限。
        }

        data1 = findViewById(R.id.editText);
        btn = findViewById(R.id.button);
        share = findViewById(R.id.share);
        cancel = findViewById(R.id.cancel);

        dataRv = findViewById(R.id.dataRv);
        dataRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        dataRv.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        mDatas = new ArrayList<String>();
//        for (int i = 0; i < 50; i++) {
//            mDatas.add("item" + i);
//        }
        adapter = new MyRecyclerAdapter(mDatas);
        dataRv.setAdapter(adapter);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScanBarcode();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(fileName);
                shareFile(MainActivity.this, file);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteFile(fileName);
                mDatas.clear();
                adapter.setModels(mDatas);
            }
        });

        new InitTask().execute();
    }

    public void put(String s) {
        try {
            FileOutputStream outStream = new FileOutputStream(fileName, true);
            OutputStreamWriter writer = new OutputStreamWriter(outStream, "gb2312");
            writer.write(s);
            writer.write("\n");
            writer.flush();
            writer.close();//记得关闭

            outStream.close();
        } catch (Exception e) {
            Log.e("m", "file write error");
        }
    }

    public boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                System.out.println("删除单个文件" + fileName + "成功！");
                Toast.makeText(MainActivity.this, "删除文件成功", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                System.out.println("删除单个文件" + fileName + "失败！");
                return false;
            }
        } else {
            System.out.println("删除单个文件失败：" + fileName + "不存在！");
            Toast.makeText(MainActivity.this, "文件不存在", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void shareFile(Context context, File file) {
        if (null != file && file.exists()) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            share.setType(getMimeType(file.getAbsolutePath()));//此处可发送多种文件
            share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(share, "分享文件"));
        } else {
            Toast.makeText(MainActivity.this, "分享文件不存在", Toast.LENGTH_SHORT).show();
        }
    }

    // 根据文件后缀名获得对应的MIME类型。
    private String getMimeType(String filePath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        String mime = "*/*";
        if (filePath != null) {
            try {
                mmr.setDataSource(filePath);
                mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            } catch (IllegalStateException e) {
                return mime;
            } catch (IllegalArgumentException e) {
                return mime;
            } catch (RuntimeException e) {
                return mime;
            }
        }
        return mime;
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        if (barcode2DWithSoft != null) {
            barcode2DWithSoft.stopScan();
            barcode2DWithSoft.close();
        }
        super.onDestroy();
        //android.os.Process.killProcess(Process.myPid());
    }


    public Barcode2DWithSoft.ScanCallback ScanBack = new Barcode2DWithSoft.ScanCallback() {
        @Override
        public void onScanComplete(int i, int length, byte[] bytes) {
            if (length < 1) {
                if (length == -1) {
                    data1.setText("Scan cancel");
                } else if (length == 0) {
                    data1.setText("Scan TimeOut");
                } else {
                    Log.i(TAG, "Scan fail");
                }
            } else {

                String barCode = new String(bytes, 0, length);
                // data1.setText(barCode);
                Boolean flag = false;
                if (mDatas.size() > 0) {
                    for (String s : mDatas) {
                        if (s.equals(barCode)) {
                            flag = true;
                            break;
                        }
                    }

                    if (!flag) {
                        put(barCode);
                        mDatas.add(barCode);
                        adapter.setModels(mDatas);
                        SoundManage.PlaySound(MainActivity.this, SoundManage.SoundType.SUCCESS);
                    }
                } else {
                    put(barCode);
                    mDatas.add(barCode);
                    adapter.setModels(mDatas);
                    SoundManage.PlaySound(MainActivity.this, SoundManage.SoundType.SUCCESS);
                }
            }

        }
    };

    private void ScanBarcode() {
        if (barcode2DWithSoft != null) {
            Log.i(TAG, "ScanBarcode");

            barcode2DWithSoft.scan();
            barcode2DWithSoft.setScanCallback(ScanBack);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 139 || keyCode == 280) {
            if (event.getRepeatCount() == 0) {
                ScanBarcode();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == 139 || keyCode == 280) {
            if (event.getRepeatCount() == 0) {
                barcode2DWithSoft.stopScan();
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    public class InitTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub

            if (barcode2DWithSoft == null) {
                barcode2DWithSoft = Barcode2DWithSoft.getInstance();
            }
            boolean reuslt = false;
            if (barcode2DWithSoft != null) {
                reuslt = barcode2DWithSoft.open(MainActivity.this);
                Log.i(TAG, "open=" + reuslt);

            }
            return reuslt;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                barcode2DWithSoft.setParameter(324, 1);
                barcode2DWithSoft.setParameter(300, 0); // Snapshot Aiming
                barcode2DWithSoft.setParameter(361, 0); // Image Capture Illumination

                // interleaved 2 of 5
                barcode2DWithSoft.setParameter(6, 1);
                barcode2DWithSoft.setParameter(22, 0);
                barcode2DWithSoft.setParameter(23, 55);

                Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "fail", Toast.LENGTH_SHORT).show();
            }
            mypDialog.cancel();
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            mypDialog = new ProgressDialog(MainActivity.this);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setMessage("init...");
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }

    }
}
