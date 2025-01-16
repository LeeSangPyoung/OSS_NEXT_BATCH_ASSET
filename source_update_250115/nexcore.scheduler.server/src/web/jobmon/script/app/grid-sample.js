$a.page(function() {
	
	var data = [
	        	{
	        		"name": "Vera",
	        		"phone": "138-955-8109",
	        		"email": "eu.odio@maurisblanditmattis.ca",
	        		"birthday": "2015-07-18",
	        		"zip": "467025",
	        		"salary": 787197,
	        		"absence": 100,
	        		"country": "Bahamas",
	        		"city": "Serrungarina",
	        		"company": "Suspendisse Dui Fusce PC",
	        		"type": 2
	        	},
	        	{
	        		"name": "Autumn",
	        		"phone": "369-405-2973",
	        		"email": "Quisque@loremsemper.com",
	        		"birthday": "2014-05-03",
	        		"zip": "370659",
	        		"salary": 168857,
	        		"absence": 200,
	        		"country": "Suriname",
	        		"city": "Eigenbrakel",
	        		"company": "Nibh Dolor Nonummy LLC",
	        		"type": 9
	        	},
	        	{
	        		"name": "Amy",
	        		"phone": "693-6192",
	        		"email": "sit@egetmagnaSuspendisse.ca",
	        		"birthday": "2015-08-06",
	        		"zip": "2794",
	        		"salary": 235913,
	        		"absence": 30,
	        		"country": "Cocos (Keeling) Islands",
	        		"city": "Chartres",
	        		"company": "Tincidunt Consulting",
	        		"type": 1
	        	},
	        	{
	        		"name": "Thor",
	        		"phone": "569-6512",
	        		"email": "parturient.montes.nascetur@imperdietnon.edu",
	        		"birthday": "2014-01-17",
	        		"zip": "26962",
	        		"salary": 853938,
	        		"absence": 10,
	        		"country": "Spain",
	        		"city": "Beho",
	        		"company": "Nulla LLC",
	        		"type": 10
	        	},
	        	{
	        		"name": "Dakota",
	        		"phone": "270-477-0444",
	        		"email": "sed.pede@tortordictumeu.edu",
	        		"birthday": "2015-06-17",
	        		"zip": "7374PU",
	        		"salary": 575048,
	        		"absence": 10,
	        		"country": "Bonaire, Sint Eustatius and Saba",
	        		"city": "Yahyalı",
	        		"company": "Parturient Montes Nascetur Inc.",
	        		"type": 1
	        	},
	        	{
	        		"name": "Cooper",
	        		"phone": "610-9420",
	        		"email": "placerat.Cras.dictum@eratVivamus.net",
	        		"birthday": "2015-07-20",
	        		"zip": "73706",
	        		"salary": 170683,
	        		"absence": 100,
	        		"country": "New Caledonia",
	        		"city": "Deline",
	        		"company": "Nulla Tincidunt Neque Ltd",
	        		"type": 2
	        	},
	        	{
	        		"name": "Bethany",
	        		"phone": "949-378-7845",
	        		"email": "erat@Morbiaccumsanlaoreet.edu",
	        		"birthday": "2014-12-16",
	        		"zip": "83245-257",
	        		"salary": 998642,
	        		"absence": 89,
	        		"country": "Poland",
	        		"city": "Milwaukee",
	        		"company": "Vel Sapien Company",
	        		"type": 1
	        	},
	        	{
	        		"name": "Ronan",
	        		"phone": "503-6165",
	        		"email": "pellentesque@egestasDuisac.co.uk",
	        		"birthday": "2015-03-23",
	        		"zip": "1124",
	        		"salary": 38416,
	        		"absence": 65,
	        		"country": "Aruba",
	        		"city": "Calgary",
	        		"company": "Donec PC",
	        		"type": 7
	        	},
	        	{
	        		"name": "Lamar",
	        		"phone": "244-4542",
	        		"email": "enim.sit@nonarcu.com",
	        		"birthday": "2015-07-29",
	        		"zip": "18214",
	        		"salary": 106591,
	        		"absence": 4,
	        		"country": "Palau",
	        		"city": "Portland",
	        		"company": "Tristique Pharetra Quisque Corp.",
	        		"type": 4
	        	},
	        	{
	        		"name": "Gisela",
	        		"phone": "203-196-7156",
	        		"email": "porttitor.tellus@Sed.org",
	        		"birthday": "2014-03-20",
	        		"zip": "587048",
	        		"salary": 255047,
	        		"absence": 100,
	        		"country": "Italy",
	        		"city": "Aberystwyth",
	        		"company": "Augue Eu Tempor Associates",
	        		"type": 6
	        	},
	        	{
	        		"name": "Nolan",
	        		"phone": "694-889-2611",
	        		"email": "Etiam.laoreet.libero@Aliquamadipiscing.ca",
	        		"birthday": "2014-07-10",
	        		"zip": "74-869",
	        		"salary": 205358,
	        		"absence": 30,
	        		"country": "Gambia",
	        		"city": "Coldstream",
	        		"company": "Ac Fermentum Vel PC",
	        		"type": 7
	        	},
	        	{
	        		"name": "Vera",
	        		"phone": "138-955-8109",
	        		"email": "eu.odio@maurisblanditmattis.ca",
	        		"birthday": "2015-07-18",
	        		"zip": "467025",
	        		"salary": 787197,
	        		"absence": 6,
	        		"country": "Bahamas",
	        		"city": "Serrungarina",
	        		"company": "Suspendisse Dui Fusce PC",
	        		"type": 2
	        	},
	        	{
	        		"name": "Autumn",
	        		"phone": "369-405-2973",
	        		"email": "Quisque@loremsemper.com",
	        		"birthday": "2014-05-03",
	        		"zip": "370659",
	        		"salary": 168857,
	        		"absence": 100,
	        		"country": "Suriname",
	        		"city": "Eigenbrakel",
	        		"company": "Nibh Dolor Nonummy LLC",
	        		"type": 9
	        	},
	        	{
	        		"name": "Amy",
	        		"phone": "693-6192",
	        		"email": "sit@egetmagnaSuspendisse.ca",
	        		"birthday": "2015-08-06",
	        		"zip": "2794",
	        		"salary": 235913,
	        		"absence": 40,
	        		"country": "Cocos (Keeling) Islands",
	        		"city": "Chartres",
	        		"company": "Tincidunt Consulting",
	        		"type": 1
	        	},
	        	{
	        		"name": "Thor",
	        		"phone": "569-6512",
	        		"email": "parturient.montes.nascetur@imperdietnon.edu",
	        		"birthday": "2014-01-17",
	        		"zip": "26962",
	        		"salary": 853938,
	        		"absence": 100,
	        		"country": "Spain",
	        		"city": "Beho",
	        		"company": "Nulla LLC",
	        		"type": 10
	        	},
	        	{
	        		"name": "Dakota",
	        		"phone": "270-477-0444",
	        		"email": "sed.pede@tortordictumeu.edu",
	        		"birthday": "2015-06-17",
	        		"zip": "7374PU",
	        		"salary": 575048,
	        		"absence": 100,
	        		"country": "Bonaire, Sint Eustatius and Saba",
	        		"city": "Yahyalı",
	        		"company": "Parturient Montes Nascetur Inc.",
	        		"type": 1
	        	},
	        	{
	        		"name": "Cooper",
	        		"phone": "610-9420",
	        		"email": "placerat.Cras.dictum@eratVivamus.net",
	        		"birthday": "2015-07-20",
	        		"zip": "73706",
	        		"salary": 170683,
	        		"absence": 10,
	        		"country": "New Caledonia",
	        		"city": "Deline",
	        		"company": "Nulla Tincidunt Neque Ltd",
	        		"type": 2
	        	},
	        	{
	        		"name": "Bethany",
	        		"phone": "949-378-7845",
	        		"email": "erat@Morbiaccumsanlaoreet.edu",
	        		"birthday": "2014-12-16",
	        		"zip": "83245-257",
	        		"salary": 998642,
	        		"absence": 10,
	        		"country": "Poland",
	        		"city": "Milwaukee",
	        		"company": "Vel Sapien Company",
	        		"type": 1
	        	},
	        	{
	        		"name": "Ronan",
	        		"phone": "503-6165",
	        		"email": "pellentesque@egestasDuisac.co.uk",
	        		"birthday": "2015-03-23",
	        		"zip": "1124",
	        		"salary": 38416,
	        		"absence": 80,
	        		"country": "Aruba",
	        		"city": "Calgary",
	        		"company": "Donec PC",
	        		"type": 7
	        	},
	        	{
	        		"name": "Lamar",
	        		"phone": "244-4542",
	        		"email": "enim.sit@nonarcu.com",
	        		"birthday": "2015-07-29",
	        		"zip": "18214",
	        		"salary": 106591,
	        		"absence": 100,
	        		"country": "Palau",
	        		"city": "Portland",
	        		"company": "Tristique Pharetra Quisque Corp.",
	        		"type": 4
	        	},
	        	{
	        		"name": "Gisela",
	        		"phone": "203-196-7156",
	        		"email": "porttitor.tellus@Sed.org",
	        		"birthday": "2014-03-20",
	        		"zip": "587048",
	        		"salary": 255047,
	        		"absence": 60,
	        		"country": "Italy",
	        		"city": "Aberystwyth",
	        		"company": "Augue Eu Tempor Associates",
	        		"type": 6
	        	},
	        	{
	        		"name": "Nolan",
	        		"phone": "694-889-2611",
	        		"email": "Etiam.laoreet.libero@Aliquamadipiscing.ca",
	        		"birthday": "2014-07-10",
	        		"zip": "74-869",
	        		"salary": 205358,
	        		"absence": 40,
	        		"country": "Gambia",
	        		"city": "Coldstream",
	        		"company": "Ac Fermentum Vel PC",
	        		"type": 7
	        	},
	        	{
	        		"name": "Vera",
	        		"phone": "138-955-8109",
	        		"email": "eu.odio@maurisblanditmattis.ca",
	        		"birthday": "2015-07-18",
	        		"zip": "467025",
	        		"salary": 787197,
	        		"absence": 50,
	        		"country": "Bahamas",
	        		"city": "Serrungarina",
	        		"company": "Suspendisse Dui Fusce PC",
	        		"type": 2
	        	},
	        	{
	        		"name": "Autumn",
	        		"phone": "369-405-2973",
	        		"email": "Quisque@loremsemper.com",
	        		"birthday": "2014-05-03",
	        		"zip": "370659",
	        		"salary": 168857,
	        		"absence": 100,
	        		"country": "Suriname",
	        		"city": "Eigenbrakel",
	        		"company": "Nibh Dolor Nonummy LLC",
	        		"type": 9
	        	},
	        	{
	        		"name": "Amy",
	        		"phone": "693-6192",
	        		"email": "sit@egetmagnaSuspendisse.ca",
	        		"birthday": "2015-08-06",
	        		"zip": "2794",
	        		"salary": 235913,
	        		"absence": 100,
	        		"country": "Cocos (Keeling) Islands",
	        		"city": "Chartres",
	        		"company": "Tincidunt Consulting",
	        		"type": 1
	        	},
	        	{
	        		"name": "Thor",
	        		"phone": "569-6512",
	        		"email": "parturient.montes.nascetur@imperdietnon.edu",
	        		"birthday": "2014-01-17",
	        		"zip": "26962",
	        		"salary": 853938,
	        		"absence": 10,
	        		"country": "Spain",
	        		"city": "Beho",
	        		"company": "Nulla LLC",
	        		"type": 10
	        	},
	        	{
	        		"name": "Dakota",
	        		"phone": "270-477-0444",
	        		"email": "sed.pede@tortordictumeu.edu",
	        		"birthday": "2015-06-17",
	        		"zip": "7374PU",
	        		"salary": 575048,
	        		"absence": 10,
	        		"country": "Bonaire, Sint Eustatius and Saba",
	        		"city": "Yahyalı",
	        		"company": "Parturient Montes Nascetur Inc.",
	        		"type": 1
	        	},
	        	{
	        		"name": "Cooper",
	        		"phone": "610-9420",
	        		"email": "placerat.Cras.dictum@eratVivamus.net",
	        		"birthday": "2015-07-20",
	        		"zip": "73706",
	        		"salary": 170683,
	        		"absence": 100,
	        		"country": "New Caledonia",
	        		"city": "Deline",
	        		"company": "Nulla Tincidunt Neque Ltd",
	        		"type": 2
	        	},
	        	{
	        		"name": "Bethany",
	        		"phone": "949-378-7845",
	        		"email": "erat@Morbiaccumsanlaoreet.edu",
	        		"birthday": "2014-12-16",
	        		"zip": "83245-257",
	        		"salary": 998642,
	        		"absence": 1,
	        		"country": "Poland",
	        		"city": "Milwaukee",
	        		"company": "Vel Sapien Company",
	        		"type": 1
	        	},
	        	{
	        		"name": "Ronan",
	        		"phone": "503-6165",
	        		"email": "pellentesque@egestasDuisac.co.uk",
	        		"birthday": "2015-03-23",
	        		"zip": "1124",
	        		"salary": 38416,
	        		"absence": 8,
	        		"country": "Aruba",
	        		"city": "Calgary",
	        		"company": "Donec PC",
	        		"type": 7
	        	},
	        	{
	        		"name": "Lamar",
	        		"phone": "244-4542",
	        		"email": "enim.sit@nonarcu.com",
	        		"birthday": "2015-07-29",
	        		"zip": "18214",
	        		"salary": 106591,
	        		"absence": 9,
	        		"country": "Palau",
	        		"city": "Portland",
	        		"company": "Tristique Pharetra Quisque Corp.",
	        		"type": 4
	        	},
	        	{
	        		"name": "Gisela",
	        		"phone": "203-196-7156",
	        		"email": "porttitor.tellus@Sed.org",
	        		"birthday": "2014-03-20",
	        		"zip": "587048",
	        		"salary": 255047,
	        		"absence": 300,
	        		"country": "Italy",
	        		"city": "Aberystwyth",
	        		"company": "Augue Eu Tempor Associates",
	        		"type": 6
	        	},
	        	{
	        		"name": "Nolan",
	        		"phone": "694-889-2611",
	        		"email": "Etiam.laoreet.libero@Aliquamadipiscing.ca",
	        		"birthday": "2014-07-10",
	        		"zip": "74-869",
	        		"salary": 205358,
	        		"absence": 10,
	        		"country": "Gambia",
	        		"city": "Coldstream",
	        		"company": "Ac Fermentum Vel PC",
	        		"type": 7
	        	}
	        ];
	
    // 초기화 함수
    this.init = function(id, param) {
        gridInit();
    };
    
    
    function gridInit(){
    	$('#gr').alopexGrid({
    		pager: true,
    		paging: {
    			perPage: 10,
    			pagerCount: 5,
    			pagerTotal: true,
    			pagerSelect: true,
    		},
    		autoColumnIndex: true,
    		columnMapping : [
    			{
    				align : 'center',
    				key : 'check',
    				width : '30px',
    				selectorColumn : true
    			}, {
    				key : 'name',
    				title : 'name',
    				width : '100px'
    			}, {
    				key : 'phone',
    				title : 'phone',
    				width : '100px'
    			}, {
    				key : 'email',
    				title : 'email',
    				width : '250px'
    			}, {
    				align : 'center',
    				key : 'birthday',
    				title : 'birthday',
    				width : '100px'
    			}, {
    				align : 'center',
    				key : 'zip',
    				title : 'zip',
    				width : '100px'
    			}, {
    				align : 'right',
    				key : 'salary',
    				title : 'salary',
    				width : '100px'
    			}, {
    				key : 'country',
    				title : 'country',
    				width : '200px'
    			}, {
    				key : 'city',
    				title : 'city',
    				width : '150px'
    			}, {
    				key : 'company',
    				title : 'company',
    				width : '200px'
    			}, {
    				key : 'type',
    				align: 'center',
    				title : 'type',
    				width : '50px'
    			}
    		],
    		data: data
    	});
    	
    	$('#gr2').alopexGrid({
    		height: 200,
    		autoColumnIndex: true,
    		columnMapping : [
    			{
    				align : 'center',
    				width : 30,
    				numberingColumn : true
    			}, {
    				key : 'name',
    				title : 'name',
    				width : 100
    			}, {
    				key : 'phone',
    				title : 'phone',
    				width : 100
    			}, {
    				key : 'email',
    				title : 'email',
    				width : 250
    			}, {
    				align : 'center',
    				key : 'birthday',
    				title : 'birthday',
    				width : 100
    			}, {
    				align : 'center',
    				key : 'zip',
    				title : 'zip',
    				width : 100
    			}
    		],
    		data: data
    	});
    	
    	
    	$('#gr-tree').alopexGrid({
    		renderMapping : {
    			"string" : {
    				renderer:  function(value, data, render, mapping, grid) {
    					return "$" + value;
    				}
    			},  
    			"bargraph" : {
    				//render : {type:"bargraph",rule:[min,max]}
    				renderer : function(value, data, render, mapping, grid) {
    					var rule = render.rule;
    					var percentage = 0;
    					
    					if(rule) {
    						var min = rule[0];
    						var max = rule[1];
    						if(typeof min === "string") {
    							min = parseFloat(data[min]);
    						}
    						if(typeof max === "string") {
    							max = parseFloat(data[max]);
    						}
    						if(typeof min === "number" && typeof max === "number") {
    							percentage = parseInt((value-min)/(max-min)*100);
    							percentage = Math.min(Math.max(0,percentage),100);
    						}
    					}
    					return '<div class="progress_container"><div class="percentage-bar" style="width:'+percentage+'%;"></div></div>';
    				}
    			}
    		},
    		height: 400,
    		autoColumnIndex: true,
    		columnMapping : [
    			{
    				align : 'center',
    				key : 'check',
    				width : '30px',
    				selectorColumn : true
    			}, {
    				key : 'name',
    				title : 'name',
    				render : function( value, data, render, mapping, grid) {
    					if (value.length > 3) {
    						return value.substring(0,3) + "**";
    					} else {
    						return value;
    					}
    				},
    				tooltip : function( value, data, render, mapping, grid) {
    					if (value.length > 3) {
    						return value.substring(0,3) + "**";
    					} else {
    						return value;
    					}
    				},
    				width : '100px'
    			}, {
    				key : 'phone',
    				title : 'phone',
    				width : '100px'
    			}, {
    				key : 'email',
    				title : 'email',
    				width : '250px'
    			}, {
    				align : 'center',
    				key : 'birthday',
    				title : 'birthday',
    				width : '100px'
    			}, {
    				align : 'center',
    				key : 'zip',
    				title : 'zip',
    				width : '100px'
    			}, {
    				align : 'right',
    				key : 'salary',
    				title : 'salary',
    				width : '100px',
    				render : {type : "string"}
    			}, {
    				key : 'absence',
    				title : 'absence',
    				width : '200px',
    				render : {type:"bargraph",rule:[0,365]} //숫자로 퍼센트 계산을 위한 min/max 지정 
    			}, {
    				key : 'city',
    				title : 'city',
    				width : '150px' 
    			}, {
    				key : 'company',
    				title : 'company',
    				width : '200px'
    			}, {
    				key : 'type',
    				align : 'center',
    				title : 'type',
    				width : '50px'
    			} 
    		],
    		data : data
    	});
    	
    }
});