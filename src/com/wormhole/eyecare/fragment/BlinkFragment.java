package com.wormhole.eyecare.fragment;

import java.util.Timer;
import java.util.TimerTask;

import com.wormhole.eyecare.R;
import com.wormhole.eyecare.utils.Constant;
import com.wormhole.eyecare.utils.DoubleTransaction;
import com.wormhole.eyecare.view.BlinkView;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.media.AudioManager;
import android.media.SoundPool;
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

public class BlinkFragment extends Fragment implements OnClickListener{
	
	private static boolean isVisible;
	private SoundPool sp;
	
	private TextView start;
	private TextView replay;
	private TextView back;
	private RelativeLayout blinkLayout;
	
	private ModelFragment modelFragmentL;
	private ModelFragment modelFragmentR;
	private RelaxFragment relaxFragmentL;
	private RelaxFragment relaxFragmentR;
	private BlinkView blinkView;
	private FragmentManager fm;
	
	private boolean playClose;
	private boolean playOpen;
	private boolean isDailyModel;
	private int count;
	private int playCount;
	/** 音效ID */
	private int musicID;
	
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
				blinkView.setVisibility(View.GONE);
				replay.setVisibility(View.VISIBLE);
				replay.setFocusable(true);
				back.setVisibility(View.VISIBLE);
				back.setFocusable(true);
				if(isDailyModel){
					MyTimerTask timerTask = new MyTimerTask(-1);
					timer.schedule(timerTask, 3000);
				}
				break;
			case -1:
				onClick(back);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
		
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_blink_view, container, false);
		
		start = (TextView) view.findViewById(R.id.blink_start);
		replay = (TextView) view.findViewById(R.id.blink_replay);
		back = (TextView) view.findViewById(R.id.blink_back);
		
		start.setOnClickListener(this);
		replay.setOnClickListener(this);
		back.setOnClickListener(this);
		
		blinkView = new BlinkView(getActivity());
		blinkLayout = (RelativeLayout) view.findViewById(R.id.blink_layout);
		animControl();
		
		if(isDailyModel){
			MyTimerTask timerTask = new MyTimerTask(0);
			timer.schedule(timerTask, 3000);
		}
		
		sp = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
		musicID = sp.load(getActivity(), R.raw.gear, 1);
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
		case R.id.blink_start:
			start.setVisibility(View.GONE);
			RelativeLayout.LayoutParams layoutParams= new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			blinkLayout.removeView(blinkView);
			blinkLayout.addView(blinkView, layoutParams);
			playClose = true;
			break;
		case R.id.blink_replay:
			if(relaxFragmentL == null){
				relaxFragmentL = new RelaxFragment();
			}
			if(relaxFragmentR == null){
				relaxFragmentL = new RelaxFragment();
			}
			transaction.replace(relaxFragmentL,relaxFragmentR);
			break;
		case R.id.blink_back:
			if(modelFragmentL == null){
				modelFragmentL = new ModelFragment();
			}
			if(modelFragmentR == null){
				modelFragmentR = new ModelFragment();
			}
			transaction.replace(modelFragmentL, modelFragmentR);
			isVisible = true;
			break;
		default:
			break;
		}
		transaction.commit();
	}
	
	private void animControl(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(playClose){
						if(count < 20){
							blinkView.playEyeClose();
							count++;
						}else {
							playClose = false;
							playOpen = true;
							count = 0;
						}
					}else if (playOpen) {
						if(count < 20){
							blinkView.playEyeOpen();
							count++;
						}else {
							playOpen = false;
							count = 0;
							if(playCount < 20){
								playCount++;
								playClose = true;
								sp.play(musicID, 1, 1, 0, 0, 1); 
							}else {
								Message message = mHandler.obtainMessage();
								message.what = 1;
								mHandler.sendMessage(message);
							}
						}
					}
					try {
						Thread.sleep(50);
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
