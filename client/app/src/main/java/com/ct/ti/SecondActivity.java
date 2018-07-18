package com.ct.ti;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class SecondActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{

    private String MAC="B8:27:EB:BE:AE:FC";
    private static final int RequestPermission_bluetooth=1;
    private static final String TAG="SecondActivity";
    private String filePath;
    private Button button_OK;
    private Button submit_bt;

    //所有使用到的控件
    private RadioGroup orientation_rg;
    private RadioGroup range_rg;
    private RadioButton optional_rb;
    private RadioButton all_rb;
    private RadioGroup sides_rg;
    private RadioGroup quality_rg;
    private RadioGroup chromaticity_rg;
    private Spinner MediaSize_sp;
    private Spinner margin_sp;
    private EditText first_page_et;
    private EditText last_page_et;

    //文本框的内容
    private String sides_str;
    private String range_str;
    private String quality_str;
    private String chromaticity_str;
    private String orientation_str;
    private String MediaSize_str;
    private String margin_str;
    private int first_page_int = 1;
    private int last_page_int = 1;
    private int copies_int = 1;
    //private Spinner spinner;
    private List<Map<String,Object>> data;

    //应该传递的参数的值,将Text的值转换为实际的参数
    private String sides_para;
    private String range_para;
    private String quality_para;
    private String chromaticity_para;
    private String orientation_para;
    private String MediaSize_para;
    private String margin_para;
    private int first_page_para = 1;
    private int last_page_para = 1;
    private int copies_para = 1;

    private BluetoothAdapter mBluetoothAdapter;
    private final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket clientSocket;
    private OutputStream outputStream;//输出流
    private OutputStreamWriter outputStreamWriter;
    private InputStream inputStream;
    private InputStreamReader inputStreamReader;
    private JsonWriter jsonWriter;
    private JsonReader jsonReader;
    private ProgressDialog progressDialog;
    private BluetoothDevice remoteDevice;
    @SuppressLint("HandlerLeak")
    public Handler uiHandler=new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:                                 //found
                    submit_bt.setEnabled(true);
                    break;
                case 2:                             //not found
                    Toast.makeText(SecondActivity.this,"连接蓝牙失败，请重试",Toast.LENGTH_LONG).show();
                    break;
                case 3:
                    Toast.makeText(SecondActivity.this,"传输失败",Toast.LENGTH_LONG).show();//传输失败
                    progressDialog.dismiss();
                    break;
                case 4:                                     //开始传输
                    progressDialog.show();
                    break;
                case 5:                                     //正在传输
                    progressDialog.incrementProgressBy(msg.arg1);
                    break;
                case 6:                                     //传输成功
                    progressDialog.dismiss();
                    showAlertDialog();
                    break;
                default:
                    break;
            }
        }
    };
    private IChangeCoutCallback callback = new IChangeCoutCallback() {
        @Override
        public void change(int count) {            //总价变化
            copies_int = count;
        }
    };
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    Log.e(TAG,device.getAddress());
                    if(device.getAddress().equals(MAC)) {
                        Log.e(TAG, "true");
                        remoteDevice = device;
                        try{
                            clientSocket = remoteDevice.createRfcommSocketToServiceRecord(MY_UUID);
                            //开始连接蓝牙，如果没有配对则弹出对话框提示我们进行配对
                            clientSocket.connect();
                            Message message=new Message();
                            message.what=1;
                            uiHandler.sendMessage(message);
                        }catch (IOException e){
                            e.printStackTrace();
                            Message message=new Message();
                            message.what=2;
                            uiHandler.sendMessage(message);
                        }
                    }
                    else
                        Log.e(TAG,"false");
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //已搜素完成
                Message message=new Message();
                message.what=2;
                uiHandler.sendMessage(message);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        Intent intent=getIntent();
        String action = intent.getAction();
        if(intent.ACTION_VIEW.equals(action)){
            filePath=intent.getDataString();
        }else{
            Uri uri=getIntent().getData();
            filePath=uri.getPath();
        }



        final CounterView copies = (CounterView) findViewById(R.id.Copies);
        copies.setCallback(callback);

        progressDialog=new ProgressDialog(SecondActivity.this);
        progressDialog.setTitle("上传文件");
        progressDialog.setMessage("上传文件进度");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setIndeterminate(false);
        button_OK=findViewById(R.id.button_SecondOK);
        submit_bt = findViewById(R.id.submit);
        sides_rg = findViewById(R.id.sides);
        orientation_rg= findViewById(R.id.orientation);    //方向为竖或者横
        quality_rg = findViewById(R.id.quality);
        chromaticity_rg = findViewById(R.id.Chromaticity);
        range_rg = findViewById(R.id.PageRanges);
        all_rb = findViewById(R.id.all);
        optional_rb = findViewById(R.id.optional);
        MediaSize_sp = findViewById(R.id.MediaName);
        margin_sp = findViewById(R.id.MediaPrintableArea);
        first_page_et = findViewById(R.id.first_page);
        last_page_et = findViewById(R.id.last_page);
        first_page_et.setEnabled(false);
        last_page_et.setEnabled(false);
//      tv= (TextView) findViewById(R.id.tv);
        all_rb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                first_page_et.setEnabled(false);
                last_page_et.setEnabled(false);
                first_page_et.setText(null);
                last_page_et.setText(null);
            }
        });
        optional_rb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                first_page_et.setEnabled(true);
                last_page_et.setEnabled(true);
            }
        });


        submit_bt.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                sides_str = ((RadioButton) findViewById(sides_rg.getCheckedRadioButtonId())).getText().toString();
                orientation_str = ((RadioButton) findViewById(orientation_rg.getCheckedRadioButtonId())).getText().toString();
                range_str = ((RadioButton) findViewById(range_rg.getCheckedRadioButtonId())).getText().toString();
                chromaticity_str = ((RadioButton) findViewById(chromaticity_rg.getCheckedRadioButtonId())).getText().toString();
                quality_str = ((RadioButton) findViewById(quality_rg.getCheckedRadioButtonId())).getText().toString();
                MediaSize_str = MediaSize_sp.getSelectedItem().toString();
                margin_str = margin_sp.getSelectedItem().toString();
                String temp = null;
                if (range_str.equals("自定义")) {
                    if (first_page_et.getText().length() == 0 || last_page_et.getText().length() == 0) {
                        Toast.makeText(SecondActivity.this, "请输入页码范围", Toast.LENGTH_LONG).show();
                    }
                    else {
                        first_page_int = Integer.parseInt(first_page_et.getText().toString());
                        last_page_int = Integer.parseInt(last_page_et.getText().toString());
                        if (first_page_int > last_page_int) {
                            Toast.makeText(SecondActivity.this, "页码范围无效，请重新输入", Toast.LENGTH_LONG).show();
                        } else {
                            temp = "你选择了" + sides_str + "打印" + "\n" + "打印效果：" + chromaticity_str + "\n"
                                    + "打印方向：" + orientation_str + "\n" + "打印范围为"
                                    + first_page_int + "-" + last_page_int + "\n" +
                                    "打印质量：" + quality_str + "\n" + "纸张大小：" + MediaSize_str + "\n"
                                    + "打印边距：" + margin_str + "\n" + "共打印" + copies_int + "份";
                            show(temp);
                        }
                    }

                } else {
                    temp = "你选择了" + sides_str + "打印" + "\n" + "打印效果：" + chromaticity_str + "\n"
                             + "打印方向：" + orientation_str + "\n" + "打印范围为"
                            + range_str + "\n" +
                            "打印质量：" + quality_str + "\n" + "纸张大小：" + MediaSize_str + "\n"
                            + "打印边距：" + margin_str + "\n" + "共打印" + copies_int + "份";
                    show(temp);
                }
                initParam();

            }
            private void initParam() {
                switch (chromaticity_str) {
                    case "黑白": chromaticity_para = "MONOCHROME";break;
                    case "彩印": chromaticity_para = "COLOR";break;
                }
                MediaSize_para = "MediaSize.ISO".concat(MediaSize_str);
                range_para = range_str;
                first_page_para = first_page_int;
                last_page_para = last_page_int;
                switch (orientation_str) {
                    case "竖屏" : orientation_para = "PORTRAIT";break;
                    case "横屏" : orientation_para = "LANDSCAPE";break;
                }
                switch (quality_str) {
                    case "高" : quality_para = "HIGH";break;
                    case "普通" : quality_para = "NORMAL";break;
                    case "低" : quality_para = "DRAFT";break;
                }
                switch (sides_str) {
                    case "单面" : sides_para = "ONE_SIDED";break;
                    case "双面" : sides_para = "DUPLEX";break;
                }
                copies_para = copies_int;
                margin_para = margin_str;
//                String tempt = "你选择了" + sides_para + "打印" + "\n" + "打印效果：" + "\n"
//                        + chromaticity_para + "打印方向：" + orientation_para + "\n" + "打印范围为"
//                        + first_page_para + "-" + last_page_para + "\n" +
//                        "打印质量：" + quality_para + "\n" + "纸张大小：" + MediaSize_para + "\n"
//                        + "打印边距：" + margin_para + "\n" + "共打印" + copies_para + "份";
//
//                Toast.makeText(SecondActivity.this,tempt,Toast.LENGTH_LONG).show();
            }
            private void show(String temp)
            {
                AlertDialog.Builder printMessege = new AlertDialog.Builder(SecondActivity.this);
                printMessege.setTitle("打印参数")
                        .setMessage(temp)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which){
                                new thread_uploadFile().start();
                            }
                        })
                        .setNegativeButton("不，我要修改", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        });

        button_OK.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(Build.VERSION.SDK_INT >= 23){
                    if (ContextCompat.checkSelfPermission(SecondActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(SecondActivity.this, new String[]{Manifest.permission.BLUETOOTH}, RequestPermission_bluetooth);
                    } else {
                        new thread_searchDevice().start();
                    }
                }else{
                    new thread_searchDevice().start();
                }
            }
        });

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);
    }
//    private void initMySpinner() {
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
//                this, R.array.phones_array,
//                android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(adapter);
//        spinner.setPrompt("test");
//        spinner.setOnItemSelectedListener(new SpinnerOnSelectedListener());
//    }
//
//    class SpinnerOnSelectedListener implements OnItemSelectedListener{
//        public void onItemSelected(AdapterView<?> adapterView, View view, int position,
//                                   long id) {
//            // TODO Auto-generated method stub
//            selected = adapterView.getItemAtPosition(position).toString();
//            System.out.println("selected===========>" + selected);
//        }
//
//        public void onNothingSelected(AdapterView<?> arg0) {
//            // TODO Auto-generated method stub
//            System.out.println("selected===========>" + "Nothing");
//        }
//    }
//}

    @Override
    public void onRequestPermissionsResult(int RequestCode,String[] permissions,int[] grantResults){
        Log.e(TAG,"called");
        switch (RequestCode){
            case RequestPermission_bluetooth:
                if(grantResults.length>1 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    new thread_searchDevice().start();
                }else{
                    Toast.makeText(SecondActivity.this,"请同意使用蓝牙",Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }
    private void showAlertDialog(){
        final AlertDialog.Builder builder=new AlertDialog.Builder(SecondActivity.this);
        builder.setTitle("提示");
        builder.setMessage("传输完成");
        builder.setPositiveButton("知道了！", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int whitch) {
                end();
            }
        });
        builder.create().show();
    }
    private void end(){
        if(mBluetoothAdapter!=null)
            mBluetoothAdapter.disable();
        Intent intent = new Intent(SecondActivity.this,FirstActivity.class);
        startActivity(intent);
        unregisterReceiver(receiver);
        finish();
    }
    @Override
    public void onBackPressed(){
        end();
    }


    class thread_uploadFile extends Thread{
        public void run(){
            Message message;
            try {
                clientSocket = remoteDevice.createRfcommSocketToServiceRecord(MY_UUID);
                //开始连接蓝牙，如果没有配对则弹出对话框提示我们进行配对
                clientSocket.connect();
            }catch (IOException e) {
                message=new Message();
                message.what=2;
                uiHandler.sendMessage(message);
                e.printStackTrace();
                return;
            }
            try{
                //获得输出流（客户端指向服务端输出文本）
                outputStream = clientSocket.getOutputStream();
                outputStreamWriter=new OutputStreamWriter(outputStream);
                inputStream = clientSocket.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                jsonWriter = new JsonWriter(outputStreamWriter);
                Bundle bundle = new Bundle();
                String name=filePath.split("/")[filePath.split("/").length-1];
                do {
                    jsonWriter.beginObject();
                    jsonWriter.name("fileName").value(name);
                    jsonWriter.name("mediaSize").value(MediaSize_para);
                    jsonWriter.name("copies").value(copies_para);
                    jsonWriter.name("margin").value(margin_para);
                    jsonWriter.name("orientation").value(orientation_para);
                    jsonWriter.name("chromaticity").value(chromaticity_para);
                    jsonWriter.name("sides").value(sides_para);
                    jsonWriter.name("pageRange").value(range_para);
                    jsonWriter.name("firstPage").value(first_page_para);
                    jsonWriter.name("lastPage").value(last_page_para);
                    jsonWriter.name("quality").value(quality_para);
                    jsonWriter.endObject();
                    jsonWriter.flush();
                    jsonReader = new JsonReader(inputStreamReader);
                    jsonReader.beginObject();
                    while (jsonReader.hasNext()) {
                        bundle.putString(jsonReader.nextName(), jsonReader.nextString());
                    }
                    jsonReader.endObject();
                }while (!bundle.getString("result").equals("success"));
                message=new Message();
                message.what=4;
                uiHandler.sendMessage(message);
                try{
                    byte[] buffer=new byte[1024*100];
                    int length=0;
                    File file=new File(filePath);
                    FileInputStream fileInputStream = new FileInputStream(file);
                    while((length=fileInputStream.read(buffer,0,buffer.length))!=-1) {
                        outputStream.write(buffer,0,length);
                        outputStream.flush();
                        message=new Message();
                        message.what=5;
                        message.arg1=length*100/fileInputStream.available();
                        uiHandler.sendMessage(message);
                    }
                    message=new Message();
                    message.what=6;
                    uiHandler.sendMessage(message);
                }catch(IOException e){
                    message =new Message();
                    message.what=3;
                    uiHandler.sendMessage(message);
                    e.printStackTrace();
                }
            }catch (IOException  e){
                message =new Message();
                message.what=3;
                uiHandler.sendMessage(message);
                e.printStackTrace();
            }
            if(clientSocket.isConnected()){
                try{
                    clientSocket.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    class thread_searchDevice extends Thread{
        @Override
        public void run(){
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(!mBluetoothAdapter.isEnabled())
                mBluetoothAdapter.enable();
            Set<BluetoothDevice> pairedDevices=mBluetoothAdapter.getBondedDevices();
            if(pairedDevices.size()>0){
                for(BluetoothDevice device : pairedDevices){
                    Log.e(TAG,device.getAddress());
                    if (device.getAddress().equals(MAC)) {
                        remoteDevice=device;
                        Message message=new Message();
                        message.what=1;
                        uiHandler.sendMessage(message);
                        return;
                    }
                }
            }
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            //开启搜索
            mBluetoothAdapter.startDiscovery();
        }
    }
}