/*
 * Sample of JS chart, data from WEF JSON/REST service
 */

var chartSample = {

	displayChart : function(data) {
		// If we received results, then construct the chart with the response data
	if (data.accounts && data.accounts.accountSummary) {

			// The results JSON structure has a repeating set of "AccountsRow" within the AccountsRowSet parent 
			var accts = data.accounts.accountSummary;

			// Transform the data to the way jqPlot Charts wants to see/find it:
			var jqPlotChartData = jQuery.map(accts, function(accountSummary, index) {
				if (accountSummary.accountBalance> 0.0) {
					return [ [ accountSummary.accountName, parseFloat(accountSummary.accountBalance) ] ];
				}
			});

			// Build the chart - see the chart provider's doc for more options
			jQuery.jqplot('AccountsChart', [ jqPlotChartData ], {
				seriesDefaults : {
					renderer : jQuery.jqplot.PieRenderer,
					rendererOptions : {
						showDataLabels : true
					}
				},
				legend : {
					location : 'e',
					show : true
				}
			// Legend is on "east" side
			});
		}
	},

	getAccountsDisplayChart : function() {
		// Fetch dynamic JSON sing WEF REST Enabled Data Service REST URL
		$.getJSON(bankAccountsServiceUrls.getAccounts , {}, function(ajaxData) {
			chartSample.displayChart(ajaxData);
		});
	}

};

$(document).ready(function() {
	// Get Accounts and Populate List and click handlers
	chartSample.getAccountsDisplayChart();
});
