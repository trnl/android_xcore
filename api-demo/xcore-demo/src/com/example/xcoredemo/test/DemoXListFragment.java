package com.example.xcoredemo.test;

import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import by.istin.android.xcore.fragment.XListFragment;
import by.istin.android.xcore.processor.impl.GsonArrayContentValuesProcessor;
import by.istin.android.xcore.provider.ModelContract;

import com.example.xcoredemo.R;
import com.example.xcoredemo.test.bo.TestEntity;

public class DemoXListFragment extends XListFragment {

	@Override
	public void onListItemClick(Cursor cursor, View v, int position, long id) {

	}

	@Override
	public int getViewLayout() {
		return R.layout.fragment_demox;
	}

	@Override
	public Uri getUri() {
		return ModelContract.getUri(TestEntity.class);
	}

	@Override
	public String getUrl() {
		return "https://dl.dropboxusercontent.com/s/3c0n0ijpjjorqih/jsonarray";
	}

	@Override
	public String getProcessorKey() {
		return new GsonArrayContentValuesProcessor(TestEntity.class)
				.getAppServiceKey();
	}

	@Override
	protected String[] getAdapterColumns() {
		return new String[] { TestEntity.STRING_VALUE };
	}

	@Override
	protected int[] getAdapterControlIds() {
		return new int[] { R.id.tv_id };
	}

	@Override
	protected int getAdapterLayout() {
		return R.layout.adapter_demox;
	}

	@Override
	protected void onPageLoad(int newPage, int totalItemCount) {
		if (newPage == 1) {
			loadData(
					getActivity(),
					"https://dl.dropboxusercontent.com/s/slnep5t0p6yvjr1/jsonarray1",
					null);
		}
		if (newPage == 2) {
			loadData(
					getActivity(),
					"https://dl.dropboxusercontent.com/s/xn8f9rw9034da2u/jsonarray2",
					null);
		}
	}

	@Override
	protected boolean isPagingSupport() {
		return true;
	}

}
