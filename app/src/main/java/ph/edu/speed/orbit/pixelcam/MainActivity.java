package ph.edu.speed.orbit.pixelcam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;


public class MainActivity extends ActionBarActivity implements SurfaceHolder.Callback {

    Camera camera;
    SurfaceHolder surfaceHolder;
    Integer pixel,red,green,blue;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SurfaceView surfaceview = (SurfaceView) findViewById(R.id.svCam);
        surfaceHolder = surfaceview.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setFormat(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try {
            //open camera
            camera = Camera.open();
            camera.setPreviewDisplay(surfaceHolder);

            //setting parameters
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(320, 240);
            parameters.setPreviewFrameRate(30);
            parameters.setSceneMode(Camera.Parameters.SCENE_MODE_NIGHT);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            camera.setParameters(parameters);

            //correct camera display orientation
            camera.setDisplayOrientation(90);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, final int width, final int height) {


        camera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {

                TextView statView = (TextView) findViewById(R.id.tvStat);

                int frameHeight = camera.getParameters().getPreviewSize().height;
                int frameWidth = camera.getParameters().getPreviewSize().width;
                int rgb[] = new int[frameWidth * frameHeight]; // number of pixels

                decodeYUV420SP(rgb, data, frameWidth, frameHeight);
                new pixelcomparator(rgb);

                int dcolor = pixelcomparator.temp;
                int dcount = pixelcomparator.max;

                bitmap  = Bitmap.createBitmap(frameWidth,frameHeight, Bitmap.Config.ARGB_4444);
                bitmap.setPixels(new int[]{dcolor}, 0, 1, 0, 0,1,1);
                pixel = bitmap.getPixel(0,0);
                red = Color.red(pixel);
                green = Color.green(pixel);
                blue = Color.blue(pixel);

                statView.setText("R: " + String.valueOf(red) + " " +
                                 "G: " + String.valueOf(green) + " " +
                                 "B: " + String.valueOf(blue));




                SurfaceView surfaceview = (SurfaceView) findViewById(R.id.svCam);
                final TextView result = (TextView) findViewById(R.id.tvResult);
                surfaceview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        result.setText("R: " + String.valueOf(red) + " " +
                                "G: " + String.valueOf(green) + " " +
                                "B: " + String.valueOf(blue));
                    }
                });
            }

            private void decodeYUV420SP(int[] rgb, byte[] data, int width, int height) {

                final int frameSize = width * height;

                for (int j = 0, yp = 0; j < height; j++) {
                    int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
                    for (int i = 0; i < width; i++, yp++) {
                        int y = (0xff & ((int) data[yp])) - 16;
                        if (y < 0) y = 0;
                        if ((i & 1) == 0) {
                            v = (0xff & data[uvp++]) - 128;
                            u = (0xff & data[uvp++]) - 128;
                        }

                        int y1192 = 1192 * y;
                        int r = (y1192 + 1634 * v);
                        int g = (y1192 - 833 * v - 400 * u);
                        int b = (y1192 + 2066 * u);

                        if (r < 0) r = 0; else if (r > 262143) r = 262143;
                        if (g < 0) g = 0; else if (g > 262143) g = 262143;
                        if (b < 0) b = 0; else if (b > 262143) b = 262143;

                        rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
                    }
                }
            }


        });

    }



    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        camera.release();

    }
}
