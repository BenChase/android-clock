package com.jaminland.apps.clock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Calendar;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static android.graphics.Paint.Style.FILL_AND_STROKE;

/**
 * Created by bbc on 12/5/14.
 */
public class ClockView extends SurfaceView implements Runnable {
    Path path;

    SurfaceHolder surfaceHolder;
    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint paintRim;
    private Paint paintEvent9;
    private Paint paintMinutes;
    private Paint paintHours;
    private Paint paintHoursIn;
    private Paint paintMinuteHand;
    private Paint paintHourHand;
    private Paint paintSecondHand;
    private Path secondHand;
    private Path minuteHand;
    private Path hourHand;
    private Path face;
    float cx=0;
    float cy=0;
    float radius=1;

    public ClockView(Context context) {
        super(context);
        init();
    }
    public ClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public ClockView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    protected void reinit(Canvas c) {
        int w = c.getWidth();
        int h = c.getHeight();
        cx = w / 2.0F;
        cy = h / 2.0F;
        radius = 0.9F * Math.min(cx,cy);
    }

    private Path makeSecondHand() {
        Path p = new Path();
        p.moveTo(0F,0F);
        p.lineTo(0F, 0.88F);

/*
        float tipLimit=0.88F;
        float tipDelta=0.1F;
        float tip=tipLimit-tipDelta;
        for ( ; tip>0; tip -= tipDelta) {
            p = transform(p, -1 * 360/60);
            p.moveTo(0, tip);
            p.lineTo(0, tipLimit);
        }
        */
        return p;
    }
    private Path makeMinuteHand() {
        Path path = new Path();
        path.moveTo(0,0);
        path.lineTo(0.03F, 0.25F);
        path.lineTo(0F, 0.93F);
        path.lineTo(-0.03F,0.25F);
        path.close();
        return path;
    }
    private Path makeHourHand() {
        Path p = new Path();
        p.moveTo(0F, 0F);
        p.lineTo(0F, 0.5F);
        return p;
    }
    private Path makeFace() {
        Path p = new Path();
        p.addCircle(0, 0, 1.0F, Path.Direction.CCW);
        p = transform(p,secondsToDegrees(0));

        Path minuteTick = new Path();
        minuteTick.moveTo(0F, 0.98F);
        minuteTick.lineTo(0F, 1.0F);
        for (int minutes=0; minutes<60; minutes +=1) {
            p.addPath(transform(minuteTick,secondsToDegrees(minutes)));
        }

        Path hourTick = new Path();
        hourTick.moveTo(0F,.94F);
        hourTick.lineTo(0F,1F);
        for (int hour=0; hour<12; hour++) {
            p.addPath(transform(hourTick,secondsToDegrees(5*hour)));
        }

        return p;
    }


    protected void init() {
        paintEvent9      = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintEvent9.setColor(0xffffffff);
        paintEvent9.setStrokeWidth(3F);
        paintEvent9.setStyle(Paint.Style.STROKE);

        paintRim        = new Paint(Paint.ANTI_ALIAS_FLAG);
        int red = 0xffff3010;
        paintRim.setColor(red);
        paintRim.setStrokeWidth(4.5F);
        paintRim.setStyle(Paint.Style.STROKE);

        paintMinutes    = new Paint(paintRim);
        paintMinutes.setStrokeCap(Paint.Cap.ROUND);
        paintMinutes.setStyle(FILL_AND_STROKE);

        paintHours      = new Paint(paintRim);
        paintHours.setStyle(FILL_AND_STROKE);
        paintHours.setStrokeWidth(9F);

        paintHoursIn    = new Paint(paintHours);
        paintHoursIn.setStrokeCap(Paint.Cap.ROUND);

        paintMinuteHand = new Paint(paintHoursIn);
        paintMinuteHand.setStyle(Paint.Style.STROKE);
        paintMinuteHand.setColor(red);

        paintHourHand   = new Paint(paintHoursIn);
        paintHourHand.setColor(red);

        paintSecondHand = new Paint(paintHoursIn);
        paintSecondHand.setStyle(Paint.Style.STROKE);
        paintSecondHand.setStrokeWidth(3F);
        paintSecondHand.setColor(red);

        secondHand = makeSecondHand();
        minuteHand = makeMinuteHand();
        hourHand   = makeHourHand();
        face = makeFace();

        surfaceHolder = getHolder();

        final ClockView that = this;

        surfaceHolder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                executor.shutdown();
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Canvas c = holder.lockCanvas(null);
                reinit(c);
                drawClock(c);
                holder.unlockCanvasAndPost(c);
                executor.scheduleAtFixedRate(that, 0L,1L, TimeUnit.SECONDS);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format,
                                       int width, int height) {
            }
        });
    }

    private float secondsToDegrees(float seconds) {
        return 360 * (seconds-30) / 60.0F;
    }

    private Path transform(Path path, float degrees) {
        Path dst = new Path();
        Matrix matrix = new Matrix();
        matrix.preScale(radius,radius);
        matrix.postRotate(degrees);
        matrix.postTranslate(cx, cy);
        path.transform(matrix, dst);
        return dst;
    }

    private void drawHourHand(Canvas c, int h, int m) {
        Path path = transform(hourHand,secondsToDegrees(5 * (h + m/60.0F)));
        c.drawPath(path, paintHourHand);
    }
    private void drawMinuteHand(Canvas c, int m) {
        Path path = transform(minuteHand,secondsToDegrees(m));
        c.drawPath(path, paintMinuteHand);
    }

    private void drawSecondHand(Canvas c, int s) {
        Path path = transform(secondHand,secondsToDegrees(s));
        c.drawPath(path, paintSecondHand);
    }

    private void drawHands(Canvas c, int h, int m, int s) {
        drawSecondHand(c, s);
        drawMinuteHand(c, m);
        drawHourHand(c, h, m);
    }

    private void drawFace(Canvas c) {
        c.drawColor(Color.BLACK);

        Path path = transform(face,0);
        c.drawPath(path, paintRim);

        c.drawCircle(cx, cy, radius*0.9F, paintEvent9);
        c.drawCircle(cx, cy, radius*0.8F, paintEvent9);
        c.drawCircle(cx, cy, radius*0.7F, paintEvent9);
        c.drawCircle(cx, cy, radius*0.6F, paintEvent9);
        c.drawCircle(cx, cy, radius*0.5F, paintEvent9);
        c.drawCircle(cx, cy, radius*0.4F, paintEvent9);
        c.drawCircle(cx, cy, radius*0.3F, paintEvent9);
        c.drawCircle(cx, cy, radius * 0.2F, paintEvent9);
    }

    private void drawClock(Canvas c) {
        drawFace(c);
        Calendar calendar = Calendar.getInstance();
        drawHands(c,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));
    }

    public void onResume(){
        /*
        running = true;
        thread = new Thread(this);
        thread.start();
        */
    }

    public void onPause(){
        /*
        boolean retry = true;
        running = false;
        while(retry){
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        */
    }

    public void run() {
        if(! surfaceHolder.getSurface().isValid()) {
            return;
        }

        Canvas canvas = surfaceHolder.lockCanvas();
        drawClock(canvas);
        surfaceHolder.unlockCanvasAndPost(canvas);
    }

/*
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            path = new Path();
            path.moveTo(event.getX(), event.getY());
        }else if(event.getAction() == MotionEvent.ACTION_MOVE){
            path.lineTo(event.getX(), event.getY());
        }else if(event.getAction() == MotionEvent.ACTION_UP){
            path.lineTo(event.getX(), event.getY());
        }

        if(path != null){
            Canvas canvas = surfaceHolder.lockCanvas();
            canvas.drawPath(path, paint);
            surfaceHolder.unlockCanvasAndPost(canvas);
        }

        return true;
    }
    */
}

