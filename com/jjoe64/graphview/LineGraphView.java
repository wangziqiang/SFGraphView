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

import java.text.DecimalFormat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Align;
import android.graphics.PathEffect;
import android.util.AttributeSet;

import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.GraphViewStyle.GridStyle;

/**
 * Line Graph View. This draws a line chart.
 */
public class LineGraphView extends GraphView {
	private boolean drawValuesOnTop;
	private final Paint paintBackground;
	private Paint pointLabelPaint;
	private Paint verLinePaint;
	private Paint horLabelPaint;
	private boolean drawBackground;
	private boolean drawDataPoints;
	private float dataPointsRadius = 10f;
	private int valuesOnTopColor = Color.WHITE;
	private double valuesOnTopBaseNum = 0;
	private double valuesOnTopUnitNum = 1;
	private String valuesOnTopUnitName;
	private String valuesOnTopDecimalFormat = "0";
	private float valuesOnTopTextSize = 30f;
	private Paint trendPaint;

	public LineGraphView(Context context, AttributeSet attrs) {
		super(context, attrs);

		paintBackground = new Paint();
		paintBackground.setColor(Color.rgb(20, 40, 60));
		paintBackground.setStrokeWidth(4);
		paintBackground.setAlpha(128);

		pointLabelPaint = new Paint();
		verLinePaint = new Paint();
		horLabelPaint = new Paint();
		trendPaint = new Paint();
	}

	// 6.1add-----add parameter verticalAlign because of GraphView has been
	// added.-----
	public LineGraphView(Context context, String title, Align verticalAlign) {
		super(context, title, verticalAlign);

		paintBackground = new Paint();
		paintBackground.setColor(Color.rgb(20, 40, 60));
		paintBackground.setStrokeWidth(4);
		paintBackground.setAlpha(128);

		pointLabelPaint = new Paint();
		verLinePaint = new Paint();
		horLabelPaint = new Paint();
		trendPaint = new Paint();
	}

	@Override
	public void drawSeries(Canvas canvas, GraphViewDataInterface[] values, float graphwidth,
			float graphheight, float border, double minX, double minY, double diffX, double diffY,
			float horstart, double viewportSize, boolean scalable, GraphViewSeries series,
			double _maxX) {
		//
		GraphViewSeriesStyle style = series.style;
		Bitmap bitmap = series.bitmap;
		double suiteY = series.suiteY;

		// reset graphwidth.
		if (diffX == 0) {
			diffX = 1;
		}
		graphwidth = graphwidth - 1 / (2 * (float) diffX) * graphwidth;

		// draw background
		double lastEndY = 0;
		double lastEndX = 0;
		//
		float pointLabelX = 0;
		float pointLabelY = 0;

		// draw data
		paint.setStrokeWidth(style.thickness);
		paint.setColor(style.color);
		paint.setAlpha(style.alpha);

		Path bgPath = null;
		if (drawBackground) {
			bgPath = new Path();
		}

		lastEndY = 0;
		lastEndX = 0;
		float firstX = 0;
		for (int i = 0; i < values.length; i++) {
			double valY = values[i].getY() - minY;
			double ratY = valY / diffY;
			double y = graphheight * ratY;
			double valX = values[i].getX().getKey() - minX;
			double ratX = valX / diffX;
			double x = graphwidth * ratX;

			if (i > 0) {
				float startX = (float) lastEndX + (horstart + 1);
				float startY = (float) (border - lastEndY) + graphheight;
				float endX = (float) x + (horstart + 1);
				float endY = (float) (border - y) + graphheight;

				if (values[i].getX().getValue().contains("(预)")) {
					// if (values[i].getY() > values[i - 1].getY()) {
					// trendPaint.setColor(Color.RED);
					// } else {
					// trendPaint.setColor(Color.GREEN);
					// }
					trendPaint.setColor(style.color);
					trendPaint.setStrokeWidth(style.thickness);
					trendPaint.setAntiAlias(true);
					trendPaint.setFilterBitmap(true);
					// draw data point
					if (drawDataPoints) {
						// fix: last value was not drawn. Draw here now the end
						// values
						trendPaint.setPathEffect(null);
						trendPaint.setStyle(Paint.Style.FILL);
						canvas.drawCircle(endX, endY, dataPointsRadius, trendPaint);
					}
					// draw suite.
					if (null != bitmap && 0 != suiteY) {
						double _valY = suiteY - minY;
						double _ratY = _valY / diffY;
						double _y = graphheight * _ratY;
						float _endY = (float) (border - _y) + graphheight;
						canvas.drawBitmap(bitmap,
								endX - (float) (graphwidth / diffX) - bitmap.getWidth() / 2f, _endY
										- bitmap.getHeight() / 2f, trendPaint);
					}
					trendPaint.setStyle(Paint.Style.STROKE);
					PathEffect effects = new DashPathEffect(new float[] { 10, 10, 10, 10 }, 1);
					trendPaint.setPathEffect(effects);
					Path path = new Path();
					path.moveTo(startX, startY);
					path.lineTo(endX, endY);

					canvas.drawPath(path, trendPaint);
					// canvas.drawLine(startX, startY, endX, endY, paint);
				} else {
					// draw data point
					if (drawDataPoints) {
						// fix: last value was not drawn. Draw here now the end
						// values
						canvas.drawCircle(endX, endY, dataPointsRadius, paint);
					}

					canvas.drawLine(startX, startY, endX, endY, paint);

					if (values[i].getX().getKey() == _maxX) {
						// draw suite.
						if (null != bitmap && 0 != suiteY) {
							double _valY = suiteY - minY;
							double _ratY = _valY / diffY;
							double _y = graphheight * _ratY;
							float _endY = (float) (border - _y) + graphheight;
							canvas.drawBitmap(bitmap, endX - bitmap.getWidth() / 2f,
									_endY - bitmap.getHeight() / 2f, paint);
						}
					}
				}
				if (bgPath != null) {
					if (i == 1) {
						firstX = startX;
						bgPath.moveTo(startX, startY);
					}
					bgPath.lineTo(endX, endY);
				}
				//
				pointLabelX = endX;
				pointLabelY = endY;
			} else if (drawDataPoints) {
				// fix: last value not drawn as datapoint. Draw first point
				// here, and then on every step the end values (above)
				float first_X = 0;
				if (0 == viewportSize) {
					first_X = graphwidth;
				} else {
					first_X = (float) x + (horstart + 1);
				}
				float first_Y = (float) (border - y) + graphheight;
				canvas.drawCircle(first_X, first_Y, dataPointsRadius, paint);
				//
				pointLabelX = first_X;
				pointLabelY = first_Y;
			}

			// 6.1add-----draw removable vertical lines---------
			verLinePaint.setColor(graphViewStyle.getGridColor());
			verLinePaint.setStrokeWidth(0);
			if (graphViewStyle.getGridStyle() != GridStyle.VERTICAL) {
				canvas.drawLine(pointLabelX, graphheight + (2 * border) - border, pointLabelX,
						border, verLinePaint);
			}
			// 6.1add-----draw horizontal labels-----
			horLabelPaint.setTextAlign(Align.CENTER);
			if (values[i].getX().getValue().contains("(预)")) {
				canvas.drawText("预测", pointLabelX, border, horLabelPaint);
			}
			if (scalable) {
				// if (i == viewportSize + 1) {
				// horLabelPaint.setTextAlign(Align.RIGHT);
				// }
				// if (i == 0 && values[0].getX().getKey() == 0) {
				// horLabelPaint.setTextAlign(Align.LEFT);
				// }
			} else {
				// if (i == values.length - 1) {
				// horLabelPaint.setTextAlign(Align.RIGHT);
				// }
				// if (i == 0 && values[0].getX().getKey() == 0) {
				// horLabelPaint.setTextAlign(Align.LEFT);
				// }
			}
			horLabelPaint.setColor(graphViewStyle.getHorizontalLabelsColor());
			horLabelPaint.setTextSize(graphViewStyle.getTextSize());
			canvas.drawText(values[i].getX().getValue(), pointLabelX, graphheight + border
					+ graphViewStyle.getTextSize(), horLabelPaint);// horlabels[i]

			// 6.1add-----Set values on top of graph---------
			if (drawValuesOnTop) {
				pointLabelY -= dataPointsRadius + 4;
				if (pointLabelY <= border + valuesOnTopTextSize)
					pointLabelY += 2 * (dataPointsRadius + 4) + valuesOnTopTextSize;

				pointLabelPaint.setTextAlign(Align.CENTER);
				if (scalable) {
					if (i == viewportSize + 1) {
						pointLabelPaint.setTextAlign(Align.RIGHT);
					}
					if (i == 0 && values[0].getX().getKey() == 0) {
						pointLabelPaint.setTextAlign(Align.LEFT);
					}
				} else {
					if (i == values.length - 1) {
						pointLabelPaint.setTextAlign(Align.RIGHT);
					}
					if (i == 0) {
						pointLabelPaint.setTextAlign(Align.LEFT);
					}
				}

				pointLabelPaint.setColor(valuesOnTopColor);
				pointLabelPaint.setTextSize(valuesOnTopTextSize);

				DecimalFormat df = new DecimalFormat(valuesOnTopDecimalFormat);
				double value = valuesOnTopBaseNum + values[i].getY() / valuesOnTopUnitNum;
				canvas.drawText(df.format(value) + valuesOnTopUnitName, pointLabelX, pointLabelY,
						pointLabelPaint);
			}

			lastEndY = y;
			lastEndX = x;
		}

		if (bgPath != null) {
			// end / close path
			bgPath.lineTo((float) lastEndX, graphheight + border);
			bgPath.lineTo(firstX, graphheight + border);
			bgPath.close();
			canvas.drawPath(bgPath, paintBackground);
		}

	}

	public boolean getDrawValuesOnTop() {
		return drawValuesOnTop;
	}

	public int getValuesOnTopColor() {
		return valuesOnTopColor;
	}

	public float getValuesOnTopTextSize() {
		return valuesOnTopTextSize;
	}

	public double getValuesOnTopBaseNum() {
		return valuesOnTopBaseNum;
	}

	public double getValuesOnTopUnitNum() {
		return valuesOnTopUnitNum;
	}

	public String getValuesOnTopUnitName() {
		return valuesOnTopUnitName;
	}

	public String getValuesOnTopDecimalFormat() {
		return valuesOnTopDecimalFormat;
	}

	public int getBackgroundColor() {
		return paintBackground.getColor();
	}

	public float getDataPointsRadius() {
		return dataPointsRadius;
	}

	public boolean getDrawBackground() {
		return drawBackground;
	}

	public boolean getDrawDataPoints() {
		return drawDataPoints;
	}

	public void setDrawValuesOnTop(boolean drawValuesOnTop) {
		this.drawValuesOnTop = drawValuesOnTop;
	}

	public void setValuesOnTopColor(int valuesOnTopColor) {
		this.valuesOnTopColor = valuesOnTopColor;
	}

	public void setValuesOnTopTextSize(float valuesOnTopTextSize) {
		this.valuesOnTopTextSize = valuesOnTopTextSize;
	}

	public void setValuesOnTopBaseNum(double valuesOnTopBaseNum) {
		this.valuesOnTopBaseNum = valuesOnTopBaseNum;
	}

	public void setValuesOnTopUnitNum(double valuesOnTopUnitNum) {
		this.valuesOnTopUnitNum = valuesOnTopUnitNum;
	}

	public void setValuesOnTopUnitName(String valuesOnTopUnitName) {
		this.valuesOnTopUnitName = valuesOnTopUnitName;
	}

	public void setValuesOnTopDecimalFormat(String valuesOnTopDecimalFormat) {
		this.valuesOnTopDecimalFormat = valuesOnTopDecimalFormat;
	}

	/**
	 * sets the background color for the series. This is not the background
	 * color of the whole graph.
	 * 
	 * @see #setDrawBackground(boolean)
	 */
	@Override
	public void setBackgroundColor(int color) {
		paintBackground.setColor(color);
	}

	/**
	 * sets the radius of the circles at the data points.
	 * 
	 * @see #setDrawDataPoints(boolean)
	 * @param dataPointsRadius
	 */
	public void setDataPointsRadius(float dataPointsRadius) {
		this.dataPointsRadius = dataPointsRadius;
	}

	/**
	 * @param drawBackground
	 *            true for a light blue background under the graph line
	 * @see #setBackgroundColor(int)
	 */
	public void setDrawBackground(boolean drawBackground) {
		this.drawBackground = drawBackground;
	}

	/**
	 * You can set the flag to let the GraphView draw circles at the data points
	 * 
	 * @see #setDataPointsRadius(float)
	 * @param drawDataPoints
	 */
	public void setDrawDataPoints(boolean drawDataPoints) {
		this.drawDataPoints = drawDataPoints;
	}

}
