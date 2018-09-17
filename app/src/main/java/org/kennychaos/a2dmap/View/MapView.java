package org.kennychaos.a2dmap.View;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;

import org.kennychaos.a2dmap.BuildConfig;
import org.kennychaos.a2dmap.Model.BlockMap;
import org.kennychaos.a2dmap.Model.MapPoint;
import org.kennychaos.a2dmap.Model.Track;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yhrd_1 on 2016/12/20.
 */

public class MapView extends View implements View.OnTouchListener ,GestureDetector.OnDoubleTapListener,GestureDetector.OnGestureListener,ScaleGestureDetector.OnScaleGestureListener {
    private final String TAG = "=====" + getClass().getSimpleName() + "=====";

    private Paint paint_block, paint_cleaned, paint_track, paint_bot, paint_gray = null;
    private Paint paint_cache = new Paint();
    private float scale_screen = 1f;

    private Bitmap bitmap_pointCache, bitmap_lineCache = null;
    private Canvas canvas_pointCache, canvas_lineCache = null;

    /* */
    private final float offset = 3 / 8;
    private final float r = 5f;
    private final float stroke = 1 / 4;

    private final int TYPE_BLOCK = 1;
    private final int TYPE_CLEANED = 2;
    private final int TYPE_TRACK = 9;
    private final int TYPE_UNCLEANED = 10;
    private List<BlockMap> blockMapList = new ArrayList<>();
    private Track track = new Track();

    private final float MAX_ZOOM_SCALE = 4f;                            //放大的比例
    private final float MIN_NARROW_SCALE = .5f;                         //缩小的比例
    private Matrix matrix = null;                                       //上一次偏移的矩阵
    public Matrix matrix_translate = null;                             //偏移矩阵
    public Matrix matrix_scale = null;                                 //缩放矩阵
    private GestureDetector gestureDetector = null;                     //检测单手指手势类型
    private ScaleGestureDetector scaleGestureDetector = null;           //检测双手指手势类型
    private float scale_after_matrix = 1.0f;                            //矩阵得出的比列

    private long __time = 0;

    public MapView(Context context) {
        super(context);
        setOnTouchListener(this);
        gestureDetector = new GestureDetector(context, this);
        scaleGestureDetector = new ScaleGestureDetector(context, this);
    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);
        gestureDetector = new GestureDetector(context, this);
        scaleGestureDetector = new ScaleGestureDetector(context, this);
    }

    public MapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnTouchListener(this);
        gestureDetector = new GestureDetector(context, this);
        scaleGestureDetector = new ScaleGestureDetector(context, this);
    }

    /**
     * @param scale_screen
     * @param bg_color_string
     * @param view_width
     * @param view_height
     */
    public void init(float scale_screen, String bg_color_string, int view_width, int view_height) {
        int bg_color = 0;
        matrix_scale = new Matrix();
        matrix_translate = new Matrix();
        matrix = new Matrix();
        try {
            bg_color = Color.parseColor(bg_color_string);
        } catch (IllegalArgumentException e) {
            if (BuildConfig.DEBUG)
                Log.e(TAG, "传入的颜色String有误 - " + bg_color_string);
            bg_color = Color.parseColor("#D8B0B0B0");
        } finally {
            bitmap_pointCache = Bitmap.createBitmap(view_width, view_height, Bitmap.Config.ARGB_8888);
            canvas_pointCache = new Canvas(bitmap_pointCache);
            bitmap_lineCache = Bitmap.createBitmap(view_width, view_height, Bitmap.Config.ARGB_8888);
            canvas_lineCache = new Canvas(bitmap_lineCache);
            setLayoutParams(new ViewGroup.LayoutParams(view_width, view_height));
            setBackgroundColor(bg_color);
            this.scale_screen = scale_screen;
            setPaints(this.scale_screen);
            setClickable(true);
        }
    }

    public synchronized void refresh() {
        __time = System.currentTimeMillis();
        Log.e("draw time cost", "start = " + __time);
//        clear();
        for (BlockMap blockMap : blockMapList) {
            if (blockMap.getHistory_id() != -1)
                for (MapPoint m : blockMap.getMapPointList()) {
                    // TODO draw point
                    if (m.getType() == 1)
                        draw(m.getX(), m.getY(), 0, 0, TYPE_BLOCK);
                    else if (m.getType() == 2)
                        draw(m.getX(), m.getY(), 0, 0, TYPE_CLEANED);
                    else if (m.getType() != 0)
                        draw(m.getX(), m.getY(), 0, 0, TYPE_UNCLEANED);
                }
        }
        Log.e("draw time cost", "to map = " + (System.currentTimeMillis() - __time));
        clearTrack();
        if (track.getIndex_begin() >= 0 && track.getMapPointList().size() > 0) {
            for (int index = 0; index + 1 < track.getMapPointList().size(); index++) {
                MapPoint p = track.getMapPointList().get(index);
                MapPoint p_next = track.getMapPointList().get(index + 1);
                /* 绘制 */
                draw(p.getX(), p.getY(), p_next.getX(), p_next.getY(), TYPE_TRACK);
            }

            MapPoint botPoint = track.getMapPointList().get(track.getMapPointList().size() - 1);
            canvas_lineCache.drawCircle((botPoint.getX() + offset) * scale_screen, (botPoint.getY() + offset) * scale_screen, r * scale_screen, paint_bot);
            Log.i(TAG + " bot ", botPoint.toString());
        }
        Log.e("draw time cost", "to track = " + (System.currentTimeMillis() - __time));
        postInvalidate();
    }

    public void clear() {
        if (canvas_pointCache != null) {
            Paint cleanPaint = new Paint();
            cleanPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas_pointCache.drawPaint(cleanPaint);
        }
        if (canvas_lineCache != null) {
            Paint cleanPaint = new Paint();
            cleanPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas_lineCache.drawPaint(cleanPaint);
        }
        postInvalidate();
    }

    public void clearTrack() {
        if (canvas_lineCache != null) {
            Paint cleanPaint = new Paint();
            cleanPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas_lineCache.drawPaint(cleanPaint);
        }
        postInvalidate();
    }

    /**
     * @param scale
     */
    private void setPaints(float scale) {
        paint_block = new Paint();
        paint_block.setColor(Color.parseColor("#98F5FF"));
        paint_block.setStrokeWidth(scale);
        paint_block.setAntiAlias(true);
        paint_cleaned = new Paint();
        paint_cleaned.setColor(Color.parseColor("#FF616D82"));
        paint_cleaned.setStrokeWidth(scale);
        paint_cleaned.setAntiAlias(true);
        paint_track = new Paint();
        paint_track.setColor(Color.RED);
        paint_track.setStrokeWidth(scale * stroke);
        paint_track.setAntiAlias(true);
        paint_bot = new Paint();
        paint_bot.setColor(Color.parseColor("#E0B700"));
        paint_bot.setAntiAlias(true);
        paint_gray = new Paint();
        paint_gray.setStrokeWidth(scale);
        paint_gray.setColor(Color.parseColor("#4D4D4D"));
        paint_gray.setAntiAlias(true);
    }

    /**
     * @param scale
     * @param paint_block_color
     * @param paint_cleaned_color
     * @param paint_track_color
     * @param paint_bot_color
     */
    @Deprecated
    private void setPaints(float scale, String paint_block_color, String paint_cleaned_color, String paint_track_color, String paint_bot_color) {
        int block_color = 0;
        int cleaned_color = 0;
        int track_color = 0;
        int bot_color = 0;
        try {
            block_color = Color.parseColor(paint_block_color);
            cleaned_color = Color.parseColor(paint_cleaned_color);
            track_color = Color.parseColor(paint_track_color);
            bot_color = Color.parseColor(paint_bot_color);
        } catch (IllegalArgumentException e) {
            block_color = Color.parseColor("#4D4D4D");
            cleaned_color = Color.parseColor("#FF616D82");
            track_color = Color.rgb(86, 147, 193);
            bot_color = Color.parseColor("#E1E0B700");
            if (BuildConfig.DEBUG)
                Log.e(TAG, "传入的颜色String有误");
        } finally {
            paint_block = new Paint();
            paint_block.setColor(block_color);
            paint_block.setStrokeWidth(scale);
            paint_cleaned = new Paint();
            paint_cleaned.setColor(cleaned_color);
            paint_cleaned.setStrokeWidth(scale);
            paint_track = new Paint();
            paint_track.setColor(track_color);
            paint_track.setStrokeWidth(scale * stroke);
            paint_bot = new Paint();
            paint_bot.setColor(bot_color);
        }
    }

    /**
     * @param x
     * @param y
     * @param x_next
     * @param y_next
     * @param draw_type
     */
    private void draw(int x, int y, int x_next, int y_next, int draw_type) {
        switch (draw_type) {
            case TYPE_BLOCK:
                canvas_pointCache.drawPoint(x * scale_screen, y * scale_screen, paint_block);
                break;
            case TYPE_CLEANED:
                canvas_pointCache.drawPoint(x * scale_screen, y * scale_screen, paint_cleaned);
                break;
            case TYPE_TRACK:
                canvas_lineCache.drawLine((x + offset) * scale_screen, (y + offset) * scale_screen, (x_next + offset) * scale_screen, (y_next + offset) * scale_screen, paint_track);
                break;
            case TYPE_UNCLEANED:
                canvas_pointCache.drawPoint(x * scale_screen, y * scale_screen, paint_gray);
                break;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        /* 将缓存画布拼接上原有画布 */
        canvas.concat(matrix_translate);
        canvas.concat(matrix_scale);
        if (bitmap_pointCache != null)
            canvas.drawBitmap(bitmap_pointCache, 0, 0, paint_cache);
        Log.e("draw time cost", "onDraw bitmap point = " + (System.currentTimeMillis() - __time));
        if (bitmap_lineCache != null)
            canvas.drawBitmap(bitmap_lineCache, 0, 0, paint_cache);
        Log.e("draw time cost", "onDraw bitmap line = " + (System.currentTimeMillis() - __time));
        super.onDraw(canvas);
        Log.e("draw time cost", "onDraw finish = " + (System.currentTimeMillis() - __time));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        matrix_translate.reset();
        matrix_scale.reset();
        postInvalidate();
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent event_before, MotionEvent event_current, float distanceX, float distanceY) {
        matrix_translate.postTranslate(-distanceX, -distanceY);
        postInvalidate();
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float[] values = new float[9];
        matrix_scale.getValues(values);
        scale_after_matrix = values[Matrix.MSCALE_X];

        float scale = detector.getScaleFactor() * scale_after_matrix;
        if (scale > MAX_ZOOM_SCALE) {
            scale = MAX_ZOOM_SCALE;
        }
        if (scale < MIN_NARROW_SCALE) {
            scale = MIN_NARROW_SCALE;
        }
        matrix_scale.setScale(scale, scale, detector.getFocusX(), detector.getFocusY());
        postInvalidate();
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    public List<BlockMap> getBlockMapList() {
        return blockMapList;
    }

    public void setBlockMapList(List<BlockMap> blockMapList) {
        this.blockMapList = blockMapList;
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }
}
