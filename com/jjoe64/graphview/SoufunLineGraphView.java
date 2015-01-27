package com.jjoe64.graphview;

import java.util.ArrayList;

import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * custom graph view.
 * 
 * @author renxin.sun
 * 
 */
public class SoufunLineGraphView extends LinearLayout {

	private Context context;

	private ViewGroup mContainer;

	int widthPixels;

	private GraphView graphView;

	private LegendAlign align = LegendAlign.MIDDLE;

	private boolean showLegend = true;

	private String suiteText = "本房源";

	boolean scrollable = false;

	private GraphViewStyle style;

	boolean drawDataPoints;

	private boolean drawValuesOnTop;

	private int valuesOnTopColor;

	private double valuesOnTopBaseNum = 0;

	private double valuesOnTopUnitNum = 1.0;

	private String valuesOnTopDecimalFormat = "0";

	private String valuesOnTopUnitName = "";

	public static final float DEFAULT_TEXTSIZE = 30f;

	public static final int DEFAULT_LEGENDBORDER = 20;

	public static final int DEFAULT_LEGENDSPACING = 40;

	public static final int DEFAULT_DATAPOINTSRADIUS = 10;

	public static final float DEFAULT_VALUESONTOPTEXTSIZE = 30f;

	public static final int DEFAULT_LINEOFTHICKNESS = 3;

	private int legendBorder = DEFAULT_LEGENDBORDER;

	private int legendSpacing = DEFAULT_LEGENDSPACING;

	private int lineOfThickness = DEFAULT_LINEOFTHICKNESS;

	private String graphViewTitle;

	private boolean showGraphViewTitle;

	private boolean showVerticalLabels;

	private String[] YAxisLabels;

	private double YAxisMax;

	private double YAxisMin;

	private float dataPointsRadius = DEFAULT_DATAPOINTSRADIUS;

	private float valuesOnTopTextSize = DEFAULT_VALUESONTOPTEXTSIZE;

	private double XAxisSize = 0;

	private double XAxisStart = 0;

	private float horVerTextSize = DEFAULT_TEXTSIZE;

	private Align verticalAlign = Align.LEFT;

	private Align verticalLabelsAlign = Align.LEFT;

	private ArrayList<GraphViewSeries> valueslist;

	protected boolean manualYAxisBounds;

	protected int YAxisLabelsNum;

	private boolean isDrawHoriLines;

	private String verticalUnit = "元/㎡";

	private OnGraphScrolledListener onGraphScrolledListener;

	private int valuesMaxLength = 0;

	private boolean showSuiteLegend;

	public SoufunLineGraphView(Context context) {
		super(context);
		this.context = context;
		style = new GraphViewStyle();

		valueslist = new ArrayList<GraphViewSeries>();

		DisplayMetrics displayMetrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		widthPixels = displayMetrics.widthPixels;

		// Init child view
		LinearLayout container = new LinearLayout(context);
		container.setOrientation(LinearLayout.VERTICAL);
		container.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		mContainer = container;
		addView(mContainer);
	}

	public SoufunLineGraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		style = new GraphViewStyle();

		valueslist = new ArrayList<GraphViewSeries>();

		DisplayMetrics displayMetrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		widthPixels = displayMetrics.widthPixels;
		Log.i("convertView", "widthPixels: " + widthPixels);

		// Init child view
		LinearLayout container = new LinearLayout(context);
		container.setOrientation(LinearLayout.VERTICAL);
		container.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		mContainer = container;
		addView(mContainer);
	}

	/**
	 * 实例化GraphView.应用已设置的特性。
	 */
	public void initializeGraphView() {
		if (null != graphView) {
			return;
		}
		// instantiated graphView.
		graphView = new LineGraphView(context, "", verticalAlign);

		graphView.setOnScrolledListener(new OnScrolledListener() {
			@Override
			public void OnScrolled() {
				if (null != onGraphScrolledListener) {
					onGraphScrolledListener.OnGraphScrolled();
				}
			}
		});

		//
		style.setTextSize(widthPixels / 1080f * horVerTextSize);
		style.setDistance(widthPixels / 1080f * (-30f));
		style.setVerticalUnit(verticalUnit);
		if (isDrawHoriLines) {
			style.setIsDrawHoriLines(true);
		}

		// set Legend.
		graphView.setShowLegend(showLegend);
		graphView.setLegendAlign(align);
		graphView.setSuiteText(suiteText);
		style.setLegendBorder((int) (widthPixels / 1080f * legendBorder));
		style.setLegendSpacing((int) (widthPixels / 1080f * legendSpacing));
		// style.setLegendWidth((int) (0.07 * widthPixels + legendText
		// .length() * style.getTextSize()));

		// set graphView title.
		if (showGraphViewTitle) {
			graphView.setTitle(graphViewTitle);
			// set border.
			style.setBorder((int) ((style.getTitleHeightFactor() + 1) * style.getTextSize() + style
					.getTitleTextSize()));
		} else {
			// set border.
			style.setBorder((int) ((style.getTitleHeightFactor() + 1) * style.getTextSize()));
		}

		// set verticalLabels.
		if (showVerticalLabels) {
			graphView.setVerticalLabels(YAxisLabels);
			style.setVerticalLabelsAlign(verticalLabelsAlign);

			int labelLength = 0;
			for (int i = 0; i < YAxisLabels.length; i++) {
				if (YAxisLabels[i] != null) {
					int length = YAxisLabels[i].length();
					if (length > labelLength) {
						labelLength = length;
					}
				}
			}

			style.setVerticalLabelsWidth((int) (labelLength / 1.3 * style.getTextSize()));
			style.setNumVerticalLabels(YAxisLabels.length + 1);
			graphView.setManualYAxisBounds(YAxisMax, YAxisMin);

		} else {
			style.setVerticalLabelsWidth((int) (5 / 1.3 * style.getTextSize()));
			style.setNumVerticalLabels(6);
		}

		// set graph view align.0:centerInScreen;1:halfScreen;2:fullScreen
		style.setGraphAlign(2);
		style.setShowSuiteLegend(showSuiteLegend);

		// add style to graphView.
		graphView.setGraphViewStyle(style);

		// set draw data points.
		if (drawDataPoints) {
			((LineGraphView) graphView).setDrawDataPoints(true);
			((LineGraphView) graphView).setDataPointsRadius(widthPixels / 1080f * dataPointsRadius);
		}

		// set draw values on top.
		if (drawValuesOnTop) {
			((LineGraphView) graphView).setDrawValuesOnTop(true);
			((LineGraphView) graphView).setValuesOnTopColor(valuesOnTopColor);
			((LineGraphView) graphView).setValuesOnTopTextSize(widthPixels / 1080f
					* valuesOnTopTextSize);
			((LineGraphView) graphView).setValuesOnTopBaseNum(valuesOnTopBaseNum);
			((LineGraphView) graphView).setValuesOnTopUnitNum(valuesOnTopUnitNum);
			((LineGraphView) graphView).setValuesOnTopDecimalFormat(valuesOnTopDecimalFormat);
			((LineGraphView) graphView).setValuesOnTopUnitName(valuesOnTopUnitName);

		}

		// set graphView scroll.
		if (scrollable) {
			graphView.setViewPort(XAxisStart, XAxisSize);
			graphView.setScalable(scrollable);
		}

		mContainer.addView(graphView);
	}

	/**
	 * 6.1add 有动态绘制曲线的动画。
	 * 
	 * @param lineColor
	 *            曲线颜色值。
	 * @param legendText
	 *            曲线图例的内容。
	 * @param values
	 *            点集。
	 * @return
	 */
	public GraphViewSeries addSeries(int lineColor, String legendText, GraphViewData[] values) {
		return addSeries(lineColor, legendText, values, null, 0, true);
	}

	/**
	 * 
	 * @param lineColor
	 *            曲线颜色值。
	 * @param legendText
	 *            曲线图例内容。
	 * @param values
	 *            点集。
	 * @param isDrawingLine
	 *            是否启动动态绘制曲线动画。
	 * @return
	 */
	public GraphViewSeries addSeries(int lineColor, String legendText, GraphViewData[] values,
			boolean isDrawingLine) {
		return addSeries(lineColor, legendText, values, null, 0, isDrawingLine);
	}

	/**
	 * 
	 * @param lineColor
	 *            曲线颜色值。
	 * @param legendText
	 *            曲线图例内容
	 * @param values
	 *            点集。
	 * @param bitmap
	 *            图片点。
	 * @param suiteY
	 *            图片点纵坐标，横坐标为横坐标最大值。
	 * @param isDrawingLine
	 *            是否启动动态绘制曲线动画。
	 * @return
	 */
	public GraphViewSeries addSeries(int lineColor, String legendText, GraphViewData[] values,
			Bitmap bitmap, double suiteY, boolean isDrawingLine) {

		if (null != graphView && values != null) {
			//
			GraphViewSeries series = new GraphViewSeries(legendText, new GraphViewSeriesStyle(
					lineColor, (int) (widthPixels / 1080f * lineOfThickness)), values, bitmap,
					suiteY);
			graphView.addSeries(series, isDrawingLine);
			return series;
		}
		return null;
	}

	/**
	 * 有动态绘制曲线的动画。
	 * 
	 * @param lineColor
	 * @param lineAlpha
	 * @param legendText
	 * @param values
	 * @return
	 */
	public GraphViewSeries addSeries(int lineColor, int lineAlpha, String legendText,
			GraphViewData[] values) {
		return addSeries(lineColor, lineAlpha, legendText, values, null, 0, true);
	}

	/**
	 * 
	 * @param lineColor
	 *            曲线颜色值。
	 * @param lineAlpha
	 *            曲线通明度0-255;
	 * @param legendText
	 *            曲线图例的内容。
	 * @param values
	 *            点集。
	 * @param isDrawingLine
	 *            是否启动动态绘制曲线的动画。
	 * @return
	 */
	public GraphViewSeries addSeries(int lineColor, int lineAlpha, String legendText,
			GraphViewData[] values, boolean isDrawingLine) {
		return addSeries(lineColor, lineAlpha, legendText, values, null, 0, isDrawingLine);
	}

	/**
	 * 添加曲线。
	 * 
	 * @param lineColor
	 *            曲线颜色值。
	 * @param lineAlpha
	 *            曲线通明度。0-255.
	 * @param legendText
	 *            图裂显示的内容。
	 * @param values
	 *            点集。
	 * @param bitmap
	 *            图片。将被绘制在横坐标最后一个值的位置。(排除预测值)
	 * @param suiteY
	 *            图片的纵坐标值。横坐标为横坐标最大值。
	 * @param isDrawingLine
	 *            是否启动动态绘制曲线动画。
	 * @return
	 */
	public GraphViewSeries addSeries(int lineColor, int lineAlpha, String legendText,
			GraphViewData[] values, Bitmap bitmap, double suiteY, boolean isDrawingLine) {
		if (null != graphView && values != null) {
			//
			GraphViewSeries series = new GraphViewSeries(legendText, new GraphViewSeriesStyle(
					lineColor, lineAlpha, (int) (widthPixels / 1080f * lineOfThickness)), values,
					bitmap, suiteY);
			graphView.addSeries(series, isDrawingLine);
			return series;
		}
		return null;
	}

	/**
	 * 6.2add 有动态绘制曲线动画。
	 * 
	 * @param lineColor
	 *            曲线颜色值。
	 * @param legendText
	 *            曲线图例的内容。
	 * @param values
	 *            点集。
	 * @param trendX
	 *            预测点x坐标。
	 * @param trendY
	 *            预测点y坐标。
	 * @return if trendX is null or empty, or trendY less than zero, it will be
	 *         regarded as normal series without predicted data.
	 */
	public GraphViewSeries addSeries(int lineColor, String legendText, GraphViewData[] values,
			String trendX, double trendY) {
		return addSeries(lineColor, legendText, values, trendX, trendY, null, 0, true);
	}

	/**
	 * 有动态绘制曲线动画。
	 * 
	 * @param lineColor
	 *            曲线颜色值。
	 * @param legendText
	 *            曲线图例的内容。
	 * @param values
	 *            点集。
	 * @param trendX
	 *            预测点x坐标。
	 * @param trendY
	 *            预测点y坐标。
	 * @param bitmap
	 *            图片点。
	 * @param suiteY
	 *            图片点纵坐标值。横坐标为横坐标最大值。
	 * @return if trendX is null or empty, or trendY less than zero, it will be
	 *         regarded as normal series without predicted data.
	 */
	public GraphViewSeries addSeries(int lineColor, String legendText, GraphViewData[] values,
			String trendX, double trendY, Bitmap bitmap, double suiteY) {
		return addSeries(lineColor, legendText, values, trendX, trendY, bitmap, suiteY, true);
	}

	/**
	 * 
	 * @param lineColor
	 *            曲线颜色值。
	 * @param legendText
	 *            曲线图例的内容。
	 * @param values
	 *            点集。
	 * @param trendX
	 *            预测点x坐标。
	 * @param trendY
	 *            预测点y坐标。
	 * @param bitmap
	 *            图片点。
	 * @param suiteY
	 *            图片点的纵坐标。横坐标为横坐标最大值。
	 * @param isDrawingLine
	 *            是否启动动态绘制曲线的动画。
	 * @return if trendX is null or empty, or trendY less than zero, it will be
	 *         regarded as normal series without predicted data.
	 */
	public GraphViewSeries addSeries(int lineColor, String legendText, GraphViewData[] values,
			String trendX, double trendY, Bitmap bitmap, double suiteY, boolean isDrawingLine) {
		if (null != graphView && values != null) {
			GraphViewSeries series;
			//
			if (trendX != null && !"".equals(trendX) && trendY > 0) {
				series = new GraphViewSeries(legendText, new GraphViewSeriesStyle(lineColor,
						(int) (widthPixels / 1080f * lineOfThickness)), values, new KeyValuePair(
						valuesMaxLength, trendX + "(预)"), trendY, bitmap, suiteY);
				graphView.setViewPort(XAxisStart + 1, XAxisSize);
			} else {
				series = new GraphViewSeries(legendText, new GraphViewSeriesStyle(lineColor,
						(int) (widthPixels / 1080f * lineOfThickness)), values, bitmap, suiteY);
			}
			graphView.addSeries(series, isDrawingLine);
			return series;
		}
		return null;
	}

	@Deprecated
	public void appendData(boolean scrollToEnd, int maxDataCount, GraphViewDataInterface... values) {
		for (int i = 0; i < values.length; i++) {
			valueslist.get(i).appendData(values[i], scrollToEnd, maxDataCount);
		}
	}

	/**
	 * 移除特定曲线。
	 * 
	 * @param series
	 *            待移除曲线对象。
	 */
	public void removeSeries(GraphViewSeries series) {
		if (null != graphView && series != null) {
			graphView.removeSeries(series);
		}
	}

	/**
	 * 移除所有曲线，但graphview对象未置空。
	 */
	public void removeAllSeries() {
		if (null != graphView) {
			graphView.removeAllSeries();
		}
	}

	private void resetValue(GraphViewData[] values1, GraphViewData[] values2) {

		int ii = 0;

		if (values1.length >= values2.length) {
			valuesMaxLength = values1.length;
			for (int i = 0; i < values1.length; i++) {
				values1[i].getX().setKey(i);
			}

			for (int i = 0; i < values2.length; i++) {
				for (int j = 0; j < values1.length; j++) {
					if (values2[i].getX().getValue().trim()
							.equals(values1[j].getX().getValue().trim())) {
						values2[i].valueX.setKey(values1[j].getX().getKey());
						// reset.
						values2[ii].setX(values2[i].valueX);
						values2[ii].setY(values2[i].valueY);
						ii++;
						break;
					}
				}
			}
			for (int i = ii; i < values2.length; i++) {
				if (ii > 0) {
					values2[i].setX(values2[ii - 1].valueX);
					values2[i].setY(values2[ii - 1].valueY);
				} else {
					values2[i].setX(values1[values1.length - 1].valueX);
					values2[i].setY(values2[0].valueY);
				}
			}

			return;
		}

		ii = 0;
		if (values2.length > values1.length) {
			valuesMaxLength = values2.length;
			for (int i = 0; i < values2.length; i++) {
				values2[i].getX().setKey(i);
			}

			for (int i = 0; i < values1.length; i++) {
				for (int j = 0; j < values2.length; j++) {
					if (values1[i].getX().getValue().trim()
							.equals(values2[j].getX().getValue().trim())) {
						values1[i].valueX.setKey(values2[j].getX().getKey());
						// reset.
						values1[ii].setX(values1[i].valueX);
						values1[ii].setY(values1[i].valueY);
						ii++;
						break;
					}
				}
			}
			for (int i = ii; i < values1.length; i++) {
				if (ii > 0) {
					values1[i].setX(values1[ii - 1].valueX);
					values1[i].setY(values1[ii - 1].valueY);
				} else {
					values1[i].setX(values2[values2.length - 1].valueX);
					values1[i].setY(values1[0].valueY);
				}
			}

			return;
		}
	}

	private void resetValue(GraphViewData[] values1, GraphViewData[] values2,
			GraphViewData[] values3) {

		int ii = 0;

		if (values1.length >= (values2.length > values3.length ? values2.length : values3.length)) {
			valuesMaxLength = values1.length;

			for (int i = 0; i < values1.length; i++) {
				values1[i].getX().setKey(i);
			}

			for (int i = 0; i < values2.length; i++) {
				for (int j = 0; j < values1.length; j++) {
					if (values2[i].getX().getValue().trim()
							.equals(values1[j].getX().getValue().trim())) {
						values2[i].valueX.setKey(values1[j].getX().getKey());
						// reset.
						values2[ii].setX(values2[i].valueX);
						values2[ii].setY(values2[i].valueY);
						ii++;
						break;
					}
				}
			}
			for (int i = ii; i < values2.length; i++) {
				if (ii > 0) {
					values2[i].setX(values2[ii - 1].valueX);
					values2[i].setY(values2[ii - 1].valueY);
				} else {
					values2[i].setX(values1[values1.length - 1].valueX);
					values2[i].setY(values2[0].valueY);
				}
			}

			ii = 0;
			for (int i = 0; i < values3.length; i++) {
				for (int j = 0; j < values1.length; j++) {
					if (values3[i].getX().getValue().trim()
							.equals(values1[j].getX().getValue().trim())) {
						values3[i].valueX.setKey(values1[j].getX().getKey());
						// reset.
						values3[ii].setX(values3[i].valueX);
						values3[ii].setY(values3[i].valueY);
						ii++;
						break;
					}
				}
			}
			for (int i = ii; i < values3.length; i++) {
				if (ii > 0) {
					values3[i].setX(values3[ii - 1].valueX);
					values3[i].setY(values3[ii - 1].valueY);
				} else {
					values3[i].setX(values1[values1.length - 1].valueX);
					values3[i].setY(values3[0].valueY);
				}
			}

			return;
		}

		if (values2.length >= (values1.length > values3.length ? values1.length : values3.length)) {
			valuesMaxLength = values2.length;
			for (int i = 0; i < values2.length; i++) {
				values2[i].getX().setKey(i);
			}

			for (int i = 0; i < values1.length; i++) {
				for (int j = 0; j < values2.length; j++) {
					if (values1[i].getX().getValue().trim()
							.equals(values2[j].getX().getValue().trim())) {
						values1[i].valueX.setKey(values2[j].getX().getKey());
						// reset.
						values1[ii].setX(values1[i].valueX);
						values1[ii].setY(values1[i].valueY);
						ii++;
						break;
					}
				}
			}
			for (int i = ii; i < values1.length; i++) {
				if (ii > 0) {
					values1[i].setX(values1[ii - 1].valueX);
					values1[i].setY(values1[ii - 1].valueY);
				} else {
					values1[i].setX(values2[values2.length - 1].valueX);
					values1[i].setY(values1[0].valueY);
				}
			}

			ii = 0;
			for (int i = 0; i < values3.length; i++) {
				for (int j = 0; j < values2.length; j++) {
					if (values3[i].getX().getValue().trim()
							.equals(values2[j].getX().getValue().trim())) {
						values3[i].valueX.setKey(values2[j].getX().getKey());
						// reset.
						values3[ii].setX(values3[i].valueX);
						values3[ii].setY(values3[i].valueY);
						ii++;
						break;
					}
				}
			}
			for (int i = ii; i < values3.length; i++) {
				if (ii > 0) {
					values3[i].setX(values3[ii - 1].valueX);
					values3[i].setY(values3[ii - 1].valueY);
				} else {
					values3[i].setX(values2[values2.length - 1].valueX);
					values3[i].setY(values3[0].valueY);
				}
			}

			return;
		}

		if (values3.length >= (values2.length > values1.length ? values2.length : values1.length)) {
			valuesMaxLength = values3.length;
			for (int i = 0; i < values3.length; i++) {
				values3[i].getX().setKey(i);
			}

			for (int i = 0; i < values2.length; i++) {
				for (int j = 0; j < values3.length; j++) {
					if (values2[i].getX().getValue().trim()
							.equals(values3[j].getX().getValue().trim())) {
						values2[i].valueX.setKey(values3[j].getX().getKey());
						// reset.
						values2[ii].setX(values2[i].valueX);
						values2[ii].setY(values2[i].valueY);
						ii++;
						break;
					}
				}
			}
			for (int i = ii; i < values2.length; i++) {
				if (ii > 0) {
					values2[i].setX(values2[ii - 1].valueX);
					values2[i].setY(values2[ii - 1].valueY);
				} else {
					values2[i].setX(values3[values3.length - 1].valueX);
					values2[i].setY(values2[0].valueY);
				}
			}

			ii = 0;
			for (int i = 0; i < values1.length; i++) {
				for (int j = 0; j < values3.length; j++) {
					if (values1[i].getX().getValue().trim()
							.equals(values3[j].getX().getValue().trim())) {
						values1[i].valueX.setKey(values3[j].getX().getKey());
						// reset.
						values1[ii].setX(values1[i].valueX);
						values1[ii].setY(values1[i].valueY);
						ii++;
						break;
					}
				}
			}
			for (int i = ii; i < values1.length; i++) {
				if (ii > 0) {
					values1[i].setX(values1[ii - 1].valueX);
					values1[i].setY(values1[ii - 1].valueY);
				} else {
					values1[i].setX(values3[values3.length - 1].valueX);
					values1[i].setY(values1[0].valueY);
				}
			}

			return;
		}
	}

	/**
	 * 检验横坐标时间数据是否连贯且递增，并调整横坐标时间，使其合理。最多支持三条曲线。
	 * 
	 * @param values1
	 *            曲线点集。
	 * @param values2
	 *            曲线点集。可以为空。
	 * @param values3
	 *            曲线点集。可以为空。
	 */
	public void resetValues(GraphViewData[] values1, GraphViewData[] values2,
			GraphViewData[] values3) {
		if (values1 == null) {
			if (values2 == null) {
				if (values3 == null) {
					return;
				} else {
					valuesMaxLength = values3.length;
				}
			} else {
				if (values3 == null) {
					valuesMaxLength = values2.length;
					return;
				} else {
					resetValue(values2, values3);
				}
			}
		} else if (values2 == null) {
			if (values3 == null) {
				valuesMaxLength = values1.length;
				return;
			} else {
				resetValue(values1, values3);
			}
		} else if (values3 == null) {
			resetValue(values1, values2);
		} else {
			resetValue(values1, values2, values3);
		}
	}

	/**
	 * 清空曲线图。graphview已置空，所以若接下来需再显示，需调用initializeGraphView();
	 */
	public void setEmptyGraphView() {
		if (graphView != null) {
			graphView = null;
			valueslist.clear();
			mContainer.removeAllViews();
		}
	}

	/**
	 * 设置曲线图滑动监听事件。可用于GA统计。
	 * 
	 * @param onGraphScrolledListener
	 */
	public void setOnGraphScrolledListener(OnGraphScrolledListener onGraphScrolledListener) {
		this.onGraphScrolledListener = onGraphScrolledListener;
	}

	/**
	 * 设置纵坐标的单位。
	 * 
	 * @param verticalUnit
	 *            默认：元/㎡
	 */
	public void setVerticalUnit(String verticalUnit) {
		this.verticalUnit = verticalUnit;
	}

	/**
	 * 是否绘制网格横线。
	 * 
	 * @param isDrawHoriLines
	 *            默认不绘制横线
	 */
	public void setIsDrawHoriLines(boolean isDrawHoriLines) {
		this.isDrawHoriLines = isDrawHoriLines;
	}

	/**
	 * 设置纵坐标文本对齐方式。
	 * 
	 * @param align
	 *            默认Align.left
	 */
	public void setVerticalLabelsLayoutInParent(Align align) {
		this.verticalAlign = align;
	}

	/**
	 * 设置横纵坐标文本的字体大小。
	 * 
	 * @param horVerTextSize
	 *            默认30像素。
	 */
	public void setHorVerTextSize(float horVerTextSize) {
		this.horVerTextSize = horVerTextSize;
	}

	/**
	 * 设置线的粗度。
	 * 
	 * @param thickness
	 *            默认是3像素。
	 */
	public void setLineOfThickness(int thickness) {
		this.lineOfThickness = thickness;
	}

	/**
	 * 设置点半径。
	 * 
	 * @param dataPointsRadius
	 *            默认10像素。
	 */
	public void setDataPointsRadius(float dataPointsRadius) {
		drawDataPoints = true;
		this.dataPointsRadius = dataPointsRadius;
	}

	/**
	 * 设置自定义纵坐标文本。默认纵坐标文本左对齐，纵坐标显示在图像左侧。
	 * 
	 * @param YAxisLabels
	 *            纵坐标文本集。
	 * @param YAxisMax
	 *            纵坐标最大值。
	 * @param YAxisMin
	 *            纵坐标最小值。
	 */
	public void setShowVerticalLabels(String[] YAxisLabels, double YAxisMax, double YAxisMin) {
		setShowVerticalLabels(YAxisLabels, YAxisMax, YAxisMin, Align.LEFT, Align.LEFT);
	}

	/**
	 * 设置自定义纵坐标文本。
	 * 
	 * @param YAxisLabels
	 *            纵坐标文本集。
	 * @param YAxisMax
	 *            纵坐标最大值。
	 * @param YAxisMin
	 *            纵坐标最小值。
	 * @param verticalLabelsTextAlign
	 *            纵坐标文本对齐方式。
	 * @param vertialLabelsLayoutInParentAlign
	 *            纵坐标在图像左侧还是右侧。
	 */
	public void setShowVerticalLabels(String[] YAxisLabels, double YAxisMax, double YAxisMin,
			Align verticalLabelsTextAlign, Align vertialLabelsLayoutInParentAlign) {
		showVerticalLabels = true;

		this.YAxisLabels = YAxisLabels;
		this.YAxisMax = YAxisMax;
		this.YAxisMin = YAxisMin;
		this.verticalLabelsAlign = verticalLabelsTextAlign;
		this.verticalAlign = vertialLabelsLayoutInParentAlign;
	}

	/**
	 * 设置纵坐标范围。
	 * 
	 * @param YAxisMax
	 *            纵坐标最大值。
	 * @param YAxisMin
	 *            纵坐标最小值。
	 * @param YAxisLabelsNum
	 *            纵坐标个数。
	 */
	public void setManualYAxisBounds(double YAxisMax, double YAxisMin, int YAxisLabelsNum) {
		manualYAxisBounds = true;
		this.YAxisMax = YAxisMax;
		this.YAxisMin = YAxisMin;
		this.YAxisLabelsNum = YAxisLabelsNum;
	}

	/**
	 * 设置曲线图的标题。
	 * 
	 * @param graphViewTitle
	 *            标题文本。
	 * @param titleColor
	 *            标题颜色。
	 * @param titleSize
	 *            标题字体大小。
	 */
	public void setShowGraphViewTitle(String graphViewTitle, int titleColor, float titleSize) {
		this.showGraphViewTitle = true;
		this.graphViewTitle = graphViewTitle;
		style.setTitleColor(titleColor);
		style.setTitleTextSize(widthPixels / 1080f * titleSize);
	}

	/**
	 * 设置Legend的对齐方式。
	 * 
	 * @param align
	 */
	public void setLegend(LegendAlign align) {
		this.align = align;
	}

	/**
	 * 是否显示图例。
	 * 
	 * @param showLegend
	 *            默认值为true(show).
	 */
	public void setShowLegend(boolean showLegend) {
		this.showLegend = showLegend;
	}

	/**
	 * 是否显示图片点图例。
	 * 
	 * @param showSuiteLegend
	 *            默认值为false(do not show)
	 */
	public void setShowSuiteLegend(boolean showSuiteLegend) {
		this.showSuiteLegend = showSuiteLegend;
	}

	/**
	 * 图片点图例文本内容。
	 * 
	 * @param suiteText
	 *            默认值为“本房源”
	 */
	public void setSuiteText(String suiteText) {
		this.suiteText = suiteText;
	}

	/**
	 * 设置曲线图可滑动查看，否则在当前屏幕大小中显示出所有数据点。
	 * 
	 * @param XAxisStart
	 *            开始点。
	 * @param XAxisSize
	 *            当前屏幕显示几个点。
	 */
	public void setScrollable(double XAxisStart, double XAxisSize) {
		if (XAxisStart >= 0) {
			this.scrollable = true;
		}
		this.XAxisStart = XAxisStart;
		if (1 < XAxisSize) {
			this.XAxisSize = XAxisSize - 1;
		}
	}

	/**
	 * 设置点上方是否显示对应的纵坐标值文本。
	 * 
	 * @param valuesOnTopColor
	 *            文本颜色值。
	 * @param valuesOnTopTextSize
	 *            文本字体大小。
	 */
	public void setDrawValuesOnTop(int valuesOnTopColor, float valuesOnTopTextSize) {
		this.drawValuesOnTop = true;
		this.valuesOnTopColor = valuesOnTopColor;
		this.valuesOnTopTextSize = valuesOnTopTextSize;
	}

	/**
	 * 设置点上方是否显示对应的纵坐标值文本。
	 * 
	 * @param valuesOnTopColor
	 *            文本颜色值 。
	 * @param valuesOnTopTextSize
	 *            文本字体大小 。
	 * @param valuesOnTopUnitNum
	 *            文本显示内容的单位数。15000需要显示成1.5，该参数需要设置为10000.
	 * @param valuesOnTopDecimalFormat
	 *            小数位数。
	 * @param valuesOnTopUnitName
	 *            单位名。
	 */
	public void setDrawValuesOnTop(int valuesOnTopColor, float valuesOnTopTextSize,
			double valuesOnTopUnitNum, String valuesOnTopDecimalFormat, String valuesOnTopUnitName) {
		this.drawValuesOnTop = true;
		this.valuesOnTopColor = valuesOnTopColor;
		this.valuesOnTopTextSize = valuesOnTopTextSize;
		// this.valuesOnTopBaseNum = valuesOnTopBaseNum;
		this.valuesOnTopUnitNum = valuesOnTopUnitNum;
		this.valuesOnTopDecimalFormat = valuesOnTopDecimalFormat;
		this.valuesOnTopUnitName = valuesOnTopUnitName;
	}

	public void setGridColor(int gridColor) {
		style.setGridColor(gridColor);
	}

	public void setHorizontalLabelsColor(int horLabelsColor) {
		style.setHorizontalLabelsColor(horLabelsColor);
	}

	public void setVerticalLabelsColor(int verLabelsColor) {
		style.setVerticalLabelsColor(verLabelsColor);
	}

	public int getLineOfThickness() {
		return lineOfThickness;
	}

	public boolean getDrawPoint() {
		return drawDataPoints;
	}

	public float getDataPointsRadius() {
		return this.dataPointsRadius;
	}

	public boolean getShowVerticalLabels() {
		return showVerticalLabels;
	}

	public boolean getShowGraphViewTitle() {
		return showGraphViewTitle;
	}

	public int getValuesMaxLength() {
		return this.valuesMaxLength;
	}

	public double getViewPortStart() {
		if (null != graphView) {
			return graphView.getViewPortStart();
		}
		return 0;
	}

	public interface OnGraphScrolledListener {
		void OnGraphScrolled();
	}

}
