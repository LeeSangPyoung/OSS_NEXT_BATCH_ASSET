//Multi-select 컴포넌트 

$a.setup({
    defaultComponentClass: {
    	multiSelect: 'MultiSelect',
    	splitter: 'Splitter'
    }
});

$a.widget.multiSelect = $a.inherit($a.widget.object, {
	widgetName: 'multiSelect',
	setters: ['multipleselect', 'refresh'],  
	getters: ['isOpen', 'getChecked', 'getButton', 'widget'],
	
	properties: {
	  multiple: true,
	  noneSelectedText: "선택하세요",
	  header: true,
	  minWidth: 180,
	  selectedList: 2,
	  checkAllText: '전체선택',
	  uncheckAllText: '전체해제',
	  selectedText: '#개 선택됨',
	  classes: 'MultiSelect',
	  filter: true,
	  label: '필터',
	  placeholder: '검색어를 입력하세요'
	},
	
	init: function(el, options) { // 새로운 컴포넌트의 동작이나 마크업등을 설정하는 부분입니다. 사용자는 $el을 이용하여 커스텀하게 마크업, 스타일등을 만들어낼 수 있습니다.
		  var $el = $(el);
		  $el.attr('multiple', 'multiple');
		  el.opts = $.extend(true, {}, this.properties, options);
		  if (el.opts.filter){
			  $el.multiselect(el.opts).multiselectfilter(el.opts);
		  } else{
			  $el.multiselect(el.opts);
		  }
		  
		  return;
	},
	refresh: function(el) {
		  $(el).multiselect('refresh');
	}
});

//splitter_panel
$a.widget.splitter = $a.inherit($a.widget.object, {
	widgetName: 'splitter',
	properties: {
		position: '50%',
		limit: 10,
		orientation: 'horizontal'
	},
	init: function(el, options){
		var opts = $.extend(true, {}, this.properties, options);
		$(el).split({
		    orientation: opts.orientation,
		    limit: opts.limit,
		    position: opts.position
		});
	}
});
