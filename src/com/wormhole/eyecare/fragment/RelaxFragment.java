package com.wormhole.eyecare.fragment;

import java.util.Timer;
import java.util.TimerTask;

import com.wormhole.eyecare.R;
import com.wormhole.eyecare.utils.Constant;
import com.wormhole.eyecare.utils.DoubleTransaction;
import com.wormhole.eyecare.view.RelaxView;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class RelaxFragment extends Fragment implements OnClickListener {

	private static boolean isVisible = false;
	
	private TextView start;
	private TextView next;
	private RelaxView relaxView;
	private RelativeLayout relaxLayout;
	private AdjustFragment adjustFragmentL;
	private AdjustFragment adjustFragmentR;
	private FragmentManager fm;

	private boolean playToBig;
	private boolean playToSmall;
	private long duration = 0;
	private boolean isDailyModel;

	private Timer timer = new Timer();

	private class MyTimerTask extends TimerTask {

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

	private Handler mHandler = new Handler() {

		MyTimerTask timerTask;

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				onClick(start);
				break;
			case 1:
				playToBig = true;
				break;
			case 2:
				relaxView.setColor(getResources().getColor(R.color.indianred));
				timerTask = new MyTimerTask(3);
				timer.schedule(timerTask, 2000);
				break;
			case 3:
				playToSmall = true;
				break;
			case 4:
				relaxView.setColor(getResources().getColor(R.color.green));
				timerTask = new MyTimerTask(5);
				timer.schedule(timerTask, 2000);
				break;
			case 5:
				relaxView.setVisibility(View.GONE);
				next.setVisibility(View.VISIBLE);
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
		View view = inflater.inflate(R.layout.fragment_relax_view, container, false);

		start = (TextView) view.findViewById(R.id.relax_start);
		next = (TextView) view.findViewById(R.id.relax_next);
		start.setOnClickListener(this);
		next.setOnClickListener(this);

		relaxView = new RelaxView(getActivity());
		relaxLayout = (RelativeLayout) view.findViewById(R.id.relax_layout);
		animContorl();
		
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
		Activity activity = getActivity();
		Log.w("ischanged", "activity = "+activity);
		DoubleTransaction transaction = new DoubleTransaction(fm);
		
		switch (v.getId()) {
		case R.id.relax_start:
			v.setVisibility(View.GONE);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			relaxLayout.removeView(relaxView);
			relaxLayout.addView(relaxView, layoutParams);

			MyTimerTask timerTask = new MyTimerTask(1);
			timer.schedule(timerTask, 2000);
			break;
		case R.id.relax_next:
			if (adjustFragmentL == null) {
				adjustFragmentL = new AdjustFragment();
			}
			if (adjustFragmentR == null) {
				adjustFragmentR = new AdjustFragment();
			}
			if(isDailyModel){
				adjustFragmentL.setModel(true);
				adjustFragmentR.setModel(true);
			}
			transaction.replace(adjustFragmentL, adjustFragmentR);
			transaction.commit();
			isVisible = true;
			break;
		default:
			break;
		}
	}

	public void animContorl() {
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					if (playToBig) {
						if (duration < 40) {
							relaxView.toBig();
							duration++;
						} else {
							duration = 0;
							playToBig = false;
							MyTimerTask timerTask = new MyTimerTask(2);
							timer.schedule(timerTask, 2000);
						}
					}
					if (playToSmall) {
						if (duration < 40) {
							relaxView.toSmall();
							duration++;
						} else {
							duration = 0;
							playToSmall = false;
							MyTimerTask timerTask = new MyTimerTask(4);
							timer.schedule(timerTask, 2000);
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
