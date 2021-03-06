package com.wormhole.eyecare.fragment;

import java.util.Timer;
import java.util.TimerTask;

import com.wormhole.eyecare.R;
import com.wormhole.eyecare.utils.Constant;
import com.wormhole.eyecare.utils.DoubleTransaction;
import com.wormhole.eyecare.view.TrainView;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class TrainFragment extends Fragment implements OnClickListener{

	private static boolean isVisible;
	private TextView start;
	private TextView next;
	private RelativeLayout trainLayout;
	private WatchFragment watchFragmentL;
	private WatchFragment watchFragmentR;
	private TrainView trainView;
	private FragmentManager fm;
	
	private boolean disperse; 
	private boolean merge;
	private int count;
	private int playCount;
	private boolean isDailyModel;
	
	private Timer timer = new Timer();
	
	
	private class MyTimerTask extends TimerTask{
		int what;

		public MyTimerTask(int what) {
			this.what = what;
		}
		
		@Override
		public void run() {
			Message message = mHandler.obtainMessage();
			message.what = this.what;
			mHandler.sendMessage(message);
		}
	}
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				onClick(start);
				break;
			case 1:
				next.setVisibility(View.VISIBLE);
				trainView.setVisibility(View.GONE);
				next.setFocusable(true);
				if(isDailyModel){
					MyTimerTask timerTask = new MyTimerTask(-1);
					timer.schedule(timerTask, 3000);
				}
				break;
			case -1:
				onClick(next);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_train_view, container, false);
		
		start = (TextView) view.findViewById(R.id.train_start);
		next = (TextView) view.findViewById(R.id.train_next);
		
		start.setOnClickListener(this);
		next.setOnClickListener(this);
		
		trainView = new TrainView(getActivity());
		trainLayout = (RelativeLayout) view.findViewById(R.id.train_layout);
		animControl();
		if(isDailyModel){
			MyTimerTask timerTask = new MyTimerTask(0);
			timer.schedule(timerTask, 3000);
		}
		return view;
	}

	@Override
	public void onClick(View v) {
		if(isVisible){
			return;
		}
		
		if(fm == null){
			fm = getActivity().getFragmentManager();
		}
		DoubleTransaction transaction = new DoubleTransaction(fm);
		
		switch (v.getId()) {
		case R.id.train_start:
			v.setVisibility(View.GONE);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			trainLayout.removeView(trainView);
			trainLayout.addView(trainView, layoutParams);
			disperse = true;
			break;
		case R.id.train_next:
			if(watchFragmentL == null){
				watchFragmentL = new WatchFragment();
			}
			if(watchFragmentR == null){
				watchFragmentR = new WatchFragment();
			}
			if(isDailyModel){
				watchFragmentL.setModel(true);
				watchFragmentR.setModel(true);
			}
			transaction.replace(watchFragmentL, watchFragmentR);
			transaction.commit();
			isVisible = true;
			break;
		default:
			break;
		}
	}
	
	public void animControl(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(disperse){
						if(count < 30){
							trainView.playDisperse();
							count++;
						}else{
							disperse = false;
							count = 0;
							merge = true;
						}
					}else if (merge) {
						if(count < 30){
							trainView.playMerge();
							count++;
						}else{
							merge = false;
							count = 0;
							if(playCount < 3){
								disperse = true;
								playCount++;
							}else {
								Message message = mHandler.obtainMessage();
								message.what = 1;
								mHandler.sendMessage(message);
							}
						}
					}
					
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
	
	public void setModel(boolean isDailyModel){
		this.isDailyModel = isDailyModel;
	}
}
