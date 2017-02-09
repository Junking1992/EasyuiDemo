/**
 * 引入jkjs001.js
 */ 
document.write("<script language='javascript' src='js/jkjs001.js'></script>");

/**
 * 打开新tab
 * 
 * @param tabsId
 *            父选项卡Id
 * @param title
 *            新标签Title
 * @param href
 *            打开链接
 */
function addTab(tabsId, title, href) {
	if ($('#' + tabsId).tabs('exists', title)) {
		$('#' + tabsId).tabs('select', title);
	} else {
		$('#' + tabsId).tabs('add', {
			title : title,
			href : href,
			closable : true,
		});
		// 使用异步确认对话框
		$('#' + tabsId).tabs({
			onBeforeClose : function(title, index) {
				var target = this;
				$.messager.confirm('确认', '你确认想要关闭' + title + '?', function(c) {
					if (c) {
						var opts = $(target).tabs('options');
						var bc = opts.onBeforeClose;
						opts.onBeforeClose = function() {
						}; // 允许现在关闭
						$(target).tabs('close', index);
						opts.onBeforeClose = bc; // 还原事件函数
					}
				});
				return false; // 阻止关闭
			}
		});
	}
}
