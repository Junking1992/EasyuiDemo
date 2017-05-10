function upLoad(id) {
	var filePath = $("#" + id).textbox('getValue');
	var fileName = filePath.substring(filePath.lastIndexOf('\\') + 1);
	if (fileName == "") {
		$.messager.alert('警告', "请选择Excel!");
		return;
	}
	$.post("upload", {
		fileName : fileName,
		action : id
	});
	$(".easyui-linkbutton").linkbutton({
		disabled : true
	});
	$("#msg_" + id).hide();
	$("#progressbar_" + id).show();
	$("#content").empty();
	progress(id);
}

function autoUpLoad(id) {
	$.post("upload", {
		fileName : '',
		action : id
	});
}

function progress(id) {
	$('<audio id="chatAudio"> <source src="audio/xunlei.mp3" type="audio/mpeg"> </audio>').appendTo('body');
	setTimeout(function() {
		$.get("upload",
				function(data, status) {
					if (status == "success") {
						if (data.startsWith("Msg")) {
							$(".easyui-linkbutton").linkbutton({
								disabled : false
							});
							$("#progressbar_" + id).hide();
							$("#progressbar_" + id).progressbar('setValue',0);
							$("#msg_" + id).show();
							$("#content").empty();
							$("#content").append(data.substring(3));
							$('#chatAudio')[0].play();
						} else {
							$("#progressbar_" + id).progressbar('setValue',data.substring(0,data.indexOf(':')));
							$("#content").empty();
							$("#content").append(data.substring(data.indexOf(':')+1));
							progress(id);
						}
					}
				});
	}, 1000);
}
