package sample.change.me.clothesdetect;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

import sample.change.me.clothesdetect.adapter.GridViewAdapter;
import sample.change.me.clothesdetect.nativeMothed.NativeMothed;
import sample.change.me.clothesdetect.utils.SearchImageUtils;
import sample.change.me.clothesdetect.view.NoScrollGridView;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";
    private GridViewAdapter mAdapter;
    private ArrayList<Bitmap>mList=new ArrayList<Bitmap>();
    private MyHandler mHandler =new MyHandler();
    private String basePath;
    static {
        System.loadLibrary("OPEN_CV");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Bundle bundle = this.getIntent().getExtras();
        basePath=bundle.getString("path");
        setBaseImg();
        mAdapter=new GridViewAdapter(SearchActivity.this, mList);
        NoScrollGridView sv =(NoScrollGridView) findViewById(R.id.listview_item_gridview);
        sv.setAdapter(mAdapter);
        new Thread(new Runnable() {
            @Override
            public void run() {
                searchImage();
            }
        }).start();
    }
    private void  setBaseImg(){
        Bitmap b = BitmapFactory.decodeFile(basePath);
        mList.add(b);
        NativeMothed.nativeSetBaseImage(b);
    }

    private void searchImage(){
        try {
            String fileNames[] = getAssets().list("img");//获取assets目录下的所有文件及目录名
            for (int i=0;i<fileNames.length;i++){
                Bitmap bitmap = SearchImageUtils.getImageFromAssetsFile(SearchActivity.this, fileNames[i]);
                if(NativeMothed.nativeGetImage(bitmap)){
                    Message message =mHandler.obtainMessage();
                    message.obj=bitmap;
                    mHandler.sendMessage(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            Bitmap bitmap=(Bitmap) msg.obj;
            mList.add(bitmap);
            mAdapter.notifyDataSetInvalidated();
        }
    }
}
