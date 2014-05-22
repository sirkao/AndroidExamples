package com.plus.sample.adapters;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public abstract class ArrayBaseAdapter<T> extends ArrayAdapter<T> {

	public ArrayBaseAdapter(Context context, int resource) {
		super(context, resource);
	}

	public ArrayBaseAdapter(Context context, int resource, List<T> listGroupPackages) {
		super(context, resource, listGroupPackages);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = getViewR(position, convertView, parent);
		return v;
	}

	public abstract View getViewR(int position, View convertView, ViewGroup parent);

}
