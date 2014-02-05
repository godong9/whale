package com.fitplus.whale;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
public class MainActivity extends Activity {

	private static final int PICK_FROM_CAMERA = 0;
	private static final int PICK_FROM_ALBUM = 1;
	private static final int CROP_FROM_CAMERA = 2;
	private static final String PACKAGE_NAME="com.fitplus.whale";
	private static final String IMAGES = "com.nostra13.example.universalimageloader.IMAGES";
	private static final String IMAGE_POSITION = "com.nostra13.example.universalimageloader.IMAGE_POSITION";
	private ImageAdapter _imageAdapter;
	private SharedPreferences prefs;
	private String path = Environment.getExternalStorageDirectory().toString();
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private ArrayList<String> _imageNames = new ArrayList();
	private AbsListView listView;

	
	private DisplayImageOptions options;
	private Uri _imageCaptureUri;
	private String _calName = "";
	private int _calCnt = 0;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		//StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDialog().build());
		//StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyDeath().build());
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button btn1 = (Button)findViewById(R.id.button1);
		btn1.setOnClickListener(btnClickListener);	
		prefs = getSharedPreferences("whaleapp", MODE_PRIVATE);
		int calNum = prefs.getInt("calNum", 0);
		int calCnt = 0;
		
		if(calNum == 0){
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt("calNum", 1);
			editor.commit();
			_calName = "cal1";
		}
		else {
			String tmpCal = String.valueOf(calNum);
			calCnt = prefs.getInt(tmpCal, 1);
			_calName = "cal"+calCnt;
		}
		
		if(calCnt == 0){
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt("calCnt", 1);
			editor.commit();
			_calCnt = 1;
		}
		else {
			calCnt = prefs.getInt("calCnt", 1);
			_calCnt = calCnt;
		}
	
		for(int i=1; i<=calCnt; i++) {
			String tmpImageName = _calName+"_"+i+".jpg";
			String filePath = path+"/whale/" + PACKAGE_NAME + "/"+_calName+"/"+tmpImageName;		
			_imageNames.add("file://"+filePath);
		}
		
		//Toast.makeText(this, (String)_imageNames.get(1), Toast.LENGTH_SHORT).show();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
			.threadPriority(Thread.NORM_PRIORITY - 2)
			.denyCacheImageMultipleSizesInMemory()
			.discCacheFileNameGenerator(new Md5FileNameGenerator())
			.tasksProcessingOrder(QueueProcessingType.LIFO)
			.writeDebugLogs() // Remove for release app
			.build();
// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
		options = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.ic_stub)
			.showImageForEmptyUri(R.drawable.ic_empty)
			.showImageOnFail(R.drawable.ic_error)
			.cacheInMemory(true)
			.cacheOnDisc(true)
			.considerExifParams(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();
		
			listView = (GridView) findViewById(R.id.gridview);
		_imageAdapter = new ImageAdapter();
		((GridView) listView).setAdapter(_imageAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				startImagePagerActivity(position);
			}
		});
	}
	
	public void refreshListView(){
		int tmpCnt = _calCnt;
		String tmpImageName = _calName+"_"+tmpCnt+".jpg";
		String filePath = path+"/whale/" + PACKAGE_NAME + "/"+_calName+"/"+tmpImageName;		
		_imageNames.add("file://"+filePath);
		_imageAdapter.notifyDataSetChanged();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void startImagePagerActivity(int position) {
		Intent intent = new Intent(this, ImagePagerActivity.class);
		String[] strArray = _imageNames.toArray(new String[_imageNames.size()]);
		intent.putExtra(IMAGES, strArray);
		intent.putExtra(IMAGE_POSITION, position);
		startActivity(intent);
	}
	


	View.OnClickListener btnClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.button1:
					doTakePhotoAction();
					break;
			}
		}
	};
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(resultCode != -1)	
		{
			return;
		}

		switch(requestCode)
		{
			
			case PICK_FROM_CAMERA:
			{
				/*
				final Bundle extras = data.getExtras();
				Bitmap resize;			
				*/	
				
				try{
					Bitmap tmpPicture = Images.Media.getBitmap(getContentResolver(), _imageCaptureUri);
					//Bitmap resize;	
					
					//resize = Bitmap.createScaledBitmap(tmpPicture, 320, 320, true);
					
					FileOutputStream fOut = null;
					String tmp_file_name = "";
					String path = Environment.getExternalStorageDirectory().toString();
					tmp_file_name = _calName+"_"+_calCnt+".jpg";			
					Toast.makeText(this, _imageCaptureUri.toString(), Toast.LENGTH_SHORT).show();
					String folderPath = path+"/whale/" + PACKAGE_NAME + "/"+_calName;
					String filePath = path+"/whale/" + PACKAGE_NAME + "/"+_calName+"/"+tmp_file_name;
					
					File folder = new File(folderPath);
					File file = new File(filePath);
					
					if (!folder.exists()) {
						folder.mkdirs();
					}
											
					fOut = new FileOutputStream(filePath);	
					tmpPicture.compress(CompressFormat.JPEG, 100, fOut);	
					
					fOut.flush();
					fOut.close();
					
					File tmp_file = new File(_imageCaptureUri.getPath());	

					if(tmp_file.exists())
					{
						tmp_file.delete();	
					}		
					//카운트 추가
					_calCnt++;
					SharedPreferences.Editor editor = prefs.edit();
					editor.putInt("calCnt", _calCnt);
					editor.commit();
					//refresh 추가 
					refreshListView();
				}
				catch(Exception e)
				{
					System.out.println("ERROR");
				}
				
			}	
			break;
		}
	}
	
	private void doTakePhotoAction()
	{
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		
		String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
		_imageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));
		
		intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, _imageCaptureUri);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, PICK_FROM_CAMERA);
	}
	
	public class ImageAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return _imageNames.size()-1;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			View view = convertView;
			
			if (view == null) {
				view = getLayoutInflater().inflate(R.layout.item_grid_image, parent, false);
				holder = new ViewHolder();
				assert view != null;
				holder.imageView = (ImageView) view.findViewById(R.id.image);
				holder.progressBar = (ProgressBar) view.findViewById(R.id.progress);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}
			
			imageLoader.displayImage(_imageNames.get(position), holder.imageView, options, new SimpleImageLoadingListener() {
					 @Override
					 public void onLoadingStarted(String imageUri, View view) {
						 holder.progressBar.setProgress(0);
						 holder.progressBar.setVisibility(View.VISIBLE);
					 }

					 @Override
					 public void onLoadingFailed(String imageUri, View view,
							 FailReason failReason) {
						 holder.progressBar.setVisibility(View.GONE);
					 }

					 @Override
					 public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
						 holder.progressBar.setVisibility(View.GONE);
					 }
				 }, new ImageLoadingProgressListener() {
					 @Override
					 public void onProgressUpdate(String imageUri, View view, int current,
							 int total) {
						 holder.progressBar.setProgress(Math.round(100.0f * current / total));
					 }
				 }
			);

			return view;
		}

		class ViewHolder {
			ImageView imageView;
			ProgressBar progressBar;
		}
	}

}
