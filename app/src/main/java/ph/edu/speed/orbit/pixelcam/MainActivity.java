package ph.edu.speed.orbit.pixelcam;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;


public class MainActivity extends ActionBarActivity implements SurfaceHolder.Callback {

    Camera camera;
    SurfaceHolder surfaceHolder;
    int pixel, red, green, blue;
    Bitmap bitmap;
    Boolean isLedOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SurfaceView surfaceview = (SurfaceView) findViewById(R.id.svCam);
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
            //parameters.setPreviewSize(320, 240);
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
                final int rgb[] = new int[frameWidth * frameHeight]; // number of pixels

                decodeYUV420SP(rgb, data, frameWidth, frameHeight);

                bitmap = Bitmap.createBitmap(frameWidth, frameHeight, Bitmap.Config.RGB_565);
                bitmap.setPixels(rgb, 0, frameWidth, 0, 0, frameWidth, frameHeight);
                int dcolor = bitmap.getPixel(frameWidth / 2, frameHeight / 2);

                red = Color.red(dcolor);
                green = Color.green(dcolor);
                blue = Color.blue(dcolor);

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

                        if (r < 0) r = 0;
                        else if (r > 262143) r = 262143;
                        if (g < 0) g = 0;
                        else if (g > 262143) g = 262143;
                        if (b < 0) b = 0;
                        else if (b > 262143) b = 262143;

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

    @Override
    protected void onPause() {
        camera.unlock();
        super.onPause();


    }
}
