package ua.naiksoftware.phprunner.editor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

public class VerticalNumsLine extends View {

    private static final String tag = "VerticalNumsLine";
    private int lines = 0;
    private float interval, top;
    private float scaledDensity;
    private TextPaint paint = new TextPaint();
    private Context ctx;

    public VerticalNumsLine(Context context) {
        super(context);
        init(context);
    }

    public VerticalNumsLine(Context context, AttributeSet attr) {
        super(context, attr);
        init(context);
    }

    public VerticalNumsLine(Context context, AttributeSet attr, int style) {
        super(context, attr, style);
        init(context);
    }

    private void init(Context context) {
        scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        paint.density = scaledDensity;
        ctx = context;
    }

    public void setTextSize(int textSize) {
        paint.setTypeface(Typeface.MONOSPACE);
        paint.setTextSize(textSize);
        float fontSpacing = paint.getFontMetricsInt(null); //paint.getFontSpacing();
        float leading = paint.getFontMetrics().leading;// всегда возвращает 0
        top = fontSpacing + leading;
        interval = fontSpacing;
        invalidate();
    }

    public void setTextColor(int color) {
        paint.setColor(color);
        invalidate();
    }

    public void setLines(int num) {
        lines = num;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int j = 0; j < lines; j++) {
            canvas.drawText(String.valueOf(j + 1), 0, j * interval + top, paint);
        }
    }

    public TextPaint getPaint() {
        return paint;
    }
}
