package com.example.places.filter;

import android.R.style;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.places.R;

import java.util.ArrayList;
import java.util.List;

public class FilterDialogFragment extends DialogFragment implements FilterContract.View {
    private FilterContract.Presenter mPresenter = null;

    public FilterDialogFragment() {
    }

    @Override
    public final View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                   final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        getDialog().setTitle(R.string.filter_dialog);
        final View view = inflater.inflate(R.layout.filter_view, container, false);
        final ListView listView = view.findViewById(R.id.filterView);
        final List<FilterItem> filters = mPresenter.getFilteredCategories();
        final ArrayList<FilterItem> arrayList = new ArrayList<>();
        arrayList.addAll(filters);
        final FilterItemAdapter mFilterItemAdapter = new FilterItemAdapter(getActivity(), arrayList);
        listView.setAdapter(mFilterItemAdapter);

        final Button cancel = view.findViewById(R.id.btnCancel);
        cancel.setOnClickListener(v -> dismiss());
        final Button apply = view.findViewById(R.id.btnApply);
        apply.setOnClickListener(v -> {
            final Activity activity = getActivity();
            if (activity instanceof FilterContract.FilterView) {
                ((FilterContract.FilterView) activity).onFilterDialogClose(true);
            }
            dismiss();
        });
        return view;
    }

    @Override
    public final void onCreate(final Bundle savedBundleState) {
        super.onCreate(savedBundleState);
        setStyle(DialogFragment.STYLE_NORMAL, style.Theme_Material_Light_Dialog);
        mPresenter.start();
    }

    @Override
    public final void setPresenter(final FilterContract.Presenter presenter) {
        mPresenter = presenter;
    }


    public class FilterItemAdapter extends ArrayAdapter<FilterItem> {
        public FilterItemAdapter(final Context context, final List<FilterItem> items) {
            super(context, 0, items);
        }

        private class ViewHolder {
            Button btn = null;
            TextView txtName = null;
        }

        @NonNull
        @Override
        public final View getView(final int position, View convertView, @NonNull final ViewGroup parent) {
            final FilterDialogFragment.FilterItemAdapter.ViewHolder holder;

            final FilterItem item = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.filter_list_item, parent, false);
                holder = new FilterDialogFragment.FilterItemAdapter.ViewHolder();
                holder.btn = convertView.findViewById(R.id.categoryBtn);
                holder.txtName = convertView.findViewById(R.id.categoryName);
                convertView.setTag(holder);
            } else {
                holder = (FilterDialogFragment.FilterItemAdapter.ViewHolder) convertView.getTag();
            }
            holder.txtName.setText((item != null) ? item.getTitle() : null);

            if (item.getSelected()) {
                holder.btn.setBackgroundResource(item.getSelectedIconId());
                holder.btn.setAlpha(1.0f);
            } else {
                holder.btn.setBackgroundResource(item.getIconId());
                holder.btn.setAlpha(0.5f);
            }

            convertView.setOnClickListener(v -> {
                final FilterItem clickedItem = getItem(position);
                if (clickedItem != null) {
                    if (clickedItem.getSelected()) {
                        clickedItem.setSelected(false);
                        holder.btn.setBackgroundResource(item.getIconId());
                        holder.btn.setAlpha(0.5f);
                    } else {
                        clickedItem.setSelected(true);
                        holder.btn.setBackgroundResource(item.getSelectedIconId());
                        holder.btn.setAlpha(1.0f);
                    }
                }

            });

            return convertView;
        }
    }

}
