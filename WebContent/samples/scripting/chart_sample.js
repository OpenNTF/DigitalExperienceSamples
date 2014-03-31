/*
 * Copyright 2014  IBM Corp.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 * specific language governing permissions and limitations under the License.
*/

var defaultData = [ {
	"Month" : "January",
	"Goal" : 4000,
	"Actual" : 1233
}, {
	"Month" : "February",
	"Goal" : 5000,
	"Actual" : 5613
}, {
	"Month" : "March",
	"Goal" : 5000,
	"Actual" : 4302
} ];

var chartSample = {
	displayBarChart : function(id, data) {

		var chart = new cfx.Chart();
		chart.setGallery(cfx.Gallery.Bar);
		chart.getAllSeries().getPointLabels().setVisible(true);

		// set title
		var title = new cfx.TitleDockable();
		title.setText("Goal and Actual Sales by Month");
		chart.getTitles().add(title);

		chart.setDataSource(data);
		chartData = chart.getData();
		chartData.setSeries(2); // two series
		chartData.setPoints(data.length);
		chart.getLegendBox().setVisible(true);
		chart.create(id);
	},

	// 
	displayBarChartFromService : function(id, data) {
		console.log("data: " + JSON.stringify(data));
		// Transform the data to the way jqPlot Charts wants it:
		var jqPlotChartData = jQuery.map(data, function(row, index) {
			var newRow = {};
			newRow["Month"] = row.Month;
			newRow["Goal"] = parseFloat(row.Goal);
			newRow["Actual"] = parseFloat(row.Actual);
			return newRow;
		});
		chartSample.displayBarChart(id, jqPlotChartData);
	}
};

$(document).ready(
		function() {
			// see if "salesData" variable is available, with members for URLs for REST services
			if (typeof salesData == "undefined") {
				// Use default static data
				chartSample.displayBarChart('ChartDiv', defaultData);
			} else {
				// Fetch dynamic JSON sing WEF REST Enabled Data Service REST URL
				$.getJSON(salesData.getSalesReportsURL, {}, function(ajaxData) {
					chartSample.displayBarChartFromService('ChartDiv',
							ajaxData.SalesData.MonthData);
				});
			}
		});
