package cn.com.bter.easyble.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * 带点击的RecycleView的RecyclerView.Adapter
 * 若设置选中条目背景要打开是否可选中条目才有效果，原理选择效果在点击时认为选中
 * Created by admin on 2017/5/27.
 */

public abstract class BaseRecycleViewAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    private OnItemClickLitener mOnItemClickLitener;
    /**
     * 是否可选中
     */
    private boolean isCanSetItemSelected = false;

    private boolean isSelectByClick = false;

    /**
     * 当前选中的条目
     */
    private int selectItemPosition = -1;
    /**
     * 当前选中的控件
     */
    private View currentSelectItmeView;

    private RecyclerView recyclerView;

    private View.OnTouchListener mOnTouchListener;

    private boolean isTouch;

    public BaseRecycleViewAdapter(RecyclerView recyclerView){
        if(null == recyclerView){
            throw new NullPointerException("this recyclerView must be not null!");
        }
        this.recyclerView = recyclerView;

        this.recyclerView.setOnTouchListener(onTouchListener);
    }

    /**
     * 内部点击事件
     */
    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    isTouch = true;
                    break;
                case MotionEvent.ACTION_UP:
                    isTouch = false;
                    break;
            }
            if(null != mOnTouchListener){
                return mOnTouchListener.onTouch(v,event);
            }
            return false;
        }
    };

    /**
     * 设置点击或长按监听<br\>
     * 如果存在嵌套的话，有可能受事件分发的影响会无效.<br\>
     * 所以要明确所有控件的clickenable状态.<br\>
     * 特别像一个ViewGrup嵌套Button,ViewGrup增加click会无效<br\>
     * @param mOnItemClickLitener
     */
    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    /**
     * 重写该方法以达到设置点击监听
     */
    @Override
    public final VH onCreateViewHolder(ViewGroup parent, int viewType) {
        final VH holder = onCreateViewHolder(viewType,parent);

        holder.itemView.setClickable(true);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getLayoutPosition();
                if(isSelectByClick) {
                    setSelectItem(holder.itemView, position);
                }
                if(null != mOnItemClickLitener){
                    mOnItemClickLitener.onItemClick(holder.itemView,position);
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(null != mOnItemClickLitener){
                    return mOnItemClickLitener.onItemLongClick(holder.itemView,holder.getLayoutPosition());
                }
                return false;
            }
        });

        return holder;
    }

    public abstract VH onCreateViewHolder(int viewType,ViewGroup parent);

    /**
     * 重写该方法以达到更新选中条目的作用
     */
    @Override
    public final void onBindViewHolder(VH holder, int position) {
        holder.itemView.setSelected(false);
        if(position == selectItemPosition){
//            holder.itemView.setSelected(true);
            setSelectItem(holder.itemView,position);
        }
        onBindViewHolder(position,holder);
    }

    public abstract void onBindViewHolder(int position, VH holder);

    /**
     * 设置选中的条目
     */
    private void setSelectItem(View view,int position){
        if(view != null){
            View temp = null;
            if(null != currentSelectItmeView){
                currentSelectItmeView.setSelected(false);
                temp = currentSelectItmeView;
            }
            if(isCanSetItemSelected){
                view.setSelected(true);
                currentSelectItmeView = view;
                selectItemPosition = position;
                onItemSelectChange(temp,currentSelectItmeView);
            }

        }
    }

    /**
     *
     * 告诉子类列表元素被选中
     * 子类可以通过重写来达到目的
     * @param oldView 可能为空
     * @param newView
     */
    protected void onItemSelectChange(View oldView,View newView){

    }

    /**
     * 设置选中的条目
     * 要开启可选择功能isCanSetItemSelected()
     */
    public void setSelectItem(final int position,boolean isSmooth){
        if(getItemCount() > position && position > -1 && isCanSetItemSelected){
            selectItemPosition = position;
            final RecyclerView.OnScrollListener listener = new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    setSelectItem(recyclerView.getLayoutManager().findViewByPosition(selectItemPosition),selectItemPosition);//防止未移动到目标找不到view
                }
            };
            recyclerView.addOnScrollListener(listener);
//            recyclerView.setOnScrollListener(listener);
            if(!isSmooth) {
                recyclerView.scrollToPosition(selectItemPosition);
            }else {
                recyclerView.smoothScrollToPosition(selectItemPosition);
//                recyclerView.scrollToPosition(selectItemPosition);
            }
            setSelectItem(recyclerView.getLayoutManager().findViewByPosition(selectItemPosition),selectItemPosition);//防止目标已经在可视区却没有更新状态
            recyclerView.removeOnScrollListener(listener);
        }
    }

    /**
     * 设置RecyclerView的OnTouchListener
     * @param mOnTouchListener
     */
    public void setRecyclerViewOnTouchListener(View.OnTouchListener mOnTouchListener){
        this.mOnTouchListener = mOnTouchListener;
    }

    /**
     * 是否开启可选择功能
     * @param isCanSetItemSelected
     */
    public void isCanSetItemSelected(boolean isCanSetItemSelected){
        this.isCanSetItemSelected = isCanSetItemSelected;

        if(!isCanSetItemSelected && null != currentSelectItmeView){
            selectItemPosition = -1;
            currentSelectItmeView.setSelected(false);
        }
    }

    public int getSelectItemPosition() {
        return selectItemPosition;
    }

    /**
     * 点击是否可选中
     * @param selectByClick
     */
    public void setSelectByClick(boolean selectByClick) {
        isSelectByClick = selectByClick;
    }

    public interface OnItemClickLitener{
        void onItemClick(View view, int position);

        /**
         *
         * @param view
         * @param position
         * @return true可防止触发onItemLongClick 不会触发onItemClick，反之则会
         */
        boolean onItemLongClick(View view, int position);
    }
}
