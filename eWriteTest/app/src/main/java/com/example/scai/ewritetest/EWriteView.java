package com.example.scai.ewritetest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.lang.reflect.Type;

import static android.view.MotionEvent.ACTION_CANCEL;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;

public class EWriteView extends View {

    private Canvas mEditCanvas;
    private Paint mEditRectPaint;
    private Paint mEditCirclePaint;
    private Path mEditCirclePath;
    private float startEditPointX, startEditPointY;     //触摸屏幕开始位置
    private float moveEditPointX, moveEditPointY;       //触摸屏幕移动位置
    private float drawRectLeft, drawRectTop, drawRectRight, drawRectDown;       //最终画完矩形的坐标
    private float leftP, topP, rightP, endP;    //最终画完矩形的坐标（用于保存与替换的坐标）
    private float circleRadius;     //圆半径
    private boolean isEdit = false;     //已经画了矩形
    private boolean isRectMoved = false;    //是否可以移动
    private float moveLeft, moveTop, moveRight, moveDown;       //矩形移动后的坐标

    public EWriteView(Context context) {
        super(context);
    }

    public EWriteView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        circleRadius = ScreenScale.getWidthScale() * 50;
        mEditRectPaint = new Paint();
        mEditRectPaint.setColor(getResources().getColor(R.color.gray_666));
        mEditRectPaint.setStyle(Paint.Style.STROKE);
        mEditRectPaint.setStrokeWidth(3);

        mEditCirclePaint = new Paint();
        mEditCirclePaint.setColor(getResources().getColor(R.color.colorPrimary));
        mEditCirclePaint.setStyle(Paint.Style.FILL);
        mEditCirclePaint.setStrokeWidth(3);

        mEditRectPaint.setPathEffect(new DashPathEffect(new float[]{10, 5}, 0));

        mEditCirclePath = new Path();
    }

    public EWriteView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case ACTION_DOWN:
                startEditPointX = event.getX();
                startEditPointY = event.getY();
                if (isEdit) {
                    int rectWidth = (int) Math.abs(rightP - leftP) / 4;     //矩形宽度/4
                    int rectHeight = (int) Math.abs(endP - topP) / 4;       //矩形高度/4

                    int inRectMoveLeft = (int) (rectWidth + leftP);
                    int inRectMoveTop = (int) (rectHeight + topP);
                    int inRectMoveRight = (int) (rightP - rectWidth);
                    int inRectMoveDown = (int) (endP - rectHeight);

                    if (startEditPointX > inRectMoveLeft
                            && startEditPointX < inRectMoveRight
                            && startEditPointY > inRectMoveTop
                            && startEditPointY < inRectMoveDown) {
                        //点击位置在矩形中间1/4位置，表示可以移动
                        isRectMoved = true;
                    } else {
                        isRectMoved = false;
                    }
                }
                break;
            case ACTION_MOVE:
                moveEditPointX = event.getX();
                moveEditPointY = event.getY();
                mEditCirclePath.reset();

                if (isRectMoved) {
                    //得到手指移动距离
                    float slideX = moveEditPointX - startEditPointX;
                    float slideY = moveEditPointY - startEditPointY;

                    moveLeft = drawRectLeft + slideX;
                    moveTop = drawRectTop + slideY;
                    moveRight = drawRectRight + slideX;
                    moveDown = drawRectDown + slideY;

                    addMoveCircle(slideX, slideY);
                    invalidate();
                } else {
                    //判断往哪个方向画的矩形(起点和移动方向对比)
                    if (startEditPointX < moveEditPointX) {
                        if (startEditPointY < moveEditPointY) {
                            //5点钟方向
                            if (isEdit) {
                                editCanMove(1);
                            } else {
                                addCircle();
                                setRectValue(startEditPointX, startEditPointY, moveEditPointX, moveEditPointY);
                                isEdit = false;
                                isRectMoved = false;
                            }
                        } else {
                            //2点钟方向
                            if (isEdit) {
                                editCanMove(2);
                            } else {
                                addCircle();
                                setRectValue(startEditPointX, moveEditPointY, moveEditPointX, startEditPointY);
                                isEdit = false;
                                isRectMoved = false;
                            }
                        }
                    } else {
                        if (startEditPointY < moveEditPointY) {
                            //7点钟方向
                            if (isEdit) {
                                editCanMove(3);
                            } else {
                                addCircle();
                                setRectValue(moveEditPointX, startEditPointY, startEditPointX, moveEditPointY);
                                isEdit = false;
                                isRectMoved = false;
                            }
                        } else {
                            //10点钟方向
                            if (isEdit) {
                                editCanMove(4);
                            } else {
                                addCircle();
                                setRectValue(moveEditPointX, moveEditPointY, startEditPointX, startEditPointY);
                                isEdit = false;
                                isRectMoved = false;
                            }
                        }
                    }
                    invalidate();
                }
                break;
            case ACTION_CANCEL:
            case ACTION_UP:
                isEdit = true;
                if (isRectMoved) {
                    leftP = moveLeft;
                    topP = moveTop;
                    rightP = moveRight;
                    endP = moveDown;

                    setRectValue(moveLeft, moveTop, moveRight, moveDown);
                } else {
                    leftP = drawRectLeft;
                    topP = drawRectTop;
                    rightP = drawRectRight;
                    endP = drawRectDown;
                }
                changeSpot();
                break;
        }
        return true;
    }

    /**
     * 替换坐标，左右小的为left，上下小的为top
     */
    private void changeSpot() {
        if (leftP > rightP) {
            float change = leftP;
            leftP = rightP;
            rightP = change;
        }
        if (topP > endP) {
            float change = topP;
            topP = endP;
            endP = change;
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mEditCanvas = canvas;
        if (isRectMoved) {
            mEditCanvas.drawRect(moveLeft, moveTop, moveRight, moveDown, mEditRectPaint);
        } else {
            mEditCanvas.drawRect(drawRectLeft, drawRectTop, drawRectRight, drawRectDown, mEditRectPaint);
        }
        canvas.drawPath(mEditCirclePath, mEditCirclePaint);
    }


    public void setClipRect() {
    }

    /**
     * 判断在已经画了矩形的状态下，按下是否在四个圆内
     *
     * @param type
     */
    private void editCanMove(int type) {
        isRectMoved = false;
        if (canMoveEdit(leftP, topP, startEditPointX, startEditPointY)) {
            setRectValue(moveEditPointX, moveEditPointY, rightP, endP);
            mEditCirclePath.addCircle(moveEditPointX, moveEditPointY, circleRadius, Path.Direction.CCW);
            mEditCirclePath.addCircle(moveEditPointX, endP, circleRadius, Path.Direction.CCW);
            mEditCirclePath.addCircle(rightP, moveEditPointY, circleRadius, Path.Direction.CCW);
            mEditCirclePath.addCircle(rightP, endP, circleRadius, Path.Direction.CCW);
        } else if (canMoveEdit(leftP, endP, startEditPointX, startEditPointY)) {
            setRectValue(moveEditPointX, topP, rightP, moveEditPointY);
            mEditCirclePath.addCircle(moveEditPointX, topP, circleRadius, Path.Direction.CCW);
            mEditCirclePath.addCircle(moveEditPointX, moveEditPointY, circleRadius, Path.Direction.CCW);
            mEditCirclePath.addCircle(rightP, topP, circleRadius, Path.Direction.CCW);
            mEditCirclePath.addCircle(rightP, moveEditPointY, circleRadius, Path.Direction.CCW);
        } else if (canMoveEdit(rightP, topP, startEditPointX, startEditPointY)) {
            setRectValue(leftP, moveEditPointY, moveEditPointX, endP);
            mEditCirclePath.addCircle(leftP, moveEditPointY, circleRadius, Path.Direction.CCW);
            mEditCirclePath.addCircle(leftP, endP, circleRadius, Path.Direction.CCW);
            mEditCirclePath.addCircle(moveEditPointX, moveEditPointY, circleRadius, Path.Direction.CCW);
            mEditCirclePath.addCircle(moveEditPointX, endP, circleRadius, Path.Direction.CCW);
        } else if (canMoveEdit(rightP, endP, startEditPointX, startEditPointY)) {
            setRectValue(leftP, topP, moveEditPointX, moveEditPointY);
            mEditCirclePath.addCircle(leftP, topP, circleRadius, Path.Direction.CCW);
            mEditCirclePath.addCircle(leftP, moveEditPointY, circleRadius, Path.Direction.CCW);
            mEditCirclePath.addCircle(moveEditPointX, topP, circleRadius, Path.Direction.CCW);
            mEditCirclePath.addCircle(moveEditPointX, moveEditPointY, circleRadius, Path.Direction.CCW);
        } else {
            switch (type) {
                case 1:
                    addCircle();
                    setRectValue(startEditPointX, startEditPointY, moveEditPointX, moveEditPointY);
                    break;
                case 2:
                    addCircle();
                    setRectValue(startEditPointX, moveEditPointY, moveEditPointX, startEditPointY);
                    break;
                case 3:
                    addCircle();
                    setRectValue(moveEditPointX, startEditPointY, startEditPointX, moveEditPointY);
                    break;
                case 4:
                    addCircle();
                    setRectValue(moveEditPointX, moveEditPointY, startEditPointX, startEditPointY);
                    break;
            }
        }
    }

    /**
     * 添加圆
     */
    private void addCircle() {
        mEditCirclePath.addCircle(startEditPointX, startEditPointY, circleRadius, Path.Direction.CCW);
        mEditCirclePath.addCircle(startEditPointX, moveEditPointY, circleRadius, Path.Direction.CCW);
        mEditCirclePath.addCircle(moveEditPointX, startEditPointY, circleRadius, Path.Direction.CCW);
        mEditCirclePath.addCircle(moveEditPointX, moveEditPointY, circleRadius, Path.Direction.CCW);
    }

    /**
     * 添加移动中的圆
     *
     * @param moveX
     * @param moveY
     */
    private void addMoveCircle(float moveX, float moveY) {
        mEditCirclePath.addCircle(leftP + moveX, topP + moveY, circleRadius, Path.Direction.CCW);
        mEditCirclePath.addCircle(leftP + moveX, endP + moveY, circleRadius, Path.Direction.CCW);
        mEditCirclePath.addCircle(rightP + moveX, topP + moveY, circleRadius, Path.Direction.CCW);
        mEditCirclePath.addCircle(rightP + moveX, endP + moveY, circleRadius, Path.Direction.CCW);
    }

    /**
     * 设置矩形坐标
     *
     * @param left
     * @param top
     * @param right
     * @param down
     */
    private void setRectValue(float left, float top, float right, float down) {
        drawRectLeft = left;
        drawRectTop = top;
        drawRectRight = right;
        drawRectDown = down;
    }

    /**
     * 判断点击是否在圆内，圆内可以移动，否则不可以
     *
     * @param circleCenterX
     * @param circleCenterY
     * @param downX
     * @param downY
     * @return
     */
    private boolean canMoveEdit(float circleCenterX, float circleCenterY, float downX, float downY) {
        int distanceX = (int) Math.abs(circleCenterX - downX);
        int distanceY = (int) Math.abs(circleCenterY - downY);
        int distanceZ = (int) Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
        if (distanceZ < circleRadius) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 点击编辑框外面，编辑框消失
     *
     * @param downX
     * @param downY
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     */
    private void cancleEdit(float downX, float downY, float startX, float startY, float endX, float endY) {
        if (downX < startX || downX > endX && downY < startY || downY > endY) {
            reset(mEditCanvas);
        }
    }

    private void reset(Canvas canvas) {
//        isEdit = false;
       /* Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));*/

//        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }
}
