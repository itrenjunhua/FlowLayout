# FlowLayout 流式布局
Android 流式布局控件，实现自动换行，操出范围可以滑动功能，**未使用控件复用功能，所以不应该有太多的子控件**。

## 主要包含功能：

* 流式布局，自动换行
* 使用Adapter的形势注入子控件
* 设置子控件之间的间距(水平方向和竖直方向)
* 竖直方向超出高度可以滑动
* 给子控件设置点击监听
* 设置可显示的最大行数，并提供方法判断是否当前所有的子控件都显示完成
* 可以设置行内水平方向上对齐方式（居左对齐、居右对齐、两端对齐/左右对齐、居中对齐）
* 提供自动滚动到顶部、滚动到底部、滚动到指定位置和滚动到指定行方法

![效果图](https://github.com/itrenjunhua/FlowLayout/blob/master/images/FlowLayout.gif)

## 使用

1. 在布局中使用控件

	    <com.renj.flowlayout.FlowLayout
	            android:id="@+id/flow_layout"
	            android:layout_width="match_parent"
	            android:layout_height="wrap_content"
	            android:layout_marginTop="8dp"
	            android:background="@color/colorAccent"
	            android:paddingStart="12dp"
	            android:paddingTop="8dp"
	            android:paddingEnd="8dp"
	            android:paddingBottom="12dp"
	            app:flow_horizontal_gravity="right"
	            app:flow_horizontal_spacing="6dp"
	            app:flow_vertical_spacing="6dp" />

2. 继承 `FlowLayoutAdapter` ，重写相关方法

		public class MainFlowLayoutAdapter extends FlowLayoutAdapter {
		    private List<String> dataList;
		    private int mCheckedPosition = -1;

		    public MainFlowLayoutAdapter(List<String> datas) {
			this.dataList = datas;
		    }

		    @Override
		    protected View createView(Context context, FlowLayout flowLayout, int position) {
			TextView textView = new TextView(context);
			textView.setTextSize(16);
			textView.setTextColor(context.getResources().getColor(R.color.color_text_grey));
			textView.setBackgroundResource(R.drawable.shape_text_bg);
			textView.setPadding(16, 6, 16, 6);
				textView.setText(dataList.get(position));
			return textView;
		    }

		    @Override
		    public int getItemCount() {
			return dataList == null ? 0 : dataList.size();
		    }

		    @Override
		    public Object getItem(int position) {
			if (ListUtils.isEmpty(dataList)) return null;
			return dataList.get(position);
		    }
		}

3. 给 `FlowLayout` 控件设置Adapter

		flowLayout.setAdapter(new MainFlowLayoutAdapter(DataUtils.getDataList(30)));

## 其他属性设置
### 代码设置

* setMaxRowCount(int maxRowCount)：设置最大显示行数，maxRowCount：最大显示行数  小于0表示全部显示
* setHorizontalGravity(int horizontalGravity)：设置水平方向控件对齐方式，默认居左对齐，取值：
	* FlowLayout.HORIZONTAL_GRAVITY_LEFT
	* FlowLayout.HORIZONTAL_GRAVITY_LEFT
	* FlowLayout.HORIZONTAL_GRAVITY_LEFT
	* FlowLayout.HORIZONTAL_GRAVITY_LEFT
* setSpacing(int horizontalSpacing, int verticalSpacing)：设置子控件之间的间距
* scrollToTop(boolean animation)：滚动到顶部，参数 true：使用动画滚动  false：不使用动画
* scrollToBottom(boolean animation)：滚动到底部，参数 true：使用动画滚动  false：不使用动画
* scrollToPosition(int position, boolean animation)：滚动到指定位置，参数 animation： true：使用动画滚动  false：不使用动画
* scrollToRowNumber(int rowNumber, boolean animation)：滚动到指定行数，参数 animation： true：使用动画滚动  false：不使用动画
* isChildViewAllShow()：是否所有的子控件都显示了，需要在 `setOnChildLayoutFinishListener(OnChildLayoutFinishListener)`  回调中调用保证结果的正确
* getShowRowCount()：获取显示的行数，需要在 `setOnChildLayoutFinishListener(OnChildLayoutFinishListener)`  回调中调用保证结果的正确
* setOnChildLayoutFinishListener(OnChildLayoutFinishListener onChildLayoutFinishListener)：设置子控件布局完成监听
* setOnItemClickListener(OnItemClickListener onItemClickListener)：设置子控件点击监听

### 属性控制

	<declare-styleable name="FlowLayout">
        <!-- 最大显示行数 -->
        <attr name="flow_max_row_count" format="integer" />
        <!-- 每一行水平方向对齐方式 -->
        <attr name="flow_horizontal_gravity" format="enum">
            <!-- 每一行水平方向对齐方式：左对齐，默认 -->
            <enum name="left" value="0" />
            <!-- 每一行水平方向对齐方式：右对齐 -->
            <enum name="right" value="1" />
            <!-- 每一行水平方向对齐方式：左右对齐 -->
            <enum name="left_right" value="2" />
            <!-- 每一行水平方向对齐方式：居中对齐 -->
            <enum name="center" value="3" />
        </attr>
        <!-- 水平方向间距 -->
        <attr name="flow_horizontal_spacing" format="dimension" />
        <!-- 竖直方向间距 -->
        <attr name="flow_vertical_spacing" format="dimension" />
    </declare-styleable>
