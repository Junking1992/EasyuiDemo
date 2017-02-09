<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta charset="utf-8">
<title>System</title>
<link rel="stylesheet" type="text/css" href="jquery-easyui-v1.5/themes/metro/easyui.css">
<link rel="stylesheet" type="text/css" href="jquery-easyui-v1.5/themes/icon.css">
<link rel="stylesheet" type="text/css" href="jquery-easyui-v1.5/demo/demo.css">
<script src="jquery-easyui-v1.5/jquery.min.js"></script>
<script src="jquery-easyui-v1.5/jquery.easyui.min.js"></script>
<script src="jquery-easyui-v1.5/locale/easyui-lang-zh_CN.js"></script>
<script src="js/main.js"></script>
</head>
<body class="easyui-layout">
	<div data-options="region:'north'" style="height: 29px; background: #fff;">
		<a href="#" class="easyui-menubutton" data-options="menu:'#mm1'">编辑</a>
		<a href="#" class="easyui-menubutton" data-options="menu:'#mm2'">帮助</a>
		<a href="#" class="easyui-menubutton" data-options="menu:'#mm3'">关于</a>
	</div>
	<div data-options="region:'center'">
		<div id="tab" class="easyui-tabs" data-options="fit:true,border:false">
			<div title="功能导航" style="padding: 4px" href="jsp/function.jsp">
			</div>
		</div>
	</div>
	
	<div id="mm1" style="width:150px;">
		<div data-options="iconCls:'icon-undo'">Undo</div>
		<div data-options="iconCls:'icon-redo'">Redo</div>
		<div class="menu-sep"></div>
		<div>Cut</div>
		<div>Copy</div>
		<div>Paste</div>
		<div class="menu-sep"></div>
		<div>
			<span>Toolbar</span>
			<div>
				<div>Address</div>
				<div>Link</div>
				<div>Navigation Toolbar</div>
				<div>Bookmark Toolbar</div>
				<div class="menu-sep"></div>
				<div>New Toolbar...</div>
			</div>
		</div>
		<div data-options="iconCls:'icon-remove'">Delete</div>
		<div>Select All</div>
	</div>
	<div id="mm2" style="width:100px;">
		<div>Help</div>
		<div>Update</div>
		<div>About</div>
	</div>
	<div id="mm3" class="menu-content" style="background:#f0f0f0;padding:10px;text-align:left">
		<img src="http://www.jeasyui.com/images/logo1.png" style="width:150px;height:50px">
		<p style="font-size:14px;color:#444;">Try jQuery EasyUI to build your modern, interactive, javascript applications.</p>
	</div>
</body>
</html>
