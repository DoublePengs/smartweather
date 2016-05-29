package db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

// 创建一个类 继承SQLiteOpenHelper 
public class SmartWeatherOpenHelper extends SQLiteOpenHelper {

	public SmartWeatherOpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	/*   ☆☆☆   定义三个字符串常量 用来表示创建表结构的语句   ☆☆☆   */

	// 创建省份 Province 表的语句
	public static final String CREATE_PROVINCE = "create table Province(id integer primary key autoincrement,province_name text,province_code text)";
	// 创建城市 City 表的语句
	public static final String CREATE_CITY = "create table City(id integer primary key autoincrement,city_name text,city_code text,province_id integer)";
	// 创建县 County 表的语句
	public static final String CREATE_COUNTY = "create table County(id integer primary key autoincrement,county_name text,county_code text,city_id integer)";

	// 数据库创建时  初始化省、市、县的三张表结构
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_PROVINCE);	// 创建Province表
		db.execSQL(CREATE_CITY);		// 创建City表
		db.execSQL(CREATE_COUNTY);		// 创建County表
	}

	// 数据库版本升级时调用
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
