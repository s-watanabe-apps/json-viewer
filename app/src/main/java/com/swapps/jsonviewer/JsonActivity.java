package com.swapps.jsonviewer;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JsonActivity extends AppCompatActivity {

    public static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 1;
    String writeJsonString = null;
    String writeJsonPath = null;

    List<Float> textSizes;
    int sizeIndex = 6;

    TextView textJsonViewer;
    StringBuffer textJsonBuffer;

    EditText editJsonViewer;
    StringBuffer editJsonBuffer;
    LinearLayout layoutEditViewer;

    Button buttonSave;

    public static final int JSON_OBJECT = 1;
    public static final int JSON_ARRAY = 2;
    public static final int JSON_ERROR = 3;

    ProgressDialog progressDialog;

    Handler handler;

    String json;

    boolean isParseSuccess = true;

    boolean isUpdated = false;

    int backgroundColor;
    int editTextColor;
    String stringColor;
    String decimalColor;
    String boolColor;
    String normalColor;

    //public static final String tag = "watanabe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json);

        init();
    }

    public void init() {
        SharedPreferences preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        if(preferences.getInt("background", 0) == 0) {
            backgroundColor = Color.parseColor(getString(R.string.json_color_white_background));
            editTextColor = Color.parseColor(getString(R.string.json_color_white_edit_text));
            stringColor = getString(R.string.json_color_white_element_string);
            decimalColor = getString(R.string.json_color_white_element_decimal);
            boolColor = getString(R.string.json_color_white_element_bool);
            normalColor = getString(R.string.json_color_white_normal_text);
        } else{
            backgroundColor = Color.parseColor(getString(R.string.json_color_black_background));
            editTextColor = Color.parseColor(getString(R.string.json_color_black_edit_text));
            stringColor = getString(R.string.json_color_black_element_string);
            decimalColor = getString(R.string.json_color_black_element_decimal);
            boolColor = getString(R.string.json_color_black_element_bool);
            normalColor = getString(R.string.json_color_black_normal_text);
        }

        Typeface face = Typeface.createFromAsset(getAssets(), "gothic.ttf");

        ScrollView scrollJsonView = findViewById(R.id.scrollJsonView);
        scrollJsonView.setBackgroundColor(backgroundColor);

        // JSON表示用EditText
        editJsonViewer = findViewById(R.id.editJsonViewer);
        editJsonViewer.setTypeface(face);
        editJsonViewer.setTextColor(editTextColor);
        layoutEditViewer = findViewById(R.id.layoutEditViewer);

        // JSON表示用TextView
        textJsonViewer = findViewById(R.id.textJsonViewer);
        textJsonViewer.setTypeface(face);
        textJsonViewer.setBackgroundColor(backgroundColor);
        textJsonViewer.setTextColor(Color.parseColor(normalColor));

        //
        textSizes = new ArrayList<>();
        Resources resources = getResources();
        textSizes.add(resources.getDimension(R.dimen.font_size_0));
        textSizes.add(resources.getDimension(R.dimen.font_size_1));
        textSizes.add(resources.getDimension(R.dimen.font_size_2));
        textSizes.add(resources.getDimension(R.dimen.font_size_3));
        textSizes.add(resources.getDimension(R.dimen.font_size_4));
        textSizes.add(resources.getDimension(R.dimen.font_size_5));
        textSizes.add(resources.getDimension(R.dimen.font_size_6));
        textSizes.add(resources.getDimension(R.dimen.font_size_7));
        textSizes.add(resources.getDimension(R.dimen.font_size_8));
        textSizes.add(resources.getDimension(R.dimen.font_size_9));
        textSizes.add(resources.getDimension(R.dimen.font_size_10));
        textSizes.add(resources.getDimension(R.dimen.font_size_11));
        textSizes.add(resources.getDimension(R.dimen.font_size_12));
        textSizes.add(resources.getDimension(R.dimen.font_size_13));
        textSizes.add(resources.getDimension(R.dimen.font_size_14));
        textSizes.add(resources.getDimension(R.dimen.font_size_15));
        textSizes.add(resources.getDimension(R.dimen.font_size_16));

        textJsonViewer.setTextSize(textSizes.get(sizeIndex));

        final String path = getIntent().getStringExtra("path");
        setTitle(new File(path).getName());

        // 保存ボタン
        buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String json = getJsonString(editJsonViewer.getText().toString());
                if(json != null) {
                    writeJsonPath = path;
                    writeJsonString = json;

                    try {
                        writeToFile(writeJsonPath, writeJsonString);
                        isUpdated = true;

                        loadJsonTextView(path);
                    } catch(Exception e) {
                        Toast.makeText(JsonActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else{
                    Toast.makeText(JsonActivity.this, "Parse error!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        loadJsonTextView(path);
    }

    public void loadJsonTextView(final String path) {
        //Log.d(tag, "loadJsonTextView:" + path);

        progressDialog = new ProgressDialog(JsonActivity.this);
        progressDialog.setTitle(getString(R.string.json_file_parsing));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        // JSON表示用バッファ
        textJsonBuffer = new StringBuffer();
        editJsonBuffer = new StringBuffer();
        
        handler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    //Log.d(tag, path);

                    FileInputStream fileInputStream = new FileInputStream(path);
                    int size = fileInputStream.available();
                    byte[] buffer = new byte[size];
                    fileInputStream.read(buffer);
                    fileInputStream.close();

                    json = new String(buffer);
                } catch(Exception e) {
                    //Log.d(tag, e.getMessage());
                }

                if(json != null) {
                    try {
                        readJson(json, 0);
                    } catch (Exception e) {
                        //Log.d(tag, "ERR:" + e.getMessage());
                    } finally {
                        progressDialog.dismiss();
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        editJsonViewer.setText(editJsonBuffer.toString());
                        textJsonViewer.setText(Html.fromHtml(textJsonBuffer.toString()));
                    }
                });

            }
        }).start();
    }

    public void readJson(String json, int indent) throws Exception{
        readJson(json, indent, null, "");
    }

    int count0 = 0;
    int count1 = 0;
    int count2 = 0;
    public void readJson(String json, int indent, String parent, String lastComma) throws Exception {
        int jsonType = getJsonType(json);

        if(indent == 0) {
            count0++;
            count1 = 0;
            count2 = 0;
            //Log.d(tag, count0 + " - " + count1 + " - " + count2);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    progressDialog.setMessage(count0 + " - " + count1 + " - " + count2);
                }
            });
        } else if(indent == 1) {
            count1++;
            count2 = 0;
            //Log.d(tag, count0 + " - " + count1 + " - " + count2);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    progressDialog.setMessage(count0 + " - " + count1 + " - " + count2);
                }
            });
        } else if(indent == 2) {
            count2++;
            //Log.d(tag, count0 + " - " + count1 + " - " + count2);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    progressDialog.setMessage(count0 + " - " + count1 + " - " + count2);
                }
            });
        }


        if(jsonType == JSON_OBJECT) {
            final JSONObject jsonObject = new JSONObject(json);
            Iterator iterator = jsonObject.keys();

            if(parent != null) {
                appendEdit(getSpace(indent) + getEnclose(parent) + ":{");
                appendText(getNbsp(indent) + getColorHtml(getEnclose(parent), stringColor) + ":{");
            } else{
                appendEdit(getSpace(indent) + "{");
                appendText(getNbsp(indent) + "{");
            }

            while(iterator.hasNext()){
                final String key = (String) iterator.next();
                Object value = jsonObject.get(key);
                String comma = iterator.hasNext() ? "," : "";
                if(value instanceof String) {
                    appendEdit(getSpace(indent + 1) + getEnclose(key) + ":" + getEnclose(value.toString()) + comma);
                    appendText(getNbsp(indent + 1) + getColorHtml(getEnclose(key), stringColor) + ":" + getColorHtml(getEnclose(value.toString()), stringColor) + comma);
                } else if(value instanceof Integer || value instanceof Long || value instanceof Double || value instanceof Float) {
                    appendEdit(getSpace(indent + 1) + getEnclose(key) + ":" + value + comma);
                    appendText(getNbsp(indent + 1) + getColorHtml(getEnclose(key), stringColor) + ":" + getColorHtml(value, decimalColor) + comma);
                } else if(value instanceof  Boolean) {
                    appendEdit(getSpace(indent + 1) + getEnclose(key) + ":" + value + comma);
                    appendText(getNbsp(indent + 1) + getColorHtml(getEnclose(key), stringColor) + ":" + getColorHtml(value, boolColor) + comma);
                } else if(value.toString().equals("null")) {
                    appendEdit(getSpace(indent + 1) + getEnclose(key) + ":null" + comma);
                    appendText(getNbsp(indent + 1) + getColorHtml(getEnclose(key), stringColor) + ":" + getColorHtml("null", normalColor) + comma);
                } else{
                    readJson(value.toString(), indent + 1, key, comma);
                }
            }

            appendEdit(getSpace(indent) + "}" + lastComma);
            appendText(getNbsp(indent) + "}" + lastComma);
            //Log.d(tag, jsonObject.toString());
        } else if(jsonType == JSON_ARRAY) {
            JSONArray jsonArray = new JSONArray(json);

            if(parent != null) {
                appendEdit(getSpace(indent) + getEnclose(parent) + ":[");
                appendText(getNbsp(indent) + getColorHtml(getEnclose(parent), stringColor) + ":[");
            } else{
                appendEdit(getSpace(indent) + "[");
                appendText(getNbsp(indent) + "[");
            }

            for(int i = 0; i < jsonArray.length(); i++) {
                Object value = jsonArray.get(i);
                String comma = i < jsonArray.length() - 1 ? "," : "";
                if(value instanceof String) {
                    appendEdit(getSpace(indent + 1) + getEnclose(value.toString()) + comma);
                    appendText(getNbsp(indent + 1) + getColorHtml(getEnclose(value.toString()), stringColor) + comma);
                } else if(value instanceof Integer || value instanceof Long || value instanceof Double || value instanceof Float) {
                    appendEdit(getSpace(indent + 1) + value + comma);
                    appendText(getNbsp(indent + 1) + getColorHtml(value, decimalColor) + comma);
                } else if(value instanceof  Boolean) {
                    appendEdit(getSpace(indent + 1) + value + comma);
                    appendText(getNbsp(indent + 1) + getColorHtml(value, boolColor) + comma);
                } else if(value.toString().equals("null")) {
                    appendEdit(getSpace(indent + 1) + "null" + comma);
                    appendText(getNbsp(indent + 1) + getColorHtml("null", normalColor) + comma);
                } else {
                    readJson(value.toString(), indent + 1, null, comma);
                }
            }

            appendEdit(getSpace(indent) + "]" + lastComma);
            appendText(getNbsp(indent) + "]" + lastComma);
        } else{
            append("Parse error!!");
            isParseSuccess = false;
        }
    }

    public String getSpace(int indent) {
        StringBuffer space = new StringBuffer();

        for(int i = 0; i < indent; i++) {
            space.append("  ");
        }

        return space.toString();
    }

    public String getNbsp(int indent) {
        StringBuffer space = new StringBuffer();

        for(int i = 0; i < indent; i++) {
            space.append("&nbsp;&nbsp;");
        }

        return space.toString();
    }

    public int getJsonType(String json) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
            if(jsonObject != null) {
                //Log.d(tag, jsonObject.toString());
                return JSON_OBJECT;
            }
        } catch (Exception e) {
            //
        }

        JSONArray jsonArray = null;
        try{
            jsonArray = new JSONArray(json);
            if(jsonArray != null) {
                //Log.d(tag, jsonArray.toString());
                return JSON_ARRAY;
            }
        } catch (Exception e) {
            //
        }

        isParseSuccess = false;
        return JSON_ERROR;
    }

    public String getJsonString(String json) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
            if(jsonObject != null) {
                //Log.d(tag, jsonObject.toString());
                return jsonObject.toString();
            }
        } catch (Exception e) {
            //
        }

        JSONArray jsonArray = null;
        try{
            jsonArray = new JSONArray(json);
            if(jsonArray != null) {
                //Log.d(tag, jsonArray.toString());
                return jsonArray.toString();
            }
        } catch (Exception e) {
            //
        }

        return null;
    }


    public void append(String string) {
        appendEdit(string);
        appendText(string);
    }

    public void appendEdit(String string) {
        editJsonBuffer.append(string + "\n");
    }

    public void appendText(String string) {
        textJsonBuffer.append(string + "<br>");
    }

    public String getEnclose(String value) {
        return "\"" + value + "\"";
    }

    public String getColorHtml(Object value, String colorString) {
        return "<font color=\"" + colorString + "\">" + value.toString().replace(" ", "&nbsp;") + "</font>";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.json_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.json_menu_plus:
                // ズームイン
                if(++sizeIndex < textSizes.size()) {
                    textJsonViewer.setTextSize(textSizes.get(sizeIndex));
                    editJsonViewer.setTextSize(textSizes.get(sizeIndex));
                } else{
                    sizeIndex = textSizes.size() - 1;
                }
                break;
            case R.id.json_menu_minus:
                // ズームアウト
                if(--sizeIndex >= 0) {
                    textJsonViewer.setTextSize(textSizes.get(sizeIndex));
                    editJsonViewer.setTextSize(textSizes.get(sizeIndex));
                } else{
                    sizeIndex = 0;
                }

                break;
            case R.id.json_menu_edit:
                // 編集
                if(textJsonViewer.getVisibility() == View.VISIBLE) {
                    checkPermission();
                    /*
                    if(getJsonType(editJsonViewer.getText().toString()) != JSON_ERROR) {
                        textJsonViewer.setVisibility(View.GONE);
                        layoutEditViewer.setVisibility(View.VISIBLE);
                    } else{
                        Toast.makeText(this, "Parseエラー発生時は編集できません！", Toast.LENGTH_SHORT).show();
                    }
                    */
                } else{
                    textJsonViewer.setVisibility(View.VISIBLE);
                    layoutEditViewer.setVisibility(View.GONE);
                }

                break;
            default:
        }
        return true;
    }

    public void writeToFile(String fileName, String text) throws IOException{
        FileOutputStream fileOutputStream = null;

        fileOutputStream = new FileOutputStream(new File(fileName));
        fileOutputStream.write(text.getBytes());

        Toast.makeText(this, getString(R.string.json_save_message), Toast.LENGTH_SHORT).show();
        textJsonViewer.setVisibility(View.VISIBLE);
        layoutEditViewer.setVisibility(View.GONE);
    }

    public void checkPermission() {
        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(JsonActivity.this)
                    .setTitle(getString(R.string.permission_write_external_storage_title))
                    .setMessage(getString(R.string.permission_write_external_storage_body))
                    .setCancelable(false)
                    .setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //
                        }
                    })
                    .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
                        }
                    })
                    .show();
        } else{
            if(getJsonType(editJsonViewer.getText().toString()) != JSON_ERROR) {
                textJsonViewer.setVisibility(View.GONE);
                layoutEditViewer.setVisibility(View.VISIBLE);
            } else{
                Toast.makeText(this, getString(R.string.json_file_parse_error_edited), Toast.LENGTH_SHORT).show();
            }
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
            case REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(getJsonType(editJsonViewer.getText().toString()) != JSON_ERROR) {
                        textJsonViewer.setVisibility(View.GONE);
                        layoutEditViewer.setVisibility(View.VISIBLE);
                    } else{
                        Toast.makeText(this, getString(R.string.json_file_parse_error_edited), Toast.LENGTH_SHORT).show();
                    }
                } else{
                    //
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            int result;
            Intent data = new Intent();
            if(isUpdated) {
                Bundle bundle = new Bundle();
                bundle.putInt("index", getIntent().getIntExtra("index", -1));
                data.putExtras(bundle);
                result = RESULT_OK;
            } else{
                result = RESULT_CANCELED;
            }
            setResult(result, data);

            finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

}
