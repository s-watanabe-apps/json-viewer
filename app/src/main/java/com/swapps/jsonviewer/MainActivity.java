package com.swapps.jsonviewer;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.swapps.jsonviewer.comparator.DateComparator;
import com.swapps.jsonviewer.comparator.DirectoryComparator;
import com.swapps.jsonviewer.comparator.SizeComparator;
import com.swapps.jsonviewer.util.CommonUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 1;

    public static final int REQUEST_CODE_SETTING = 2;

    public static final int REQUEST_CODE_JSON = 3;

    //public static final String tag ="watanabe";

    ProgressDialog progressDialog;

    Handler handler;

    List<String> listDirectory;

    ListView jsonListView;

    List<Item> listItems;

    JsonListAdapter adapter;

    SharedPreferences preferences;

    // 全面広告
    private InterstitialAd interstitialAd;

    public static int AD_LOAD_TIMEOUT = 30;

    private int adLoadCount = 0;

    private void showInterstitial() {
        if(interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else{
            goToNextLevel();
        }
    }

    private void loadInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().setRequestAgent("android_studio:ad_template").build();
        interstitialAd.loadAd(adRequest);
    }

    private void goToNextLevel() {
        interstitialAd = newInterstitialAd();
        loadInterstitial();
    }

    private InterstitialAd newInterstitialAd() {
        InterstitialAd interstitialAd = new InterstitialAd(this);

        interstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                //Log.d(getClass().getName(), "onAdLoaded()");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                //Log.d(getClass().getName(), "onAdFailedToLoad(" + errorCode + ")");
            }

            @Override
            public void onAdClosed() {
                //Log.d(getClass().getName(), "onAdClosed()");
                goToNextLevel();
            }
        });

        return interstitialAd;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermittion();
    }

    public void checkPermittion() {
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(getString(R.string.permission_read_external_storage_title))
                    .setMessage(getString(R.string.permission_read_external_storage_body))
                    .setCancelable(false)
                    .setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
                        }
                    })
                    .show();
        } else{
            init();
        }
    }

    /**
     * パーミッション設定イベント
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode) {
            case REQUEST_PERMISSION_READ_EXTERNAL_STORAGE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //
                    init();
                } else{
                    //Toast.makeText(this, getString(R.string.permission_no_granted_message), Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }


    private void init() {
        preferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);

        interstitialAd = null;
        interstitialAd = newInterstitialAd();
        loadInterstitial();

        jsonListView = findViewById(R.id.json_list_view);
        jsonListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                Intent intent = new Intent(MainActivity.this, JsonActivity.class);
                intent.putExtra("index", pos);
                intent.putExtra("path", listItems.get(pos).getPath() + "/" + listItems.get(pos).getName());
                startActivityForResult(intent, REQUEST_CODE_JSON);
            }
        });

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle(getString(R.string.search_json_file));
        progressDialog.setMessage("---");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        handler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                String path;
                if(preferences.getInt("storage", 0) == 0) {
                    // 内部ストレージ
                    path = Environment.getExternalStorageDirectory().getPath();
                } else {
                    // 外部ストレージ
                    path = CommonUtil.getSdCardFilesDirPathListForLollipop(MainActivity.this);
                }
                searchJsonFiles(path);

                final Timer timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if((interstitialAd != null && interstitialAd.isLoaded()) || adLoadCount >= AD_LOAD_TIMEOUT) {
                                    // 準備OK
                                    timer.cancel();

                                    progressDialog.dismiss();

                                    if(interstitialAd != null) {
                                        int count = preferences.getInt("count", 0);
                                        if (count % 3 == 1) {
                                            showInterstitial();
                                        }
                                        SharedPreferences.Editor editor = preferences.edit();
                                        count++;
                                        editor.putInt("count", count);
                                        editor.commit();
                                    }
                                } else{
                                    // ロード中
                                    adLoadCount++;
                                    Log.d("wata", "ad_load:" + adLoadCount);
                                    if(adLoadCount >= AD_LOAD_TIMEOUT){
                                        interstitialAd = null;
                                    }
                                }
                            }
                        });
                    }
                },0,500);
            }
        }).start();

    }

    private void searchJsonFiles(String path){
        listDirectory = new ArrayList<>();
        listItems = new ArrayList<>();

        listDirectory.add(path);

        int m = 0;
        int n;
        String[] fileName;

        //Log.d(tag, "path:" + path);

        // dirList.size() は動的変化あり注意
        while(listDirectory.size() > m){

            // get(m) リスト内の指定された位置 m にある要素を返す
            final File directory = new File(listDirectory.get(m));

            handler.post(new Runnable() {
                @Override
                public void run() {
                    progressDialog.setMessage(directory.getPath());
                }
            });

            // java.io.File クラスのメソッド list()
            // 指定したディレクトリに含まれるファイル、ディレクトリの一覧を String 型の配列で返す。
            fileName = directory.list();

            n = 0;
            while(fileName.length > n){

                File subFile;
                subFile = new File(directory.getPath() + "/" + fileName[n]);

                if(subFile.isDirectory()){
                    listDirectory.add(directory.getPath() + "/" + fileName[n]);
                } else{
                    int pos = subFile.getName().lastIndexOf(".");
                    if (pos != -1) {
                        if(!subFile.getName().substring(0, 1).equals(".") && subFile.getName().substring(pos + 1).toLowerCase().equals("json")) {
                            Item item = new Item();
                            item.setName(subFile.getName());
                            item.setPath(subFile.getParent());
                            item.setSize(subFile.length());
                            item.setLastModified(new Date(subFile.lastModified()));
                            listItems.add(item);
                            //Log.d(tag, subFile.getPath());
                        }
                    }
                }
                n++;
            }
            m++;
        }

        // 出力結果をリストビューに表示
        handler.post(new Runnable() {
            @Override
            public void run() {
                setJsonListView();
            }
        });

    }

    public void setJsonListView() {
        clearJsonListView();

        if(preferences.getInt("background", 0) == 0) {
            jsonListView.setBackgroundColor(Color.parseColor(getString(R.string.list_color_white_background)));
        } else{
            jsonListView.setBackgroundColor(Color.parseColor(getString(R.string.list_color_black_background)));
        }

        int sort = preferences.getInt("sort", 0);
        if(sort == 1) {
            Collections.sort(listItems, new SizeComparator());
        } else if(sort == 2) {
            Collections.sort(listItems, new DateComparator());
        } else{
            Collections.sort(listItems, new DirectoryComparator());
        }

        adapter = new JsonListAdapter(MainActivity.this, R.layout.list_item, listItems);
        jsonListView.setAdapter(adapter);
    }

    public void clearJsonListView() {
        adapter = new JsonListAdapter(MainActivity.this, R.layout.list_item, new ArrayList<Item>());
        jsonListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.main_menu_orderby:
                // 並べ替え
                final String itemList[] = getResources().getStringArray(R.array.sort_names);

                final int sort = preferences.getInt("sort", 0);
                final SharedPreferences.Editor editor = preferences.edit();
                new AlertDialog.Builder(MainActivity.this)
                        .setIcon(android.R.drawable.ic_menu_sort_by_size)
                        .setTitle(getString(R.string.sort_title))
                        .setSingleChoiceItems(itemList, sort, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                editor.putInt("sort", whichButton);
                                editor.apply();
                            }
                        })
                        .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if(sort != preferences.getInt("sort", 0)) {
                                    setJsonListView();
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if(sort != preferences.getInt("sort", 0)) {
                                    editor.putInt("sort", sort);
                                    editor.apply();
                                }
                            }
                        })
                        .show();
                break;
            case R.id.main_menu_setting:
                // 設定
                Intent intent = new Intent(this, SettingActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SETTING);
                break;
            default:
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case (REQUEST_CODE_SETTING):
                // SettingActivityから戻ってきた場合
                if (resultCode == RESULT_OK) {
                    //OKボタンを押して戻ってきたときの処理
                    init();
                } else if (resultCode == RESULT_CANCELED) {
                    //キャンセルボタンを押して戻ってきたときの処理
                    setJsonListView();
                } else {
                    //その他
                    Toast.makeText(this, "What!?", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_CODE_JSON:
                // JsonActivityから戻ってきた場合
                if (resultCode == RESULT_OK) {
                    int pos = data.getIntExtra("index", -1);
                    if(pos > -1) {
                        //Log.d(tag, "pos:" + pos);
                        File file = new File(listItems.get(pos).getPath() + "/" + listItems.get(pos).getName());
                        listItems.get(pos).setSize(file.length());
                        listItems.get(pos).setLastModified(new Date(file.lastModified()));
                        adapter.notifyDataSetChanged();
                    }
                }
            default:
                break;
        }
    }
}
