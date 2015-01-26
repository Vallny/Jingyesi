package com.vallny.jing.bean;


import java.util.List;

public abstract class ListBean<T extends ItemBean, K> {

    protected int total_number = 0;
    protected long previous_cursor = 0;
    protected long next_cursor = 0;

    public abstract int getSize();

    public int getTotal_number() {
        return total_number;
    }

    public void setTotal_number(int total_number) {
        this.total_number = total_number;
    }

    public abstract T getItem(int position);

    public abstract List<T> getItemList();

    public long getPrevious_cursor() {
        return previous_cursor;
    }

    public void setPrevious_cursor(int previous_cursor) {
        this.previous_cursor = previous_cursor;
    }

    public long getNext_cursor() {
        return next_cursor;
    }

    public void setNext_cursor(int next_cursor) {
        this.next_cursor = next_cursor;
    }

    public abstract void addNewData(K newValue);

    public abstract void addOldData(K oldValue);
}
