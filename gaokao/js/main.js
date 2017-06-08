//自定义tap
$(document).on("touchstart", function(e) {
    if(!$(e.target).hasClass("disable")) $(e.target).data("isMoved", 0);
});
$(document).on("touchmove", function(e) {
    if(!$(e.target).hasClass("disable")) $(e.target).data("isMoved", 1);
});
$(document).on("touchend", function(e) {
    if(!$(e.target).hasClass("disable") && $(e.target).data("isMoved") == 0) $(e.target).trigger("tap");
});

var serverUrl = "http://115.28.47.57:8080/taida/wx/";
/**
 * Description:[输出指定n位数的随机数的随机整数]
 *
 * @param n  指定n位数
 *
 */
function RndNum(n) {
    var rnd = "";
    for (var i = 0; i < n; i++) {
        rnd += Math.floor(Math.random() * 10);
    }
    return "201706"+rnd;
};

var name = '';
var zhunkaozhenghao = RndNum(7);
/**
 * Description:[实现翻题的操作]
 *
 * @param d  0 上一页 1 下一页
 *
 */
function changeTi(n,b){
    $('#'+n).hide();
    $('#'+b).show();
};


function validateName(desc){
    $('#mask').html(desc);
    $('#mask').show();
    $('#mask2').show();
};

var ti1DaAn=0;
var ti2DaAn=0;
var ti3DaAn=0;
var ti4DaAn=0;
var ti5DaAn=0;
var ti6DaAn=0;
var daanArray = new Array();
daanArray['1'] = ti1DaAn;
daanArray['2'] = ti2DaAn;
daanArray['3'] = ti3DaAn;
daanArray['4'] = ti4DaAn;
daanArray['5'] = ti5DaAn;
daanArray['6'] = ti6DaAn;

function setName() {
    for (var i=1;i<7;i++)
    {
         $('#n'+i).html(name);
         $('#z'+i).html(zhunkaozhenghao);
    }
};

function getDaAn(iid){    
    return daanArray[iid];
};
function setDaAn(id,iid){
    daanArray[id] = iid;
};
function validateDaAn(id){
    var daan = getDaAn(id);
    if(daan<=0){
        $('#tishi'+id).show();
        return false;
    }else{
        return true;
    }
};

function randomSetWxConteng(){
	var titleArray = new Array("2017过来人高考，你随便做，得到满分算我输！","2017过来人高考，证明你颜值与才华齐飞的时刻到了！");
	var descArray = new Array("听说大神级别30秒答完！","听说大神级别30秒答完！");

	var index = parseInt(Math.random()*2);
	wxtitle = titleArray[index];
	wxdesc = descArray[index];
};

function randomSetWxContentByFen(fen){
    if(fen>=600 && fen<=750){
        wxtitle = '我的2017高考成绩是'+fen+'分，我就问还有谁？';
	    wxdesc = '2017过来人高考，你随便做得到满分算我输！';
    }else if(fen>=350 && fen<=550){
        wxtitle = '我的2017高考成绩是'+fen+'分，我就随便做做而已！';
	    wxdesc = '2017过来人高考，你随便做得到满分算我输！';
    }else{
         wxtitle = '我的2017高考成绩居然是'+fen+'分，扶我起来我还要考！';
	    wxdesc = '2017过来人高考，你随便做得到满分算我输！';
    }
};

function panjun(){
            var yuwenCJ = daanArray['1']==13? 150 : 0;
			var shuxueCJ = daanArray['2']==21? 150 : 0;
			var yingyuCJ = daanArray['3']==33? 150 : 0;
			var wenzongCJ = daanArray['4']==42? 100 : 0;
			var lizongCJ = daanArray['5']==53? 100 : 0;
			var jiafenCJ = daanArray['6']==64? 100 : 0;
			
			var n = (yuwenCJ+shuxueCJ+yingyuCJ+wenzongCJ+lizongCJ+jiafenCJ);
            return n;
};

window.onload = function () {
// 初始化微信
  randomSetWxConteng();
  init(location.href,wxtitle,location.href,"http://taida.laiqu.cn/gaokao/share.jpg",wxdesc);

   $('ul li').on('tap',
    function(e){
        var iid = this.id;
        var id = parseInt(iid/10);
        $('#'+iid).addClass("right");
        var localDaAn = getDaAn(id);
        if(iid==localDaAn){return;}
        $('#'+localDaAn).removeClass("right");
        setDaAn(id,iid);
        $('#tishi'+id).hide();
   })

   $('.gk-next2').on('tap',
    function(e){
        var id = this.id;
        var tempn = id - 100;
        if(!validateDaAn(tempn)){
            return;
        }
        var tempb = id -100 +1;
        changeTi(tempn,tempb);
   });

   $('.gk-last').on('tap',
    function(e){
        var id = this.id;
        var tempn = id - 100;
        var tempb = id -100 -1;
        changeTi(tempn,tempb);
   });

    $('.gk-next').on('tap',
    function(e){
        var id = this.id;
        var tempn = id - 100;
         if(!validateDaAn(tempn)){
            return;
        }
        var tempb = id -100 +1;
        changeTi(tempn,tempb);
   });

//重新考试
$('.continue').on('tap',
    function(e){
        location.href = 'http://taida.laiqu.cn/gaokao/index.html';
   });

//不考了
$('.exit').on('tap',
    function(e){
        location.href = 'https://d.elong.com/a/0602';
   });

//    开始
$('#start').on('tap',
    function(e){
        
    name = $('#name').val();
     $('#mask').hide();
     $('#mask2').hide();
     //验证长度等合法性
     if(name.length<=0){
         validateName('请填写您的姓名');
         return;
     }else if(name.length>4 || name.length<1){
         validateName('要求姓名长度为1-4位');
         return;
     }

    $.ajax({
        url: serverUrl+"filter",
        data: {
            n : name
        },
        async: false,
        success: function( result ) {
            if(result==1){
                validateName('姓名中含有不当词语，请重新输入哦');
                return;
            }else{
                //切到答题页面
                $('#name').blur();
                $('#1').show();
                $('#main').hide();

                setName();
            }
        }
});
   });

   // 交卷
    $('.gk-save').on('tap',
    function(e){
        if(!validateDaAn(6)){
            return;
        }
        $('#6').hide();
        $('#loading').show();

       $.ajax({
        url: serverUrl+"gen",
        data: {
            n : name,
            z : zhunkaozhenghao,
            yuwen : getDaAn(1),
            shuxue : getDaAn(2),
            yingyu : getDaAn(3),
            wenzong : getDaAn(4),
            lizong : getDaAn(5),
            jiafen : getDaAn(6)
        },
        async: false,
        success: function( result ) {
            var img = new Image();
            var imgpath = "http://taida.laiqu.cn/gaokao/cj/"+result;
            img.onload =function(){  
                img.onload =null;
                $('#loading').hide();
                $('#chengjidan').attr('src',imgpath);
                $('#chengji').show();
            }
            img.onerror =function(){  
                img.onload =null;
                $('#loading').hide();
                $('#chengjidan').attr('src','img/chengji2.jpg');
                $('#chengji').show();
            }
            img.src = imgpath;

            //重新设置分享文案
            var zongfen = panjun();
            randomSetWxContentByFen(zongfen);
            resetWxP();
        },
        error: function(XMLHttpRequest, textStatus, errorThrown) {
            $('#chengjidan').attr('src','img/chengji2.jpg');
            $('#loading').hide();
             $('#chengji').show();
        }
});
   });
   
};