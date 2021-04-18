package com.my.notes.notesforlater;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class ModuleStatusView extends View
{
	public static final int INVALID_INDEX = -1;
	public static final int SHAPE_CIRCLE = 0;
	public static final float DEFAULT_OUTLINE_WIDTH_DP = 2f;
	public final int EDIT_MODE_MODULE_ACCOUNT = 7;
	private String mExampleString;
	private float mExampleDimension;
	private Drawable mExampleDrawable;
	private boolean[] mCirclesStatus;
	private float mOutlineWidth;
	private float mShapeSize;
	private float mSpacing;
	private Rect[] mModuleRectangles;
	private int mOutlineColour;
	private android.graphics.Paint mPaintOutline;
	private int mFillColour;
	private Paint mPaintFill;
	private float mRadius;
	private int mMaxHorizontalCirclesThatCanFit;
	private int mShape;

	public ModuleStatusView(Context context)
	{
		super(context);
		init(null, 0);
	}

	public ModuleStatusView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(attrs, 0);
	}

	public ModuleStatusView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	private void init(AttributeSet attrs, int defStyle)
	{
		if (isInEditMode())
		{
			setupEditModeValues();
		}

		DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
		float displayDensity = dm.density;
		float defaultOutlineWidthPixels = displayDensity * DEFAULT_OUTLINE_WIDTH_DP;


		// Load attributes
		final TypedArray a = getContext()
				.obtainStyledAttributes(attrs, R.styleable.ModuleStatusView, defStyle, 0);

		mOutlineColour = a.getColor(R.styleable.ModuleStatusView_outline_colour, Color.BLACK);
		mShape = a.getInt(R.styleable.ModuleStatusView_shape, SHAPE_CIRCLE);
		mOutlineWidth = a
				.getDimension(R.styleable.ModuleStatusView_outlineWidth, defaultOutlineWidthPixels);

		a.recycle();

		mShapeSize = 144f;
		mSpacing = 30f;
		mRadius = (mShapeSize - mOutlineWidth) / 2;


		mPaintOutline = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintOutline.setStyle(android.graphics.Paint.Style.STROKE);
		mPaintOutline.setStrokeWidth(mOutlineWidth);
		mPaintOutline.setColor(mOutlineColour);


		mFillColour = getContext().getResources().getColor(R.color.sypho_blue_light);
		mPaintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintFill.setStyle(Paint.Style.FILL);
		mPaintFill.setColor(mFillColour);
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int desiredWidth = 0;
		int desiredHeight = 0;

		int specWidth = MeasureSpec.getSize(widthMeasureSpec);
		int availableWidth = specWidth - getPaddingLeft() - getPaddingRight();
		int horizontalCirclesThatCanFit = (int) (availableWidth / (mShapeSize + mSpacing));
		mMaxHorizontalCirclesThatCanFit = Math
				.min(horizontalCirclesThatCanFit, mCirclesStatus.length);

		desiredWidth = (int) ((mMaxHorizontalCirclesThatCanFit * (mShapeSize + mSpacing)) - mSpacing);
		desiredWidth += getPaddingLeft() + getPaddingRight();

		int rows = ((mCirclesStatus.length - 1) / mMaxHorizontalCirclesThatCanFit) + 1;


		desiredHeight = (int) ((rows * (mShapeSize + mSpacing)) - mSpacing);
		desiredHeight += getPaddingTop() + getPaddingBottom();

		int width = resolveSizeAndState(desiredWidth, widthMeasureSpec, 0);
		int height = resolveSizeAndState(desiredHeight, heightMeasureSpec, 0);

		setMeasuredDimension(width, height);
	}

	private void setupEditModeValues()
	{
		boolean[] exampleModuleValues = new boolean[EDIT_MODE_MODULE_ACCOUNT];
		int middle = EDIT_MODE_MODULE_ACCOUNT / 2;

		for (int i = 0; i < middle; i++)
		{
			exampleModuleValues[i] = true;
		}
		setCirclesStatus(exampleModuleValues);
	}

	private void setupModuleRectangles(int width)
	{
		int availableWidth = width - getPaddingLeft() - getPaddingRight();
		int horizontalCirclesThatCanFit = (int) (availableWidth / (mShapeSize + mSpacing));
		int MaxHorizontalCircles = Math.min(horizontalCirclesThatCanFit, mCirclesStatus.length);

		mModuleRectangles = new Rect[mCirclesStatus.length];
		for (int moduleIndex = 0, recLength = mModuleRectangles.length; moduleIndex < recLength; moduleIndex++)
		{
			int column = moduleIndex % MaxHorizontalCircles;
			int row = moduleIndex / MaxHorizontalCircles;

			int x = getPaddingLeft() + (int) (column * (mShapeSize + mSpacing));
			int y = getPaddingTop() + (int) (row * (mShapeSize + mSpacing));

			mModuleRectangles[moduleIndex] = new Rect(x, y, x + (int) mShapeSize, y + (int) mShapeSize);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		setupModuleRectangles(w);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		switch (event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				return true;
			case MotionEvent.ACTION_UP:
				int moduleIndex = findItemAtPoint(event.getX(), event.getY());
				onModuleSelected(moduleIndex);
				return true;
		}

		return super.onTouchEvent(event);
	}

	private void onModuleSelected(int moduleIndex)
	{
		if (moduleIndex == INVALID_INDEX) return;

		mCirclesStatus[moduleIndex] = !mCirclesStatus[moduleIndex];

		invalidate();
	}

	private int findItemAtPoint(float x, float y)
	{
		int moduleIndex = INVALID_INDEX;

		for (int i = 0; i < mModuleRectangles.length; i++)
		{
			if (mModuleRectangles[i].contains((int) x, (int) y))
			{
				moduleIndex = i;
				break;
			}
		}
		return moduleIndex;
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		for (int moduleIndex = 0, recLength = mModuleRectangles.length; moduleIndex < recLength; moduleIndex++)
		{
			if (mShape == SHAPE_CIRCLE)
			{
				float x = mModuleRectangles[moduleIndex].centerX();
				float y = mModuleRectangles[moduleIndex].centerY();

				if (mCirclesStatus[moduleIndex])
				{
					canvas.drawCircle(x, y, mRadius, mPaintFill);
				}

				canvas.drawCircle(x, y, mRadius, mPaintOutline);
			}
			else
			{
				drawSquare(canvas, moduleIndex);
			}
		}
	}


	void drawSquare(Canvas canvas, int moduleIndex)
	{
		Rect shapeRectangle = mModuleRectangles[moduleIndex];

		if (mCirclesStatus[moduleIndex])
		{
			canvas.drawRect(shapeRectangle, mPaintFill);
		}

		canvas.drawRect(
				shapeRectangle.left + (mOutlineWidth / 2),
				shapeRectangle.top + (mOutlineWidth / 2),
				shapeRectangle.right - (mOutlineWidth / 2),
				shapeRectangle.bottom + (mOutlineWidth / 2),
				mPaintOutline);


	}

	public boolean[] getCirclesStatus()
	{
		return mCirclesStatus;
	}

	public void setCirclesStatus(boolean[] circlesStatus)
	{
		mCirclesStatus = circlesStatus;
	}


	/**
	 * Gets the example string attribute value.
	 *
	 * @return The example string attribute value.
	 */
	public String getExampleString()
	{
		return mExampleString;
	}

	/**
	 * Sets the view"s example string attribute value. In the example view, this string
	 * is the text to draw.
	 *
	 * @param exampleString The example string attribute value to use.
	 */
	public void setExampleString(String exampleString)
	{
		mExampleString = exampleString;
	}


	/**
	 * Gets the example dimension attribute value.
	 *
	 * @return The example dimension attribute value.
	 */
	public float getExampleDimension()
	{
		return mExampleDimension;
	}

	/**
	 * Sets the view"s example dimension attribute value. In the example view, this dimension
	 * is the font size.
	 *
	 * @param exampleDimension The example dimension attribute value to use.
	 */
	public void setExampleDimension(float exampleDimension)
	{
		mExampleDimension = exampleDimension;
	}

	/**
	 * Gets the example drawable attribute value.
	 *
	 * @return The example drawable attribute value.
	 */
	public Drawable getExampleDrawable()
	{
		return mExampleDrawable;
	}

	/**
	 * Sets the view"s example drawable attribute value. In the example view, this drawable is
	 * drawn above the text.
	 *
	 * @param exampleDrawable The example drawable attribute value to use.
	 */
	public void setExampleDrawable(Drawable exampleDrawable)
	{
		mExampleDrawable = exampleDrawable;
	}
}