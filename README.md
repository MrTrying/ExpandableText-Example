# ExpandableText-Example


----------
## 一.简介

仿小红书实现的文本展开/收起的功能


## 二.方法说明

在设置文本之前，必须手动调用`initWidth`初始化文本显示的宽

|方法					|说明						|
| :---- 				| :---- 					|
|`initWidth(int width)`	|**初始化`ExpandableText`宽度，必须在`setOriginalText()`之前调用**		|
|`setMaxLines(int maxLines)`|设置最多显示行数|
|`setOpenSuffix(String openSuffix)`|设置**需要展开**时显示的文字，默认为`展开`|
|`setOpenSuffixColor(@ColorInt int openSuffixColor)`|设置**需要展开**时显示的文字的文字颜色|
|`setCloseSuffix(String closeSuffix)`|设置**需要收起**时显示的文字，默认为`收起`|
|`setCloseSuffixColor(@ColorInt int closeSuffixColor)`|设置**需要收起**时显示的文字的文字颜色|
|`setCloseInNewLine(boolean closeInNewLine)`|设置**需要收起**时收起文字是否另起一行|
|`setOpenAndCloseCallback(OpenAndCloseCallback callback)`|设置展开&收起的点击Callback|
|`setCharSequenceToSpannableHandler(CharSequenceToSpannableHandler handler)`|设置文本转换成`Spannable`的预处理回调，可以处理特殊的文本样式|