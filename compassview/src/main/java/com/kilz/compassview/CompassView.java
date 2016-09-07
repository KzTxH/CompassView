package com.kilz.compassview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import java.util.Locale;


/**
 * TODO: CompassView Class
 * code: KilzVN
 */
public class CompassView extends View {
    private final String[] directions_def = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
    private final String[] directions_vi = {"B", "ĐB", "Đ", "ĐN", "N", "TN", "T", "TB"};
    private String[] directions;
    private int direction_start;
    private float x_start;
    private int sticker_size, space;
    private float half_sticker_size;
    private int sticker_and_space, sticker_space_x2;
    private int degrees, temp_degrees;
    private float distance_per_degrees;
    private float text_size;
    private Paint paint;
    private boolean running = false;
    private int sticker_color;
    private int mid_sticker_color;
    private int text_color;
    private RectF rect;
    private boolean check = true;
    private Shader shader;

    public CompassView(Context context) {
        super(context);
        loadAttrs(context, null, 0);
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadAttrs(context, attrs, 0);
    }

    public CompassView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        loadAttrs(context, attrs, defStyle);
    }

    private void loadAttrs(Context context, AttributeSet attrs, int defStyle) {
        // Load Attrs
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CompassView);
        degrees = typedArray.getInteger(R.styleable.CompassView_degrees, 0);
        sticker_size = typedArray.getInteger(R.styleable.CompassView_sticker_size, 7);
        text_size = typedArray.getFloat(R.styleable.CompassView_text_size, 80f);
        space = typedArray.getInteger(R.styleable.CompassView_space, 100);
        sticker_color = typedArray.getColor(R.styleable.CompassView_sticker_color, Color.RED);
        mid_sticker_color = typedArray.getColor(R.styleable.CompassView_mid_sticker_color, Color.MAGENTA);
        text_color = typedArray.getColor(R.styleable.CompassView_text_color, Color.GREEN);
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = 400;
        int desiredHeight = 120;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }
        setMeasuredDimension(width, height);
        if (check) {
            init(width, height);
            check = false;
        }
    }

    private void init(int width, int height) {
        //init
        if (Locale.getDefault().getDisplayLanguage().equals("Tiếng Việt")) {
            directions = directions_vi;
        } else {
            directions = directions_def;
        }
        sticker_and_space = sticker_size + space;
        sticker_space_x2 = sticker_and_space * 2;
        distance_per_degrees = sticker_and_space / 22.5f;
        half_sticker_size = sticker_size / 2f;
        paint = new Paint();
        rect = new RectF(0, 0, width, height);
        shader = new RadialGradient(width / 2f, height / 2f, width / 2f, new int[]{Color.argb(0, 255, 255, 255), Color.argb(255, 0, 0, 0)}, new float[]{0.1f, 1f}, Shader.TileMode.CLAMP);
        if ((width / 2) % sticker_space_x2 != 0) {
            x_start = (width / 2) - (width / 2 / sticker_space_x2 + 1) * sticker_space_x2;
            direction_start = 8 + (-(width / 2 / sticker_space_x2 + 1));
        } else {
            x_start = 0;
            direction_start = 8 + (-(width / 2 / sticker_space_x2));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int direction_next;
        float x_next;
        super.onDraw(canvas);

        // canvas color
        canvas.drawColor(Color.WHITE);
        // Center Sticker
        paint.setColor(mid_sticker_color);
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(null);
        canvas.drawRect((canvas.getWidth() / 2) - half_sticker_size, 0, (canvas.getWidth() / 2) + sticker_size - half_sticker_size, canvas.getHeight(), paint);

        getXStart();
        x_next = x_start;
        direction_next = direction_start;
        do {
            //draw Sticker
            paint.setColor(sticker_color);
            canvas.drawRect(x_next - half_sticker_size, canvas.getHeight() / 5 * 4, x_next - half_sticker_size + sticker_size, canvas.getHeight(), paint);
            canvas.drawRect(x_next - half_sticker_size + sticker_and_space, canvas.getHeight() / 5, x_next - half_sticker_size + sticker_and_space + sticker_size, canvas.getHeight(), paint);
            //draw Text
            paint.setColor(text_color);
            paint.setTextAlign(Paint.Align.CENTER);
            direction_next = loop(direction_next, 8);
            //set text size
            if (direction_next % 2 == 0) {
                paint.setTextSize(text_size);
            } else {

                paint.setTextSize(text_size / 3 * 2);
            }
            canvas.drawText(directions[direction_next], x_next - half_sticker_size, canvas.getHeight() / 3 * 2, paint);

            direction_next++;
            x_next += sticker_space_x2;
        } while (x_next <= canvas.getWidth() + sticker_space_x2);

        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(canvas.getWidth());
        paint.setShader(shader);
        canvas.drawOval(rect, paint);

        if (running) {
            if (temp_degrees <= 360 - temp_degrees) {
                x_start -= distance_per_degrees * temp_degrees;
                running = false;
                this.invalidate();
            } else {
                x_start += distance_per_degrees * (360 - temp_degrees);
                running = false;
                this.invalidate();
            }
        }
    }

    private void getXStart() {
        if (x_start >= 0) {
            x_start -= sticker_space_x2;
            direction_start--;
            direction_start = loop(direction_start, 8);
        } else if (x_start <= 0 - sticker_space_x2) {
            x_start += sticker_space_x2;
            direction_start++;
            direction_start = loop(direction_start, 8);
        }
        if (x_start <= 0 && x_start >= 0 - sticker_space_x2) {

        } else {
            getXStart();
        }
    }

    private int loop(int value, int limited) {
        if (value >= limited) {
            value -= limited;
        } else if (value < 0) {
            value = limited + value;
        }
        if (value < limited && value >= 0) {

        } else {
            loop(value, limited);
        }
        return value;
    }

    public int getDegrees() {
        return degrees;
    }

    public void setDegrees(int degrees) {
        int old_degrees;
        if (!running && this.degrees != degrees) {
            old_degrees = this.degrees;
            this.degrees = degrees;
            temp_degrees = degrees - old_degrees;
            temp_degrees = loop(temp_degrees, 360);
            running = true;
            this.invalidate();
        }
    }

    public int getStickerSize() {
        return sticker_size;
    }

    public void setStickerSize(int size) {
        this.sticker_size = size;
    }

    public float getTextSize() {
        return text_size;
    }

    public void setTextSize(float size) {
        this.text_size = size;
    }

    public int getSpace() {
        return space;
    }

    public void setSpace(int space) {
        this.space = space;
    }

    public int getStickerColor() {
        return sticker_color;
    }

    public void setStickerColor(int color) {
        this.sticker_color = color;
    }

    public int getMidStickercolor() {
        return mid_sticker_color;
    }

    public void setMidStickerColor(int color) {
        this.mid_sticker_color = color;
    }

    public int getTextColor() {
        return text_color;
    }

    public void setTextColor(int color) {
        this.text_color = color;
    }
}
