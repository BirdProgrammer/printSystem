package com.ct.ti;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import java.io.File;

public class FirstActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG="FirstActivity";
    private TextView textView_fileName;
    private TextView textView_filePath;
    private TextView textView_fileSize;
    private Button button_choseFile;
    private Button button_OK;
    private Uri uri;
    private final int REQUESTCODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        textView_filePath=findViewById(R.id.textView_path);
        textView_fileName=findViewById(R.id.textView_name);
        textView_fileSize=findViewById(R.id.textView_size);
        button_OK=findViewById(R.id.button_FirstOK);
        button_choseFile=findViewById(R.id.button_choseFile);
        button_OK.setOnClickListener(this);
        button_choseFile.setOnClickListener(this);
        requestPer();
    }

    @Override
    public void onClick(View v){
        Intent intent;
        switch (v.getId()){
            case R.id.button_FirstOK:
                if(uri==null){
                    Toast.makeText(FirstActivity.this,"请选择文件",Toast.LENGTH_SHORT).show();
                }else{
                    intent=new Intent(FirstActivity.this,SecondActivity.class);
                    intent.setData(uri);
                    startActivity(intent);
                    this.finish();
                }
                break;
            case R.id.button_choseFile:
                intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                String[] types={"application/pdf","text/plain","application/msword","application/vnd.ms-powerpoint","application/vnd.ms-excel","image/"};
//                intent.setType("application/pdf||text/plain");                  //pdf
//                intent.setType("application/vnd.ms-powerpoint");    //PPT
//                intent.setType("application/vnd.ms-excel");         //excel
//                intent.setType("application/msword");               //word
//                intent.setType("text/plain");                       //txt
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.putExtra(Intent.EXTRA_MIME_TYPES,types);
                startActivityForResult(intent,1);
                break;
            default:
                break;
        }
    }
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if (requestCode==1 && resultCode== Activity.RESULT_OK) {
            uri=data.getData();
            File file=new File(uri.getPath());
            textView_fileName.setText(file.getName());
            textView_filePath.setText(file.getAbsolutePath());
            textView_fileSize.setText(String.valueOf(file.length())+"B");
        }
    }
    @Override
    public void onBackPressed(){
        this.finish();
    }

    private  void requestPer() {
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions( new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUESTCODE );
        else
            return;
    }

    @Override
    public void onRequestPermissionsResult(int RequestCode,String[] permissions,int[] grantResults){
        switch (RequestCode){
            case REQUESTCODE:
                for(int i = 0;i<grantResults.length;i++){
                    if(grantResults[i] == PackageManager.PERMISSION_GRANTED){
                        continue;
                    }else{
                        requestPer();
                        Toast.makeText(this,"请同意",Toast.LENGTH_SHORT);
                    }
                }
                break;
            default:
                break;
        }
    }
}
