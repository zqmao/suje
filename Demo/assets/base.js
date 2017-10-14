var base = {};
//当前用户ID
base.ownerId = "48";
//当前文件夹 root
base.parent = 0;
//token信息
base.token = "Onebox/BA84668F6CE69F6F9AFBA8BDD5DE41E30112DCA85A6897A0F2168696";
//预上传URL
base.uploadUrl = "https://pan8.csibox.cn/api/v2/files/"+base.ownerId;
base.uploadRefreshUrl = "https://pan8.csibox.cn/api/v2/files/:ownerId/:fileId/refreshurl";
//获取下载URL
base.downloadUrl = "https://pan8.csibox.cn/api/v2/files/"+base.ownerId;

function isIOS_(){
	var u = navigator.userAgent;
    var isiOS = !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/); //ios终端
	if(isiOS){
		return true;
	}
	
	return false;
}

var iSuje_ = {
	isIOS : isIOS_(),
	getNetwork:function(successFun){
        if(iSuje_.isIOS){
            window.webkit.messageHandlers.iSuje.postMessage({methodUser:"iSuje",methodName:"getNetwork",callback1:successFun});
        }else{
            iSuje.getNetwork(successFun);
        }
    },
	updateStatus:function(fileId,status,isDownload,successFun){
        if(iSuje_.isIOS){
            window.webkit.messageHandlers.iSuje.postMessage({methodUser:"iSuje",methodName:"updateStatus",callback1:fileId,callback2:status,callback3:isDownload,callback4:successFun});
        }else{
            iSuje.updateStatus(fileId,status,isDownload,successFun);
        }
    },
	getImageBase64:function(url,id,successFun,errorFun){
		if(iSuje_.isIOS){
			window.webkit.messageHandlers.iSuje.postMessage({methodUser:"iSuje",methodName:"getImageBase64",callback1:url,callback2:id,callback3:successFun,callback4:errorFun});
        }else{
            iSuje.getImageBase64(url,id,successFun,errorFun);
        }
    },
	uploadInject:function(successFun,errFun){
		if(iSuje_.isIOS){
			window.webkit.messageHandlers.iSuje.postMessage({methodUser:"iSuje",methodName:"uploadInject",callback1:successFun,callback2:errFun});
		}else{
			iSuje.uploadInject(successFun,errFun);
		}
	},
	getUploadFiles:function(thirdExterpriseId, pageNum, pageSize, successFun,errFun){
		if(iSuje_.isIOS){
			window.webkit.messageHandlers.iSuje.postMessage({methodUser:"iSuje",methodName:"getUploadFiles",callback1:successFun,callback2:errFun});
		}else{
			iSuje.getUploadFiles(thirdExterpriseId, pageNum, pageSize,successFun,errFun);
		}
	},
	upload:function(thirdExterpriseId,config,successFun,errFun){
		if(iSuje_.isIOS){
			window.webkit.messageHandlers.iSuje.postMessage({methodUser:"iSuje",methodName:"upload",callback1:config,callback2:successFun,callback3:errFun});
		}else{
			iSuje.upload(thirdExterpriseId,config,successFun,errFun);
		}
	},
	uploadPause:function(fileId,h5,successFun,errFun){
		if(iSuje_.isIOS){
			window.webkit.messageHandlers.iSuje.postMessage({methodUser:"iSuje",methodName:"uploadPause",callback1:fileId,callback2:h5,callback3:successFun,callback4:errFun});
		}else{
			iSuje.uploadPause(fileId,h5,successFun,errFun);
		}
	},
	uploadDelete:function(fileId,h5,successFun,errFun){
		if(iSuje_.isIOS){
			window.webkit.messageHandlers.iSuje.postMessage({methodUser:"iSuje",methodName:"uploadDelete",callback1:fileId,callback2:h5,callback3:successFun,callback4:errFun});
		}else{
			iSuje.uploadDelete(fileId,h5,successFun,errFun);
		}
	},


	downLoadInject:function(successFun,errFun){
		if(iSuje_.isIOS){
			window.webkit.messageHandlers.iSuje.postMessage({methodUser:"iSuje",methodName:"downLoadInject",callback1:successFun,callback2:errFun});
		}else{
			iSuje.downLoadInject(successFun,errFun);
		}
	},
	getDownLoadFiles:function(thirdExterpriseId, pageNum, pageSize,successFun,errFun){
		if(iSuje_.isIOS){
			window.webkit.messageHandlers.iSuje.postMessage({methodUser:"iSuje",methodName:"getDownLoadFiles",callback1:successFun,callback2:errFun});
		}else{
			iSuje.getDownLoadFiles(thirdExterpriseId, pageNum, pageSize,successFun,errFun);
		}
	},
	download:function(thirdExterpriseId,config,successFun,errFun){
		if(iSuje_.isIOS){
			window.webkit.messageHandlers.iSuje.postMessage({methodUser:"iSuje",methodName:"download",callback1:config,callback2:successFun,callback3:errFun});
		}else{
			iSuje.download(thirdExterpriseId,config,successFun,errFun);
		}
	},
	downLoadPause:function(fileId,h5,successFun,errFun){
		if(iSuje_.isIOS){
			window.webkit.messageHandlers.iSuje.postMessage({methodUser:"iSuje",methodName:"downLoadPause",callback1:fileId,callback2:h5,callback3:successFun,callback4:errFun});
		}else{
			iSuje.downLoadPause(fileId,h5,successFun,errFun);
		}
	},
	downLoadDelete:function(fileId,h5,successFun,errFun){
		if(iSuje_.isIOS){
			window.webkit.messageHandlers.iSuje.postMessage({methodUser:"iSuje",methodName:"downLoadDelete",callback1:fileId,callback2:h5,callback3:successFun,callback4:errFun});
		}else{
			iSuje.downLoadDelete(fileId,h5,successFun,errFun);
		}
	},
}