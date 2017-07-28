package com.sleepingbear.pvhdictandvoc;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class FlowLayout extends ViewGroup {

    private int line_height;

    public static class LayoutParams extends ViewGroup.LayoutParams {

        public final int horizontal_spacing;
        public final int vertical_spacing;

        public LayoutParams(int horizontal_spacing, int vertical_spacing) {
            super(0, 0);
            this.horizontal_spacing = horizontal_spacing;
            this.vertical_spacing = vertical_spacing;
        }
    }

    public FlowLayout(Context context) {
        super(context);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        assert (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED);

        final int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
        final int count = getChildCount();
        int line_height = 0;

        int xpos = getPaddingLeft();
        int ypos = getPaddingTop();

        int childHeightMeasureSpec;
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
        } else {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                child.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), childHeightMeasureSpec);
                //final int childw = child.getMeasuredWidth();
                //final int childw = (((String)child.getTag()).length() <= 3 ? 100 : ((String)child.getTag()).length() * 35);
                final int childw = getWordWidth((String)child.getTag());
                //line_height = Math.max(line_height, child.getMeasuredHeight() + lp.vertical_spacing);
                line_height = Math.max(line_height, 120 + lp.vertical_spacing);
                //System.out.println(child.getMeasuredHeight());
                if (xpos + childw > width) {
                    xpos = getPaddingLeft();
                    ypos += line_height;
                }

                xpos += childw + lp.horizontal_spacing;
            }
        }
        this.line_height = line_height;

        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            height = ypos + line_height;

        } else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            if (ypos + line_height < height) {
                height = ypos + line_height;
            }
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(1, 1); // default of 1px spacing
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        final int width = r - l;
        int xpos = getPaddingLeft();
        int ypos = getPaddingTop();

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                //final int childw = child.getMeasuredWidth();
                //final int childw = (((String)child.getTag()).length() <= 3 ? 100 : ((String)child.getTag()).length() * 35);
                final int childw = getWordWidth((String)child.getTag());
                //final int childh = child.getMeasuredHeight();
                final int childh = 120;
                //System.out.println(childh);
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (xpos + childw > width) {
                    xpos = getPaddingLeft();
                    //ypos += line_height;
                    ypos += 120 + lp.vertical_spacing;
                }
                child.layout(xpos, ypos, xpos + childw, ypos + childh);
                xpos += childw + lp.horizontal_spacing;
            }
        }
    }

    public int getWordWidth(String word) {
        int width = 0;
        String compWord1 = "ABCDEFGHJKLMNOPQRSTUVWXYZ";
        String compWord2 = "I";
        String compWord3 = "abcdeghkmnopqsuvwxyz";
        String compWord4 = "fijltr";

        for ( int i = 0; i < word.length() ; i++ ) {
            if ( compWord1.indexOf(word.substring(i, i +1)) > -1 ) {
                width += 36;
            } else if ( compWord2.indexOf(word.substring(i, i +1)) > -1 ) {
                width += 25;
            } else if ( compWord3.indexOf(word.substring(i, i +1)) > -1 ) {
                width += 32;
            } else if ( compWord4.indexOf(word.substring(i, i +1)) > -1 ) {
                width += 23;
            } else {
                width += 30;
            }
        }
        //DicUtils.dicLog(word + " : " + word.length() + " : " + width);
        return width;
    }
}