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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.view.ContextThemeWrapper;

/**
 * Styles for the GraphView Important: Use
 * {@link com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle} for
 * series-specify styling
 * 
 */
public class GraphViewStyle {
	private int titleColor;
	private int verticalLabelsColor;
	private int horizontalLabelsColor;
	private int gridColor;
	private GridStyle gridStyle = GridStyle.BOTH;
	private float textSize;
	private int verticalLabelsWidth;
	private int numVerticalLabels;
	private int numHorizontalLabels;
	private int legendWidth;
	private int legendBorder;
	private int legendSpacing;
	private int legendMarginBottom;
	private Align verticalLabelsAlign;
	// 6.1add-----add title text size.-----
	private float titleTextSize;
	private boolean isDrawHoriLines;
	private String verticalUnit;
	private int border;
	private int graphAlign;
	private int titleHeightFactor;
	// 6.2add
	private float distance = -20;
	private boolean showSuiteLegend;

	public GraphViewStyle() {
		setDefaults();
	}

	public GraphViewStyle(int vLabelsColor, int hLabelsColor, int gridColor) {
		setDefaults();
		this.verticalLabelsColor = vLabelsColor;
		this.horizontalLabelsColor = hLabelsColor;
		this.gridColor = gridColor;
	}

	public int getTitleColor() {
		return titleColor;
	}

	public int getGridColor() {
		return gridColor;
	}

	public GridStyle getGridStyle() {
		return gridStyle;
	}

	public int getHorizontalLabelsColor() {
		return horizontalLabelsColor;
	}

	public int getLegendBorder() {
		return legendBorder;
	}

	public int getLegendSpacing() {
		return legendSpacing;
	}

	public int getLegendWidth() {
		return legendWidth;
	}

	public int getLegendMarginBottom() {
		return legendMarginBottom;
	}

	public int getNumHorizontalLabels() {
		return numHorizontalLabels;
	}

	public int getNumVerticalLabels() {
		return numVerticalLabels;
	}

	public float getTextSize() {
		return textSize;
	}

	public Align getVerticalLabelsAlign() {
		return verticalLabelsAlign;
	}

	public int getVerticalLabelsColor() {
		return verticalLabelsColor;
	}

	public int getVerticalLabelsWidth() {
		return verticalLabelsWidth;
	}

	public boolean getIsDrawHoriLines() {
		return isDrawHoriLines;
	}

	/**
	 * 6.1add-----add title text size.-----
	 */
	public float getTitleTextSize() {
		return titleTextSize;
	}

	public String getVerticalUnit() {
		return verticalUnit;
	}

	public int getBorder() {
		return this.border;
	}

	public int getGraphAlign() {
		return this.graphAlign;
	}

	public int getTitleHeightFactor() {
		return this.titleHeightFactor;
	}

	public float getDistance() {
		return this.distance;
	}

	public boolean getShowSuiteLegend() {
		return this.showSuiteLegend;
	}

	private void setDefaults() {
		// 6.1change-----change
		// titleColor/verticalLabelsColor/horizontalLabelsColor default white
		// color to black.-----
		titleColor = Color.BLACK;
		verticalLabelsColor = Color.BLACK;
		horizontalLabelsColor = Color.BLACK;
		gridColor = Color.DKGRAY;
		textSize = 30f;
		legendWidth = 120;
		legendBorder = 10;
		legendSpacing = 10;
		legendMarginBottom = 0;
		verticalLabelsAlign = Align.CENTER;
		// 6.1add-----add title text size-----
		titleTextSize = 50f;
		isDrawHoriLines = false;
		verticalUnit = "";
		border = 30;
		graphAlign = 1;
		titleHeightFactor = 2;
	}

	public void setTitleColor(int c) {
		titleColor = c;
	}

	public void setGridStyle(GridStyle style) {
		gridStyle = style;
	}

	public void setGridColor(int c) {
		gridColor = c;
	}

	public void setHorizontalLabelsColor(int c) {
		horizontalLabelsColor = c;
	}

	public void setLegendBorder(int legendBorder) {
		this.legendBorder = legendBorder;
	}

	public void setLegendSpacing(int legendSpacing) {
		this.legendSpacing = legendSpacing;
	}

	public void setLegendWidth(int legendWidth) {
		this.legendWidth = legendWidth;
	}

	public void setLegendMarginBottom(int legendMarginBottom) {
		this.legendMarginBottom = legendMarginBottom;
	}

	/**
	 * @param numHorizontalLabels
	 *            0 = auto
	 */
	public void setNumHorizontalLabels(int numHorizontalLabels) {
		this.numHorizontalLabels = numHorizontalLabels;
	}

	/**
	 * @param numVerticalLabels
	 *            0 = auto
	 */
	public void setNumVerticalLabels(int numVerticalLabels) {
		this.numVerticalLabels = numVerticalLabels;
	}

	public void setTextSize(float textSize) {
		this.textSize = textSize;
	}

	public void setVerticalLabelsAlign(Align verticalLabelsAlign) {
		this.verticalLabelsAlign = verticalLabelsAlign;
	}

	public void setVerticalLabelsColor(int c) {
		verticalLabelsColor = c;
	}

	/**
	 * @param verticalLabelsWidth
	 *            0 = auto
	 */
	public void setVerticalLabelsWidth(int verticalLabelsWidth) {
		this.verticalLabelsWidth = verticalLabelsWidth;
	}

	/**
	 * 6.1add-----add title text size.-----
	 * 
	 * @param titleSize
	 *            title text size.
	 */
	public void setTitleTextSize(float titleSize) {
		this.titleTextSize = titleSize;
	}

	public void setIsDrawHoriLines(boolean isDrawHoriLines) {
		this.isDrawHoriLines = isDrawHoriLines;
	}

	public void setVerticalUnit(String verticalUnit) {
		this.verticalUnit = verticalUnit;
	}

	public void setBorder(int border) {
		this.border = border;
	}

	public void setGraphAlign(int align) {
		this.graphAlign = align;
	}

	public void setTitleHeightFactor(int factor) {
		this.titleHeightFactor = factor;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}

	public void setShowSuiteLegend(boolean show) {
		showSuiteLegend = show;
	}

	/**
	 * tries to get the theme's font color and use it for labels
	 * 
	 * @param context
	 *            must be instance of ContextThemeWrapper
	 */
	public void useTextColorFromTheme(Context context) {
		if (context instanceof ContextThemeWrapper) {
			TypedArray array = ((ContextThemeWrapper) context).getTheme().obtainStyledAttributes(
					new int[] { android.R.attr.textColorPrimary });
			int color = array.getColor(0, getVerticalLabelsColor());
			array.recycle();

			setVerticalLabelsColor(color);
			setHorizontalLabelsColor(color);
		}
	}

	/**
	 * Definition which lines will be drawn in the backgrounds
	 */
	public enum GridStyle {
		BOTH, VERTICAL, HORIZONTAL
	}
}
