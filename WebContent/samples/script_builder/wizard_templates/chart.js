/*
 * Copyright 2014  IBM Corp.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 * specific language governing permissions and limitations under the License.
*/

/*  Sample implementation for a Bar chart 
 * 
 var data = [ ];

var chart = {
	displayBarChart : function(id, plotData) {
	},
	// Get data into the required format, then display the chart
	displayChart: function(id, data) {
	},

	// Display chart using data from service - 
	displayBarChartFromService : function(id, data) {
	}
};

// sample of ready function when using jQuery
$(document).ready(
		function() {
			// an example with a data service and a variable created by wef to access it 
			// see if "customersServiceUrls" variable is available, with members for URLs
			// for REST services
			if (typeof customersServiceUrls == "undefined") {
				 $('#customerListTable').append("No customerServiceUrls defined");
			} else {
				// Fetch dynamic JSON sing WEF REST Enabled Data Service REST URL
				$.getJSON(customersServiceUrls.getCustomersURL, {}, function(ajaxData) {
					// console.log("ajaxData: " + JSON.stringify(ajaxData));
					displayDataTable(ajaxData.customerList.customer, "customerListTable");
				});
			}
			;
		});
*/
