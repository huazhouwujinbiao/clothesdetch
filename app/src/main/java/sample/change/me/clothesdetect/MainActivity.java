package sample.change.me.clothesdetect;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity implements CvCameraViewListener2 {

    private final static String ALBUM_PATH = Environment.getExternalStorageDirectory() + "/opencv/";
    private static final String TAG = "OCVSample::Activity";
    private static int width;
    private static int height;
    private static int scannWidth=0;
    private static int scannHeight=0;
    private static boolean flag=true;
    private static Handler handler=new Handler(Looper.getMainLooper());

    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    private static final Scalar SCAANER_RECT_COLOR = new Scalar(127,180,70, 100);
    private static int speed=5;
    private static final int SIZE=300;
    public static final int JAVA_DETECTOR = 0;

    public static final int NATIVE_DETECTOR = 1;

    private MenuItem mItemFace50;

    private MenuItem mItemFace40;

    private MenuItem mItemFace30;

    private MenuItem mItemFace20;

    private MenuItem mItemType;

    private Mat mRgba;

    private Mat mGray;

    private File mCascadeFile;

    private CascadeClassifier mJavaDetector;

//    private DetectionBasedTracker mNativeDetector;

    private int mDetectorType = JAVA_DETECTOR;

    private String[] mDetectorName;

    private float mRelativeFaceSize = 0.2f;

    private int mAbsoluteFaceSize = 0;

    //在 mOpenCvCameraView 的回调接口onCameraFrame函数里面处理每一帧从相机获取到的图片。

    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override

        public void onManagerConnected(int status) {

            switch (status) {

                case LoaderCallbackInterface.SUCCESS:

                {

                    Log.i(TAG, "OpenCVloaded successfully");

                    // Load native libraryafter(!) OpenCV initialization

                    System.loadLibrary("OPEN_CV");
                    try {

                        // load cascade filefrom application resources

                        InputStream is = getResources().openRawResource(R.raw.cascade);

//这里去加载人脸识别分类文件（lbpcascade_frontalface.XML 是XML文件，这都是利用Opencv给我们提供好的XML人脸识别分类文件，在opencv/source/data/目录下,这里把那个文件拉到了Raw资源文件里面，方便Android调用，如果要自己实现一个XML人脸识别分类文件的话，需要用到opencv_haartraining，来训练大量数据，最终生成XML人脸识别分类文件）

                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);

                        mCascadeFile = new File(cascadeDir, "cascade.xml");

                        FileOutputStream os = new FileOutputStream(mCascadeFile);


                        byte[] buffer = new byte[4096];

                        int bytesRead;

                        while ((bytesRead = is.read(buffer)) != -1) {

                            os.write(buffer, 0, bytesRead);

                        }

                        is.close();

                        os.close();


                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());

                        if (mJavaDetector.empty()) {

                            Log.e(TAG, "Failed to load cascade classifier");

                            mJavaDetector = null;

                        } else

                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());


//                        mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);


                        cascadeDir.delete();


                    } catch (IOException e) {

                        e.printStackTrace();

                        Log.e(TAG, "Failedto load cascade. Exception thrown: " + e);

                    }


                    mOpenCvCameraView.enableView();

                }
                break;

                default:

                {

                    super.onManagerConnected(status);

                }
                break;

            }

        }

    };

    public MainActivity() {

        mDetectorName = new String[2];

        mDetectorName[JAVA_DETECTOR] = "Java";

        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new" + this.getClass());

    }

    /**
     * Called when the activity is first created.
     */

    @Override

    public void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "calledonCreate");

        super.onCreate(savedInstanceState);

       /* getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);*/
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //设置拍摄方向

        /*

                       系统默认

                       ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

                       锁定直式

                       ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                       锁定横式

                       ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                       随使用者当下

                       ActivityInfo.SCREEN_ORIENTATION_USER

                       与活动线程下相同的设定

                       ActivityInfo.SCREEN_ORIENTATION_BEHIND

                       不随SENSOR改变

                       ActivityInfo.SCREEN_ORIENTATION_NOSENSOR

                       随SENSOR改变

                       ActivityInfo.SCREEN_ORIENTATION_SENSOR



         */

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);

        mOpenCvCameraView.setCvCameraViewListener(this);
        DisplayMetrics dm = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(dm);

        height=dm.heightPixels;

        width=dm.widthPixels;
        scannWidth=width/2+300;
        scannHeight=height/2-300;
    }


    @Override

    public void onPause()

    {

        super.onPause();

        if (mOpenCvCameraView != null)

            mOpenCvCameraView.disableView();

    }


    @Override

    public void onResume()

    {
        flag=true;
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_10, this, mLoaderCallback);

    }


    public void onDestroy() {

        super.onDestroy();

        mOpenCvCameraView.disableView();

    }


    public void onCameraViewStarted(int width, int height) {

        mGray = new Mat();

        mRgba = new Mat();

    }


    public void onCameraViewStopped() {

        mGray.release();

        mRgba.release();

    }


    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        //这里获取相机拍摄到的原图，彩色图
        mRgba = inputFrame.rgba();
        Mat tempRgba=new Mat();
        mRgba.copyTo(tempRgba);
//这里获取相机拍摄到的灰度图，用来给下面检测人脸使用。

        mGray = inputFrame.gray();
        Mat temp= new Mat(mGray,new Range(height/2-SIZE,height/2+SIZE),new Range(width/2-SIZE,width/2+SIZE));
//        Highgui.imwrite(ALBUM_PATH+"1.jpg",temp);
        if (mAbsoluteFaceSize == 0) {

            int height = mGray.rows();

            if (Math.round(height * mRelativeFaceSize) > 0) {

                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);

            }

//            mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);

        }


        MatOfRect faces = new MatOfRect();


        if (mDetectorType == JAVA_DETECTOR) {

            if (mJavaDetector != null)

//调用opencv的detectMultiScale（）检测函数，参数意义如下

/*

        mGray表示的是要检测的输入图像，faces表示检测到的目标序列,存储检测结果（坐标位置，长，宽），1.1表示
            每次图像尺寸减小的比例为1.1，2表示每一个目标至少要被检测到3次才算是真的目标(因为周围的像素和不同的窗口大
            小都可以检测到目标),2（其实是一个常量：CV_HAAR_SCALE_IMAGE）表示不是缩放分类器来检测，而是缩放图像，最后两个size()为检测目标的
            最小最大尺寸
        CV_HAAR_DO_CANNY_PRUNING利用Canny边缘检测器来排除一些边缘很少或者很多的图像区域，
        CV_HAAR_SCALE_IMAGE就是按比例正常检测，
        CV_HAAR_FIND_BIGGEST_OBJECT只检测最大的物体，
        CV_HAAR_DO_ROUGH_SEARCH只做初略检测
*/

                mJavaDetector.detectMultiScale(temp, faces, 1.1, 2, 2, // TODO:objdetect.CV_HAAR_SCALE_IMAGE

                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
                Log.d(TAG, "onCameraFrame: -----------------------------------------"+mAbsoluteFaceSize);
             /*   mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO:objdetect.CV_HAAR_SCALE_IMAGE

                        new Size(150, 150), new Size(250,250));*/

        }
        /*else if (mDetectorType == NATIVE_DETECTOR) {

            if (mNativeDetector != null)

                mNativeDetector.detect(mGray, faces);

        } else {

            Log.e(TAG, "Detection methodis not selected!");

        }*/

        scannHeight+=speed;
        speed++;
        Rect[] facesArray = faces.toArray();
        if(scannHeight>=height/2+SIZE){
            scannHeight=height/2-SIZE;
            speed=5;
        }
        Core.line(mRgba,new Point(width/2-SIZE,scannHeight),new Point(scannWidth,scannHeight),SCAANER_RECT_COLOR,2);

        for (int i = 0; i < facesArray.length; i++) {

//在原图mRgba上为每个检测到的人脸画一个绿色矩形
            int x = facesArray[i].x + height/2;
            int y = facesArray[i].y +60;
            int tmpWidth=x+facesArray[i].width;
            int tmpHeight=y+facesArray[i].height;
//            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
                    Core.rectangle(mRgba, new Point(x, y), new Point(tmpWidth,tmpHeight), FACE_RECT_COLOR, 3);
            //返回处理好的图像，返回后会直接显示在JavaCameraView上。
            Mat mat= new Mat(tempRgba, new Rect(x,y,facesArray[i].width,facesArray[i].height));
            final String path =ALBUM_PATH+"1.jpg";
            Highgui.imwrite(path,mat);
            if(flag){
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent=new Intent(MainActivity.this,SearchActivity.class);
                        intent.putExtra("path",path);
                        startActivity(intent);
                    }
                },3000);
                flag=false;
            }
        }
        return mRgba;
    }


    @Override

    public boolean onCreateOptionsMenu(Menu menu) {

        Log.i(TAG, "calledonCreateOptionsMenu");

        mItemFace50 = menu.add("Face size50%");

        mItemFace40 = menu.add("Face size40%");

        mItemFace30 = menu.add("Face size30%");

        mItemFace20 = menu.add("Face size20%");

        mItemType = menu.add(mDetectorName[mDetectorType]);

        return true;

    }


    @Override

    public boolean onOptionsItemSelected(MenuItem item) {

        Log.i(TAG, "calledonOptionsItemSelected; selected item: " + item);

        if (item == mItemFace50)

            setMinFaceSize(0.5f);

        else if (item == mItemFace40)

            setMinFaceSize(0.4f);

        else if (item == mItemFace30)

            setMinFaceSize(0.3f);

        else if (item == mItemFace20)

            setMinFaceSize(0.2f);

        else if (item == mItemType) {

            int tmpDetectorType = (mDetectorType + 1) % mDetectorName.length;

            item.setTitle(mDetectorName[tmpDetectorType]);

            setDetectorType(tmpDetectorType);

        }

        return true;

    }


    private void setMinFaceSize(float faceSize) {

        mRelativeFaceSize = faceSize;

        mAbsoluteFaceSize = 0;

    }


    private void setDetectorType(int type) {

        if (mDetectorType != type) {

            mDetectorType = type;

/*
            if (type == NATIVE_DETECTOR) {

                Log.i(TAG, "DetectionBased Tracker enabled");

                mNativeDetector.start();

            } else {

                Log.i(TAG, "Cascadedetector enabled");

                mNativeDetector.stop();

            }*/

        }

    }

}