var appid;
var noncestr;
var timestamp;
var signature;

var shareUrl;

var wxtitle='';
var wxlink = '';
var wximgUrl = '';
var wxdesc = '';

function init(shareUrl,wxtitle,wxlink,wximgUrl,wxdesc){
    this.shareUrl = shareUrl;
    this.wxtitle = wxtitle;
    this.wxlink = wxlink;
    this.wximgUrl = wximgUrl;
    this.wxdesc = wxdesc;

    initWxConfig();
}

function initWxConfig(){
    $.ajax({
        url: "http://115.28.47.57:8080/taida/wx/config",
        data: {
            url : shareUrl
        },
        success: function( result ) {
            var r = result.split(",");
            appid = r[0];
            noncestr = r[1];
            timestamp = r[2];
            signature = r[3];

            wx.config({    
                debug: false,
                appId: appid,
                timestamp: timestamp,
                nonceStr: noncestr,
                signature: signature,
                jsApiList: ['checkJsApi','onMenuShareTimeline','onMenuShareAppMessage','onMenuShareQQ','onMenuShareWeibo'] // 必填，需要使用的JS接口列表，所有JS接口列表见附录2    
            });    
        }
    });

    wx.ready(function(){
        resetWxP();
    });
};

function resetWxP(){
            // alert("->"+shareUrl+"|"+wxtitle+"|"+wxlink+"|"+wximgUrl+"|"+wxdesc);
            wx.onMenuShareTimeline({    
            title: wxtitle, // 分享标题    
            link: wxlink, // 分享链接    
            imgUrl: wximgUrl, // 分享图标    
            success: function () {
                shareend();
            },    
            cancel: function () {
                shareend();   
            }    
        }); 

        wx.onMenuShareAppMessage({
            desc: wxdesc, // 分享描述
            title: wxtitle, // 分享标题    
            link: wxlink, // 分享链接    
            imgUrl: wximgUrl, // 分享图标  
            type: '', // 分享类型,music、video或link，不填默认为link
            dataUrl: '', // 如果type是music或video，则要提供数据链接，默认为空
            success: function () { 
                shareend();
            },
            cancel: function () { 
                shareend();
            }
        });
        wx.onMenuShareWeibo({
                desc: wxdesc, // 分享描述
                title: wxtitle, // 分享标题    
                link: wxlink, // 分享链接    
                imgUrl: wximgUrl, // 分享图标  
                success: function () { 
                shareend();
                },
                cancel: function () { 
                    shareend();
                }
        });

        wx.error(function(res){   
            shareend(); 
        });  


}

function shareend(){
};

 