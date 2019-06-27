package com.shenjinmao.xw;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Vector;

public class PlayGround extends SurfaceView implements View.OnTouchListener {
    private static int WIDTH=0;
    private static final int COL=11;
    private static final int ROW=11;
    private static final int BLOCKS=30;//默认添加的路障数量
    private Dot matrix[][];
    private Dot cat;
    public PlayGround(Context context) {
        super(context);
        setOnTouchListener(this);
        getHolder().addCallback(callback);
        matrix=new Dot[ROW][COL];
        for (int i=0;i<ROW;i++){
            for (int j=0;j<COL;j++){
                matrix[i][j]=new Dot(j,i);
            }
        }
        initGame();
    }
    private Dot getDot(int x,int y){
        return matrix[y][x];
    }
    private boolean 边界判断(Dot d){
        if (d.getX()*d.getY()==0||d.getX()+1==COL||d.getY()+1==ROW){
            return true;
        }
        return false;
    }
    private int 到边界距离(Dot one,int dir){
        int distance=0;
        if (边界判断(one)){
            return 1;
        }
        Dot ori=one,next;
        while (true){
            next= 六边判断(ori,dir);
            if (next.getStatus()==Dot.STATUS_ON){
                return distance*-1;
            }
            if (边界判断(next)){
                distance++;
                return distance;
            }
            distance++;
            ori=next;
        }
    }
    private Dot 六边判断(Dot one,int dir){
        switch (dir){
            case 1:
                return getDot(one.getX()-1,one.getY());
            case 2:
                if (one.getY()%2==0){
                    return getDot(one.getX()-1,one.getY()-1);
                }else{
                    return getDot(one.getX(),one.getY()-1);
                }
            case 3:
                if (one.getY()%2==0){
                    return  getDot(one.getX(),one.getY()-1);
                }else{
                    return getDot(one.getX()+1,one.getY()-1);
                }
            case 4:
                return getDot(one.getX()+1,one.getY());
            case 5:
                if (one.getY()%2==0){
                    return  getDot(one.getX(),one.getY()+1);
                }else{
                    return  getDot(one.getX()+1,one.getY()+1);
                }
            case 6:
                if (one.getY()%2==0){
                    return  getDot(one.getX()-1,one.getY()+1);
                }else{
                    return  getDot(one.getX(),one.getY()+1);
                }
        }
        return null;
    }

    private void move(){
        if (边界判断(cat)){  Toast.makeText(getContext(),"失败了",Toast.LENGTH_LONG).show();return;   }//如果到边界了说明输了
        Vector<Dot> avaliable=new Vector<>();//可以走的路径集合。即红点六个没有障碍物的方向
        Vector<Dot> positive=new Vector<>();//
        HashMap<Dot,Integer> al=new HashMap<>();
        //循环得到红点周边六个点的坐标
        for (int i=1;i<7;i++){
            Dot n=六边判断(cat,i);
            if (n.getStatus()==Dot.STATUS_OFF){//判断红点周边六个点坐标的状态
                avaliable.add(n);
                al.put(n,i);
                if (到边界距离(n,i)>0){
                    positive.add(n);
                }
            }
        }
        if (avaliable.size()==0){//无路可走
            Toast.makeText(getContext(),"过关啦",Toast.LENGTH_LONG).show();
        }else if (avaliable.size()==1){//只有一条路可以走，走那一条
            MoveTo(avaliable.get(0));
        }else{//很多路走
            Dot best = null;
            if (positive.size()!=0){//存在可以直接到达屏幕边缘的走向
                int min=999;
                for (int i=0;i<positive.size();i++){
                    int a=到边界距离(positive.get(i),al.get(positive.get(i)));
                    if (a<min){
                        min=a;
                        best=positive.get(i);
                    }
                }
            }else{//所有方向都有路障












                best=avaliable.get(0);
            }
            MoveTo(best);
        }
    }
    private void MoveTo(Dot one){
        one.setStatus(Dot.STATUS_IN);
        getDot(cat.getX(),cat.getY()).setStatus(Dot.STATUS_OFF);
        cat.setXY(one.getX(),one.getY());
    }
    private void redraw(){
        Canvas c= getHolder().lockCanvas();//锁定Canvas
        c.drawColor(Color.LTGRAY);
        Paint paint=new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        for (int i=0;i<ROW;i++){
            int offset=0;
            if (i%2!=0){
                offset=WIDTH/2;
            }
            for (int j=0;j<COL;j++){
                Dot one=getDot(j,i);
                switch (one.getStatus()){
                    case Dot.STATUS_OFF:
                        paint.setColor(0xFFEEEEEE);
                        break;
                    case Dot.STATUS_ON:
                        paint.setColor(0xFFFFAA00);
                        break;
                    case Dot.STATUS_IN:
                        paint.setColor(0xFFFF0000);
                        break;
                }
                RectF rectF=new RectF(
                        one.getX()*WIDTH+offset,
                        one.getY()*WIDTH,
                        (one.getX()+1)*WIDTH+offset,
                        (one.getY()+1)*WIDTH);
                c.drawOval(rectF,paint);




//                //设置文字水平居中
//                Paint fondPaint=new Paint();
//                fondPaint.setColor(0xFF0000EE);
//                fondPaint.setTextSize(40);
//                fondPaint.setTextAlign(Paint.Align.CENTER);
//                //设置文字垂直居中
//                Paint.FontMetricsInt fontMetrics = fondPaint.getFontMetricsInt();
//                float baseRectF = (rectF.bottom + rectF.top - fontMetrics.bottom - fontMetrics.top)/2;
//                //行:row i x列:col j y
//                int x= cat.getX();
//                int y=cat.getY();
//                int n=Math.abs(Math.abs((i-x))-Math.abs((j-y)));
//
//                c.drawText(n+"", rectF.centerX(), baseRectF, fondPaint);


            }
        }
        getHolder().unlockCanvasAndPost(c);//解锁Canvas
    }
    SurfaceHolder.Callback callback=new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            redraw();
        }
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            WIDTH=width/(COL+1)*21/20;
            redraw();
        }
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    };
    private void initGame(){
        for (int i=0;i<ROW;i++){
            for (int j=0;j<COL;j++){
                matrix[i][j].setStatus(Dot.STATUS_OFF);
            }
        }
        cat=new Dot(5,5);
        getDot(5,5).setStatus(Dot.STATUS_IN);
        for (int i=0;i<BLOCKS;){
            int x=(int)((Math.random()*1000)%COL);
            int y=(int)((Math.random()*1000)%ROW);
            if (getDot(x,y).getStatus()==Dot.STATUS_OFF){
                getDot(x,y).setStatus(Dot.STATUS_ON);
                i++;
            }
        }
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_UP:
                int x,y;
                y= (int) (event.getY()/WIDTH);
                if (y%2==0){
                    x= (int) (event.getX()/WIDTH);
                }else{
                    x= (int) ((event.getX()-WIDTH/2)/WIDTH);
                }
                if (x+1>COL||y+1>ROW){
                    initGame();
                }else if(getDot(x,y).getStatus()==Dot.STATUS_OFF){
                    getDot(x,y).setStatus(Dot.STATUS_ON);
                    move();
                }
                redraw();
                break;
        }
        return true;
    }
}
