package edu.csulb.android.letschat;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static org.apache.commons.io.IOUtils.toByteArray;

public class MainActivity extends ActionBarActivity {

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static  final int MESSAGE_IMAGE=6;
    public static int imagesize;
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final int GALLERY_IMAGE_PICK = 8 ;

    String AudioSavePathInDevice = null;
    String ReceivedFilePath = null;
    MediaRecorder mediaRecorder ;
    Random random ;
    String RandomAudioFileName = "ABCDEFGHIJKLMNOP";
    public static final int RequestPermissionCode = 1;
    MediaPlayer mediaPlayer ;
    private ImageView imageview;
    private ListView lvMainChat;
    private EditText etMain;
    private Button btnSend;
    private Button btnImage;
    private Button btnRecord, btnStop, btnPlay, btnSendAudio;
    private Uri imageUri =null;
    private String connectedDeviceName = null;
    private ArrayAdapter<String> chatArrayAdapter;
    private boolean isImage=false;
    private StringBuffer outStringBuffer;
    private BluetoothAdapter bluetoothAdapter = null;
    private ChatService chatService = null;
    static String recmsg ="";
    private static boolean imgrecd=false;
    private static boolean audiorecd=false;
    private Handler handler = new Handler(new Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case ChatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to,
                                    connectedDeviceName));
                            chatArrayAdapter.clear();
                            break;
                        case ChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case ChatService.STATE_LISTEN:
                        case ChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;

                    String writeMessage = new String(writeBuf);
                    chatArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    if(readMessage.contains("IMGEND"))
                    {
                        recmsg=recmsg+readMessage;
                        imgrecd=false;
                        recmsg=recmsg.substring(3);
                        recmsg=recmsg.replaceAll("IMGEND","");
                        Log.d("Image recd",recmsg);
                        Bitmap b = StringToBitmap(recmsg);
                        imageview.setImageBitmap(b);
                        imageview.setVisibility(View.VISIBLE);
                        recmsg="";
                        break;
                    }
                    if(imgrecd==true)
                    {
                        Log.d("Image recd",recmsg);
                        recmsg=recmsg+readMessage;
                        //   break;
                    }
                    if(readMessage.length()>3&& readMessage.substring(0,3).matches("IMG"))
                    {
                        recmsg=recmsg+readMessage;
                        imgrecd=true;
                       // break;
                    }
                    if(readMessage.contains("AEND"))
                    {
                        recmsg=recmsg+readMessage;
                        audiorecd=false;
                        recmsg=recmsg.substring(5);
                        recmsg=recmsg.replaceFirst("AEND","");
                        byte[] b=Base64.decode(recmsg,Base64.DEFAULT);
                        //byte[] recdaudio = Base64.decode(recmsg,Base64.DEFAULT);
                        //String recaudio= new String(recmsg);
                        Log.v("Audio recd",recmsg);
                        ReceivedFilePath =
                                Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
                                        CreateRandomAudioFileName(2) + "AudioRecording.aac";
                        //------------------------------------------------------------------

                        File path = new File(ReceivedFilePath);
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(path);
                            fos.write(b);
                            fos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        MediaPlayer mediaPlayer = new MediaPlayer();

                        try {
                            mediaPlayer.setDataSource(ReceivedFilePath);
                            mediaPlayer.prepare();
                            //    mediaPlayer.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //------------------------------------------------------------------
                        /*BufferedOutputStream buf = null;
                        try {
                            buf = new BufferedOutputStream(new FileOutputStream(ReceivedFilePath));
                            buf.write(recmsg.getBytes());
                            buf.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mediaPlayer = new MediaPlayer();
                        try {
                            mediaPlayer.setDataSource(ReceivedFilePath);
                            mediaPlayer.prepare();

                            //mediaPlayer
                        } catch (IOException e) {
                            e.printStackTrace();
                        }*/
                        mediaPlayer.start();
                        recmsg="";
                        break;
                    }
                    if(audiorecd==true)
                    {
                       // Log.d("Image recd",recmsg);
                        recmsg=recmsg+readMessage;
                        //break;
                    }
                    if(readMessage.length()>5 && readMessage.substring(0,5).matches("AUDIO"))
                    {
                        recmsg=recmsg+readMessage;
                        audiorecd=true;
                        // break;
                    }

                    Log.e("Message:",readMessage);
                    chatArrayAdapter.add(connectedDeviceName + ":  " + readMessage);
                    break;
                /*case MESSAGE_IMAGE:
                    byte[] ImageBuffer = (byte[]) msg.obj;
                    String readImage = new String(ImageBuffer, 0, msg.arg1);
                    Log.d("Receive",readImage);
                    Bitmap bitmap;
                    if(readImage.length()>3)
                    {
                        readImage=readImage.substring(3);
                        readImage=readImage.replaceFirst("IMGEND","");
                        readImage=readImage.replaceAll("\n","");
                        bitmap=StringToBitmap(readImage);
                        imageview.setImageBitmap(bitmap);
                    }
                    break;*/
                case MESSAGE_DEVICE_NAME:
                    connectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(),
                            "Connected to " + connectedDeviceName,
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
            return false;
        }
    });

    public static Bitmap StringToBitmap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString,Base64.NO_WRAP);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (NullPointerException e) {
            e.getMessage();
            return null;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main1);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        getWidgetReferences();
        bindEventHandler();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    private void getWidgetReferences() {
        lvMainChat = (ListView) findViewById(R.id.lvMainChat);
        etMain = (EditText) findViewById(R.id.etMain);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnImage=(Button) findViewById(R.id.btnImg);
        imageview=(ImageView)findViewById(R.id.imageView);
        btnPlay=(Button)findViewById(R.id.btnPlay);
        btnRecord=(Button)findViewById(R.id.btnRecord);
        btnStop=(Button)findViewById(R.id.btnStop);
        btnSendAudio=(Button)findViewById(R.id.btnSendAudio);
        btnPlay.setEnabled(false);
        btnSendAudio.setEnabled(false);
    }

    private void bindEventHandler() {
        etMain.setOnEditorActionListener(mWriteListener);

        btnSend.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String message = etMain.getText().toString();
                sendMessage(message);
            }
        });

        btnImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                isImage=true;
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GALLERY_IMAGE_PICK);

            }
        });
        random = new Random();
        btnRecord.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPermission()) {

                    AudioSavePathInDevice =
                            Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
                                    CreateRandomAudioFileName(5) + "AudioRecording.aac";

                    MediaRecorderReady();

                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IllegalStateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    btnRecord.setEnabled(false);
                    btnStop.setEnabled(true);

                    Toast.makeText(MainActivity.this, "Recording started",
                            Toast.LENGTH_LONG).show();
                } else {
                    requestPermission();
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaRecorder.stop();
                btnStop.setEnabled(false);
                btnPlay.setEnabled(true);
                btnRecord.setEnabled(true);
                btnSendAudio.setEnabled(true);

                Toast.makeText(MainActivity.this, "Recording Completed",
                        Toast.LENGTH_LONG).show();
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) throws IllegalArgumentException,
                    SecurityException, IllegalStateException {

                btnStop.setEnabled(false);
                btnRecord.setEnabled(false);
                //buttonStopPlayingRecording.setEnabled(true);

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(AudioSavePathInDevice);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.start();
                Toast.makeText(MainActivity.this, "Recording Playing",
                        Toast.LENGTH_LONG).show();
            }
        });

        btnSendAudio.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              /*  //------------------------------------------------
                byte[] soundBytes={};

                try {
                    InputStream inputStream = getContentResolver().openInputStream(Uri.fromFile(new File(AudioSavePathInDevice)));
                    soundBytes = new byte[inputStream.available()];

                    soundBytes = toByteArray(inputStream);
                }catch(Exception e){
                    e.printStackTrace();
                }
                //-----------------------------------------------
                */
                byte[] bytes={};
               File audio = new File(AudioSavePathInDevice);
                if(audio!=null)
                {
                    bytes= new byte[(int)audio.length()];
                    try {
                        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(audio));
                        buf.read(bytes, 0, bytes.length);
                        buf.close();
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    try {
                        bytes=toByteArray(new FileInputStream(audio));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }

              //  byte[] audioToBeSent = Base64.encode(bytes,Base64.DEFAULT);
                //String audioString =new String(bytes);
               // Log.v("sent audio",audioString);
                String a = Base64.encodeToString(bytes,Base64.DEFAULT);
                a="AUDIO"+a;
                a=a+"AEND";
                //Log.v("sent audio",audioString);
                sendMessage(a);
            }

        });


    }

   /* public byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read = 0;
        byte[] buffer = new byte[1024];
        while (read != -1) {
            read = in.read(buffer);
            if (read != -1)
                out.write(buffer,0,read);
        }
        out.close();
        return out.toByteArray();
    }*/

    public void MediaRecorderReady(){
        mediaRecorder=new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
    }

    public String CreateRandomAudioFileName(int string){
        StringBuilder stringBuilder = new StringBuilder( string );
        int i = 0 ;
        while(i < string ) {
            stringBuilder.append(RandomAudioFileName.
                    charAt(random.nextInt(RandomAudioFileName.length())));

            i++ ;
        }
        return stringBuilder.toString();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    public static String BitmapToString(Bitmap bitmap) {
        try {
           ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] b = baos.toByteArray();
            String temp = Base64.encodeToString(b, Base64.DEFAULT);
            Log.d("Image",temp);
            imagesize=temp.length();
            return temp;

        } catch (NullPointerException e) {
            return null;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case GALLERY_IMAGE_PICK:
                if(resultCode==Activity.RESULT_OK)
                {
                    try {
                        Toast.makeText(this, "Returned Result", Toast.LENGTH_SHORT).show();
                        imageUri = data.getData();
                        Log.d("URI",imageUri.toString());
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        String image = "IMG"+BitmapToString(bitmap)+"IMGEND";
                        sendMessage(image);
                       // sendMessage("TXTImage Sent Successfully!");
                    }catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                break;
            case REQUEST_CONNECT_DEVICE_SECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    setupChat();
                } else {
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        String address = data.getExtras().getString(
                DeviceListActivity.DEVICE_ADDRESS);
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        chatService.connect(device, secure);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
            case R.id.secure_connect_scan:
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            case R.id.insecure_connect_scan:
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent,
                        REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            case R.id.discoverable:
                ensureDiscoverable();
                return true;
        }
        return false;
    }

    private void ensureDiscoverable() {
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(
                    BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void sendMessage(String message) {
        if (chatService.getState() != ChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        if (message.length() > 0) {
            byte[] send = message.getBytes();
            chatService.write(send);

            outStringBuffer.setLength(0);
            etMain.setText(outStringBuffer);
        }
    }

    private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId,
                                      KeyEvent event) {
            if (actionId == EditorInfo.IME_NULL
                    && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    private final void setStatus(int resId) {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(resId);
    }

    private final void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(subTitle);
    }

    private void setupChat() {
        chatArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        lvMainChat.setAdapter(chatArrayAdapter);

        chatService = new ChatService(this, handler);

        outStringBuffer = new StringBuffer("");
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (chatService == null)
                setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        if (chatService != null) {
            if (chatService.getState() == ChatService.STATE_NONE) {
                chatService.start();
            }
        }
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatService != null)
            chatService.stop();
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length> 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        Toast.makeText(MainActivity.this, "Permission Granted",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

}

