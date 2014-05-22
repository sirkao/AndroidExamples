package com.plus.sample.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.plus.sample.R;
import com.plus.sample.domain.CirclePerson;
import com.plus.sample.graphic.CircleTransformation;
import com.squareup.picasso.Picasso;

public class CirclePeopleAdapter extends ArrayBaseAdapter<CirclePerson> {

	private Context mContext;
	private static int layoutResource = R.layout.circle_item;
	private int targetWidth = 100;
	private int targetHeight = 100;

	public CirclePeopleAdapter(Context context) {
		super(context, layoutResource);
		this.mContext = context;
	}

	public class ViewHolder {
		ImageView ivIcon;
		TextView tvTitle;
		TextView tvSubTitle;
	}

	@Override
	public View getViewR(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(layoutResource, null, false);
			holder = new ViewHolder();
			holder.ivIcon = (ImageView) rowView.findViewById(R.id.iv_icon);
			holder.tvTitle = (TextView) rowView.findViewById(R.id.tv_title);
			holder.tvSubTitle = (TextView) rowView.findViewById(R.id.tv_subtitle);
			rowView.setTag(holder);
		} else {
			holder = (ViewHolder) rowView.getTag();
		}

		final CirclePerson person = getItem(position);
		Picasso.with(mContext).load(person.getImageUrl()).resize(targetWidth, targetHeight).transform(new CircleTransformation()).into(holder.ivIcon);
		holder.tvTitle.setText(person.getDisplayName());
		holder.tvSubTitle.setVisibility(View.GONE);
		return rowView;
	}

}