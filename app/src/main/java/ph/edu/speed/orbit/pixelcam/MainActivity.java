package ph.edu.speed.orbit.pixelcam;

import android.graphics.Color;
import android.hardware.Camera;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;


public class MainActivity extends ActionBarActivity implements SurfaceHolder.Callback {

    Camera camera;
    SurfaceHolder surfaceHolder;
    String stat;
    Integer pixel,red,green,blue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SurfaceView surfaceview = (SurfaceView) findViewById(R.id.svCam);
        surfaceHolder = surfaceview.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);



    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open();
        try {
            camera.setPreviewDisplay( surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        camera.setDisplayOrientation(90);
        camera.startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, final int width, final int height) {

        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewFrameRate(20);
        parameters.setPreviewSize(176, 144);
        camera.setParameters(parameters);

        camera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {

                TextView statView = (TextView) findViewById(R.id.tvStat);

                int frameHeight = camera.getParameters().getPreviewSize().height;
                int frameWidth = camera.getParameters().getPreviewSize().width;
                int rgb[] = new int[frameWidth * frameHeight]; // number of pixels
                int[] rec = decodeYUV420SP(rgb, data, frameWidth, frameHeight);


                if (rec != null) {
                    int frameSize = frameHeight * frameWidth;
                    if (frameSize%2 == 1){
                        pixel = (frameSize-1)/2;
                        red = Color.red(rec[pixel]);
                        green = Color.green(rec[pixel]);
                        blue = Color.blue(rec[pixel]);

                    }else{

                        pixel = (frameSize/2)-2;
                        red = Color.red(rec[pixel]);
                        green = Color.green(rec[pixel]);
                        blue = Color.blue(rec[pixel]);

                    }

                    stat = "R:"+String.valueOf(red)+" G:"+String.valueOf(green)+" B:"+String.valueOf(blue);

                    statView.setText("Realtime: "+ stat);

                } else
                    statView.setText("No data");


                //
                //

                SurfaceView surfaceview = (SurfaceView) findViewById(R.id.svCam);
                final TextView result = (TextView) findViewById(R.id.tvResult);
                surfaceview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        result.setText("Retained:" + stat);
                    }
                });
            }

            private int[] decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {

                // here we're using our own internal PImage attributes
                final int frameSize = width * height;

                for (int j = 0, yp = 0; j < height; j++) {
                    int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
                    for (int i = 0; i < width; i++, yp++) {
                        int y = (0xff & ((int) yuv420sp[yp])) - 16;
                        if (y < 0)
                            y = 0;
                        if ((i & 1) == 0) {
                            v = (0xff & yuv420sp[uvp++]) - 128;
                            u = (0xff & yuv420sp[uvp++]) - 128;
                        }

                        int y1192 = 1192 * y;
                        int r = (y1192 + 1634 * v);
                        int g = (y1192 - 833 * v - 400 * u);
                        int b = (y1192 + 2066 * u);

                        if (r < 0)
                            r = 0;
                        else if (r > 262143)
                            r = 262143;
                        if (g < 0)
                            g = 0;
                        else if (g > 262143)
                            g = 262143;
                        if (b < 0)
                            b = 0;
                        else if (b > 262143)
                            b = 262143;

                        // use interal buffer instead of pixels for UX reasons
                        rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000)
                                | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
                    }
                }

                return rgb;
            }
        });

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        camera.release();

    }
}
