package com.dany.btnxt;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.Button;

public class TouchPadView extends Button{

	public TouchPadView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	public TouchPadView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public TouchPadView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	private int width = 200;
	private int height = 100;
	
	
	@Override
    public void onDraw(Canvas canvas) {
		canvas.drawRGB(0, 0, 0); //Sets background color
		Paint paint = new Paint();
		paint.setColor(0xff00ff00); //Green
		paint.setStyle(Paint.Style.STROKE);
		
		for(int i = 0; i < 400; i = i+50){
			canvas.drawArc(new RectF(i,i,400 - i,400 - i), 180, 180, true, paint);
		}
		
		canvas.drawLine(200, 0, 200, 200, paint);

	}
	
	

}
