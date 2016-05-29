package activity;

import java.util.ArrayList;
import java.util.List;

import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;
import model.City;
import model.County;
import model.Province;
import model.SmartWeatherDB;
import com.smartweather.app.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

// 定义选择 省份 城市 县 的Activity界面
public class ChooseAreaActivity extends Activity {
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	
	private ListView listView;
	private TextView titleText;
	private ProgressDialog progressDialog;	// 进度条对话框
	private ArrayAdapter<String> adapter;	// 数组适配器
	private SmartWeatherDB db;
	private List<String> dataList = new ArrayList<String>();	// 要显示数据的List集合
	
	private List<Province> provinceList;	// 省的列表
	private List<City> cityList;			// 市的列表
	private List<County> countyList;		// 县的列表
	
	private Province selectedProvince;		// 当前选中的省
	private City selectedCity;				// 当前选中的市
	
	private int currentLevel;				// 当前选中的级别

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);	// 设置不显示标题栏
		setContentView(R.layout.choose_area);	// 加载自定义布局
		
		// 找到关心的控件
		titleText = (TextView) findViewById(R.id.tv_title);
		listView = (ListView) findViewById(R.id.lv_list);	// ListView 用来显示省 市 县 的列表
		
		// 设置 数组适配器
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		
		// 获取数据库对象的实例
		db = SmartWeatherDB.getInstance(this);
		
		// 给ListView设置条目点击监听
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index,
					long arg3) {
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(index);
					
					queryCities();	// 调用查找市级数据方法
				} else if (currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(index);
					
					queryCounties(); // 调用查找县级数据的方法
				}
			}

		});
		
		queryProvinces();	// 加载省级的数据
		
	}
	
	// 查询所有的省级数据  优先从数据库查找，如果没有查到再去服务器查询数据
	private void queryProvinces(){
		List<Province> provinceList = db.loadProvinces();
		
		if (provinceList.size() > 0) {
			dataList.clear();	// 清空之前的列表数据
			
			for (Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			
			adapter.notifyDataSetChanged();	// 通知适配器数据发生了改变，重新加载。
			
			listView.setSelection(0);
			titleText.setText("中国");
			
			currentLevel = LEVEL_PROVINCE;
		} else {
			// 调用从服务器查询数据的方法
			queryFromServer(null, "province");
		}
	}
	
	// 查询所有的市级数据	优先从数据库查找，如果没有查到再去服务器查询数据
	private void queryCities() {

		cityList = db.loadCities(selectedProvince.getId());
		
		if (cityList.size() > 0) {
			dataList.clear();
			
			for (City city : cityList) {
				dataList.add(city.getCityName());
			}
			
			adapter.notifyDataSetChanged();
			
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			
			currentLevel = LEVEL_CITY;
		} else {
			// 调用从服务器查询数据的方法
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}
	
	// 查询所有的县级数据 	优先从数据库查找，如果没有查到再去服务器查询数据
	private void queryCounties() {
		
		countyList = db.loadCounties(selectedCity.getId());
		
		if(countyList.size() > 0){
			dataList.clear();
			
			for (County county : countyList) {
				dataList.add(county.getCountyName());
			}
			
			adapter.notifyDataSetChanged();
			
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			
			currentLevel = LEVEL_COUNTY;
		} else {
			// 调用从服务器查询数据的方法
			queryFromServer(selectedCity.getCityCode(), "county");
		}
	}
	
	// 定义一个方法 根据传入的代号code 和 类型 到服务器查询省市县的数据
	private void queryFromServer(final String code, final String type){
		String address;
		if (!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
		} else {
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		
		// 调用显示进度条对话框的方法
		showProgressDialog();
		
		// 通过封装好的 HttpUtil 工具类，向服务器发送请求
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {

				boolean result = false;
				
				if ("province".equals(type)) {
					result = Utility.handleProvinceResponse(db, response);	
				} else if ("city".equals(type)) {
					result = Utility.handleCitiesReoponse(db, response, selectedProvince.getId());
				} else if ("county".equals(type)) {
					result = Utility.handleCountiesReoponse(db, response, selectedCity.getId());
				}
				
				if (result) {
					
					// 通过 runOnUiThread() 方法回到主线程处理逻辑
					runOnUiThread(new Runnable() {
						public void run() {
							
							// 关闭进度条对话框
							closeProgressDialog();
							
							if ("province".equals(type)) {
								queryProvinces();
							} else if ("city".equals(type)) {
								queryCities();
							} else if ("county".equals(type)) {
								queryProvinces();
							}
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {

				// 通过 runOnUiThread() 方法回到主线程处理逻辑
				runOnUiThread(new Runnable() {
					public void run() {
						
						// 关闭进度条对话框
						closeProgressDialog();
						
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	
	// 定义一个方法 显示进度条对话框
	private void showProgressDialog(){
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		
		progressDialog.show();
	}
	
	// 定义一个方法 关闭进度条对话框
	private void closeProgressDialog(){
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	
	// 根据当前的级别 判断按下 Back 键，是应该返回上一级列表，还是应该直接退出。
	@Override
	public void onBackPressed() {
		
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
		} else {
			finish();
		}
	}
}
