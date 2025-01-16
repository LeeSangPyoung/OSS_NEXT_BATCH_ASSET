$(document).ready(function(){
	$('.search-list__wrap').hide();
	$('.search-title label, .search-toggle').click(function(e){
		e.preventDefault();
		$('.search-toggle').toggleClass('search-toggle__up');
		$('.search-list__wrap').slideToggle('fast');
	});
	
	//left menu
	/*$('.left-wrap__close').hide();*/
	$('.left-close').click(function(e){
		setLeftCloseClick();
		$a.session('menu_open', false); //메뉴상태 저장
	});
	
	$('.left-open').click(function(e){
		setLeftOpenClick();
		$a.session('menu_open', true); //메뉴상태 저장
	});
	
	//selectMenuItem();
	// 대메뉴 로딩 하기
	$('.leftmenu > li > ul').hide();
	$('.leftmenu > li:eq('+ $a.session('menu_no1') + ')').find(' > ul').slideDown();
	$('.leftmenu > li:eq('+ $a.session('menu_no1') + ')').addClass('Expanded');
	$('.leftmenu > li:eq('+ $a.session('menu_no2') + ')').find(' > ul').slideDown();
	$('.leftmenu > li:eq('+ $a.session('menu_no2') + ')').addClass('Expanded');

	//left slide menu
	$('.leftmenu > li > a').click(function(e){
		e.preventDefault();

		// 메뉴선택 표시
		$('.leftmenu > li:eq('+ $a.session('menu_no1') + ')').removeClass('Expanded');
		$thisParent = $(this).parent();
		$thisParent.addClass('Expanded');
		$thisParent.find(' > ul').slideDown();
		
		// 메뉴 인덱스 저장
		if($a.session('menu_open') == 'true') {
			$a.session('menu_no1', $('.leftmenu > li').index($thisParent));
			$a.session('menu_no2', $('.leftmenu > li').index($thisParent) + 6);
		} else {
			$a.session('menu_no1', $('.leftmenu > li').index($thisParent) - 6);
			$a.session('menu_no2', $('.leftmenu > li').index($thisParent));
		}
		
		var url = $(this).attr('href');
		location.href = url;
	});
	
	// 서브메뉴 로딩 하기
	var $leftmenuSubLiA = $('.leftmenu > li > ul > li > a');
	$('.leftmenu > li.Expanded > ul > li:eq('+ $a.session('menu_no3') + ')').addClass('Expanded');
	
	$($leftmenuSubLiA).click(function(e){
		e.preventDefault();
		$a.session('menu_no3', $leftmenuSubLiA.index(this)); //메뉴 인덱스 저장
		
		var url = e.target.href;
		if(url == null || url == '' || url == 'undefined') {//2 level에서 URL이 있을 경우에는 URL을 호출하도록 함. 
			$(this).addClass('Expanded');
			var $thisParent = $(this);
			if($thisParent.hasClass('Expanded')){
				$thisParent.find(' > ul').slideDown();
				$thisParent.siblings('li').removeClass('Expanded').find(' > ul').slideUp();
			}
		} else {
			//a태그에 있는 url로 호출됨
			location.href=url;
		}
	});
	
	//top profile menu
	var $btnProfile = $('.btn-profile');
	var $profile = $('.profile-wrap');
	/*$profile.hide();*/
	
	$($btnProfile).click(function(e){
		e.preventDefault();
		$($profile).slideToggle();
		
		var $profileMenu = $('.profile-menu a');
		$($profileMenu).click(function(e){
			$profile.hide();
		});
	});
});

function setLeftOpenClick(){
	$('body').removeClass('left-bg__simple').addClass('left-bg__full');
	$('.left-wrap__close').hide();
	$('.left-wrap').show();
	$('.content-wrap').removeClass('menu-close__wrap');
}

function setLeftCloseClick() {
	$('body').removeClass('left-bg__full').addClass('left-bg__simple');
	$('.left-wrap').hide();
	$('.left-wrap__close').show();
	$('.content-wrap').addClass('menu-close__wrap');
	
	if($('.leftmenu > li > a').parents().hasClass('left-wrap__close')){
		$('.left-wrap__close .leftmenu > li > ul').hide();
		$('.leftmenu > li > a').click(function(e){
			setLeftOpenClick();
		});
//		$('.btn-lng').click(function(e){
//			setLeftOpenClick();
//		});
	}
}

/**
 * 레이어팝업창 열기
 ******* option 정보 *******
 * title - 다이얼로그의 제목
 * width - 다이얼로그의 넓이값
 * height - 다이얼로그의 높이값
 * left, top - 다이얼로그 위치값
 * type - 다이얼로그 타입설정. "null" 값 설정 시 다이얼로그 헤더 없음
 * resizable - 다이얼로그 크기조절 가능유무 설정
 * scroll - data-dialog-scroll 영역의 스크롤 가능유무 설정
 * movable - 다이얼로그 위치조정 가능유무 설정
 * animation(show/fade/slide) - 다이얼로그 종료시 적용되는 애니메이션 설정
 * animationtime - 애니메이션 동작 시간 설정
 * modal - 다이얼로그 open 시 배경에 modal 유무 설정
 * modalscrollhidden - 다이얼로그 modal 상태에서 세로 스크롤바 유무 설정
 * toggle - 다이얼로그 접고, 펴기 토글 버튼 생성
 */
function layerPopOpen(buttonId, dialogId, title, width, height, top, type, resizable, movable, modal, animation, animationtime, scroll, toggle) {
   	//$('#'+buttonId).bind('click', function() {
   	$('#'+buttonId).click(function() {
   		if(buttonId == null || buttonId == "") { alert("buttonId는 필수입니다."); 	return false; }
   		if(dialogId == null || dialogId == "") { alert("dialogId는 필수입니다."); 	return false; }
   		if(title 	== null || title 	== "") { alert("title은 필수입니다."); 		return false; }
   		if(width 	== null || width 	== "") { alert("width는 필수입니다."); 		return false; }
   		if(height 	== null || height 	== "") { alert("height는 필수입니다."); 		return false; }	  
   		var xPos = (window.screen.width) ? (window.screen.width - width) /2 : 0;
     	$('#'+dialogId).open({
          title 		: title,
          //left 			: xPos,
          //top			: top,
          width			: width,
          height		: height,
          type			: type,
          resizable		: resizable,
          movable		: movable,
          modal			: modal,
          animation		: animation,
          animationtime	: animationtime,
          scroll		: scroll,
          toggle		: toggle
       	});            
     }); 
}
