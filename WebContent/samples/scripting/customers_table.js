/*
 * Copyright 2014  IBM Corp.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 * specific language governing permissions and limitations under the License.
*/
// Customer data
function displayDataTable(data, tableId) {
	var items = [];
    $.each( data, function( key,value ) {
  	  var data ='<tr class="customer" data-id="'+value.id+'">'+
		'	<td class="id">'+value.id+'</td>'+
		'	<td class="name">'+value.name+'</td>'+
		'	<td class="balance">'+value.balance+'</td>'+
		'	<td class="city">'+value.city+'</td>'+
		'</tr>';

        items.push( data );
      });
    $('#' + tableId).append("<tbody></tbody>").append(items);
	$('#' + tableId).dataTable({
		"sPaginationType": "two_button", 
		"bFilter": true, 
		"bSort": true, 
		"bLengthChange": true,
	    "oLanguage": {
	        "sInfo": "_START_ to _END_ of _TOTAL_ entries"
	      }
	});
	
};

$(document).ready(
		function() {
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



