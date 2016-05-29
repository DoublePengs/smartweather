package util;

import android.text.TextUtils;
import model.City;
import model.County;
import model.Province;
import model.SmartWeatherDB;

// 该工具类用来解析和处理服务器返回的数据
public class Utility {

	// 解析处理省的数据   ( 01|北京,02|上海,03|天津,04|重庆,.... )
	public synchronized static boolean handleProvinceResponse(SmartWeatherDB db, String response){
		if (!TextUtils.isEmpty(response)) {
			String[] allProvinces = response.split(",");	// 使用逗号把数据进行分割，获得省份的字符串数组
			if (allProvinces != null && allProvinces.length > 0) {
				for (String p : allProvinces) {
					String[] array = p.split("\\|");	// 用 | 再次对 编号|城市 进行切割。  注意 \\ 转义字符
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					
					// 调用 SmartWeatherDB数据库对象的方法  把获取到的省份信息存到数据库 Province表中
					db.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}
	
	// 解析处理返回的城市数据  ( 1001|太原,1002|大同,1003|阳泉,1004|晋中,1005|长治,.... )
	public static boolean handleCitiesReoponse(SmartWeatherDB db, String response, int provinceId){
		if (!TextUtils.isEmpty(response)) {
			String[] allCities = response.split(",");
			if (allCities != null && allCities.length > 0) {
				for (String c : allCities) {
					City city = new City();
					String[] array = c.split("\\|");
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					
					// 调用 SmartWeatherDB数据库对象的方法  把获取到的城市信息存到数据库 City表中
					db.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}
	
	// 解析处理返回的县级数据  ( 100701|临汾,100702|曲沃,100703|永和,100704|隰县,100705|大宁,100706|吉县,100707|襄汾,.... )
	public static boolean handleCountiesReoponse(SmartWeatherDB db, String response, int cityId){
		if (!TextUtils.isEmpty(response)) {
			String[] allCounties = response.split(",");
			if (allCounties != null && allCounties.length > 0) {
				for (String c : allCounties) {
					County county = new County();
					String[] array = c.split("\\|");
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
					
					// 调用 SmartWeatherDB数据库对象的方法  把获取到的县级信息存到数据库 County表中
					db.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}
}
