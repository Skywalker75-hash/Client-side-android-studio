package com.example.myapplication8;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
//该类中的方法进行隐式调用
/**
 * 用于RecyclerView网格布局中添加间距的装饰类。
 */
public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private int spanCount; // 网格中每行的项数
    private int spacing; // 项之间的间距大小
    private boolean includeEdge; // 是否在网格的边缘添加间距

    /**
     * 构造函数。
     *
     * @param spanCount 每行的项数
     * @param spacing 项之间的间距
     * @param includeEdge 是否包含边缘的间距
     */
    public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.spacing = spacing;
        this.includeEdge = includeEdge;
    }

    /**
     * 为每个项设置偏移量，以创建间距。
     *
     * @param outRect 表示项的偏移量的Rect
     * @param view 当前项的视图
     * @param parent RecyclerView实例
     * @param state RecyclerView的当前状态
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view); // 当前项的位置
        int column = position % spanCount; // 计算当前项在网格中的列

        if (includeEdge) {
            // 如果包含边缘
            outRect.left = spacing - column * spacing / spanCount; // 左边间距
            outRect.right = (column + 1) * spacing / spanCount; // 右边间距

            if (position < spanCount) {
                // 顶部的项
                outRect.top = spacing;
            }
            // 所有项的底部间距
            outRect.bottom = spacing;
        } else {
            // 不包含边缘
            outRect.left = column * spacing / spanCount; // 左边间距
            outRect.right = spacing - (column + 1) * spacing / spanCount; // 右边间距
            if (position >= spanCount) {
                // 顶部不是第一行的项
                outRect.top = spacing;
            }
        }
    }
}