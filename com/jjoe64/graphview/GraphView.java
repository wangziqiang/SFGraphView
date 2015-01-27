/**

 * This file is part of GraphView.
 *
 * GraphView is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GraphView is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GraphView.  If not, see <http://www.gnu.org/licenses/lgpl.html>.
 *
 * Copyright Jonas Gehring
 */

package com.jjoe64.graphview;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphViewStyle.GridStyle;
import com.jjoe64.graphview.compatible.ScaleGestureDetector;

/**
 * GraphView is a Android View for creating zoomable and scrollable graphs. This
 * is the abstract base class for all graphs. Extend this class and implement
 * {@link #drawSeries(android.graphics.Canvas, GraphViewDataInterface[], float, float, float, double, double, double, double, float, com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle)}
 * to display a custom graph. Use {@link com.jjoe64.graphview.LineGraphView} for
 * creating a line chart.
 * 
 * @author jjoe64 - jonas gehring - http://www.jjoe64.com
 * 
 *         Copyright (C) 2011 Jonas Gehring Licensed under the GNU Lesser
 *         General Public License (LGPL) http://www.gnu.org/licenses/lgpl.html
 */
abstract public class GraphView extends LinearLayout {
	// static final private class GraphViewConfig {
	// static final float BORDER = 50;
	// }

	private class GraphViewContentView extends View {
		private float lastTouchEventX;
		private float lastTouchEventY;
		private float graphwidth;
		private boolean scrollingStarted;

		private float scrollX;
		private float scrollY;
		// 6.2add
		private float v0;
		private VelocityTracker tracker = null;

		/**
		 * @param context
		 */
		public GraphViewContentView(Context context) {
			super(context);
			setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		}

		/**
		 * @param canvas
		 */
		@Override
		protected void onDraw(Canvas canvas) {

			paint.setAntiAlias(true);

			// normal
			paint.setStrokeWidth(0);

			float border = getGraphViewStyle().getBorder();
			float horstart = 0;
			float height = getHeight();
			float width = getWidth() - 1;
			double maxY = getMaxY();
			double minY = getMinY();
			double maxX = getMaxX(false);
			double minX = getMinX(false);
			double diffX = maxX - minX;

			// measure bottom text
			if (labelTextHeight == null || horLabelTextWidth == null) {
				paint.setTextSize(getGraphViewStyle().getTextSize());
				double testX = ((getMaxX(true) - getMinX(true)) * 0.783) + getMinX(true);
				String testLabel = formatLabel(testX, true);
				paint.getTextBounds("," + testLabel, 0, testLabel.length(), textBounds);
				labelTextHeight = (textBounds.height());
				horLabelTextWidth = (textBounds.width());
			}
			border += labelTextHeight;

			float graphheight = height - (2 * border);
			graphwidth = width;
			// if labels is static,do not invoke generateHorlabels.
			if (horlabels == null) {
				horlabels = generateHorlabels(graphwidth);
			}
			if (verlabels == null) {
				verlabels = generateVerlabels(graphheight);
			}

			// 6.1add-----abolish horizontal lines by default-----
			if (graphViewStyle.getIsDrawHoriLines()) {

				if (graphViewStyle.getGridStyle() != GridStyle.HORIZONTAL) {
					paint.setTextAlign(Align.LEFT);
					int vers = verlabels.length - 1;
					for (int i = 0; i < verlabels.length; i++) {
						paint.setColor(graphViewStyle.getGridColor());
						float y = ((graphheight / vers) * i) + border;
						canvas.drawLine(horstart, y, width, y, paint);
					}
				}
			} else {

				// 6.1add-----draw bottom horizontal line only.-----
				paint.setColor(graphViewStyle.getGridColor());
				canvas.drawLine(horstart, graphheight + border, width, graphheight + border, paint);
			}
			// 6.1deprecated-----deprecated drawing vertical lines in
			// here,instead of implementing it in LineGraphView.-----

			// drawHorizontalLabels(canvas, border, horstart, height, horlabels,
			// graphwidth, scrollX);

			// 6.1add-----draw two vertical border lines.-----
			drawVerticalBorderLines(canvas, border, horstart, height, graphwidth, verticalAlign);
			// draw title
			paint.setColor(graphViewStyle.getTitleColor());
			paint.setTextAlign(Align.CENTER);
			paint.setTextSize(graphViewStyle.getTitleTextSize());
			canvas.drawText(
					title,
					(graphwidth / 2) + horstart,
					border - graphViewStyle.getTitleHeightFactor()
							* graphViewStyle.getTitleTextSize(), paint);

			paint.setTextSize(graphViewStyle.getTextSize());

			if (maxY == minY) {
				// if min/max is the same, fake it so that we can render a line
				if (maxY == 0) {
					// if both are zero, change the values to prevent division
					// by zero
					maxY = 1.0d;
					minY = 0.0d;
				} else {
					maxY = maxY * 1.05d;
					minY = minY * 0.95d;
				}
			}

			double diffY = oldMaxY - oldMinY;
			paint.setStrokeCap(Paint.Cap.ROUND);
			double _maxX = getMaxX(true);

			for (int i = 0; i < graphSeries.size(); i++) {
				drawSeries(canvas, _values(i), graphwidth, graphheight, border, minX, oldMinY,
						diffX, diffY, horstart, viewportSize, scalable, graphSeries.get(i), _maxX);
			}

			if (showLegend)
				// 6.1deprecated-----legend align bottom.-----
				// drawLegend(canvas, height, width);
				drawBottomLegend(canvas, height, width, border);
		}

		private void onMoveGesture(float f) {
			// view port update
			if (viewportSize != 0) {
				viewportStart -= f * viewportSize / graphwidth;

				// minimal and maximal view limit
				double minX = getMinX(true);
				double maxX = getMaxX(true);
				if (viewportStart < minX && viewportStart < -0.5) {
					viewportStart = -0.5;
				} else if (viewportStart + viewportSize > maxX) {
					viewportStart = maxX - viewportSize;
				}

				// labels have to be regenerated
				if (!staticHorizontalLabels)
					horlabels = null;
				if (!staticVerticalLabels)
					verlabels = null;
				viewVerLabels.invalidate();
			}
			invalidate();
		}

		/**
		 * @param event
		 */
		@Override
		public boolean onTouchEvent(MotionEvent event) {

			if (!isScrollable() || isDisableTouch()) {
				return super.onTouchEvent(event);
			}

			boolean handled = false;
			// first scale
			if (scalable && scaleDetector != null) {
				scaleDetector.onTouchEvent(event);
				handled = scaleDetector.isInProgress();
			}
			if (!handled) {
				// Log.d("GraphView",
				// "on touch event scale not handled+"+lastTouchEventX);
				// if not scaled, scroll
				if ((event.getAction() & MotionEvent.ACTION_DOWN) == MotionEvent.ACTION_DOWN
						&& (event.getAction() & MotionEvent.ACTION_MOVE) == 0) {
					scrollingStarted = true;
					handled = true;
					// 6.2add
					if (null == tracker) {
						tracker = VelocityTracker.obtain();
					} else {
						tracker.clear();
					}
					tracker.addMovement(event);
				}
				if ((event.getAction() & MotionEvent.ACTION_UP) == MotionEvent.ACTION_UP) {
					scrollingStarted = false;
					lastTouchEventX = 0;
					lastTouchEventY = 0;
					handled = true;
					// 6.2add
					final float a = -0.3f;
					final float ut = 10f;
					Runnable r = new Runnable() {
						@Override
						public void run() {
							float s1 = v0 * ut + 0.5f * a * ut * ut;
							float s2 = v0 * ut - 0.5f * a * ut * ut;
							if (scrollX > 0 && s1 > 0) {
								onMoveGesture(s1 / 10);
								mHandler.postDelayed(this, 10);
								v0 = s1 / 10;
							} else if (scrollX < 0 && s2 < 0) {
								onMoveGesture(s2 / 10);
								mHandler.postDelayed(this, 10);
								v0 = s2 / 10;
							}
						}
					};
					mHandler.post(r);
					if (onScrolledListener != null) {
						onScrolledListener.OnScrolled();
					}
				}
				if ((event.getAction() & MotionEvent.ACTION_MOVE) == MotionEvent.ACTION_MOVE) {
					if (scrollingStarted) {
						if (lastTouchEventX != 0) {
							scrollX = event.getX() - lastTouchEventX;
							scrollY = event.getY() - lastTouchEventY;

							if (Math.abs(scrollX) > Math.abs(scrollY)) {
								if (getParent() != null) {
									getParent().requestDisallowInterceptTouchEvent(true);
								}
							}

							onMoveGesture(scrollX);
						}
						lastTouchEventX = event.getX();
						lastTouchEventY = event.getY();
						handled = true;
						// 6.2add
						tracker.addMovement(event);
						tracker.computeCurrentVelocity(10);
						v0 = tracker.getXVelocity();
					}
				}
				if ((event.getAction() & MotionEvent.ACTION_CANCEL) == MotionEvent.ACTION_CANCEL) {
					try {
						tracker.recycle();
					} catch (Exception e) {
					}
					tracker = null;
				}
				if (handled)
					invalidate();
			} else {
				// currently scaling
				scrollingStarted = false;
				lastTouchEventX = 0;
				lastTouchEventY = 0;
			}
			return handled;
		}
	}

	/**
	 * one data set for a graph series
	 */
	static public class GraphViewData implements GraphViewDataInterface {
		public KeyValuePair valueX;
		public double valueY;

		public GraphViewData(KeyValuePair valueX, double valueY) {
			super();
			this.valueX = valueX;
			this.valueY = valueY;
		}

		@Override
		public KeyValuePair getX() {
			return valueX;
		}

		@Override
		public double getY() {
			return valueY;
		}

		@Override
		public void setX(KeyValuePair pair) {
			valueX = pair;
		}

		@Override
		public void setY(double y) {
			valueY = y;
		}

	}

	public enum LegendAlign {
		TOP, MIDDLE, BOTTOM
	}

	private class VerLabelsView extends View {
		/**
		 * @param context
		 */
		public VerLabelsView(Context context) {
			super(context);
			setLayoutParams(new LayoutParams(
					getGraphViewStyle().getVerticalLabelsWidth() == 0 ? 100 : getGraphViewStyle()
							.getVerticalLabelsWidth(), LayoutParams.FILL_PARENT));
		}

		/**
		 * @param canvas
		 */
		@SuppressLint("DrawAllocation")
		@Override
		protected void onDraw(Canvas canvas) {
			// normal
			paint.setStrokeWidth(0);
			// measure bottom text
			if (labelTextHeight == null || verLabelTextWidth == null) {
				paint.setTextSize(getGraphViewStyle().getTextSize());
				double testY = ((getMaxY() - getMinY()) * 0.783) + getMinY();
				String testLabel = formatLabel(testY, false);
				paint.getTextBounds(testLabel, 0, testLabel.length(), textBounds);
				labelTextHeight = (textBounds.height());
				verLabelTextWidth = (textBounds.width());
			}

			if (getGraphViewStyle().getVerticalLabelsWidth() == 0
					&& getLayoutParams().width != verLabelTextWidth
							+ getGraphViewStyle().getBorder()) {
				setLayoutParams(new LayoutParams((int) (verLabelTextWidth + getGraphViewStyle()
						.getBorder()), LayoutParams.FILL_PARENT));
			} else if (getGraphViewStyle().getVerticalLabelsWidth() != 0
					&& getGraphViewStyle().getVerticalLabelsWidth() != getLayoutParams().width) {
				setLayoutParams(new LayoutParams(getGraphViewStyle().getVerticalLabelsWidth(),
						LayoutParams.FILL_PARENT));
			}

			float border = getGraphViewStyle().getBorder();
			border += labelTextHeight;
			float height = getHeight();
			float graphheight = height - (2 * border);

			if (verlabels == null) {
				verlabels = generateVerlabels(graphheight);
			} else if (staticVerticalLabels) {
				oldMaxY = manualMaxYValue;
				oldMinY = manualMinYValue;
			}

			// vertical labels
			paint.setTextAlign(getGraphViewStyle().getVerticalLabelsAlign());
			int labelsWidth = getWidth();
			int labelsOffset = 0;
			if (getGraphViewStyle().getVerticalLabelsAlign() == Align.RIGHT) {
				labelsOffset = labelsWidth;
			} else if (getGraphViewStyle().getVerticalLabelsAlign() == Align.CENTER) {
				labelsOffset = labelsWidth / 2;
			}
			int vers = verlabels.length - 1;
			for (int i = 0; i < verlabels.length; i++) {
				float y = ((graphheight / vers) * i) + border;
				paint.setColor(graphViewStyle.getVerticalLabelsColor());
				canvas.drawText(verlabels[i], labelsOffset, y + graphViewStyle.getTextSize() / 3f,
						paint);
			}

			// 6.1add-----draw unit description.-----
			canvas.drawText(graphViewStyle.getVerticalUnit(), labelsOffset,
					border - graphViewStyle.getTextSize(), paint);

			// reset
			paint.setTextAlign(Align.LEFT);
		}
	}

	private class ScaleView extends View {

		public ScaleView(Context context) {
			super(context);
			setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			float border = getGraphViewStyle().getBorder();
			float horstart = 0;
			float height = getHeight();
			float width = getWidth() - 1;

			border += labelTextHeight;
			float graphheight = height - (2 * border);

			paint.setStyle(Style.FILL);
			paint.setColor(Color.WHITE);
			canvas.drawRect(horstart, border - paint.getTextSize(), width - 1, graphheight + border
					- 1, paint);
		}

	}

	// TODO Auto-generated method stub
	private class CoverView extends View {

		public CoverView(Context context) {
			super(context);
			setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			float border = getGraphViewStyle().getBorder();
			float height = getHeight();
			float width = getWidth() - 1;

			border += labelTextHeight;
			float graphheight = height - (2 * border);

			paint.setStyle(Style.FILL);
			paint.setColor(Color.WHITE);
			canvas.drawRect(width - width / 8, border - paint.getTextSize(), width - 1, graphheight
					+ border - 1, paint);
		}

	}

	protected final Paint paint;
	protected String[] horlabels;
	private String[] verlabels;
	private String title;
	private boolean scrollable;
	private boolean disableTouch;
	private double viewportStart;
	private double viewportSize;
	private View viewVerLabels;
	private ScaleGestureDetector scaleDetector;
	private boolean scalable;
	private final NumberFormat[] numberformatter = new NumberFormat[2];
	private final List<GraphViewSeries> graphSeries;
	private boolean showLegend = false;
	private LegendAlign legendAlign = LegendAlign.MIDDLE;
	private String suiteText = "";
	private boolean manualYAxis;
	private boolean manualMaxY;
	private boolean manualMinY;
	private double manualMaxYValue;
	private double manualMinYValue;
	protected GraphViewStyle graphViewStyle;
	private GraphViewContentView graphViewContentView;
	private CustomLabelFormatter customLabelFormatter;
	private Integer labelTextHeight;
	private Integer horLabelTextWidth;
	private Integer verLabelTextWidth;
	private final Rect textBounds = new Rect();
	private boolean staticHorizontalLabels;
	private boolean staticVerticalLabels;
	private boolean showHorizontalLabels = true;
	private boolean showVerticalLabels = true;
	private Align verticalAlign;
	private OnScrolledListener onScrolledListener;
	private ScaleView scaleView;
	private CoverView coverView;
	private FrameLayout frameLayout;
	private Animation animation;
	double _viewportStart = 0;
	private Handler mHandler = new Handler();
	private Context context;

	public GraphView(Context context, AttributeSet attrs) {
		this(context, attrs.getAttributeValue(null, "title"), Align.LEFT);

		int width = attrs.getAttributeIntValue("android", "layout_width", LayoutParams.FILL_PARENT);
		int height = attrs.getAttributeIntValue("android", "layout_height",
				LayoutParams.FILL_PARENT);
		setLayoutParams(new LayoutParams(width, height));
	}

	/**
	 * @param context
	 * @param title
	 *            [optional]
	 */
	public GraphView(Context context, String title, Align verticalAlign) {
		super(context);
		this.context = context;
		setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		scaleView = new ScaleView(context);
		coverView = new CoverView(context);
		frameLayout = new FrameLayout(context);

		this.verticalAlign = verticalAlign;

		if (title == null)
			this.title = "";
		else
			this.title = title;

		graphViewStyle = new GraphViewStyle();
		graphViewStyle.useTextColorFromTheme(context);

		paint = new Paint();
		graphSeries = new ArrayList<GraphViewSeries>();

	}

	private GraphViewDataInterface[] _values(int idxSeries) {
		GraphViewDataInterface[] values = graphSeries.get(idxSeries).values;
		synchronized (values) {
			if (viewportStart == 0 && viewportSize == 0) {
				// all data
				return values;
			} else {
				// viewport
				List<GraphViewDataInterface> listData = new ArrayList<GraphViewDataInterface>();
				for (int i = 0; i < values.length; i++) {
					if (values[i].getX().getKey() >= viewportStart) {
						if (values[i].getX().getKey() > viewportStart + viewportSize) {
							listData.add(values[i]); // one more for nice
														// scrolling
							break;
						} else {
							listData.add(values[i]);
						}
					} else {
						if (listData.isEmpty()) {
							listData.add(values[i]);
						}
						listData.set(0, values[i]); // one before, for nice
													// scrolling
					}
				}
				return listData.toArray(new GraphViewDataInterface[listData.size()]);
			}
		}
	}

	public void addSeries(GraphViewSeries series) {
		addSeries(series, true);
	}

	/**
	 * add a series of data to the graph
	 * 
	 * @param series
	 */
	public void addSeries(GraphViewSeries series, boolean isDrawingLine) {
		if (series == null) {
			return;
		}
		if (series.trendX != null && series.trendY > 0) {
			series.addValue(series.trendX, series.trendY);
		}
		series.addGraphView(this);
		graphSeries.add(series);

		if (null == graphViewContentView && null == viewVerLabels) {

			if (verticalAlign == Align.RIGHT) {
				graphViewContentView = new GraphViewContentView(context);
				frameLayout.addView(graphViewContentView, new LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
				frameLayout.addView(scaleView, new LayoutParams(LayoutParams.FILL_PARENT,
						LayoutParams.FILL_PARENT));
				frameLayout.addView(coverView, new LayoutParams(LayoutParams.FILL_PARENT,
						LayoutParams.FILL_PARENT));
				addView(frameLayout, new LayoutParams(LayoutParams.FILL_PARENT,
						LayoutParams.FILL_PARENT, 1));
				viewVerLabels = new VerLabelsView(context);
				addView(viewVerLabels);
			} else {
				viewVerLabels = new VerLabelsView(context);
				addView(viewVerLabels);
				graphViewContentView = new GraphViewContentView(context);
				frameLayout.addView(graphViewContentView, new LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
				frameLayout.addView(scaleView, new LayoutParams(LayoutParams.FILL_PARENT,
						LayoutParams.FILL_PARENT));
				frameLayout.addView(coverView, new LayoutParams(LayoutParams.FILL_PARENT,
						LayoutParams.FILL_PARENT));
				addView(frameLayout, new LayoutParams(LayoutParams.FILL_PARENT,
						LayoutParams.FILL_PARENT, 1));
			}
		} else {
			// do nothing.
		}
		if (isDrawingLine) {
			// setViewPort(0, 5);
			_viewportStart = 0;
			animation = new ScaleAnimation(1.0f, 0.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF,
					1.0f, Animation.RELATIVE_TO_SELF, 1.0f);
			animation.setDuration(1000);
			animation.setInterpolator(new LinearInterpolator());
			animation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {

				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					scaleView.setVisibility(View.GONE);
					postdelay();
				}
			});
			scaleView.startAnimation(animation);
		} else {
			scaleView.setVisibility(View.GONE);
			coverView.setVisibility(View.GONE);
		}
	}

	private void postdelay() {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				graphViewContentView.onMoveGesture(graphViewStyle.getDistance());
				if (viewportStart > _viewportStart) {
					mHandler.postDelayed(this, 5);
					_viewportStart = viewportStart;
				} else {
					coverView.setVisibility(View.GONE);
				}
			}
		};
		mHandler.post(r);
	}

	public void setOnScrolledListener(OnScrolledListener onScrolledListener) {
		this.onScrolledListener = onScrolledListener;
	}

	/**
	 * 6.1deprecated-----instead of implementing it in GraphView.-----
	 * 
	 * @param canvas
	 * @param border
	 * @param horstart
	 * @param height
	 * @param horlabels
	 * @param graphwidth
	 * @param scrollX
	 */
	@Deprecated
	protected void drawHorizontalLabels(Canvas canvas, float border, float horstart, float height,
			String[] horlabels, float graphwidth, float scrollX) {
		// horizontal labels + vertical lines
		int hors = horlabels.length - 1;
		for (int i = 0; i < horlabels.length; i++) {
			paint.setColor(graphViewStyle.getGridColor());
			paint.setStrokeWidth(0);

			float x = ((graphwidth / hors) * i) + horstart + scrollX;
			if (graphViewStyle.getGridStyle() != GridStyle.VERTICAL) {
				canvas.drawLine(x, height - border, x, border, paint);
			}
			if (showHorizontalLabels) {
				paint.setTextAlign(Align.CENTER);
				if (i == horlabels.length - 1)
					paint.setTextAlign(Align.RIGHT);
				if (i == 0)
					paint.setTextAlign(Align.LEFT);
				paint.setColor(graphViewStyle.getHorizontalLabelsColor());
				canvas.drawText(horlabels[i], x, height - 4, paint);
			}
		}
	}

	/**
	 * 6.1add-----draw two vertical border lines.-----
	 * 
	 * @param canvas
	 * @param border
	 * @param horstart
	 * @param height
	 * @param graphwidth
	 */
	protected void drawVerticalBorderLines(Canvas canvas, float border, float horstart,
			float height, float graphwidth, Align verticalAlign) {
		paint.setColor(graphViewStyle.getGridColor());
		paint.setStrokeWidth(0);
		if (graphViewStyle.getGridStyle() != GridStyle.VERTICAL) {
			if (verticalAlign == Align.RIGHT) {
				canvas.drawLine(graphwidth + horstart, height - border, graphwidth + horstart,
						border, paint);
			} else {
				canvas.drawLine(horstart, height - border, horstart, border, paint);
			}

		}
	}

	/**
	 * 6.1deprecated-----legend align bottom-----
	 * 
	 * @param canvas
	 * @param height
	 * @param width
	 */
	@Deprecated
	protected void drawLegend(Canvas canvas, float height, float width) {
		float textSize = paint.getTextSize();
		int spacing = getGraphViewStyle().getLegendSpacing();
		int border = getGraphViewStyle().getLegendBorder();
		int legendWidth = getGraphViewStyle().getLegendWidth();

		int shapeSize = (int) (textSize * 0.8d);
		Log.d("GraphView", "draw legend size: " + paint.getTextSize());

		// rect
		paint.setARGB(180, 100, 100, 100);
		float legendHeight = (shapeSize + spacing) * graphSeries.size() + 2 * border - spacing;
		float lLeft = width - legendWidth - border * 2;
		float lTop;
		switch (legendAlign) {
		case TOP:
			lTop = 0;
			break;
		case MIDDLE:
			lTop = height / 2 - legendHeight / 2;
			break;
		default:
			lTop = height - getGraphViewStyle().getBorder() - legendHeight
					- getGraphViewStyle().getLegendMarginBottom();
		}
		float lRight = lLeft + legendWidth;
		float lBottom = lTop + legendHeight;
		canvas.drawRoundRect(new RectF(lLeft, lTop, lRight, lBottom), 8, 8, paint);

		for (int i = 0; i < graphSeries.size(); i++) {
			paint.setColor(graphSeries.get(i).style.color);
			canvas.drawRect(new RectF(lLeft + border, lTop + border + (i * (shapeSize + spacing)),
					lLeft + border + shapeSize, lTop + border + (i * (shapeSize + spacing))
							+ shapeSize), paint);
			if (graphSeries.get(i).description != null) {
				paint.setColor(Color.WHITE);
				paint.setTextAlign(Align.LEFT);
				canvas.drawText(graphSeries.get(i).description, lLeft + border + shapeSize
						+ spacing, lTop + border + shapeSize + (i * (shapeSize + spacing)), paint);
			}
		}
	}

	/**
	 * 6.1add-----align legend bottom.-----
	 * 
	 * @param canvas
	 * @param height
	 * @param width
	 * @param border
	 */
	protected void drawBottomLegend(Canvas canvas, float height, float width, float border) {
		int number = graphSeries.size();
		float textSize = paint.getTextSize();
		int spacing = getGraphViewStyle().getLegendSpacing();
		int legendBorder = getGraphViewStyle().getLegendBorder();
		// int legendWidth = getGraphViewStyle().getLegendWidth();

		int shapeSize = (int) (textSize * 0.8d);

		float top = height - border + textSize;//

		float textTotallength = 0;
		for (int i = 0; i < number; i++) {
			if (graphSeries.get(i).description != null) {
				float length = graphSeries.get(i).description.length() * textSize;
				textTotallength += length;
			}
		}
		// boolean hasSuite = false;
		Bitmap bitmap = null;
		for (int i = 0; i < number; i++) {
			if (null != graphSeries.get(i).bitmap && 0 != graphSeries.get(i).suiteY) {
				// hasSuite = true;
				bitmap = graphSeries.get(i).bitmap;
			}
		}
		float left;
		if (getGraphViewStyle().getShowSuiteLegend()) {
			left = (width - (number + 1) * (shapeSize + legendBorder) - number * spacing
					- textTotallength - 3 * textSize) / 2f;
		} else {
			left = (width - number * (shapeSize + legendBorder) - (number - 1) * spacing - textTotallength) / 2f;
		}
		for (int i = 0; i < number; i++) {
			if (graphSeries.get(i).description != null) {

				paint.setColor(graphSeries.get(i).style.color);
				paint.setTextAlign(Align.LEFT);
				float textLength = 0;
				if (i > 0) {
					for (int j = 0; j < i; j++) {
						if (graphSeries.get(j).description != null) {
							textLength += graphSeries.get(j).description.length() * textSize;
						}
					}
				}
				float _left = left + i * (shapeSize + legendBorder + spacing) + textLength;
				float _top = top + textSize;
				float _right = _left + shapeSize;
				float _bottom = _top + shapeSize;
				canvas.drawRect(new RectF(_left, _top, _right, _bottom), paint);

				canvas.drawText(graphSeries.get(i).description, _left + shapeSize + legendBorder,
						_bottom, paint);
			}
		}

		if (getGraphViewStyle().getShowSuiteLegend() && null != bitmap) {
			float _left = left + number * (shapeSize + legendBorder + spacing) + textTotallength;
			float _top = top + textSize;
			float _bottom = _top + shapeSize;
			paint.setColor(Color.rgb(78, 149, 239));
			canvas.drawBitmap(bitmap, _left + shapeSize / 2f - bitmap.getWidth() / 2f, _top
					+ shapeSize / 2f - bitmap.getHeight() / 2f, paint);

			canvas.drawText(suiteText, _left + shapeSize + legendBorder, _bottom, paint);
		}
	}

	abstract protected void drawSeries(Canvas canvas, GraphViewDataInterface[] values,
			float graphwidth, float graphheight, float border, double minX, double minY,
			double diffX, double diffY, float horstart, double viewportSize, boolean scalable,
			GraphViewSeries series, double _maxX);

	/**
	 * formats the label use #setCustomLabelFormatter or static labels if you
	 * want custom labels
	 * 
	 * @param value
	 *            x and y values
	 * @param isValueX
	 *            if false, value y wants to be formatted
	 * @deprecated use {@link #setCustomLabelFormatter(CustomLabelFormatter)}
	 * @return value to display
	 */
	@Deprecated
	protected String formatLabel(double value, boolean isValueX) {
		if (customLabelFormatter != null) {
			String label = customLabelFormatter.formatLabel(value, isValueX);
			if (label != null) {
				return label;
			}
		}
		int i = isValueX ? 1 : 0;
		if (numberformatter[i] == null) {
			numberformatter[i] = NumberFormat.getNumberInstance();
			double highestvalue = isValueX ? getMaxX(false) : getMaxY();
			double lowestvalue = isValueX ? getMinX(false) : getMinY();
			if (highestvalue - lowestvalue < 0.1) {
				numberformatter[i].setMaximumFractionDigits(6);
			} else if (highestvalue - lowestvalue < 1) {
				numberformatter[i].setMaximumFractionDigits(4);
			} else if (highestvalue - lowestvalue < 20) {
				numberformatter[i].setMaximumFractionDigits(3);
			} else if (highestvalue - lowestvalue < 100) {
				numberformatter[i].setMaximumFractionDigits(1);
			} else {
				numberformatter[i].setMaximumFractionDigits(0);
			}
		}
		return numberformatter[i].format(value);
	}

	private String[] generateHorlabels(float graphwidth) {
		int numLabels = getGraphViewStyle().getNumHorizontalLabels() - 1;
		if (numLabels < 0) {
			if (graphwidth <= 0)
				graphwidth = 1f;
			numLabels = (int) (graphwidth / (horLabelTextWidth * 2));
		}

		String[] labels = new String[numLabels + 1];
		double min = getMinX(false);
		double max = getMaxX(false);
		for (int i = 0; i <= numLabels; i++) {
			labels[i] = formatLabel(min + ((max - min) * i / numLabels), true);
		}
		return labels;
	}

	double oldMaxY = 1000;
	double oldMinY = 100000;

	/**
	 * 6.1change-----control graph align------
	 * 
	 * @param graphheight
	 * @return
	 */
	synchronized private String[] generateVerlabels(float graphheight) {
		int numLabels = getGraphViewStyle().getNumVerticalLabels() - 1;
		if (numLabels < 0) {
			if (graphheight <= 0)
				graphheight = 1f;
			numLabels = (int) (graphheight / (labelTextHeight * 3));
			if (numLabels == 0) {
				Log.w("GraphView",
						"Height of Graph is smaller than the label text height, so no vertical labels were shown!");
			}
		}
		String[] labels = new String[numLabels + 1];
		double min = getMinY();
		double max = getMaxY();
		double diffY = max - min;

		switch (getGraphViewStyle().getGraphAlign()) {
		// centerInScreen
		case 0:
			min -= diffY;
			max += diffY + 1000;
			break;
		// halfScreen
		case 1:
			min -= diffY / 2d;
			max += diffY / 2d + 1000;
			break;
		// fullScreen
		case 2:
			min -= 1000;
			max += 1000;
			break;

		default:
			break;
		}

		if (max == min) {
			// if min/max is the same, fake it so that we can render a line
			if (max == 0) {
				// if both are zero, change the values to prevent division by
				// zero
				max = 1.0d;
				min = 0.0d;
			} else {
				max = max * 1.05d;
				min = min * 0.95d;
			}
		}
		int maxY = (((int) max) / 1000) * 1000;
		int minY = (((int) min) / 1000) * 1000;

		// int quot = (int) (maxY - minY) / (int) (numLabels * 1000);
		// if (quot > 0) {
		// int addvalue = (quot + 1) * numLabels * 1000 - (int) (maxY - minY);
		// int averageQuot = addvalue / 2000;
		//
		// minY -= averageQuot * 1000;
		// maxY += addvalue - averageQuot * 1000;
		// }

		double _oldMaxY = oldMaxY;
		double _oldMinY = oldMinY;

		if (maxY > _oldMaxY) {
			_oldMaxY = maxY;
		}

		if (minY < _oldMinY) {
			if (minY > 0) {
				_oldMinY = minY;
			} else {
				_oldMinY = 0;
			}
		}

		int quot = (int) (_oldMaxY - _oldMinY) / (int) (numLabels * 1000);
		int mod = (int) (_oldMaxY - _oldMinY) % (int) (numLabels * 1000);
		if (quot > 0 && mod != 0) {
			int addvalue = (quot + 1) * numLabels * 1000 - (int) (_oldMaxY - _oldMinY);
			int averageQuot = addvalue / 2000;

			if (_oldMinY > 0) {
				_oldMinY -= averageQuot * 1000;
				_oldMaxY += addvalue - averageQuot * 1000;
			} else {
				_oldMaxY += addvalue;
			}
		}

		if (_oldMaxY > oldMaxY) {
			oldMaxY = _oldMaxY;
		}

		if (_oldMinY < oldMinY) {
			if (_oldMinY > 0) {
				oldMinY = _oldMinY;
			} else {
				oldMinY = 0;
			}
		}

		for (int i = 0; i <= numLabels; i++) {
			labels[numLabels - i] = formatLabel(oldMinY + ((oldMaxY - oldMinY) * i / numLabels),
					false);
		}

		return labels;
	}

	/**
	 * @return the custom label formatter, if there is one. otherwise null
	 */
	public CustomLabelFormatter getCustomLabelFormatter() {
		return customLabelFormatter;
	}

	/**
	 * @return the graphview style. it will never be null.
	 */
	public GraphViewStyle getGraphViewStyle() {
		return graphViewStyle;
	}

	/**
	 * get the position of the legend
	 * 
	 * @return
	 */
	public LegendAlign getLegendAlign() {
		return legendAlign;
	}

	/**
	 * @return legend width
	 * @deprecated use {@link GraphViewStyle#getLegendWidth()}
	 */
	@Deprecated
	public float getLegendWidth() {
		return getGraphViewStyle().getLegendWidth();
	}

	/**
	 * returns the maximal X value of the current viewport (if viewport is set)
	 * otherwise maximal X value of all data.
	 * 
	 * @param ignoreViewport
	 * 
	 *            warning: only override this, if you really know want you're
	 *            doing!
	 */
	protected double getMaxX(boolean ignoreViewport) {
		// if viewport is set, use this
		if (!ignoreViewport && viewportSize != 0) {
			return viewportStart + viewportSize;
		} else {
			// otherwise use the max x value
			// values must be sorted by x, so the last value has the largest X
			// value
			double highest = 0;
			if (graphSeries.size() > 0) {
				GraphViewDataInterface[] values = graphSeries.get(0).values;
				if (values.length == 0) {
					highest = 0;
				} else {
					highest = values[values.length - 1].getX().getKey();
				}
				for (int i = 1; i < graphSeries.size(); i++) {
					values = graphSeries.get(i).values;
					if (values.length > 0) {
						highest = Math.max(highest, values[values.length - 1].getX().getKey());
					}
				}
			}
			return highest;
		}
	}

	/**
	 * returns the maximal Y value of all data.
	 * 
	 * warning: only override this, if you really know want you're doing!
	 */
	protected double getMaxY() {
		double largest;
		if (manualYAxis || manualMaxY) {
			largest = manualMaxYValue;
		} else {
			largest = Integer.MIN_VALUE;
			for (int i = 0; i < graphSeries.size(); i++) {
				GraphViewDataInterface[] values = _values(i);
				for (int ii = 0; ii < values.length; ii++)
					if (values[ii].getY() > largest)
						largest = values[ii].getY();
				if (null != graphSeries && null != graphSeries.get(i).bitmap
						&& 0 != graphSeries.get(i).suiteY) {
					if (graphSeries.get(i).suiteY > largest)
						largest = graphSeries.get(i).suiteY;
				}
			}
		}

		return largest;
	}

	/**
	 * returns the minimal X value of the current viewport (if viewport is set)
	 * otherwise minimal X value of all data.
	 * 
	 * @param ignoreViewport
	 * 
	 *            warning: only override this, if you really know want you're
	 *            doing!
	 */
	protected double getMinX(boolean ignoreViewport) {
		// if viewport is set, use this
		if (!ignoreViewport && viewportSize != 0) {
			return viewportStart;
		} else {
			// otherwise use the min x value
			// values must be sorted by x, so the first value has the smallest X
			// value
			double lowest = 0;
			if (graphSeries.size() > 0) {
				GraphViewDataInterface[] values = graphSeries.get(0).values;
				if (values.length == 0) {
					lowest = 0;
				} else {
					lowest = values[0].getX().getKey();
				}
				for (int i = 1; i < graphSeries.size(); i++) {
					values = graphSeries.get(i).values;
					if (values.length > 0) {
						lowest = Math.min(lowest, values[0].getX().getKey());
					}
				}
			}
			return lowest;
		}
	}

	/**
	 * returns the minimal Y value of all data.
	 * 
	 * warning: only override this, if you really know want you're doing!
	 */
	protected double getMinY() {
		double smallest;
		if (manualYAxis || manualMinY) {
			smallest = manualMinYValue;
		} else {
			smallest = Integer.MAX_VALUE;
			for (int i = 0; i < graphSeries.size(); i++) {
				GraphViewDataInterface[] values = _values(i);
				for (int ii = 0; ii < values.length; ii++)
					if (values[ii].getY() < smallest)
						smallest = values[ii].getY();
				if (null != graphSeries && null != graphSeries.get(i).bitmap
						&& 0 != graphSeries.get(i).suiteY) {
					if (graphSeries.get(i).suiteY < smallest)
						smallest = graphSeries.get(i).suiteY;
				}
			}
		}

		return smallest;
	}

	/**
	 * returns the size of the Viewport
	 * 
	 */
	public double getViewportSize() {
		return viewportSize;
	}

	public double getViewPortStart() {
		return viewportStart;
	}

	public boolean isDisableTouch() {
		return disableTouch;
	}

	public boolean isScrollable() {
		return scrollable;
	}

	public boolean isShowLegend() {
		return showLegend;
	}

	/**
	 * forces graphview to invalide all views and caches. Normally there is no
	 * need to call this manually.
	 */
	public void redrawAll() {
		if (!staticVerticalLabels)
			verlabels = null;
		if (!staticHorizontalLabels)
			horlabels = null;
		numberformatter[0] = null;
		numberformatter[1] = null;
		labelTextHeight = null;
		horLabelTextWidth = null;
		verLabelTextWidth = null;

		invalidate();
		viewVerLabels.invalidate();
		graphViewContentView.invalidate();
	}

	/**
	 * removes all series
	 */
	public void removeAllSeries() {
		for (GraphViewSeries s : graphSeries) {
			s.removeGraphView(this);
		}
		while (!graphSeries.isEmpty()) {
			graphSeries.remove(0);
		}
		redrawAll();
	}

	/**
	 * removes a series
	 * 
	 * @param series
	 *            series to remove
	 */
	public void removeSeries(GraphViewSeries series) {
		series.removeGraphView(this);
		graphSeries.remove(series);
		redrawAll();
	}

	/**
	 * removes series
	 * 
	 * @param index
	 */
	public void removeSeries(int index) {
		if (index < 0 || index >= graphSeries.size()) {
			throw new IndexOutOfBoundsException("No series at index " + index);
		}

		removeSeries(graphSeries.get(index));
	}

	/**
	 * scrolls to the last x-value
	 * 
	 * @throws IllegalStateException
	 *             if scrollable == false
	 */
	public void scrollToEnd() {
		if (!scrollable)
			throw new IllegalStateException("This GraphView is not scrollable.");
		double max = getMaxX(true);
		viewportStart = max - viewportSize;

		// don't clear labels width/height cache
		// so that the display is not flickering
		if (!staticVerticalLabels)
			verlabels = null;
		if (!staticHorizontalLabels)
			horlabels = null;

		invalidate();
		viewVerLabels.invalidate();
		graphViewContentView.invalidate();
	}

	/**
	 * set a custom label formatter
	 * 
	 * @param customLabelFormatter
	 */
	public void setCustomLabelFormatter(CustomLabelFormatter customLabelFormatter) {
		this.customLabelFormatter = customLabelFormatter;
	}

	/**
	 * The user can disable any touch gestures, this is useful if you are using
	 * a real time graph, but don't want the user to interact
	 * 
	 * @param disableTouch
	 */
	public void setDisableTouch(boolean disableTouch) {
		this.disableTouch = disableTouch;
	}

	/**
	 * set custom graphview style
	 * 
	 * @param style
	 */
	public void setGraphViewStyle(GraphViewStyle style) {
		graphViewStyle = style;
		labelTextHeight = null;
	}

	/**
	 * 6.1deprecated-----set's static horizontal labels (from left to right)
	 * 
	 * @param horlabels
	 *            if null, labels were generated automatically
	 */
	@Deprecated
	public void setHorizontalLabels(String[] horlabels) {
		staticHorizontalLabels = horlabels != null;
		this.horlabels = horlabels;
	}

	/**
	 * legend position
	 * 
	 * @param legendAlign
	 */
	public void setLegendAlign(LegendAlign legendAlign) {
		this.legendAlign = legendAlign;
	}

	/**
	 * legend width
	 * 
	 * @param legendWidth
	 * @deprecated use {@link GraphViewStyle#setLegendWidth(int)}
	 */
	@Deprecated
	public void setLegendWidth(float legendWidth) {
		getGraphViewStyle().setLegendWidth((int) legendWidth);
	}

	/**
	 * you have to set the bounds {@link #setManualYAxisBounds(double, double)}.
	 * That automatically enables manualYAxis-flag. if you want to disable the
	 * menual y axis, call this method with false.
	 * 
	 * @param manualYAxis
	 */
	public void setManualYAxis(boolean manualYAxis) {
		this.manualYAxis = manualYAxis;
	}

	/**
	 * if you want to disable the menual y axis maximum bound, call this method
	 * with false.
	 */
	public void setManualMaxY(boolean manualMaxY) {
		this.manualMaxY = manualMaxY;
	}

	/**
	 * if you want to disable the menual y axis minimum bound, call this method
	 * with false.
	 */
	public void setManualMinY(boolean manualMinY) {
		this.manualMinY = manualMinY;
	}

	/**
	 * set manual Y axis limit
	 * 
	 * @param max
	 * @param min
	 */
	public void setManualYAxisBounds(double max, double min) {
		manualMaxYValue = max;
		manualMinYValue = min;
		manualYAxis = true;
	}

	/*
	 * set manual Y axis max limit
	 * 
	 * @param max
	 */
	public void setManualYMaxBound(double max) {
		manualMaxYValue = max;
		manualMaxY = true;
	}

	/*
	 * set manual Y axis min limit
	 * 
	 * @param min
	 */
	public void setManualYMinBound(double min) {
		manualMinYValue = min;
		manualMinY = true;
	}

	/**
	 * this forces scrollable = true
	 * 
	 * @param scalable
	 */
	synchronized public void setScalable(boolean scalable) {
		this.scalable = scalable;
		if (scalable == true && scaleDetector == null) {
			scrollable = true; // automatically forces this
			scaleDetector = new ScaleGestureDetector(getContext(),
					new ScaleGestureDetector.SimpleOnScaleGestureListener() {
						@Override
						public boolean onScale(ScaleGestureDetector detector) {
							double center = viewportStart + viewportSize / 2;
							viewportSize /= detector.getScaleFactor();
							viewportStart = center - viewportSize / 2;

							// viewportStart must not be < minX
							double minX = getMinX(true);
							if (viewportStart < minX) {
								viewportStart = minX;
							}

							// viewportStart + viewportSize must not be > maxX
							double maxX = getMaxX(true);
							if (viewportSize == 0) {
								viewportSize = maxX;
							}
							double overlap = viewportStart + viewportSize - maxX;
							if (overlap > 0) {
								// scroll left
								if (viewportStart - overlap > minX) {
									viewportStart -= overlap;
								} else {
									// maximal scale
									viewportStart = minX;
									viewportSize = maxX - viewportStart;
								}
							}
							redrawAll();
							return true;
						}
					});
		}
	}

	/**
	 * the user can scroll (horizontal) the graph. This is only useful if you
	 * use a viewport {@link #setViewPort(double, double)} which doesn't
	 * displays all data.
	 * 
	 * @param scrollable
	 */
	public void setScrollable(boolean scrollable) {
		this.scrollable = scrollable;
	}

	public void setShowLegend(boolean showLegend) {
		this.showLegend = showLegend;
	}

	public void setSuiteText(String suiteText) {
		this.suiteText = suiteText;
	}

	/**
	 * sets the title of graphview
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * set's static vertical labels (from top to bottom)
	 * 
	 * @param verlabels
	 *            if null, labels were generated automatically
	 */
	public void setVerticalLabels(String[] verlabels) {
		staticVerticalLabels = verlabels != null;
		this.verlabels = verlabels;
	}

	/**
	 * set's the viewport for the graph.
	 * 
	 * @see #setManualYAxisBounds(double, double) to limit the y-viewport
	 * @param start
	 *            x-value
	 * @param size
	 */
	public void setViewPort(double start, double size) {
		if (size < 0) {
			throw new IllegalArgumentException("Viewport size must be greater than 0!");
		}
		viewportStart = start;
		viewportSize = size;
	}

	/**
	 * Sets whether horizontal labels are drawn or not.
	 * 
	 * @param showHorizontalLabels
	 */
	public void setShowHorizontalLabels(boolean showHorizontalLabels) {
		this.showHorizontalLabels = showHorizontalLabels;
		redrawAll();
	}

	/**
	 * Gets are horizontal labels drawn.
	 * 
	 * @return {@code True} if horizontal labels are drawn
	 */
	public boolean getShowHorizontalLabels() {
		return showHorizontalLabels;
	}

	/**
	 * Sets whether vertical labels are drawn or not.
	 * 
	 * @param showVerticalLabels
	 */
	public void setShowVerticalLabels(boolean showVerticalLabels) {
		this.showVerticalLabels = showVerticalLabels;
		if (this.showVerticalLabels) {
			addView(viewVerLabels, 0);
		} else {
			removeView(viewVerLabels);
		}
	}

	/**
	 * Gets are vertical labels are drawn.
	 * 
	 * @return {@code True} if vertical labels are drawn
	 */
	public boolean getShowVerticalLabels() {
		return showVerticalLabels;
	}

}
